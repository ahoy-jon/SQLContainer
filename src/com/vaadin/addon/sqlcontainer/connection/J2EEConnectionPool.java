package com.vaadin.addon.sqlcontainer.connection;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class J2EEConnectionPool implements JDBCConnectionPool, Serializable {
    private static final long serialVersionUID = 3814945507319125205L;

    private String dataSourceJndiName;

    public J2EEConnectionPool(String dataSourceJndiName) {
        this.dataSourceJndiName = dataSourceJndiName;
    }

    public Connection reserveConnection() throws SQLException {
        Connection conn = null;
        DataSource ds = null;

        try {
            InitialContext ic = new InitialContext();
            ds = (DataSource) ic.lookup(dataSourceJndiName);
            conn = ds.getConnection();
            conn.setAutoCommit(false);
        } catch (NamingException e) {
            throw new SQLException(
                    "NamingException - Cannot connect to the database. Cause: "
                            + e.getMessage());
        }
        return conn;
    }

    public void releaseConnection(Connection conn) {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void destroy() {
        // Nothing to do.
    }

}
