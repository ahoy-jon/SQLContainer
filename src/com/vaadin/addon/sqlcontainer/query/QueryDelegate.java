package com.vaadin.addon.sqlcontainer.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.vaadin.addon.sqlcontainer.RowItem;

public interface QueryDelegate {
    /**
     * Generates and executes a query to determine the current row count from
     * the DB. Row count will be fetched using filters that are currently set to
     * the QueryDelegate.
     * 
     * @return row count
     * @throws SQLException
     */
    public int getCount() throws SQLException;

    /**
     * Executes a paged SQL query and returns the ResultSet. The query is
     * defined through implementations of this QueryDelegate interface.
     * 
     * @param offset
     *            the first item of the page to load
     * @param pagelength
     *            the length of the page to load
     * @return a ResultSet containing the rows of the page
     * @throws SQLException
     *             if the database access fails.
     */
    public ResultSet getResults(int offset, int pagelength) throws SQLException;

    /**
     * Allows the SQLContainer implementation to check whether the QueryDelegate
     * implementation implements paging in the getResults method.
     * 
     * @see QueryDelegate#getResults(int, int)
     * 
     * @return true if the delegate implements paging
     */
    public boolean implementationRespectsPagingLimits();

    /**
     * Sets the filters to apply when performing the SQL query. These are
     * translated into a WHERE clause. Default filtering mode will be used.
     * 
     * @param filters
     *            The filters to apply.
     * @throws UnsupportedOperationException
     *             if the implementation doesn't support filtering.
     */
    public void setFilters(List<Filter> filters)
            throws UnsupportedOperationException;

    /**
     * Sets the filters to apply when performing the SQL query. These are
     * translated into a WHERE clause.
     * 
     * @param filters
     *            The filters to apply.
     * @param filteringMode
     *            Filtering mode.
     *            <code>FilteringMode.FILTERING_MODE_INCLUSIVE</code> =
     *            Inclusive (AND).
     *            <code>FilteringMode.FILTERING_MODE_EXCLUSIVE</code> =
     *            Exclusive (OR)
     * @throws UnsupportedOperationException
     *             if the implementation doesn't support filtering.
     */
    public void setFilters(List<Filter> filters, FilteringMode filteringMode)
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
    public int storeRow(RowItem row) throws UnsupportedOperationException,
            SQLException;

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
     * Starts a new database transaction. Used when storing multiple changes.
     * 
     * Note that if a transaction is already open, it will be rolled back when a
     * new transaction is started.
     * 
     * @throws SQLException
     *             if the database access fails.
     */
    public void beginTransaction() throws SQLException;

    /**
     * Commits a transaction. If a transaction is not open nothing should
     * happen.
     * 
     * @throws SQLException
     *             if the database access fails.
     */
    public void commit() throws SQLException;

    /**
     * Rolls a transaction back. If a transaction is not open nothing should
     * happen.
     * 
     * @throws SQLException
     *             if the database access fails.
     */
    public void rollback() throws SQLException;

    /**
     * Returns a list of primary key column names. The list is either fetched
     * from the database (TableQuery) or given as an argument depending on
     * implementation.
     * 
     * @return
     */
    public List<String> getPrimaryKeyColumns();

    /**
     * Performs a query to find out whether the SQL table contains a row with
     * the given set of primary keys.
     * 
     * @param keys
     *            the primary keys
     * @return true if the SQL table contains a row with the provided keys
     * @throws SQLException
     */
    public boolean containsRowWithKey(Object... keys) throws SQLException;
}
