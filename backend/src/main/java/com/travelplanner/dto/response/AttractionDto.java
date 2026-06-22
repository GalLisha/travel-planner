package com.travelplanner.dto.response;

import com.travelplanner.model.Attraction;

public class AttractionDto {
    private String id;
    private String name;
    private String description;
    private String category;
    private double latitude;
    private double longitude;
    private int averageVisitDurationMinutes;
    private String openingHours;
    private String estimatedCost;
    private String imageUrl;
    private boolean aiSourced;

    public static AttractionDto fromModel(Attraction a) {
        AttractionDto dto = new AttractionDto();
        dto.id = a.getId();
        dto.name = a.getName();
        dto.description = a.getDescription();
        dto.category = a.getCategory() != null ? a.getCategory().name() : null;
        dto.latitude = a.getLocation() != null ? a.getLocation().getLatitude() : 0;
        dto.longitude = a.getLocation() != null ? a.getLocation().getLongitude() : 0;
        dto.averageVisitDurationMinutes = a.getAverageVisitDurationMinutes();
        dto.openingHours = a.getOpeningHours();
        dto.estimatedCost = a.getEstimatedCost() != null ? a.getEstimatedCost().name() : null;
        dto.imageUrl = a.getImageUrl();
        dto.aiSourced = a.isAiSourced();
        return dto;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public int getAverageVisitDurationMinutes() {
        return averageVisitDurationMinutes;
    }

    public String getOpeningHours() {
        return openingHours;
    }

    public String getEstimatedCost() {
        return estimatedCost;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean isAiSourced() {
        return aiSourced;
    }
}
