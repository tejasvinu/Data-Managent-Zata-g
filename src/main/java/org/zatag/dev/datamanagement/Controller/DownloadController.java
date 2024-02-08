package org.zatag.dev.datamanagement.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zatag.dev.datamanagement.Scheduled.FileChangeChecker;

import java.io.File;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/download")
public class DownloadController {

    @Autowired
    private FileChangeChecker fileChangeChecker;
    @GetMapping("/{fileName}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        try {
            // Replace "path_to_your_file" with the actual path to your generated JSON file
            File file = new File("C:\\Users\\workhorse\\Documents\\Final Project\\Data Managent(Zata-g)\\GeneratedFiles\\"+fileName);

            // Create a FileSystemResource from  the file
            Resource resource = new FileSystemResource(file);

            // Set content disposition as attachment
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=mysql_table_data.json");

            // Return a ResponseEntity with the file content, headers, and OK status
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (Exception e) {
            // Handle exception and return appropriate response
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/test")
    public String ChangeChecker() {
        fileChangeChecker.checkForFileChanges();
        return "File Change Checker";
    }
}
