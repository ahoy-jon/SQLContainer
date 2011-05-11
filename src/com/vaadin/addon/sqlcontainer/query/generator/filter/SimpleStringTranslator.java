package com.vaadin.addon.sqlcontainer.query.generator.filter;

import com.vaadin.addon.sqlcontainer.filters.Like;
import com.vaadin.addon.sqlcontainer.query.generator.StatementHelper;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.SimpleStringFilter;

public class SimpleStringTranslator implements FilterTranslator {

    public boolean translatesFilter(Filter filter) {
        return filter instanceof SimpleStringFilter;
    }

    public String getWhereStringForFilter(Filter filter, StatementHelper sh) {
        SimpleStringFilter ssf = (SimpleStringFilter) filter;
        // Create a Like filter based on the SimpleStringFilter and execute the
        // LikeTranslator
        String likeStr = ssf.isOnlyMatchPrefix() ? ssf.getFilterString() + "%"
                : "%" + ssf.getFilterString() + "%";
        Like like = new Like(ssf.getPropertyId().toString(), likeStr);
        like.setCaseSensitive(!ssf.isIgnoreCase());
        return new LikeTranslator().getWhereStringForFilter(like, sh);
    }

}
