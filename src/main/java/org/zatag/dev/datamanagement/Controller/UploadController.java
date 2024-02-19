package org.zatag.dev.datamanagement.Controller;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.zatag.dev.datamanagement.Service.ProcessOcrDocument;
import org.zatag.dev.datamanagement.Service.UploadService;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.List;

@RestController
@RequestMapping("/api/upload")
public class UploadController {
    @Autowired
    private UploadService uploadService;
    String serverUrl = "http://localhost:5000/api/json/generate";
    String directoryPath = "C:\\Users\\workhorse\\Documents\\Final Project\\Data Managent(Zata-g)\\GeneratedFiles";

    @Autowired
    private ProcessOcrDocument processOcrDocument;

    @PostMapping
    public CompletableFuture<String> upload(@RequestParam("file") MultipartFile file, @RequestParam("prompt") String prompt,String MongoUri,String MongoDbName,String MongoCollectionName) throws IOException {
        if (file.isEmpty()) {
            return CompletableFuture.completedFuture("please upload a file that is not empty!");
        }
        uploadService.convertAndStoreMultipartFile(file, directoryPath);
        String filePath = directoryPath + "\\" + file.getOriginalFilename();
        try {
            processOcrDocument.processOcrDocument(filePath);
        } catch (IOException | ExecutionException | InterruptedException | TimeoutException e) {
            throw new RuntimeException(e);
        }
        String textFileName = file.getOriginalFilename() + ".txt";
        CompletableFuture<List<JSONObject>> res = uploadService.sendFileToServerAsync(directoryPath, textFileName, serverUrl, prompt);

        return res.thenApply(r -> {
            if (r != null && !r.isEmpty()) {
                uploadService.createCollectionWithJson(MongoUri, MongoDbName, MongoCollectionName, r);
                return "File uploaded successfully";
            } else {
                return "Failed to upload file or empty response received.";
            }
        });
    }
//    @GetMapping
//    public void test() throws IOException, ExecutionException, InterruptedException, TimeoutException {
//        processOcrDocument.processOcrDocument();
//    }
}