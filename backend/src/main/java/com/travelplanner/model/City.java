package com.travelplanner.model;

/** A real-world city returned by the city search provider (e.g. OpenStreetMap Nominatim). */
public class City {
    private String name;
    private String country;
    private String countryCode;
    private String region;
    private GeoPoint location;
    private Long population;
    private String curatedDestinationId;

    public City() {
    }

    public City(String name, String country, String countryCode, String region, GeoPoint location, Long population) {
        this.name = name;
        this.country = country;
        this.countryCode = countryCode;
        this.region = region;
        this.location = location;
        this.population = population;
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

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    public Long getPopulation() {
        return population;
    }

    public void setPopulation(Long population) {
        this.population = population;
    }

    public String getCuratedDestinationId() {
        return curatedDestinationId;
    }

    public void setCuratedDestinationId(String curatedDestinationId) {
        this.curatedDestinationId = curatedDestinationId;
    }
}
