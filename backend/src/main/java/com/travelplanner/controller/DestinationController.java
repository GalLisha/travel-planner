package com.travelplanner.controller;

import com.travelplanner.dto.request.DestinationSuggestionRequestDto;
import com.travelplanner.dto.response.DestinationDto;
import com.travelplanner.http.RequestContext;
import com.travelplanner.http.Router;
import com.travelplanner.repository.DestinationRepository;
import com.travelplanner.service.DestinationRecommendationService;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class DestinationController {

    private final DestinationRepository destinationRepository;
    private final DestinationRecommendationService recommendationService;

    public DestinationController(DestinationRepository destinationRepository,
                                  DestinationRecommendationService recommendationService) {
        this.destinationRepository = destinationRepository;
        this.recommendationService = recommendationService;
    }

    public void registerRoutes(Router router) {
        router.get("/api/destinations", this::listAll);
        router.post("/api/destinations/suggestions", this::suggest);
    }

    private void listAll(RequestContext ctx) throws IOException {
        List<DestinationDto> dtos = destinationRepository.findAll().stream()
                .map(DestinationDto::fromModel)
                .collect(Collectors.toList());
        ctx.sendJson(200, dtos);
    }

    private void suggest(RequestContext ctx) throws IOException {
        DestinationSuggestionRequestDto request = ctx.bodyAs(DestinationSuggestionRequestDto.class);
        List<DestinationDto> suggestions = recommendationService.suggest(request);
        ctx.sendJson(200, suggestions);
    }
}
