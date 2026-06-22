package com.travelplanner.service.hotel;

import com.travelplanner.http.JsonUtil;
import com.travelplanner.model.GeoPoint;
import com.travelplanner.model.Hotel;
import com.travelplanner.util.SimpleHttpClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Real, keyless {@link HotelService} backed by OpenStreetMap data via the free
 * Overpass API - the same kind of zero-config, no-API-key approach already used for
 * city search (see {@code PhotonCityService}). Returns real names/addresses/locations;
 * OSM has no reliable pricing or photos for lodging, and star ratings are only present
 * when an establishment happens to be tagged with "stars". The frontend already treats
 * those fields as optional, so leaving them null just means fewer badges, not a broken UI.
 */
public class OverpassHotelService implements HotelService {

    private static final String OVERPASS_URL = "https://overpass-api.de/api/interpreter";
    private static final String PHOTON_URL = "https://photon.komoot.io/api/";
    private static final int SEARCH_RADIUS_METERS = 5000;
    private static final int MAX_RESULTS = 30;
    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double LOOKUP_MAX_DISTANCE_KM = 50.0;

    @Override
    public List<Hotel> searchHotels(HotelSearchQuery query) {
        String ql = "[out:json][timeout:25];"
                + "("
                + "node[\"tourism\"~\"^(hotel|hostel|guest_house|motel)$\"](around:" + SEARCH_RADIUS_METERS + ","
                + query.getLatitude() + "," + query.getLongitude() + ");"
                + "way[\"tourism\"~\"^(hotel|hostel|guest_house|motel)$\"](around:" + SEARCH_RADIUS_METERS + ","
                + query.getLatitude() + "," + query.getLongitude() + ");"
                + ");"
                + "out center " + MAX_RESULTS + ";";
        String url = OVERPASS_URL + "?data=" + SimpleHttpClient.encode(ql);

        try {
            String json = SimpleHttpClient.getJson(url, new java.util.LinkedHashMap<>(), 5000, 15000);
            OverpassResponse response = JsonUtil.GSON.fromJson(json, OverpassResponse.class);

            List<Hotel> hotels = new ArrayList<>();
            if (response.elements != null) {
                for (Element element : response.elements) {
                    Hotel hotel = toHotel(element, query);
                    if (hotel != null) {
                        hotels.add(hotel);
                    }
                }
            }
            hotels.sort(Comparator.comparingDouble(Hotel::getDistanceFromCenterKm));
            return hotels;
        } catch (IOException e) {
            throw new RuntimeException("Hotel search is temporarily unavailable: " + e.getMessage(), e);
        }
    }

    private Hotel toHotel(Element element, HotelSearchQuery query) {
        Map<String, String> tags = element.tags;
        if (tags == null || tags.get("name") == null) {
            return null;
        }

        double lat = element.lat != null ? element.lat : (element.center != null ? element.center.lat : query.getLatitude());
        double lon = element.lon != null ? element.lon : (element.center != null ? element.center.lon : query.getLongitude());

        Hotel hotel = new Hotel();
        hotel.setId(element.type + "/" + element.id);
        hotel.setName(tags.get("name:en") != null ? tags.get("name:en") : tags.get("name"));
        hotel.setStarRating(parseStars(tags.get("stars")));
        hotel.setMinPricePerNight(null);
        hotel.setMaxPricePerNight(null);
        hotel.setCurrency(null);
        hotel.setAddress(buildAddress(tags, query));
        hotel.setLocation(new GeoPoint(lat, lon));
        hotel.setDistanceFromCenterKm(distanceKm(query.getLatitude(), query.getLongitude(), lat, lon));
        hotel.setImageUrl(null);
        return hotel;
    }

    /**
     * No API key is configured, so there's no Google Places lookup available here -
     * this falls back to Photon (the same free, keyless OSM geocoder used for city
     * search) as the "search the web for this name" step for manually-typed hotels.
     */
    @Override
    public Optional<Hotel> lookupByName(String name, HotelSearchQuery query) {
        String url = PHOTON_URL + "?lang=en&limit=5"
                + "&lat=" + query.getLatitude() + "&lon=" + query.getLongitude()
                + "&q=" + SimpleHttpClient.encode(name);

        try {
            String json = SimpleHttpClient.getJson(url, new LinkedHashMap<>(), 5000, 8000);
            PhotonResponse response = JsonUtil.GSON.fromJson(json, PhotonResponse.class);
            if (response.features == null) {
                return Optional.empty();
            }
            for (PhotonFeature feature : response.features) {
                PhotonProperties p = feature.properties;
                if (p == null || p.name == null || feature.geometry == null || feature.geometry.coordinates == null
                        || feature.geometry.coordinates.length != 2) {
                    continue;
                }
                double lon = feature.geometry.coordinates[0];
                double lat = feature.geometry.coordinates[1];
                double distance = distanceKm(query.getLatitude(), query.getLongitude(), lat, lon);
                if (distance > LOOKUP_MAX_DISTANCE_KM) {
                    continue;
                }
                Hotel hotel = new Hotel();
                hotel.setId("photon/" + lat + "/" + lon);
                hotel.setName(p.name);
                hotel.setAddress(buildPhotonAddress(p, query));
                hotel.setLocation(new GeoPoint(lat, lon));
                hotel.setDistanceFromCenterKm(distance);
                return Optional.of(hotel);
            }
            return Optional.empty();
        } catch (IOException e) {
            throw new RuntimeException("Hotel lookup is temporarily unavailable: " + e.getMessage(), e);
        }
    }

    private static String buildPhotonAddress(PhotonProperties p, HotelSearchQuery query) {
        StringBuilder address = new StringBuilder();
        if (p.street != null) {
            address.append(p.street);
            if (p.housenumber != null) {
                address.append(" ").append(p.housenumber);
            }
        }
        if (address.length() > 0) {
            address.append(", ");
        }
        address.append(p.city != null ? p.city : query.getCityName());
        return address.toString();
    }

    private static Double parseStars(String stars) {
        if (stars == null) {
            return null;
        }
        try {
            return Double.parseDouble(stars);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String buildAddress(Map<String, String> tags, HotelSearchQuery query) {
        String houseNumber = tags.get("addr:housenumber");
        String street = tags.get("addr:street");
        if (street != null) {
            return houseNumber != null ? street + " " + houseNumber : street;
        }
        return query.getCityName() + ", " + query.getCountryName();
    }

    private static double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double h = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return EARTH_RADIUS_KM * 2 * Math.atan2(Math.sqrt(h), Math.sqrt(1 - h));
    }

    private static class OverpassResponse {
        Element[] elements;
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
        String street;
        String housenumber;
        String city;
    }

    private static class PhotonGeometry {
        double[] coordinates;
    }

    private static class Element {
        String type;
        long id;
        Double lat;
        Double lon;
        Center center;
        Map<String, String> tags;
    }

    private static class Center {
        double lat;
        double lon;
    }
}
