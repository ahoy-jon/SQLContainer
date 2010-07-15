package com.vaadin.addon.sqlcontainer.query.generator;

import java.util.List;
import java.util.Map;

import com.vaadin.addon.sqlcontainer.query.Filter;
import com.vaadin.addon.sqlcontainer.query.OrderBy;

public class OracleGenerator implements SQLGenerator {

    public String generateSelectQuery(List<Filter> filters,
            List<OrderBy> orderBys, int offset, int pagelength) {
        // TODO Auto-generated method stub
        return null;
    }

    public String generateUpdateQuery(Map<String, String> columnToValueMap,
            Map<String, String> rowIdentifiers) {
        // TODO Auto-generated method stub
        return null;
    }

    public String generateInsertQuery(Map<String, String> columnToValueMap) {
        // TODO Auto-generated method stub
        return null;
    }

}
