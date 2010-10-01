package com.vaadin.addon.sqlcontainer.query.generator;

import java.util.List;

import com.vaadin.addon.sqlcontainer.query.Filter;
import com.vaadin.addon.sqlcontainer.query.FilteringMode;
import com.vaadin.addon.sqlcontainer.query.OrderBy;

@SuppressWarnings("serial")
public class OracleGenerator extends DefaultSQLGenerator {

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.addon.sqlcontainer.query.generator.DefaultSQLGenerator#
     * generateSelectQuery(java.lang.String, java.util.List,
     * com.vaadin.addon.sqlcontainer.query.FilteringMode, java.util.List, int,
     * int, java.lang.String)
     */
    @Override
    public StatementHelper generateSelectQuery(String tableName,
            List<Filter> filters, FilteringMode filterMode,
            List<OrderBy> orderBys, int offset, int pagelength, String toSelect) {
        if (tableName == null || tableName.trim().equals("")) {
            throw new IllegalArgumentException("Table name must be given.");
        }
        /* Adjust offset and page length parameters to match "row numbers" */
        if (pagelength > 1) {
            offset++;
            pagelength--;
        }
        StatementHelper sh = new StatementHelper();
        StringBuffer query = new StringBuffer();

        /* Row count request is handled here */
        if ("COUNT(*)".equalsIgnoreCase(toSelect)) {
            query
                    .append("SELECT COUNT(*) AS \"rowcount\" FROM (SELECT * FROM ");
            query.append(tableName);
            if (filters != null && !filters.isEmpty()) {
                for (Filter f : filters) {
                    generateFilter(query, f, filters.indexOf(f) == 0,
                            filterMode, sh);
                }
            }
            query.append(")");
            sh.setQueryString(query.toString());
            return sh;
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
                    generateFilter(query, f, filters.indexOf(f) == 0,
                            filterMode, sh);
                }
            }
            if (orderBys != null) {
                for (OrderBy o : orderBys) {
                    generateOrderBy(query, o, orderBys.indexOf(o) == 0);
                }
            }
            sh.setQueryString(query.toString());
            return sh;
        }

        if (toSelect == null) {
            query
                    .append("SELECT * FROM (SELECT x.*, ROWNUM AS \"rownum\" FROM (SELECT * FROM ");
        } else {
            query
                    .append("SELECT * FROM (SELECT x.*, ROWNUM AS \"rownum\" FROM (SELECT "
                            + toSelect + " FROM ");
        }
        query.append(tableName);

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
        query.append(") x) WHERE \"rownum\" BETWEEN ").append(
                Integer.toString(offset)).append(" AND ").append(
                Integer.toString(offset + pagelength));
        sh.setQueryString(query.toString());
        return sh;
    }
}