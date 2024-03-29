package org.zatag.dev.datamanagement.Scheduled;

import lombok.Getter;
import lombok.Setter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;


@Getter
@Setter
public class ExcelMySqlSync {

    private String excelFilePath;
    private String dbUrl;
    private String user;
    private String pass;
    private String tableName;
    private  Map<String, String> schema;
    private DataSource dataSource;

    public ExcelMySqlSync(Path excelFilePath, String dbUrl, String user, String pass, String tableName, Map<String, String> schema) {
        this.excelFilePath = String.valueOf(excelFilePath);
        this.dbUrl = dbUrl;
        this.user = user;
        this.pass = pass;
        this.tableName = tableName;
        this.schema = schema;
        this.dataSource = new DriverManagerDataSource();
        System.out.println("ExcelMySqlSync constructor called");
        System.out.println("Excel file path: " + this.excelFilePath);
        System.out.println("DB URL: " + this.dbUrl);
        System.out.println("User: " + this.user);
        System.out.println("Pass: " + this.pass);
        System.out.println("Table name: " + this.tableName);
        System.out.println("db url"+ dbUrl);
        System.out.println("schema: " + this.schema);
    }

    public ExcelMySqlSync() {

    }

    public static void main(String[] args) throws IOException, SQLException {

    }

    public void syncData() throws IOException, SQLException {
        System.out.println("Syncing data");
        if (!createTableIfNotExists()) {
            System.out.println("Table created");
            List<Map<String, String>> excelData = readExcelFile(excelFilePath);
            System.out.println("Excel data: " + excelData);
            for (Map<String, String> row : excelData) {
                System.out.println("Inserting row: " + row);
                insertRowToMySQL(row);
            }
            return; // End the function execution here
        }
        // Read Excel file
        List<Map<String, String>> excelData = readExcelFile(excelFilePath);

        System.out.println("Excel data: " + excelData);
        System.out.println();
        // Fetch data from MySQL table
        List<Map<String, String>> mysqlData = fetchMySQLData();
        System.out.println("MySQL data: " + mysqlData);
        // Compare and sync data
        syncData(excelData, mysqlData);
    }

    public List<Map<String, String>> readExcelFile(String filePath) throws IOException {
        List<Map<String, String>> data = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(new FileInputStream(filePath))) {
            for (Sheet sheet : workbook) {
                Iterator<Row> rowIterator = sheet.iterator();
                // Read the first row (header)
                List<String> headers = new ArrayList<>();
                if (rowIterator.hasNext()) {
                    Row headerRow = rowIterator.next();
                    for (Cell cell : headerRow) {
                        headers.add(cell.getStringCellValue());
                    }
                }
                System.out.println("Headers: " + headers);
                // Read the data
                while (rowIterator.hasNext()) {
                    Row row = rowIterator.next();
                    Map<String, String> rowData = new HashMap<>();
                    for (int i = 0; i < headers.size(); i++) {
                        Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        System.out.println("Cell: " + cell);
                        String cellValue = "";
                        switch (cell.getCellType()) {
                            case STRING:
                                cellValue = cell.getStringCellValue();
                                break;
                            case NUMERIC:
                                if (headers.get(i).equals("id")) {
                                    cellValue = String.valueOf((int) cell.getNumericCellValue());
                                } else {
                                    cellValue = String.valueOf(cell.getNumericCellValue());
                                }
                                break;
                            default:
                                cellValue = cell.getStringCellValue();
                        }
                        rowData.put(headers.get(i), cellValue);
                    }
                    System.out.println("Row data: " + rowData);
                    data.add(rowData);
                }
            }
        }
        return data;
    }

    public List<Map<String, String>> fetchMySQLData() throws SQLException {
        System.out.println("URL: " + dbUrl);
        System.out.println("USER: " + user);
        System.out.println("PASS: " + pass);
        System.out.println("TABLE_NAME: " + tableName);
        ((DriverManagerDataSource) dataSource).setUrl(dbUrl);
        ((DriverManagerDataSource) dataSource).setUsername(user);
        ((DriverManagerDataSource) dataSource).setPassword(pass);
        List<Map<String, String>> data = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName)) {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (rs.next()) {
                Map<String, String> rowData = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    String value = rs.getString(i);
                    rowData.put(columnName, value);
                }
                data.add(rowData);
            }
        }
        return data;
    }

    private void syncData(List<Map<String, String>> excelData, List<Map<String, String>> mysqlData) throws IOException, SQLException {
        // Find new rows in Excel data
        List<Map<String, String>> newExcelRows = new ArrayList<>(excelData);
        System.out.println("excelData: " + excelData);
        System.out.println("mysqlData: " + mysqlData);
        newExcelRows.removeIf(excelRow -> mysqlData.stream().anyMatch(mysqlRow -> mysqlRow.get("id").equals(excelRow.get("id"))));

        System.out.println("newExcelRows: " + newExcelRows);
        // Find deleted rows in Excel data
        List<Map<String, String>> deletedExcelRows = new ArrayList<>(mysqlData);
        System.out.println("mysqlData: " + mysqlData);
        System.out.println("excelData: " + excelData);
        deletedExcelRows.removeIf(mysqlRow -> excelData.stream().anyMatch(excelRow -> excelRow.get("id").equals(mysqlRow.get("id"))));
        System.out.println("deletedExcelRows: " + deletedExcelRows);
        // Find updated rows in Excel data
        List<Map<String, String>> updatedExcelRows = new ArrayList<>();
        for (Map<String, String> excelRow : excelData) {
            for (Map<String, String> mysqlRow : mysqlData) {
                if (Objects.equals(mysqlRow.get("id"), excelRow.get("id")) && !mysqlRow.equals(excelRow)) {
                    updatedExcelRows.add(excelRow);
                    break;
                }
            }
        }
        System.out.println("updatedExcelRows: " + updatedExcelRows);

        // Update MySQL table
        for (Map<String, String> row : newExcelRows) {
            insertRowToMySQL(row);
        }
        for (Map<String, String> row : deletedExcelRows) {
            deleteRowFromMySQL(row);
        }
        for (Map<String, String> row : updatedExcelRows) {
            updateRowInMySQL(row);
        }

        // Update Excel file
        excelData.removeAll(deletedExcelRows);
        excelData.addAll(newExcelRows);
        writeDataToExcel(excelData, excelFilePath);
    }

    private void updateRowInMySQL(Map<String, String> row) {
        ((DriverManagerDataSource) dataSource).setUrl(dbUrl);
        ((DriverManagerDataSource) dataSource).setUsername(user);
        ((DriverManagerDataSource) dataSource).setPassword(pass);
        // Parse the row information as needed
        System.out.println("Received row data: " + row);
        String primaryKey = getPrimaryKey(tableName);
        System.out.println("Primary key: " + primaryKey);
        // Now you can connect to the database and update the row
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            // Implement your update logic using the row data
            String updateValues = row.entrySet().stream()
                    .map(entry -> entry.getKey() + " = '" + entry.getValue() + "'")
                    .reduce((entry1, entry2) -> entry1 + ", " + entry2)
                    .orElse("");
            statement.executeUpdate("UPDATE " + tableName + " SET " + updateValues + " WHERE " + primaryKey + " = '" + row.get(primaryKey) + "'");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void insertRowToMySQL(Map<String, String> row) throws SQLException {
        System.out.println("insertRowToMySQL called");
        ((DriverManagerDataSource) dataSource).setUrl(dbUrl);
        ((DriverManagerDataSource) dataSource).setUsername(user);
        ((DriverManagerDataSource) dataSource).setPassword(pass);

           // Parse the row information as needed
        System.out.println("Received row data: " + row);
        // Now you can connect to the database and insert the row
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            // Implement your insert logic using the row data
            String columns = String.join(", ", row.keySet());
            String values = row.values().stream()
                    .map(value -> "'" + value + "'")
                    .reduce((value1, value2) -> value1 + ", " + value2)
                    .orElse("");
            statement.executeUpdate("INSERT INTO " + tableName + " (" + columns + ") VALUES (" + values + ")");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteRowFromMySQL(Map<String, String> row) throws SQLException {
        System.out.println("deleteRowFromMySQL called");
        ((DriverManagerDataSource) dataSource).setUrl(dbUrl);
        ((DriverManagerDataSource) dataSource).setUsername(user);
        ((DriverManagerDataSource) dataSource).setPassword(pass);
        System.out.println("URL: " + ((DriverManagerDataSource) dataSource).getUrl());
        // Parse the row information as needed
        System.out.println("Received row data: " + row);
        String primaryKey = getPrimaryKey(tableName);
        System.out.println("Primary key: " + primaryKey);
        System.out.println(row.get(primaryKey));
        // Now you can connect to the database and delete the row
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            // Implement your delete logic using the row data
            statement.executeUpdate("DELETE FROM " + tableName + " WHERE " + primaryKey + " = '" + row.get(primaryKey) + "'");
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

    public static void writeDataToExcel(List<Map<String, String>> data, String filePath) throws IOException {
        // Implement this method to write data to Excel file
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");
        Row headerRow = sheet.createRow(0);
        int rowNum = 1;
        for (Map<String, String> row : data) {
            Row excelRow = sheet.createRow(rowNum++);
            int cellNum = 0;
            for (Map.Entry<String, String> entry : row.entrySet()) {
                if (rowNum == 2) {
                    Cell headerCell = headerRow.createCell(cellNum);
                    headerCell.setCellValue(entry.getKey());
                }
                Cell cell = excelRow.createCell(cellNum++);
                cell.setCellValue(entry.getValue());
            }
        }
    }
    public boolean createTableIfNotExists() throws SQLException, IOException {
    boolean tableExists = false;
    try (Connection conn = DriverManager.getConnection(dbUrl, user, pass);
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery("SHOW TABLES LIKE '" + tableName + "'")) {
        if (!rs.next()) {
            StringBuilder columns = new StringBuilder();
            for (Map.Entry<String, String> entry : schema.entrySet()) {
                if (!columns.isEmpty()) {
                    columns.append(", ");
                }
                columns.append(entry.getKey()).append(" ").append(entry.getValue());
            }
            String createTableSql = "CREATE TABLE " + tableName + " (" + columns + ", PRIMARY KEY (id)) ENGINE=INNODB;";
            stmt.execute(createTableSql);
        } else {
            tableExists = true;
        }
    }
    return tableExists;
    }
}
