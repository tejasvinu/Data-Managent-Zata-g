package org.zatag.dev.datamanagement.Controller.Mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.zatag.dev.datamanagement.Models.Mongo.MongoDBLink;
import org.zatag.dev.datamanagement.Models.Mongo.MongoFiles;
import org.zatag.dev.datamanagement.Repository.Mongo.MongoDBLinkRepository;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.mongodb.core.query.Update;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
            System.out.println("Connected to MongoDB successfully!");
            // Specify the database and collection name
            MongoCollection<Document> collection = mongoClient.getDatabase(Link.getDbName())
                    .getCollection(Link.getCollectionName());

            // Fetch data from the MongoDB collection
            StringBuilder jsonData = new StringBuilder("[");
            MongoCursor<Document> cursor = collection.find().iterator();
            System.out.println("Fetching data from MongoDB...");
            while (cursor.hasNext()) {
                Document document = cursor.next();
                System.out.println(document.toJson());
                jsonData.append(document.toJson()).append(",");
            }
            cursor.close();
            jsonData.deleteCharAt(jsonData.length() - 1); // Remove the last comma
            jsonData.append("]");
            System.out.println("Data fetched successfully!"
                    + jsonData.toString());
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
    @PutMapping("/view/{id}")
    public void updateMongoLink(@PathVariable String id, @RequestBody HashMap<String,String> row) {
        // Connect to MongoDB using the provided connection details
        Optional<MongoDBLink> optionalRequest = mongoDBLinkRepository.findById(id);
        MongoDBLink request = null;
        if (optionalRequest.isPresent()) {
            request = optionalRequest.get();
            // Rest of your code
        } else {
            // Handle the case where no MongoLink was found for the provided id
            System.out.println("No MongoLink found for id: " + id);
        }
        assert request != null;

        // Parse the row information as needed
        System.out.println("Received row data: " + row);

        // Now you can connect to the database and update the row
        try {
            // Implement your update logic using the row data
            Update update = new Update();
            for (Map.Entry<String, String> entry : row.entrySet()) {
                update.set(entry.getKey(), entry.getValue());
            }
            mongoTemplate.updateFirst(Query.query(Criteria.where("_id").is(id)), update, request.getCollectionName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PostMapping("/view")
    public void addMongoDBLink(@RequestBody MongoDBLink link) {
        mongoDBLinkRepository.save(link);
    }
}
