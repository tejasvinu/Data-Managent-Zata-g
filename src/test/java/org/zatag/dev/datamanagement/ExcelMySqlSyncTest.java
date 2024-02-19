package org.zatag.dev.datamanagement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.zatag.dev.datamanagement.Scheduled.ExcelMySqlSync;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

public class ExcelMySqlSyncTest {

    @Mock
    private DriverManagerDataSource dataSource;

    private ExcelMySqlSync excelMySqlSync;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        Map<String, String> schema = new HashMap<>();
        schema.put("id", "INT");
        schema.put("name", "VARCHAR(255)");
        Path path = Paths.get("src/test/resources/test.xlsx");
        excelMySqlSync = new ExcelMySqlSync(path, "jdbc:mysql://localhost:3306/testdb", "root", "123", "test", schema);
        excelMySqlSync.setDataSource(dataSource);
    }

    @Test
    public void syncDataHappyPath() throws Exception {
        when(dataSource.getConnection()).thenReturn(null);
        excelMySqlSync.syncData();
    }

    @Test
    public void readExcelFileHappyPath() throws Exception {
        when(dataSource.getConnection()).thenReturn(null);
        assertEquals(0, excelMySqlSync.readExcelFile("src/test/resources/test.xlsx").size());
    }

    @Test
    public void fetchMySQLDataHappyPath() throws Exception {
        when(dataSource.getConnection()).thenReturn(null);
        assertEquals(0, excelMySqlSync.fetchMySQLData().size());
    }

    @Test
    public void getPrimaryKeyHappyPath() throws Exception {
        when(dataSource.getConnection()).thenReturn(null);
        assertEquals("", excelMySqlSync.getPrimaryKey("test"));
    }

    @Test
    public void createTableIfNotExistsHappyPath() throws Exception {
        when(dataSource.getConnection()).thenReturn(null);
        assertFalse(excelMySqlSync.createTableIfNotExists());
    }
}