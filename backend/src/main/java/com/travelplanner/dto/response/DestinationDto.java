package com.travelplanner.dto.response;

import com.travelplanner.model.ActivityTag;
import com.travelplanner.model.Destination;
import com.travelplanner.model.VacationStyle;

import java.util.stream.Collectors;

public class DestinationDto {
    private String id;
    private String name;
    private String country;
    private String region;
    private String description;
    private String imageUrl;
    private String budgetLevel;
    private int avgFlightDurationHours;
    private java.util.List<String> vacationStyles;
    private java.util.List<String> activityTags;
    private boolean familyFriendly;
    private boolean romantic;
    private boolean nightlife;
    private double matchScore;
    private double latitude;
    private double longitude;

    public static DestinationDto fromModel(Destination d) {
        return fromModel(d, 0);
    }

    public static DestinationDto fromModel(Destination d, double matchScore) {
        DestinationDto dto = new DestinationDto();
        dto.id = d.getId();
        dto.name = d.getName();
        dto.country = d.getCountry();
        dto.region = d.getRegion() != null ? d.getRegion().name() : null;
        dto.description = d.getDescription();
        dto.imageUrl = d.getImageUrl();
        dto.budgetLevel = d.getBudgetLevel() != null ? d.getBudgetLevel().name() : null;
        dto.avgFlightDurationHours = d.getAvgFlightDurationHours();
        dto.vacationStyles = d.getVacationStyles() == null ? java.util.Collections.emptyList()
                : d.getVacationStyles().stream().map(VacationStyle::name).collect(Collectors.toList());
        dto.activityTags = d.getActivityTags() == null ? java.util.Collections.emptyList()
                : d.getActivityTags().stream().map(ActivityTag::name).collect(Collectors.toList());
        dto.familyFriendly = d.isFamilyFriendly();
        dto.romantic = d.isRomantic();
        dto.nightlife = d.isNightlife();
        dto.matchScore = matchScore;
        dto.latitude = d.getLocation() != null ? d.getLocation().getLatitude() : 0;
        dto.longitude = d.getLocation() != null ? d.getLocation().getLongitude() : 0;
        return dto;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }

    public String getRegion() {
        return region;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getBudgetLevel() {
        return budgetLevel;
    }

    public int getAvgFlightDurationHours() {
        return avgFlightDurationHours;
    }

    public java.util.List<String> getVacationStyles() {
        return vacationStyles;
    }

    public java.util.List<String> getActivityTags() {
        return activityTags;
    }

    public boolean isFamilyFriendly() {
        return familyFriendly;
    }

    public boolean isRomantic() {
        return romantic;
    }

    public boolean isNightlife() {
        return nightlife;
    }

    public double getMatchScore() {
        return matchScore;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
