package com.vaadin.addon.sqlcontainer.query;

import com.vaadin.addon.sqlcontainer.Util;

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

    public String getEscapedColumn() {
        return Util.escapeSQL(column);
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public String getEscapedValue() {
        return Util.escapeSQL(String.valueOf(value));
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

    public boolean passes(Object testValue) {
        /* Handle null values. Here null will equal null. */
        if (value == null) {
            return testValue == null ? true : false;
        }
        if (value != null && testValue == null) {
            return false;
        }
        switch (getComparisonType()) {
        case EQUALS:
            return compareValues(testValue, value) == 0;
        case GREATER:
            return compareValues(testValue, value) > 0;
        case LESS:
            return compareValues(testValue, value) < 0;
        case GREATER_OR_EQUAL:
            return compareValues(testValue, value) >= 0;
        case LESS_OR_EQUAL:
            return compareValues(testValue, value) <= 0;
        case STARTS_WITH:
            if (testValue instanceof String) {
                if (isCaseSensitive) {
                    return ((String) testValue).startsWith(String
                            .valueOf(value));
                } else {
                    return ((String) testValue).toUpperCase().startsWith(
                            String.valueOf(value).toUpperCase());
                }
            }
            break;
        case ENDS_WITH:
            if (testValue instanceof String) {
                if (isCaseSensitive) {
                    return ((String) testValue).endsWith(String.valueOf(value));
                } else {
                    return ((String) testValue).toUpperCase().endsWith(
                            String.valueOf(value).toUpperCase());
                }
            }
            break;
        case CONTAINS:
            if (testValue instanceof String) {
                if (isCaseSensitive) {
                    return ((String) testValue).contains(String.valueOf(value));
                } else {
                    return ((String) testValue).toUpperCase().contains(
                            String.valueOf(value).toUpperCase());
                }
            }
            break;
        case BETWEEN:
            break;
        }
        return false;
    }

    /**
     * Returns this filtering rule as a string that can be used when generating
     * PreparedStatement objects
     */
    public String toPreparedStatementString() {
        if (value == null) {
            return null;
        }
        StringBuffer where = new StringBuffer();
        if (isCaseSensitive()) {
            where.append("\"" + getEscapedColumn() + "\"");
        } else {
            where.append("UPPER(\"").append(getEscapedColumn()).append("\")");
        }
        switch (getComparisonType()) {
        case EQUALS:
            where.append(" = ?");
            break;
        case GREATER:
            where.append(" > ?");
            break;
        case LESS:
            where.append(" < ?");
            break;
        case GREATER_OR_EQUAL:
            where.append(" >= ?");
            break;
        case LESS_OR_EQUAL:
            where.append(" <= ?");
            break;
        case STARTS_WITH:
        case CONTAINS:
        case ENDS_WITH:
            where.append(" LIKE ?");
            break;
        case BETWEEN:
            where.append(" BETWEEN ? AND ?");
            break;
        }
        return where.toString();
    }

    /**
     * Returns the value of this rule to be inserted as a parameter into a
     * PreparedStatement
     */
    public Object getPreparedStatementValue() {
        switch (getComparisonType()) {
        case STARTS_WITH:
            return upperCaseIfCaseInsensitive(String.valueOf(getValue())) + "%";
        case ENDS_WITH:
            return "%" + upperCaseIfCaseInsensitive(String.valueOf(getValue()));
        case CONTAINS:
            return "%" + upperCaseIfCaseInsensitive(String.valueOf(getValue()))
                    + "%";
        default:
            return getValue();
        }
    }

    /**
     * Returns this filtering rule as a string that can be used when generating
     * Statement objects
     */
    @Deprecated
    public String toWhereString() {
        /* Null filter equates to 1=1 where clause */
        if (value == null) {
            return "1=1";
        }

        StringBuffer where = new StringBuffer();
        if (isCaseSensitive()) {
            where.append("\"" + getEscapedColumn() + "\"");
        } else {
            where.append("UPPER(\"").append(getEscapedColumn()).append("\")");
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
            break;
        }
        return where.toString();
    }

    private String upperCaseIfCaseInsensitive(String value) {
        if (isCaseSensitive()) {
            return value;
        }
        return value.toUpperCase();
    }

    private int compareValues(Object value1, Object value2) {
        if (value1 instanceof String) {
            if (isCaseSensitive) {
                return ((String) value1).compareTo((String) value2);
            } else {
                return ((String) value1).compareToIgnoreCase((String) value2);
            }
        } else if (value1 instanceof Integer) {
            return ((Integer) value1).compareTo((Integer) value2);
        } else if (value1 instanceof Long) {
            return ((Long) value1).compareTo((Long) value2);
        } else if (value1 instanceof Double) {
            return ((Double) value1).compareTo((Double) value2);
        } else if (value1 instanceof Float) {
            return ((Float) value1).compareTo((Float) value2);
        } else if (value1 instanceof Short) {
            return ((Short) value1).compareTo((Short) value2);
        } else if (value1 instanceof Byte) {
            return ((Byte) value1).compareTo((Byte) value2);
        }
        throw new IllegalArgumentException("Could not compare the arguments: "
                + value1 + ", " + value2);
    }

    @Deprecated
    private String format(Object value) {
        String toReturn = Util.escapeSQL(upperCaseIfCaseInsensitive(String
                .valueOf(value)));
        if (value instanceof String) {
            return "'" + toReturn + "'";
        }
        return toReturn;
    }
}
