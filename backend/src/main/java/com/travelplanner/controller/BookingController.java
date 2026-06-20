package com.travelplanner.controller;

import com.travelplanner.dto.request.BookingRequestDto;
import com.travelplanner.dto.response.BookingResponseDto;
import com.travelplanner.http.RequestContext;
import com.travelplanner.http.Router;
import com.travelplanner.service.booking.FlightBookingService;
import com.travelplanner.service.booking.HotelBookingService;

import java.io.IOException;

/**
 * Exposes the (currently unsupported) flight/hotel booking flow. Kept as a real
 * endpoint - rather than a frontend-only message - so a future version can swap
 * in a working {@link FlightBookingService}/{@link HotelBookingService} without
 * changing the API contract the frontend already calls.
 */
public class BookingController {

    private final FlightBookingService flightBookingService;
    private final HotelBookingService hotelBookingService;

    public BookingController(FlightBookingService flightBookingService, HotelBookingService hotelBookingService) {
        this.flightBookingService = flightBookingService;
        this.hotelBookingService = hotelBookingService;
    }

    public void registerRoutes(Router router) {
        router.post("/api/bookings/flights", this::bookFlight);
        router.post("/api/bookings/hotels", this::bookHotel);
    }

    private void bookFlight(RequestContext ctx) throws IOException {
        BookingRequestDto request = ctx.bodyAs(BookingRequestDto.class);
        BookingResponseDto response = flightBookingService.searchAndBook(request);
        ctx.sendJson(200, response);
    }

    private void bookHotel(RequestContext ctx) throws IOException {
        BookingRequestDto request = ctx.bodyAs(BookingRequestDto.class);
        BookingResponseDto response = hotelBookingService.searchAndBook(request);
        ctx.sendJson(200, response);
    }
}
