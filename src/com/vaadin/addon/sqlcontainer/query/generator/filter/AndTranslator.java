package com.vaadin.addon.sqlcontainer.query.generator.filter;

import com.vaadin.addon.sqlcontainer.query.generator.StatementHelper;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.And;

public class AndTranslator implements FilterTranslator {

    public boolean translatesFilter(Filter filter) {
        return filter instanceof And;
    }

    public String getWhereStringForFilter(Filter filter, StatementHelper sh) {
        return QueryBuilder.group(QueryBuilder
                .getJoinedFilterString(((And) filter).getFilters(), "AND", sh));
    }

}
