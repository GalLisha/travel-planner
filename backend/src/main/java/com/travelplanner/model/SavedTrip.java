package com.travelplanner.model;

/** A user-saved snapshot of a built itinerary. The itinerary itself is stored opaquely
 *  (whatever JSON the frontend already has) rather than re-modeled server-side. */
public class SavedTrip {
    private String id;
    private String userId;
    private String destinationName;
    private String countryName;
    private String departureDate;
    private String returnDate;
    private Object itinerary;
    private long savedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
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

    public Object getItinerary() {
        return itinerary;
    }

    public void setItinerary(Object itinerary) {
        this.itinerary = itinerary;
    }

    public long getSavedAt() {
        return savedAt;
    }

    public void setSavedAt(long savedAt) {
        this.savedAt = savedAt;
    }
}
