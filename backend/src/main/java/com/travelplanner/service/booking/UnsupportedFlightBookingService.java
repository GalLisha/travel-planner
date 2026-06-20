package com.travelplanner.service.booking;

import com.travelplanner.dto.request.BookingRequestDto;
import com.travelplanner.dto.response.BookingResponseDto;

public class UnsupportedFlightBookingService implements FlightBookingService {
    @Override
    public BookingResponseDto searchAndBook(BookingRequestDto request) {
        return new BookingResponseDto(false,
                "Flight booking isn't available yet. This feature is coming in a future version of the app - " +
                        "for now, please book your flights separately and enter your travel dates manually.");
    }
}
