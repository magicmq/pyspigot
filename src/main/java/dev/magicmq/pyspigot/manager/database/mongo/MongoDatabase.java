package dev.magicmq.pyspigot.manager.database.mongo;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.*;
import dev.magicmq.pyspigot.manager.database.Database;
import dev.magicmq.pyspigot.manager.script.Script;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.*;

public class MongoDatabase extends Database {

    private MongoClient mongoClient;
    private String uri;

    public MongoDatabase(Script script, String uri) {
        super(script);
        this.uri = uri;
    }

    @Override
    public boolean open() {
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(uri))
                .build();
        mongoClient = MongoClients.create(settings);
        return true;
    }

    @Override
    public boolean close() {
        mongoClient.close();
        return true;
    }

    //Init Document methods

    public Document createDocument(String json) {
        return Document.parse(json);
    }

    public Document createDocument(String key, Object value) {
        return new Document(key, value);
    }

    //Database methods

    public com.mongodb.client.MongoDatabase getDatabase(String database) {
        return mongoClient.getDatabase(database);
    }

    public boolean doesDatabaseExist(String database) {
        for (String db : mongoClient.listDatabaseNames()) {
            if (db.equals(database))
                return true;
        }
        return false;
    }

    //Collection methods

    public boolean createCollection(String database, String collection) {
        if (!doesCollectionExist(database, collection)) {
            getDatabase(database).createCollection(collection);
            return true;
        } else
            return false;
    }

    public boolean deleteCollection(String database, String collection) {
        if (doesCollectionExist(database, collection)) {
            getDatabase(database).getCollection(collection).drop();
            return true;
        } else
            return false;
    }

    public MongoCollection<Document> getCollection(String database, String collection) {
        return getDatabase(database)
                .getCollection(collection);
    }

    public boolean doesCollectionExist(String database, String collection) {
        com.mongodb.client.MongoDatabase mongoDatabase = getDatabase(database);
        for (String col : mongoDatabase.listCollectionNames()) {
            if (col.equals(collection))
                return true;
        }
        return false;
    }

    public String createCollectionIndex(String database, String collection, Bson keys) {
        return getDatabase(database)
                .getCollection(collection)
                .createIndex(keys);
    }

    //Document get

    public Document getDocument(String database, String collection, Bson object) {
        return getDatabase(database)
                .getCollection(collection)
                .find(object)
                .first();
    }

    public Document getDocument(String database, String collection, Bson object, Bson projections, Bson sorts) {
        return getDatabase(database)
                .getCollection(collection)
                .find(object)
                .projection(projections)
                .sort(sorts)
                .first();
    }

    public Collection<Document> getDocuments(String database, String collection, Bson object) {
        return getDatabase(database)
                .getCollection(collection)
                .find(object)
                .into(new ArrayList<>());
    }

    //Document insert

    public boolean insertDocument(String database, String collection, Document document) {
        MongoCollection<Document> col = getDatabase(database).getCollection(collection);
        return col.insertOne(document).wasAcknowledged();
    }

    public boolean insertDocuments(String database, String collection, List<Document> documents) {
        MongoCollection<Document> col = getDatabase(database).getCollection(collection);
        return col.insertMany(documents).wasAcknowledged();
    }

    //Document update

    public boolean updateDocument(String database, String collection, Bson filter, Bson update) {
        return updateDocument(database, collection, filter, Collections.singletonList(update));
    }

    public boolean updateDocument(String database, String collection, Bson filter, Bson update, UpdateOptions updateOptions) {
        return updateDocument(database, collection, filter, Collections.singletonList(update), updateOptions);
    }

    public boolean updateDocument(String database, String collection, Bson filter, List<Bson> update) {
        return updateDocument(database, collection, filter, update, new UpdateOptions());
    }

    public boolean updateDocument(String database, String collection, Bson filter, List<Bson> update, UpdateOptions updateOptions) {
        MongoCollection<Document> col = getDatabase(database).getCollection(collection);
        return col.updateOne(filter, update, updateOptions).wasAcknowledged();
    }

    //Document find and update

    public Document findAndUpdateDocument(String database, String collection, Bson filter, Bson update) {
        return findAndUpdateDocument(database, collection, filter, Collections.singletonList(update));
    }

    public Document findAndUpdateDocument(String database, String collection, Bson filter, Bson update, FindOneAndUpdateOptions updateOptions) {
        return findAndUpdateDocument(database, collection, filter, Collections.singletonList(update), updateOptions);
    }

    public Document findAndUpdateDocument(String database, String collection, Bson filter, List<Bson> update) {
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions();
        options.returnDocument(ReturnDocument.AFTER);
        return findAndUpdateDocument(database, collection, filter, update, options);
    }

    public Document findAndUpdateDocument(String database, String collection, Bson filter, List<Bson> update, FindOneAndUpdateOptions updateOptions) {
        MongoCollection<Document> col = getDatabase(database).getCollection(collection);
        return col.findOneAndUpdate(filter, update, updateOptions);
    }

    //Many document update

    public long updateDocuments(String database, String collection, Bson filter, Bson update) {
        return updateDocuments(database, collection, filter, Collections.singletonList(update));
    }

    public long updateDocuments(String database, String collection, Bson filter, Bson update, UpdateOptions updateOptions) {
        return updateDocuments(database, collection, filter, Collections.singletonList(update), updateOptions);
    }

    public long updateDocuments(String database, String collection, Bson filter, List<Bson> update) {
        return updateDocuments(database, collection, filter, update, new UpdateOptions());
    }

    public long updateDocuments(String database, String collection, Bson filter, List<Bson> update, UpdateOptions updateOptions) {
        MongoCollection<Document> col = getDatabase(database).getCollection(collection);
        return col.updateMany(filter, update, updateOptions).getModifiedCount();
    }

    //Document delete

    public boolean deleteDocument(String database, String collection, Bson filter) {
        return getDatabase(database)
                .getCollection(collection)
                .deleteOne(filter)
                .wasAcknowledged();
    }

    public boolean deleteDocuments(String database, String collection, Bson filter) {
        return getDatabase(database)
                .getCollection(collection)
                .deleteMany(filter)
                .wasAcknowledged();
    }
}
