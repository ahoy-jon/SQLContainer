package com.vaadin.addon.sqlcontainer.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/**
 * Simple implementation of the JDBCConnectionPool interface. Handles loading
 * the JDBC driver, setting up the connections and ensuring they are still
 * usable upon release.
 */
public class SimpleJDBCConnectionPool implements JDBCConnectionPool {

    private int initialConnections = 5;
    private int maxConnections = 20;

    private String connectionUri;
    private String userName;
    private String password;

    private Set<Connection> availableConnections;
    private Set<Connection> reservedConnections;

    private boolean initialized;

    public SimpleJDBCConnectionPool(String driverName, String connectionUri,
            String userName, String password) throws SQLException {
        if (driverName == null) {
            throw new IllegalArgumentException(
                    "JDBC driver class name must be given.");
        }
        if (connectionUri == null) {
            throw new IllegalArgumentException(
                    "Database connection URI must be given.");
        }
        if (userName == null) {
            throw new IllegalArgumentException(
                    "Database username must be given.");
        }
        if (password == null) {
            throw new IllegalArgumentException(
                    "Database password must be given.");
        }
        this.connectionUri = connectionUri;
        this.userName = userName;
        this.password = password;

        /* Initialize JDBC driver */
        try {
            Class.forName(driverName).newInstance();
        } catch (Exception ex) {
            throw new RuntimeException("Specified JDBC Driver: " + driverName
                    + " - initialization failed.", ex);
        }
    }

    public SimpleJDBCConnectionPool(String driverName, String connectionUri,
            String userName, String password, int initialConnections,
            int maxConnections) throws SQLException {
        this(driverName, connectionUri, userName, password);
        this.initialConnections = initialConnections;
        this.maxConnections = maxConnections;
    }

    private void initializeConnections() throws SQLException {
        availableConnections = new HashSet<Connection>(initialConnections);
        reservedConnections = new HashSet<Connection>(initialConnections);
        for (int i = 0; i < initialConnections; i++) {
            availableConnections.add(createConnection());
        }
        initialized = true;
    }

    public synchronized Connection reserveConnection() throws SQLException {
        if (!initialized) {
            initializeConnections();
        }
        if (availableConnections.isEmpty()) {
            if (reservedConnections.size() <= maxConnections) {
                availableConnections.add(createConnection());
            } else {
                throw new IllegalStateException(
                        "Connection limit has been reached.");
            }
        }

        Connection c = availableConnections.iterator().next();
        availableConnections.remove(c);
        reservedConnections.add(c);

        return c;
    }

    public synchronized void releaseConnection(Connection conn) {
        if (conn == null || !initialized) {
            return;
        }
        /* Try to roll back if necessary */
        try {
            if (!conn.getAutoCommit()) {
                conn.rollback();
            }
        } catch (SQLException e) {
            /* Roll back failed, discard connection */
            reservedConnections.remove(conn);
        }
        reservedConnections.remove(conn);
        availableConnections.add(conn);
    }

    private Connection createConnection() throws SQLException {
        Connection c = DriverManager.getConnection(connectionUri, userName,
                password);
        c.setAutoCommit(false);
        return c;
    }

}
