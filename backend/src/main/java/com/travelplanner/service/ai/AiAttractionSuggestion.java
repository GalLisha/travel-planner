package com.travelplanner.service.ai;

/** A single attraction suggested by the AI, tailored to a destination and travel group type. */
public class AiAttractionSuggestion {
    public String name;
    public String description;
    public String category;
    public Integer averageVisitDurationMinutes;
    public Double latitude;
    public Double longitude;
}
