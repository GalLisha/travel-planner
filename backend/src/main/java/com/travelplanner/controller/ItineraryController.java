package com.travelplanner.controller;

import com.travelplanner.dto.request.GenerateItineraryRequestDto;
import com.travelplanner.dto.request.ReplaceAttractionRequestDto;
import com.travelplanner.dto.response.AttractionDto;
import com.travelplanner.dto.response.ItineraryResponseDto;
import com.travelplanner.exception.BadRequestException;
import com.travelplanner.http.RequestContext;
import com.travelplanner.http.Router;
import com.travelplanner.model.Attraction;
import com.travelplanner.model.Itinerary;
import com.travelplanner.service.ItineraryService;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class ItineraryController {

    private final ItineraryService itineraryService;

    public ItineraryController(ItineraryService itineraryService) {
        this.itineraryService = itineraryService;
    }

    public void registerRoutes(Router router) {
        router.post("/api/itinerary", this::generate);
        router.get("/api/itinerary/{id}", this::getOne);
        router.put("/api/itinerary/{id}/replace", this::replace);
        router.get("/api/itinerary/{id}/alternatives", this::alternatives);
    }

    private void generate(RequestContext ctx) throws IOException {
        GenerateItineraryRequestDto request = ctx.bodyAs(GenerateItineraryRequestDto.class);
        Itinerary itinerary = itineraryService.generateItinerary(request);
        ctx.sendJson(201, ItineraryResponseDto.fromModel(itinerary));
    }

    private void getOne(RequestContext ctx) throws IOException {
        Itinerary itinerary = itineraryService.getItinerary(ctx.pathParam("id"));
        ctx.sendJson(200, ItineraryResponseDto.fromModel(itinerary));
    }

    private void replace(RequestContext ctx) throws IOException {
        ReplaceAttractionRequestDto request = ctx.bodyAs(ReplaceAttractionRequestDto.class);
        Itinerary itinerary = itineraryService.replaceAttraction(ctx.pathParam("id"), request);
        ctx.sendJson(200, ItineraryResponseDto.fromModel(itinerary));
    }

    private void alternatives(RequestContext ctx) throws IOException {
        String dayNumberParam = ctx.queryParam("dayNumber");
        String attractionId = ctx.queryParam("attractionId");
        if (dayNumberParam == null || attractionId == null) {
            throw new BadRequestException("dayNumber and attractionId query parameters are required");
        }
        int dayNumber;
        try {
            dayNumber = Integer.parseInt(dayNumberParam);
        } catch (NumberFormatException ex) {
            throw new BadRequestException("dayNumber must be an integer");
        }
        List<Attraction> alternatives = itineraryService.findAlternatives(ctx.pathParam("id"), dayNumber, attractionId);
        List<AttractionDto> dtos = alternatives.stream().map(AttractionDto::fromModel).collect(Collectors.toList());
        ctx.sendJson(200, dtos);
    }
}
