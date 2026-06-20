package com.travelplanner.dto.response;

import com.travelplanner.model.ItineraryItem;

public class ItineraryItemDto {
    private int order;
    private AttractionDto attraction;
    private String arrivalTime;
    private String departureTime;
    private int visitDurationMinutes;
    private double travelDistanceFromPreviousKm;
    private int travelTimeFromPreviousMinutes;
    private String travelMode;

    public static ItineraryItemDto fromModel(ItineraryItem item) {
        ItineraryItemDto dto = new ItineraryItemDto();
        dto.order = item.getOrder();
        dto.attraction = AttractionDto.fromModel(item.getAttraction());
        dto.arrivalTime = item.getArrivalTime();
        dto.departureTime = item.getDepartureTime();
        dto.visitDurationMinutes = item.getVisitDurationMinutes();
        dto.travelDistanceFromPreviousKm = round1(item.getTravelDistanceFromPreviousKm());
        dto.travelTimeFromPreviousMinutes = item.getTravelTimeFromPreviousMinutes();
        dto.travelMode = item.getTravelMode();
        return dto;
    }

    private static double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    public int getOrder() {
        return order;
    }

    public AttractionDto getAttraction() {
        return attraction;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public int getVisitDurationMinutes() {
        return visitDurationMinutes;
    }

    public double getTravelDistanceFromPreviousKm() {
        return travelDistanceFromPreviousKm;
    }

    public int getTravelTimeFromPreviousMinutes() {
        return travelTimeFromPreviousMinutes;
    }

    public String getTravelMode() {
        return travelMode;
    }
}
