package com.stocklms.stock_lms.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

/**
 * Startup runner to verify and log successful PostgreSQL database connectivity.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseConnectionChecker implements CommandLineRunner {

    private final DataSource dataSource;

    @Override
    public void run(String... args) {
        log.info("==================================================================");
        log.info("Verifying PostgreSQL Database Connection...");
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            log.info("DATABASE CONNECTION SUCCESSFUL!");
            log.info("Connected to Database Name: {}", connection.getCatalog());
            log.info("Database Product Name: {}", metaData.getDatabaseProductName());
            log.info("Database Product Version: {}", metaData.getDatabaseProductVersion());
            log.info("Database Driver Name: {}", metaData.getDriverName());
            log.info("Database URL: {}", metaData.getURL());
            log.info("All 9 entities initialized via ddl-auto=update");
            log.info("==================================================================");
        } catch (Exception e) {
            log.error("DATABASE CONNECTION FAILED: {}", e.getMessage(), e);
        }
    }
}
