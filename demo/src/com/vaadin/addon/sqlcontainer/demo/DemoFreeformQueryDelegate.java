package com.vaadin.addon.sqlcontainer.demo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import com.vaadin.addon.sqlcontainer.RowItem;
import com.vaadin.addon.sqlcontainer.TemporaryRowId;
import com.vaadin.addon.sqlcontainer.Util;
import com.vaadin.addon.sqlcontainer.query.Filter;
import com.vaadin.addon.sqlcontainer.query.FilteringMode;
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
        if (offset != 0 || limit != 0) {
            query.append(" LIMIT ").append(limit);
            query.append(" OFFSET ").append(offset);
        }
        return query.toString();
    }

    private String getOrderByString() {
        StringBuffer orderBuffer = new StringBuffer("");
        if (orderBys != null && !orderBys.isEmpty()) {
            orderBuffer.append(" ORDER BY ");
            OrderBy lastOrderBy = orderBys.get(orderBys.size() - 1);
            for (OrderBy orderBy : orderBys) {
                orderBuffer.append(Util.escapeSQL(orderBy.getColumn()));
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

    public void setFilters(List<Filter> filters, FilteringMode filteringMode)
            throws UnsupportedOperationException {
        // TODO: Implement ?
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
        PreparedStatement statement = null;
        if (row.getId() instanceof TemporaryRowId) {
            statement = conn
                    .prepareStatement("INSERT INTO PEOPLE VALUES(DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            setRowValues(statement, row);
        } else {
            statement = conn
                    .prepareStatement("UPDATE PEOPLE SET FIRSTNAME = ?, LASTNAME = ?, COMPANY = ?, MOBILE = ?, WORKPHONE = ?, HOMEPHONE = ?, WORKEMAIL = ?, HOMEEMAIL = ?, STREET = ?, ZIP = ?, CITY = ?, STATE = ?, COUNTRY = ? WHERE ID = ?");
            setRowValues(statement, row);
            statement
                    .setInt(14, (Integer) row.getItemProperty("ID").getValue());
        }

        int retval = statement.executeUpdate();
        statement.close();
        return retval;
    }

    private void setRowValues(PreparedStatement statement, RowItem row)
            throws SQLException {
        statement.setString(1, (String) row.getItemProperty("FIRSTNAME")
                .getValue());
        statement.setString(2, (String) row.getItemProperty("LASTNAME")
                .getValue());
        statement.setString(3, (String) row.getItemProperty("COMPANY")
                .getValue());
        statement.setString(4, (String) row.getItemProperty("MOBILE")
                .getValue());
        statement.setString(5, (String) row.getItemProperty("WORKPHONE")
                .getValue());
        statement.setString(6, (String) row.getItemProperty("HOMEPHONE")
                .getValue());
        statement.setString(7, (String) row.getItemProperty("WORKEMAIL")
                .getValue());
        statement.setString(8, (String) row.getItemProperty("HOMEEMAIL")
                .getValue());
        statement.setString(9, (String) row.getItemProperty("STREET")
                .getValue());
        statement.setString(10, (String) row.getItemProperty("ZIP").getValue());
        statement
                .setString(11, (String) row.getItemProperty("CITY").getValue());
        statement.setString(12, (String) row.getItemProperty("STATE")
                .getValue());
        statement.setString(13, (String) row.getItemProperty("COUNTRY")
                .getValue());
    }

    public boolean removeRow(Connection conn, RowItem row)
            throws UnsupportedOperationException, SQLException {
        PreparedStatement statement = conn
                .prepareStatement("DELETE FROM people WHERE ID = ?");
        statement.setInt(1, (Integer) row.getItemProperty("ID").getValue());
        int rowsChanged = statement.executeUpdate();
        statement.close();
        return rowsChanged == 1;
    }

    public String getContainsRowQueryString(Object... keys)
            throws UnsupportedOperationException {
        return "SELECT * FROM people WHERE ID = "
                + Util.escapeSQL(String.valueOf(keys[0]));
    }
}
