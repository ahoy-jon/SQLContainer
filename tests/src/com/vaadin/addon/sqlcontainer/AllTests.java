package com.vaadin.addon.sqlcontainer;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.vaadin.addon.sqlcontainer.connection.SimpleJDBCConnectionPoolTest;
import com.vaadin.addon.sqlcontainer.query.FreeformQueryTest;

@RunWith(Suite.class)
@SuiteClasses({ SimpleJDBCConnectionPoolTest.class, FreeformQueryTest.class })
public class AllTests {

}
