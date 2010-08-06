package com.vaadin.addon.sqlcontainer;

import java.io.Serializable;

/**
 * RowId represents identifiers of a single database result set row.
 * 
 * The data structure of a RowId is an Object array which contains the values of
 * the primary key columns of the identified row. This allows easy equals()
 * -comparison of RowItems.
 */
public class RowId implements Serializable {
    private static final long serialVersionUID = -3161778404698901258L;
    private Object[] id;

    /**
     * Prevent instantiation without required parameters.
     */
    private RowId() {
    }

    public RowId(Object[] id) {
        this.id = id;
    }

    public Object[] getId() {
        return id;
    }

    @Override
    public int hashCode() {
        int result = 31;
        for (Object o : id) {
            result += o.hashCode();
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof RowId)) {
            return false;
        }
        Object[] compId = ((RowId) obj).getId();
        if (id.length != compId.length) {
            return false;
        }
        for (int i = 0; i < id.length; i++) {
            if (!id[i].equals(compId[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        String s = "";
        for (int i = 0; i < id.length; i++) {
            s += id[i].toString();
        }
        return s;
    }
}
