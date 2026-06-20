package com.travelplanner.model;

/** A single scheduled stop within a day: an attraction plus how it connects to the previous stop. */
public class ItineraryItem {
    private int order;
    private Attraction attraction;
    private String arrivalTime;
    private String departureTime;
    private int visitDurationMinutes;
    private double travelDistanceFromPreviousKm;
    private int travelTimeFromPreviousMinutes;
    private String travelMode;

    public ItineraryItem() {
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public Attraction getAttraction() {
        return attraction;
    }

    public void setAttraction(Attraction attraction) {
        this.attraction = attraction;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public int getVisitDurationMinutes() {
        return visitDurationMinutes;
    }

    public void setVisitDurationMinutes(int visitDurationMinutes) {
        this.visitDurationMinutes = visitDurationMinutes;
    }

    public double getTravelDistanceFromPreviousKm() {
        return travelDistanceFromPreviousKm;
    }

    public void setTravelDistanceFromPreviousKm(double travelDistanceFromPreviousKm) {
        this.travelDistanceFromPreviousKm = travelDistanceFromPreviousKm;
    }

    public int getTravelTimeFromPreviousMinutes() {
        return travelTimeFromPreviousMinutes;
    }

    public void setTravelTimeFromPreviousMinutes(int travelTimeFromPreviousMinutes) {
        this.travelTimeFromPreviousMinutes = travelTimeFromPreviousMinutes;
    }

    public String getTravelMode() {
        return travelMode;
    }

    public void setTravelMode(String travelMode) {
        this.travelMode = travelMode;
    }
}
