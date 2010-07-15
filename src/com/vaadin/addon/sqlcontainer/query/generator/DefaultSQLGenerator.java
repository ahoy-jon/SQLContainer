package com.vaadin.addon.sqlcontainer.query.generator;

import java.util.List;
import java.util.Map;

import com.vaadin.addon.sqlcontainer.query.Filter;
import com.vaadin.addon.sqlcontainer.query.OrderBy;

/**
 * Generates generic SQL that is supported by HSQLDB, MySQL and PostgreSQL.
 * 
 * @author Jonatan Kronqvist / IT Mill Ltd
 */
public class DefaultSQLGenerator implements SQLGenerator {

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
