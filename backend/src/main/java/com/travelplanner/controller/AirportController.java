package com.travelplanner.controller;

import com.travelplanner.dto.response.AirportDto;
import com.travelplanner.exception.BadRequestException;
import com.travelplanner.http.RequestContext;
import com.travelplanner.http.Router;
import com.travelplanner.model.Airport;
import com.travelplanner.service.airport.AirportService;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class AirportController {

    private final AirportService airportService;

    public AirportController(AirportService airportService) {
        this.airportService = airportService;
    }

    public void registerRoutes(Router router) {
        router.get("/api/airports/search", this::search);
    }

    private void search(RequestContext ctx) throws IOException {
        String latParam = ctx.queryParam("lat");
        String lonParam = ctx.queryParam("lon");
        if (latParam == null || lonParam == null) {
            throw new BadRequestException("lat and lon query parameters are required");
        }

        double lat;
        double lon;
        try {
            lat = Double.parseDouble(latParam);
            lon = Double.parseDouble(lonParam);
        } catch (NumberFormatException e) {
            throw new BadRequestException("lat and lon must be numbers");
        }

        List<Airport> airports = airportService.findNear(lat, lon);
        List<AirportDto> dtos = airports.stream().map(AirportDto::fromModel).collect(Collectors.toList());
        ctx.sendJson(200, dtos);
    }
}
