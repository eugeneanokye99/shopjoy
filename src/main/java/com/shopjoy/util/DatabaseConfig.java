package com.shopjoy.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.*;

/**
 * Simple manual connection pool and DB utility. Singleton.
 * Uses DriverManager to create connections. No external pooling libraries.
 */
public class DatabaseConfig {
    private static DatabaseConfig instance;

    private final Properties props = new Properties();
    private final List<Connection> available = new ArrayList<>();
    private final Set<Connection> inUse = new HashSet<>();

    private final String url;
    private final String username;
    private final String password;
    private final String driver;
    private final int maxPoolSize;

    private DatabaseConfig() {
        try (InputStream in = getClass().getResourceAsStream("/application.properties")) {
            if (in != null) props.load(in);
        } catch (IOException e) {
            System.out.println("Failed to load application.properties: " + e.getMessage());
        }

        url = props.getProperty("db.url");
        username = props.getProperty("db.username");
        password = props.getProperty("db.password");
        driver = props.getProperty("db.driver", "org.postgresql.Driver");
        int initial = Integer.parseInt(props.getProperty("db.poolSize", "5"));
        maxPoolSize = Integer.parseInt(props.getProperty("db.maxPoolSize", String.valueOf(Math.max(10, initial))));

        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            System.out.println("JDBC Driver class not found: " + driver + " -> " + e.getMessage());
        }

        for (int i = 0; i < initial && available.size() < maxPoolSize; i++) {
            Connection c = createNewConnection();
            if (c != null) available.add(c);
        }
    }

    public static synchronized DatabaseConfig getInstance() {
        if (instance == null) instance = new DatabaseConfig();
        return instance;
    }

    private Connection createNewConnection() {
        try {
            Connection c = DriverManager.getConnection(url, username, password);
            c.setAutoCommit(true);
            return c;
        } catch (SQLException e) {
            System.out.println("Failed to create DB connection: " + e.getMessage());
            return null;
        }
    }

    public synchronized Connection getConnection() {
        try {
            while (true) {
                if (!available.isEmpty()) {
                    Connection c = available.remove(available.size() - 1);
                    if (isValid(c)) {
                        inUse.add(c);
                        return c;
                    } else {
                        closeQuiet(c);
                        continue;
                    }
                }

                int total = available.size() + inUse.size();
                if (total < maxPoolSize) {
                    Connection c = createNewConnection();
                    if (c != null) {
                        inUse.add(c);
                        return c;
                    } else {
                        // wait briefly and retry
                        waitMillis(200);
                    }
                } else {
                    // pool exhausted - wait until a connection is released
                    try {
                        wait();
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Interrupted while waiting for a DB connection", ie);
                    }
                }
            }
        } catch (RuntimeException re) {
            throw re;
        }
    }

    public synchronized void releaseConnection(Connection conn) {
        if (conn == null) return;
        if (inUse.remove(conn)) {
            if (isValid(conn)) {
                available.add(conn);
            } else {
                closeQuiet(conn);
            }
            notifyAll();
        } else {
            // connection not recognised - close it
            closeQuiet(conn);
        }
    }

    public synchronized void closeAllConnections() {
        for (Connection c : available) closeQuiet(c);
        for (Connection c : inUse) closeQuiet(c);
        available.clear();
        inUse.clear();
        System.out.println("All DB connections closed.");
    }

    public void closeResources(ResultSet rs, PreparedStatement ps, Connection conn) {
        try { if (rs != null) rs.close(); } catch (SQLException e) { System.out.println("Failed to close ResultSet: " + e.getMessage()); }
        try { if (ps != null) ps.close(); } catch (SQLException e) { System.out.println("Failed to close PreparedStatement: " + e.getMessage()); }
        if (conn != null) releaseConnection(conn);
    }

    public boolean testConnection() {
        Connection c = null;
        try {
            c = getConnection();
            boolean ok = isValid(c);
            System.out.println("testConnection: " + ok);
            return ok;
        } catch (RuntimeException e) {
            System.out.println("testConnection failed: " + e.getMessage());
            return false;
        } finally {
            if (c != null) releaseConnection(c);
        }
    }

    private boolean isValid(Connection c) {
        if (c == null) return false;
        try {
            return !c.isClosed() && c.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    private void closeQuiet(Connection c) {
        if (c == null) return;
        try { c.close(); } catch (SQLException e) { /* ignore */ }
    }

    private void waitMillis(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
