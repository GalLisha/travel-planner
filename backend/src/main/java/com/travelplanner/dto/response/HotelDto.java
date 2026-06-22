package com.travelplanner.dto.response;

import com.travelplanner.model.Hotel;

public class HotelDto {
    private String id;
    private String name;
    private Double starRating;
    private Double minPricePerNight;
    private Double maxPricePerNight;
    private String currency;
    private double distanceFromCenterKm;
    private String imageUrl;
    private String address;
    private double latitude;
    private double longitude;
    private boolean aiSourced;

    public static HotelDto fromModel(Hotel hotel) {
        HotelDto dto = new HotelDto();
        dto.id = hotel.getId();
        dto.name = hotel.getName();
        dto.starRating = hotel.getStarRating();
        dto.minPricePerNight = hotel.getMinPricePerNight();
        dto.maxPricePerNight = hotel.getMaxPricePerNight();
        dto.currency = hotel.getCurrency();
        dto.distanceFromCenterKm = Math.round(hotel.getDistanceFromCenterKm() * 10.0) / 10.0;
        dto.imageUrl = hotel.getImageUrl();
        dto.address = hotel.getAddress();
        dto.latitude = hotel.getLocation() != null ? hotel.getLocation().getLatitude() : 0;
        dto.longitude = hotel.getLocation() != null ? hotel.getLocation().getLongitude() : 0;
        dto.aiSourced = hotel.isAiSourced();
        return dto;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Double getStarRating() {
        return starRating;
    }

    public Double getMinPricePerNight() {
        return minPricePerNight;
    }

    public Double getMaxPricePerNight() {
        return maxPricePerNight;
    }

    public String getCurrency() {
        return currency;
    }

    public double getDistanceFromCenterKm() {
        return distanceFromCenterKm;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getAddress() {
        return address;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public boolean isAiSourced() {
        return aiSourced;
    }
}
