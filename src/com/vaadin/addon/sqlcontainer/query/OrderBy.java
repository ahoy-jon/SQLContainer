package com.vaadin.addon.sqlcontainer.query;

public class OrderBy {
    private String column;
    private boolean isAscending;

    public OrderBy(String column, boolean isAscending) {
        setColumn(column);
        setAscending(isAscending);
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public String getColumn() {
        return column;
    }

    public void setAscending(boolean isAscending) {
        this.isAscending = isAscending;
    }

    public boolean isAscending() {
        return isAscending;
    }
}
