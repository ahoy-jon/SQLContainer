package com.vaadin.addon.sqlcontainer.query.generator;

import java.util.Collection;
import java.util.List;

import com.vaadin.addon.sqlcontainer.RowItem;
import com.vaadin.addon.sqlcontainer.query.Filter;
import com.vaadin.addon.sqlcontainer.query.OrderBy;
import com.vaadin.addon.sqlcontainer.query.Filter.ComparisonType;

/**
 * Generates generic SQL that is supported by HSQLDB, MySQL and PostgreSQL.
 * 
 * @author Jonatan Kronqvist / IT Mill Ltd
 */
public class DefaultSQLGenerator implements SQLGenerator {

    protected String searchStringEscape;

    public String generateSelectQuery(String tableName, List<Filter> filters,
            List<OrderBy> orderBys, int offset, int pagelength, String toSelect) {
        if (tableName == null || tableName.trim().equals("")) {
            throw new IllegalArgumentException("Table name must be given.");
        }

        StringBuffer query = new StringBuffer();
        if (toSelect == null) {
            query.append("SELECT * FROM ");
        } else {
            query.append("SELECT " + toSelect + " FROM ");
        }
        query.append(tableName);

        if (filters != null) {
            for (Filter f : filters) {
                generateFilter(query, f, filters.indexOf(f) == 0);
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

        // TODO: Extract properties from item and generate rest of the query
        // TODO: Input sanitation ??

        // boolean first = true;
        // for (String column : columnToValueMap.keySet()) {
        // if (first) {
        // query.append(" ");
        // } else {
        // query.append(", ");
        // }
        // query.append(column);
        // query.append(" = '");
        // query.append(columnToValueMap.get(column));
        // query.append("'");
        // first = false;
        // }
        //
        // first = true;
        // for (String column : rowIdentifiers.keySet()) {
        // if (first) {
        // query.append(" WHERE ");
        // } else {
        // query.append(" AND ");
        // }
        // query.append(column);
        // query.append(" = ");
        // query.append(rowIdentifiers.get(column));
        // first = false;
        // }

        return query.toString();
    }

    public String generateInsertQuery(String tableName, RowItem item) {
        if (tableName == null || tableName.trim().equals("")) {
            throw new IllegalArgumentException("Table name must be given.");
        }
        if (item == null) {
            throw new IllegalArgumentException("New item must be given.");
        }
        StringBuffer query = new StringBuffer();
        query.append("INSERT INTO ");
        query.append(tableName);
        query.append(" (");

        // TODO: Extract properties from item and generate rest of the query
        // TODO: Input sanitation ??

        // item.getItemPropertyIds()

        // boolean first = true;
        // for (String column : columnToValueMap.keySet()) {
        // if (!first) {
        // query.append(", ");
        // }
        // query.append(column);
        // first = false;
        // }
        //
        // query.append(") VALUES (");
        //
        // first = true;
        // for (String column : columnToValueMap.keySet()) {
        // if (!first) {
        // query.append(", ");
        // }
        // query.append("'");
        // query.append(columnToValueMap.get(column));
        // query.append("'");
        // first = false;
        // }
        // query.append(")");

        return query.toString();
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
     * @return
     */
    protected StringBuffer generateFilter(StringBuffer sb, Filter f,
            boolean firstFilter) {
        if (firstFilter) {
            sb.append(" WHERE ");
        } else {
            sb.append(" AND ");
        }
        String name, value;
        if (f.getComparisonType() == ComparisonType.STARTS_WITH) {
            value = escapeWildCards(f.getValue()) + "%";
            f.setNeedsQuotes(true);
        } else if (f.getComparisonType() == ComparisonType.CONTAINS) {
            value = "%" + escapeWildCards(f.getValue()) + "%";
            f.setNeedsQuotes(true);
        } else if (f.getComparisonType() == ComparisonType.ENDS_WITH) {
            value = "%" + escapeWildCards(f.getValue());
            f.setNeedsQuotes(true);
        } else {
            value = f.getValue();
        }
        if (!f.isCaseSensitive()) {
            name = "LOWER(" + f.getColumn() + ")";
            value = "LOWER('" + value + "')";
        } else {
            name = f.getColumn();
            if (f.isNeedsQuotes()) {
                value = "'" + value + "'";
            }
        }

        sb.append(name);

        switch (f.getComparisonType()) {
        case STARTS_WITH:
        case CONTAINS:
        case ENDS_WITH:
            sb.append(" LIKE ");
            break;
        case LESS:
            sb.append(" < ");
            break;
        case LESS_OR_EQUAL:
            sb.append(" <= ");
            break;
        case EQUALS:
            sb.append(" = ");
            break;
        case GREATER:
            sb.append(" > ");
            break;
        case GREATER_OR_EQUAL:
            sb.append(" >= ");
            break;
        }
        sb.append(value);
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
        sb.append(o.getColumn());
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

    /**
     * This function will escape wildcard characters used in SQL search strings,
     * e.g. with the LIKE keyword. If the escape string is not set, the original
     * input will be returned as is.
     * 
     * @param constant
     *            Original string to perform the escaping on.
     * @return Escaped search string.
     */
    protected String escapeWildCards(String constant) {
        String fixedConstant;
        if (searchStringEscape == null) {
            return constant;
        }
        if (constant != null) {
            fixedConstant = constant.replace("%", searchStringEscape + "%");
            fixedConstant = fixedConstant
                    .replace("_", searchStringEscape + "_");
            return fixedConstant;
        }
        return null;
    }

    public void setSearchStringEscape(String searchStringEscape) {
        this.searchStringEscape = searchStringEscape;
    }

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
        Collection propIds = item.getItemPropertyIds();
        int count = 1;
        for (Object p : propIds) {
            if (item.getItemProperty(p).getValue() != null) {
                query.append(" ");
                query.append(p.toString());
                query.append(" = '");
                query.append(item.getItemProperty(p).getValue().toString());
                query.append("'");
            }
            if (count < propIds.size()) {
                query.append(" AND");
            }
            count++;
        }
        return query.toString();
    }
}
