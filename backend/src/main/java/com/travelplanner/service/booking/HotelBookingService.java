package com.travelplanner.service.booking;

import com.travelplanner.dto.request.BookingRequestDto;
import com.travelplanner.dto.response.BookingResponseDto;

/**
 * Abstraction for hotel search/booking. The current implementation is a stub
 * that always reports the feature as unsupported; a future version can add an
 * implementation backed by a real hotel-search/booking API without any
 * change to callers.
 */
public interface HotelBookingService {
    BookingResponseDto searchAndBook(BookingRequestDto request);
}
