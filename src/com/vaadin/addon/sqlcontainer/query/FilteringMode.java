package com.vaadin.addon.sqlcontainer.query;

public enum FilteringMode {
    /**
     * Inclusive filtering mode. All filtering rules will be treated as logical
     * conjunction (AND). This is the default filtering mode.
     */
    FILTERING_MODE_INCLUSIVE,
    /**
     * Exclusive filtering mode. All filtering rules will be treated as logical
     * disjunction (OR).
     */
    FILTERING_MODE_EXCLUSIVE
}
