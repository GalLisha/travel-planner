package com.travelplanner.service;

import com.travelplanner.dto.request.DestinationSuggestionRequestDto;
import com.travelplanner.dto.response.DestinationDto;
import com.travelplanner.model.ActivityTag;
import com.travelplanner.model.BudgetLevel;
import com.travelplanner.model.Destination;
import com.travelplanner.model.Region;
import com.travelplanner.model.TravelGroupType;
import com.travelplanner.model.VacationStyle;
import com.travelplanner.repository.DestinationRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Scores and ranks destinations against the traveler's stated preferences. */
public class DestinationRecommendationService {

    private static final int MAX_RESULTS = 6;

    private final DestinationRepository destinationRepository;

    public DestinationRecommendationService(DestinationRepository destinationRepository) {
        this.destinationRepository = destinationRepository;
    }

    public List<DestinationDto> suggest(DestinationSuggestionRequestDto request) {
        TravelGroupType groupType = parseEnum(TravelGroupType.class, request.getTravelGroupType());
        BudgetLevel budget = parseEnum(BudgetLevel.class, request.getBudgetLevel());
        Region region = parseEnum(Region.class, request.getRegion());
        Integer maxFlightHours = request.getMaxFlightDurationHours();

        List<Destination> candidates = new ArrayList<>();
        for (Destination destination : destinationRepository.findAll()) {
            if (maxFlightHours != null && destination.getAvgFlightDurationHours() > maxFlightHours) {
                continue;
            }
            if (region != null && destination.getRegion() != region) {
                continue;
            }
            candidates.add(destination);
        }

        List<DestinationDto> scored = new ArrayList<>();
        for (Destination destination : candidates) {
            double score = score(destination, request, groupType, budget);
            scored.add(DestinationDto.fromModel(destination, score));
        }

        scored.sort(Comparator.comparingDouble(DestinationDto::getMatchScore).reversed());

        return scored.size() > MAX_RESULTS ? scored.subList(0, MAX_RESULTS) : scored;
    }

    private double score(Destination destination, DestinationSuggestionRequestDto request,
                          TravelGroupType groupType, BudgetLevel budget) {
        double score = 0;

        if (groupType != null) {
            switch (groupType) {
                case FAMILY:
                    score += destination.isFamilyFriendly() ? 3 : 0;
                    break;
                case COUPLE:
                    score += destination.isRomantic() ? 3 : 0;
                    break;
                case FRIENDS:
                    score += destination.isNightlife() ? 3 : 0;
                    break;
            }
        }

        if (budget != null && budget == destination.getBudgetLevel()) {
            score += 2;
        }

        if (request.getVacationStyles() != null) {
            for (String styleName : request.getVacationStyles()) {
                VacationStyle style = parseEnum(VacationStyle.class, styleName);
                if (style != null && destination.getVacationStyles().contains(style)) {
                    score += 1;
                }
            }
        }

        if (request.getActivities() != null) {
            for (String activityName : request.getActivities()) {
                ActivityTag tag = parseEnum(ActivityTag.class, activityName);
                if (tag != null && destination.getActivityTags().contains(tag)) {
                    score += 1;
                }
            }
        }

        if (request.getAdditionalPreferences() != null && !request.getAdditionalPreferences().trim().isEmpty()) {
            String haystack = (destination.getName() + " " + destination.getCountry() + " " +
                    destination.getDescription()).toLowerCase(Locale.ROOT);
            for (String keyword : request.getAdditionalPreferences().toLowerCase(Locale.ROOT).split("[,\\s]+")) {
                if (keyword.length() > 2 && haystack.contains(keyword)) {
                    score += 1;
                }
            }
        }

        return score;
    }

    private static <T extends Enum<T>> T parseEnum(Class<T> type, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Enum.valueOf(type, value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
