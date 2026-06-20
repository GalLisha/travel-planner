package com.travelplanner.service.hotel;

import com.travelplanner.model.Hotel;

import java.util.List;

/**
 * Abstraction over a hotel search provider. {@link MockHotelService} is the
 * zero-config default; {@link GooglePlacesHotelService} is a real, ready-to-activate
 * implementation. Swapping providers (e.g. for Amadeus or Booking.com later) only
 * requires a new implementation of this interface - no caller-side changes.
 */
public interface HotelService {
    List<Hotel> searchHotels(HotelSearchQuery query);
}
