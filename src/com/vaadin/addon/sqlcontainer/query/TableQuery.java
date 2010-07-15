package com.vaadin.addon.sqlcontainer.query;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import com.vaadin.addon.sqlcontainer.query.generator.DefaultSQLGenerator;
import com.vaadin.addon.sqlcontainer.query.generator.SQLGenerator;

public class TableQuery implements QueryDelegate {

    private String tableName;
    private List<String> primaryKeyColumns;
    private String versionColumn;

    private SQLGenerator sqlGenerator;

    public TableQuery(String tableName) {
        setTableName(tableName);
        setSqlGenerator(new DefaultSQLGenerator());
    }

    public ResultSet getResults(int offset, int pagelength) {
        // TODO Auto-generated method stub
        return null;
    }

    public void setFilters(List<Filter> filters)
            throws UnsupportedOperationException {
        // TODO Auto-generated method stub

    }

    public void setOrderBy(List<OrderBy> orderBys)
            throws UnsupportedOperationException {
        // TODO Auto-generated method stub

    }

    public void storeRow(Map<String, String> columnToValueMap)
            throws UnsupportedOperationException {
        // TODO Auto-generated method stub

    }

    public void beginTransaction() throws UnsupportedOperationException {
        // TODO Auto-generated method stub

    }

    public void commit() throws UnsupportedOperationException {
        // TODO Auto-generated method stub

    }

    public void rollback() throws UnsupportedOperationException {
        // TODO Auto-generated method stub

    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setPrimaryKeyColumns(List<String> primaryKeyColumns) {
        this.primaryKeyColumns = primaryKeyColumns;
    }

    public List<String> getPrimaryKeyColumns() {
        return primaryKeyColumns;
    }

    public void setVersionColumn(String versionColumn) {
        this.versionColumn = versionColumn;
    }

    public String getVersionColumn() {
        return versionColumn;
    }

    public void setSqlGenerator(SQLGenerator sqlGenerator) {
        this.sqlGenerator = sqlGenerator;
    }

    public SQLGenerator getSqlGenerator() {
        return sqlGenerator;
    }

}
