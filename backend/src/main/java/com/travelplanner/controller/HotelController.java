package com.travelplanner.controller;

import com.travelplanner.dto.response.HotelDto;
import com.travelplanner.exception.BadRequestException;
import com.travelplanner.http.RequestContext;
import com.travelplanner.http.Router;
import com.travelplanner.model.Hotel;
import com.travelplanner.service.hotel.HotelSearchQuery;
import com.travelplanner.service.hotel.HotelService;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class HotelController {

    private final HotelService hotelService;

    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    public void registerRoutes(Router router) {
        router.get("/api/hotels/search", this::search);
    }

    private void search(RequestContext ctx) throws IOException {
        String city = ctx.queryParam("city");
        String country = ctx.queryParam("country");
        String latParam = ctx.queryParam("lat");
        String lonParam = ctx.queryParam("lon");
        if (city == null || latParam == null || lonParam == null) {
            throw new BadRequestException("city, lat and lon query parameters are required");
        }

        double lat;
        double lon;
        try {
            lat = Double.parseDouble(latParam);
            lon = Double.parseDouble(lonParam);
        } catch (NumberFormatException e) {
            throw new BadRequestException("lat and lon must be numbers");
        }

        HotelSearchQuery query = new HotelSearchQuery(city, country == null ? "" : country, lat, lon);
        List<Hotel> hotels = hotelService.searchHotels(query);
        List<HotelDto> dtos = hotels.stream().map(HotelDto::fromModel).collect(Collectors.toList());
        ctx.sendJson(200, dtos);
    }
}
