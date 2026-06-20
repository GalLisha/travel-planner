package com.travelplanner.dto.response;

import com.travelplanner.model.ItineraryDay;

import java.util.List;
import java.util.stream.Collectors;

public class ItineraryDayDto {
    private int dayNumber;
    private String date;
    private List<ItineraryItemDto> items;
    private double totalDistanceKm;
    private int totalTravelTimeMinutes;

    public static ItineraryDayDto fromModel(ItineraryDay day) {
        ItineraryDayDto dto = new ItineraryDayDto();
        dto.dayNumber = day.getDayNumber();
        dto.date = day.getDate();
        dto.items = day.getItems().stream().map(ItineraryItemDto::fromModel).collect(Collectors.toList());
        dto.totalDistanceKm = Math.round(day.getTotalDistanceKm() * 10.0) / 10.0;
        dto.totalTravelTimeMinutes = day.getTotalTravelTimeMinutes();
        return dto;
    }

    public int getDayNumber() {
        return dayNumber;
    }

    public String getDate() {
        return date;
    }

    public List<ItineraryItemDto> getItems() {
        return items;
    }

    public double getTotalDistanceKm() {
        return totalDistanceKm;
    }

    public int getTotalTravelTimeMinutes() {
        return totalTravelTimeMinutes;
    }
}
