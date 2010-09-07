package com.vaadin.addon.sqlcontainer.query;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.vaadin.addon.sqlcontainer.ColumnProperty;
import com.vaadin.addon.sqlcontainer.RowItem;
import com.vaadin.addon.sqlcontainer.TemporaryRowId;
import com.vaadin.addon.sqlcontainer.connection.JDBCConnectionPool;
import com.vaadin.addon.sqlcontainer.query.Filter.ComparisonType;
import com.vaadin.addon.sqlcontainer.query.generator.DefaultSQLGenerator;
import com.vaadin.addon.sqlcontainer.query.generator.SQLGenerator;

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

    private boolean debug = false;

    /**
     * Prevent no-parameters instantiation of TableQuery
     */
    @SuppressWarnings("unused")
    private TableQuery() {
    }

    public TableQuery(String tableName, JDBCConnectionPool connectionPool,
            SQLGenerator sqlGenerator) {
        if (tableName == null || tableName.trim().length() < 1
                || connectionPool == null || sqlGenerator == null) {
            throw new IllegalArgumentException(
                    "All parameters must be non-null and a table name must be given.");
        }
        this.tableName = tableName;
        this.sqlGenerator = sqlGenerator;
        this.connectionPool = connectionPool;
        fetchMetaData();
    }

    public TableQuery(String tableName, JDBCConnectionPool connectionPool) {
        this(tableName, connectionPool, new DefaultSQLGenerator());
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

    public boolean implementationRespectsPagingLimits() {
        return true;
    }

    public int storeRow(RowItem row) throws UnsupportedOperationException,
            SQLException {
        if (row == null) {
            throw new IllegalArgumentException("Row argument must be non-null.");
        }
        String query = null;
        if (row.getId() instanceof TemporaryRowId) {
            query = sqlGenerator.generateInsertQuery(tableName, row);
        } else {
            try {
                ((ColumnProperty) row.getItemProperty(versionColumn))
                        .setVersionColumn(true);
            } catch (Exception e) {
                throw new IllegalStateException(
                        "Version column not set or does not exist.", e);
            }
            query = sqlGenerator.generateUpdateQuery(tableName, row);
        }
        return executeUpdate(query);
    }

    public void setFilters(List<Filter> filters)
            throws UnsupportedOperationException {
        if (filters == null) {
            this.filters = null;
            return;
        }
        this.filters = Collections.unmodifiableList(filters);
    }

    public void setOrderBy(List<OrderBy> orderBys)
            throws UnsupportedOperationException {
        if (orderBys == null) {
            this.orderBys = null;
            return;
        }
        this.orderBys = Collections.unmodifiableList(orderBys);
    }

    public void beginTransaction() throws UnsupportedOperationException,
            SQLException {
        if (transactionOpen && activeConnection != null) {
            throw new IllegalStateException();
        }
        debug("DB -> begin transaction");
        activeConnection = connectionPool.reserveConnection();
        activeConnection.setAutoCommit(false);
        transactionOpen = true;
    }

    public void commit() throws UnsupportedOperationException, SQLException {
        if (transactionOpen && activeConnection != null) {
            debug("DB -> commit");
            activeConnection.commit();
            connectionPool.releaseConnection(activeConnection);
        } else {
            throw new SQLException("No active transaction");
        }
        transactionOpen = false;
    }

    public void rollback() throws UnsupportedOperationException, SQLException {
        if (transactionOpen && activeConnection != null) {
            debug("DB -> rollback");
            activeConnection.rollback();
            connectionPool.releaseConnection(activeConnection);
        } else {
            throw new SQLException("No active transaction");
        }
        transactionOpen = false;
    }

    public List<String> getPrimaryKeyColumns() {
        return Collections.unmodifiableList(primaryKeyColumns);
    }

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
            if (transactionOpen && activeConnection != null) {
                c = activeConnection;
            } else {
                c = connectionPool.reserveConnection();
            }
            Statement statement = c.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            debug("DB -> " + query);
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
            if (transactionOpen && activeConnection != null) {
                c = activeConnection;
            } else {
                c = connectionPool.reserveConnection();
            }
            Statement statement = c.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            debug("DB -> " + query);
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
                ResultSet tables = dbmd.getTables(null, null, tableName, null);
                if (!tables.next()) {
                    throw new IllegalArgumentException("Table with the name \""
                            + tableName
                            + "\" was not found. Check your database contents.");
                }
                ResultSet rs = dbmd.getPrimaryKeys(null, null, tableName);
                List<String> names = new ArrayList<String>();
                while (rs.next()) {
                    names.add(rs.getString("COLUMN_NAME"));
                }
                if (!names.isEmpty()) {
                    primaryKeyColumns = names;
                }
                if (primaryKeyColumns == null || primaryKeyColumns.isEmpty()) {
                    throw new IllegalArgumentException(
                            "Primary key constraints have not been defined for the table \""
                                    + tableName
                                    + "\". Use FreeFormQuery to access this table.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            connectionPool.releaseConnection(c);
        }
    }

    private void debug(String message) {
        if (debug) {
            System.out.println(message);
        }
    }

    public boolean removeRow(RowItem row) throws UnsupportedOperationException,
            SQLException {
        debug("Removing row with id: " + row.getId().getId()[0].toString());
        if (executeUpdate(sqlGenerator.generateDeleteQuery(getTableName(), row)) == 1) {
            return true;
        }
        return false;
    }

    public boolean containsRowWithKey(Object... keys) throws SQLException {
        ArrayList<Filter> filtersAndKeys = new ArrayList<Filter>();
        if (filters != null) {
            filtersAndKeys.addAll(filters);
        }
        int ix = 0;
        for (String colName : primaryKeyColumns) {
            filtersAndKeys.add(new Filter(colName, ComparisonType.EQUALS,
                    String.valueOf(keys[ix])));
            ix++;
        }
        String query = sqlGenerator.generateSelectQuery(tableName,
                filtersAndKeys, orderBys, 0, 0, "*");
        ResultSet rs = executeQuery(query);
        boolean contains = rs.next();
        rs.close();
        return contains;
    }
}
