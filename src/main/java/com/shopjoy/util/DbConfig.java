package com.shopjoy.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DbConfig {

    private static final Properties props = new Properties();

    static {
        try (InputStream in = DbConfig.class.getResourceAsStream("/application.properties")) {
            if (in == null) {
                throw new RuntimeException("application.properties not found on classpath");
            }
            props.load(in);

            // Explicit driver load (safe + predictable)
            String driver = props.getProperty("db.driver");
            if (driver != null) {
                Class.forName(driver);
            }

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to initialize DbConfig", e);
        }
    }

    private DbConfig() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                props.getProperty("db.url"),
                props.getProperty("db.username"),
                props.getProperty("db.password")
        );
    }
}
