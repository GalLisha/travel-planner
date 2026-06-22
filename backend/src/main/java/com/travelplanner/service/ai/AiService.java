package com.travelplanner.service.ai;

import com.travelplanner.model.TravelGroupType;

import java.util.List;
import java.util.Optional;

/**
 * Optional AI-powered supplement used across three call sites: hotel-search fallback when
 * a real provider returns nothing, hotel-name validation as a last resort, and personalized
 * attraction recommendations for a given travel group type. {@link NullAiService} is used
 * when no provider key is configured, so every caller can hold a non-null {@code AiService}
 * with no null-checks - every method is guaranteed to never throw and to degrade to an
 * empty/absent result on any failure (timeout, malformed response, disabled).
 */
public interface AiService {

    boolean isEnabled();

    List<AiHotelSuggestion> suggestHotels(String cityName, String countryName, int maxResults);

    Optional<AiHotelLookupResult> assessHotelExistence(String hotelName, String cityName, String countryName);

    List<AiAttractionSuggestion> suggestAttractions(String destinationName, String countryName,
                                                      TravelGroupType groupType, int maxResults);
}
