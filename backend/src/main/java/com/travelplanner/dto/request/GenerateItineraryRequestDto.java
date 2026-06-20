package com.travelplanner.dto.request;

/** Submitted once the user has chosen a destination and confirmed they already have flights + hotel booked. */
public class GenerateItineraryRequestDto {
    private String destinationId;
    private String travelGroupType;
    private String departureDate;
    private String returnDate;
    private String hotelName;
    private String hotelAddress;
    private Double hotelLatitude;
    private Double hotelLongitude;

    // Fallback destination info, used only when destinationId doesn't match a
    // curated destination (e.g. a real city found via city search that isn't
    // one of the 10 destinations with built-in attraction data).
    private String cityName;
    private String countryName;
    private Double latitude;
    private Double longitude;

    // Arrival airport + real flight times, all optional. When present, the first/last
    // day's schedule is anchored to the actual flight times (plus a transfer leg) instead
    // of the generic hardcoded arrival/departure windows.
    private String arrivalAirportName;
    private Double arrivalAirportLatitude;
    private Double arrivalAirportLongitude;
    private String arrivalTime;
    private String departureTime;
    private String transferMode;

    public String getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(String destinationId) {
        this.destinationId = destinationId;
    }

    public String getTravelGroupType() {
        return travelGroupType;
    }

    public void setTravelGroupType(String travelGroupType) {
        this.travelGroupType = travelGroupType;
    }

    public String getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(String departureDate) {
        this.departureDate = departureDate;
    }

    public String getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(String returnDate) {
        this.returnDate = returnDate;
    }

    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public String getHotelAddress() {
        return hotelAddress;
    }

    public void setHotelAddress(String hotelAddress) {
        this.hotelAddress = hotelAddress;
    }

    public Double getHotelLatitude() {
        return hotelLatitude;
    }

    public void setHotelLatitude(Double hotelLatitude) {
        this.hotelLatitude = hotelLatitude;
    }

    public Double getHotelLongitude() {
        return hotelLongitude;
    }

    public void setHotelLongitude(Double hotelLongitude) {
        this.hotelLongitude = hotelLongitude;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getArrivalAirportName() {
        return arrivalAirportName;
    }

    public void setArrivalAirportName(String arrivalAirportName) {
        this.arrivalAirportName = arrivalAirportName;
    }

    public Double getArrivalAirportLatitude() {
        return arrivalAirportLatitude;
    }

    public void setArrivalAirportLatitude(Double arrivalAirportLatitude) {
        this.arrivalAirportLatitude = arrivalAirportLatitude;
    }

    public Double getArrivalAirportLongitude() {
        return arrivalAirportLongitude;
    }

    public void setArrivalAirportLongitude(Double arrivalAirportLongitude) {
        this.arrivalAirportLongitude = arrivalAirportLongitude;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public String getTransferMode() {
        return transferMode;
    }

    public void setTransferMode(String transferMode) {
        this.transferMode = transferMode;
    }
}
