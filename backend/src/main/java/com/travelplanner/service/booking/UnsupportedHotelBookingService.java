package com.travelplanner.service.booking;

import com.travelplanner.dto.request.BookingRequestDto;
import com.travelplanner.dto.response.BookingResponseDto;

public class UnsupportedHotelBookingService implements HotelBookingService {
    @Override
    public BookingResponseDto searchAndBook(BookingRequestDto request) {
        return new BookingResponseDto(false,
                "Hotel booking isn't available yet. This feature is coming in a future version of the app - " +
                        "for now, please book your hotel separately and enter its details manually.");
    }
}
