package com.travelplanner.service;

import com.travelplanner.dto.request.SaveTripRequestDto;
import com.travelplanner.exception.BadRequestException;
import com.travelplanner.model.SavedTrip;
import com.travelplanner.repository.SavedTripRepository;

import java.util.List;

public class TripService {

    private final SavedTripRepository savedTripRepository;

    public TripService(SavedTripRepository savedTripRepository) {
        this.savedTripRepository = savedTripRepository;
    }

    public SavedTrip saveTrip(String userId, SaveTripRequestDto request) {
        if (request.getItinerary() == null) {
            throw new BadRequestException("itinerary is required");
        }
        SavedTrip trip = new SavedTrip();
        trip.setUserId(userId);
        trip.setDestinationName(request.getDestinationName());
        trip.setCountryName(request.getCountryName());
        trip.setDepartureDate(request.getDepartureDate());
        trip.setReturnDate(request.getReturnDate());
        trip.setItinerary(request.getItinerary());
        trip.setSavedAt(System.currentTimeMillis());
        return savedTripRepository.insert(trip);
    }

    public List<SavedTrip> listTrips(String userId) {
        return savedTripRepository.findByUserId(userId);
    }
}
