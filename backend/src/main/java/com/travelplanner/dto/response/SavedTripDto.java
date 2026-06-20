package com.travelplanner.dto.response;

import com.travelplanner.model.SavedTrip;

public class SavedTripDto {
    private String id;
    private String destinationName;
    private String countryName;
    private String departureDate;
    private String returnDate;
    private long savedAt;
    private Object itinerary;

    public static SavedTripDto fromModel(SavedTrip trip) {
        SavedTripDto dto = new SavedTripDto();
        dto.id = trip.getId();
        dto.destinationName = trip.getDestinationName();
        dto.countryName = trip.getCountryName();
        dto.departureDate = trip.getDepartureDate();
        dto.returnDate = trip.getReturnDate();
        dto.savedAt = trip.getSavedAt();
        dto.itinerary = trip.getItinerary();
        return dto;
    }

    public String getId() {
        return id;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public String getCountryName() {
        return countryName;
    }

    public String getDepartureDate() {
        return departureDate;
    }

    public String getReturnDate() {
        return returnDate;
    }

    public long getSavedAt() {
        return savedAt;
    }

    public Object getItinerary() {
        return itinerary;
    }
}
