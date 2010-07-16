package com.vaadin.addon.sqlcontainer.connection;

import java.sql.Connection;

public interface JDBCConnectionPool {
    public Connection reserveConnection();
}
