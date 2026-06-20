package com.travelplanner.dto.request;

/**
 * Placeholder request shape for the not-yet-implemented flight/hotel booking flow.
 * Kept intentionally generic so a future real integration can extend it without
 * breaking the controller contract.
 */
public class BookingRequestDto {
    private String destinationId;
    private String departureDate;
    private String returnDate;

    public String getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(String destinationId) {
        this.destinationId = destinationId;
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
}
