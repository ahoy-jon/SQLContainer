package com.vaadin.addon.sqlcontainer;

import java.lang.reflect.Constructor;

import com.vaadin.data.Property;

/**
 * ColumnProperty represents the value of one column in a RowItem. In addition
 * to the value, ColumnProperty also contains some basic column attributes such
 * as nullability status, read-only status and data type.
 * 
 * Note that depending on the QueryDelegate in use this does not necessarily map
 * into an actual column in a database table.
 */
final public class ColumnProperty implements Property {
    private static final long serialVersionUID = -3694463129581802457L;

    private RowItem owner;

    private String propertyId;

    private boolean readOnly;
    private boolean allowReadOnlyChange = true;
    private boolean nullable = true;

    private Object value;
    private Object changedValue;
    private Class<?> type;

    /**
     * Prevent instantiation without required parameters.
     */
    private ColumnProperty() {
    }

    public ColumnProperty(String propertyId, boolean readOnly,
            boolean allowReadOnlyChange, boolean nullable, Object value,
            Class<?> type) {
        if (propertyId == null) {
            throw new IllegalArgumentException("Properties must be named.");
        }
        if (type == null) {
            throw new IllegalArgumentException("Property type must be set.");
        }
        this.propertyId = propertyId;
        this.type = type;
        this.value = value;

        this.allowReadOnlyChange = allowReadOnlyChange;
        this.nullable = nullable;
        this.readOnly = readOnly;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object newValue) throws ReadOnlyException,
            ConversionException {
        if (newValue == null && !nullable) {
            throw new ConversionException(
                    "Null values not allowed for this property.");
        }
        if (readOnly) {
            throw new ReadOnlyException(
                    "Cannot set value for read-only property.");
        }
        if (newValue != null
                && !getType().isAssignableFrom(newValue.getClass())) {
            try {
                final Constructor<?> constr = getType().getConstructor(
                        new Class[] { String.class });
                newValue = constr.newInstance(new Object[] { newValue
                        .toString() });
            } catch (Exception e) {
                throw new ConversionException(e);
            }
        }
        changedValue = newValue;
        owner.getContainer().itemChangeNotification(owner);
    }

    public Class<?> getType() {
        return type;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean newStatus) {
        if (allowReadOnlyChange) {
            readOnly = newStatus;
        }
    }

    public String getPropertyId() {
        return propertyId;
    }

    @Override
    public String toString() {
        if (value != null) {
            return value.toString();
        }
        return "";
    }

    public void setOwner(RowItem owner) {
        if (owner == null) {
            throw new IllegalArgumentException("Owner can not be set to null.");
        }
        if (this.owner != null) {
            throw new IllegalStateException(
                    "ColumnProperties can only be bound once.");
        }
        this.owner = owner;
    }

    public boolean isModified() {
        if (changedValue != null && !changedValue.equals(value)) {
            return true;
        }
        return false;
    }
}
