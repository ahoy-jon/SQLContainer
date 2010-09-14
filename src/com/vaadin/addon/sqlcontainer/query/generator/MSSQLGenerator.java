package com.vaadin.addon.sqlcontainer.query.generator;

import java.util.List;

import com.vaadin.addon.sqlcontainer.query.Filter;
import com.vaadin.addon.sqlcontainer.query.OrderBy;

public class MSSQLGenerator extends DefaultSQLGenerator {

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.addon.sqlcontainer.query.generator.DefaultSQLGenerator#
     * generateSelectQuery(java.lang.String, java.util.List, java.util.List,
     * int, int, java.lang.String)
     */
    @Override
    public String generateSelectQuery(String tableName, List<Filter> filters,
            List<OrderBy> orderBys, int offset, int pagelength, String toSelect) {
        if (tableName == null || tableName.trim().equals("")) {
            throw new IllegalArgumentException("Table name must be given.");
        }
        if (pagelength > 1) {
            offset++;
            pagelength--;
        }
        StringBuffer query = new StringBuffer();

        /* Row count request is handled here */
        if ("COUNT(*)".equalsIgnoreCase(toSelect)) {
            query
                    .append("SELECT COUNT(*) AS \"rowcount\" FROM (SELECT * FROM ");
            query.append(tableName);
            if (filters != null && !filters.isEmpty()) {
                for (Filter f : filters) {
                    generateFilter(query, f, filters.indexOf(f) == 0);
                }
            }
            query.append(") AS t");
            return query.toString();
        }

        /* SELECT without row number constraints */
        if (offset == 0 && pagelength == 0) {
            query.append("SELECT ");
            if (toSelect != null) {
                query.append(toSelect);
            } else {
                query.append("*");
            }
            query.append(" FROM ");
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
            return query.toString();
        }

        /* Remaining SELECT cases are handled here */
        query.append("SELECT * FROM (SELECT row_number() OVER (");
        if (orderBys != null) {
            for (OrderBy o : orderBys) {
                generateOrderBy(query, o, orderBys.indexOf(o) == 0);
            }
        }
        if (toSelect == null) {
            query.append(") AS rownum, * FROM ");
        } else {
            query.append(") AS rownum, " + toSelect + " FROM ");
        }

        query.append(tableName);

        if (filters != null) {
            for (Filter f : filters) {
                generateFilter(query, f, filters.indexOf(f) == 0);
            }
        }

        query.append(") AS a WHERE a.rownum BETWEEN ");
        query.append(offset);
        query.append(" AND ");
        query.append(Integer.toString(offset + pagelength));
        return query.toString();
    }

}
