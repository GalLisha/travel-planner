package com.travelplanner.service;

import com.travelplanner.dto.request.GenerateItineraryRequestDto;
import com.travelplanner.dto.request.ReplaceAttractionRequestDto;
import com.travelplanner.exception.BadRequestException;
import com.travelplanner.exception.NotFoundException;
import com.travelplanner.model.ActivityTag;
import com.travelplanner.model.Attraction;
import com.travelplanner.model.Destination;
import com.travelplanner.model.GeoPoint;
import com.travelplanner.model.Itinerary;
import com.travelplanner.model.ItineraryDay;
import com.travelplanner.model.ItineraryItem;
import com.travelplanner.model.TravelGroupType;
import com.travelplanner.repository.AttractionRepository;
import com.travelplanner.repository.DestinationRepository;
import com.travelplanner.repository.ItineraryRepository;

import java.time.LocalDate;
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

    private final DestinationRepository destinationRepository;
    private final AttractionRepository attractionRepository;
    private final ItineraryRepository itineraryRepository;
    private final RouteOptimizationService routeOptimizationService;

    public ItineraryService(DestinationRepository destinationRepository,
                             AttractionRepository attractionRepository,
                             ItineraryRepository itineraryRepository,
                             RouteOptimizationService routeOptimizationService) {
        this.destinationRepository = destinationRepository;
        this.attractionRepository = attractionRepository;
        this.itineraryRepository = itineraryRepository;
        this.routeOptimizationService = routeOptimizationService;
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

        // Attractions only exist for the curated catalog; a real city found via city
        // search that isn't one of those 10 destinations simply yields an empty pool,
        // which naturally produces a trip of "free days" below rather than failing.
        List<Attraction> pool = rankedAttractionsFor(destination.getId(), groupType);

        Itinerary itinerary = new Itinerary();
        itinerary.setId(UUID.randomUUID().toString());
        itinerary.setDestinationId(destination.getId());
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
            int startMinutes = isFirstDay ? ARRIVAL_DAY_START_MINUTES : DAY_START_MINUTES;
            int endCutoff = isLastDay ? DEPARTURE_DAY_END_MINUTES : DAY_END_MINUTES;

            List<Attraction> dayAttractions = fillDayWithinBudget(pool, poolIndex, hotelLocation, startMinutes, endCutoff);
            poolIndex += dayAttractions.size();

            ItineraryDay day = buildDay(dayNumber, departureDate.plusDays(dayNumber - 1), dayAttractions,
                    hotelLocation, isFirstDay);
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
        ItineraryDay rebuilt = buildDay(day.getDayNumber(), LocalDate.parse(itinerary.getDepartureDate())
                .plusDays(day.getDayNumber() - 1), dayAttractions, itinerary.getHotelLocation(), isFirstDay);

        int index = itinerary.getDays().indexOf(day);
        itinerary.getDays().set(index, rebuilt);
        itineraryRepository.save(itinerary);
        return itinerary;
    }

    public List<Attraction> findAlternatives(String itineraryId, int dayNumber, String attractionId) {
        Itinerary itinerary = getItinerary(itineraryId);
        Attraction current = attractionRepository.findById(attractionId);
        Set<String> usedElsewhere = usedAttractionIds(itinerary, dayNumber);

        List<Attraction> candidates = rankedAttractionsFor(itinerary.getDestinationId(), itinerary.getTravelGroupType());
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
                                   GeoPoint hotelLocation, boolean isFirstDay) {
        List<Attraction> ordered = routeOptimizationService.optimizeOrder(hotelLocation, dayAttractions);

        ItineraryDay day = new ItineraryDay();
        day.setDayNumber(dayNumber);
        day.setDate(date.toString());

        int currentMinutes = isFirstDay ? ARRIVAL_DAY_START_MINUTES : DAY_START_MINUTES;
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

    /** Attractions suited to the travel group, ranked by relevance to that group's typical interests. */
    private List<Attraction> rankedAttractionsFor(String destinationId, TravelGroupType groupType) {
        List<Attraction> attractions = new ArrayList<>(attractionRepository.findByDestination(destinationId));
        attractions.removeIf(a -> !a.isSuitableFor(groupType));
        Map<ActivityTag, Integer> weights = categoryWeights(groupType);
        attractions.sort(Comparator.comparingInt(
                (Attraction a) -> weights.getOrDefault(a.getCategory(), 0)).reversed());
        return attractions;
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
