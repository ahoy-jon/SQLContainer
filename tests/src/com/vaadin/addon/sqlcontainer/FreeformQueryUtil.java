package com.vaadin.addon.sqlcontainer;

import java.util.List;

import com.vaadin.addon.sqlcontainer.AllTests.DB;
import com.vaadin.addon.sqlcontainer.query.Filter;

public class FreeformQueryUtil {
    @SuppressWarnings("deprecation")
    public static String getQueryStringWithFilters(List<Filter> filters,
            int offset, int limit) {
        if (AllTests.db == DB.MSSQL) {
            if (limit > 1) {
                offset++;
                limit--;
            }
            StringBuffer query = new StringBuffer();
            query.append("SELECT * FROM (SELECT row_number() OVER (");
            query.append("ORDER BY \"ID\" ASC");
            query.append(") AS rownum, * FROM \"PEOPLE\"");

            if (!filters.isEmpty()) {
                Filter lastFilter = filters.get(filters.size() - 1);
                query.append(" WHERE ");
                for (Filter filter : filters) {
                    query.append(filter.toWhereString());
                    if (lastFilter != filter) {
                        query.append(" AND ");
                    }
                }
            }
            query.append(") AS a WHERE a.rownum BETWEEN ").append(offset)
                    .append(" AND ").append(Integer.toString(offset + limit));
            return query.toString();
        } else if (AllTests.db == DB.ORACLE) {
            if (limit > 1) {
                offset++;
                limit--;
            }
            StringBuffer query = new StringBuffer();
            query.append("SELECT * FROM (SELECT x.*, ROWNUM AS "
                    + "\"rownum\" FROM (SELECT * FROM \"PEOPLE\"");
            if (!filters.isEmpty()) {
                Filter lastFilter = filters.get(filters.size() - 1);
                query.append(" WHERE ");
                for (Filter filter : filters) {
                    query.append(filter.toWhereString());
                    if (lastFilter != filter) {
                        query.append(" AND ");
                    }
                }
            }
            query.append(") x) WHERE \"rownum\" BETWEEN ")
                    .append(Integer.toString(offset)).append(" AND ")
                    .append(Integer.toString(offset + limit));
            return query.toString();
        } else {
            StringBuffer query = new StringBuffer("SELECT * FROM people");
            if (!filters.isEmpty()) {
                Filter lastFilter = filters.get(filters.size() - 1);
                query.append(" WHERE ");
                for (Filter filter : filters) {
                    query.append(filter.toWhereString());
                    if (lastFilter != filter) {
                        query.append(" AND ");
                    }
                }
            }
            if (limit != 0 || offset != 0) {
                query.append(" LIMIT ").append(limit).append(" OFFSET ")
                        .append(offset);
            }
            return query.toString();
        }
    }

}
