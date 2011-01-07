package com.vaadin.addon.sqlcontainer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Assert;

import com.vaadin.addon.sqlcontainer.AllTests.DB;
import com.vaadin.addon.sqlcontainer.connection.JDBCConnectionPool;

public class DataGenerator {

    public static void addPeopleToDatabase(JDBCConnectionPool connectionPool) {
        try {
            Connection conn = connectionPool.reserveConnection();
            Statement statement = conn.createStatement();
            try {
                statement.execute("drop table PEOPLE");
                if (AllTests.db == DB.ORACLE) {
                    statement.execute("drop sequence people_seq");
                }
            } catch (SQLException e) {
                // Will fail if table doesn't exist, which is OK.
                conn.rollback();
            }
            statement.execute(AllTests.peopleFirst);
            if (AllTests.peopleSecond != null) {
                statement.execute(AllTests.peopleSecond);
            }
            if (AllTests.db == DB.ORACLE) {
                statement.execute(AllTests.peopleThird);
            }
            if (AllTests.db == DB.MSSQL) {
                statement
                        .executeUpdate("insert into people values('Ville', '23')");
                statement
                        .executeUpdate("insert into people values('Kalle', '7')");
                statement
                        .executeUpdate("insert into people values('Pelle', '18')");
                statement
                        .executeUpdate("insert into people values('Börje', '64')");
            } else {
                statement
                        .executeUpdate("insert into people values(default, 'Ville', '23')");
                statement
                        .executeUpdate("insert into people values(default, 'Kalle', '7')");
                statement
                        .executeUpdate("insert into people values(default, 'Pelle', '18')");
                statement
                        .executeUpdate("insert into people values(default, 'Börje', '64')");
            }
            statement.close();
            statement = conn.createStatement();
            ResultSet rs = statement.executeQuery("select * from PEOPLE");
            Assert.assertTrue(rs.next());
            statement.close();
            conn.commit();
            connectionPool.releaseConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    public static void addFiveThousandPeople(JDBCConnectionPool connectionPool)
            throws SQLException {
        Connection conn = connectionPool.reserveConnection();
        Statement statement = conn.createStatement();
        for (int i = 4; i < 5000; i++) {
            if (AllTests.db == DB.MSSQL) {
                statement.executeUpdate("insert into people values('Person "
                        + i + "', '" + i % 99 + "')");
            } else {
                statement
                        .executeUpdate("insert into people values(default, 'Person "
                                + i + "', '" + i % 99 + "')");
            }
        }
        statement.close();
        conn.commit();
        connectionPool.releaseConnection(conn);
    }

}
