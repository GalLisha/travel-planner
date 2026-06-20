package com.travelplanner.model;

public class Attraction {
    private String id;
    private String destinationId;
    private String name;
    private String description;
    private ActivityTag category;
    private GeoPoint location;
    private int averageVisitDurationMinutes;
    private boolean suitableForFamily;
    private boolean suitableForCouple;
    private boolean suitableForFriends;
    private String openingHours;
    private BudgetLevel estimatedCost;
    private String imageUrl;

    public Attraction() {
    }

    public Attraction(String id, String destinationId, String name, String description, ActivityTag category,
                       GeoPoint location, int averageVisitDurationMinutes, boolean suitableForFamily,
                       boolean suitableForCouple, boolean suitableForFriends, String openingHours,
                       BudgetLevel estimatedCost, String imageUrl) {
        this.id = id;
        this.destinationId = destinationId;
        this.name = name;
        this.description = description;
        this.category = category;
        this.location = location;
        this.averageVisitDurationMinutes = averageVisitDurationMinutes;
        this.suitableForFamily = suitableForFamily;
        this.suitableForCouple = suitableForCouple;
        this.suitableForFriends = suitableForFriends;
        this.openingHours = openingHours;
        this.estimatedCost = estimatedCost;
        this.imageUrl = imageUrl;
    }

    public boolean isSuitableFor(TravelGroupType groupType) {
        switch (groupType) {
            case FAMILY:
                return suitableForFamily;
            case COUPLE:
                return suitableForCouple;
            case FRIENDS:
                return suitableForFriends;
            default:
                return true;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(String destinationId) {
        this.destinationId = destinationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ActivityTag getCategory() {
        return category;
    }

    public void setCategory(ActivityTag category) {
        this.category = category;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    public int getAverageVisitDurationMinutes() {
        return averageVisitDurationMinutes;
    }

    public void setAverageVisitDurationMinutes(int averageVisitDurationMinutes) {
        this.averageVisitDurationMinutes = averageVisitDurationMinutes;
    }

    public boolean isSuitableForFamily() {
        return suitableForFamily;
    }

    public void setSuitableForFamily(boolean suitableForFamily) {
        this.suitableForFamily = suitableForFamily;
    }

    public boolean isSuitableForCouple() {
        return suitableForCouple;
    }

    public void setSuitableForCouple(boolean suitableForCouple) {
        this.suitableForCouple = suitableForCouple;
    }

    public boolean isSuitableForFriends() {
        return suitableForFriends;
    }

    public void setSuitableForFriends(boolean suitableForFriends) {
        this.suitableForFriends = suitableForFriends;
    }

    public String getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(String openingHours) {
        this.openingHours = openingHours;
    }

    public BudgetLevel getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(BudgetLevel estimatedCost) {
        this.estimatedCost = estimatedCost;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
