package com.vaadin.addon.sqlcontainer.query;

import java.util.List;
import java.util.Map;

public interface FreeformQueryDelegate {

    /**
     * Should return the SQL query string to be performed. This method is
     * responsible for gluing together the select query from the filters and the
     * order by conditions if these are supported.
     */
    public void getQueryString();

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
}
