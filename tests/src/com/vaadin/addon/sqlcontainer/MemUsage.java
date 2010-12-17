package com.vaadin.addon.sqlcontainer;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.addon.sqlcontainer.connection.JDBCConnectionPool;
import com.vaadin.addon.sqlcontainer.connection.SimpleJDBCConnectionPool;
import com.vaadin.addon.sqlcontainer.query.TableQuery;

public class MemUsage {

    private JDBCConnectionPool pool;

    static class DebugUtils {
        private static class ByteCountNullOutputStream extends OutputStream
                implements Serializable {
            private static final long serialVersionUID = 4220043426041762877L;
            private long bytes;

            @Override
            public void write(int b) {
                bytes++;
            }

            public long getBytes() {
                return bytes;
            }
        }

        public static long getSize(Object object) {
            ByteCountNullOutputStream os = new ByteCountNullOutputStream();
            ObjectOutputStream oos;
            try {
                oos = new ObjectOutputStream(os);
                oos.writeObject(object);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return os.getBytes();
        }
    }

    private long getUsedMemory() {
        return Runtime.getRuntime().totalMemory()
                - Runtime.getRuntime().freeMemory();
    }

    private String getRandonName() {
        final String[] tokens = new String[] { "sa", "len", "da", "vid", "ma",
                "ry", "an", "na", "jo", "bri", "son", "mat", "e", "ric", "ge",
                "eu", "han", "har", "ri", "ja", "lo" };
        StringBuffer sb = new StringBuffer();
        int len = (int) (Math.random() * 3 + 2);
        while (len-- > 0) {
            sb.append(tokens[(int) (Math.random() * tokens.length)]);
        }
        return Character.toUpperCase(sb.charAt(0)) + sb.toString().substring(1);
    }

    @Before
    public void setUp() throws SQLException {
        pool = new SimpleJDBCConnectionPool(AllTests.dbDriver, AllTests.dbURL,
                AllTests.dbUser, AllTests.dbPwd, 2, 2);
        Connection conn = pool.reserveConnection();
        Statement statement = conn.createStatement();
        try {
            statement.execute("drop table AUTHOR");
        } catch (SQLException e) {
            // ignore
        }
        statement
                .execute("create table AUTHOR (ID integer auto_increment not null,"
                        + " LAST_NAME varchar(40), FIRST_NAMES varchar(80), primary key(ID))");
        pool.releaseConnection(conn);
    }

    @Test
    public void tableQuery_manyInserts_shouldNotLeakMemory()
            throws SQLException {
        int cents = 1000;
        long usedMemory = getUsedMemory();
        for (int cent = 0; cent < cents; cent++) {
            TableQuery q = new TableQuery("AUTHOR", pool);
            q.setVersionColumn("ID");
            SQLContainer container = new SQLContainer(q);
            for (int i = 0; i < 100; i++) {
                Object id = container.addItem();
                container.getContainerProperty(id, "FIRST_NAMES").setValue(
                        getRandonName());
                container.getContainerProperty(id, "LAST_NAME").setValue(
                        getRandonName());
            }
            container.commit();
            System.out.println("Container size: "
                    + DebugUtils.getSize(container));
            long mem = getUsedMemory();
            long delta = mem - usedMemory;
            System.out.println(String.format("%d bytes used, delta: %d", mem,
                    delta));
        }
        System.out.println("end");
    }
}
