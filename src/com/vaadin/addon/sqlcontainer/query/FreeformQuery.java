package com.vaadin.addon.sqlcontainer.query;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;

import com.vaadin.addon.sqlcontainer.RowItem;
import com.vaadin.addon.sqlcontainer.connection.JDBCConnectionPool;

public class FreeformQuery implements QueryDelegate {

    FreeformQueryDelegate delegate = null;
    private String queryString;
    private List<String> primaryKeyColumns;
    private JDBCConnectionPool connectionPool;
    private Connection activeConnection = null;

    /**
     * Prevent no-parameters instantiation of FreeformQuery
     */
    private FreeformQuery() {
    }

    public FreeformQuery(String queryString, List<String> primaryKeyColumns,
            JDBCConnectionPool connectionPool) {
        if (primaryKeyColumns == null || primaryKeyColumns.isEmpty()) {
            throw new IllegalArgumentException(
                    "The primary key columns must be specified!");
        } else if (primaryKeyColumns.contains("")) {
            throw new IllegalArgumentException(
                    "The primary key columns contain an empty string!");
        }
        this.queryString = queryString;
        this.primaryKeyColumns = Collections
                .unmodifiableList(primaryKeyColumns);
        this.connectionPool = connectionPool;
    }

    /**
     * This implementation of getCount() actually fetches all records from the
     * database, which might be a performance issue. Override this method with a
     * SELECT COUNT() ... query if this is too slow for your needs.
     * 
     * @see com.vaadin.addon.sqlcontainer.query.QueryDelegate#getCount()
     */
    public int getCount() throws SQLException {
        // TODO: try with the delegate first before doing it as below!
        Connection conn = getConnection();
        Statement statement = conn.createStatement(
                ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = statement.executeQuery(queryString);
        if (!rs.last()) {
            throw new SQLException(
                    "There were no records in the recordset - cannot count.");
        }
        return rs.getRow();
    }

    private Connection getConnection() throws SQLException {
        if (activeConnection != null) {
            return activeConnection;
        }
        return connectionPool.reserveConnection();
    }

    /**
     * Fetches the results for the query. This implementation always fetches the
     * entire record set, ignoring the offset and pagelength parameters. In
     * order to support lazy loading of records, you must supply a
     * FreeformQueryDelegate that implements the
     * FreeformQueryDelegate.getQueryString(int,int) method.
     * 
     * @throws SQLException
     * 
     * @see com.vaadin.addon.sqlcontainer.query.FreeformQueryDelegate#getQueryString(int,
     *      int)
     * @see com.vaadin.addon.sqlcontainer.query.QueryDelegate#getResults(int,
     *      int)
     */
    public ResultSet getResults(int offset, int pagelength) throws SQLException {
        String query = queryString;
        if (delegate != null) {
            try {
                query = delegate.getQueryString(offset, pagelength);
            } catch (UnsupportedOperationException e) {
                // This is fine, we'll just use the default queryString.
            }
        }
        Connection conn = getConnection();
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery(query);
        statement.close();
        return rs;
    }

    public void setFilters(List<Filter> filters)
            throws UnsupportedOperationException {
        if (delegate != null) {
            delegate.setFilters(filters);
        } else if (filters != null) {
            throw new UnsupportedOperationException(
                    "FreeFormQueryDelegate not set!");
        }
    }

    public void setOrderBy(List<OrderBy> orderBys)
            throws UnsupportedOperationException {
        if (delegate != null) {
            delegate.setOrderBy(orderBys);
        } else if (orderBys != null) {
            throw new UnsupportedOperationException(
                    "FreeFormQueryDelegate not set!");
        }
    }

    public int storeRow(RowItem row) throws UnsupportedOperationException {
        if (delegate != null) {
            return delegate.storeRow(row);
        } else {
            throw new UnsupportedOperationException(
                    "FreeFormQueryDelegate not set!");
        }
    }

    public boolean removeRow(RowItem row) throws UnsupportedOperationException,
            SQLException {
        if (delegate != null) {
            return delegate.removeRow(row);
        } else {
            throw new UnsupportedOperationException(
                    "FreeFormQueryDelegate not set!");
        }
    }

    public synchronized void beginTransaction()
            throws UnsupportedOperationException, SQLException {
        if (delegate == null) {
            throw new UnsupportedOperationException();
        }
        if (activeConnection != null) {
            connectionPool.releaseConnection(activeConnection);
        }
        activeConnection = connectionPool.reserveConnection();
        activeConnection.setAutoCommit(false);
    }

    public synchronized void commit() throws UnsupportedOperationException,
            SQLException {
        if (delegate == null) {
            throw new UnsupportedOperationException();
        }
        if (activeConnection == null) {
            throw new SQLException("No active transaction");
        }
        if (!activeConnection.getAutoCommit()) {
            activeConnection.commit();
        }
        connectionPool.releaseConnection(activeConnection);
        activeConnection = null;
    }

    public synchronized void rollback() throws UnsupportedOperationException,
            SQLException {
        if (delegate == null) {
            throw new UnsupportedOperationException();
        }
        if (activeConnection == null) {
            throw new SQLException("No active transaction");
        }
        activeConnection.rollback();
        connectionPool.releaseConnection(activeConnection);
        activeConnection = null;
    }

    public List<String> getPrimaryKeyColumns() {
        return primaryKeyColumns;
    }

    public String getQueryString() {
        return queryString;
    }

    public FreeformQueryDelegate getDelegate() {
        return delegate;
    }

    public void setDelegate(FreeformQueryDelegate delegate) {
        this.delegate = delegate;
    }

    public ResultSet getIdList() throws SQLException {
        // TODO This method should be removed from the interface and not used in
        // any way in any implementation.
        return null;
    }

}
