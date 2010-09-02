package com.vaadin.addon.sqlcontainer.query;

import java.sql.SQLException;
import java.util.List;

import com.vaadin.addon.sqlcontainer.RowItem;

public interface FreeformQueryDelegate {
    /**
     * Should return the SQL query string to be performed. This method is
     * responsible for gluing together the select query from the filters and the
     * order by conditions if these are supported.
     * 
     * @param offset
     *            the first record (row) to fetch.
     * @param pagelength
     *            the number of records (rows) to fetch. 0 means all records
     *            starting from offset.
     */
    public String getQueryString(int offset, int limit)
            throws UnsupportedOperationException;

    /**
     * Generates and executes a query to determine the current row count from
     * the DB. Row count will be fetched using filters that are currently set to
     * the QueryDelegate.
     * 
     * @return row count
     * @throws SQLException
     */
    public String getCountQuery() throws UnsupportedOperationException;

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
     * @param row
     *            RowItem to be stored or updated.
     * @throws UnsupportedOperationException
     *             if the implementation is read only.
     */
    public int storeRow(RowItem row) throws UnsupportedOperationException;

    /**
     * Removes the given RowItem from the database.
     * 
     * @param row
     *            RowItem to be removed
     * @return true on success
     * @throws UnsupportedOperationException
     * @throws SQLException
     */
    public boolean removeRow(RowItem row) throws UnsupportedOperationException,
            SQLException;

    /**
     * Generates an SQL Query string that allows the user of the FreeformQuery
     * class to customize the query string used by the
     * FreeformQuery.containsRowWithKeys() method. This is useful for cases when
     * the logic in the containsRowWithKeys method is not enough to support more
     * complex free form queries.
     * 
     * @param keys
     *            the values of the primary keys
     * @throws UnsupportedOperationException
     *             to use the default logic in FreeformQuery
     */
    public String getContainsRowQueryString(Object... keys)
            throws UnsupportedOperationException;
}
