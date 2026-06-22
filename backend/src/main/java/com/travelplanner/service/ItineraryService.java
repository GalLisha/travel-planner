package com.travelplanner.service;

import com.travelplanner.dto.request.GenerateItineraryRequestDto;
import com.travelplanner.dto.request.ReplaceAttractionRequestDto;
import com.travelplanner.exception.BadRequestException;
import com.travelplanner.exception.NotFoundException;
import com.travelplanner.model.ActivityTag;
import com.travelplanner.model.Attraction;
import com.travelplanner.model.BudgetLevel;
import com.travelplanner.model.Destination;
import com.travelplanner.model.GeoPoint;
import com.travelplanner.model.Itinerary;
import com.travelplanner.model.ItineraryDay;
import com.travelplanner.model.ItineraryItem;
import com.travelplanner.model.TransferLeg;
import com.travelplanner.model.TravelGroupType;
import com.travelplanner.repository.AttractionRepository;
import com.travelplanner.repository.DestinationRepository;
import com.travelplanner.repository.ItineraryRepository;
import com.travelplanner.service.ai.AiAttractionSuggestion;
import com.travelplanner.service.ai.AiService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Builds and recalculates day-by-day itineraries: selects attractions suited
 * to the travel group, distributes them across the trip's days, orders each
 * day's stops, and derives arrival/departure times from estimated travel
 * times and visit durations.
 */
public class ItineraryService {

    private static final int DAY_START_MINUTES = 9 * 60 + 30;
    private static final int ARRIVAL_DAY_START_MINUTES = 15 * 60;
    private static final int DAY_END_MINUTES = 21 * 60;
    private static final int DEPARTURE_DAY_END_MINUTES = 13 * 60;
    private static final int MAX_ITEMS_PER_DAY = 4;

    private static final double MAX_ATTRACTION_DISTANCE_FROM_CENTER_KM = 50.0;
    private static final int AI_ATTRACTION_SUGGESTION_COUNT = 8;
    private static final int DEFAULT_AI_ATTRACTION_VISIT_MINUTES = 90;

    private final DestinationRepository destinationRepository;
    private final AttractionRepository attractionRepository;
    private final ItineraryRepository itineraryRepository;
    private final RouteOptimizationService routeOptimizationService;
    private final AiService aiService;

    public ItineraryService(DestinationRepository destinationRepository,
                             AttractionRepository attractionRepository,
                             ItineraryRepository itineraryRepository,
                             RouteOptimizationService routeOptimizationService,
                             AiService aiService) {
        this.destinationRepository = destinationRepository;
        this.attractionRepository = attractionRepository;
        this.itineraryRepository = itineraryRepository;
        this.routeOptimizationService = routeOptimizationService;
        this.aiService = aiService;
    }

    public Itinerary generateItinerary(GenerateItineraryRequestDto request) {
        Destination destination = resolveDestination(request);
        TravelGroupType groupType = parseGroupType(request.getTravelGroupType());
        LocalDate departureDate = parseDate(request.getDepartureDate(), "departureDate");
        LocalDate returnDate = parseDate(request.getReturnDate(), "returnDate");

        long totalDays = ChronoUnit.DAYS.between(departureDate, returnDate) + 1;
        if (totalDays < 1) {
            throw new BadRequestException("returnDate must be on or after departureDate");
        }

        // Prefer the real hotel's coordinates (from city/hotel search) as the anchor
        // point for distance/time calculations; fall back to the destination's own
        // coordinates when no hotel location is available (e.g. manually-entered hotel).
        GeoPoint hotelLocation = (request.getHotelLatitude() != null && request.getHotelLongitude() != null)
                ? new GeoPoint(request.getHotelLatitude(), request.getHotelLongitude())
                : destination.getLocation();

        // Curated attractions exist for only 10 destinations; for everything else (and to
        // personalize even curated ones) the AI service - when configured - supplements
        // the pool with attractions tailored to the chosen travel group type.
        List<Attraction> pool = rankedAttractionsFor(destination, groupType);

        // Real flight times + an arrival airport let day 1/the last day be anchored to the
        // actual flight schedule (plus a calculated transfer leg) instead of the generic
        // hardcoded windows below; absent that info, behavior is unchanged from before.
        GeoPoint airportLocation = (request.getArrivalAirportLatitude() != null && request.getArrivalAirportLongitude() != null)
                ? new GeoPoint(request.getArrivalAirportLatitude(), request.getArrivalAirportLongitude())
                : null;
        String transferMode = request.getTransferMode() != null ? request.getTransferMode().trim().toUpperCase(Locale.ROOT) : "TAXI";
        String airportLabel = request.getArrivalAirportName() != null ? request.getArrivalAirportName() : "Airport";
        String hotelLabel = request.getHotelName() != null ? request.getHotelName() : "Hotel";

        Integer day1StartOverride = null;
        TransferLeg arrivalTransferLeg = null;
        Integer lastDayEndCutoffOverride = null;
        TransferLeg departureTransferLeg = null;

        if (airportLocation != null) {
            double transferDistance = routeOptimizationService.distanceKm(airportLocation, hotelLocation);
            int transferMinutes = routeOptimizationService.airportTransferTimeMinutes(transferDistance, transferMode);

            Integer flightArrivalMinutes = parseTimeToMinutes(request.getArrivalTime(), "arrivalTime");
            if (flightArrivalMinutes != null) {
                day1StartOverride = flightArrivalMinutes + transferMinutes;
                arrivalTransferLeg = buildTransferLeg(airportLabel, hotelLabel, transferMode, transferDistance,
                        transferMinutes, flightArrivalMinutes, day1StartOverride);
            }

            Integer flightDepartureMinutes = parseTimeToMinutes(request.getDepartureTime(), "departureTime");
            if (flightDepartureMinutes != null) {
                int mustArriveAtAirportBy = flightDepartureMinutes - RouteOptimizationService.AIRPORT_CHECKIN_BUFFER_MINUTES;
                lastDayEndCutoffOverride = mustArriveAtAirportBy - transferMinutes;
                departureTransferLeg = buildTransferLeg(hotelLabel, airportLabel, transferMode, transferDistance,
                        transferMinutes, lastDayEndCutoffOverride, mustArriveAtAirportBy);
            }
        }

        Itinerary itinerary = new Itinerary();
        itinerary.setId(UUID.randomUUID().toString());
        itinerary.setDestinationId(destination.getId());
        itinerary.setDestinationName(destination.getName());
        itinerary.setDestinationCountry(destination.getCountry());
        itinerary.setTravelGroupType(groupType);
        itinerary.setDepartureDate(request.getDepartureDate());
        itinerary.setReturnDate(request.getReturnDate());
        itinerary.setHotelName(request.getHotelName());
        itinerary.setHotelAddress(request.getHotelAddress());
        itinerary.setHotelLocation(hotelLocation);

        List<ItineraryDay> days = new ArrayList<>();
        int poolIndex = 0;
        for (int dayNumber = 1; dayNumber <= totalDays; dayNumber++) {
            boolean isFirstDay = dayNumber == 1;
            boolean isLastDay = dayNumber == totalDays;
            int startMinutes = isFirstDay && day1StartOverride != null ? day1StartOverride
                    : isFirstDay ? ARRIVAL_DAY_START_MINUTES : DAY_START_MINUTES;
            int endCutoff = isLastDay && lastDayEndCutoffOverride != null ? lastDayEndCutoffOverride
                    : isLastDay ? DEPARTURE_DAY_END_MINUTES : DAY_END_MINUTES;

            List<Attraction> dayAttractions = fillDayWithinBudget(pool, poolIndex, hotelLocation, startMinutes, endCutoff);
            poolIndex += dayAttractions.size();

            ItineraryDay day = buildDay(dayNumber, departureDate.plusDays(dayNumber - 1), dayAttractions,
                    hotelLocation, startMinutes);
            if (isFirstDay) {
                day.setArrivalTransfer(arrivalTransferLeg);
            }
            if (isLastDay) {
                day.setDepartureTransfer(departureTransferLeg);
            }
            days.add(day);
        }
        itinerary.setDays(days);

        itineraryRepository.save(itinerary);
        return itinerary;
    }

    /**
     * Resolves the trip's destination. If {@code destinationId} matches one of the curated
     * destinations, that (attraction-rich) destination is used as-is. Otherwise - e.g. a real
     * city selected via city search that isn't one of the 10 curated destinations - a
     * transient, non-persisted destination is built from the request's city/country/lat/lon
     * so the rest of the pipeline works unchanged, just with no attractions to schedule.
     */
    private Destination resolveDestination(GenerateItineraryRequestDto request) {
        if (request.getDestinationId() != null && !request.getDestinationId().trim().isEmpty()) {
            java.util.Optional<Destination> curated = destinationRepository.findById(request.getDestinationId());
            if (curated.isPresent()) {
                return curated.get();
            }
        }

        if (request.getCityName() == null || request.getLatitude() == null || request.getLongitude() == null) {
            throw new NotFoundException("Unknown destination: " + request.getDestinationId());
        }

        Destination virtual = new Destination();
        virtual.setId(request.getDestinationId() != null ? request.getDestinationId() : "city:" + request.getCityName());
        virtual.setName(request.getCityName());
        virtual.setCountry(request.getCountryName());
        virtual.setLocation(new GeoPoint(request.getLatitude(), request.getLongitude()));
        return virtual;
    }

    public Itinerary replaceAttraction(String itineraryId, ReplaceAttractionRequestDto request) {
        Itinerary itinerary = getItinerary(itineraryId);
        ItineraryDay day = itinerary.getDays().stream()
                .filter(d -> d.getDayNumber() == request.getDayNumber())
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Itinerary has no day number " + request.getDayNumber()));

        Attraction newAttraction = attractionRepository.findById(request.getNewAttractionId());
        if (newAttraction == null || !newAttraction.getDestinationId().equals(itinerary.getDestinationId())) {
            throw new BadRequestException("Replacement attraction does not belong to this destination");
        }

        Set<String> usedElsewhere = usedAttractionIds(itinerary, request.getDayNumber());
        if (usedElsewhere.contains(newAttraction.getId())) {
            throw new BadRequestException("That attraction is already scheduled elsewhere in this trip");
        }

        List<Attraction> dayAttractions = new ArrayList<>();
        boolean replaced = false;
        for (ItineraryItem item : day.getItems()) {
            if (!replaced && item.getAttraction().getId().equals(request.getOldAttractionId())) {
                dayAttractions.add(newAttraction);
                replaced = true;
            } else {
                dayAttractions.add(item.getAttraction());
            }
        }
        if (!replaced) {
            throw new NotFoundException("Day " + request.getDayNumber() + " does not contain attraction " +
                    request.getOldAttractionId());
        }

        boolean isFirstDay = day.getDayNumber() == 1;
        int startMinutes = isFirstDay && day.getArrivalTransfer() != null
                ? parseTimeToMinutes(day.getArrivalTransfer().getArrivalTime(), "arrivalTime")
                : isFirstDay ? ARRIVAL_DAY_START_MINUTES : DAY_START_MINUTES;
        ItineraryDay rebuilt = buildDay(day.getDayNumber(), LocalDate.parse(itinerary.getDepartureDate())
                .plusDays(day.getDayNumber() - 1), dayAttractions, itinerary.getHotelLocation(), startMinutes);
        rebuilt.setArrivalTransfer(day.getArrivalTransfer());
        rebuilt.setDepartureTransfer(day.getDepartureTransfer());

        int index = itinerary.getDays().indexOf(day);
        itinerary.getDays().set(index, rebuilt);
        itineraryRepository.save(itinerary);
        return itinerary;
    }

    public List<Attraction> findAlternatives(String itineraryId, int dayNumber, String attractionId) {
        Itinerary itinerary = getItinerary(itineraryId);
        Attraction current = attractionRepository.findById(attractionId);
        Set<String> usedElsewhere = usedAttractionIds(itinerary, dayNumber);

        Destination destination = destinationForAlternatives(itinerary);
        List<Attraction> candidates = rankedAttractionsFor(destination, itinerary.getTravelGroupType());
        candidates.removeIf(a -> usedElsewhere.contains(a.getId()) || a.getId().equals(attractionId));

        if (current != null) {
            ActivityTag sameCategory = current.getCategory();
            candidates.sort(Comparator.comparingInt((Attraction a) -> a.getCategory() == sameCategory ? 0 : 1));
        }
        return candidates;
    }

    public Itinerary getItinerary(String itineraryId) {
        return itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new NotFoundException("Unknown itinerary: " + itineraryId));
    }

    private Set<String> usedAttractionIds(Itinerary itinerary, int excludingDayNumber) {
        Set<String> used = new HashSet<>();
        for (ItineraryDay day : itinerary.getDays()) {
            if (day.getDayNumber() == excludingDayNumber) {
                continue;
            }
            for (ItineraryItem item : day.getItems()) {
                used.add(item.getAttraction().getId());
            }
        }
        return used;
    }

    /**
     * Greedily picks attractions (in pool order, starting at {@code fromIndex}) for a single day,
     * re-checking the running schedule after each addition so a day never overflows its time
     * window (e.g. two 6-hour theme parks won't get crammed into one evening).
     */
    private List<Attraction> fillDayWithinBudget(List<Attraction> pool, int fromIndex, GeoPoint hotelLocation,
                                                  int startMinutes, int endCutoffMinutes) {
        List<Attraction> chosen = new ArrayList<>();
        int index = fromIndex;
        while (index < pool.size() && chosen.size() < MAX_ITEMS_PER_DAY) {
            List<Attraction> trial = new ArrayList<>(chosen);
            trial.add(pool.get(index));
            List<Attraction> orderedTrial = routeOptimizationService.optimizeOrder(hotelLocation, trial);
            int finishMinutes = simulateFinishMinutes(startMinutes, hotelLocation, orderedTrial);

            if (finishMinutes > endCutoffMinutes && !chosen.isEmpty()) {
                break;
            }
            chosen.add(pool.get(index));
            index++;
            if (finishMinutes > endCutoffMinutes) {
                // Even a single attraction overflows the window; take it alone and stop for the day.
                break;
            }
        }
        return chosen;
    }

    private int simulateFinishMinutes(int startMinutes, GeoPoint hotelLocation, List<Attraction> orderedAttractions) {
        int current = startMinutes;
        GeoPoint previous = hotelLocation;
        for (Attraction attraction : orderedAttractions) {
            double distance = routeOptimizationService.distanceKm(previous, attraction.getLocation());
            current += routeOptimizationService.travelTimeMinutes(distance) + attraction.getAverageVisitDurationMinutes();
            previous = attraction.getLocation();
        }
        return current;
    }

    private ItineraryDay buildDay(int dayNumber, LocalDate date, List<Attraction> dayAttractions,
                                   GeoPoint hotelLocation, int startMinutes) {
        List<Attraction> ordered = routeOptimizationService.optimizeOrder(hotelLocation, dayAttractions);

        ItineraryDay day = new ItineraryDay();
        day.setDayNumber(dayNumber);
        day.setDate(date.toString());

        int currentMinutes = startMinutes;
        GeoPoint previousLocation = hotelLocation;
        double totalDistance = 0;
        int totalTravelTime = 0;

        List<ItineraryItem> items = new ArrayList<>();
        int order = 1;
        for (Attraction attraction : ordered) {
            double distance = routeOptimizationService.distanceKm(previousLocation, attraction.getLocation());
            int travelTime = routeOptimizationService.travelTimeMinutes(distance);
            String mode = routeOptimizationService.travelMode(distance);

            int arrivalMinutes = currentMinutes + travelTime;
            int departureMinutes = arrivalMinutes + attraction.getAverageVisitDurationMinutes();

            ItineraryItem item = new ItineraryItem();
            item.setOrder(order++);
            item.setAttraction(attraction);
            item.setArrivalTime(formatTime(arrivalMinutes));
            item.setDepartureTime(formatTime(departureMinutes));
            item.setVisitDurationMinutes(attraction.getAverageVisitDurationMinutes());
            item.setTravelDistanceFromPreviousKm(distance);
            item.setTravelTimeFromPreviousMinutes(travelTime);
            item.setTravelMode(mode);
            items.add(item);

            totalDistance += distance;
            totalTravelTime += travelTime;
            currentMinutes = departureMinutes;
            previousLocation = attraction.getLocation();
        }

        day.setItems(items);
        day.setTotalDistanceKm(totalDistance);
        day.setTotalTravelTimeMinutes(totalTravelTime);
        return day;
    }

    /** Attractions suited to the travel group, ranked by relevance to that group's typical interests.
     * When the AI service is configured, the curated catalog is supplemented with attractions the AI
     * recommends specifically for this destination + travel group, so personalization isn't limited
     * to the 10 curated destinations. */
    private List<Attraction> rankedAttractionsFor(Destination destination, TravelGroupType groupType) {
        List<Attraction> attractions = new ArrayList<>(attractionRepository.findByDestination(destination.getId()));
        attractions.removeIf(a -> !a.isSuitableFor(groupType));
        if (aiService.isEnabled()) {
            // The AI has no knowledge of the curated catalog, so it can (and does) suggest
            // something already in it - e.g. "Disneyland Paris" again - which would otherwise
            // get scheduled twice. Dedupe by normalized name against what's already in the pool.
            Set<String> existingNames = new HashSet<>();
            for (Attraction a : attractions) {
                existingNames.add(normalizedName(a.getName()));
            }
            for (Attraction ai : aiSupplementAttractions(destination, groupType)) {
                if (existingNames.add(normalizedName(ai.getName()))) {
                    attractions.add(ai);
                }
            }
        }
        Map<ActivityTag, Integer> weights = categoryWeights(groupType);
        attractions.sort(Comparator.comparingInt(
                (Attraction a) -> weights.getOrDefault(a.getCategory(), 0)).reversed());
        return attractions;
    }

    /** Resolves the destination context needed to ask the AI for alternatives. Curated destinations
     * are looked up directly; non-curated ones (a real city found via city search) only persisted
     * their name/country/hotel location on the {@link Itinerary} itself, so a lightweight stand-in
     * is built from that instead of failing. */
    private Destination destinationForAlternatives(Itinerary itinerary) {
        java.util.Optional<Destination> curated = destinationRepository.findById(itinerary.getDestinationId());
        if (curated.isPresent()) {
            return curated.get();
        }
        Destination virtual = new Destination();
        virtual.setId(itinerary.getDestinationId());
        virtual.setName(itinerary.getDestinationName());
        virtual.setCountry(itinerary.getDestinationCountry());
        virtual.setLocation(itinerary.getHotelLocation());
        return virtual;
    }

    /** Asks the AI for attractions tailored to this destination + travel group, discarding any
     * suggestion whose own coordinates land implausibly far from the destination - a sanity check
     * on the AI's data, not an independent geocoding pass. Never throws: any failure (timeout,
     * malformed response, all suggestions rejected) simply yields no supplemental attractions. */
    private List<Attraction> aiSupplementAttractions(Destination destination, TravelGroupType groupType) {
        try {
            List<AiAttractionSuggestion> suggestions = aiService.suggestAttractions(
                    destination.getName(), destination.getCountry(), groupType, AI_ATTRACTION_SUGGESTION_COUNT);
            List<Attraction> result = new ArrayList<>();
            for (AiAttractionSuggestion suggestion : suggestions) {
                Attraction attraction = toAttraction(suggestion, destination, groupType);
                if (attraction != null) {
                    result.add(attraction);
                }
            }
            return result;
        } catch (RuntimeException e) {
            System.err.println("AI attraction supplementation failed: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private Attraction toAttraction(AiAttractionSuggestion suggestion, Destination destination, TravelGroupType groupType) {
        if (suggestion.name == null || suggestion.name.trim().isEmpty()
                || suggestion.latitude == null || suggestion.longitude == null) {
            return null;
        }
        GeoPoint point = new GeoPoint(suggestion.latitude, suggestion.longitude);
        if (destination.getLocation() != null
                && routeOptimizationService.distanceKm(destination.getLocation(), point) > MAX_ATTRACTION_DISTANCE_FROM_CENTER_KM) {
            return null;
        }

        Attraction attraction = new Attraction();
        attraction.setId("ai-" + UUID.randomUUID());
        attraction.setDestinationId(destination.getId());
        attraction.setName(suggestion.name);
        attraction.setDescription(suggestion.description != null ? suggestion.description : "");
        attraction.setCategory(parseCategory(suggestion.category));
        attraction.setLocation(point);
        attraction.setAverageVisitDurationMinutes(suggestion.averageVisitDurationMinutes != null
                ? suggestion.averageVisitDurationMinutes : DEFAULT_AI_ATTRACTION_VISIT_MINUTES);
        // Suggested specifically for this one group type - not re-asked or guessed for the other two.
        attraction.setSuitableForFamily(groupType == TravelGroupType.FAMILY);
        attraction.setSuitableForCouple(groupType == TravelGroupType.COUPLE);
        attraction.setSuitableForFriends(groupType == TravelGroupType.FRIENDS);
        attraction.setEstimatedCost(BudgetLevel.MEDIUM);
        attraction.setAiSourced(true);
        return attraction;
    }

    private static String normalizedName(String name) {
        return name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
    }

    private static ActivityTag parseCategory(String category) {
        if (category != null) {
            try {
                return ActivityTag.valueOf(category.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
                // fall through to the default below
            }
        }
        return ActivityTag.LANDMARKS;
    }

    private Map<ActivityTag, Integer> categoryWeights(TravelGroupType groupType) {
        Map<ActivityTag, Integer> weights = new EnumMap<>(ActivityTag.class);
        if (groupType == null) {
            return weights;
        }
        switch (groupType) {
            case FAMILY:
                weights.put(ActivityTag.THEME_PARKS, 3);
                weights.put(ActivityTag.WILDLIFE, 3);
                weights.put(ActivityTag.BEACH, 2);
                weights.put(ActivityTag.NATURE, 2);
                weights.put(ActivityTag.HIKING, 1);
                break;
            case COUPLE:
                weights.put(ActivityTag.LANDMARKS, 3);
                weights.put(ActivityTag.FOOD, 3);
                weights.put(ActivityTag.ART, 2);
                weights.put(ActivityTag.HISTORY, 2);
                weights.put(ActivityTag.BEACH, 1);
                break;
            case FRIENDS:
                weights.put(ActivityTag.NIGHTLIFE, 3);
                weights.put(ActivityTag.WATER_SPORTS, 3);
                weights.put(ActivityTag.SHOPPING, 2);
                weights.put(ActivityTag.FOOD, 2);
                weights.put(ActivityTag.HIKING, 1);
                break;
        }
        return weights;
    }

    private static String formatTime(int minutesFromMidnight) {
        int wrapped = minutesFromMidnight % (24 * 60);
        return String.format(Locale.ROOT, "%02d:%02d", wrapped / 60, wrapped % 60);
    }

    private TransferLeg buildTransferLeg(String fromLabel, String toLabel, String mode, double distanceKm,
                                          int travelTimeMinutes, int departureMinutes, int arrivalMinutes) {
        TransferLeg leg = new TransferLeg();
        leg.setFromLabel(fromLabel);
        leg.setToLabel(toLabel);
        leg.setMode(mode);
        leg.setDistanceKm(distanceKm);
        leg.setTravelTimeMinutes(travelTimeMinutes);
        leg.setDepartureTime(formatTime(departureMinutes));
        leg.setArrivalTime(formatTime(arrivalMinutes));
        return leg;
    }

    /** Parses an optional "HH:mm" field to minutes-from-midnight; null/blank input returns null. */
    private static Integer parseTimeToMinutes(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            LocalTime time = LocalTime.parse(value.trim());
            return time.getHour() * 60 + time.getMinute();
        } catch (DateTimeParseException ex) {
            throw new BadRequestException(field + " must be in HH:mm format");
        }
    }

    private static TravelGroupType parseGroupType(String value) {
        try {
            return TravelGroupType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new BadRequestException("Invalid travelGroupType: " + value);
        }
    }

    private static LocalDate parseDate(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new BadRequestException(field + " is required");
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException ex) {
            throw new BadRequestException(field + " must be an ISO date (yyyy-MM-dd)");
        }
    }
}
