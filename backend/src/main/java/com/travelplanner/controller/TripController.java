package com.travelplanner.controller;

import com.travelplanner.dto.request.SaveTripRequestDto;
import com.travelplanner.dto.response.SavedTripDto;
import com.travelplanner.http.RequestContext;
import com.travelplanner.http.Router;
import com.travelplanner.model.SavedTrip;
import com.travelplanner.model.User;
import com.travelplanner.service.AuthService;
import com.travelplanner.service.TripService;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class TripController {

    private final TripService tripService;
    private final AuthService authService;

    public TripController(TripService tripService, AuthService authService) {
        this.tripService = tripService;
        this.authService = authService;
    }

    public void registerRoutes(Router router) {
        router.post("/api/trips", this::save);
        router.get("/api/trips", this::list);
    }

    private void save(RequestContext ctx) throws IOException {
        User user = authService.resolveUser(ctx.header("Authorization"));
        SaveTripRequestDto request = ctx.bodyAs(SaveTripRequestDto.class);
        SavedTrip trip = tripService.saveTrip(user.getId(), request);
        ctx.sendJson(201, SavedTripDto.fromModel(trip));
    }

    private void list(RequestContext ctx) throws IOException {
        User user = authService.resolveUser(ctx.header("Authorization"));
        List<SavedTripDto> dtos = tripService.listTrips(user.getId()).stream()
                .map(SavedTripDto::fromModel)
                .collect(Collectors.toList());
        ctx.sendJson(200, dtos);
    }
}
