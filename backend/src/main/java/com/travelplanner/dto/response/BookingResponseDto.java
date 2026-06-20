package com.travelplanner.dto.response;

/** Response returned by the booking stub endpoints until real provider integration exists. */
public class BookingResponseDto {
    private boolean supported;
    private String message;

    public BookingResponseDto() {
    }

    public BookingResponseDto(boolean supported, String message) {
        this.supported = supported;
        this.message = message;
    }

    public boolean isSupported() {
        return supported;
    }

    public void setSupported(boolean supported) {
        this.supported = supported;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
