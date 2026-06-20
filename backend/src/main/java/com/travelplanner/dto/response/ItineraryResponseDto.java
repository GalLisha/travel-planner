package com.travelplanner.dto.response;

import com.travelplanner.model.Itinerary;

import java.util.List;
import java.util.stream.Collectors;

public class ItineraryResponseDto {
    private String id;
    private String destinationId;
    private String travelGroupType;
    private String departureDate;
    private String returnDate;
    private String hotelName;
    private String hotelAddress;
    private List<ItineraryDayDto> days;

    public static ItineraryResponseDto fromModel(Itinerary itinerary) {
        ItineraryResponseDto dto = new ItineraryResponseDto();
        dto.id = itinerary.getId();
        dto.destinationId = itinerary.getDestinationId();
        dto.travelGroupType = itinerary.getTravelGroupType() != null ? itinerary.getTravelGroupType().name() : null;
        dto.departureDate = itinerary.getDepartureDate();
        dto.returnDate = itinerary.getReturnDate();
        dto.hotelName = itinerary.getHotelName();
        dto.hotelAddress = itinerary.getHotelAddress();
        dto.days = itinerary.getDays().stream().map(ItineraryDayDto::fromModel).collect(Collectors.toList());
        return dto;
    }

    public String getId() {
        return id;
    }

    public String getDestinationId() {
        return destinationId;
    }

    public String getTravelGroupType() {
        return travelGroupType;
    }

    public String getDepartureDate() {
        return departureDate;
    }

    public String getReturnDate() {
        return returnDate;
    }

    public String getHotelName() {
        return hotelName;
    }

    public String getHotelAddress() {
        return hotelAddress;
    }

    public List<ItineraryDayDto> getDays() {
        return days;
    }
}
