package com.travelplanner.service.city;

import com.travelplanner.http.JsonUtil;
import com.travelplanner.model.City;
import com.travelplanner.model.GeoPoint;
import com.travelplanner.util.SimpleHttpClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * City search backed by the free, keyless Photon geocoder (https://photon.komoot.io),
 * built on OpenStreetMap data specifically for autocomplete/typeahead use cases - unlike
 * Nominatim's public API, whose usage policy explicitly disallows search-as-you-type
 * traffic. Photon does prefix matching (e.g. "par" matches "Paris"), which is what makes
 * real typeahead possible here.
 */
public class PhotonCityService implements CityService {

    private static final String BASE_URL = "https://photon.komoot.io/api/";
    private static final long CACHE_TTL_MS = 5 * 60 * 1000;
    private static final Set<String> SETTLEMENT_TYPES = new HashSet<>(Arrays.asList(
            "city", "town", "village", "hamlet", "municipality", "borough", "suburb", "quarter", "locality"));

    // Some major world cities (e.g. Tokyo) are mapped in OSM at the administrative
    // province/state level, and popular island destinations (e.g. Santorini, Bali's
    // own island entry) are mapped as place=island/archipelago rather than a simple
    // settlement node. These are accepted too, but ranked after genuine settlement matches.
    private static final Set<String> SECONDARY_TYPES = new HashSet<>(Arrays.asList(
            "province", "state", "island", "archipelago"));

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    @Override
    public List<City> search(String query, String countryCode) {
        if (query == null || query.trim().length() < 2) {
            return new ArrayList<>();
        }
        String normalizedQuery = query.trim();
        String normalizedCountry = (countryCode == null || countryCode.trim().isEmpty())
                ? "" : countryCode.trim().toUpperCase(Locale.ROOT);
        String cacheKey = normalizedQuery.toLowerCase(Locale.ROOT) + "|" + normalizedCountry;

        CacheEntry cached = cache.get(cacheKey);
        if (cached != null && (System.currentTimeMillis() - cached.cachedAtMillis) < CACHE_TTL_MS) {
            return cached.cities;
        }

        List<City> cities = fetchFromPhoton(normalizedQuery, normalizedCountry);
        cache.put(cacheKey, new CacheEntry(cities));
        return cities;
    }

    private List<City> fetchFromPhoton(String query, String countryCode) {
        // Fetch a generous raw pool: non-place results, duplicates, and country
        // filtering all shrink it before the final top-10 cap, and important matches
        // (e.g. islands like Santorini) can rank below a wave of similarly-named towns.
        int limit = countryCode.isEmpty() ? 25 : 35;
        String url = BASE_URL + "?lang=en&osm_tag=place&limit=" + limit + "&q=" + SimpleHttpClient.encode(query);

        try {
            String json = SimpleHttpClient.getJson(url, new LinkedHashMap<>(), 5000, 8000);
            PhotonResponse response = JsonUtil.GSON.fromJson(json, PhotonResponse.class);
            return toCities(response, countryCode);
        } catch (IOException e) {
            throw new RuntimeException("City search is temporarily unavailable: " + e.getMessage(), e);
        }
    }

    private List<City> toCities(PhotonResponse response, String countryCode) {
        List<City> result = new ArrayList<>();
        if (response == null || response.features == null) {
            return result;
        }
        Set<String> seen = new HashSet<>();

        // Kept in Photon's own relevance order rather than re-sorted by place type:
        // Photon already accounts for population/importance, so e.g. "Bali, Indonesia"
        // (a state-level match) correctly outranks an unrelated small town also named
        // Bali - forcing all settlement-type matches first would undo that.
        for (PhotonFeature feature : response.features) {
            PhotonProperties p = feature.properties;
            if (p == null || p.name == null || p.country == null || p.osm_value == null) {
                continue;
            }
            if (!SETTLEMENT_TYPES.contains(p.osm_value) && !SECONDARY_TYPES.contains(p.osm_value)) {
                continue;
            }
            if (!countryCode.isEmpty() && (p.countrycode == null || !p.countrycode.equalsIgnoreCase(countryCode))) {
                continue;
            }

            String dedupeKey = (p.name + "|" + p.country).toLowerCase(Locale.ROOT);
            if (!seen.add(dedupeKey)) {
                continue;
            }

            double lon = 0;
            double lat = 0;
            if (feature.geometry != null && feature.geometry.coordinates != null && feature.geometry.coordinates.length == 2) {
                lon = feature.geometry.coordinates[0];
                lat = feature.geometry.coordinates[1];
            }

            result.add(new City(
                    p.name,
                    p.country,
                    p.countrycode == null ? null : p.countrycode.toUpperCase(Locale.ROOT),
                    p.state,
                    new GeoPoint(lat, lon),
                    p.population));
            if (result.size() >= 10) {
                break;
            }
        }
        return result;
    }

    private static class CacheEntry {
        final List<City> cities;
        final long cachedAtMillis = System.currentTimeMillis();

        CacheEntry(List<City> cities) {
            this.cities = cities;
        }
    }

    private static class PhotonResponse {
        PhotonFeature[] features;
    }

    private static class PhotonFeature {
        PhotonProperties properties;
        PhotonGeometry geometry;
    }

    private static class PhotonProperties {
        String name;
        String country;
        String countrycode;
        String state;
        String osm_value;
        Long population;
    }

    private static class PhotonGeometry {
        double[] coordinates;
    }
}
