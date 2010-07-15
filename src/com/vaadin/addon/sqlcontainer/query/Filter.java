package com.vaadin.addon.sqlcontainer.query;

public class Filter {

    public enum ComparisonType {
        EQUALS, GREATER, LESS, GREATER_OR_EQUAL, LESS_OR_EQUAL, STARTS_WITH, ENDS_WITH, CONTAINS;
    }

    private String column;
    private String value;
    private ComparisonType comparisonType;
    private boolean isCaseSensitive;

    public Filter() {

    }

    public Filter(String column, ComparisonType comparisonType, String value) {
        setColumn(column);
        setComparisonType(comparisonType);
        setValue(value);
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public String getColumn() {
        return column;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setComparisonType(ComparisonType comparisonType) {
        this.comparisonType = comparisonType;
    }

    public ComparisonType getComparisonType() {
        return comparisonType;
    }

    public void setCaseSensitive(boolean isCaseSensitive) {
        this.isCaseSensitive = isCaseSensitive;
    }

    public boolean isCaseSensitive() {
        return isCaseSensitive;
    }
}
