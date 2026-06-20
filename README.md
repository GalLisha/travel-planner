# Vacation Planner

A vacation planning web app: a React frontend wizard backed by a pure-Java
(no Spring, no framework) REST API.

```
backend/    Pure Java REST API (JDK com.sun.net.httpserver + Gson for JSON)
frontend/   React app (Vite)
```

## Running the backend

Requires Java 8+ and Maven.

```bash
cd backend
mvn package
java -jar target/vacation-planner-backend.jar
```

Listens on `http://localhost:8080` (override with the `PORT` env var).

## Running the frontend

Requires Node 18+.

```bash
cd frontend
npm install
npm run dev
```

Serves on `http://localhost:5173` and proxies `/api/*` to the backend on port 8080.

## API overview

| Method | Path | Purpose |
|---|---|---|
| GET | `/api/destinations` | List all destinations |
| POST | `/api/destinations/suggestions` | Recommend destinations from preferences |
| GET | `/api/attractions?destinationId=` | List attractions for a destination |
| POST | `/api/itinerary` | Generate a day-by-day itinerary |
| GET | `/api/itinerary/{id}` | Fetch a generated itinerary |
| PUT | `/api/itinerary/{id}/replace` | Swap an attraction and recalculate the day |
| GET | `/api/itinerary/{id}/alternatives` | Candidate replacements for an attraction |
| POST | `/api/bookings/flights` | Flight booking (stub - not yet supported) |
| POST | `/api/bookings/hotels` | Hotel booking (stub - not yet supported) |
| GET | `/api/cities/search?query=&countryCode=` | Live city autocomplete (any city worldwide) |
| GET | `/api/hotels/search?city=&country=&lat=&lon=` | Hotels near a city center |

## City & hotel search

- **Cities**: `service.city.CityService` → `PhotonCityService`, backed by the free,
  keyless [Photon](https://photon.komoot.io) geocoder (OSM data, built for
  autocomplete/prefix matching - Nominatim's public API explicitly disallows
  search-as-you-type traffic, which is why Photon is used instead). Results that
  match one of the 10 curated destinations are tagged with `curatedDestinationId`
  so the frontend knows a full attraction-based itinerary is available; other
  real cities still get hotels and a day-by-day shell, just with "free days"
  instead of attractions (see `ItineraryService.resolveDestination`).
- **Hotels**: `service.hotel.HotelService` → `MockHotelService` by default
  (deterministic per-city placeholder hotels, no key needed) or
  `GooglePlacesHotelService` automatically if a `GOOGLE_PLACES_API_KEY`
  environment variable is set - no other code changes needed to go live:
  ```bash
  GOOGLE_PLACES_API_KEY=your-key java -jar target/vacation-planner-backend.jar
  ```
  Swapping in Amadeus or Booking.com later just means a new `HotelService`
  implementation selected in `Main.java`.

## Architecture notes

- **Backend layers**: `controller` (HTTP) → `service` (business logic) → `repository`
  (in-memory mock data). DTOs in `dto.request`/`dto.response` decouple the wire
  format from the `model` domain classes.
- **Distances/travel times** are computed with the Haversine formula and a
  nearest-neighbor heuristic in `RouteOptimizationService` - no external maps
  API is wired up, so it works offline against the mock attraction coordinates.
- **Booking is intentionally unimplemented.** `service.booking.FlightBookingService`
  and `HotelBookingService` are interfaces with stub implementations that report
  the feature as unsupported. A real provider integration is a drop-in
  replacement of those two implementations in `Main.java` - no controller or
  frontend changes required.
- **Itinerary state** lives in-memory on the backend (`ItineraryRepository`),
  keyed by a generated itinerary id, so attraction replacement can recalculate
  a day's route server-side without the frontend re-sending the whole trip.
