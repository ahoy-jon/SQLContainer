package com.vaadin.addon.sqlcontainer.query.generator.filter;

import com.vaadin.addon.sqlcontainer.query.generator.StatementHelper;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.Or;

public class OrTranslator implements FilterTranslator {

    public boolean translatesFilter(Filter filter) {
        return filter instanceof Or;
    }

    public String getWhereStringForFilter(Filter filter, StatementHelper sh) {
        return QueryBuilder.group(QueryBuilder
                .getJoinedFilterString(((Or) filter).getFilters(), "OR", sh));
    }

}
