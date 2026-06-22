package com.travelplanner.service.ai;

import com.travelplanner.model.TravelGroupType;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/** No-op {@link AiService} used when no AI provider key is configured. */
public class NullAiService implements AiService {

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public List<AiHotelSuggestion> suggestHotels(String cityName, String countryName, int maxResults) {
        return Collections.emptyList();
    }

    @Override
    public Optional<AiHotelLookupResult> assessHotelExistence(String hotelName, String cityName, String countryName) {
        return Optional.empty();
    }

    @Override
    public List<AiAttractionSuggestion> suggestAttractions(String destinationName, String countryName,
                                                             TravelGroupType groupType, int maxResults) {
        return Collections.emptyList();
    }
}
