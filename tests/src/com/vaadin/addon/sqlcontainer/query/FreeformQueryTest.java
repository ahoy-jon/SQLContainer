package com.vaadin.addon.sqlcontainer.query;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.addon.sqlcontainer.RowId;
import com.vaadin.addon.sqlcontainer.RowItem;
import com.vaadin.addon.sqlcontainer.SQLContainer;
import com.vaadin.addon.sqlcontainer.connection.JDBCConnectionPool;
import com.vaadin.addon.sqlcontainer.connection.SimpleJDBCConnectionPool;
import com.vaadin.addon.sqlcontainer.query.Filter.ComparisonType;

public class FreeformQueryTest {

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
    public void construction_legalParameters_shouldSucceed() {
        FreeformQuery ffQuery = new FreeformQuery("SELECT * FROM foo",
                Arrays.asList("id"), connectionPool);
        Assert.assertArrayEquals(new Object[] { "id" }, ffQuery
                .getPrimaryKeyColumns().toArray());

        Assert.assertEquals("SELECT * FROM foo", ffQuery.getQueryString());
    }

    @Test
    public void construction_emptyQueryString_shouldSucceed() {
        FreeformQuery ffQuery = new FreeformQuery("", Arrays.asList("id"),
                connectionPool);
        Assert.assertEquals("", ffQuery.getQueryString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void construction_nullPrimaryKeys_shouldFail() {
        new FreeformQuery("SELECT * FROM foo", null, connectionPool);
    }

    @Test(expected = IllegalArgumentException.class)
    public void construction_emptyPrimaryKeys_shouldFail() {
        new FreeformQuery("SELECT * FROM foo", new ArrayList<String>(),
                connectionPool);
    }

    @Test(expected = IllegalArgumentException.class)
    public void construction_emptyStringsInPrimaryKeys_shouldFail() {
        new FreeformQuery("SELECT * FROM foo", Arrays.asList(""),
                connectionPool);
    }

    @Test
    public void getCount_simpleQuery_returnsFour() throws SQLException {
        FreeformQuery query = new FreeformQuery("SELECT * FROM people",
                Arrays.asList("id"), connectionPool);
        Assert.assertEquals(4, query.getCount());
    }

    @Test
    public void getCount_simpleQueryTwoMorePeopleAdded_returnsSix()
            throws SQLException {
        // Add some people
        Connection conn = connectionPool.reserveConnection();
        Statement statement = conn.createStatement();
        statement.executeUpdate("insert into people values(default, 'Bengt')");
        statement.executeUpdate("insert into people values(default, 'Ingvar')");
        statement.close();
        conn.commit();
        connectionPool.releaseConnection(conn);

        FreeformQuery query = new FreeformQuery("SELECT * FROM people",
                Arrays.asList("id"), connectionPool);

        Assert.assertEquals(6, query.getCount());
    }

    @Test
    public void getCount_moreComplexQuery_returnsThree() throws SQLException {
        FreeformQuery query = new FreeformQuery(
                "SELECT * FROM people WHERE name LIKE '%lle'",
                Arrays.asList("id"), connectionPool);
        Assert.assertEquals(3, query.getCount());
    }

    @Test
    public void getResults_simpleQuery_returnsFourRecords() throws SQLException {
        FreeformQuery query = new FreeformQuery("SELECT id,name FROM people",
                Arrays.asList("id"), connectionPool);
        ResultSet rs = query.getResults(0, 0);

        Assert.assertTrue(rs.next());
        Assert.assertEquals(0, rs.getInt(1));
        Assert.assertEquals("Ville", rs.getString(2));

        Assert.assertTrue(rs.next());
        Assert.assertEquals(1, rs.getInt(1));
        Assert.assertEquals("Kalle", rs.getString(2));

        Assert.assertTrue(rs.next());
        Assert.assertEquals(2, rs.getInt(1));
        Assert.assertEquals("Pelle", rs.getString(2));

        Assert.assertTrue(rs.next());
        Assert.assertEquals(3, rs.getInt(1));
        Assert.assertEquals("Börje", rs.getString(2));

        Assert.assertFalse(rs.next());
    }

    @Test
    public void getResults_moreComplexQuery_returnsThreeRecords()
            throws SQLException {
        FreeformQuery query = new FreeformQuery(
                "SELECT * FROM people WHERE name LIKE '%lle'",
                Arrays.asList("id"), connectionPool);
        ResultSet rs = query.getResults(0, 0);

        Assert.assertTrue(rs.next());
        Assert.assertEquals(0, rs.getInt(1));
        Assert.assertEquals("Ville", rs.getString(2));

        Assert.assertTrue(rs.next());
        Assert.assertEquals(1, rs.getInt(1));
        Assert.assertEquals("Kalle", rs.getString(2));

        Assert.assertTrue(rs.next());
        Assert.assertEquals(2, rs.getInt(1));
        Assert.assertEquals("Pelle", rs.getString(2));

        Assert.assertFalse(rs.next());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setFilters_noDelegate_shouldFail() {
        FreeformQuery query = new FreeformQuery("SELECT * FROM people",
                Arrays.asList("id"), connectionPool);
        query.setFilters(Arrays.asList(new Filter("name",
                Filter.ComparisonType.LIKE, "%lle")));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setOrderBy_noDelegate_shouldFail() {
        FreeformQuery query = new FreeformQuery("SELECT * FROM people",
                Arrays.asList("id"), connectionPool);
        query.setOrderBy(Arrays.asList(new OrderBy("name", true)));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void storeRow_noDelegate_shouldFail() throws SQLException {
        FreeformQuery query = new FreeformQuery("SELECT * FROM people",
                Arrays.asList("id"), connectionPool);
        query.storeRow(new RowItem(new SQLContainer(query), new RowId(
                new Object[] { 1 }), null));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void removeRow_noDelegate_shouldFail() throws SQLException {
        FreeformQuery query = new FreeformQuery("SELECT * FROM people",
                Arrays.asList("id"), connectionPool);
        query.removeRow(new RowItem(new SQLContainer(query), new RowId(
                new Object[] { 1 }), null));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void beginTransaction_readOnly_shouldFail() throws SQLException {
        FreeformQuery query = new FreeformQuery("SELECT * FROM people",
                Arrays.asList("id"), connectionPool);
        query.beginTransaction();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void commit_readOnly_shouldFail() throws SQLException {
        FreeformQuery query = new FreeformQuery("SELECT * FROM people",
                Arrays.asList("id"), connectionPool);
        query.commit();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void rollback_readOnly_shouldFail() throws SQLException {
        FreeformQuery query = new FreeformQuery("SELECT * FROM people",
                Arrays.asList("id"), connectionPool);
        query.rollback();
    }

    // -------- Tests with a delegate ---------

    @Test
    public void setDelegate_noExistingDelegate_shouldRegisterNewDelegate() {
        FreeformQuery query = new FreeformQuery("SELECT * FROM people",
                Arrays.asList("id"), connectionPool);
        FreeformQueryDelegate delegate = EasyMock
                .createMock(FreeformQueryDelegate.class);
        query.setDelegate(delegate);
        Assert.assertEquals(delegate, query.getDelegate());
    }

    @Test
    public void getResults_hasDelegate_shouldCallDelegate() throws SQLException {
        FreeformQuery query = new FreeformQuery("SELECT * FROM people",
                Arrays.asList("id"), connectionPool);
        FreeformQueryDelegate delegate = EasyMock
                .createMock(FreeformQueryDelegate.class);
        EasyMock.expect(delegate.getQueryString(0, 2)).andReturn(
                "SELECT * FROM people LIMIT 2 OFFSET 0");
        EasyMock.replay(delegate);

        query.setDelegate(delegate);
        query.getResults(0, 2);
        EasyMock.verify(delegate);
    }

    @Test
    public void getResults_delegateImplementsGetQueryString_shouldHonorOffsetAndPagelength()
            throws SQLException {
        FreeformQuery query = new FreeformQuery("SELECT * FROM people",
                Arrays.asList("id"), connectionPool);
        FreeformQueryDelegate delegate = EasyMock
                .createMock(FreeformQueryDelegate.class);
        EasyMock.expect(delegate.getQueryString(0, 2)).andReturn(
                "SELECT * FROM people LIMIT 2 OFFSET 0");
        EasyMock.replay(delegate);
        query.setDelegate(delegate);

        ResultSet rs = query.getResults(0, 2);
        Assert.assertTrue(rs.next());
        Assert.assertEquals(0, rs.getInt(1));
        Assert.assertEquals("Ville", rs.getString(2));

        Assert.assertTrue(rs.next());
        Assert.assertEquals(1, rs.getInt(1));
        Assert.assertEquals("Kalle", rs.getString(2));

        Assert.assertFalse(rs.next());

        EasyMock.verify(delegate);
    }

    @Test
    public void setFilters_delegateImplementsSetFilters_shouldPassFiltersToDelegate() {
        FreeformQuery query = new FreeformQuery("SELECT * FROM people",
                Arrays.asList("id"), connectionPool);
        FreeformQueryDelegate delegate = EasyMock
                .createMock(FreeformQueryDelegate.class);
        List<Filter> filters = Arrays.asList(new Filter("name",
                ComparisonType.LIKE, "%lle"));
        delegate.setFilters(filters);
        EasyMock.replay(delegate);
        query.setDelegate(delegate);

        query.setFilters(filters);

        EasyMock.verify(delegate);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setFilters_delegateDoesNotImplementSetFilters_shouldFail() {
        FreeformQuery query = new FreeformQuery("SELECT * FROM people",
                Arrays.asList("id"), connectionPool);
        FreeformQueryDelegate delegate = EasyMock
                .createMock(FreeformQueryDelegate.class);
        List<Filter> filters = Arrays.asList(new Filter("name",
                ComparisonType.LIKE, "%lle"));
        delegate.setFilters(filters);
        EasyMock.expectLastCall().andThrow(new UnsupportedOperationException());
        EasyMock.replay(delegate);
        query.setDelegate(delegate);

        query.setFilters(filters);

        EasyMock.verify(delegate);
    }

    @Test
    public void setOrderBy_delegateImplementsSetOrderBy_shouldPassArgumentsToDelegate() {
        FreeformQuery query = new FreeformQuery("SELECT * FROM people",
                Arrays.asList("id"), connectionPool);
        FreeformQueryDelegate delegate = EasyMock
                .createMock(FreeformQueryDelegate.class);
        List<OrderBy> orderBys = Arrays.asList(new OrderBy("name", false));
        delegate.setOrderBy(orderBys);
        EasyMock.replay(delegate);
        query.setDelegate(delegate);

        query.setOrderBy(orderBys);

        EasyMock.verify(delegate);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setOrderBy_delegateDoesNotImplementSetOrderBy_shouldFail() {
        FreeformQuery query = new FreeformQuery("SELECT * FROM people",
                Arrays.asList("id"), connectionPool);
        FreeformQueryDelegate delegate = EasyMock
                .createMock(FreeformQueryDelegate.class);
        List<OrderBy> orderBys = Arrays.asList(new OrderBy("name", false));
        delegate.setOrderBy(orderBys);
        EasyMock.expectLastCall().andThrow(new UnsupportedOperationException());
        EasyMock.replay(delegate);
        query.setDelegate(delegate);

        query.setOrderBy(orderBys);

        EasyMock.verify(delegate);
    }

    @Test
    public void setFilters_noDelegateAndNullParameter_shouldSucceed() {
        FreeformQuery query = new FreeformQuery("SELECT * FROM people",
                Arrays.asList("id"), connectionPool);
        query.setFilters(null);
    }

    @Test
    public void setOrderBy_noDelegateAndNullParameter_shouldSucceed() {
        FreeformQuery query = new FreeformQuery("SELECT * FROM people",
                Arrays.asList("id"), connectionPool);
        query.setOrderBy(null);
    }

    @Test
    public void storeRow_delegateImplementsStoreRow_shouldPassToDelegate()
            throws SQLException {
        FreeformQuery query = new FreeformQuery("SELECT * FROM people",
                Arrays.asList("id"), connectionPool);
        FreeformQueryDelegate delegate = EasyMock
                .createMock(FreeformQueryDelegate.class);
        RowItem row = new RowItem(new SQLContainer(query), new RowId(
                new Object[] { 1 }), null);
        EasyMock.expect(delegate.storeRow(row)).andReturn(1);
        EasyMock.replay(delegate);
        query.setDelegate(delegate);

        query.storeRow(row);

        EasyMock.verify(delegate);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void storeRow_delegateDoesNotImplementStoreRow_shouldFail()
            throws SQLException {
        FreeformQuery query = new FreeformQuery("SELECT * FROM people",
                Arrays.asList("id"), connectionPool);
        FreeformQueryDelegate delegate = EasyMock
                .createMock(FreeformQueryDelegate.class);
        RowItem row = new RowItem(new SQLContainer(query), new RowId(
                new Object[] { 1 }), null);
        EasyMock.expect(delegate.storeRow(row)).andThrow(
                new UnsupportedOperationException());
        EasyMock.replay(delegate);
        query.setDelegate(delegate);

        query.storeRow(row);

        EasyMock.verify(delegate);
    }

    @Test
    public void removeRow_delegateImplementsRemoveRow_shouldPassToDelegate()
            throws SQLException {
        FreeformQuery query = new FreeformQuery("SELECT * FROM people",
                Arrays.asList("id"), connectionPool);
        FreeformQueryDelegate delegate = EasyMock
                .createMock(FreeformQueryDelegate.class);
        RowItem row = new RowItem(new SQLContainer(query), new RowId(
                new Object[] { 1 }), null);
        EasyMock.expect(delegate.removeRow(row)).andReturn(true);
        EasyMock.replay(delegate);
        query.setDelegate(delegate);

        query.removeRow(row);

        EasyMock.verify(delegate);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void removeRow_delegateDoesNotImplementRemoveRow_shouldFail()
            throws SQLException {
        FreeformQuery query = new FreeformQuery("SELECT * FROM people",
                Arrays.asList("id"), connectionPool);
        FreeformQueryDelegate delegate = EasyMock
                .createMock(FreeformQueryDelegate.class);
        RowItem row = new RowItem(new SQLContainer(query), new RowId(
                new Object[] { 1 }), null);
        EasyMock.expect(delegate.removeRow(row)).andThrow(
                new UnsupportedOperationException());
        EasyMock.replay(delegate);
        query.setDelegate(delegate);

        query.removeRow(row);

        EasyMock.verify(delegate);
    }

    @Test
    public void beginTransaction_delegateRegistered_shouldSucceed()
            throws UnsupportedOperationException, SQLException {
        FreeformQuery query = new FreeformQuery("SELECT * FROM people",
                Arrays.asList("id"), connectionPool);
        FreeformQueryDelegate delegate = EasyMock
                .createMock(FreeformQueryDelegate.class);
        EasyMock.replay(delegate);
        query.setDelegate(delegate);

        query.beginTransaction();
    }

    @Test(expected = SQLException.class)
    public void commit_delegateRegisteredNoActiveTransaction_shouldFail()
            throws UnsupportedOperationException, SQLException {
        FreeformQuery query = new FreeformQuery("SELECT * FROM people",
                Arrays.asList("id"), connectionPool);
        FreeformQueryDelegate delegate = EasyMock
                .createMock(FreeformQueryDelegate.class);
        EasyMock.replay(delegate);
        query.setDelegate(delegate);

        query.commit();
    }

    @Test
    public void commit_delegateRegisteredActiveTransaction_shouldSucceed()
            throws UnsupportedOperationException, SQLException {
        FreeformQuery query = new FreeformQuery("SELECT * FROM people",
                Arrays.asList("id"), connectionPool);
        FreeformQueryDelegate delegate = EasyMock
                .createMock(FreeformQueryDelegate.class);
        EasyMock.replay(delegate);
        query.setDelegate(delegate);

        query.beginTransaction();
        query.commit();
    }

    @Test(expected = SQLException.class)
    public void commit_delegateRegisteredActiveTransactionDoubleCommit_shouldFail()
            throws UnsupportedOperationException, SQLException {
        FreeformQuery query = new FreeformQuery("SELECT * FROM people",
                Arrays.asList("id"), connectionPool);
        FreeformQueryDelegate delegate = EasyMock
                .createMock(FreeformQueryDelegate.class);
        EasyMock.replay(delegate);
        query.setDelegate(delegate);

        query.beginTransaction();
        query.commit();
        query.commit();
    }

    @Test(expected = SQLException.class)
    public void rollback_delegateRegisteredNoActiveTransaction_shouldFail()
            throws UnsupportedOperationException, SQLException {
        FreeformQuery query = new FreeformQuery("SELECT * FROM people",
                Arrays.asList("id"), connectionPool);
        FreeformQueryDelegate delegate = EasyMock
                .createMock(FreeformQueryDelegate.class);
        EasyMock.replay(delegate);
        query.setDelegate(delegate);

        query.rollback();
    }

    @Test
    public void rollback_delegateRegisteredActiveTransaction_shouldSucceed()
            throws UnsupportedOperationException, SQLException {
        FreeformQuery query = new FreeformQuery("SELECT * FROM people",
                Arrays.asList("id"), connectionPool);
        FreeformQueryDelegate delegate = EasyMock
                .createMock(FreeformQueryDelegate.class);
        EasyMock.replay(delegate);
        query.setDelegate(delegate);

        query.beginTransaction();
        query.rollback();
    }

    @Test(expected = SQLException.class)
    public void rollback_delegateRegisteredActiveTransactionDoubleRollback_shouldFail()
            throws UnsupportedOperationException, SQLException {
        FreeformQuery query = new FreeformQuery("SELECT * FROM people",
                Arrays.asList("id"), connectionPool);
        FreeformQueryDelegate delegate = EasyMock
                .createMock(FreeformQueryDelegate.class);
        EasyMock.replay(delegate);
        query.setDelegate(delegate);

        query.beginTransaction();
        query.rollback();
        query.rollback();
    }

    @Test(expected = SQLException.class)
    public void rollback_delegateRegisteredCommittedTransaction_shouldFail()
            throws UnsupportedOperationException, SQLException {
        FreeformQuery query = new FreeformQuery("SELECT * FROM people",
                Arrays.asList("id"), connectionPool);
        FreeformQueryDelegate delegate = EasyMock
                .createMock(FreeformQueryDelegate.class);
        EasyMock.replay(delegate);
        query.setDelegate(delegate);

        query.beginTransaction();
        query.commit();
        query.rollback();
    }

    @Test(expected = SQLException.class)
    public void commit_delegateRegisteredRollbackedTransaction_shouldFail()
            throws UnsupportedOperationException, SQLException {
        FreeformQuery query = new FreeformQuery("SELECT * FROM people",
                Arrays.asList("id"), connectionPool);
        FreeformQueryDelegate delegate = EasyMock
                .createMock(FreeformQueryDelegate.class);
        EasyMock.replay(delegate);
        query.setDelegate(delegate);

        query.beginTransaction();
        query.rollback();
        query.commit();
    }
}
