package com.vaadin.addon.sqlcontainer;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.vaadin.addon.sqlcontainer.connection.SimpleJDBCConnectionPoolTest;
import com.vaadin.addon.sqlcontainer.query.FreeformQueryTest;
import com.vaadin.addon.sqlcontainer.query.TableQueryTest;
import com.vaadin.addon.sqlcontainer.query.generator.DefaultSQLGenerator;
import com.vaadin.addon.sqlcontainer.query.generator.MSSQLGenerator;
import com.vaadin.addon.sqlcontainer.query.generator.OracleGenerator;
import com.vaadin.addon.sqlcontainer.query.generator.SQLGenerator;
import com.vaadin.addon.sqlcontainer.query.generator.SQLGeneratorsTest;

@RunWith(Suite.class)
@SuiteClasses({ SimpleJDBCConnectionPoolTest.class, FreeformQueryTest.class,
        RowIdTest.class, SQLContainerTest.class,
        SQLContainerTableQueryTest.class, ColumnPropertyTest.class,
        TableQueryTest.class, SQLGeneratorsTest.class, UtilTest.class,
        TicketTests.class })
public class AllTests {
    /* Set the DB used for testing here! */
    public enum DB {
        HSQLDB, MYSQL, POSTGRESQL, MSSQL, ORACLE;
    }

    /* 0 = HSQLDB, 1 = MYSQL, 2 = POSTGRESQL, 3 = MSSQL, 4 = ORACLE */
    public static final DB db = DB.HSQLDB;

    /* Auto-increment column offset (HSQLDB = 0, MYSQL = 1, POSTGRES = 1) */
    public static int offset;
    /* Garbage table creation query (=three queries for oracle) */
    public static String createGarbage;
    public static String createGarbageSecond;
    public static String createGarbageThird;
    /* DB Drivers, urls, usernames and passwords */
    public static String dbDriver;
    public static String dbURL;
    public static String dbUser;
    public static String dbPwd;
    /* People -test table creation statement(s) */
    public static String peopleFirst;
    public static String peopleSecond;
    public static String peopleThird;
    /* SQL Generator used during the testing */
    public static SQLGenerator sqlGen;

    /* Set DB-specific settings based on selected DB */
    static {
        sqlGen = new DefaultSQLGenerator();
        switch (db) {
        case HSQLDB:
            offset = 0;
            createGarbage = "create table garbage (id integer generated always as identity, type varchar(32), PRIMARY KEY(id))";
            dbDriver = "org.hsqldb.jdbc.JDBCDriver";
            dbURL = "jdbc:hsqldb:mem:sqlcontainer";
            dbUser = "SA";
            dbPwd = "";
            peopleFirst = "create table people (id integer generated always as identity, name varchar(32), AGE INTEGER)";
            peopleSecond = "alter table people add primary key (id)";
            break;
        case MYSQL:
            offset = 1;
            createGarbage = "create table GARBAGE (ID integer auto_increment, type varchar(32), PRIMARY KEY(ID))";
            dbDriver = "com.mysql.jdbc.Driver";
            dbURL = "jdbc:mysql:///sqlcontainer";
            dbUser = "sqlcontainer";
            dbPwd = "sqlcontainer";
            peopleFirst = "create table PEOPLE (ID integer auto_increment not null, NAME varchar(32), AGE INTEGER, primary key(ID))";
            peopleSecond = null;
            break;
        case POSTGRESQL:
            offset = 1;
            createGarbage = "create table GARBAGE (\"ID\" serial PRIMARY KEY, \"TYPE\" varchar(32))";
            dbDriver = "org.postgresql.Driver";
            dbURL = "jdbc:postgresql://localhost:5432/test";
            dbUser = "postgres";
            dbPwd = "postgres";
            peopleFirst = "create table PEOPLE (\"ID\" serial primary key, \"NAME\" VARCHAR(32), \"AGE\" INTEGER)";
            peopleSecond = null;
            break;
        case MSSQL:
            offset = 1;
            createGarbage = "create table GARBAGE (\"ID\" int identity(1,1) primary key, \"TYPE\" varchar(32))";
            dbDriver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            dbURL = "jdbc:sqlserver://localhost:1433;databaseName=tempdb;";
            dbUser = "sa";
            dbPwd = "sa";
            peopleFirst = "create table PEOPLE (\"ID\" int identity(1,1) primary key, \"NAME\" VARCHAR(32), \"AGE\" INTEGER)";
            peopleSecond = null;
            sqlGen = new MSSQLGenerator();
            break;
        case ORACLE:
            offset = 1;
            createGarbage = "create table GARBAGE (\"ID\" integer primary key, \"TYPE\" varchar2(32))";
            createGarbageSecond = "create sequence garbage_seq start with 1 increment by 1 nomaxvalue";
            createGarbageThird = "create trigger garbage_trigger before insert on GARBAGE for each row begin select garbage_seq.nextval into :new.ID from dual; end;";
            dbDriver = "oracle.jdbc.OracleDriver";
            dbURL = "jdbc:oracle:thin:test/test@localhost:1521:XE";
            dbUser = "test";
            dbPwd = "test";
            peopleFirst = "create table PEOPLE (\"ID\" integer primary key, \"NAME\" VARCHAR2(32), \"AGE\" INTEGER)";
            peopleSecond = "create sequence people_seq start with 1 increment by 1 nomaxvalue";
            peopleThird = "create trigger people_trigger before insert on PEOPLE for each row begin select people_seq.nextval into :new.ID from dual; end;";
            sqlGen = new OracleGenerator();
            break;
        }
    }
}
