package com.travelplanner.db;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;

/**
 * Thin bootstrap around the MongoDB driver: connects using the configured connection
 * string and ensures the indexes the app relies on exist. Repositories receive the
 * resulting MongoDatabase directly via constructor injection, same as every other
 * repository in this app.
 */
public final class MongoConfig {

    private MongoConfig() {
    }

    public static MongoDatabase connect(String connectionString, String databaseName) {
        MongoClient client = MongoClients.create(connectionString);
        MongoDatabase database = client.getDatabase(databaseName);
        database.getCollection("users").createIndex(Indexes.ascending("email"), new IndexOptions().unique(true));
        return database;
    }
}
