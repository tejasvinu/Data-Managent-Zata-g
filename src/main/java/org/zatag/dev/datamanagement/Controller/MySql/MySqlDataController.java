package org.zatag.dev.datamanagement.Controller.MySql;

import org.jetbrains.annotations.NotNull;
import org.zatag.dev.datamanagement.Models.MySql.MySQLLink;
import org.zatag.dev.datamanagement.Models.MySql.MySqlFiles;
import org.zatag.dev.datamanagement.Repository.MySql.MySQLLinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Optional;

/**
 * This class is a controller for handling MySQL data operations.
 * It provides endpoints for extracting, deleting, updating, and inserting data into a MySQL database.
 */
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/mysql-data")
public class MySqlDataController {

    private final DataSource dataSource;
    @Autowired
    private MySQLLinkRepository mySQLLinkRepository;

    @Autowired
    public MySqlDataController(MySQLLinkRepository mySQLLinkRepository) {
        this.dataSource = new DriverManagerDataSource();
        this.mySQLLinkRepository = mySQLLinkRepository;
    }

    /**
     * This method extracts data from a MySQL table and returns it as a JSON string.
     * @param id The id of the MySQLLink to use for the extraction
     * @return A JSON string containing the extracted data
     */
    @GetMapping("/view/{id}")
    public MySqlFiles extractMySQLTable(@PathVariable String id) {

        try {
            Optional<MySQLLink> optionalRequest = mySQLLinkRepository.findById(id);
            MySQLLink request = null;
            if (optionalRequest.isPresent()) {
                request = optionalRequest.get();
                // Configure the DataSource based on the request
                System.out.println(request);
                // Rest of your code
            } else {
                // Handle the case where no MySQLLink was found for the provided id
                System.out.println("No MySQLLink found for id: " + id);
            }
            assert request != null;
            ((DriverManagerDataSource) dataSource).setUrl(request.getMysqlUrl());
            System.out.println(request.getMysqlUrl());
            ((DriverManagerDataSource) dataSource).setUsername(request.getUsername());
            ((DriverManagerDataSource) dataSource).setPassword(request.getPassword());

            // Now you can connect to the database and extract data
            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery("SELECT * FROM " + request.getTableName())) {

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

                return new MySqlFiles(jsonData.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * This method deletes a row from a MySQL table based on the provided row data.
     * @param id The id of the MySQLLink to use for the deletion
     * @param row The row data to use for the deletion
     */
    @DeleteMapping("/view/{id}")
    public void deleteMySQLLink(@PathVariable String id, @RequestBody HashMap<String,String> row) {
        // Connect to MySQL using the provided connection details
        Optional<MySQLLink> optionalRequest = mySQLLinkRepository.findById(id);
        MySQLLink request = null;
        if (optionalRequest.isPresent()) {
            request = optionalRequest.get();
            // Configure the DataSource based on the request
            System.out.println(request);
            // Rest of your code
        } else {
            // Handle the case where no MySQLLink was found for the provided id
            System.out.println("No MySQLLink found for id: " + id);
        }
        assert request != null;
        ((DriverManagerDataSource) dataSource).setUrl(request.getMysqlUrl());
        System.out.println(request.getMysqlUrl());
        ((DriverManagerDataSource) dataSource).setUsername(request.getUsername());
        ((DriverManagerDataSource) dataSource).setPassword(request.getPassword());
        // Parse the row information as needed
        System.out.println("Received row data: " + row);
        String primaryKey = getPrimaryKey(request.getTableName());
        System.out.println("Primary key: " + primaryKey);
        System.out.println(row.get(primaryKey));
        // Now you can connect to the database and delete the row
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            // Implement your delete logic using the row data
            statement.executeUpdate("DELETE FROM " + request.getTableName() + " WHERE " + primaryKey + " = '" + row.get(primaryKey) + "'");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * This method returns the primary key of a MySQL table.
     * @param tableName The name of the table
     * @return The primary key of the table
     */
    public String getPrimaryKey(String tableName) {
        String primaryKey = "";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SHOW INDEXES FROM " + tableName + " WHERE Key_name = 'PRIMARY'")) {

            while (resultSet.next()) {
                primaryKey = resultSet.getString("Column_name");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return primaryKey;
    }

    /**
     * This method updates a row in a MySQL table based on the provided row data.
     * @param id The id of the MySQLLink to use for the update
     * @param row The row data to use for the update
     */
    @PutMapping("/view/{id}")
    public void updateMySQLLink(@PathVariable String id, @RequestBody HashMap<String,String> row) {
        // Connect to MySQL using the provided connection details
        Optional<MySQLLink> optionalRequest = mySQLLinkRepository.findById(id);
        MySQLLink request = null;
        if (optionalRequest.isPresent()) {
            request = optionalRequest.get();
            // Configure the DataSource based on the request
            System.out.println(request);
            // Rest of your code
        } else {
            // Handle the case where no MySQLLink was found for the provided id
            System.out.println("No MySQLLink found for id: " + id);
        }
        assert request != null;
        ((DriverManagerDataSource) dataSource).setUrl(request.getMysqlUrl());
        System.out.println(request.getMysqlUrl());
        ((DriverManagerDataSource) dataSource).setUsername(request.getUsername());
        ((DriverManagerDataSource) dataSource).setPassword(request.getPassword());
        // Parse the row information as needed
        System.out.println("Received row data: " + row);
        String primaryKey = getPrimaryKey(request.getTableName());
        System.out.println("Primary key: " + primaryKey);
        System.out.println(row.get(primaryKey));
        // Now you can connect to the database and update the row
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            // Implement your update logic using the row data
            statement.executeUpdate("UPDATE " + request.getTableName() + " SET " + getUpdateQuery(row) + " WHERE " + primaryKey + " = '" + row.get(primaryKey) + "'");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method returns the update query for a row in a MySQL table.
     * @param row The row data
     * @return The update query for the row
     */
    private @NotNull String getUpdateQuery(@NotNull HashMap<String, String> row) {
        StringBuilder updateQuery = new StringBuilder();
        for (String key : row.keySet()) {
            if (!key.equals("id")) {
                updateQuery.append(key).append(" = '").append(row.get(key)).append("', ");
            }
        }
        updateQuery.deleteCharAt(updateQuery.length() - 2); // Remove the last comma and space
        System.out.println("Update query: " + updateQuery);
        return updateQuery.toString();
    }

    /**
     * This method inserts a row into a MySQL table based on the provided row data.
     * @param id The id of the MySQLLink to use for the insertion
     * @param row The row data to use for the insertion
     */
    @PostMapping("/view/{id}")
    public void insertMySQLLink(@PathVariable String id, @RequestBody HashMap<String,String> row) {
        // Connect to MySQL using the provided connection details
        Optional<MySQLLink> optionalRequest = mySQLLinkRepository.findById(id);
        MySQLLink request = null;
        if (optionalRequest.isPresent()) {
            request = optionalRequest.get();
            // Configure the DataSource based on the request
            System.out.println(request);
            // Rest of your code
        } else {
            // Handle the case where no MySQLLink was found for the provided id
            System.out.println("No MySQLLink found for id: " + id);
        }
        assert request != null;
        ((DriverManagerDataSource) dataSource).setUrl(request.getMysqlUrl());
        System.out.println(request.getMysqlUrl());
        ((DriverManagerDataSource) dataSource).setUsername(request.getUsername());
        ((DriverManagerDataSource) dataSource).setPassword(request.getPassword());
        // Parse the row information as needed
        System.out.println("Received row data: " + row);
        // Now you can connect to the database and insert the row
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            // Implement your insert logic using the row data
            statement.executeUpdate("INSERT INTO " + request.getTableName() + " " + getInsertQuery(row));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method returns the insert query for a row in a MySQL table.
     * @param row The row data
     * @return The insert query for the row
     */
    private @NotNull String getInsertQuery(@NotNull HashMap<String, String> row) {
        StringBuilder insertQuery = new StringBuilder("(");
        for (String key : row.keySet()) {
            insertQuery.append(key).append(", ");
        }
        insertQuery.deleteCharAt(insertQuery.length() - 2); // Remove the last comma and space
        insertQuery.append(") VALUES (");
        for (String value : row.values()) {
            insertQuery.append("'").append(value).append("', ");
        }
        insertQuery.deleteCharAt(insertQuery.length() - 2); // Remove the last comma and space
        insertQuery.append(")");
        System.out.println("Insert query: " + insertQuery);
        return insertQuery.toString();
    }
}
