package com.travelplanner.repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
import com.travelplanner.http.JsonUtil;
import com.travelplanner.model.SavedTrip;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.mongodb.client.model.Filters.eq;

/** MongoDB-backed saved-trip storage, separate from the in-memory ItineraryRepository used while building a trip. */
public class SavedTripRepository {

    private final MongoCollection<Document> collection;

    public SavedTripRepository(MongoDatabase database) {
        this.collection = database.getCollection("saved_trips");
    }

    public SavedTrip insert(SavedTrip trip) {
        trip.setId(UUID.randomUUID().toString());
        collection.insertOne(Document.parse(JsonUtil.GSON.toJson(trip)));
        return trip;
    }

    public List<SavedTrip> findByUserId(String userId) {
        List<SavedTrip> trips = new ArrayList<>();
        for (Document doc : collection.find(eq("userId", userId)).sort(Sorts.descending("savedAt"))) {
            doc.remove("_id");
            trips.add(JsonUtil.GSON.fromJson(doc.toJson(), SavedTrip.class));
        }
        return trips;
    }
}
