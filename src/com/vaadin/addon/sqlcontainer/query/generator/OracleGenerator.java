package com.vaadin.addon.sqlcontainer.query.generator;

import java.util.List;

import com.vaadin.addon.sqlcontainer.query.OrderBy;
import com.vaadin.addon.sqlcontainer.query.generator.filter.FilterToWhereTranslator;
import com.vaadin.data.Container.Filter;

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
            List<Filter> filters, List<OrderBy> orderBys, int offset,
            int pagelength, String toSelect) {
        if (tableName == null || tableName.trim().equals("")) {
            throw new IllegalArgumentException("Table name must be given.");
        }
        /* Adjust offset and page length parameters to match "row numbers" */
        offset = pagelength > 1 ? ++offset : offset;
        pagelength = pagelength > 1 ? --pagelength : pagelength;
        toSelect = toSelect == null ? "*" : toSelect;
        StatementHelper sh = new StatementHelper();
        StringBuffer query = new StringBuffer();

        /* Row count request is handled here */
        if ("COUNT(*)".equalsIgnoreCase(toSelect)) {
            query.append("SELECT COUNT(*) AS \"rowcount\" FROM (SELECT * FROM ");
            query.append(tableName);
            if (filters != null && !filters.isEmpty()) {
                query.append(FilterToWhereTranslator.getWhereStringForFilters(
                        filters, sh));
            }
            query.append(")");
            sh.setQueryString(query.toString());
            return sh;
        }

        /* SELECT without row number constraints */
        if (offset == 0 && pagelength == 0) {
            query.append("SELECT ").append(toSelect).append(" FROM ")
                    .append(tableName);
            if (filters != null) {
                query.append(FilterToWhereTranslator.getWhereStringForFilters(
                        filters, sh));
            }
            if (orderBys != null) {
                for (OrderBy o : orderBys) {
                    generateOrderBy(query, o, orderBys.indexOf(o) == 0);
                }
            }
            sh.setQueryString(query.toString());
            return sh;
        }

        /* Remaining SELECT cases are handled here */
        query.append("SELECT * FROM ")
                .append("(SELECT x.*, ROWNUM AS \"rownum\" FROM (SELECT "
                        + toSelect + " FROM ").append(tableName);
        if (filters != null) {
            query.append(FilterToWhereTranslator.getWhereStringForFilters(
                    filters, sh));
        }
        if (orderBys != null) {
            for (OrderBy o : orderBys) {
                generateOrderBy(query, o, orderBys.indexOf(o) == 0);
            }
        }
        query.append(") x) WHERE \"rownum\" BETWEEN ")
                .append(Integer.toString(offset)).append(" AND ")
                .append(Integer.toString(offset + pagelength));
        sh.setQueryString(query.toString());
        return sh;
    }
}