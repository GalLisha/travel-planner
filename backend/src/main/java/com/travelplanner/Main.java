package com.travelplanner;

import com.sun.net.httpserver.HttpServer;
import com.travelplanner.controller.AttractionController;
import com.travelplanner.controller.BookingController;
import com.travelplanner.controller.CityController;
import com.travelplanner.controller.DestinationController;
import com.travelplanner.controller.HotelController;
import com.travelplanner.controller.ItineraryController;
import com.travelplanner.http.Router;
import com.travelplanner.repository.AttractionRepository;
import com.travelplanner.repository.DestinationRepository;
import com.travelplanner.repository.ItineraryRepository;
import com.travelplanner.service.DestinationRecommendationService;
import com.travelplanner.service.ItineraryService;
import com.travelplanner.service.RouteOptimizationService;
import com.travelplanner.service.booking.FlightBookingService;
import com.travelplanner.service.booking.HotelBookingService;
import com.travelplanner.service.booking.UnsupportedFlightBookingService;
import com.travelplanner.service.booking.UnsupportedHotelBookingService;
import com.travelplanner.service.city.CityService;
import com.travelplanner.service.city.PhotonCityService;
import com.travelplanner.service.hotel.GooglePlacesHotelService;
import com.travelplanner.service.hotel.HotelService;
import com.travelplanner.service.hotel.MockHotelService;

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

        // Services (business logic layer)
        RouteOptimizationService routeOptimizationService = new RouteOptimizationService();
        DestinationRecommendationService recommendationService =
                new DestinationRecommendationService(destinationRepository);
        ItineraryService itineraryService = new ItineraryService(
                destinationRepository, attractionRepository, itineraryRepository, routeOptimizationService);

        // Booking services are stubs today; swapping these two lines for real
        // implementations is the only change needed to enable booking later.
        FlightBookingService flightBookingService = new UnsupportedFlightBookingService();
        HotelBookingService hotelBookingService = new UnsupportedHotelBookingService();

        // City search: Photon (komoot), free, keyless, and built for autocomplete -
        // unlike Nominatim's public API, whose usage policy disallows that traffic pattern.
        CityService cityService = new PhotonCityService();

        // Hotel search: live Google Places if a key is configured, otherwise a
        // deterministic mock provider - the rest of the app is unaffected either way.
        String googlePlacesApiKey = System.getenv("GOOGLE_PLACES_API_KEY");
        HotelService hotelService = (googlePlacesApiKey != null && !googlePlacesApiKey.trim().isEmpty())
                ? new GooglePlacesHotelService(googlePlacesApiKey.trim())
                : new MockHotelService();

        // Controllers (HTTP layer) + routing
        Router router = new Router();
        new DestinationController(destinationRepository, recommendationService).registerRoutes(router);
        new AttractionController(attractionRepository).registerRoutes(router);
        new ItineraryController(itineraryService).registerRoutes(router);
        new BookingController(flightBookingService, hotelBookingService).registerRoutes(router);
        new CityController(cityService, destinationRepository).registerRoutes(router);
        new HotelController(hotelService).registerRoutes(router);
        router.get("/api/health", ctx -> ctx.sendJson(200, new HealthStatus("ok")));

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", router);
        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();

        System.out.println("Vacation Planner backend listening on http://localhost:" + port);
        System.out.println("Hotel provider: " + (hotelService instanceof GooglePlacesHotelService ? "Google Places (live)" : "Mock"));
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
