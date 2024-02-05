package org.zatag.dev.datamanagement.Controller.Mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.zatag.dev.datamanagement.Models.Mongo.MongoDBLink;
import org.zatag.dev.datamanagement.Models.Mongo.MongoFiles;
import org.zatag.dev.datamanagement.Repository.Mongo.MongoDBLinkRepository;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/mongo-data")
public class MongoDataController {
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private MongoDBLinkRepository mongoDBLinkRepository;

    @GetMapping("/view/{id}")
    public MongoFiles extractMongoCollection(@PathVariable String id) {
        try {
            // Connect to MongoDB using the provided connection URI
            MongoDBLink Link = mongoDBLinkRepository.findById(id).get();
            MongoClient mongoClient = MongoClients.create(Link.getMongoUri());

            // Specify the database and collection name
            MongoCollection<Document> collection = mongoClient.getDatabase(Link.getDbName())
                    .getCollection(Link.getCollectionName());

            // Fetch data from the MongoDB collection
            StringBuilder jsonData = new StringBuilder("[");
            MongoCursor<Document> cursor = collection.find().iterator();
            while (cursor.hasNext()) {
                Document document = cursor.next();
                jsonData.append(document.toJson()).append(",");
            }
            cursor.close();
            jsonData.deleteCharAt(jsonData.length() - 1); // Remove the last comma
            jsonData.append("]");

            System.out.println("JSON file generated successfully!");

            return new MongoFiles(jsonData.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    @DeleteMapping("/view/{id}")
    public void deleteMongoDBLink(@PathVariable String id) {
        // Connect to MongoDB using the provided connection URI
        MongoDBLink Link = mongoDBLinkRepository.findById(id).get();
        MongoClient mongoClient = MongoClients.create(Link.getMongoUri());

        // Specify the database and collection name
        MongoCollection<Document> collection = mongoClient.getDatabase(Link.getDbName())
                .getCollection(Link.getCollectionName());
        collection.dropIndex("id");
    }
}
