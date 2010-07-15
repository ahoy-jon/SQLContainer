package com.vaadin.addon.sqlcontainer.query;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

public class FreeformQuery implements QueryDelegate {

    FreeformQueryDelegate delegate = null;
    private String queryString;

    public FreeformQuery(String queryString) {
        setQueryString(queryString);
    }

    public ResultSet getResults(int offset, int pagelength) {
        // TODO Auto-generated method stub
        return null;
    }

    public void setFilters(List<Filter> filters)
            throws UnsupportedOperationException {
        if (delegate != null) {
            delegate.setFilters(filters);
        }
    }

    public void setOrderBy(List<OrderBy> orderBys)
            throws UnsupportedOperationException {
        if (delegate != null) {
            delegate.setOrderBy(orderBys);
        }
    }

    public void storeRow(Map<String, String> columnToValueMap)
            throws UnsupportedOperationException {
        if (delegate != null) {
            delegate.storeRow(columnToValueMap);
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

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public String getQueryString() {
        return queryString;
    }

}
