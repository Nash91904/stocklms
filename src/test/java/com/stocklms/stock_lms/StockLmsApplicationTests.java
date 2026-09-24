package com.stocklms.stock_lms;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Integration test verifying that Spring ApplicationContext loads
 * and PostgreSQL database connection is operational.
 */
@SpringBootTest
class StockLmsApplicationTests {

    @Autowired
    private DataSource dataSource;

    @Test
    void contextLoads() {
    }

    @Test
    void testDatabaseConnection() throws SQLException {
        assertNotNull(dataSource, "DataSource should be injected and not null");
        try (Connection connection = dataSource.getConnection()) {
            assertNotNull(connection, "Database connection should not be null");
            assertFalse(connection.isClosed(), "Database connection should be active and open");
            System.out.println("=== Successfully connected to PostgreSQL: " + connection.getCatalog() + " ===");
        }
    }
}
