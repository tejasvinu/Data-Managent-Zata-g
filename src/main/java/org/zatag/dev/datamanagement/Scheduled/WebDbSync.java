package org.zatag.dev.datamanagement.Scheduled;

import okhttp3.*;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Component
public class WebDbSync {
    @Autowired
    private MongoTemplate mongoTemplate;
    public void  runscraper(String fileName)
    {
        try {
            String filePath = "src/main/java/scrapers/" + fileName;
            System.out.println("Running the scraper: " + filePath);

            // Run the Python script
            // Path to the Python script
            ProcessBuilder builder = new ProcessBuilder(
                    "python",
                    filePath
            );
            builder.redirectErrorStream(true);
            Process process = builder.start();

            // Specify the directory where you want to store the output files
            String outputDir = "C:\\Users\\workhorse\\Documents\\Final Project\\Data Managent(Zata-g)\\GeneratedFiles\\scrapedData\\";

            // Create a FileWriter to write the output to a file in the specified directory
            String outputFileName = outputDir + fileName.replace(".py", ".txt");
            FileWriter writer = new FileWriter(new File(outputFileName));

            // Read the output of the process and write it to the file
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line + System.lineSeparator());
            }
            writer.close();

            // Wait for the process to complete
            int exitCode = process.waitFor();
            System.out.println("Process exited with code: " + exitCode);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void runAllScrapers() {
    try {
        // Specify the directory containing the scrapers
        File scraperDir = new File("src/main/java/scrapers/");

        // Get all the files in the directory
        File[] scraperFiles = scraperDir.listFiles();

        // Check if the directory is empty
        if (scraperFiles != null) {
            // Iterate over each file
            for (File scraperFile : scraperFiles) {
                // Get the file name
                String fileName = scraperFile.getName();

                // Run the scraper
                runscraper(fileName);
            }
        } else {
            System.out.println("No scraper files found in the directory.");
        }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JSONObject sendFileToServer(String directoryPath, String fileName, String serverUrl) {
    File file = new File(directoryPath, fileName);

    if (file.exists()) {
        OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.MINUTES)
            .readTimeout(5, TimeUnit.MINUTES)
            .writeTimeout(5, TimeUnit.MINUTES)
            .build();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", fileName,
                        RequestBody.create(file, MediaType.parse("text/plain")))
                .build();

        Request request = new Request.Builder()
                .url(serverUrl)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                try {
                    JSONArray jsonArrayResponse = new JSONArray(responseBody);
                    System.out.println(jsonArrayResponse);
                } catch (JSONException e) {
                    System.out.println("Invalid JSON response: " + responseBody);
                    e.printStackTrace();
                }
            }
        });
    }
          return null;
    }
    public List<Document> GetCollection(String collectionName) {
        if (!mongoTemplate.collectionExists(collectionName)) {
            mongoTemplate.createCollection(collectionName);
        }
        // Create a new query
        Query query = new Query();
        // Use the find method to retrieve the data
        return mongoTemplate.find(query, Document.class, collectionName);
    }
    public CompletableFuture<JSONArray> sendFileToServerAsync(String directoryPath, String fileName, String serverUrl) {
        return CompletableFuture.supplyAsync(() -> {
            File file = new File(directoryPath, fileName);

            if (file.exists()) {
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(5, TimeUnit.MINUTES)
                        .readTimeout(5, TimeUnit.MINUTES)
                        .writeTimeout(5, TimeUnit.MINUTES)
                        .build();

                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", fileName,
                                RequestBody.create(file, MediaType.parse("text/plain")))
                        .build();

                Request request = new Request.Builder()
                        .url(serverUrl)
                        .post(requestBody)
                        .build();

                try {
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        JSONArray jsonArrayResponse = new JSONArray(responseBody);
                        System.out.println(jsonArrayResponse);
                        return jsonArrayResponse; // Return the JSON response
                    } else {
                        System.out.println("Request failed: " + response.code() + " - " + response.message());
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("File not found: " + fileName);
            }
            return null;
        });
    }

    public void SyncDbs() {
        runAllScrapers();
        String directoryPath = "C:\\Users\\workhorse\\Documents\\Final Project\\Data Managent(Zata-g)\\GeneratedFiles\\scrapedData\\";
        String serverUrl = "http://localhost:5000/api/json/generate";
        File[] files = new File(directoryPath).listFiles();
        for (File file : files) {
            if (file.isFile()) {
                String fileName = file.getName();

                // Send the file to the server asynchronously and await the response
                CompletableFuture<JSONArray> responseFuture = sendFileToServerAsync(directoryPath, fileName, serverUrl);
                try {
                    JSONArray response = responseFuture.get(); // Await the response asynchronously
                    if (response != null) {
                        List<Document> collection = GetCollection(fileName);
                        System.out.println(collection);
                        System.out.println(response);

                        // Parse the response into a list of Document objects
                        List<Document> responseDocuments = new ArrayList<>();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject jsonObject = response.getJSONObject(i);
                            Document document = Document.parse(jsonObject.toString());
                            responseDocuments.add(document);
                        }

                        // Compare the responseDocuments and collection
                        for (Document responseDocument : responseDocuments) {
                            if (!collection.contains(responseDocument)) {
                                // If a document from the response does not exist in the collection, insert it into the collection
                                mongoTemplate.insert(responseDocument, fileName);
                            }
                        }
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
