package com.vaadin.addon.sqlcontainer.query.generator;

import java.util.List;
import java.util.Map;

import com.vaadin.addon.sqlcontainer.query.Filter;
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
     * Generates a SELECT query with the provided parameters.
     * 
     * @param filters
     *            The filters, converted into a WHERE clause
     * @param orderBys
     *            The the ordering conditions, converted into an ORDER BY clause
     * @param offset
     *            The offset of the first row to be included
     * @param pagelength
     *            The number of rows to be returned when the query executes
     * @return a string with the SQL query that will return the wanted results.
     */
    public String generateSelectQuery(List<Filter> filters,
            List<OrderBy> orderBys, int offset, int pagelength);

    /**
     * Generates an UPDATE query with the provided parameters.
     * 
     * @param columnToValueMap
     *            The columns to update along with their (new) values.
     * @param rowIdentifiers
     *            the columns and values that make up the primary key for the
     *            row to be updated.
     * @return a string with the SQL query that will update a specific row's
     *         values.
     */
    public String generateUpdateQuery(Map<String, String> columnToValueMap,
            Map<String, String> rowIdentifiers);

    /**
     * Generates an INSERT query for inserting a new row with the provided
     * values.
     * 
     * @param columnToValueMap
     *            The columns of the row along with their values.
     * @return a string with the SQL query that will insert a new row with the
     *         given values.
     */
    public String generateInsertQuery(Map<String, String> columnToValueMap);
}
