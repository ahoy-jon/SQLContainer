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
    @SuppressWarnings("unused")
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
        } else if (queryString == null || "".equals(queryString)) {
            throw new IllegalArgumentException(
                    "The query string may not be empty or null!");
        } else if (connectionPool == null) {
            throw new IllegalArgumentException(
                    "The connectionPool may not be null!");
        }
        this.queryString = queryString;
        this.primaryKeyColumns = Collections
                .unmodifiableList(primaryKeyColumns);
        this.connectionPool = connectionPool;
    }

    /**
     * This implementation of getCount() actually fetches all records from the
     * database, which might be a performance issue. Override this method with a
     * SELECT COUNT(*) ... query if this is too slow for your needs.
     * 
     * {@inheritDoc}
     */
    public int getCount() throws SQLException {
        // First try the delegate
        int count = countByDelegate();
        if (count < 0) {
            // Couldn't use the delegate, use the bad way.
            Connection conn = getConnection();
            Statement statement = conn.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = statement.executeQuery(queryString);
            if (rs.last()) {
                count = rs.getRow();
            } else {
                count = 0;
            }
            rs.close();
            releaseConnection(conn);
        }
        return count;
    }

    private int countByDelegate() throws SQLException {
        int count = -1;
        if (delegate != null) {
            try {
                String countQuery = delegate.getCountQuery();
                if (countQuery != null) {
                    Connection conn = getConnection();
                    Statement statement = conn.createStatement();
                    ResultSet rs = statement.executeQuery(countQuery);
                    rs.next();
                    count = rs.getInt(1);
                    rs.close();
                    releaseConnection(conn);
                    return count;
                }
            } catch (UnsupportedOperationException e) {
                // No go, the count query wasn't implemented.
                // Do it all below...
            }
        }
        return count;
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
     *      int) {@inheritDoc}
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
        releaseConnection(conn);
        return rs;
    }

    public boolean implementationRespectsPagingLimits() {
        if (delegate == null) {
            return false;
        }
        try {
            String queryString = delegate.getQueryString(0, 50);
            return queryString != null && queryString.length() > 0;
        } catch (UnsupportedOperationException e) {
            return false;
        }
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

    public int storeRow(RowItem row) throws SQLException {
        if (activeConnection == null) {
            throw new IllegalStateException("No transaction is active!");
        }
        if (delegate != null) {
            return delegate.storeRow(activeConnection, row);
        } else {
            throw new UnsupportedOperationException(
                    "FreeFormQueryDelegate not set!");
        }
    }

    public boolean removeRow(RowItem row) throws SQLException {
        if (activeConnection == null) {
            throw new IllegalStateException("No transaction is active!");
        }
        if (delegate != null) {
            return delegate.removeRow(activeConnection, row);
        } else {
            throw new UnsupportedOperationException(
                    "FreeFormQueryDelegate not set!");
        }
    }

    public synchronized void beginTransaction()
            throws UnsupportedOperationException, SQLException {
        if (activeConnection != null) {
            throw new IllegalStateException("A transaction is already active!");
        }
        activeConnection = connectionPool.reserveConnection();
        activeConnection.setAutoCommit(false);
    }

    public synchronized void commit() throws UnsupportedOperationException,
            SQLException {
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

    /**
     * This implementation of the containsRowWithKey method rewrites existing
     * WHERE clauses in the query string. The logic is, however, not very
     * complex and some times can do the Wrong Thing<sup>TM</sup>. For the
     * situations where this logic is not enough, you can implement the
     * getContainsRowQueryString method in FreeformQueryDelegate and this will
     * be used instead of the logic.
     * 
     * @see com.vaadin.addon.sqlcontainer.query.FreeformQueryDelegate#getContainsRowQueryString(Object...)
     * 
     *      {@inheritDoc}
     */
    public boolean containsRowWithKey(Object... keys) throws SQLException {
        String query = null;
        if (delegate != null) {
            try {
                query = delegate.getContainsRowQueryString(keys);
            } catch (UnsupportedOperationException e) {
                query = modifyWhereClause(keys);
            }
        } else {
            query = modifyWhereClause(keys);
        }

        boolean contains = false;
        Connection conn = getConnection();
        try {
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(query);
            contains = rs.next();
            rs.close();
            statement.close();
        } finally {
            releaseConnection(conn);
        }
        return contains;
    }

    /**
     * Releases the connection if it is not part of an active transaction.
     * 
     * @param conn
     *            the connection to release
     */
    private void releaseConnection(Connection conn) {
        if (conn != activeConnection) {
            connectionPool.releaseConnection(conn);
        }
    }

    private String modifyWhereClause(Object... keys) {
        // Build the where rules for the provided keys
        StringBuffer where = new StringBuffer();
        for (int ix = 0; ix < primaryKeyColumns.size(); ix++) {
            where.append("\"" + primaryKeyColumns.get(ix) + "\"").append("=");
            if (keys[ix] == null) {
                where.append("null");
            } else {
                where.append("'").append(keys[ix]).append("'");
            }
            if (ix < primaryKeyColumns.size() - 1) {
                where.append(" AND ");
            }
        }
        // Is there already a WHERE clause in the query string?
        int index = queryString.toLowerCase().indexOf("where ");
        if (index > -1) {
            // Rewrite the where clause
            return queryString.substring(0, index) + "WHERE " + where + " AND "
                    + queryString.substring(index + 6);
        }
        // Append a where clause
        return queryString + " WHERE " + where;
    }

}
