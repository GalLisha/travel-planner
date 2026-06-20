package com.travelplanner.controller;

import com.travelplanner.dto.response.AttractionDto;
import com.travelplanner.exception.BadRequestException;
import com.travelplanner.http.RequestContext;
import com.travelplanner.http.Router;
import com.travelplanner.repository.AttractionRepository;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class AttractionController {

    private final AttractionRepository attractionRepository;

    public AttractionController(AttractionRepository attractionRepository) {
        this.attractionRepository = attractionRepository;
    }

    public void registerRoutes(Router router) {
        router.get("/api/attractions", this::listByDestination);
    }

    private void listByDestination(RequestContext ctx) throws IOException {
        String destinationId = ctx.queryParam("destinationId");
        if (destinationId == null || destinationId.trim().isEmpty()) {
            throw new BadRequestException("destinationId query parameter is required");
        }
        List<AttractionDto> dtos = attractionRepository.findByDestination(destinationId).stream()
                .map(AttractionDto::fromModel)
                .collect(Collectors.toList());
        ctx.sendJson(200, dtos);
    }
}
