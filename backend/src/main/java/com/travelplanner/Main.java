package com.travelplanner;

import com.mongodb.client.MongoDatabase;
import com.sun.net.httpserver.HttpServer;
import com.travelplanner.controller.AirportController;
import com.travelplanner.controller.AttractionController;
import com.travelplanner.controller.AuthController;
import com.travelplanner.controller.BookingController;
import com.travelplanner.controller.CityController;
import com.travelplanner.controller.DestinationController;
import com.travelplanner.controller.HotelController;
import com.travelplanner.controller.ItineraryController;
import com.travelplanner.controller.TripController;
import com.travelplanner.db.MongoConfig;
import com.travelplanner.http.Router;
import com.travelplanner.repository.AttractionRepository;
import com.travelplanner.repository.DestinationRepository;
import com.travelplanner.repository.ItineraryRepository;
import com.travelplanner.repository.SavedTripRepository;
import com.travelplanner.repository.UserRepository;
import com.travelplanner.service.AuthService;
import com.travelplanner.service.DestinationRecommendationService;
import com.travelplanner.service.ItineraryService;
import com.travelplanner.service.RouteOptimizationService;
import com.travelplanner.service.TripService;
import com.travelplanner.service.ai.AiService;
import com.travelplanner.service.ai.GeminiAiService;
import com.travelplanner.service.ai.NullAiService;
import com.travelplanner.service.airport.AirportService;
import com.travelplanner.service.airport.OverpassAirportService;
import com.travelplanner.service.booking.FlightBookingService;
import com.travelplanner.service.booking.HotelBookingService;
import com.travelplanner.service.booking.UnsupportedFlightBookingService;
import com.travelplanner.service.booking.UnsupportedHotelBookingService;
import com.travelplanner.service.city.CityService;
import com.travelplanner.service.city.PhotonCityService;
import com.travelplanner.service.hotel.GooglePlacesHotelService;
import com.travelplanner.service.hotel.HotelService;
import com.travelplanner.service.hotel.OverpassHotelService;
import com.travelplanner.util.TokenService;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Application entry point. Pure Java - no Spring / no web framework: wires
 * repositories, services and controllers by hand and serves them over the
 * JDK's built-in {@link HttpServer}.
 */
public final class Main {

    public static void main(String[] args) throws Exception {
        int port = resolvePort();

        // Repositories (mock/in-memory data access layer)
        DestinationRepository destinationRepository = new DestinationRepository();
        AttractionRepository attractionRepository = new AttractionRepository();
        ItineraryRepository itineraryRepository = new ItineraryRepository();

        // AI features (hotel-search fallback, hotel-name validation as a last resort,
        // personalized attraction recommendations) are entirely optional - disabled out of
        // the box, enabled by setting GEMINI_API_KEY. Every caller holds a non-null AiService
        // and never needs to null-check; NullAiService simply returns nothing, which every
        // call site already treats as "AI found nothing new".
        String geminiApiKey = System.getenv("GEMINI_API_KEY");
        AiService aiService = (geminiApiKey != null && !geminiApiKey.trim().isEmpty())
                ? new GeminiAiService(geminiApiKey.trim())
                : new NullAiService();

        // Services (business logic layer)
        RouteOptimizationService routeOptimizationService = new RouteOptimizationService();
        DestinationRecommendationService recommendationService =
                new DestinationRecommendationService(destinationRepository);
        ItineraryService itineraryService = new ItineraryService(
                destinationRepository, attractionRepository, itineraryRepository, routeOptimizationService, aiService);

        // Booking services are stubs today; swapping these two lines for real
        // implementations is the only change needed to enable booking later.
        FlightBookingService flightBookingService = new UnsupportedFlightBookingService();
        HotelBookingService hotelBookingService = new UnsupportedHotelBookingService();

        // City search: Photon (komoot), free, keyless, and built for autocomplete -
        // unlike Nominatim's public API, whose usage policy disallows that traffic pattern.
        CityService cityService = new PhotonCityService();

        // Airport search: same free/keyless Overpass approach as hotels - aerodromes
        // tagged with an IATA code, searched within a wide radius of the destination.
        AirportService airportService = new OverpassAirportService();

        // Hotel search: live Google Places if a key is configured (richer data: pricing,
        // ratings, photos), otherwise the free/keyless OpenStreetMap-backed Overpass
        // provider - the rest of the app is unaffected either way.
        String googlePlacesApiKey = System.getenv("GOOGLE_PLACES_API_KEY");
        HotelService hotelService = (googlePlacesApiKey != null && !googlePlacesApiKey.trim().isEmpty())
                ? new GooglePlacesHotelService(googlePlacesApiKey.trim())
                : new OverpassHotelService();

        // Controllers (HTTP layer) + routing
        Router router = new Router();
        new DestinationController(destinationRepository, recommendationService).registerRoutes(router);
        new AttractionController(attractionRepository).registerRoutes(router);
        new ItineraryController(itineraryService).registerRoutes(router);
        new BookingController(flightBookingService, hotelBookingService).registerRoutes(router);
        new CityController(cityService, destinationRepository).registerRoutes(router);
        new HotelController(hotelService, aiService).registerRoutes(router);
        new AirportController(airportService).registerRoutes(router);

        // User accounts + saved trips: only enabled when a MongoDB connection string is
        // configured (sign-up/sign-in/save-trip routes simply don't exist otherwise -
        // every other feature works the same with or without it).
        String mongoUri = System.getenv("MONGODB_URI");
        if (mongoUri != null && !mongoUri.trim().isEmpty()) {
            String dbName = System.getenv("MONGODB_DB_NAME");
            MongoDatabase database = MongoConfig.connect(mongoUri.trim(),
                    (dbName != null && !dbName.trim().isEmpty()) ? dbName.trim() : "vacation_planner");

            UserRepository userRepository = new UserRepository(database);
            SavedTripRepository savedTripRepository = new SavedTripRepository(database);
            TokenService tokenService = new TokenService(System.getenv("JWT_SECRET"));
            AuthService authService = new AuthService(userRepository, tokenService);
            TripService tripService = new TripService(savedTripRepository);

            new AuthController(authService).registerRoutes(router);
            new TripController(tripService, authService).registerRoutes(router);
            System.out.println("User accounts: enabled (MongoDB)");
        } else {
            System.out.println("User accounts: disabled (no MONGODB_URI configured)");
        }

        router.get("/api/health", ctx -> ctx.sendJson(200, new HealthStatus("ok")));

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", router);
        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();

        System.out.println("Vacation Planner backend listening on http://localhost:" + port);
        System.out.println("Hotel provider: " + (hotelService instanceof GooglePlacesHotelService ? "Google Places (live)" : "OpenStreetMap/Overpass (live)"));
        System.out.println("AI features: " + (aiService.isEnabled() ? "enabled (Gemini)" : "disabled (no GEMINI_API_KEY configured)"));
    }

    private static int resolvePort() {
        String envPort = System.getenv("PORT");
        if (envPort != null && !envPort.trim().isEmpty()) {
            try {
                return Integer.parseInt(envPort.trim());
            } catch (NumberFormatException ignored) {
                // fall through to default
            }
        }
        return 8080;
    }

    private static final class HealthStatus {
        final String status;

        HealthStatus(String status) {
            this.status = status;
        }
    }
}
