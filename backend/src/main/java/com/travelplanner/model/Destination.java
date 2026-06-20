package com.travelplanner.model;

import java.util.Set;

public class Destination {
    private String id;
    private String name;
    private String country;
    private Region region;
    private String description;
    private String imageUrl;
    private BudgetLevel budgetLevel;
    private int avgFlightDurationHours;
    private Set<VacationStyle> vacationStyles;
    private Set<ActivityTag> activityTags;
    private boolean familyFriendly;
    private boolean romantic;
    private boolean nightlife;
    private GeoPoint location;

    public Destination() {
    }

    public Destination(String id, String name, String country, Region region, String description, String imageUrl,
                        BudgetLevel budgetLevel, int avgFlightDurationHours, Set<VacationStyle> vacationStyles,
                        Set<ActivityTag> activityTags, boolean familyFriendly, boolean romantic, boolean nightlife,
                        GeoPoint location) {
        this.id = id;
        this.name = name;
        this.country = country;
        this.region = region;
        this.description = description;
        this.imageUrl = imageUrl;
        this.budgetLevel = budgetLevel;
        this.avgFlightDurationHours = avgFlightDurationHours;
        this.vacationStyles = vacationStyles;
        this.activityTags = activityTags;
        this.familyFriendly = familyFriendly;
        this.romantic = romantic;
        this.nightlife = nightlife;
        this.location = location;
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

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public BudgetLevel getBudgetLevel() {
        return budgetLevel;
    }

    public void setBudgetLevel(BudgetLevel budgetLevel) {
        this.budgetLevel = budgetLevel;
    }

    public int getAvgFlightDurationHours() {
        return avgFlightDurationHours;
    }

    public void setAvgFlightDurationHours(int avgFlightDurationHours) {
        this.avgFlightDurationHours = avgFlightDurationHours;
    }

    public Set<VacationStyle> getVacationStyles() {
        return vacationStyles;
    }

    public void setVacationStyles(Set<VacationStyle> vacationStyles) {
        this.vacationStyles = vacationStyles;
    }

    public Set<ActivityTag> getActivityTags() {
        return activityTags;
    }

    public void setActivityTags(Set<ActivityTag> activityTags) {
        this.activityTags = activityTags;
    }

    public boolean isFamilyFriendly() {
        return familyFriendly;
    }

    public void setFamilyFriendly(boolean familyFriendly) {
        this.familyFriendly = familyFriendly;
    }

    public boolean isRomantic() {
        return romantic;
    }

    public void setRomantic(boolean romantic) {
        this.romantic = romantic;
    }

    public boolean isNightlife() {
        return nightlife;
    }

    public void setNightlife(boolean nightlife) {
        this.nightlife = nightlife;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }
}
