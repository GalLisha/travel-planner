package com.travelplanner.service.hotel;

/** Parameters for a hotel search, anchored on the selected city's center point. */
public class HotelSearchQuery {
    private final String cityName;
    private final String countryName;
    private final double latitude;
    private final double longitude;

    public HotelSearchQuery(String cityName, String countryName, double latitude, double longitude) {
        this.cityName = cityName;
        this.countryName = countryName;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getCityName() {
        return cityName;
    }

    public String getCountryName() {
        return countryName;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
