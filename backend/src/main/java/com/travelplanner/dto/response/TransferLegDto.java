package com.travelplanner.dto.response;

import com.travelplanner.model.TransferLeg;

public class TransferLegDto {
    private String fromLabel;
    private String toLabel;
    private String mode;
    private double distanceKm;
    private int travelTimeMinutes;
    private String departureTime;
    private String arrivalTime;

    public static TransferLegDto fromModel(TransferLeg leg) {
        if (leg == null) {
            return null;
        }
        TransferLegDto dto = new TransferLegDto();
        dto.fromLabel = leg.getFromLabel();
        dto.toLabel = leg.getToLabel();
        dto.mode = leg.getMode();
        dto.distanceKm = Math.round(leg.getDistanceKm() * 10.0) / 10.0;
        dto.travelTimeMinutes = leg.getTravelTimeMinutes();
        dto.departureTime = leg.getDepartureTime();
        dto.arrivalTime = leg.getArrivalTime();
        return dto;
    }

    public String getFromLabel() {
        return fromLabel;
    }

    public String getToLabel() {
        return toLabel;
    }

    public String getMode() {
        return mode;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public int getTravelTimeMinutes() {
        return travelTimeMinutes;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }
}
