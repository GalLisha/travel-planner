package com.travelplanner.model;

/** A commercial airport found near a destination, used as the anchor point for transfer-leg calculations. */
public class Airport {
    private String name;
    private String iataCode;
    private GeoPoint location;
    private double distanceFromCityCenterKm;

    public Airport() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIataCode() {
        return iataCode;
    }

    public void setIataCode(String iataCode) {
        this.iataCode = iataCode;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    public double getDistanceFromCityCenterKm() {
        return distanceFromCityCenterKm;
    }

    public void setDistanceFromCityCenterKm(double distanceFromCityCenterKm) {
        this.distanceFromCityCenterKm = distanceFromCityCenterKm;
    }
}
