package org.zatag.dev.datamanagement.Controller.Mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import org.bson.types.ObjectId;

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
    public ResponseEntity<?> deleteMongoDBLink(@PathVariable String id, @RequestBody Map<String, Object> row) {
        try {
            // Connect to MongoDB using the provided connection URI
            MongoDBLink Link = mongoDBLinkRepository.findById(id).get();
            MongoClient mongoClient = MongoClients.create(Link.getMongoUri());

            // Specify the database and collection name
            MongoCollection<Document> collection = mongoClient.getDatabase(Link.getDbName())
                    .getCollection(Link.getCollectionName());

            // Convert the $oid value to an ObjectId
            Map<String, String> idDocument = (Map<String, String>) row.get("_id");
            ObjectId objectId = new ObjectId(idDocument.get("$oid"));

            // Delete the row
            collection.deleteOne(new Document("_id", objectId));
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @PutMapping("/view/{id}")
    public ResponseEntity<?> updateMongoLink(@PathVariable String id, @RequestBody Map<String, Object> row) {
        try {
            // Connect to MongoDB using the provided connection URI
            MongoDBLink Link = mongoDBLinkRepository.findById(id).get();
            MongoClient mongoClient = MongoClients.create(Link.getMongoUri());

            // Specify the database and collection name
            MongoCollection<Document> collection = mongoClient.getDatabase(Link.getDbName())
                    .getCollection(Link.getCollectionName());

            // Convert the $oid value to an ObjectId
            Map<String, String> idDocument = (Map<String, String>) row.get("_id");
            ObjectId objectId = new ObjectId(idDocument.get("$oid"));

            // Remove _id from row map as it's not part of the update fields
            row.remove("_id");

            // Prepare the update document
            Document updateDoc = new Document("$set", new Document(row));

            // Update the row
            collection.updateOne(new Document("_id", objectId), updateDoc);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/view")
    public void addMongoDBLink(@RequestBody MongoDBLink link) {
        mongoDBLinkRepository.save(link);
    }
}
