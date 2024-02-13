package org.zatag.dev.datamanagement.Scheduled;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.nio.file.Files;
import java.util.stream.Stream;

import org.apache.poi.ss.usermodel .*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
@Component
public class FileChangeChecker {

    private final Path directoryPath = Paths.get("C:\\Users\\workhorse\\Documents\\Final Project\\Data Managent(Zata-g)\\GeneratedFiles");
    private final Map<Path, String> fileContents = new HashMap<>();

    //@Scheduled(fixedRate = 10000) // Check for changes every 5 seconds
    public void checkForFileChanges() {
        System.out.println("Checking for file changes");
        try (Stream<Path> paths = Files.walk(directoryPath)) {
            paths.forEach(path -> {
                if (path.toString().endsWith(".xlsx")) {
                    String fileName = path.toString();
                    String className = fileName.replace(".xlsx", ".json");
                    System.out.println(className);
                    Map<String, Object> dbInfo = extractDbInfo(className);
                    System.out.println(dbInfo);
                    System.out.println("File found: " + path);
                    String newContent = readExcelFile(path);
                    String dbUrl = (String) dbInfo.get("mysqlUrl");
                    String user = (String) dbInfo.get("username");
                    String pass = (String) dbInfo.get("password");
                    String tableName = (String) dbInfo.get("tableName");
                    Map<String, String> schema = (Map<String, String>) dbInfo.get("schema");
                    System.out.println("DB URL: " + dbUrl);
                    System.out.println("User: " + user);
                    System.out.println("Pass: " + pass);
                    System.out.println("Table name: " + tableName);
                    System.out.println("schema: " + schema);
                    ExcelMySqlSync excelMySqlSync = new ExcelMySqlSync(path, dbUrl, user, pass, tableName, schema);
                    try {
                        if (excelMySqlSync.createTableIfNotExists()) {
                            if (fileContents.containsKey(path)) {
                                String oldContent = fileContents.get(path);
                                if (!newContent.equals(oldContent)) {
                                    System.out.println("File changed: " + path);
                                    excelMySqlSync.syncData();
                                    fileContents.put(path, newContent);
                                }
                            } else {
                                fileContents.put(path, newContent);
                                excelMySqlSync.syncData();
                            }
                        }
                    } catch (SQLException | IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private String readExcelFile(Path path) {
        StringBuilder content = new StringBuilder();
        try (Workbook workbook = new XSSFWorkbook(Files.newInputStream(path))) {
            for (Sheet sheet : workbook) {
                for (Row row : sheet) {
                    for (Cell cell : row) {
                        content.append(cell.toString()).append(" ");
                    }
                    content.append("\n");
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + path);
            System.out.println("Skipping file.");
            return "";
        }
        return content.toString();
    }

    public Map<String, Object> extractDbInfo(String filePath) {
        Path jsonPath = Paths.get(filePath);
        System.out.println("json: "+ jsonPath);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            if (Files.exists(jsonPath) && jsonPath.toString().endsWith(".json")) {
                String jsonContent = Files.readString(jsonPath);
                return objectMapper.readValue(jsonContent, Map.class);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

}

