package com.travelplanner.service.ai;

import com.google.gson.JsonSyntaxException;
import com.travelplanner.http.JsonUtil;
import com.travelplanner.model.ActivityTag;
import com.travelplanner.model.TravelGroupType;
import com.travelplanner.util.SimpleHttpClient;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Real {@link AiService} backed by Google's Gemini {@code generateContent} REST API.
 * Every public method is exception-safe: transport failures, timeouts and malformed
 * responses are all caught here and degrade to an empty/absent result, so callers never
 * need to handle an AI-specific failure mode beyond "found nothing this time."
 */
public class GeminiAiService implements AiService {

    private static final String ENDPOINT_TEMPLATE =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent";
    private static final String MODEL = "gemini-2.5-flash";
    private static final int CONNECT_TIMEOUT_MS = 4000;
    private static final int READ_TIMEOUT_MS = 25000;

    private final String apiKey;

    public GeminiAiService(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public List<AiHotelSuggestion> suggestHotels(String cityName, String countryName, int maxResults) {
        String prompt = "Suggest " + maxResults + " real, well-known hotels in " + cityName
                + (countryName != null && !countryName.isEmpty() ? ", " + countryName : "")
                + ". Respond with JSON only, in this exact shape: "
                + "{\"hotels\":[{\"name\":string,\"approximateAddress\":string,\"starRating\":number|null}]}";

        return generateJson(prompt)
                .map(json -> {
                    try {
                        HotelsWrapper wrapper = JsonUtil.GSON.fromJson(json, HotelsWrapper.class);
                        return wrapper != null && wrapper.hotels != null ? wrapper.hotels : Collections.<AiHotelSuggestion>emptyList();
                    } catch (JsonSyntaxException e) {
                        return Collections.<AiHotelSuggestion>emptyList();
                    }
                })
                .orElse(Collections.emptyList());
    }

    @Override
    public Optional<AiHotelLookupResult> assessHotelExistence(String hotelName, String cityName, String countryName) {
        String prompt = "Does a hotel named \"" + hotelName + "\" plausibly exist in " + cityName
                + (countryName != null && !countryName.isEmpty() ? ", " + countryName : "")
                + "? Respond with JSON only, in this exact shape: "
                + "{\"plausible\":boolean,\"approximateAddress\":string|null}";

        return generateJson(prompt).flatMap(json -> {
            try {
                AiHotelLookupResult result = JsonUtil.GSON.fromJson(json, AiHotelLookupResult.class);
                return Optional.ofNullable(result);
            } catch (JsonSyntaxException e) {
                return Optional.empty();
            }
        });
    }

    @Override
    public List<AiAttractionSuggestion> suggestAttractions(String destinationName, String countryName,
                                                             TravelGroupType groupType, int maxResults) {
        String validCategories = Arrays.toString(ActivityTag.values());
        String audience = groupType != null ? groupType.name().toLowerCase(Locale.ROOT) + " trip" : "general trip";
        String prompt = "Suggest " + maxResults + " real tourist attractions in " + destinationName
                + (countryName != null && !countryName.isEmpty() ? ", " + countryName : "")
                + " best suited for a " + audience + ". "
                + "Each attraction must include its real approximate latitude and longitude. "
                + "The category field must be exactly one of: " + validCategories + ". "
                + "Respond with JSON only, in this exact shape: "
                + "{\"attractions\":[{\"name\":string,\"description\":string,\"category\":string,"
                + "\"averageVisitDurationMinutes\":number,\"latitude\":number,\"longitude\":number}]}";

        return generateJson(prompt)
                .map(json -> {
                    try {
                        AttractionsWrapper wrapper = JsonUtil.GSON.fromJson(json, AttractionsWrapper.class);
                        return wrapper != null && wrapper.attractions != null
                                ? wrapper.attractions : Collections.<AiAttractionSuggestion>emptyList();
                    } catch (JsonSyntaxException e) {
                        return Collections.<AiAttractionSuggestion>emptyList();
                    }
                })
                .orElse(Collections.emptyList());
    }

    private Optional<String> generateJson(String prompt) {
        String url = String.format(ENDPOINT_TEMPLATE, MODEL);

        GeminiRequest request = new GeminiRequest();
        Content content = new Content();
        Part part = new Part();
        part.text = prompt;
        content.parts = new Part[]{part};
        request.contents = new Content[]{content};
        request.generationConfig = new GenerationConfig();
        request.generationConfig.responseMimeType = "application/json";
        // These are simple structured-output lookups, not requests needing deep reasoning -
        // disabling "thinking" cuts latency/cost noticeably on thinking-enabled flash models.
        request.generationConfig.thinkingConfig = new ThinkingConfig();
        request.generationConfig.thinkingConfig.thinkingBudget = 0;

        String requestBody = JsonUtil.GSON.toJson(request);

        try {
            Map<String, String> headers = new LinkedHashMap<>();
            headers.put("X-goog-api-key", apiKey);
            String json = SimpleHttpClient.postJson(url, headers, requestBody, CONNECT_TIMEOUT_MS, READ_TIMEOUT_MS);
            GeminiResponse response = JsonUtil.GSON.fromJson(json, GeminiResponse.class);
            return Optional.ofNullable(extractText(response));
        } catch (IOException | JsonSyntaxException e) {
            System.err.println("Gemini call failed: " + e.getMessage());
            return Optional.empty();
        }
    }

    private static String extractText(GeminiResponse response) {
        if (response == null || response.candidates == null || response.candidates.length == 0) {
            return null;
        }
        Content content = response.candidates[0].content;
        if (content == null || content.parts == null || content.parts.length == 0) {
            return null;
        }
        String text = content.parts[0].text;
        return (text == null || text.trim().isEmpty()) ? null : text;
    }

    private static class HotelsWrapper {
        List<AiHotelSuggestion> hotels;
    }

    private static class AttractionsWrapper {
        List<AiAttractionSuggestion> attractions;
    }

    private static class GeminiRequest {
        Content[] contents;
        GenerationConfig generationConfig;
    }

    private static class Content {
        Part[] parts;
    }

    private static class Part {
        String text;
    }

    private static class GenerationConfig {
        String responseMimeType;
        ThinkingConfig thinkingConfig;
    }

    private static class ThinkingConfig {
        int thinkingBudget;
    }

    private static class GeminiResponse {
        Candidate[] candidates;
    }

    private static class Candidate {
        Content content;
    }
}
