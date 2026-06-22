package com.travelplanner.model;

import java.util.ArrayList;
import java.util.List;

public class Itinerary {
    private String id;
    private String destinationId;
    private String destinationName;
    private String destinationCountry;
    private TravelGroupType travelGroupType;
    private String departureDate;
    private String returnDate;
    private String hotelName;
    private String hotelAddress;
    private GeoPoint hotelLocation;
    private List<ItineraryDay> days = new ArrayList<>();

    public Itinerary() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(String destinationId) {
        this.destinationId = destinationId;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public String getDestinationCountry() {
        return destinationCountry;
    }

    public void setDestinationCountry(String destinationCountry) {
        this.destinationCountry = destinationCountry;
    }

    public TravelGroupType getTravelGroupType() {
        return travelGroupType;
    }

    public void setTravelGroupType(TravelGroupType travelGroupType) {
        this.travelGroupType = travelGroupType;
    }

    public String getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(String departureDate) {
        this.departureDate = departureDate;
    }

    public String getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(String returnDate) {
        this.returnDate = returnDate;
    }

    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public String getHotelAddress() {
        return hotelAddress;
    }

    public void setHotelAddress(String hotelAddress) {
        this.hotelAddress = hotelAddress;
    }

    public GeoPoint getHotelLocation() {
        return hotelLocation;
    }

    public void setHotelLocation(GeoPoint hotelLocation) {
        this.hotelLocation = hotelLocation;
    }

    public List<ItineraryDay> getDays() {
        return days;
    }

    public void setDays(List<ItineraryDay> days) {
        this.days = days;
    }
}
