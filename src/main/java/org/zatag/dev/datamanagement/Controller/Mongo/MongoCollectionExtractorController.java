package org.zatag.dev.datamanagement.Controller.Mongo;

import org.zatag.dev.datamanagement.Models.Mongo.MongoFiles;
import org.zatag.dev.datamanagement.Models.Mongo.MongoRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/mongo")
public class MongoCollectionExtractorController {

    @Autowired
    private MongoTemplate mongoTemplate; // Inject the MongoTemplate

    @PostMapping("/extract")
    public String extractMongoCollection(@RequestBody MongoRequest request) {
        try {
            // Connect to MongoDB using the provided connection URI
            MongoClient mongoClient = MongoClients.create(request.getMongoUri());

            // Specify the database and collection name
            MongoCollection<Document> collection = mongoClient.getDatabase(request.getDbName())
                    .getCollection(request.getCollectionName());

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

            // Write JSON data to a file
            String fileName = writeJsonToFile(jsonData.toString());

            System.out.println("JSON file generated successfully!");

            // Save file generation details to the database
            saveFileGenerationDetails(fileName);

            return "JSON file generated successfully!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error occurred: " + e.getMessage();
        }
    }

    private String writeJsonToFile(String jsonData) throws IOException {
        // Replace ":" in the timestamp with "_"
        String timestamp = LocalDateTime.now().toString().replace(":", "_");
        String fileName = "mongo_collection_data_" + timestamp + ".json";
        try (FileWriter fileWriter = new FileWriter(fileName)) {
            fileWriter.write(jsonData);
        }
        return fileName;
    }

    private void saveFileGenerationDetails(String fileName) {
        // Save file generation details to the database
        MongoFiles details = new MongoFiles();
        details.setFileName(fileName);
        mongoTemplate.save(details);
    }

    @GetMapping("/List")
    public Iterable<MongoFiles> listFiles() {
        return mongoTemplate.findAll(MongoFiles.class);
    }
}
