package com.vaadin.addon.sqlcontainer.query.generator;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.addon.sqlcontainer.ColumnProperty;
import com.vaadin.addon.sqlcontainer.RowItem;
import com.vaadin.addon.sqlcontainer.TemporaryRowId;
import com.vaadin.addon.sqlcontainer.Util;
import com.vaadin.addon.sqlcontainer.query.Filter;
import com.vaadin.addon.sqlcontainer.query.FilteringMode;
import com.vaadin.addon.sqlcontainer.query.OrderBy;

/**
 * Generates generic SQL that is supported by HSQLDB, MySQL and PostgreSQL.
 * 
 * @author Jonatan Kronqvist / IT Mill Ltd
 */
@SuppressWarnings("serial")
public class DefaultSQLGenerator implements SQLGenerator {

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.addon.sqlcontainer.query.generator.SQLGenerator#
     * generateSelectQuery(java.lang.String, java.util.List,
     * com.vaadin.addon.sqlcontainer.query.FilteringMode, java.util.List, int,
     * int, java.lang.String)
     */
    public StatementHelper generateSelectQuery(String tableName,
            List<Filter> filters, FilteringMode filterMode,
            List<OrderBy> orderBys, int offset, int pagelength, String toSelect) {
        if (tableName == null || tableName.trim().equals("")) {
            throw new IllegalArgumentException("Table name must be given.");
        }
        StatementHelper sh = new StatementHelper();
        StringBuffer query = new StringBuffer();
        if (toSelect == null) {
            query.append("SELECT * FROM ").append(Util.escapeSQL(tableName));
        } else {
            query.append("SELECT " + toSelect + " FROM ").append(
                    Util.escapeSQL(tableName));
        }

        if (filters != null) {
            for (Filter f : filters) {
                generateFilter(query, f, filters.indexOf(f) == 0, filterMode,
                        sh);
            }
        }
        if (orderBys != null) {
            for (OrderBy o : orderBys) {
                generateOrderBy(query, o, orderBys.indexOf(o) == 0);
            }
        }
        if (pagelength != 0) {
            generateLimits(query, offset, pagelength);
        }
        sh.setQueryString(query.toString());
        return sh;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.addon.sqlcontainer.query.generator.SQLGenerator#
     * generateSelectQuery(java.lang.String, java.util.List, java.util.List,
     * int, int, java.lang.String)
     */
    public StatementHelper generateSelectQuery(String tableName,
            List<Filter> filters, List<OrderBy> orderBys, int offset,
            int pagelength, String toSelect) {
        return generateSelectQuery(tableName, filters,
                FilteringMode.FILTERING_MODE_INCLUSIVE, orderBys, offset,
                pagelength, toSelect);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.addon.sqlcontainer.query.generator.SQLGenerator#
     * generateUpdateQuery(java.lang.String,
     * com.vaadin.addon.sqlcontainer.RowItem)
     */
    public StatementHelper generateUpdateQuery(String tableName, RowItem item) {
        if (tableName == null || tableName.trim().equals("")) {
            throw new IllegalArgumentException("Table name must be given.");
        }
        if (item == null) {
            throw new IllegalArgumentException("Updated item must be given.");
        }
        StatementHelper sh = new StatementHelper();
        StringBuffer query = new StringBuffer();
        query.append("UPDATE ").append(tableName).append(" SET");

        /* Generate column<->value map */
        Map<String, Object> columnToValueMap = new HashMap<String, Object>();
        Map<String, Object> rowIdentifiers = new HashMap<String, Object>();
        for (Object id : item.getItemPropertyIds()) {
            ColumnProperty cp = (ColumnProperty) item.getItemProperty(id);
            /* Prevent "rownum" usage as a column name if MSSQL or ORACLE */
            if ((this instanceof MSSQLGenerator || this instanceof OracleGenerator)
                    && cp.getPropertyId().equalsIgnoreCase("rownum")) {
                continue;
            }
            Object value = cp.getValue() == null ? null : cp.getValue();
            /*
             * Only include properties whose read-only status can be altered,
             * and which are not set as version columns. The rest of the columns
             * are used as identifiers.
             */
            if (cp.isReadOnlyChangeAllowed() && !cp.isVersionColumn()) {
                columnToValueMap.put(cp.getPropertyId(), value);
            } else {
                rowIdentifiers.put(cp.getPropertyId(), value);
            }
        }

        /* Generate columns and values to update */
        boolean first = true;
        for (String column : columnToValueMap.keySet()) {
            if (first) {
                query.append(" \"" + column + "\" = ?");
            } else {
                query.append(", \"" + column + "\" = ?");
            }
            sh.addParameterValue(columnToValueMap.get(column), item
                    .getItemProperty(column).getType());
            first = false;
        }

        /* Generate identifiers for the row to be updated */
        first = true;
        for (String column : rowIdentifiers.keySet()) {
            if (first) {
                query.append(" WHERE \"" + column + "\" = ?");
            } else {
                query.append(" AND \"" + column + "\" = ?");
            }
            sh.addParameterValue(rowIdentifiers.get(column), item
                    .getItemProperty(column).getType());
            first = false;
        }
        sh.setQueryString(query.toString());
        return sh;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.addon.sqlcontainer.query.generator.SQLGenerator#
     * generateInsertQuery(java.lang.String,
     * com.vaadin.addon.sqlcontainer.RowItem)
     */
    public StatementHelper generateInsertQuery(String tableName, RowItem item) {
        if (tableName == null || tableName.trim().equals("")) {
            throw new IllegalArgumentException("Table name must be given.");
        }
        if (item == null) {
            throw new IllegalArgumentException("New item must be given.");
        }
        if (!(item.getId() instanceof TemporaryRowId)) {
            throw new IllegalArgumentException(
                    "Cannot generate an insert query for item already in database.");
        }
        StatementHelper sh = new StatementHelper();
        StringBuffer query = new StringBuffer();
        query.append("INSERT INTO ").append(tableName).append(" (");

        /* Generate column<->value map */
        Map<String, Object> columnToValueMap = new HashMap<String, Object>();
        for (Object id : item.getItemPropertyIds()) {
            ColumnProperty cp = (ColumnProperty) item.getItemProperty(id);
            /* Prevent "rownum" usage as a column name if MSSQL or ORACLE */
            if ((this instanceof MSSQLGenerator || this instanceof OracleGenerator)
                    && cp.getPropertyId().equalsIgnoreCase("rownum")) {
                continue;
            }
            Object value = cp.getValue() == null ? null : cp.getValue();
            /* Only include properties whose read-only status can be altered */
            if (cp.isReadOnlyChangeAllowed() && !cp.isVersionColumn()) {
                columnToValueMap.put(cp.getPropertyId(), value);
            }
        }

        /* Generate column names for insert query */
        boolean first = true;
        for (String column : columnToValueMap.keySet()) {
            if (!first) {
                query.append(", ");
            }
            query.append("\"" + column + "\"");
            first = false;
        }

        /* Generate values for insert query */
        query.append(") VALUES (");
        first = true;
        for (String column : columnToValueMap.keySet()) {
            if (!first) {
                query.append(", ");
            }
            query.append("?");
            sh.addParameterValue(columnToValueMap.get(column), item
                    .getItemProperty(column).getType());
            first = false;
        }
        query.append(")");
        sh.setQueryString(query.toString());
        return sh;
    }

    /**
     * Creates filtering as a WHERE -clause. Uses default filtering mode.
     * 
     * @param sb
     *            StringBuffer to which the clause is appended.
     * @param f
     *            Filter to be added to the sb.
     * @param firstFilter
     *            If true, this is the first Filter to be added.
     * @return
     */
    protected StringBuffer generateFilter(StringBuffer sb, Filter f,
            boolean firstFilter, StatementHelper sh) {
        return generateFilter(sb, f, firstFilter,
                FilteringMode.FILTERING_MODE_INCLUSIVE, sh);
    }

    /**
     * Creates filtering as a WHERE -clause
     * 
     * @param sb
     *            StringBuffer to which the clause is appended.
     * @param f
     *            Filter to be added to the sb.
     * @param firstFilter
     *            If true, this is the first Filter to be added.
     * @param filterMode
     *            FilteringMode for this set of filters.
     * @return
     */
    protected StringBuffer generateFilter(StringBuffer sb, Filter f,
            boolean firstFilter, FilteringMode filterMode, StatementHelper sh) {
        if (f.getValue() == null) {
            return sb;
        }
        if (Filter.ComparisonType.BETWEEN.equals(f.getComparisonType())) {
            if (f.getValue() != null && f.getSecondValue() != null) {
                sh.addParameterValue(f.getValue());
                sh.addParameterValue(f.getSecondValue());
            } else {
                return sb;
            }
        } else {
            if (f.getValue() != null) {
                sh.addParameterValue(f.getPreparedStatementValue());
            } else {
                return sb;
            }
        }
        if (firstFilter) {
            sb.append(" WHERE ");
        } else {
            if (FilteringMode.FILTERING_MODE_INCLUSIVE.equals(filterMode)) {
                sb.append(" AND ");
            } else if (FilteringMode.FILTERING_MODE_EXCLUSIVE
                    .equals(filterMode)) {
                sb.append(" OR ");
            }
        }
        sb.append(f.toPreparedStatementString());
        return sb;
    }

    /**
     * Generates sorting rules as an ORDER BY -clause
     * 
     * @param sb
     *            StringBuffer to which the clause is appended.
     * @param o
     *            OrderBy object to be added into the sb.
     * @param firstOrderBy
     *            If true, this is the first OrderBy.
     * @return
     */
    protected StringBuffer generateOrderBy(StringBuffer sb, OrderBy o,
            boolean firstOrderBy) {
        if (firstOrderBy) {
            sb.append(" ORDER BY ");
        } else {
            sb.append(", ");
        }
        sb.append("\"" + o.getColumn() + "\"");
        if (o.isAscending()) {
            sb.append(" ASC");
        } else {
            sb.append(" DESC");
        }
        return sb;
    }

    /**
     * Generates the LIMIT and OFFSET clause.
     * 
     * @param sb
     *            StringBuffer to which the clause is appended.
     * @param offset
     *            Value for offset.
     * @param pagelength
     *            Value for pagelength.
     * @return StringBuffer with LIMIT and OFFSET clause added.
     */
    protected StringBuffer generateLimits(StringBuffer sb, int offset,
            int pagelength) {
        sb.append(" LIMIT ").append(pagelength).append(" OFFSET ").append(
                offset);
        return sb;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.addon.sqlcontainer.query.generator.SQLGenerator#
     * generateDeleteQuery(java.lang.String,
     * com.vaadin.addon.sqlcontainer.RowItem)
     */
    public StatementHelper generateDeleteQuery(String tableName, RowItem item) {
        if (tableName == null || tableName.trim().equals("")) {
            throw new IllegalArgumentException("Table name must be given.");
        }
        if (item == null) {
            throw new IllegalArgumentException(
                    "Item to be deleted must be given.");
        }
        StatementHelper sh = new StatementHelper();
        StringBuffer query = new StringBuffer();
        query.append("DELETE FROM ").append(tableName).append(" WHERE");
        Collection<?> propIds = item.getItemPropertyIds();
        int count = 1;
        for (Object p : propIds) {
            if ((this instanceof MSSQLGenerator || this instanceof OracleGenerator)
                    && p.toString().equalsIgnoreCase("rownum")) {
                count++;
                continue;
            }
            if (item.getItemProperty(p).getValue() != null) {
                query.append(" \"" + p.toString() + "\" = ?");
                sh.addParameterValue(item.getItemProperty(p).getValue(), item
                        .getItemProperty(p).getType());
            }
            if (count < propIds.size()) {
                query.append(" AND");
            }
            count++;
        }
        /* Make sure that the where clause does not end with an AND */
        if (" AND".equals(query.substring(query.length() - 4))) {
            sh.setQueryString(query.substring(0, query.length() - 4));
        } else {
            sh.setQueryString(query.toString());
        }
        return sh;
    }
}