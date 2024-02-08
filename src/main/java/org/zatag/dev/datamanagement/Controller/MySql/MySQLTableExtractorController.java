package org.zatag.dev.datamanagement.Controller.MySql;

import org.zatag.dev.datamanagement.Models.MySql.MySqlFiles;
import org.zatag.dev.datamanagement.Models.MySql.MySQLRequest;
import org.zatag.dev.datamanagement.Repository.MySql.MySqlFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/mysql")
public class MySQLTableExtractorController {

    private final DataSource dataSource;

    @Autowired
    private MySqlFileRepository fileGenerationDetailsRepository;

    @Autowired
    public MySQLTableExtractorController() {
        this.dataSource = new DriverManagerDataSource();
    }

    @PostMapping("/extract")
    public MySqlFiles extractMySQLTable(@RequestBody MySQLRequest request) {
        try {
            System.out.println("Request received: " + request.toString());
            // Configure the DataSource based on the request
            ((DriverManagerDataSource) dataSource).setUrl(request.getMysqlUrl());
            ((DriverManagerDataSource) dataSource).setUsername(request.getUsername());
            ((DriverManagerDataSource) dataSource).setPassword(request.getPassword());

            // Now you can connect to the database and extract data
            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery("SELECT * FROM " + request.getTableName())) {
                System.out.println("Connection established successfully!");
                System.out.println("Extracting data from the table: " + request.getTableName());
                // Process ResultSet and generate JSON data
                StringBuilder jsonData = new StringBuilder("[");
                while (resultSet.next()) {
                    int columnCount = resultSet.getMetaData().getColumnCount();
                    jsonData.append("{");
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = resultSet.getMetaData().getColumnName(i);
                        String columnValue = resultSet.getString(i);
                        jsonData.append("\"").append(columnName).append("\":\"").append(columnValue).append("\",");
                    }
                    jsonData.deleteCharAt(jsonData.length() - 1); // Remove the last comma
                    jsonData.append("},");
                }
                if (jsonData.charAt(jsonData.length() - 1) == ',') {
                    jsonData.deleteCharAt(jsonData.length() - 1); // Remove the last comma
                }
                jsonData.append("]");

                // Write JSON data to a file
                String fileName = writeJsonToFile(jsonData.toString());

                System.out.println("JSON file generated successfully!");

                // Save file generation details to the database
                saveFileGenerationDetails(fileName);

                return new MySqlFiles(fileName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String writeJsonToFile(String jsonData) throws IOException {
        // Replace ":" in the timestamp with "_"
        String timestamp = LocalDateTime.now().toString().replace(":", "_");
        String fileName = "mysql_Table_data" + timestamp + ".json";

        // Specify your directory path here
        String directoryPath = "C:\\Users\\workhorse\\Documents\\Final Project\\Data Managent(Zata-g)\\GeneratedFiles\\";

        try (FileWriter fileWriter = new FileWriter(directoryPath + fileName)) {
            fileWriter.write(jsonData);
        }
        return fileName;
    }

    private void saveFileGenerationDetails(String fileName) {
        // Save file generation details to the database
        MySqlFiles details = new MySqlFiles();
        details.setFileName(fileName);
        fileGenerationDetailsRepository.save(details);
    }

    @GetMapping("/List")
    public Iterable<MySqlFiles> listFiles() {
        return fileGenerationDetailsRepository.findAll();
    }
}
