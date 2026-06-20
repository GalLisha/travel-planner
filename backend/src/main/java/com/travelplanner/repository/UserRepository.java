package com.travelplanner.repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.travelplanner.http.JsonUtil;
import com.travelplanner.model.User;
import org.bson.Document;

import java.util.Optional;
import java.util.UUID;

import static com.mongodb.client.model.Filters.eq;

/** MongoDB-backed user storage; documents are (de)serialized via the same Gson instance used elsewhere in the app. */
public class UserRepository {

    private final MongoCollection<Document> collection;

    public UserRepository(MongoDatabase database) {
        this.collection = database.getCollection("users");
    }

    public User insert(User user) {
        user.setId(UUID.randomUUID().toString());
        collection.insertOne(Document.parse(JsonUtil.GSON.toJson(user)));
        return user;
    }

    public Optional<User> findByEmail(String email) {
        return toUser(collection.find(eq("email", email)).first());
    }

    public Optional<User> findById(String id) {
        return toUser(collection.find(eq("id", id)).first());
    }

    private Optional<User> toUser(Document doc) {
        if (doc == null) {
            return Optional.empty();
        }
        doc.remove("_id");
        return Optional.of(JsonUtil.GSON.fromJson(doc.toJson(), User.class));
    }
}
