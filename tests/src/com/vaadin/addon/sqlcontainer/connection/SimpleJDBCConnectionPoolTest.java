package com.vaadin.addon.sqlcontainer.connection;

import java.sql.Connection;
import java.sql.SQLException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

public class SimpleJDBCConnectionPoolTest {

    private SimpleJDBCConnectionPool connectionPool;

    @Before
    public void setUp() {
        try {
            connectionPool = new SimpleJDBCConnectionPool(
                    "com.mysql.jdbc.Driver",
                    "jdbc:mysql://localhost:3306/sqlcontainer", "sqlcontainer",
                    "sqlcontainer", 2, 2);
        } catch (SQLException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void reserveConnection_reserveNewConnection_returnsConnection() {
        try {
            Connection conn = connectionPool.reserveConnection();
            Assert.assertNotNull(conn);
        } catch (SQLException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void releaseConnection_releaseUnused_shouldNotThrowException() {
        try {
            Connection conn = connectionPool.reserveConnection();
            connectionPool.releaseConnection(conn);
            Assert.assertFalse(conn.isClosed());
        } catch (SQLException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test(expected = SQLException.class)
    public void reserveConnection_noConnectionsLeft_shouldFail()
            throws SQLException {
        try {
            connectionPool.reserveConnection();
            connectionPool.reserveConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            Assert.fail("Exception before all connections used! "
                    + e.getMessage());
        }

        connectionPool.reserveConnection();
        Assert.fail("Reserving connection didn't fail even though no connections are available!");
    }

    @Test
    public void reserveConnection_oneConnectionLeft_returnsConnection() {
        try {
            connectionPool.reserveConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            Assert.fail("Exception before all connections used! "
                    + e.getMessage());
        }

        try {
            Connection conn = connectionPool.reserveConnection();
            Assert.assertNotNull(conn);
        } catch (SQLException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void reserveConnection_oneConnectionJustReleased_returnsConnection() {
        Connection conn2 = null;
        try {
            connectionPool.reserveConnection();
            conn2 = connectionPool.reserveConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            Assert.fail("Exception before all connections used! "
                    + e.getMessage());
        }

        connectionPool.releaseConnection(conn2);

        try {
            connectionPool.reserveConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }
}
