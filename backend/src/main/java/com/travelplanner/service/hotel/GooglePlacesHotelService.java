package com.travelplanner.service.hotel;

import com.travelplanner.http.JsonUtil;
import com.travelplanner.model.GeoPoint;
import com.travelplanner.model.Hotel;
import com.travelplanner.util.SimpleHttpClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Real {@link HotelService} backed by the Google Places API "Nearby Search" endpoint
 * (type=lodging). Activated automatically by {@code Main} when a GOOGLE_PLACES_API_KEY
 * environment variable is present - no other code changes are required to go live.
 */
public class GooglePlacesHotelService implements HotelService {

    private static final String NEARBY_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json";
    private static final String FIND_PLACE_URL = "https://maps.googleapis.com/maps/api/place/findplacefromtext/json";
    private static final String PHOTO_URL = "https://maps.googleapis.com/maps/api/place/photo";
    private static final int SEARCH_RADIUS_METERS = 5000;
    private static final int LOOKUP_BIAS_RADIUS_METERS = 50000;
    private static final double EARTH_RADIUS_KM = 6371.0;

    private final String apiKey;

    public GooglePlacesHotelService(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public List<Hotel> searchHotels(HotelSearchQuery query) {
        String url = NEARBY_SEARCH_URL
                + "?location=" + query.getLatitude() + "," + query.getLongitude()
                + "&radius=" + SEARCH_RADIUS_METERS
                + "&type=lodging"
                + "&language=en"
                + "&key=" + SimpleHttpClient.encode(apiKey);

        try {
            Map<String, String> headers = new LinkedHashMap<>();
            String json = SimpleHttpClient.getJson(url, headers, 5000, 8000);
            PlacesResponse response = JsonUtil.GSON.fromJson(json, PlacesResponse.class);

            if (response.results == null) {
                return new ArrayList<>();
            }
            List<Hotel> hotels = new ArrayList<>();
            for (PlaceResult place : response.results) {
                hotels.add(toHotel(place, query));
            }
            return hotels;
        } catch (IOException e) {
            throw new RuntimeException("Hotel search is temporarily unavailable: " + e.getMessage(), e);
        }
    }

    private Hotel toHotel(PlaceResult place, HotelSearchQuery query) {
        Hotel hotel = new Hotel();
        hotel.setId(place.place_id);
        hotel.setName(place.name);
        hotel.setStarRating(place.rating);
        hotel.setAddress(place.vicinity);
        hotel.setCurrency("USD");

        double[] priceRange = priceLevelToRange(place.price_level);
        hotel.setMinPricePerNight(priceRange[0]);
        hotel.setMaxPricePerNight(priceRange[1]);

        double lat = place.geometry != null && place.geometry.location != null ? place.geometry.location.lat : query.getLatitude();
        double lon = place.geometry != null && place.geometry.location != null ? place.geometry.location.lng : query.getLongitude();
        hotel.setLocation(new GeoPoint(lat, lon));
        hotel.setDistanceFromCenterKm(distanceKm(query.getLatitude(), query.getLongitude(), lat, lon));

        if (place.photos != null && place.photos.length > 0) {
            hotel.setImageUrl(PHOTO_URL + "?maxwidth=480&photoreference="
                    + SimpleHttpClient.encode(place.photos[0].photo_reference) + "&key=" + SimpleHttpClient.encode(apiKey));
        }
        return hotel;
    }

    @Override
    public Optional<Hotel> lookupByName(String name, HotelSearchQuery query) {
        String input = name + ", " + query.getCityName();
        String url = FIND_PLACE_URL
                + "?input=" + SimpleHttpClient.encode(input)
                + "&inputtype=textquery"
                + "&fields=place_id,name,formatted_address,geometry,rating"
                + "&locationbias=circle:" + LOOKUP_BIAS_RADIUS_METERS + "@" + query.getLatitude() + "," + query.getLongitude()
                + "&language=en"
                + "&key=" + SimpleHttpClient.encode(apiKey);

        try {
            Map<String, String> headers = new LinkedHashMap<>();
            String json = SimpleHttpClient.getJson(url, headers, 5000, 8000);
            FindPlaceResponse response = JsonUtil.GSON.fromJson(json, FindPlaceResponse.class);
            if (response.candidates == null || response.candidates.length == 0) {
                return Optional.empty();
            }
            return Optional.of(toHotel(response.candidates[0], query));
        } catch (IOException e) {
            throw new RuntimeException("Hotel lookup is temporarily unavailable: " + e.getMessage(), e);
        }
    }

    private Hotel toHotel(Candidate candidate, HotelSearchQuery query) {
        Hotel hotel = new Hotel();
        hotel.setId(candidate.place_id);
        hotel.setName(candidate.name);
        hotel.setStarRating(candidate.rating);
        hotel.setAddress(candidate.formatted_address);

        double lat = candidate.geometry != null && candidate.geometry.location != null ? candidate.geometry.location.lat : query.getLatitude();
        double lon = candidate.geometry != null && candidate.geometry.location != null ? candidate.geometry.location.lng : query.getLongitude();
        hotel.setLocation(new GeoPoint(lat, lon));
        hotel.setDistanceFromCenterKm(distanceKm(query.getLatitude(), query.getLongitude(), lat, lon));
        return hotel;
    }

    private static double[] priceLevelToRange(Integer priceLevel) {
        if (priceLevel == null) {
            return new double[]{0, 0};
        }
        switch (priceLevel) {
            case 0: return new double[]{0, 50};
            case 1: return new double[]{50, 100};
            case 2: return new double[]{100, 180};
            case 3: return new double[]{180, 300};
            case 4: return new double[]{300, 500};
            default: return new double[]{0, 0};
        }
    }

    private static double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double h = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return EARTH_RADIUS_KM * 2 * Math.atan2(Math.sqrt(h), Math.sqrt(1 - h));
    }

    private static class PlacesResponse {
        PlaceResult[] results;
    }

    private static class FindPlaceResponse {
        Candidate[] candidates;
    }

    private static class Candidate {
        String place_id;
        String name;
        String formatted_address;
        Double rating;
        Geometry geometry;
    }

    private static class PlaceResult {
        String place_id;
        String name;
        Double rating;
        Integer price_level;
        String vicinity;
        Geometry geometry;
        Photo[] photos;
    }

    private static class Geometry {
        LatLng location;
    }

    private static class LatLng {
        double lat;
        double lng;
    }

    private static class Photo {
        String photo_reference;
    }
}
