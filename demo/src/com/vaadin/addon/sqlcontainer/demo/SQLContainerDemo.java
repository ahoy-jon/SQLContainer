package com.vaadin.addon.sqlcontainer.demo;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.Application;
import com.vaadin.addon.sqlcontainer.RowId;
import com.vaadin.addon.sqlcontainer.SQLContainer;
import com.vaadin.addon.sqlcontainer.connection.SimpleJDBCConnectionPool;
import com.vaadin.addon.sqlcontainer.query.Filter;
import com.vaadin.addon.sqlcontainer.query.OrderBy;
import com.vaadin.addon.sqlcontainer.query.TableQuery;
import com.vaadin.addon.sqlcontainer.query.Filter.ComparisonType;
import com.vaadin.addon.sqlcontainer.query.generator.DefaultSQLGenerator;
import com.vaadin.addon.sqlcontainer.query.generator.MSSQLGenerator;
import com.vaadin.addon.sqlcontainer.query.generator.OracleGenerator;
import com.vaadin.data.Container;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;

public class SQLContainerDemo extends Application implements Serializable {
    private static final long serialVersionUID = -632539823251517114L;
    private SimpleJDBCConnectionPool sjcp = null;
    private Window mainWindow;
    private TableQuery tq;

    private Table t;

    @Override
    public void init() {
        mainWindow = new Window("SQLContainer test");

        final SQLContainer cr = sqlContainerTesting();

        cr.setPageLength(18);
        cr.setAutoCommit(false);
        tableTesting(cr);

        // testQueryGenerators();

        setMainWindow(mainWindow);

        Button b = new Button("Refresh Container");
        b.addListener(new Button.ClickListener() {
            private static final long serialVersionUID = -5208763132530422234L;

            public void buttonClick(ClickEvent event) {
                cr.refresh();
                /*
                 * try { tq.beginTransaction(); } catch (Exception e) {
                 * e.printStackTrace(); }
                 */
            }
        });
        mainWindow.addComponent(b);

        final TextField p = new TextField("Page length");
        Button setpl = new Button("Set!");
        setpl.addListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                cr.setPageLength(Integer.parseInt((String) p.getValue()));
            }
        });
        mainWindow.addComponent(p);
        mainWindow.addComponent(setpl);

        Button remove = new Button("Remove selected item!");
        remove.addListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                Object o = t.getValue();
                if (o != null && o instanceof RowId) {
                    System.err.println(cr.removeItem(o));
                }
            }
        });
        mainWindow.addComponent(remove);

        Button commit = new Button("Commit changes!");
        commit.addListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                try {
                    cr.commit();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        mainWindow.addComponent(commit);
    }

    private SQLContainer sqlContainerTesting() {
        try {
            sjcp = new SimpleJDBCConnectionPool("com.mysql.jdbc.Driver",
                    "jdbc:mysql://localhost:3306/database", "user", "password",
                    2, 5);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        tq = new TableQuery("users", sjcp, new DefaultSQLGenerator());
        SQLContainer sqlc = null;

        // ResultSet r = null;
        try {
            sqlc = new SQLContainer(tq);
            // r = tq.getIdList();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sqlc;
    }

    private void tableTesting(Container c) {
        IndexedContainer ic = new IndexedContainer();
        ic.addContainerProperty("name", String.class, "testing");
        for (int i = 0; i < 2000; i++) {
            ic.addItem(i);
        }

        t = new Table();
        t.setHeight("100%");
        t.setSelectable(true);
        t.setPageLength(3);
        t.setCacheRate(1);
        if (c == null) {
            t.setContainerDataSource(ic);
        } else {
            t.setContainerDataSource(c);
        }

        mainWindow.addComponent(t);
        ((VerticalLayout) mainWindow.getContent()).setExpandRatio(t, 1);
    }

    private void testQueryGenerators() {
        DefaultSQLGenerator dsg = new DefaultSQLGenerator();
        OracleGenerator og = new OracleGenerator();
        MSSQLGenerator mg = new MSSQLGenerator();

        dsg.setSearchStringEscape("\\");
        og.setSearchStringEscape("\\");
        mg.setSearchStringEscape("\\");

        String tname = "taulu";
        List<Filter> filters = new ArrayList<Filter>(3);
        filters.add(new Filter("ekacol", ComparisonType.CONTAINS, "ha%ku"));
        filters.add(new Filter("tokacol", ComparisonType.GREATER_OR_EQUAL,
                "ha__k"));
        filters.add(new Filter("kolmascol", ComparisonType.ENDS_WITH, "ha%"));
        List<OrderBy> orderBys = new ArrayList<OrderBy>(2);
        orderBys.add(new OrderBy("ekacol", true));
        orderBys.add(new OrderBy("tokacol", false));

        Map<String, String> valMap = new HashMap<String, String>();
        valMap.put("eka", "ekaVal");
        valMap.put("toka", "tokaVal");
        valMap.put("kolmas", "kolmasVal");
        Map<String, String> idMap = new HashMap<String, String>();
        idMap.put("id", "234");
        idMap.put("ik", "567");

        System.out.println("Selects:");
        System.out.println("D: "
                + dsg.generateSelectQuery(tname, filters, orderBys, 15, 25,
                        null));
        System.out.println("O: "
                + og
                        .generateSelectQuery(tname, filters, orderBys, 15, 25,
                                null));
        System.out.println("M: "
                + mg
                        .generateSelectQuery(tname, filters, orderBys, 15, 25,
                                null));

        System.out.println("Inserts:");
        // System.out.println("D: " + dsg.generateInsertQuery(tname, valMap));
        // System.out.println("O: " + og.generateInsertQuery(tname, valMap));
        // System.out.println("M: " + mg.generateInsertQuery(tname, valMap));

        System.out.println("Updates:");
        // System.out.println("D: "
        // + dsg.generateUpdateQuery(tname, valMap, idMap));
        // System.out
        // .println("O: " + og.generateUpdateQuery(tname, valMap, idMap));
        // System.out
        // .println("M: " + mg.generateUpdateQuery(tname, valMap, idMap));
    }
}
