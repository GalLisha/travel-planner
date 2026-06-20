package com.travelplanner.service.hotel;

import com.travelplanner.model.GeoPoint;
import com.travelplanner.model.Hotel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Zero-config default {@link HotelService}: generates a plausible, deterministic
 * set of hotels anchored near the requested city's coordinates. Deterministic per
 * city (same city always returns the same list) so the UI feels stable rather than
 * randomly reshuffling on every search. Stands in until a real provider key
 * (see {@link GooglePlacesHotelService}) is configured.
 */
public class MockHotelService implements HotelService {

    private static final String[] PREFIXES = {
            "Grand", "Royal", "Park", "Central", "Garden", "Imperial", "Riviera", "Heritage", "Skyline", "Harbor"
    };
    private static final String[] SUFFIXES = {
            "Hotel", "Suites", "Inn", "Resort", "Boutique Hotel", "Plaza"
    };
    private static final double EARTH_RADIUS_KM = 6371.0;

    @Override
    public List<Hotel> searchHotels(HotelSearchQuery query) {
        Random random = new Random(seedFor(query.getCityName(), query.getCountryName()));
        List<Hotel> hotels = new ArrayList<>();
        int count = 6 + random.nextInt(3);

        for (int i = 0; i < count; i++) {
            String prefix = PREFIXES[random.nextInt(PREFIXES.length)];
            String suffix = SUFFIXES[random.nextInt(SUFFIXES.length)];
            String name = String.format(Locale.ROOT, "%s %s %s", prefix, query.getCityName(), suffix);

            double starRating = 2.5 + random.nextInt(6) * 0.5; // 2.5 .. 5.0
            double basePrice = 40 + (starRating - 2.5) * 70 + random.nextInt(40);
            double minPrice = Math.round(basePrice);
            double maxPrice = Math.round(basePrice + 30 + random.nextInt(60));

            double jitterLat = (random.nextDouble() - 0.5) * 0.05;
            double jitterLon = (random.nextDouble() - 0.5) * 0.05;
            GeoPoint location = new GeoPoint(query.getLatitude() + jitterLat, query.getLongitude() + jitterLon);

            Hotel hotel = new Hotel();
            hotel.setId("mock-hotel-" + i + "-" + Math.abs(name.hashCode()));
            hotel.setName(name);
            hotel.setStarRating(Math.round(starRating * 10) / 10.0);
            hotel.setMinPricePerNight(minPrice);
            hotel.setMaxPricePerNight(maxPrice);
            hotel.setCurrency("USD");
            hotel.setDistanceFromCenterKm(distanceKm(query.getLatitude(), query.getLongitude(),
                    location.getLatitude(), location.getLongitude()));
            hotel.setImageUrl(null);
            hotel.setAddress(query.getCityName() + ", " + query.getCountryName());
            hotel.setLocation(location);
            hotels.add(hotel);
        }
        return hotels;
    }

    private static long seedFor(String cityName, String countryName) {
        String key = (cityName == null ? "" : cityName) + "|" + (countryName == null ? "" : countryName);
        return key.toLowerCase(Locale.ROOT).hashCode();
    }

    private static double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double h = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return EARTH_RADIUS_KM * 2 * Math.atan2(Math.sqrt(h), Math.sqrt(1 - h));
    }
}
