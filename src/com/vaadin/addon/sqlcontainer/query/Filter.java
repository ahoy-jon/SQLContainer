package com.vaadin.addon.sqlcontainer.query;

/**
 * Filter represents a filtering rule to be applied to a query made by the
 * SQLContainer's QueryDelegate.
 * 
 * Filter adds a few features on top of what the default sorting methods of
 * containers offer.
 */
public class Filter {

    public enum ComparisonType {
        EQUALS, GREATER, LESS, GREATER_OR_EQUAL, LESS_OR_EQUAL, STARTS_WITH, ENDS_WITH, CONTAINS, BETWEEN;
    }

    private String column;
    private Object value;
    private Object secondValue;

    private ComparisonType comparisonType;
    private boolean isCaseSensitive = true;
    private boolean needsQuotes = false;

    /**
     * Prevent instantiation without required parameters.
     */
    @SuppressWarnings("unused")
    private Filter() {
    }

    public Filter(String column, ComparisonType comparisonType, Object value) {
        setColumn(column);
        setComparisonType(comparisonType);
        setValue(value);
        if (value instanceof String || value instanceof CharSequence) {
            setNeedsQuotes(true);
        }
    }

    public Filter(String column, ComparisonType comparisonType, Object value,
            Object secondValue) {
        this(column, comparisonType, value);
        setSecondValue(secondValue);
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public String getColumn() {
        return column;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public Object getSecondValue() {
        return secondValue;
    }

    public void setSecondValue(Object secondValue) {
        this.secondValue = secondValue;
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

    public boolean isNeedsQuotes() {
        return needsQuotes;
    }

    public void setNeedsQuotes(boolean needsQuotes) {
        this.needsQuotes = needsQuotes;
    }

    public String toWhereString() {
        StringBuffer where = new StringBuffer();
        if (isCaseSensitive()) {
            where.append(getColumn());
        } else {
            where.append("UPPER(").append(getColumn()).append(")");
        }
        switch (getComparisonType()) {
        case EQUALS:
            where.append(" = ").append(format(getValue()));
            break;
        case GREATER:
            where.append(" > ").append(format(getValue()));
            break;
        case LESS:
            where.append(" < ").append(format(getValue()));
            break;
        case GREATER_OR_EQUAL:
            where.append(" >= ").append(format(getValue()));
            break;
        case LESS_OR_EQUAL:
            where.append(" <= ").append(format(getValue()));
            break;
        case STARTS_WITH:
            where.append(" LIKE ").append("'").append(
                    upperCaseIfCaseInsensitive(String.valueOf(getValue())))
                    .append("%'");
            break;
        case ENDS_WITH:
            where.append(" LIKE ").append("'%").append(
                    upperCaseIfCaseInsensitive(String.valueOf(getValue())))
                    .append("'");
            break;
        case CONTAINS:
            where.append(" LIKE ").append("'%").append(
                    upperCaseIfCaseInsensitive(String.valueOf(getValue())))
                    .append("%'");
            break;
        case BETWEEN:
            where.append(" BETWEEN ").append(format(getValue()))
                    .append(" AND ").append(format(getSecondValue()));
        }
        return where.toString();
    }

    private String format(Object value) {
        if (value instanceof String) {
            return "'" + upperCaseIfCaseInsensitive(String.valueOf(value))
                    + "'";
        }
        return upperCaseIfCaseInsensitive(String.valueOf(value));
    }

    private String upperCaseIfCaseInsensitive(String value) {
        if (isCaseSensitive()) {
            return value;
        }
        return value.toUpperCase();
    }
}
