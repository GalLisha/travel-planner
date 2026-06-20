package com.travelplanner.service.airport;

import com.travelplanner.model.Airport;

import java.util.List;

/** Abstraction over an airport-lookup provider, searched near a destination's coordinates. */
public interface AirportService {
    List<Airport> findNear(double latitude, double longitude);
}
