package com.vaadin.addon.sqlcontainer.query.generator;

import java.util.List;

import com.vaadin.addon.sqlcontainer.query.Filter;
import com.vaadin.addon.sqlcontainer.query.OrderBy;

public class OracleGenerator extends DefaultSQLGenerator {

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

        StringBuffer query = new StringBuffer();
        if (toSelect == null) {
            query
                    .append("SELECT * FROM (SELECT ROWNUM r, * FROM (SELECT * FROM ");
        } else {
            query.append("SELECT * FROM (SELECT ROWNUM r, * FROM (SELECT "
                    + toSelect + " FROM ");
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
        query.append(") WHERE ROWNUM <= ");
        query.append(Integer.toString(offset + pagelength));
        query.append(") WHERE ROWNUM >= ");
        query.append(offset);
        return query.toString();
    }

}
