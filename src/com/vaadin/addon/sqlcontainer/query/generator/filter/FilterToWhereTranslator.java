package com.vaadin.addon.sqlcontainer.query.generator.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.vaadin.addon.sqlcontainer.query.generator.StatementHelper;
import com.vaadin.data.Container.Filter;

public class FilterToWhereTranslator {

    private static ArrayList<FilterTranslator> filterTranslators = new ArrayList<FilterTranslator>();

    static {
        /* Register all default filter translators */
        addFilterTranslator(new AndTranslator());
        addFilterTranslator(new OrTranslator());
        addFilterTranslator(new LikeTranslator());
        addFilterTranslator(new BetweenTranslator());
        addFilterTranslator(new CompareTranslator());
        addFilterTranslator(new NotTranslator());
        addFilterTranslator(new IsNullTranslator());
        addFilterTranslator(new SimpleStringTranslator());
    }

    public synchronized static void addFilterTranslator(
            FilterTranslator translator) {
        filterTranslators.add(translator);
    }

    protected static String quote(Object str) {
        return "\"" + str + "\"";
    }

    protected static String parens(String str) {
        return "(" + str + ")";
    }

    /**
     * Constructs and returns a string representing
     * 
     * @param filter
     * @param sh
     * @return
     */
    public synchronized static String getWhereStringForFilter(Filter filter,
            StatementHelper sh) {
        for (FilterTranslator ft : filterTranslators) {
            if (ft.translatesFilter(filter)) {
                return ft.getWhereStringForFilter(filter, sh);
            }
        }
        return "";
    }

    public static String getJoinedFilterString(Collection<Filter> filters,
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
