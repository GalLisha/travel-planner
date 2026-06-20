package com.travelplanner.service;

import com.travelplanner.model.Attraction;
import com.travelplanner.model.GeoPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Computes distances/travel times between points and orders a day's stops
 * using a nearest-neighbor heuristic. Kept dependency-free (no external
 * maps/routing API) so it can later be swapped for a real routing provider
 * behind the same method signatures.
 */
public class RouteOptimizationService {

    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double WALK_THRESHOLD_KM = 1.2;
    private static final double WALK_SPEED_KMH = 4.5;
    private static final double DRIVE_SPEED_KMH = 28.0;

    // Airport transfers cover much longer distances than in-city hops, mostly on
    // highways, so they get their own (faster) speed model plus a fixed overhead for
    // pickup/boarding - a taxi leaves close to immediately, a bus involves waiting and stops.
    private static final double TAXI_SPEED_KMH = 55.0;
    private static final int TAXI_OVERHEAD_MINUTES = 10;
    private static final double BUS_SPEED_KMH = 32.0;
    private static final int BUS_OVERHEAD_MINUTES = 20;
    public static final int AIRPORT_CHECKIN_BUFFER_MINUTES = 60;

    /** Great-circle distance between two points, in kilometers. */
    public double distanceKm(GeoPoint a, GeoPoint b) {
        double lat1 = Math.toRadians(a.getLatitude());
        double lat2 = Math.toRadians(b.getLatitude());
        double dLat = Math.toRadians(b.getLatitude() - a.getLatitude());
        double dLon = Math.toRadians(b.getLongitude() - a.getLongitude());

        double sinDLat = Math.sin(dLat / 2);
        double sinDLon = Math.sin(dLon / 2);
        double h = sinDLat * sinDLat + Math.cos(lat1) * Math.cos(lat2) * sinDLon * sinDLon;
        double c = 2 * Math.atan2(Math.sqrt(h), Math.sqrt(1 - h));
        return EARTH_RADIUS_KM * c;
    }

    public String travelMode(double distanceKm) {
        return distanceKm <= WALK_THRESHOLD_KM ? "WALK" : "DRIVE";
    }

    /** Estimated travel time in whole minutes, with a small fixed overhead for transitions. */
    public int travelTimeMinutes(double distanceKm) {
        double speed = distanceKm <= WALK_THRESHOLD_KM ? WALK_SPEED_KMH : DRIVE_SPEED_KMH;
        double hours = distanceKm / speed;
        int minutes = (int) Math.ceil(hours * 60) + (distanceKm <= WALK_THRESHOLD_KM ? 0 : 5);
        return Math.max(minutes, 5);
    }

    /** Estimated airport<->hotel transfer time in whole minutes, including a fixed pickup/wait overhead. */
    public int airportTransferTimeMinutes(double distanceKm, String transferMode) {
        boolean isBus = "BUS".equalsIgnoreCase(transferMode);
        double speed = isBus ? BUS_SPEED_KMH : TAXI_SPEED_KMH;
        int overhead = isBus ? BUS_OVERHEAD_MINUTES : TAXI_OVERHEAD_MINUTES;
        return (int) Math.ceil(distanceKm / speed * 60) + overhead;
    }

    /**
     * Orders attractions for a single day using a nearest-neighbor heuristic,
     * starting from the given point (typically the hotel).
     */
    public List<Attraction> optimizeOrder(GeoPoint start, List<Attraction> attractions) {
        List<Attraction> remaining = new ArrayList<>(attractions);
        List<Attraction> ordered = new ArrayList<>();
        GeoPoint current = start;

        while (!remaining.isEmpty()) {
            Attraction nearest = null;
            double bestDistance = Double.MAX_VALUE;
            for (Attraction candidate : remaining) {
                double d = distanceKm(current, candidate.getLocation());
                if (d < bestDistance) {
                    bestDistance = d;
                    nearest = candidate;
                }
            }
            ordered.add(nearest);
            remaining.remove(nearest);
            current = nearest.getLocation();
        }
        return ordered;
    }
}
