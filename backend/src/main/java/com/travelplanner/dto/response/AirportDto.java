package com.travelplanner.dto.response;

import com.travelplanner.model.Airport;

public class AirportDto {
    private String name;
    private String iataCode;
    private double latitude;
    private double longitude;
    private double distanceFromCityCenterKm;

    public static AirportDto fromModel(Airport airport) {
        AirportDto dto = new AirportDto();
        dto.name = airport.getName();
        dto.iataCode = airport.getIataCode();
        dto.latitude = airport.getLocation() != null ? airport.getLocation().getLatitude() : 0;
        dto.longitude = airport.getLocation() != null ? airport.getLocation().getLongitude() : 0;
        dto.distanceFromCityCenterKm = Math.round(airport.getDistanceFromCityCenterKm() * 10.0) / 10.0;
        return dto;
    }

    public String getName() {
        return name;
    }

    public String getIataCode() {
        return iataCode;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getDistanceFromCityCenterKm() {
        return distanceFromCityCenterKm;
    }
}
