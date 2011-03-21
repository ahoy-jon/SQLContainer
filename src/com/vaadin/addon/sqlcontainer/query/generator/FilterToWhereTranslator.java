package com.vaadin.addon.sqlcontainer.query.generator;

import java.util.Collection;
import java.util.List;

import com.vaadin.addon.sqlcontainer.filters.Between;
import com.vaadin.addon.sqlcontainer.filters.Like;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.And;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.data.util.filter.Or;

public class FilterToWhereTranslator {

    private static String quote(Object str) {
        return "\"" + str + "\"";
    }

    private static String parens(String str) {
        return "(" + str + ")";
    }

    public static String getWhereStringForFilter(Filter filter,
            StatementHelper sh) {
        // Handle compound filters
        if (filter instanceof And) {
            return parens(getJoinedFilterString(((And) filter).getFilters(),
                    "AND", sh));
        }
        if (filter instanceof Or) {
            return parens(getJoinedFilterString(((Or) filter).getFilters(),
                    "OR", sh));
        }

        if (filter instanceof Like) {
            Like like = (Like) filter;
            if (like.isCaseSensitive()) {
                sh.addParameterValue(like.getValue());
                return quote(like.getPropertyId()) + " LIKE ?";
            } else {
                sh.addParameterValue(like.getValue().toUpperCase());
                return "UPPER(" + quote(like.getPropertyId()) + ") LIKE ?";
            }
        }

        if (filter instanceof Between) {
            Between between = (Between) filter;
            sh.addParameterValue(between.getStartValue());
            sh.addParameterValue(between.getEndValue());
            return quote(between.getPropertyId()) + " BETWEEN ? AND ?";
        }

        if (filter instanceof Compare) {
            Compare compare = (Compare) filter;
            sh.addParameterValue(compare.getValue());
            String prop = quote(compare.getPropertyId());
            switch (compare.getOperation()) {
            case EQUAL:
                return prop + " = ?";
            case GREATER:
                return prop + " > ?";
            case GREATER_OR_EQUAL:
                return prop + " >= ?";
            case LESS:
                return prop + " < ?";
            case LESS_OR_EQUAL:
                return prop + " <= ?";
            }
        }

        return "";
    }

    private static String getJoinedFilterString(Collection<Filter> filters,
            String joinString, StatementHelper sh) {
        StringBuilder result = new StringBuilder();
        for (Filter f : filters) {
            result.append(getWhereStringForFilter(f, sh));
            result.append(" ").append(joinString).append(" ");
        }
        // Remove the last instance of joinString
        result.delete(result.length() - joinString.length() - 2,
                result.length());
        return result.toString();
    }

    public static String getWhereStringForFilters(List<Filter> filters,
            StatementHelper sh) {
        if (filters == null || filters.isEmpty()) {
            return "";
        }
        StringBuilder where = new StringBuilder(" WHERE ");
        where.append(getJoinedFilterString(filters, "AND", sh));
        return where.toString();
    }
}
