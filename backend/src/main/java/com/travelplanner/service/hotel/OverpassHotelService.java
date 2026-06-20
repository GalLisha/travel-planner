package com.travelplanner.service.hotel;

import com.travelplanner.http.JsonUtil;
import com.travelplanner.model.GeoPoint;
import com.travelplanner.model.Hotel;
import com.travelplanner.util.SimpleHttpClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

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
    private static final int SEARCH_RADIUS_METERS = 5000;
    private static final int MAX_RESULTS = 30;
    private static final double EARTH_RADIUS_KM = 6371.0;

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
        hotel.setName(tags.get("name"));
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
