package com.travelplanner.dto.response;

import com.travelplanner.model.City;

public class CityDto {
    private String name;
    private String country;
    private String countryCode;
    private String region;
    private double latitude;
    private double longitude;
    private Long population;
    private String curatedDestinationId;

    public static CityDto fromModel(City city) {
        CityDto dto = new CityDto();
        dto.name = city.getName();
        dto.country = city.getCountry();
        dto.countryCode = city.getCountryCode();
        dto.region = city.getRegion();
        dto.latitude = city.getLocation() != null ? city.getLocation().getLatitude() : 0;
        dto.longitude = city.getLocation() != null ? city.getLocation().getLongitude() : 0;
        dto.population = city.getPopulation();
        dto.curatedDestinationId = city.getCuratedDestinationId();
        return dto;
    }

    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getRegion() {
        return region;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public Long getPopulation() {
        return population;
    }

    public String getCuratedDestinationId() {
        return curatedDestinationId;
    }
}
