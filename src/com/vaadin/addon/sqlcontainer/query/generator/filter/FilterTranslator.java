package com.vaadin.addon.sqlcontainer.query.generator.filter;

import com.vaadin.addon.sqlcontainer.query.generator.StatementHelper;
import com.vaadin.data.Container.Filter;

public interface FilterTranslator {
    public boolean translatesFilter(Filter filter);

    public String getWhereStringForFilter(Filter filter, StatementHelper sh);

}
