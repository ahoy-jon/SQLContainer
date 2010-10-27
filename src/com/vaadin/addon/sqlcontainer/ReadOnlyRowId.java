package com.vaadin.addon.sqlcontainer;

import java.util.UUID;

public class ReadOnlyRowId extends RowId {
    private static final long serialVersionUID = -2626764781642012467L;
    private final UUID uuid;

    public ReadOnlyRowId() {
        super();
        uuid = UUID.randomUUID();
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ReadOnlyRowId)) {
            return false;
        }
        return uuid.equals(((ReadOnlyRowId) obj).uuid);
    }
}
