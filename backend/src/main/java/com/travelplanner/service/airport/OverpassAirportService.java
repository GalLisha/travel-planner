package com.travelplanner.service.airport;

import com.travelplanner.http.JsonUtil;
import com.travelplanner.model.Airport;
import com.travelplanner.model.GeoPoint;
import com.travelplanner.util.SimpleHttpClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Real, keyless {@link AirportService} backed by OpenStreetMap data via the free Overpass
 * API - the same approach as {@code OverpassHotelService}. Only aerodromes tagged with an
 * "iata" code are returned, which filters out small airfields and keeps results to
 * commercial airports. Searched within a wide radius since a city's nearest commercial
 * airport is often well outside the city itself (e.g. Paris Beauvais, ~75km from Paris).
 */
public class OverpassAirportService implements AirportService {

    private static final String OVERPASS_URL = "https://overpass-api.de/api/interpreter";
    private static final int SEARCH_RADIUS_METERS = 150_000;
    private static final int MAX_RESULTS = 15;
    private static final double EARTH_RADIUS_KM = 6371.0;

    @Override
    public List<Airport> findNear(double latitude, double longitude) {
        String ql = "[out:json][timeout:25];"
                + "("
                + "node[\"aeroway\"=\"aerodrome\"][\"iata\"](around:" + SEARCH_RADIUS_METERS + ","
                + latitude + "," + longitude + ");"
                + "way[\"aeroway\"=\"aerodrome\"][\"iata\"](around:" + SEARCH_RADIUS_METERS + ","
                + latitude + "," + longitude + ");"
                + ");"
                + "out center " + MAX_RESULTS + ";";
        String url = OVERPASS_URL + "?data=" + SimpleHttpClient.encode(ql);

        try {
            String json = SimpleHttpClient.getJson(url, new LinkedHashMap<>(), 5000, 15000);
            OverpassResponse response = JsonUtil.GSON.fromJson(json, OverpassResponse.class);

            List<Airport> airports = new ArrayList<>();
            if (response.elements != null) {
                for (Element element : response.elements) {
                    Airport airport = toAirport(element, latitude, longitude);
                    if (airport != null) {
                        airports.add(airport);
                    }
                }
            }
            airports.sort(Comparator.comparingDouble(Airport::getDistanceFromCityCenterKm));
            return airports;
        } catch (IOException e) {
            throw new RuntimeException("Airport search is temporarily unavailable: " + e.getMessage(), e);
        }
    }

    private Airport toAirport(Element element, double cityLat, double cityLon) {
        Map<String, String> tags = element.tags;
        if (tags == null || tags.get("name") == null || tags.get("iata") == null) {
            return null;
        }

        double lat = element.lat != null ? element.lat : (element.center != null ? element.center.lat : cityLat);
        double lon = element.lon != null ? element.lon : (element.center != null ? element.center.lon : cityLon);

        Airport airport = new Airport();
        airport.setName(tags.get("name:en") != null ? tags.get("name:en") : tags.get("name"));
        airport.setIataCode(tags.get("iata"));
        airport.setLocation(new GeoPoint(lat, lon));
        airport.setDistanceFromCityCenterKm(distanceKm(cityLat, cityLon, lat, lon));
        return airport;
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

    private static class Element {
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
