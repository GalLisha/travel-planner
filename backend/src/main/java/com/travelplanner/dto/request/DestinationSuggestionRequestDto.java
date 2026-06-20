package com.travelplanner.dto.request;

import java.util.List;

/** Preferences collected when the user wants the system to suggest a destination. */
public class DestinationSuggestionRequestDto {
    private String travelGroupType;
    private String budgetLevel;
    private String region;
    private Integer maxFlightDurationHours;
    private List<String> vacationStyles;
    private List<String> activities;
    private String additionalPreferences;

    public String getTravelGroupType() {
        return travelGroupType;
    }

    public void setTravelGroupType(String travelGroupType) {
        this.travelGroupType = travelGroupType;
    }

    public String getBudgetLevel() {
        return budgetLevel;
    }

    public void setBudgetLevel(String budgetLevel) {
        this.budgetLevel = budgetLevel;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Integer getMaxFlightDurationHours() {
        return maxFlightDurationHours;
    }

    public void setMaxFlightDurationHours(Integer maxFlightDurationHours) {
        this.maxFlightDurationHours = maxFlightDurationHours;
    }

    public List<String> getVacationStyles() {
        return vacationStyles;
    }

    public void setVacationStyles(List<String> vacationStyles) {
        this.vacationStyles = vacationStyles;
    }

    public List<String> getActivities() {
        return activities;
    }

    public void setActivities(List<String> activities) {
        this.activities = activities;
    }

    public String getAdditionalPreferences() {
        return additionalPreferences;
    }

    public void setAdditionalPreferences(String additionalPreferences) {
        this.additionalPreferences = additionalPreferences;
    }
}
