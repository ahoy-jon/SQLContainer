package com.vaadin.addon.sqlcontainer.query.generator;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.addon.sqlcontainer.RowItem;
import com.vaadin.addon.sqlcontainer.SQLContainer;
import com.vaadin.addon.sqlcontainer.connection.JDBCConnectionPool;
import com.vaadin.addon.sqlcontainer.connection.SimpleJDBCConnectionPool;
import com.vaadin.addon.sqlcontainer.query.Filter;
import com.vaadin.addon.sqlcontainer.query.OrderBy;
import com.vaadin.addon.sqlcontainer.query.TableQuery;

public class SQLGeneratorsTest {

    private JDBCConnectionPool connectionPool;

    @Before
    public void setUp() {
        try {
            connectionPool = new SimpleJDBCConnectionPool(
                    "org.hsqldb.jdbc.JDBCDriver",
                    "jdbc:hsqldb:mem:sqlcontainer", "SA", "", 2, 2);
        } catch (SQLException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
        addPeopleToDatabase();
    }

    private void addPeopleToDatabase() {
        try {
            Connection conn = connectionPool.reserveConnection();
            Statement statement = conn.createStatement();
            try {
                statement.execute("drop table people");
            } catch (SQLException e) {
                // Will fail if table doesn't exist, which is OK.
            }
            statement
                    .execute("create table people (id integer generated always as identity, name varchar(32))");
            statement.execute("alter table people add primary key (id)");
            statement
                    .executeUpdate("insert into people values(default, 'Ville')");
            statement
                    .executeUpdate("insert into people values(default, 'Kalle')");
            statement
                    .executeUpdate("insert into people values(default, 'Pelle')");
            statement
                    .executeUpdate("insert into people values(default, 'Börje')");
            statement.close();
            statement = conn.createStatement();
            ResultSet rs = statement.executeQuery("select * from people");
            Assert.assertTrue(rs.next());
            statement.close();
            conn.commit();
            connectionPool.releaseConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void generateSelectQuery_basicQuery_shouldSucceed() {
        SQLGenerator sg = new DefaultSQLGenerator();
        String query = sg.generateSelectQuery("TABLE", null, null, 0, 0, null);
        Assert.assertEquals(query, "SELECT * FROM TABLE");
    }

    @Test
    public void generateSelectQuery_pagingAndColumnsSet_shouldSucceed() {
        SQLGenerator sg = new DefaultSQLGenerator();
        String query = sg.generateSelectQuery("TABLE", null, null, 4, 8,
                "COL1, COL2, COL3");
        Assert.assertEquals(query,
                "SELECT COL1, COL2, COL3 FROM TABLE LIMIT 8 OFFSET 4");
    }

    /**
     * Note: Only tests one kind of filter and ordering.
     */
    @Test
    public void generateSelectQuery_filtersAndOrderingSet_shouldSucceed() {
        SQLGenerator sg = new DefaultSQLGenerator();
        List<Filter> f = Arrays.asList(new Filter("name",
                Filter.ComparisonType.ENDS_WITH, "lle"));
        List<OrderBy> ob = Arrays.asList(new OrderBy("name", true));
        String query = sg.generateSelectQuery("TABLE", f, ob, 0, 0, null);
        Assert.assertEquals(query,
                "SELECT * FROM TABLE WHERE name LIKE '%lle' ORDER BY name ASC");
    }

    @Test
    public void generateDeleteQuery_basicQuery_shouldSucceed()
            throws SQLException {
        SQLGenerator sg = new DefaultSQLGenerator();
        TableQuery query = new TableQuery("PEOPLE", connectionPool);
        SQLContainer container = new SQLContainer(query);

        String queryString = sg.generateDeleteQuery("PEOPLE",
                (RowItem) container.getItem(container.getItemIds().iterator()
                        .next()));
        Assert.assertEquals(queryString,
                "DELETE FROM PEOPLE WHERE ID = '0' AND NAME = 'Ville'");
    }

    @Test
    public void generateUpdateQuery_basicQuery_shouldSucceed()
            throws SQLException {
        SQLGenerator sg = new DefaultSQLGenerator();
        TableQuery query = new TableQuery("PEOPLE", connectionPool);
        SQLContainer container = new SQLContainer(query);

        RowItem ri = (RowItem) container.getItem(container.getItemIds()
                .iterator().next());
        ri.getItemProperty("NAME").setValue("Viljami");

        String queryString = sg.generateUpdateQuery("PEOPLE", ri);

        Assert.assertEquals(queryString,
                "UPDATE PEOPLE SET NAME = 'Viljami' WHERE ID = 0");
    }

    @Test
    public void generateInsertQuery_basicQuery_shouldSucceed()
            throws SQLException {
        SQLGenerator sg = new DefaultSQLGenerator();
        TableQuery query = new TableQuery("PEOPLE", connectionPool);
        SQLContainer container = new SQLContainer(query);

        RowItem ri = (RowItem) container.getItem(container.addItem());
        ri.getItemProperty("NAME").setValue("Viljami");

        String queryString = sg.generateInsertQuery("PEOPLE", ri);

        Assert.assertEquals(queryString,
                "INSERT INTO PEOPLE (NAME) VALUES ('Viljami')");
    }

    @Test
    public void generateComplexSelectQuery_forOracle_shouldSucceed()
            throws SQLException {
        SQLGenerator sg = new OracleGenerator();
        List<Filter> f = Arrays.asList(new Filter("name",
                Filter.ComparisonType.ENDS_WITH, "lle"));
        List<OrderBy> ob = Arrays.asList(new OrderBy("name", true));
        String query = sg.generateSelectQuery("TABLE", f, ob, 4, 8, "NAME, ID");
        Assert.assertEquals(query, "SELECT * FROM (SELECT ROWNUM r, * FROM "
                + "(SELECT NAME, ID FROM TABLE WHERE name LIKE '%lle' "
                + "ORDER BY name ASC) WHERE ROWNUM <= 12) WHERE ROWNUM >= 4");
    }

    @Test
    public void generateComplexSelectQuery_forMSSQL_shouldSucceed()
            throws SQLException {
        SQLGenerator sg = new MSSQLGenerator();
        List<Filter> f = Arrays.asList(new Filter("name",
                Filter.ComparisonType.ENDS_WITH, "lle"));
        List<OrderBy> ob = Arrays.asList(new OrderBy("name", true));
        String query = sg.generateSelectQuery("TABLE", f, ob, 4, 8, "NAME, ID");
        Assert.assertEquals(query, "SELECT * FROM (SELECT row_number() OVER "
                + "( ORDER BY name ASC) AS rownum, NAME, ID "
                + "FROM TABLE WHERE name LIKE '%lle') "
                + "AS a WHERE a.rownum BETWEEN 4 AND 12");
    }
}
