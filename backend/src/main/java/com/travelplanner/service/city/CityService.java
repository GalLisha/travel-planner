package com.travelplanner.service.city;

import com.travelplanner.model.City;

import java.util.List;

/**
 * Abstraction over a city search/geocoding provider. Swapping the provider
 * (e.g. from OpenStreetMap Nominatim to GeoDB Cities) only requires a new
 * implementation of this interface - no caller-side changes.
 */
public interface CityService {
    /**
     * @param query free-text city search term (required)
     * @param countryCode optional ISO 3166-1 alpha-2 code to restrict results to one country
     */
    List<City> search(String query, String countryCode);
}
