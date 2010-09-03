package com.vaadin.addon.sqlcontainer.demo;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import com.vaadin.addon.sqlcontainer.RowItem;
import com.vaadin.addon.sqlcontainer.TemporaryRowId;
import com.vaadin.addon.sqlcontainer.query.Filter;
import com.vaadin.addon.sqlcontainer.query.FreeformQueryDelegate;
import com.vaadin.addon.sqlcontainer.query.OrderBy;

public class DemoFreeformQueryDelegate implements FreeformQueryDelegate {

    private List<Filter> filters;
    private List<OrderBy> orderBys;

    public String getQueryString(int offset, int limit)
            throws UnsupportedOperationException {
        StringBuffer query = new StringBuffer("SELECT * FROM PEOPLE");
        query.append(getFiltersString());
        query.append(getOrderByString());
        query.append(" LIMIT ").append(limit);
        query.append(" OFFSET ").append(offset);
        return query.toString();
    }

    private String getOrderByString() {
        StringBuffer orderBuffer = new StringBuffer("");
        if (orderBys != null && !orderBys.isEmpty()) {
            orderBuffer.append(" ORDER BY ");
            OrderBy lastOrderBy = orderBys.get(orderBys.size() - 1);
            for (OrderBy orderBy : orderBys) {
                orderBuffer.append(orderBy.getColumn());
                if (orderBy.isAscending()) {
                    orderBuffer.append(" ASC");
                } else {
                    orderBuffer.append(" DESC");
                }
                if (orderBy != lastOrderBy) {
                    orderBuffer.append(", ");
                }
            }
        }
        return orderBuffer.toString();
    }

    private String getFiltersString() {
        StringBuffer filterBuffer = new StringBuffer("");
        if (filters != null && !filters.isEmpty()) {
            Filter lastFilter = filters.get(filters.size() - 1);
            filterBuffer.append(" WHERE ");
            for (Filter filter : filters) {
                filterBuffer.append(filter.toWhereString());
                if (filter != lastFilter) {
                    filterBuffer.append(" AND ");
                }
            }
        }
        return filterBuffer.toString();
    }

    public String getCountQuery() throws UnsupportedOperationException {
        return "SELECT COUNT(*) FROM PEOPLE" + getFiltersString();
    }

    public void setFilters(List<Filter> filters)
            throws UnsupportedOperationException {
        this.filters = filters;
    }

    public void setOrderBy(List<OrderBy> orderBys)
            throws UnsupportedOperationException {
        this.orderBys = orderBys;
    }

    public int storeRow(Connection conn, RowItem row) throws SQLException {
        Statement statement = conn.createStatement();

        String query = null;
        if (row.getId() instanceof TemporaryRowId) {
            query = getInsertQuery(row);
        } else {
            query = getUpdateQuery(row);
        }

        int retval = statement.executeUpdate(query);
        statement.close();
        return retval;
    }

    private String getInsertQuery(RowItem row) {
        StringBuffer insert = new StringBuffer(
                "INSERT INTO PEOPLE VALUES(DEFAULT, ");
        appendInsertValue(insert, row, "FIRSTNAME");
        appendInsertValue(insert, row, "LASTNAME");
        appendInsertValue(insert, row, "COMPANY");
        appendInsertValue(insert, row, "MOBILE");
        appendInsertValue(insert, row, "WORKPHONE");
        appendInsertValue(insert, row, "HOMEPHONE");
        appendInsertValue(insert, row, "WORKEMAIL");
        appendInsertValue(insert, row, "HOMEEMAIL");
        appendInsertValue(insert, row, "STREET");
        appendInsertValue(insert, row, "ZIP");
        appendInsertValue(insert, row, "CITY");
        appendInsertValue(insert, row, "STATE");
        appendInsertValue(insert, row, "COUNTRY");
        insert.append(")");
        return insert.toString();
    }

    private void appendInsertValue(StringBuffer insert, RowItem row,
            String propId) {
        Object val = row.getItemProperty(propId).getValue();
        if (val != null) {
            insert.append("'").append(val).append("'");
        } else {
            insert.append("NULL");
        }
        if (!"COUNTRY".equals(propId)) {
            insert.append(", ");
        }
    }

    private String getUpdateQuery(RowItem row) {
        StringBuffer update = new StringBuffer("UPDATE PEOPLE SET ");
        appendUpdateValue(update, row, "FIRSTNAME");
        appendUpdateValue(update, row, "LASTNAME");
        appendUpdateValue(update, row, "COMPANY");
        appendUpdateValue(update, row, "MOBILE");
        appendUpdateValue(update, row, "WORKPHONE");
        appendUpdateValue(update, row, "HOMEPHONE");
        appendUpdateValue(update, row, "WORKEMAIL");
        appendUpdateValue(update, row, "HOMEEMAIL");
        appendUpdateValue(update, row, "STREET");
        appendUpdateValue(update, row, "ZIP");
        appendUpdateValue(update, row, "CITY");
        appendUpdateValue(update, row, "STATE");
        appendUpdateValue(update, row, "COUNTRY");
        update.append(" WHERE ID = ").append(row.getItemProperty("ID"));
        return update.toString();
    }

    private void appendUpdateValue(StringBuffer update, RowItem row,
            String propId) {
        update.append(propId).append(" = ");
        Object val = row.getItemProperty(propId).getValue();
        if (val != null) {
            update.append("'").append(val).append("'");
        } else {
            update.append("NULL");
        }

        if (!"COUNTRY".equals(propId)) {
            update.append(", ");
        }
    }

    public boolean removeRow(Connection conn, RowItem row)
            throws UnsupportedOperationException, SQLException {
        Statement statement = conn.createStatement();
        int rowsChanged = statement
                .executeUpdate("DELETE FROM people WHERE ID="
                        + row.getItemProperty("ID"));
        statement.close();
        return rowsChanged == 1;
    }

    public String getContainsRowQueryString(Object... keys)
            throws UnsupportedOperationException {
        // TODO Auto-generated method stub
        return null;
    }

}
