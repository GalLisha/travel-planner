package com.travelplanner.service.ai;

/** A single hotel suggested by the AI fallback when a real hotel provider returns nothing. */
public class AiHotelSuggestion {
    public String name;
    public String approximateAddress;
    public Double starRating;
}
