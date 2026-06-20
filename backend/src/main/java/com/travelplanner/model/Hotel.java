package com.travelplanner.model;

public class Hotel {
    private String id;
    private String name;
    private Double starRating;
    private Double minPricePerNight;
    private Double maxPricePerNight;
    private String currency;
    private double distanceFromCenterKm;
    private String imageUrl;
    private String address;
    private GeoPoint location;

    public Hotel() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getStarRating() {
        return starRating;
    }

    public void setStarRating(Double starRating) {
        this.starRating = starRating;
    }

    public Double getMinPricePerNight() {
        return minPricePerNight;
    }

    public void setMinPricePerNight(Double minPricePerNight) {
        this.minPricePerNight = minPricePerNight;
    }

    public Double getMaxPricePerNight() {
        return maxPricePerNight;
    }

    public void setMaxPricePerNight(Double maxPricePerNight) {
        this.maxPricePerNight = maxPricePerNight;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public double getDistanceFromCenterKm() {
        return distanceFromCenterKm;
    }

    public void setDistanceFromCenterKm(double distanceFromCenterKm) {
        this.distanceFromCenterKm = distanceFromCenterKm;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }
}
