package com.travelplanner.controller;

import com.travelplanner.dto.response.CityDto;
import com.travelplanner.exception.BadRequestException;
import com.travelplanner.http.RequestContext;
import com.travelplanner.http.Router;
import com.travelplanner.model.City;
import com.travelplanner.model.Destination;
import com.travelplanner.repository.DestinationRepository;
import com.travelplanner.service.city.CityService;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CityController {

    // City search providers (and free-text country names) don't always agree with the
    // curated catalog's country spelling ("USA" vs "United States"), so matching by name
    // alone risks false positives - e.g. a small town in Taiwan also named "Bali". ISO
    // country codes are unambiguous, so each curated destination is pinned to one here.
    private static final Map<String, String> CURATED_COUNTRY_CODES = new HashMap<>();

    static {
        CURATED_COUNTRY_CODES.put("dest-paris", "FR");
        CURATED_COUNTRY_CODES.put("dest-orlando", "US");
        CURATED_COUNTRY_CODES.put("dest-santorini", "GR");
        CURATED_COUNTRY_CODES.put("dest-bangkok", "TH");
        CURATED_COUNTRY_CODES.put("dest-bali", "ID");
        CURATED_COUNTRY_CODES.put("dest-cancun", "MX");
        CURATED_COUNTRY_CODES.put("dest-capetown", "ZA");
        CURATED_COUNTRY_CODES.put("dest-tokyo", "JP");
        CURATED_COUNTRY_CODES.put("dest-sydney", "AU");
        CURATED_COUNTRY_CODES.put("dest-newyork", "US");
    }

    private final CityService cityService;
    private final DestinationRepository destinationRepository;

    public CityController(CityService cityService, DestinationRepository destinationRepository) {
        this.cityService = cityService;
        this.destinationRepository = destinationRepository;
    }

    public void registerRoutes(Router router) {
        router.get("/api/cities/search", this::search);
    }

    private void search(RequestContext ctx) throws IOException {
        String query = ctx.queryParam("query");
        if (query == null || query.trim().length() < 2) {
            throw new BadRequestException("query must be at least 2 characters");
        }
        String countryCode = ctx.queryParam("countryCode");

        List<City> cities = cityService.search(query, countryCode);
        cities.forEach(this::attachCuratedDestinationIfMatched);

        List<CityDto> dtos = cities.stream().map(CityDto::fromModel).collect(Collectors.toList());
        ctx.sendJson(200, dtos);
    }

    /**
     * If a searched city's name matches one of our curated destinations, link the two so the
     * frontend knows a full attraction-based itinerary is available for it.
     */
    private void attachCuratedDestinationIfMatched(City city) {
        if (city.getCountryCode() == null) {
            return;
        }
        for (Destination destination : destinationRepository.findAll()) {
            String expectedCountryCode = CURATED_COUNTRY_CODES.get(destination.getId());
            boolean nameMatches = destination.getName().equalsIgnoreCase(city.getName());
            boolean countryMatches = expectedCountryCode != null && expectedCountryCode.equalsIgnoreCase(city.getCountryCode());
            if (nameMatches && countryMatches) {
                city.setCuratedDestinationId(destination.getId());
                return;
            }
        }
    }
}
