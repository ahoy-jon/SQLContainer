package com.vaadin.addon.sqlcontainer.query;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

public interface QueryDelegate {

    /**
     * Executes a paged SQL query and returns the ResultSet. The query is
     * defined through implementations of this QueryDelegate interface.
     * 
     * @param offset
     *            the first item of the page to load
     * @param pagelength
     *            the length of the page to load
     * @return a ResultSet containing the rows of the page
     */
    public ResultSet getResults(int offset, int pagelength);

    /**
     * Sets the filters to apply when performing the SQL query. These are
     * translated into a WHERE clause.
     * 
     * @param filters
     *            The filters to apply.
     * 
     * @throws UnsupportedOperationException
     *             if the implementation doesn't support filtering.
     */
    public void setFilters(List<Filter> filters)
            throws UnsupportedOperationException;

    /**
     * Sets the order in which to retrieve rows from the database. The result
     * can be ordered by zero or more columns and each column can be in
     * ascending or descending order. These are translated into an ORDER BY
     * clause in the SQL query.
     * 
     * @param orderBys
     *            A list of the OrderBy conditions.
     * @throws UnsupportedOperationException
     *             if the implementation doesn't support ordering.
     */
    public void setOrderBy(List<OrderBy> orderBys)
            throws UnsupportedOperationException;

    /**
     * Stores a row in the database. The implementation of this interface
     * decides how to identify whether to store a new row or update an existing
     * one.
     * 
     * @param columnToValueMap
     *            A map containing the values for all columns to be stored or
     *            updated.
     * @throws UnsupportedOperationException
     *             if the implementation is read only.
     */
    public void storeRow(Map<String, String> columnToValueMap)
            throws UnsupportedOperationException;

    /**
     * Starts a new database transaction. Used when storing multiple changes.
     * 
     * @throws UnsupportedOperationException
     *             if the implementation is read only.
     */
    public void beginTransaction() throws UnsupportedOperationException;

    /**
     * Commits a transaction.
     * 
     * @throws UnsupportedOperationException
     *             if the implementation is read only.
     */
    public void commit() throws UnsupportedOperationException;

    /**
     * Rolls a transaction back.
     * 
     * @throws UnsupportedOperationException
     *             if the implementation is read only.
     */
    public void rollback() throws UnsupportedOperationException;

}
