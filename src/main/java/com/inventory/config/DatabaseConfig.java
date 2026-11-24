package com.inventory.config;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * Manages SQLite database connection and initialization.
 */
public class DatabaseConfig {
    private static final String DEFAULT_DB_URL = "jdbc:sqlite:inventory.db";
    private static String dbUrl = DEFAULT_DB_URL;

    public static void setDatabaseUrl(String url) {
        dbUrl = url;
    }

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(dbUrl);
        // Enable foreign key constraints in SQLite
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    /**
     * Initializes the SQLite database schema if not present.
     */
    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            InputStream is = DatabaseConfig.class.getResourceAsStream("/db-schema.sql");
            if (is != null) {
                String sql = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                        .lines().collect(Collectors.joining("\n"));
                for (String query : sql.split(";")) {
                    if (!query.trim().isEmpty()) {
                        stmt.execute(query.trim());
                    }
                }
            } else {
                // Fallback default schema if resource file is not packaged
                stmt.execute("CREATE TABLE IF NOT EXISTS products (" +
                        "product_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT NOT NULL UNIQUE, " +
                        "price REAL NOT NULL, " +
                        "quantity_in_stock INTEGER NOT NULL DEFAULT 0, " +
                        "supplier_lead_time_days INTEGER NOT NULL DEFAULT 7)");

                stmt.execute("CREATE TABLE IF NOT EXISTS sales (" +
                        "sale_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "product_id INTEGER NOT NULL, " +
                        "sale_date TEXT NOT NULL, " +
                        "quantity_sold INTEGER NOT NULL, " +
                        "unit_price REAL NOT NULL, " +
                        "FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE)");
            }
        } catch (Exception e) {
            System.err.println("Database initialization warning: " + e.getMessage());
        }
    }
}
