package com.vaadin.addon.sqlcontainer.query.generator.filter;

import com.vaadin.addon.sqlcontainer.filters.Between;
import com.vaadin.addon.sqlcontainer.query.generator.StatementHelper;
import com.vaadin.data.Container.Filter;

public class BetweenTranslator implements FilterTranslator {

    public boolean translatesFilter(Filter filter) {
        return filter instanceof Between;
    }

    public String getWhereStringForFilter(Filter filter, StatementHelper sh) {
        Between between = (Between) filter;
        sh.addParameterValue(between.getStartValue());
        sh.addParameterValue(between.getEndValue());
        return QueryBuilder.quote(between.getPropertyId())
                + " BETWEEN ? AND ?";
    }

}
