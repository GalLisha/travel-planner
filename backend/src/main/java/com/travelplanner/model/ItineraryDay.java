package com.travelplanner.model;

import java.util.ArrayList;
import java.util.List;

public class ItineraryDay {
    private int dayNumber;
    private String date;
    private List<ItineraryItem> items = new ArrayList<>();
    private double totalDistanceKm;
    private int totalTravelTimeMinutes;

    public ItineraryDay() {
    }

    public int getDayNumber() {
        return dayNumber;
    }

    public void setDayNumber(int dayNumber) {
        this.dayNumber = dayNumber;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<ItineraryItem> getItems() {
        return items;
    }

    public void setItems(List<ItineraryItem> items) {
        this.items = items;
    }

    public double getTotalDistanceKm() {
        return totalDistanceKm;
    }

    public void setTotalDistanceKm(double totalDistanceKm) {
        this.totalDistanceKm = totalDistanceKm;
    }

    public int getTotalTravelTimeMinutes() {
        return totalTravelTimeMinutes;
    }

    public void setTotalTravelTimeMinutes(int totalTravelTimeMinutes) {
        this.totalTravelTimeMinutes = totalTravelTimeMinutes;
    }
}
