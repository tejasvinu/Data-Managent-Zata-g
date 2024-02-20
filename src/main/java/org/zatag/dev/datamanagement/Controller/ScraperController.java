package org.zatag.dev.datamanagement.Controller;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.zatag.dev.datamanagement.Models.scrapers.Scraper;
import org.zatag.dev.datamanagement.Repository.scraper.ScraperRepository;
import org.zatag.dev.datamanagement.Scheduled.WebDbSync;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/scraper")
public class ScraperController {

    @Autowired
    private WebDbSync webDbSync;

    @Autowired
    private ScraperRepository scraperRepository;

    @PostMapping("/upload")
    public String uploadScraper(@RequestParam("file") MultipartFile file) {
        try {
            // Change this to a directory within your project's scope
            String uploadDir = "src/main/java/scrapers/";
            String fileName = file.getOriginalFilename();
            String extension = FilenameUtils.getExtension(fileName);
            if (!"py".equals(extension)) {
                throw new IllegalArgumentException("Invalid file type: " + fileName);
            }
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Files.copy(file.getInputStream(), Paths.get(uploadDir + fileName));
            scraperRepository.save(new Scraper(fileName));
            return "Scraper uploaded successfully!";
        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to upload scraper";
        }
    }

    @GetMapping("/list")
    public List<Scraper> getScrapers() {
        return scraperRepository.findAll();
    }

    @GetMapping("/run/{fileName}")
    public void runScraper(@PathVariable String fileName) {
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
            String outputDir = "C:\\Users\\workhorse\\Documents\\Final Project\\Data Managent(Zata-g)\\GeneratedFiles\\";

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

    @GetMapping("/sync")
    public void SynchroWeb() {
        webDbSync.SyncDbs();
    }
}
