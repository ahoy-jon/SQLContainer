package com.vaadin.addon.sqlcontainer.query;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.vaadin.addon.sqlcontainer.RowItem;
import com.vaadin.addon.sqlcontainer.connection.JDBCConnectionPool;
import com.vaadin.addon.sqlcontainer.query.generator.SQLGenerator;

/**
 * 
 *
 */
public class TableQuery implements QueryDelegate {

    private String tableName;
    private List<String> primaryKeyColumns;
    private String versionColumn;

    private List<Filter> filters;
    private List<OrderBy> orderBys;

    private SQLGenerator sqlGenerator;

    private JDBCConnectionPool connectionPool;

    private Connection activeConnection;
    private boolean transactionOpen;

    /**
     * Prevent no-parameters instantiation of TableQuery
     */
    private TableQuery() {
    }

    public TableQuery(String tableName, JDBCConnectionPool connectionPool,
            SQLGenerator sqlGenerator) {
        if (tableName == null || connectionPool == null || sqlGenerator == null) {
            throw new IllegalArgumentException(
                    "All parameters must be non-null.");
        }
        this.tableName = tableName;
        this.sqlGenerator = sqlGenerator;
        this.connectionPool = connectionPool;
        fetchMetaData();
    }

    public int getCount() throws SQLException {
        String query = sqlGenerator.generateSelectQuery(tableName, filters,
                orderBys, 0, 0, "COUNT(*)");
        ResultSet r = executeQuery(query);
        r.next();
        return r.getInt(1);
    }

    public ResultSet getIdList() throws SQLException {
        StringBuffer keys = new StringBuffer();
        for (String colName : primaryKeyColumns) {
            keys.append(colName);
            if (primaryKeyColumns.indexOf(colName) < primaryKeyColumns.size() - 1) {
                keys.append(", ");
            }
        }
        String query = sqlGenerator.generateSelectQuery(tableName, null, null,
                0, 0, keys.toString());
        return executeQuery(query);
    }

    public ResultSet getResults(int offset, int pagelength) throws SQLException {
        String query = sqlGenerator.generateSelectQuery(tableName, filters,
                orderBys, offset, pagelength, null);
        return executeQuery(query);
    }

    public int storeRow(RowItem row) throws UnsupportedOperationException,
            SQLException {
        if (versionColumn == null || versionColumn.trim().equals("")) {
            throw new IllegalStateException(
                    "Version column must be set prior to writing.");
        }
        if (row == null) {
            throw new IllegalArgumentException("Row argument must be non-null.");
        }
        String query = null;
        // TODO: Generate UPDATE or INSERT query
        return executeUpdate(query);
    }

    public void setFilters(List<Filter> filters)
            throws UnsupportedOperationException {
        this.filters = filters;
    }

    public void setOrderBy(List<OrderBy> orderBys)
            throws UnsupportedOperationException {
        this.orderBys = orderBys;
    }

    public void beginTransaction() throws UnsupportedOperationException,
            SQLException {
        if (activeConnection != null) {
            connectionPool.releaseConnection(activeConnection);
        }
        System.err.println("DB -> begin transaction");
        activeConnection = connectionPool.reserveConnection();
        activeConnection.setAutoCommit(false);
        transactionOpen = true;
    }

    public void commit() throws UnsupportedOperationException, SQLException {
        if (transactionOpen && activeConnection != null
                && !activeConnection.getAutoCommit()) {
            System.err.println("DB -> commit");
            activeConnection.commit();
            connectionPool.releaseConnection(activeConnection);
        }
        transactionOpen = false;
    }

    public void rollback() throws UnsupportedOperationException, SQLException {
        if (transactionOpen && activeConnection != null
                && !activeConnection.getAutoCommit()) {
            System.err.println("DB -> rollback");
            activeConnection.rollback();
            connectionPool.releaseConnection(activeConnection);
        }
        transactionOpen = false;
    }

    public List<String> getPrimaryKeyColumns() {
        return primaryKeyColumns;
    }

    /*
     * public void setPrimaryKeyColumns(List<String> columns) throws
     * UnsupportedOperationException { throw new
     * UnsupportedOperationException(); }
     */

    public String getVersionColumn() {
        return versionColumn;
    }

    public void setVersionColumn(String versionColumn) {
        this.versionColumn = versionColumn;
    }

    public String getTableName() {
        return tableName;
    }

    public SQLGenerator getSqlGenerator() {
        return sqlGenerator;
    }

    private ResultSet executeQuery(String query) throws SQLException {
        Connection c = null;
        try {
            if (transactionOpen && activeConnection != null
                    && !activeConnection.getAutoCommit()) {
                c = activeConnection;
            } else {
                c = connectionPool.reserveConnection();
            }
            Statement statement = c.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            System.err.println("DB -> " + query);
            return statement.executeQuery(query);
        } finally {
            if (!transactionOpen) {
                connectionPool.releaseConnection(c);
            }
        }
    }

    private int executeUpdate(String query) throws SQLException {
        Connection c = null;
        try {
            if (transactionOpen && activeConnection != null
                    && !activeConnection.getAutoCommit()) {
                c = activeConnection;
            } else {
                c = connectionPool.reserveConnection();
            }
            Statement statement = c.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            System.err.println("DB -> " + query);
            return statement.executeUpdate(query);
        } finally {
            if (!transactionOpen) {
                connectionPool.releaseConnection(c);
            }
        }
    }

    /**
     * Fetches name(s) of primary key column(s) from DB metadata. Also gets the
     * escape string to be used in search strings.
     */
    private void fetchMetaData() {
        Connection c = null;
        try {
            c = connectionPool.reserveConnection();
            DatabaseMetaData dbmd = c.getMetaData();
            getSqlGenerator().setSearchStringEscape(
                    dbmd.getSearchStringEscape());
            if (dbmd != null) {
                ResultSet rs = dbmd.getPrimaryKeys(null, null, tableName);
                if (rs != null) {
                    ResultSetMetaData rsmd = rs.getMetaData();
                    if (rsmd != null) {
                        List<String> names = new ArrayList<String>();
                        while (rs.next()) {
                            names.add(rs.getString("COLUMN_NAME"));
                        }
                        if (!names.isEmpty()) {
                            primaryKeyColumns = names;
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            connectionPool.releaseConnection(c);
        }
    }

    public boolean removeRow(RowItem row) throws UnsupportedOperationException,
            SQLException {
        System.err.println("Removing row with id: "
                + row.getId().getId()[0].toString());
        if (executeUpdate(sqlGenerator.generateDeleteQuery(getTableName(), row)) == 1) {
            return true;
        }
        return false;
    }
}
