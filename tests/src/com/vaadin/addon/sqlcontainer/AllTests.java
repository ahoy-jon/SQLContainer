package com.vaadin.addon.sqlcontainer;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.vaadin.addon.sqlcontainer.connection.SimpleJDBCConnectionPoolTest;
import com.vaadin.addon.sqlcontainer.query.FreeformQueryTest;
import com.vaadin.addon.sqlcontainer.query.TableQueryTest;
import com.vaadin.addon.sqlcontainer.query.generator.DefaultSQLGenerator;
import com.vaadin.addon.sqlcontainer.query.generator.MSSQLGenerator;
import com.vaadin.addon.sqlcontainer.query.generator.SQLGenerator;
import com.vaadin.addon.sqlcontainer.query.generator.SQLGeneratorsTest;

@RunWith(Suite.class)
@SuiteClasses( { SimpleJDBCConnectionPoolTest.class, FreeformQueryTest.class,
        RowIdTest.class, SQLContainerTest.class,
        SQLContainerTableQueryTest.class, ColumnPropertyTest.class,
        TableQueryTest.class, SQLGeneratorsTest.class })
public class AllTests {
    /* Set the DB used for testing here! */
    /* 0 = HSQLDB, 1 = MYSQL, 2 = POSTGRESQL, 3 = MSSQL */
    public static final int db = 3;

    /* Auto-increment column offset (HSQLDB = 0, MYSQL = 1, POSTGRES = 1) */
    public static int offset;
    /* Garbage table creation query */
    public static String createGarbage;
    /* DB Drivers, urls, usernames and passwords */
    public static String dbDriver;
    public static String dbURL;
    public static String dbUser;
    public static String dbPwd;
    /* People -test table creation statement(s) */
    public static String peopleFirst;
    public static String peopleSecond;
    /* SQL Generator used during the testing */
    public static SQLGenerator sqlGen;

    /* Set DB-specific settings based on selected DB */
    static {
        sqlGen = new DefaultSQLGenerator();
        switch (db) {
        case 0:
            offset = 0;
            createGarbage = "create table garbage (id integer generated always as identity, type varchar(32), PRIMARY KEY(id))";
            dbDriver = "org.hsqldb.jdbc.JDBCDriver";
            dbURL = "jdbc:hsqldb:mem:sqlcontainer";
            dbUser = "SA";
            dbPwd = "";
            peopleFirst = "create table people (id integer generated always as identity, name varchar(32))";
            peopleSecond = "alter table people add primary key (id)";
            break;
        case 1:
            offset = 1;
            createGarbage = "create table GARBAGE (ID integer auto_increment, type varchar(32), PRIMARY KEY(ID))";
            dbDriver = "com.mysql.jdbc.Driver";
            dbURL = "jdbc:mysql:///accounter";
            dbUser = "root";
            dbPwd = "password";
            peopleFirst = "create table PEOPLE (ID integer auto_increment not null, NAME varchar(32), primary key(ID))";
            peopleSecond = null;
            break;
        case 2:
            offset = 1;
            createGarbage = "create table GARBAGE (\"ID\" serial PRIMARY KEY, \"TYPE\" varchar(32))";
            dbDriver = "org.postgresql.Driver";
            dbURL = "jdbc:postgresql://localhost:5432/test";
            dbUser = "postgres";
            dbPwd = "postgres";
            peopleFirst = "create table PEOPLE (\"ID\" serial primary key, \"NAME\" VARCHAR(32))";
            peopleSecond = null;
            break;
        case 3:
            offset = 1;
            createGarbage = "create table GARBAGE (\"ID\" int identity(1,1) primary key, \"TYPE\" varchar(32))";
            dbDriver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            dbURL = "jdbc:sqlserver://localhost:1433;databaseName=tempdb;";
            dbUser = "sa";
            dbPwd = "sa";
            peopleFirst = "create table PEOPLE (\"ID\" int identity(1,1) primary key, \"NAME\" VARCHAR(32))";
            peopleSecond = null;
            sqlGen = new MSSQLGenerator();
            break;
        }
    }
}
