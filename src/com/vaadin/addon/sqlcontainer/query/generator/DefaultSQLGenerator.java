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
    public String generateSelectQuery(String tableName, List<Filter> filters,
            FilteringMode filterMode, List<OrderBy> orderBys, int offset,
            int pagelength, String toSelect) {
        if (tableName == null || tableName.trim().equals("")) {
            throw new IllegalArgumentException("Table name must be given.");
        }

        StringBuffer query = new StringBuffer();
        if (toSelect == null) {
            query.append("SELECT * FROM ");
        } else {
            query.append("SELECT " + toSelect + " FROM ");
        }
        query.append(Util.escapeSQL(tableName));

        if (filters != null) {
            for (Filter f : filters) {
                generateFilter(query, f, filters.indexOf(f) == 0, filterMode);
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
        return query.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.addon.sqlcontainer.query.generator.SQLGenerator#
     * generateSelectQuery(java.lang.String, java.util.List, java.util.List,
     * int, int, java.lang.String)
     */
    public String generateSelectQuery(String tableName, List<Filter> filters,
            List<OrderBy> orderBys, int offset, int pagelength, String toSelect) {
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
    public String generateUpdateQuery(String tableName, RowItem item) {
        if (tableName == null || tableName.trim().equals("")) {
            throw new IllegalArgumentException("Table name must be given.");
        }
        if (item == null) {
            throw new IllegalArgumentException("Updated item must be given.");
        }

        StringBuffer query = new StringBuffer();
        query.append("UPDATE ");
        query.append(tableName);
        query.append(" SET");

        /* Generate column<->value map */
        Map<String, String> columnToValueMap = new HashMap<String, String>();
        Map<String, String> rowIdentifiers = new HashMap<String, String>();
        for (Object id : item.getItemPropertyIds()) {
            ColumnProperty cp = (ColumnProperty) item.getItemProperty(id);
            String value = cp.getValue() == null ? null : cp.getValue()
                    .toString();
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
                query.append(" ");
            } else {
                query.append(", ");
            }
            query.append("\"" + column + "\"");
            if (columnToValueMap.get(column) == null) {
                query.append(" = NULL");
            } else {
                query.append(" = '");
                query.append(Util.escapeSQL(columnToValueMap.get(column)));
                query.append("'");
            }
            first = false;
        }

        /* Generate identifiers for the row to be updated */
        first = true;
        for (String column : rowIdentifiers.keySet()) {
            if (first) {
                query.append(" WHERE ");
            } else {
                query.append(" AND ");
            }
            query.append("\"" + column + "\"");
            query.append(" = ");
            query.append(Util.escapeSQL(rowIdentifiers.get(column)));
            first = false;
        }

        return query.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.addon.sqlcontainer.query.generator.SQLGenerator#
     * generateInsertQuery(java.lang.String,
     * com.vaadin.addon.sqlcontainer.RowItem)
     */
    public String generateInsertQuery(String tableName, RowItem item) {
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
        StringBuffer query = new StringBuffer();
        query.append("INSERT INTO ");
        query.append(tableName);
        query.append(" (");

        /* Generate column<->value map */
        Map<String, String> columnToValueMap = new HashMap<String, String>();
        for (Object id : item.getItemPropertyIds()) {
            ColumnProperty cp = (ColumnProperty) item.getItemProperty(id);
            String value = cp.getValue() == null ? null : cp.getValue()
                    .toString();
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
            if (columnToValueMap.get(column) == null) {
                query.append("NULL");
            } else {
                query.append("'");
                query.append(Util.escapeSQL(columnToValueMap.get(column)));
                query.append("'");
            }
            first = false;
        }
        query.append(")");

        return query.toString();
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
            boolean firstFilter) {
        return generateFilter(sb, f, firstFilter,
                FilteringMode.FILTERING_MODE_INCLUSIVE);
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
            boolean firstFilter, FilteringMode filterMode) {
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
        sb.append(f.toWhereString());
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
        sb.append(" LIMIT ");
        sb.append(pagelength);
        sb.append(" OFFSET ");
        sb.append(offset);
        return sb;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.addon.sqlcontainer.query.generator.SQLGenerator#
     * generateDeleteQuery(java.lang.String,
     * com.vaadin.addon.sqlcontainer.RowItem)
     */
    public String generateDeleteQuery(String tableName, RowItem item) {
        if (tableName == null || tableName.trim().equals("")) {
            throw new IllegalArgumentException("Table name must be given.");
        }
        if (item == null) {
            throw new IllegalArgumentException(
                    "Item to be deleted must be given.");
        }
        StringBuffer query = new StringBuffer();
        query.append("DELETE FROM ");
        query.append(tableName);
        query.append(" WHERE");
        Collection<?> propIds = item.getItemPropertyIds();
        int count = 1;
        for (Object p : propIds) {
            if (item.getItemProperty(p).getValue() != null) {
                query.append(" ");
                query.append("\"" + p.toString() + "\"");
                query.append(" = '");
                query.append(Util.escapeSQL(item.getItemProperty(p).getValue()
                        .toString()));
                query.append("'");
            }
            if (count < propIds.size()) {
                query.append(" AND");
            }
            count++;
        }
        /* Make sure that the where clause does not end with an AND */
        if (" AND".equals(query.substring(query.length() - 4))) {
            return query.substring(0, query.length() - 4);
        }
        return query.toString();
    }
}
