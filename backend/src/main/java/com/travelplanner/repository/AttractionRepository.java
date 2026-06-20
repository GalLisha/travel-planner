package com.travelplanner.repository;

import com.travelplanner.model.ActivityTag;
import com.travelplanner.model.Attraction;
import com.travelplanner.model.BudgetLevel;
import com.travelplanner.model.GeoPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * In-memory attraction catalogue, keyed by destination. Stands in for a real
 * database or external attractions/POI provider (e.g. a maps or travel API).
 */
public class AttractionRepository {

    private final List<Attraction> attractions = new ArrayList<>();
    private int counter = 1;

    public AttractionRepository() {
        seedParis();
        seedOrlando();
        seedSantorini();
        seedBangkok();
        seedBali();
        seedCancun();
        seedCapeTown();
        seedTokyo();
        seedSydney();
        seedNewYork();
    }

    public List<Attraction> findAll() {
        return Collections.unmodifiableList(attractions);
    }

    public List<Attraction> findByDestination(String destinationId) {
        return attractions.stream()
                .filter(a -> a.getDestinationId().equals(destinationId))
                .collect(Collectors.toList());
    }

    public Attraction findById(String id) {
        return attractions.stream().filter(a -> a.getId().equals(id)).findFirst().orElse(null);
    }

    private void add(String destId, String name, String desc, ActivityTag category, double lat, double lng,
                      int durationMinutes, boolean family, boolean couple, boolean friends, String hours,
                      BudgetLevel cost, String imageUrl) {
        String id = destId + "-attr-" + (counter++);
        attractions.add(new Attraction(id, destId, name, desc, category, new GeoPoint(lat, lng), durationMinutes,
                family, couple, friends, hours, cost, imageUrl));
    }

    private void seedParis() {
        String d = "dest-paris";
        add(d, "Eiffel Tower", "Iconic iron landmark with sweeping city views.", ActivityTag.LANDMARKS,
                48.8584, 2.2945, 120, true, true, true, "09:00-23:00", BudgetLevel.MEDIUM, "/images/attractions/eiffel-tower.jpg");
        add(d, "Louvre Museum", "World's largest art museum, home to the Mona Lisa.", ActivityTag.MUSEUMS,
                48.8606, 2.3376, 180, true, true, false, "09:00-18:00", BudgetLevel.MEDIUM, "/images/attractions/louvre.jpg");
        add(d, "Montmartre & Sacre-Coeur", "Charming hilltop district with artists and a stunning basilica.", ActivityTag.ART,
                48.8867, 2.3431, 120, true, true, true, "06:00-22:30", BudgetLevel.LOW, "/images/attractions/montmartre.jpg");
        add(d, "Seine River Cruise", "Relaxing boat cruise past Paris's most famous landmarks.", ActivityTag.LANDMARKS,
                48.8638, 2.3105, 60, true, true, true, "10:00-22:00", BudgetLevel.LOW, "/images/attractions/seine-cruise.jpg");
        add(d, "Disneyland Paris", "Magical theme park with rides and parades for the whole family.", ActivityTag.THEME_PARKS,
                48.8722, 2.7758, 300, true, false, false, "09:30-20:00", BudgetLevel.HIGH, "/images/attractions/disneyland-paris.jpg");
        add(d, "Moulin Rouge District", "Legendary cabaret and the lively nightlife of Pigalle.", ActivityTag.NIGHTLIFE,
                48.8841, 2.3322, 150, false, true, true, "19:00-02:00", BudgetLevel.HIGH, "/images/attractions/moulin-rouge.jpg");
    }

    private void seedOrlando() {
        String d = "dest-orlando";
        add(d, "Magic Kingdom", "Disney's classic theme park with castles, rides and parades.", ActivityTag.THEME_PARKS,
                28.4177, -81.5812, 360, true, false, false, "09:00-22:00", BudgetLevel.HIGH, "/images/attractions/magic-kingdom.jpg");
        add(d, "Universal Studios Florida", "Movie-themed rides and immersive lands for thrill seekers.", ActivityTag.THEME_PARKS,
                28.4743, -81.4677, 360, true, false, true, "09:00-21:00", BudgetLevel.HIGH, "/images/attractions/universal-studios.jpg");
        add(d, "SeaWorld Orlando", "Marine-life shows, rides and conservation exhibits.", ActivityTag.WILDLIFE,
                28.4115, -81.4628, 240, true, false, false, "09:00-20:00", BudgetLevel.MEDIUM, "/images/attractions/seaworld.jpg");
        add(d, "Epcot", "Future-world pavilions and a world showcase of cultures.", ActivityTag.THEME_PARKS,
                28.3747, -81.5494, 300, true, false, false, "09:00-21:00", BudgetLevel.HIGH, "/images/attractions/epcot.jpg");
        add(d, "ICON Park", "Entertainment complex with a giant observation wheel and attractions.", ActivityTag.SHOPPING,
                28.4453, -81.4690, 120, true, false, true, "10:00-22:00", BudgetLevel.MEDIUM, "/images/attractions/icon-park.jpg");
        add(d, "Kennedy Space Center", "Day trip exploring real rockets and space exploration history.", ActivityTag.HISTORY,
                28.5728, -80.6490, 240, true, false, false, "09:00-18:00", BudgetLevel.MEDIUM, "/images/attractions/kennedy-space-center.jpg");
    }

    private void seedSantorini() {
        String d = "dest-santorini";
        add(d, "Oia Sunset Point", "World-famous sunset views over whitewashed cliffside houses.", ActivityTag.LANDMARKS,
                36.4617, 25.3753, 90, false, true, true, "All day", BudgetLevel.LOW, "/images/attractions/oia.jpg");
        add(d, "Fira Town", "Capital town with cliffside cafes, shops and caldera views.", ActivityTag.SHOPPING,
                36.4167, 25.4317, 120, false, true, true, "All day", BudgetLevel.MEDIUM, "/images/attractions/fira.jpg");
        add(d, "Red Beach", "Striking red volcanic-sand beach near Akrotiri.", ActivityTag.BEACH,
                36.3508, 25.3953, 150, false, true, true, "08:00-19:00", BudgetLevel.LOW, "/images/attractions/red-beach.jpg");
        add(d, "Akrotiri Archaeological Site", "Remarkably preserved Bronze-Age Minoan settlement.", ActivityTag.HISTORY,
                36.3508, 25.4030, 90, false, true, false, "08:00-17:00", BudgetLevel.MEDIUM, "/images/attractions/akrotiri.jpg");
        add(d, "Santo Wines Winery", "Caldera-view wine tasting of local volcanic varietals.", ActivityTag.FOOD,
                36.3625, 25.4045, 90, false, true, true, "11:00-21:00", BudgetLevel.MEDIUM, "/images/attractions/santo-wines.jpg");
        add(d, "Amoudi Bay", "Quiet fishing harbour with fresh seafood tavernas below Oia.", ActivityTag.FOOD,
                36.4622, 25.3702, 90, false, true, true, "All day", BudgetLevel.MEDIUM, "/images/attractions/amoudi-bay.jpg");
    }

    private void seedBangkok() {
        String d = "dest-bangkok";
        add(d, "Grand Palace", "Ornate former royal residence and Thailand's most sacred sites.", ActivityTag.LANDMARKS,
                13.7500, 100.4915, 150, true, false, true, "08:30-15:30", BudgetLevel.LOW, "/images/attractions/grand-palace.jpg");
        add(d, "Wat Arun", "Temple of Dawn, a striking riverside spire clad in porcelain.", ActivityTag.HISTORY,
                13.7437, 100.4888, 90, true, false, true, "08:00-18:00", BudgetLevel.LOW, "/images/attractions/wat-arun.jpg");
        add(d, "Chatuchak Weekend Market", "One of the world's largest markets with everything imaginable.", ActivityTag.SHOPPING,
                13.7999, 100.5500, 180, true, false, true, "09:00-18:00", BudgetLevel.LOW, "/images/attractions/chatuchak.jpg");
        add(d, "Khao San Road", "Buzzing backpacker street famous for street food and nightlife.", ActivityTag.NIGHTLIFE,
                13.7589, 100.4972, 150, false, false, true, "18:00-02:00", BudgetLevel.LOW, "/images/attractions/khao-san-road.jpg");
        add(d, "Chinatown Yaowarat", "Neon-lit streets packed with legendary street food stalls.", ActivityTag.FOOD,
                13.7407, 100.5096, 120, true, false, true, "17:00-23:00", BudgetLevel.LOW, "/images/attractions/yaowarat.jpg");
        add(d, "Damnoen Saduak Floating Market", "Classic day-trip boat market on the canals outside the city.", ActivityTag.SHOPPING,
                13.5210, 99.9576, 180, true, false, false, "07:00-12:00", BudgetLevel.LOW, "/images/attractions/floating-market.jpg");
    }

    private void seedBali() {
        String d = "dest-bali";
        add(d, "Tanah Lot Temple", "Iconic sea temple perched on a rock formation.", ActivityTag.HISTORY,
                -8.6212, 115.0868, 90, true, true, true, "06:00-19:00", BudgetLevel.LOW, "/images/attractions/tanah-lot.jpg");
        add(d, "Ubud Monkey Forest", "Sacred forest sanctuary with playful long-tailed macaques.", ActivityTag.WILDLIFE,
                -8.5188, 115.2588, 90, true, true, true, "08:30-18:00", BudgetLevel.LOW, "/images/attractions/monkey-forest.jpg");
        add(d, "Tegallalang Rice Terraces", "Stunning emerald-green terraced rice paddies.", ActivityTag.HIKING,
                -8.4312, 115.2783, 90, true, true, true, "07:00-19:00", BudgetLevel.LOW, "/images/attractions/tegallalang.jpg");
        add(d, "Kuta Beach", "Lively beach known for surfing and sunsets.", ActivityTag.BEACH,
                -8.7184, 115.1686, 150, true, true, true, "All day", BudgetLevel.LOW, "/images/attractions/kuta-beach.jpg");
        add(d, "Uluwatu Temple", "Cliffside temple famous for traditional Kecak fire dance at sunset.", ActivityTag.HISTORY,
                -8.8291, 115.0849, 120, false, true, true, "09:00-19:00", BudgetLevel.LOW, "/images/attractions/uluwatu.jpg");
        add(d, "Mount Batur Sunrise Trek", "Early-morning volcano hike rewarded with spectacular sunrise views.", ActivityTag.HIKING,
                -8.2422, 115.3753, 240, false, true, true, "03:30-09:00", BudgetLevel.MEDIUM, "/images/attractions/mount-batur.jpg");
    }

    private void seedCancun() {
        String d = "dest-cancun";
        add(d, "Chichen Itza", "Day trip to one of the New Seven Wonders of the World.", ActivityTag.HISTORY,
                20.6843, -88.5678, 240, true, true, true, "08:00-17:00", BudgetLevel.MEDIUM, "/images/attractions/chichen-itza.jpg");
        add(d, "Isla Mujeres", "Laid-back island getaway with turquoise water and golf carts.", ActivityTag.BEACH,
                21.2311, -86.7314, 240, true, true, true, "All day", BudgetLevel.MEDIUM, "/images/attractions/isla-mujeres.jpg");
        add(d, "Xcaret Park", "Eco-archaeological park with cenotes, rivers and cultural shows.", ActivityTag.WATER_SPORTS,
                20.5824, -87.1156, 300, true, true, false, "08:30-21:00", BudgetLevel.HIGH, "/images/attractions/xcaret.jpg");
        add(d, "Playa Delfines", "Cancun's iconic public beach with postcard turquoise water.", ActivityTag.BEACH,
                21.0775, -86.7572, 120, true, true, true, "All day", BudgetLevel.LOW, "/images/attractions/playa-delfines.jpg");
        add(d, "Coco Bongo", "High-energy nightclub with acrobatic shows and live performances.", ActivityTag.NIGHTLIFE,
                21.1380, -86.7484, 180, false, true, true, "22:00-04:00", BudgetLevel.HIGH, "/images/attractions/coco-bongo.jpg");
        add(d, "Cenote Dos Ojos", "Crystal-clear cenote near Tulum, perfect for snorkeling.", ActivityTag.WATER_SPORTS,
                20.3372, -87.3953, 180, true, true, true, "08:00-17:00", BudgetLevel.MEDIUM, "/images/attractions/cenote-dos-ojos.jpg");
    }

    private void seedCapeTown() {
        String d = "dest-capetown";
        add(d, "Table Mountain", "Cable car and hiking trails up Cape Town's iconic flat-topped peak.", ActivityTag.HIKING,
                -33.9628, 18.4098, 180, true, true, true, "08:00-18:00", BudgetLevel.MEDIUM, "/images/attractions/table-mountain.jpg");
        add(d, "Robben Island", "Historic ferry tour of Nelson Mandela's former prison island.", ActivityTag.HISTORY,
                -33.8067, 18.3667, 210, true, true, false, "09:00-15:00", BudgetLevel.MEDIUM, "/images/attractions/robben-island.jpg");
        add(d, "V&A Waterfront", "Bustling harbourside district with shops, restaurants and views.", ActivityTag.SHOPPING,
                -33.9036, 18.4202, 150, true, true, true, "09:00-21:00", BudgetLevel.MEDIUM, "/images/attractions/va-waterfront.jpg");
        add(d, "Boulders Beach", "Sheltered beach famous for its resident African penguin colony.", ActivityTag.WILDLIFE,
                -34.1936, 18.4534, 90, true, true, false, "08:00-17:00", BudgetLevel.LOW, "/images/attractions/boulders-beach.jpg");
        add(d, "Cape of Good Hope", "Dramatic cliffs and beaches at the tip of the peninsula.", ActivityTag.HIKING,
                -34.3568, 18.4740, 150, true, true, true, "07:00-17:00", BudgetLevel.LOW, "/images/attractions/cape-of-good-hope.jpg");
        add(d, "Kirstenbosch Gardens", "World-renowned botanical garden at the foot of Table Mountain.", ActivityTag.NATURE,
                -33.9881, 18.4327, 120, true, true, false, "08:00-19:00", BudgetLevel.LOW, "/images/attractions/kirstenbosch.jpg");
    }

    private void seedTokyo() {
        String d = "dest-tokyo";
        add(d, "Senso-ji Temple", "Tokyo's oldest and most colorful Buddhist temple.", ActivityTag.HISTORY,
                35.7148, 139.7967, 90, true, true, true, "06:00-17:00", BudgetLevel.LOW, "/images/attractions/sensoji.jpg");
        add(d, "Shibuya Crossing", "The world's busiest pedestrian crossing surrounded by neon and shops.", ActivityTag.LANDMARKS,
                35.6595, 139.7005, 60, true, true, true, "All day", BudgetLevel.LOW, "/images/attractions/shibuya.jpg");
        add(d, "Tokyo Skytree", "Soaring observation tower with panoramic city views.", ActivityTag.LANDMARKS,
                35.7101, 139.8107, 120, true, true, true, "08:00-22:00", BudgetLevel.MEDIUM, "/images/attractions/skytree.jpg");
        add(d, "Tsukiji Outer Market", "Legendary market for the freshest sushi and street food.", ActivityTag.FOOD,
                35.6654, 139.7707, 120, true, true, true, "05:00-14:00", BudgetLevel.MEDIUM, "/images/attractions/tsukiji.jpg");
        add(d, "Akihabara", "Electronics, anime and gaming culture hub.", ActivityTag.SHOPPING,
                35.7022, 139.7741, 120, true, false, true, "10:00-20:00", BudgetLevel.MEDIUM, "/images/attractions/akihabara.jpg");
        add(d, "Tokyo Disneyland", "Beloved theme park with classic Disney rides, Japan-style.", ActivityTag.THEME_PARKS,
                35.6329, 139.8804, 300, true, false, false, "09:00-21:00", BudgetLevel.HIGH, "/images/attractions/tokyo-disneyland.jpg");
    }

    private void seedSydney() {
        String d = "dest-sydney";
        add(d, "Sydney Opera House", "Architectural icon on the harbour, home to world-class performances.", ActivityTag.LANDMARKS,
                -33.8568, 151.2153, 90, true, true, true, "09:00-17:00", BudgetLevel.MEDIUM, "/images/attractions/opera-house.jpg");
        add(d, "Bondi Beach", "World-famous beach for swimming, surfing and the coastal walk.", ActivityTag.BEACH,
                -33.8908, 151.2743, 150, true, true, true, "All day", BudgetLevel.LOW, "/images/attractions/bondi-beach.jpg");
        add(d, "Taronga Zoo", "Harbourside zoo with native Australian wildlife and skyline views.", ActivityTag.WILDLIFE,
                -33.8430, 151.2412, 180, true, false, false, "09:00-17:00", BudgetLevel.MEDIUM, "/images/attractions/taronga-zoo.jpg");
        add(d, "Sydney Harbour Bridge Climb", "Guided climb to the summit of the harbour's iconic bridge.", ActivityTag.LANDMARKS,
                -33.8523, 151.2108, 180, false, true, true, "07:00-19:00", BudgetLevel.HIGH, "/images/attractions/harbour-bridge.jpg");
        add(d, "The Rocks", "Historic cobblestone district with markets, pubs and harbour views.", ActivityTag.HISTORY,
                -33.8599, 151.2090, 120, true, true, true, "All day", BudgetLevel.LOW, "/images/attractions/the-rocks.jpg");
        add(d, "Blue Mountains Day Trip", "Dramatic eucalyptus-forested cliffs and the Three Sisters formation.", ActivityTag.HIKING,
                -33.7178, 150.3119, 300, true, true, false, "08:00-17:00", BudgetLevel.MEDIUM, "/images/attractions/blue-mountains.jpg");
    }

    private void seedNewYork() {
        String d = "dest-newyork";
        add(d, "Statue of Liberty", "Ferry trip to the iconic symbol of freedom in the harbour.", ActivityTag.LANDMARKS,
                40.6892, -74.0445, 180, true, true, true, "08:30-16:00", BudgetLevel.MEDIUM, "/images/attractions/statue-of-liberty.jpg");
        add(d, "Central Park", "Sprawling green oasis perfect for strolls, boating and picnics.", ActivityTag.NATURE,
                40.7829, -73.9654, 120, true, true, true, "06:00-22:00", BudgetLevel.LOW, "/images/attractions/central-park.jpg");
        add(d, "Times Square", "Dazzling billboards, Broadway theatres and round-the-clock energy.", ActivityTag.LANDMARKS,
                40.7580, -73.9855, 60, true, true, true, "All day", BudgetLevel.LOW, "/images/attractions/times-square.jpg");
        add(d, "Metropolitan Museum of Art", "One of the world's great art museums, spanning 5,000 years.", ActivityTag.MUSEUMS,
                40.7794, -73.9632, 180, true, true, false, "10:00-17:00", BudgetLevel.MEDIUM, "/images/attractions/met-museum.jpg");
        add(d, "Brooklyn Bridge", "Historic pedestrian walk with stunning skyline views.", ActivityTag.LANDMARKS,
                40.7061, -73.9969, 90, true, true, true, "All day", BudgetLevel.LOW, "/images/attractions/brooklyn-bridge.jpg");
        add(d, "Broadway Show", "World-renowned live theatre in the heart of the Theater District.", ActivityTag.NIGHTLIFE,
                40.7590, -73.9845, 150, false, true, true, "19:00-23:00", BudgetLevel.HIGH, "/images/attractions/broadway.jpg");
    }
}
