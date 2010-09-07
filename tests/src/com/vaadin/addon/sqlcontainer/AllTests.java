package com.vaadin.addon.sqlcontainer;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.vaadin.addon.sqlcontainer.connection.SimpleJDBCConnectionPoolTest;
import com.vaadin.addon.sqlcontainer.query.FreeformQueryTest;
import com.vaadin.addon.sqlcontainer.query.TableQueryTest;
import com.vaadin.addon.sqlcontainer.query.generator.SQLGeneratorsTest;

@RunWith(Suite.class)
@SuiteClasses( { SimpleJDBCConnectionPoolTest.class, FreeformQueryTest.class,
        RowIdTest.class, SQLContainerTest.class, ColumnPropertyTest.class,
        TableQueryTest.class, SQLGeneratorsTest.class })
public class AllTests {

}
