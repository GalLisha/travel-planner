package com.travelplanner.controller;

import com.travelplanner.dto.response.HotelDto;
import com.travelplanner.dto.response.HotelLookupDto;
import com.travelplanner.exception.BadRequestException;
import com.travelplanner.exception.NotFoundException;
import com.travelplanner.http.RequestContext;
import com.travelplanner.http.Router;
import com.travelplanner.model.GeoPoint;
import com.travelplanner.model.Hotel;
import com.travelplanner.service.ai.AiHotelLookupResult;
import com.travelplanner.service.ai.AiHotelSuggestion;
import com.travelplanner.service.ai.AiService;
import com.travelplanner.service.hotel.HotelSearchQuery;
import com.travelplanner.service.hotel.HotelService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class HotelController {

    private static final int AI_HOTEL_SUGGESTION_COUNT = 10;

    private final HotelService hotelService;
    private final AiService aiService;

    public HotelController(HotelService hotelService, AiService aiService) {
        this.hotelService = hotelService;
        this.aiService = aiService;
    }

    public void registerRoutes(Router router) {
        router.get("/api/hotels/search", this::search);
        router.get("/api/hotels/lookup", this::lookup);
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
        List<Hotel> hotels;
        try {
            hotels = hotelService.searchHotels(query);
        } catch (RuntimeException e) {
            // A transport failure (e.g. the provider rate-limiting us) is treated the same
            // as "no results" - it should trigger the AI fallback, not a 500.
            System.err.println("Hotel provider search failed: " + e.getMessage());
            hotels = new ArrayList<>();
        }
        if (hotels.isEmpty() && aiService.isEnabled()) {
            hotels = aiFallbackHotels(query);
        }
        List<HotelDto> dtos = hotels.stream().map(HotelDto::fromModel).collect(Collectors.toList());
        ctx.sendJson(200, dtos);
    }

    /** Real providers found nothing for this city (e.g. Saint Petersburg) - ask the AI for
     * well-known hotels instead, clearly flagged so the frontend can mark them unverified. */
    private List<Hotel> aiFallbackHotels(HotelSearchQuery query) {
        try {
            List<AiHotelSuggestion> suggestions = aiService.suggestHotels(
                    query.getCityName(), query.getCountryName(), AI_HOTEL_SUGGESTION_COUNT);
            List<Hotel> hotels = new ArrayList<>();
            for (AiHotelSuggestion s : suggestions) {
                if (s.name == null || s.name.trim().isEmpty()) {
                    continue;
                }
                Hotel hotel = new Hotel();
                hotel.setId("ai-" + UUID.randomUUID());
                hotel.setName(s.name);
                hotel.setStarRating(s.starRating);
                hotel.setAddress(s.approximateAddress != null ? s.approximateAddress : query.getCityName());
                hotel.setLocation(new GeoPoint(query.getLatitude(), query.getLongitude()));
                hotel.setDistanceFromCenterKm(0);
                hotel.setAiSourced(true);
                hotels.add(hotel);
            }
            return hotels;
        } catch (RuntimeException e) {
            System.err.println("AI hotel fallback failed: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private void lookup(RequestContext ctx) throws IOException {
        String name = ctx.queryParam("name");
        String city = ctx.queryParam("city");
        String country = ctx.queryParam("country");
        String latParam = ctx.queryParam("lat");
        String lonParam = ctx.queryParam("lon");
        if (name == null || name.trim().isEmpty() || city == null || latParam == null || lonParam == null) {
            throw new BadRequestException("name, city, lat and lon query parameters are required");
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
        String trimmedName = name.trim();

        // Step 1: the configured hotel provider (Google Places text search when a key is
        // set, otherwise the free/keyless OSM Photon geocoder) - a transport failure here
        // falls through to the AI step rather than failing the whole request.
        Optional<Hotel> match;
        String matchSource;
        try {
            match = hotelService.lookupByName(trimmedName, query);
            matchSource = match.isPresent() ? "PROVIDER" : null;
        } catch (RuntimeException e) {
            System.err.println("Hotel provider lookup failed: " + e.getMessage());
            match = Optional.empty();
            matchSource = null;
        }

        // Step 2: AI plausibility check, only once the real provider has nothing.
        if (!match.isPresent() && aiService.isEnabled()) {
            Optional<AiHotelLookupResult> aiResult = aiService.assessHotelExistence(trimmedName, city, country);
            if (aiResult.isPresent() && aiResult.get().plausible) {
                Hotel hotel = new Hotel();
                hotel.setId("ai-" + UUID.randomUUID());
                hotel.setName(trimmedName);
                hotel.setAddress(aiResult.get().approximateAddress != null ? aiResult.get().approximateAddress : city);
                hotel.setLocation(new GeoPoint(lat, lon));
                hotel.setAiSourced(true);
                match = Optional.of(hotel);
                matchSource = "AI";
            }
        }

        if (!match.isPresent()) {
            throw new NotFoundException(
                    "We couldn't find \"" + trimmedName + "\" near " + city
                            + ". Double-check the spelling, or try browsing hotels instead.");
        }
        ctx.sendJson(200, HotelLookupDto.fromModel(match.get(), matchSource));
    }
}
