package com.travelplanner.dto.response;

import com.travelplanner.model.Hotel;

/** Response for the single-result {@code /api/hotels/lookup} endpoint - same hotel fields
 * as {@link HotelDto} plus which step of the validation waterfall produced the match, so
 * the frontend can tell the user when a result came from AI rather than a real provider. */
public class HotelLookupDto {
    private String id;
    private String name;
    private Double starRating;
    private String address;
    private double latitude;
    private double longitude;
    private String matchSource;

    public static HotelLookupDto fromModel(Hotel hotel, String matchSource) {
        HotelLookupDto dto = new HotelLookupDto();
        dto.id = hotel.getId();
        dto.name = hotel.getName();
        dto.starRating = hotel.getStarRating();
        dto.address = hotel.getAddress();
        dto.latitude = hotel.getLocation() != null ? hotel.getLocation().getLatitude() : 0;
        dto.longitude = hotel.getLocation() != null ? hotel.getLocation().getLongitude() : 0;
        dto.matchSource = matchSource;
        return dto;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Double getStarRating() {
        return starRating;
    }

    public String getAddress() {
        return address;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getMatchSource() {
        return matchSource;
    }
}
