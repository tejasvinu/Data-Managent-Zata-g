package org.zatag.dev.datamanagement.Service;

import com.mongodb.client.result.InsertOneResult;
import okhttp3.*;
import org.apache.pdfbox.Loader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class UploadService {

    @Autowired
    private MongoTemplate mongoTemplate;

    public CompletableFuture<List<JSONObject>> sendFileToServerAsync(String directoryPath, String fileName, String serverUrl, String prompt) {
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
                        .addFormDataPart("prompt", prompt)
                        .build();

                Request request = new Request.Builder()
                        .url(serverUrl)
                        .post(requestBody)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        JSONArray jsonArray = new JSONArray(responseBody);
                        List<JSONObject> jsonObjects = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            jsonObjects.add(jsonArray.getJSONObject(i));
                        }
                        return jsonObjects;
                    } else {
                        System.out.println("Request failed: " + response.code() + " - " + response.message());
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("File not found: " + fileName);
            }
            return Collections.emptyList();
        });
    }


    public File convertMultipartFileToFile(MultipartFile multipartFile) {
        File file = new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        try {
            multipartFile.transferTo(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public void extractTextToFile(File file, String directoryPath) throws IOException {
        String originalFilename = Objects.requireNonNull(file.getName());
        File outputFile = new File(directoryPath, originalFilename + ".txt");

        if (isPdfFile(originalFilename)) {
            extractTextFromPdf(file, outputFile);
        } else {
            extractTextFromOtherFile(file, outputFile);
        }
    }

    private boolean isPdfFile(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".")).toLowerCase();
        return ".pdf".equals(extension);
    }

    private void extractTextFromPdf(File file, File outputFile) throws IOException {
        try (PDDocument document = Loader.loadPDF(file);
             FileWriter writer = new FileWriter(outputFile)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            writer.write(pdfStripper.getText(document));
        }
    }

    private void extractTextFromOtherFile(File file, File outputFile) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(file));
             FileWriter writer = new FileWriter(outputFile)) {
            String currentLine;
            while ((currentLine = br.readLine()) != null) {
                writer.write(currentLine + "\n");
            }
        }
    }

    public void createCollectionWithJson(String mongoUrl, String dbName,
                                         String collectionName, List<JSONObject> jsonDocs) {

        // Ensure the collection doesn't already exist
        if (!mongoTemplate.collectionExists(collectionName)) {
            mongoTemplate.createCollection(collectionName);
        }

        // Iterate over each JSONObject in the list
        for (JSONObject jsonDoc : jsonDocs) {
            String jsonDocument = jsonDoc.toString();
            System.out.println(jsonDocument);
            // Convert JSON string to a Document
            org.bson.Document document = org.bson.Document.parse(jsonDocument);

            // Insert the document
            InsertOneResult result = mongoTemplate.getCollection(collectionName)
                    .insertOne(document);

            System.out.println("Document inserted into collection '" + collectionName + "': " + result.getInsertedId());
        }
    }

    //create file from multipart file
    public File convertAndStoreMultipartFile(MultipartFile multipartFile, String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdirs(); // create directory if it does not exist
        }
        String fileName = Objects.requireNonNull(multipartFile.getOriginalFilename());
        File file = new File(directoryPath, fileName);

        try {
            multipartFile.transferTo(file);
            System.out.println("File created: " + file.getName());
            return file;
        } catch (IOException e) {
            System.out.println("An error occurred while creating the file.");
            e.printStackTrace();
        }
        return file;
    }
    public void createTxtFileWithText(String text, String directoryPath, String fileName) {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdirs(); // create directory if it does not exist
        }
        String txtFileName = fileName + ".txt"; // append .txt to the filename
        File file = new File(directoryPath, txtFileName);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(text);
            System.out.println("TXT file created: " + file.getName());
        } catch (IOException e) {
            System.out.println("An error occurred while creating the TXT file.");
            e.printStackTrace();
        }
    }
}
