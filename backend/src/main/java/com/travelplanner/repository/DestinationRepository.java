package com.travelplanner.repository;

import com.travelplanner.model.ActivityTag;
import com.travelplanner.model.BudgetLevel;
import com.travelplanner.model.Destination;
import com.travelplanner.model.GeoPoint;
import com.travelplanner.model.Region;
import com.travelplanner.model.VacationStyle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

/**
 * In-memory destination catalogue. Stands in for a real database / external
 * inventory provider; swapping this out for a JDBC-backed or remote-API-backed
 * implementation later does not require any change in the service layer,
 * which only depends on the methods below.
 */
public class DestinationRepository {

    private final List<Destination> destinations = new ArrayList<>();

    public DestinationRepository() {
        seed();
    }

    public List<Destination> findAll() {
        return Collections.unmodifiableList(destinations);
    }

    public Optional<Destination> findById(String id) {
        return destinations.stream().filter(d -> d.getId().equals(id)).findFirst();
    }

    private void seed() {
        destinations.add(new Destination(
                "dest-paris", "Paris", "France", Region.EUROPE,
                "The City of Light, famed for iconic landmarks, world-class art and romantic riverside walks.",
                "/images/destinations/paris.jpg", BudgetLevel.HIGH, 8,
                set(VacationStyle.ROMANTIC, VacationStyle.CULTURE, VacationStyle.CITY_BREAK, VacationStyle.FAMILY_FUN),
                set(ActivityTag.MUSEUMS, ActivityTag.LANDMARKS, ActivityTag.ART, ActivityTag.FOOD, ActivityTag.NIGHTLIFE, ActivityTag.THEME_PARKS),
                true, true, true, new GeoPoint(48.8566, 2.3522)));

        destinations.add(new Destination(
                "dest-orlando", "Orlando", "USA", Region.NORTH_AMERICA,
                "Theme park capital of the world, packed with family rides, shows and entertainment.",
                "/images/destinations/orlando.jpg", BudgetLevel.MEDIUM, 2,
                set(VacationStyle.FAMILY_FUN, VacationStyle.ADVENTURE),
                set(ActivityTag.THEME_PARKS, ActivityTag.WATER_SPORTS, ActivityTag.WILDLIFE, ActivityTag.SHOPPING),
                true, false, false, new GeoPoint(28.5383, -81.3792)));

        destinations.add(new Destination(
                "dest-santorini", "Santorini", "Greece", Region.EUROPE,
                "Whitewashed cliffside villages, volcanic beaches and unforgettable sunsets over the Aegean.",
                "/images/destinations/santorini.jpg", BudgetLevel.HIGH, 7,
                set(VacationStyle.ROMANTIC, VacationStyle.RELAXATION, VacationStyle.CULTURE),
                set(ActivityTag.BEACH, ActivityTag.FOOD, ActivityTag.HISTORY, ActivityTag.LANDMARKS),
                false, true, true, new GeoPoint(36.3932, 25.4615)));

        destinations.add(new Destination(
                "dest-bangkok", "Bangkok", "Thailand", Region.ASIA,
                "A whirlwind of golden temples, street food markets and buzzing nightlife.",
                "/images/destinations/bangkok.jpg", BudgetLevel.LOW, 11,
                set(VacationStyle.ADVENTURE, VacationStyle.NIGHTLIFE, VacationStyle.CULTURE, VacationStyle.CITY_BREAK),
                set(ActivityTag.FOOD, ActivityTag.HISTORY, ActivityTag.NIGHTLIFE, ActivityTag.SHOPPING, ActivityTag.LANDMARKS),
                true, false, true, new GeoPoint(13.7563, 100.5018)));

        destinations.add(new Destination(
                "dest-bali", "Bali", "Indonesia", Region.ASIA,
                "Lush rice terraces, sacred temples and laid-back beach towns for every kind of traveler.",
                "/images/destinations/bali.jpg", BudgetLevel.MEDIUM, 12,
                set(VacationStyle.NATURE, VacationStyle.RELAXATION, VacationStyle.ADVENTURE, VacationStyle.ROMANTIC),
                set(ActivityTag.BEACH, ActivityTag.HIKING, ActivityTag.WILDLIFE, ActivityTag.HISTORY),
                true, true, true, new GeoPoint(-8.4095, 115.1889)));

        destinations.add(new Destination(
                "dest-cancun", "Cancun", "Mexico", Region.CARIBBEAN,
                "Turquoise Caribbean waters, ancient Mayan ruins and a legendary party scene.",
                "/images/destinations/cancun.jpg", BudgetLevel.MEDIUM, 4,
                set(VacationStyle.RELAXATION, VacationStyle.NIGHTLIFE, VacationStyle.ADVENTURE, VacationStyle.FAMILY_FUN),
                set(ActivityTag.BEACH, ActivityTag.WATER_SPORTS, ActivityTag.HISTORY, ActivityTag.NIGHTLIFE),
                true, true, true, new GeoPoint(21.1619, -86.8515)));

        destinations.add(new Destination(
                "dest-capetown", "Cape Town", "South Africa", Region.AFRICA,
                "Dramatic mountains, pristine beaches and unforgettable wildlife encounters.",
                "/images/destinations/capetown.jpg", BudgetLevel.MEDIUM, 11,
                set(VacationStyle.NATURE, VacationStyle.ADVENTURE, VacationStyle.CULTURE),
                set(ActivityTag.WILDLIFE, ActivityTag.HIKING, ActivityTag.BEACH, ActivityTag.LANDMARKS),
                true, true, true, new GeoPoint(-33.9249, 18.4241)));

        destinations.add(new Destination(
                "dest-tokyo", "Tokyo", "Japan", Region.ASIA,
                "A dazzling mix of ancient temples, futuristic skylines and incredible cuisine.",
                "/images/destinations/tokyo.jpg", BudgetLevel.HIGH, 13,
                set(VacationStyle.CULTURE, VacationStyle.CITY_BREAK, VacationStyle.FAMILY_FUN),
                set(ActivityTag.FOOD, ActivityTag.SHOPPING, ActivityTag.HISTORY, ActivityTag.THEME_PARKS, ActivityTag.LANDMARKS),
                true, true, true, new GeoPoint(35.6762, 139.6503)));

        destinations.add(new Destination(
                "dest-sydney", "Sydney", "Australia", Region.OCEANIA,
                "Harbourside icons, golden beaches and an easy-going outdoor lifestyle.",
                "/images/destinations/sydney.jpg", BudgetLevel.HIGH, 22,
                set(VacationStyle.NATURE, VacationStyle.CITY_BREAK, VacationStyle.ADVENTURE),
                set(ActivityTag.BEACH, ActivityTag.LANDMARKS, ActivityTag.WILDLIFE, ActivityTag.HIKING),
                true, true, true, new GeoPoint(-33.8688, 151.2093)));

        destinations.add(new Destination(
                "dest-newyork", "New York City", "USA", Region.NORTH_AMERICA,
                "The city that never sleeps: world-class museums, Broadway shows and iconic skylines.",
                "/images/destinations/newyork.jpg", BudgetLevel.HIGH, 7,
                set(VacationStyle.CITY_BREAK, VacationStyle.CULTURE, VacationStyle.NIGHTLIFE),
                set(ActivityTag.MUSEUMS, ActivityTag.SHOPPING, ActivityTag.LANDMARKS, ActivityTag.NIGHTLIFE, ActivityTag.FOOD),
                true, true, true, new GeoPoint(40.7128, -74.0060)));
    }

    private static EnumSet<VacationStyle> set(VacationStyle first, VacationStyle... rest) {
        EnumSet<VacationStyle> s = EnumSet.of(first, rest);
        return s;
    }

    private static EnumSet<ActivityTag> set(ActivityTag first, ActivityTag... rest) {
        EnumSet<ActivityTag> s = EnumSet.of(first, rest);
        return s;
    }
}
