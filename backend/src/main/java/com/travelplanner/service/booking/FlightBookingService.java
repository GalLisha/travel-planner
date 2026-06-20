package com.travelplanner.service.booking;

import com.travelplanner.dto.request.BookingRequestDto;
import com.travelplanner.dto.response.BookingResponseDto;

/**
 * Abstraction for flight search/booking. The current implementation is a stub
 * that always reports the feature as unsupported; a future version can add an
 * implementation backed by a real flight-search/booking API without any
 * change to callers.
 */
public interface FlightBookingService {
    BookingResponseDto searchAndBook(BookingRequestDto request);
}
