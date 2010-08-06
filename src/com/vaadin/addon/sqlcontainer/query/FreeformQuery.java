package com.vaadin.addon.sqlcontainer.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import com.vaadin.addon.sqlcontainer.RowItem;
import com.vaadin.addon.sqlcontainer.connection.JDBCConnectionPool;

public class FreeformQuery implements QueryDelegate {

    FreeformQueryDelegate delegate = null;
    private String queryString;
    private List<String> primaryKeyColumns;
    private JDBCConnectionPool connectionPool;

    /**
     * Prevent no-parameters instantiation of FreeformQuery
     */
    private FreeformQuery() {
    }

    public FreeformQuery(String queryString, List<String> primaryKeyColumns,
            JDBCConnectionPool connectionPool) {
        this.queryString = queryString;
        this.primaryKeyColumns = Collections
                .unmodifiableList(primaryKeyColumns);
        this.connectionPool = connectionPool;
    }

    public int getCount() throws SQLException {
        // TODO Auto-generated method stub
        return 0;
    }

    public ResultSet getIdList() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    public ResultSet getResults(int offset, int pagelength) {
        // TODO Auto-generated method stub
        return null;
    }

    public void setFilters(List<Filter> filters)
            throws UnsupportedOperationException {
        if (delegate != null) {
            delegate.setFilters(filters);
        } else {
            throw new UnsupportedOperationException(
                    "FreeFormQueryDelegate not set!");
        }
    }

    public void setOrderBy(List<OrderBy> orderBys)
            throws UnsupportedOperationException {
        if (delegate != null) {
            delegate.setOrderBy(orderBys);
        } else {
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

    public void beginTransaction() throws UnsupportedOperationException {
        // TODO Auto-generated method stub
    }

    public void commit() throws UnsupportedOperationException {
        // TODO Auto-generated method stub
    }

    public void rollback() throws UnsupportedOperationException {
        // TODO Auto-generated method stub
    }

    public List<String> getPrimaryKeyColumns() {
        // TODO Auto-generated method stub
        return null;
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

    public boolean removeRow(RowItem row) throws UnsupportedOperationException,
            SQLException {
        // TODO Auto-generated method stub
        return false;
    }

}
