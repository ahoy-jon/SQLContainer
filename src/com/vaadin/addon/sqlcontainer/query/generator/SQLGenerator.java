package com.vaadin.addon.sqlcontainer.query.generator;

import java.util.List;

import com.vaadin.addon.sqlcontainer.RowItem;
import com.vaadin.addon.sqlcontainer.query.Filter;
import com.vaadin.addon.sqlcontainer.query.FilteringMode;
import com.vaadin.addon.sqlcontainer.query.OrderBy;

/**
 * The SQLGenerator interface is meant to be implemented for each different SQL
 * syntax that is to be supported. By default there are implementations for
 * HSQLDB, MySQL, PostgreSQL, MSSQL and Oracle syntaxes.
 * 
 * @author Jonatan Kronqvist / IT Mill Ltd
 */
public interface SQLGenerator {
    /**
     * Generates a SELECT query with the provided parameters. Uses default
     * filtering mode (INCLUSIVE).
     * 
     * @param tableName
     *            Name of the table queried
     * @param filters
     *            The filters, converted into a WHERE clause
     * @param orderBys
     *            The the ordering conditions, converted into an ORDER BY clause
     * @param offset
     *            The offset of the first row to be included
     * @param pagelength
     *            The number of rows to be returned when the query executes
     * @param toSelect
     *            String containing what to select, e.g. "*", "COUNT(*)"
     * @return a string with the SQL query that will return the wanted results.
     */
    public String generateSelectQuery(String tableName, List<Filter> filters,
            List<OrderBy> orderBys, int offset, int pagelength, String toSelect);

    /**
     * Generates a SELECT query with the provided parameters.
     * 
     * @param tableName
     *            Name of the table queried
     * @param filters
     *            The filters, converted into a WHERE clause
     * @param filterMode
     *            Sets the filtering mode for this query. See FilteringMode.
     * @param orderBys
     *            The the ordering conditions, converted into an ORDER BY clause
     * @param offset
     *            The offset of the first row to be included
     * @param pagelength
     *            The number of rows to be returned when the query executes
     * @param toSelect
     *            String containing what to select, e.g. "*", "COUNT(*)"
     * @return a string with the SQL query that will return the wanted results.
     */
    public String generateSelectQuery(String tableName, List<Filter> filters,
            FilteringMode filterMode, List<OrderBy> orderBys, int offset,
            int pagelength, String toSelect);

    /**
     * Generates an UPDATE query with the provided parameters.
     * 
     * @param tableName
     *            Name of the table queried
     * @param item
     *            RowItem containing the updated values update.
     * @return a string with the SQL query that will update a specific row's
     *         values.
     */
    public String generateUpdateQuery(String tableName, RowItem item);

    /**
     * Generates an INSERT query for inserting a new row with the provided
     * values.
     * 
     * @param tableName
     *            Name of the table queried
     * @param item
     *            New RowItem to be inserted into the database.
     * @return a string with the SQL query that will insert a new row with the
     *         given values.
     */
    public String generateInsertQuery(String tableName, RowItem item);

    /**
     * Generates a DELETE query for deleting data related to the given RowItem
     * from the database.
     * 
     * @param tableName
     *            Name of the table queried
     * @param item
     *            Item to be deleted from the database
     * @return a string with the SQL query that will delete the given row.
     */
    public String generateDeleteQuery(String tableName, RowItem item);
}
