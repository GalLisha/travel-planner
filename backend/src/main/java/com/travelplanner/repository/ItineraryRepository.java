package com.travelplanner.repository;

import com.travelplanner.model.Itinerary;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Holds itineraries generated during the current server session, keyed by id. */
public class ItineraryRepository {

    private final Map<String, Itinerary> itineraries = new ConcurrentHashMap<>();

    public void save(Itinerary itinerary) {
        itineraries.put(itinerary.getId(), itinerary);
    }

    public Optional<Itinerary> findById(String id) {
        return Optional.ofNullable(itineraries.get(id));
    }
}
