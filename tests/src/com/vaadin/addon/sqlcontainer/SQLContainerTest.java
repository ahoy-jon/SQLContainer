package com.vaadin.addon.sqlcontainer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.addon.sqlcontainer.SQLContainer.ItemSetChangeEvent;
import com.vaadin.addon.sqlcontainer.connection.JDBCConnectionPool;
import com.vaadin.addon.sqlcontainer.connection.SimpleJDBCConnectionPool;
import com.vaadin.addon.sqlcontainer.query.Filter;
import com.vaadin.addon.sqlcontainer.query.Filter.ComparisonType;
import com.vaadin.addon.sqlcontainer.query.FreeformQuery;
import com.vaadin.addon.sqlcontainer.query.FreeformQueryDelegate;
import com.vaadin.addon.sqlcontainer.query.OrderBy;
import com.vaadin.data.Container.ItemSetChangeListener;
import com.vaadin.data.Item;

public class SQLContainerTest {

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
                    .execute("create table people (id integer generated always as identity, name varchar(32), PRIMARY KEY(id))");
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

    public void addFiveThousand() throws SQLException {
        Connection conn = connectionPool.reserveConnection();
        Statement statement = conn.createStatement();
        for (int i = 4; i < 5000; i++) {
            statement
                    .executeUpdate("insert into people values(default, 'Person "
                            + i + "')");
        }
        statement.close();
        conn.commit();
        connectionPool.releaseConnection(conn);
    }

    @Test
    public void constructor_withFreeformQuery_shouldSucceed()
            throws SQLException {
        new SQLContainer(new FreeformQuery("SELECT * FROM people",
                Arrays.asList("ID"), connectionPool));
    }

    // @Test
    // public void constructor_withTableQuery_shouldSucceed() throws
    // SQLException {
    // SQLContainer container = new SQLContainer(new TableQuery("people",
    // connectionPool));
    // }
    //
    // @Test
    // public void containsId_withTableQueryAndExistingId_returnsTrue()
    // throws SQLException {
    // SQLContainer container = new SQLContainer(new TableQuery("people",
    // connectionPool));
    // Assert.assertTrue(container.containsId(new RowId(new Object[] { 1 })));
    // }
    //
    // @Test
    // public void containsId_withTableQueryAndNonexistingId_returnsFalse()
    // throws SQLException {
    // SQLContainer container = new SQLContainer(new TableQuery("people",
    // connectionPool));
    // Assert.assertFalse(container
    // .containsId(new RowId(new Object[] { 1337 })));
    // }

    @Test
    public void containsId_withFreeformQueryAndExistingId_returnsTrue()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Assert.assertTrue(container.containsId(new RowId(new Object[] { 1 })));
    }

    @Test
    public void containsId_withFreeformQueryAndNonexistingId_returnsFalse()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Assert.assertFalse(container
                .containsId(new RowId(new Object[] { 1337 })));
    }

    @Test
    public void getContainerProperty_freeformExistingItemIdAndPropertyId_returnsProperty()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Assert.assertEquals(
                "Ville",
                container.getContainerProperty(new RowId(new Object[] { 0 }),
                        "NAME").getValue());
    }

    @Test
    public void getContainerProperty_freeformExistingItemIdAndNonexistingPropertyId_returnsNull()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Assert.assertNull(container.getContainerProperty(new RowId(
                new Object[] { 1 }), "asdf"));
    }

    @Test
    public void getContainerProperty_freeformNonexistingItemId_returnsNull()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Assert.assertNull(container.getContainerProperty(new RowId(
                new Object[] { 1337 }), "NAME"));
    }

    @Test
    public void getContainerPropertyIds_freeform_returnsIDAndNAME()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Collection<?> propertyIds = container.getContainerPropertyIds();
        Assert.assertEquals(2, propertyIds.size());
        Assert.assertArrayEquals(new String[] { "ID", "NAME" },
                propertyIds.toArray());
    }

    @Test
    public void getItem_freeformExistingItemId_returnsItem()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Item item = container.getItem(new RowId(new Object[] { 0 }));
        Assert.assertNotNull(item);
        Assert.assertEquals("Ville", item.getItemProperty("NAME").getValue());
    }

    @Test
    public void getItem_freeform5000RowsWithParameter1337_returnsItemWithId1337()
            throws SQLException {
        addFiveThousand();
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Item item = container.getItem(new RowId(new Object[] { 1337 }));
        Assert.assertNotNull(item);
        Assert.assertEquals(1337, item.getItemProperty("ID").getValue());
        Assert.assertEquals("Person 1337", item.getItemProperty("NAME")
                .getValue());
    }

    @Test
    public void getItemIds_freeform_returnsItemIdsWithKeys0through3()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Collection<?> itemIds = container.getItemIds();
        Assert.assertEquals(4, itemIds.size());
        RowId zero = new RowId(new Object[] { 0 });
        RowId one = new RowId(new Object[] { 1 });
        RowId two = new RowId(new Object[] { 2 });
        RowId three = new RowId(new Object[] { 3 });
        Assert.assertArrayEquals(new Object[] { zero, one, two, three },
                itemIds.toArray());
    }

    @Test
    public void getType_freeformNAMEPropertyId_returnsString()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Assert.assertEquals(String.class, container.getType("NAME"));
    }

    @Test
    public void getType_freeformIDPropertyId_returnsInteger()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Assert.assertEquals(Integer.class, container.getType("ID"));
    }

    @Test
    public void getType_freeformNonexistingPropertyId_returnsNull()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Assert.assertNull(container.getType("asdf"));
    }

    @Test
    public void size_freeform_returnsFour() throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Assert.assertEquals(4, container.size());
    }

    @Test
    public void size_freeformOneAddedItem_returnsFive() throws SQLException {
        Connection conn = connectionPool.reserveConnection();
        Statement statement = conn.createStatement();
        statement.executeUpdate("insert into people values(default, 'Bengt')");
        statement.close();
        conn.commit();
        connectionPool.releaseConnection(conn);

        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Assert.assertEquals(5, container.size());
    }

    @Test
    public void indexOfId_freeformWithParameterThree_returnsThree()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Assert.assertEquals(3,
                container.indexOfId(new RowId(new Object[] { 3 })));
    }

    @Test
    public void indexOfId_freeform5000RowsWithParameter1337_returns1337()
            throws SQLException {
        addFiveThousand();
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        container.getItem(new RowId(new Object[] { 1337 }));
        Assert.assertEquals(1337,
                container.indexOfId(new RowId(new Object[] { 1337 })));
    }

    @Test
    public void getIdByIndex_freeform5000rowsIndex1337_returnsRowId1337()
            throws SQLException {
        addFiveThousand();
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Object itemId = container.getIdByIndex(1337);
        Assert.assertEquals(new RowId(new Object[] { 1337 }), itemId);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void getIdByIndex_freeformWithPaging5000rowsIndex1337_returnsRowId1337()
            throws SQLException {
        addFiveThousand();
        FreeformQuery query = new FreeformQuery("SELECT * FROM people",
                Arrays.asList("ID"), connectionPool);
        FreeformQueryDelegate delegate = EasyMock
                .createMock(FreeformQueryDelegate.class);
        EasyMock.expect(
                delegate.getQueryString(EasyMock.anyInt(), EasyMock.anyInt()))
                .andAnswer(new IAnswer<String>() {
                    public String answer() throws Throwable {
                        Object[] args = EasyMock.getCurrentArguments();
                        int offset = (Integer) (args[0]);
                        int limit = (Integer) (args[1]);
                        return "SELECT * FROM people LIMIT " + limit
                                + " OFFSET " + offset;
                    }
                }).anyTimes();
        delegate.setFilters(null);
        EasyMock.expectLastCall().anyTimes();
        delegate.setFilters(EasyMock.isA(List.class));
        EasyMock.expectLastCall().anyTimes();
        delegate.setOrderBy(null);
        EasyMock.expectLastCall().anyTimes();
        delegate.setOrderBy(EasyMock.isA(List.class));
        EasyMock.expectLastCall().anyTimes();
        EasyMock.expect(delegate.getCountQuery())
                .andThrow(new UnsupportedOperationException()).anyTimes();
        EasyMock.replay(delegate);
        query.setDelegate(delegate);
        SQLContainer container = new SQLContainer(query);
        Object itemId = container.getIdByIndex(1337);
        Assert.assertEquals(new RowId(new Object[] { 1337 }), itemId);
    }

    @Test
    public void nextItemId_freeformCurrentItem1337_returnsItem1338()
            throws SQLException {
        addFiveThousand();
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Object itemId = container.getIdByIndex(1337);
        Assert.assertEquals(new RowId(new Object[] { 1338 }),
                container.nextItemId(itemId));
    }

    @Test
    public void prevItemId_freeformCurrentItem1337_returns1336()
            throws SQLException {
        addFiveThousand();
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Object itemId = container.getIdByIndex(1337);
        Assert.assertEquals(new RowId(new Object[] { 1336 }),
                container.prevItemId(itemId));
    }

    @Test
    public void firstItemId_freeform_returnsItemId0() throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Assert.assertEquals(new RowId(new Object[] { 0 }),
                container.firstItemId());
    }

    @Test
    public void lastItemId_freeform5000Rows_returnsItemId4999()
            throws SQLException {
        addFiveThousand();

        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Assert.assertEquals(new RowId(new Object[] { 4999 }),
                container.lastItemId());
    }

    @Test
    public void isFirstId_freeformActualFirstId_returnsTrue()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Assert.assertTrue(container.isFirstId(new RowId(new Object[] { 0 })));
    }

    @Test
    public void isFirstId_freeformSecondId_returnsFalse() throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Assert.assertFalse(container.isFirstId(new RowId(new Object[] { 1 })));
    }

    @Test
    public void isLastId_freeformSecondId_returnsFalse() throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Assert.assertFalse(container.isLastId(new RowId(new Object[] { 1 })));
    }

    @Test
    public void isLastId_freeformLastId_returnsTrue() throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Assert.assertTrue(container.isLastId(new RowId(new Object[] { 3 })));
    }

    @Test
    public void isLastId_freeform5000RowsLastId_returnsTrue()
            throws SQLException {
        addFiveThousand();
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Assert.assertTrue(container.isLastId(new RowId(new Object[] { 4999 })));
    }

    @Test
    public void refresh_freeform_sizeShouldUpdate() throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Assert.assertEquals(4, container.size());
        addFiveThousand();
        container.refresh();
        Assert.assertEquals(5000, container.size());
    }

    @Test
    public void refresh_freeformWithoutCallingRefresh_sizeShouldNotUpdate()
            throws SQLException {
        // Yeah, this is a weird one. We're testing that the size doesn't update
        // after adding lots of items unless we call refresh inbetween. This to
        // make sure that the refresh method actually refreshes stuff and isn't
        // a NOP.
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Assert.assertEquals(4, container.size());
        addFiveThousand();
        Assert.assertEquals(4, container.size());
    }

    @Test
    public void setAutoCommit_freeform_shouldSucceed() throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        container.setAutoCommit(true);
        Assert.assertTrue(container.isAutoCommit());
        container.setAutoCommit(false);
        Assert.assertFalse(container.isAutoCommit());
    }

    @Test
    public void getPageLength_freeform_returnsDefault100() throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Assert.assertEquals(100, container.getPageLength());
    }

    @Test
    public void setPageLength_freeform_shouldSucceed() throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        container.setPageLength(20);
        Assert.assertEquals(20, container.getPageLength());
        container.setPageLength(200);
        Assert.assertEquals(200, container.getPageLength());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void addContainerProperty_normal_isUnsupported() throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        container.addContainerProperty("asdf", String.class, "");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void removeContainerProperty_normal_isUnsupported()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        container.removeContainerProperty("asdf");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void addItemObject_normal_isUnsupported() throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        container.addItem("asdf");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void addItemAfterObjectObject_normal_isUnsupported()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        container.addItemAfter("asdf", "foo");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void addItemAtIntObject_normal_isUnsupported() throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        container.addItemAt(2, "asdf");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void addItemAtInt_normal_isUnsupported() throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        container.addItemAt(2);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void addItemAfterObject_normal_isUnsupported() throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        container.addItemAfter("asdf");
    }

    @Test
    public void addItem_freeformAddOneNewItem_returnsItemId()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Object itemId = container.addItem();
        Assert.assertNotNull(itemId);
    }

    @Test
    public void addItem_freeformAddOneNewItem_shouldChangeSize()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        int size = container.size();
        container.addItem();
        Assert.assertEquals(size + 1, container.size());
    }

    @Test
    public void addItem_freeformAddTwoNewItems_shouldChangeSize()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        int size = container.size();
        Object id1 = container.addItem();
        Object id2 = container.addItem();
        Assert.assertEquals(size + 2, container.size());
        Assert.assertNotSame(id1, id2);
        Assert.assertFalse(id1.equals(id2));
    }

    @Test
    public void nextItemId_freeformNewlyAddedItem_returnsNewlyAdded()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Object lastId = container.lastItemId();
        Object id = container.addItem();
        Assert.assertEquals(id, container.nextItemId(lastId));
    }

    @Test
    public void lastItemId_freeformNewlyAddedItem_returnsNewlyAdded()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Object lastId = container.lastItemId();
        Object id = container.addItem();
        Assert.assertEquals(id, container.lastItemId());
        Assert.assertNotSame(lastId, container.lastItemId());
    }

    @Test
    public void indexOfId_freeformNewlyAddedItem_returnsFour()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Object id = container.addItem();
        Assert.assertEquals(4, container.indexOfId(id));
    }

    @Test
    public void getItem_freeformNewlyAddedItem_returnsNewlyAdded()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Object id = container.addItem();
        Assert.assertNotNull(container.getItem(id));
    }

    @Test
    public void getItemIds_freeformNewlyAddedItem_containsNewlyAdded()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Object id = container.addItem();
        Assert.assertTrue(container.getItemIds().contains(id));
    }

    @Test
    public void getContainerProperty_freeformNewlyAddedItem_returnsPropertyOfNewlyAddedItem()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Object id = container.addItem();
        Item item = container.getItem(id);
        item.getItemProperty("NAME").setValue("asdf");
        Assert.assertEquals("asdf", container.getContainerProperty(id, "NAME")
                .getValue());
    }

    @Test
    public void containsId_freeformNewlyAddedItem_returnsTrue()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Object id = container.addItem();
        Assert.assertTrue(container.containsId(id));
    }

    @Test
    public void prevItemId_freeformTwoNewlyAddedItems_returnsFirstAddedItem()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Object id1 = container.addItem();
        Object id2 = container.addItem();
        Assert.assertEquals(id1, container.prevItemId(id2));
    }

    @Test
    public void firstItemId_freeformEmptyResultSet_returnsFirstAddedItem()
            throws SQLException {
        Connection conn = connectionPool.reserveConnection();
        Statement statement = conn.createStatement();
        try {
            statement.execute("drop table garbage");
        } catch (SQLException e) {
            // Don't worry if the table doesn't exist, since we don't want it to
        }
        statement
                .execute("create table garbage (id integer generated always as identity, type varchar(32), PRIMARY KEY(id))");
        conn.commit();
        connectionPool.releaseConnection(conn);
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM garbage", Arrays.asList("ID"), connectionPool));
        Object id = container.addItem();
        Assert.assertSame(id, container.firstItemId());
    }

    @Test
    public void isFirstId_freeformEmptyResultSet_returnsFirstAddedItem()
            throws SQLException {
        Connection conn = connectionPool.reserveConnection();
        Statement statement = conn.createStatement();
        try {
            statement.execute("drop table garbage");
        } catch (SQLException e) {
            // Don't worry if the table doesn't exist, since we don't want it to
        }
        statement
                .execute("create table garbage (id integer generated always as identity, type varchar(32), PRIMARY KEY(id))");
        conn.commit();
        connectionPool.releaseConnection(conn);
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM garbage", Arrays.asList("ID"), connectionPool));
        Object id = container.addItem();
        Assert.assertTrue(container.isFirstId(id));
    }

    @Test
    public void isLastId_freeformOneItemAdded_returnsTrueForAddedItem()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Object id = container.addItem();
        Assert.assertTrue(container.isLastId(id));
    }

    @Test
    public void isLastId_freeformTwoItemsAdded_returnsTrueForLastAddedItem()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        container.addItem();
        Object id2 = container.addItem();
        Assert.assertTrue(container.isLastId(id2));
    }

    @Test
    public void getIdByIndex_freeformOneItemAddedLastIndexInContainer_returnsAddedItem()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Object id = container.addItem();
        Assert.assertEquals(id, container.getIdByIndex(container.size() - 1));
    }

    @Test
    public void removeItem_freeformNoAddedItems_removesItemFromContainer()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        int size = container.size();
        Object id = container.firstItemId();
        Assert.assertTrue(container.removeItem(id));
        Assert.assertNotSame(id, container.firstItemId());
        Assert.assertEquals(size - 1, container.size());
    }

    @Test
    public void containsId_freeformRemovedItem_returnsFalse()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Object id = container.firstItemId();
        Assert.assertTrue(container.removeItem(id));
        Assert.assertFalse(container.containsId(id));
    }

    @Test
    public void removeItem_freeformOneAddedItem_removesTheAddedItem()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Object id = container.addItem();
        int size = container.size();
        Assert.assertTrue(container.removeItem(id));
        Assert.assertFalse(container.containsId(id));
        Assert.assertEquals(size - 1, container.size());
    }

    @Test
    public void getItem_freeformItemRemoved_returnsNull() throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Object id = container.firstItemId();
        Assert.assertTrue(container.removeItem(id));
        Assert.assertNull(container.getItem(id));
    }

    @Test
    public void getItem_freeformAddedItemRemoved_returnsNull()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Object id = container.addItem();
        Assert.assertNotNull(container.getItem(id));
        Assert.assertTrue(container.removeItem(id));
        Assert.assertNull(container.getItem(id));
    }

    @Test
    public void getItemIds_freeformItemRemoved_shouldNotContainRemovedItem()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Object id = container.firstItemId();
        Assert.assertTrue(container.getItemIds().contains(id));
        Assert.assertTrue(container.removeItem(id));
        Assert.assertFalse(container.getItemIds().contains(id));
    }

    @Test
    public void getItemIds_freeformAddedItemRemoved_shouldNotContainRemovedItem()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Object id = container.addItem();
        Assert.assertTrue(container.getItemIds().contains(id));
        Assert.assertTrue(container.removeItem(id));
        Assert.assertFalse(container.getItemIds().contains(id));
    }

    @Test
    public void containsId_freeformItemRemoved_returnsFalse()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Object id = container.firstItemId();
        Assert.assertTrue(container.containsId(id));
        Assert.assertTrue(container.removeItem(id));
        Assert.assertFalse(container.containsId(id));
    }

    @Test
    public void containsId_freeformAddedItemRemoved_returnsFalse()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Object id = container.addItem();
        Assert.assertTrue(container.containsId(id));
        Assert.assertTrue(container.removeItem(id));
        Assert.assertFalse(container.containsId(id));
    }

    @Test
    public void nextItemId_freeformItemRemoved_skipsRemovedItem()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Object first = container.getIdByIndex(0);
        Object second = container.getIdByIndex(1);
        Object third = container.getIdByIndex(2);
        Assert.assertTrue(container.removeItem(second));
        Assert.assertEquals(third, container.nextItemId(first));
    }

    @Test
    public void nextItemId_freeformAddedItemRemoved_skipsRemovedItem()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Object first = container.lastItemId();
        Object second = container.addItem();
        Object third = container.addItem();
        Assert.assertTrue(container.removeItem(second));
        Assert.assertEquals(third, container.nextItemId(first));
    }

    @Test
    public void prevItemId_freeformItemRemoved_skipsRemovedItem()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Object first = container.getIdByIndex(0);
        Object second = container.getIdByIndex(1);
        Object third = container.getIdByIndex(2);
        Assert.assertTrue(container.removeItem(second));
        Assert.assertEquals(first, container.prevItemId(third));
    }

    @Test
    public void prevItemId_freeformAddedItemRemoved_skipsRemovedItem()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Object first = container.lastItemId();
        Object second = container.addItem();
        Object third = container.addItem();
        Assert.assertTrue(container.removeItem(second));
        Assert.assertEquals(first, container.prevItemId(third));
    }

    @Test
    public void firstItemId_freeformFirstItemRemoved_resultChanges()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Object first = container.firstItemId();
        Assert.assertTrue(container.removeItem(first));
        Assert.assertNotSame(first, container.firstItemId());
    }

    @Test
    public void firstItemId_freeformNewlyAddedFirstItemRemoved_resultChanges()
            throws SQLException {
        Connection conn = connectionPool.reserveConnection();
        Statement statement = conn.createStatement();
        try {
            statement.execute("drop table garbage");
        } catch (SQLException e) {
            // Don't worry if the table doesn't exist, since we don't want it to
        }
        statement
                .execute("create table garbage (id integer generated always as identity, type varchar(32), PRIMARY KEY(id))");
        conn.commit();
        connectionPool.releaseConnection(conn);
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM garbage", Arrays.asList("ID"), connectionPool));
        Object first = container.addItem();
        Object second = container.addItem();
        Assert.assertSame(first, container.firstItemId());
        Assert.assertTrue(container.removeItem(first));
        Assert.assertSame(second, container.firstItemId());
    }

    @Test
    public void lastItemId_freeformLastItemRemoved_resultChanges()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Object last = container.lastItemId();
        Assert.assertTrue(container.removeItem(last));
        Assert.assertNotSame(last, container.lastItemId());
    }

    @Test
    public void lastItemId_freeformAddedLastItemRemoved_resultChanges()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Object last = container.addItem();
        Assert.assertSame(last, container.lastItemId());
        Assert.assertTrue(container.removeItem(last));
        Assert.assertNotSame(last, container.lastItemId());
    }

    @Test
    public void isFirstId_freeformFirstItemRemoved_returnsFalse()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Object first = container.firstItemId();
        Assert.assertTrue(container.removeItem(first));
        Assert.assertFalse(container.isFirstId(first));
    }

    @Test
    public void isFirstId_freeformAddedFirstItemRemoved_returnsFalse()
            throws SQLException {
        Connection conn = connectionPool.reserveConnection();
        Statement statement = conn.createStatement();
        try {
            statement.execute("drop table garbage");
        } catch (SQLException e) {
            // Don't worry if the table doesn't exist, since we don't want it to
        }
        statement
                .execute("create table garbage (id integer generated always as identity, type varchar(32), PRIMARY KEY(id))");
        conn.commit();
        connectionPool.releaseConnection(conn);
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM garbage", Arrays.asList("ID"), connectionPool));
        Object first = container.addItem();
        container.addItem();
        Assert.assertSame(first, container.firstItemId());
        Assert.assertTrue(container.removeItem(first));
        Assert.assertFalse(container.isFirstId(first));
    }

    @Test
    public void isLastId_freeformLastItemRemoved_returnsFalse()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Object last = container.lastItemId();
        Assert.assertTrue(container.removeItem(last));
        Assert.assertFalse(container.isLastId(last));
    }

    @Test
    public void isLastId_freeformAddedLastItemRemoved_returnsFalse()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Object last = container.addItem();
        Assert.assertSame(last, container.lastItemId());
        Assert.assertTrue(container.removeItem(last));
        Assert.assertFalse(container.isLastId(last));
    }

    @Test
    public void indexOfId_freeformItemRemoved_returnsNegOne()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Object id = container.getIdByIndex(2);
        Assert.assertTrue(container.removeItem(id));
        Assert.assertEquals(-1, container.indexOfId(id));
    }

    @Test
    public void indexOfId_freeformAddedItemRemoved_returnsNegOne()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Object id = container.addItem();
        Assert.assertTrue(container.indexOfId(id) != -1);
        Assert.assertTrue(container.removeItem(id));
        Assert.assertEquals(-1, container.indexOfId(id));
    }

    @Test
    public void getIdByIndex_freeformItemRemoved_resultChanges()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Object id = container.getIdByIndex(2);
        Assert.assertTrue(container.removeItem(id));
        Assert.assertNotSame(id, container.getIdByIndex(2));
    }

    @Test
    public void getIdByIndex_freeformAddedItemRemoved_resultChanges()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Object id = container.addItem();
        container.addItem();
        int index = container.indexOfId(id);
        Assert.assertTrue(container.removeItem(id));
        Assert.assertNotSame(id, container.getIdByIndex(index));
    }

    @Test
    public void removeAllItems_freeform_shouldSucceed() throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Assert.assertTrue(container.removeAllItems());
        Assert.assertEquals(0, container.size());
    }

    @Test
    public void removeAllItems_freeformAddedItems_shouldSucceed()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        container.addItem();
        container.addItem();
        Assert.assertTrue(container.removeAllItems());
        Assert.assertEquals(0, container.size());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void commit_freeformAddedItem_shouldBeWrittenToDB()
            throws SQLException {
        FreeformQueryDelegate delegate = EasyMock
                .createMock(FreeformQueryDelegate.class);
        EasyMock.expect(
                delegate.storeRow(EasyMock.isA(Connection.class),
                        EasyMock.isA(RowItem.class)))
                .andAnswer(new IAnswer<Integer>() {
                    public Integer answer() throws Throwable {
                        Connection conn = (Connection) EasyMock
                                .getCurrentArguments()[0];
                        RowItem item = (RowItem) EasyMock.getCurrentArguments()[1];
                        Statement statement = conn.createStatement();
                        statement
                                .executeUpdate("insert into people values(default, '"
                                        + item.getItemProperty("NAME")
                                                .getValue() + "')");
                        statement.close();
                        conn.commit();
                        connectionPool.releaseConnection(conn);
                        return 1;
                    }
                }).anyTimes();
        EasyMock.expect(
                delegate.getQueryString(EasyMock.anyInt(), EasyMock.anyInt()))
                .andAnswer(new IAnswer<String>() {
                    public String answer() throws Throwable {
                        Object[] args = EasyMock.getCurrentArguments();
                        int offset = (Integer) (args[0]);
                        int limit = (Integer) (args[1]);
                        return "SELECT * FROM people LIMIT " + limit
                                + " OFFSET " + offset;
                    }
                }).anyTimes();
        delegate.setFilters(null);
        EasyMock.expectLastCall().anyTimes();
        delegate.setFilters(EasyMock.isA(List.class));
        EasyMock.expectLastCall().anyTimes();
        delegate.setOrderBy(null);
        EasyMock.expectLastCall().anyTimes();
        delegate.setOrderBy(EasyMock.isA(List.class));
        EasyMock.expectLastCall().anyTimes();
        EasyMock.expect(delegate.getCountQuery())
                .andThrow(new UnsupportedOperationException()).anyTimes();

        FreeformQuery query = new FreeformQuery("SELECT * FROM people",
                Arrays.asList("ID"), connectionPool);
        query.setDelegate(delegate);
        EasyMock.replay(delegate);
        SQLContainer container = new SQLContainer(query);
        Object id = container.addItem();
        container.getContainerProperty(id, "NAME").setValue("New Name");
        Assert.assertTrue(id instanceof TemporaryRowId);
        Assert.assertSame(id, container.lastItemId());
        container.commit();
        Assert.assertFalse(container.lastItemId() instanceof TemporaryRowId);
        Assert.assertEquals("New Name",
                container.getContainerProperty(container.lastItemId(), "NAME")
                        .getValue());
        EasyMock.verify(delegate);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void commit_freeformTwoAddedItems_shouldBeWrittenToDB()
            throws SQLException {
        FreeformQueryDelegate delegate = EasyMock
                .createMock(FreeformQueryDelegate.class);
        EasyMock.expect(
                delegate.storeRow(EasyMock.isA(Connection.class),
                        EasyMock.isA(RowItem.class)))
                .andAnswer(new IAnswer<Integer>() {
                    public Integer answer() throws Throwable {
                        Connection conn = (Connection) EasyMock
                                .getCurrentArguments()[0];
                        RowItem item = (RowItem) EasyMock.getCurrentArguments()[1];
                        Statement statement = conn.createStatement();
                        statement
                                .executeUpdate("insert into people values(default, '"
                                        + item.getItemProperty("NAME")
                                                .getValue() + "')");
                        statement.close();
                        conn.commit();
                        connectionPool.releaseConnection(conn);
                        return 1;
                    }
                }).anyTimes();
        EasyMock.expect(
                delegate.getQueryString(EasyMock.anyInt(), EasyMock.anyInt()))
                .andAnswer(new IAnswer<String>() {
                    public String answer() throws Throwable {
                        Object[] args = EasyMock.getCurrentArguments();
                        int offset = (Integer) (args[0]);
                        int limit = (Integer) (args[1]);
                        return "SELECT * FROM people LIMIT " + limit
                                + " OFFSET " + offset;
                    }
                }).anyTimes();
        delegate.setFilters(null);
        EasyMock.expectLastCall().anyTimes();
        delegate.setFilters(EasyMock.isA(List.class));
        EasyMock.expectLastCall().anyTimes();
        delegate.setOrderBy(null);
        EasyMock.expectLastCall().anyTimes();
        delegate.setOrderBy(EasyMock.isA(List.class));
        EasyMock.expectLastCall().anyTimes();
        EasyMock.expect(delegate.getCountQuery())
                .andThrow(new UnsupportedOperationException()).anyTimes();

        FreeformQuery query = new FreeformQuery("SELECT * FROM people",
                Arrays.asList("ID"), connectionPool);
        query.setDelegate(delegate);
        EasyMock.replay(delegate);
        SQLContainer container = new SQLContainer(query);
        Object id = container.addItem();
        Object id2 = container.addItem();
        container.getContainerProperty(id, "NAME").setValue("Herbert");
        container.getContainerProperty(id2, "NAME").setValue("Larry");
        Assert.assertTrue(id2 instanceof TemporaryRowId);
        Assert.assertSame(id2, container.lastItemId());
        container.commit();
        Object nextToLast = container.getIdByIndex(container.size() - 2);
        Assert.assertFalse(nextToLast instanceof TemporaryRowId);
        Assert.assertEquals("Herbert",
                container.getContainerProperty(nextToLast, "NAME").getValue());
        Assert.assertFalse(container.lastItemId() instanceof TemporaryRowId);
        Assert.assertEquals("Larry",
                container.getContainerProperty(container.lastItemId(), "NAME")
                        .getValue());
        EasyMock.verify(delegate);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void commit_freeformRemovedItem_shouldBeRemovedFromDB()
            throws SQLException {
        FreeformQueryDelegate delegate = EasyMock
                .createMock(FreeformQueryDelegate.class);
        EasyMock.expect(
                delegate.removeRow(EasyMock.isA(Connection.class),
                        EasyMock.isA(RowItem.class)))
                .andAnswer(new IAnswer<Boolean>() {
                    public Boolean answer() throws Throwable {
                        Connection conn = (Connection) EasyMock
                                .getCurrentArguments()[0];
                        RowItem item = (RowItem) EasyMock.getCurrentArguments()[1];
                        Statement statement = conn.createStatement();
                        statement.executeUpdate("DELETE FROM people WHERE ID="
                                + item.getItemProperty("ID"));
                        statement.close();
                        return true;
                    }
                }).anyTimes();
        EasyMock.expect(
                delegate.getQueryString(EasyMock.anyInt(), EasyMock.anyInt()))
                .andAnswer(new IAnswer<String>() {
                    public String answer() throws Throwable {
                        Object[] args = EasyMock.getCurrentArguments();
                        int offset = (Integer) (args[0]);
                        int limit = (Integer) (args[1]);
                        return "SELECT * FROM people LIMIT " + limit
                                + " OFFSET " + offset;
                    }
                }).anyTimes();
        delegate.setFilters(null);
        EasyMock.expectLastCall().anyTimes();
        delegate.setFilters(EasyMock.isA(List.class));
        EasyMock.expectLastCall().anyTimes();
        delegate.setOrderBy(null);
        EasyMock.expectLastCall().anyTimes();
        delegate.setOrderBy(EasyMock.isA(List.class));
        EasyMock.expectLastCall().anyTimes();
        EasyMock.expect(delegate.getCountQuery())
                .andThrow(new UnsupportedOperationException()).anyTimes();

        FreeformQuery query = new FreeformQuery("SELECT * FROM people",
                Arrays.asList("ID"), connectionPool);
        query.setDelegate(delegate);
        EasyMock.replay(delegate);
        SQLContainer container = new SQLContainer(query);
        Object last = container.lastItemId();
        container.removeItem(last);
        container.commit();
        Assert.assertFalse(last.equals(container.lastItemId()));
        EasyMock.verify(delegate);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void commit_freeformLastItemUpdated_shouldUpdateRowInDB()
            throws SQLException {
        FreeformQueryDelegate delegate = EasyMock
                .createMock(FreeformQueryDelegate.class);
        EasyMock.expect(
                delegate.storeRow(EasyMock.isA(Connection.class),
                        EasyMock.isA(RowItem.class)))
                .andAnswer(new IAnswer<Integer>() {
                    public Integer answer() throws Throwable {
                        Connection conn = (Connection) EasyMock
                                .getCurrentArguments()[0];
                        RowItem item = (RowItem) EasyMock.getCurrentArguments()[1];
                        Statement statement = conn.createStatement();
                        statement.executeUpdate("UPDATE people SET NAME='"
                                + item.getItemProperty("NAME").getValue()
                                + "' WHERE ID="
                                + item.getItemProperty("ID").getValue());
                        statement.close();
                        conn.commit();
                        connectionPool.releaseConnection(conn);
                        return 1;
                    }
                }).anyTimes();
        EasyMock.expect(
                delegate.getQueryString(EasyMock.anyInt(), EasyMock.anyInt()))
                .andAnswer(new IAnswer<String>() {
                    public String answer() throws Throwable {
                        Object[] args = EasyMock.getCurrentArguments();
                        int offset = (Integer) (args[0]);
                        int limit = (Integer) (args[1]);
                        return "SELECT * FROM people LIMIT " + limit
                                + " OFFSET " + offset;
                    }
                }).anyTimes();
        delegate.setFilters(null);
        EasyMock.expectLastCall().anyTimes();
        delegate.setFilters(EasyMock.isA(List.class));
        EasyMock.expectLastCall().anyTimes();
        delegate.setOrderBy(null);
        EasyMock.expectLastCall().anyTimes();
        delegate.setOrderBy(EasyMock.isA(List.class));
        EasyMock.expectLastCall().anyTimes();
        EasyMock.expect(delegate.getCountQuery())
                .andThrow(new UnsupportedOperationException()).anyTimes();

        FreeformQuery query = new FreeformQuery("SELECT * FROM people",
                Arrays.asList("ID"), connectionPool);
        query.setDelegate(delegate);
        EasyMock.replay(delegate);
        SQLContainer container = new SQLContainer(query);
        Object last = container.lastItemId();
        container.getContainerProperty(last, "NAME").setValue("Donald");
        container.commit();
        Assert.assertEquals("Donald",
                container.getContainerProperty(container.lastItemId(), "NAME")
                        .getValue());
        EasyMock.verify(delegate);
    }

    @Test
    public void rollback_freeformItemAdded_discardsAddedItem()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        int size = container.size();
        Object id = container.addItem();
        container.getContainerProperty(id, "NAME").setValue("foo");
        Assert.assertEquals(size + 1, container.size());
        container.rollback();
        Assert.assertEquals(size, container.size());
        Assert.assertFalse("foo".equals(container.getContainerProperty(
                container.lastItemId(), "NAME").getValue()));
    }

    @Test
    public void rollback_freeformItemRemoved_restoresRemovedItem()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        int size = container.size();
        Object last = container.lastItemId();
        container.removeItem(last);
        Assert.assertEquals(size - 1, container.size());
        container.rollback();
        Assert.assertEquals(size, container.size());
        Assert.assertEquals(last, container.lastItemId());
    }

    @Test
    public void rollback_freeformItemChanged_discardsChanges()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Object last = container.lastItemId();
        container.getContainerProperty(last, "NAME").setValue("foo");
        container.rollback();
        Assert.assertFalse("foo".equals(container.getContainerProperty(
                container.lastItemId(), "NAME").getValue()));
    }

    @Test
    public void itemChangeNotification_freeform_isModifiedReturnsTrue()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Assert.assertFalse(container.isModified());
        RowItem last = (RowItem) container.getItem(container.lastItemId());
        container.itemChangeNotification(last);
        Assert.assertTrue(container.isModified());
    }

    @Test
    public void itemSetChangeListeners_freeform_shouldFire()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        ItemSetChangeListener listener = EasyMock
                .createMock(ItemSetChangeListener.class);
        listener.containerItemSetChange(EasyMock.isA(ItemSetChangeEvent.class));
        EasyMock.replay(listener);

        container.addListener(listener);
        container.addItem();

        EasyMock.verify(listener);
    }

    @Test
    public void itemSetChangeListeners_freeformItemRemoved_shouldFire()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        ItemSetChangeListener listener = EasyMock
                .createMock(ItemSetChangeListener.class);
        listener.containerItemSetChange(EasyMock.isA(ItemSetChangeEvent.class));
        EasyMock.expectLastCall().anyTimes();
        EasyMock.replay(listener);

        container.addListener(listener);
        container.removeItem(container.lastItemId());

        EasyMock.verify(listener);
    }

    @Test
    public void removeListener_freeform_shouldNotFire() throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        ItemSetChangeListener listener = EasyMock
                .createMock(ItemSetChangeListener.class);
        EasyMock.replay(listener);

        container.addListener(listener);
        container.removeListener(listener);
        container.addItem();

        EasyMock.verify(listener);
    }

    @Test
    public void isModified_freeformRemovedItem_returnsTrue()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Assert.assertFalse(container.isModified());
        container.removeItem(container.lastItemId());
        Assert.assertTrue(container.isModified());
    }

    @Test
    public void isModified_freeformAddedItem_returnsTrue() throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Assert.assertFalse(container.isModified());
        container.addItem();
        Assert.assertTrue(container.isModified());
    }

    @Test
    public void isModified_freeformChangedItem_returnsTrue()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Assert.assertFalse(container.isModified());
        container.getContainerProperty(container.lastItemId(), "NAME")
                .setValue("foo");
        Assert.assertTrue(container.isModified());
    }

    @Test
    public void getSortableContainerPropertyIds_freeform_returnsAllPropertyIds()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        Collection<?> sortableIds = container.getSortableContainerPropertyIds();
        Assert.assertTrue(sortableIds.contains("ID"));
        Assert.assertTrue(sortableIds.contains("NAME"));
        Assert.assertEquals(2, sortableIds.size());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void addOrderBy_freeform_shouldReorderResults() throws SQLException {
        FreeformQuery query = new FreeformQuery("SELECT * FROM people",
                Arrays.asList("ID"), connectionPool);
        FreeformQueryDelegate delegate = EasyMock
                .createMock(FreeformQueryDelegate.class);
        final ArrayList<OrderBy> orderBys = new ArrayList<OrderBy>();
        delegate.setFilters(null);
        EasyMock.expectLastCall().anyTimes();
        delegate.setFilters(EasyMock.isA(List.class));
        EasyMock.expectLastCall().anyTimes();
        delegate.setOrderBy(null);
        EasyMock.expectLastCall().anyTimes();
        delegate.setOrderBy(EasyMock.isA(List.class));
        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                List<OrderBy> orders = (List<OrderBy>) EasyMock
                        .getCurrentArguments()[0];
                orderBys.clear();
                orderBys.addAll(orders);
                return null;
            }
        }).anyTimes();
        EasyMock.expect(
                delegate.getQueryString(EasyMock.anyInt(), EasyMock.anyInt()))
                .andAnswer(new IAnswer<String>() {
                    public String answer() throws Throwable {
                        Object[] args = EasyMock.getCurrentArguments();
                        int offset = (Integer) (args[0]);
                        int limit = (Integer) (args[1]);
                        StringBuffer query = new StringBuffer(
                                "SELECT * FROM people");
                        if (!orderBys.isEmpty()) {
                            query.append(" ORDER BY ");
                            for (OrderBy orderBy : orderBys) {
                                query.append(orderBy.getColumn());
                                if (orderBy.isAscending()) {
                                    query.append(" ASC");
                                } else {
                                    query.append(" DESC");
                                }
                            }
                        }
                        query.append(" LIMIT ").append(limit)
                                .append(" OFFSET ").append(offset);
                        return query.toString();
                    }
                }).anyTimes();
        EasyMock.expect(delegate.getCountQuery())
                .andThrow(new UnsupportedOperationException()).anyTimes();

        EasyMock.replay(delegate);
        query.setDelegate(delegate);
        SQLContainer container = new SQLContainer(query);
        // Ville, Kalle, Pelle, Börje
        Assert.assertEquals("Ville",
                container.getContainerProperty(container.firstItemId(), "NAME")
                        .getValue());
        Assert.assertEquals("Börje",
                container.getContainerProperty(container.lastItemId(), "NAME")
                        .getValue());

        container.addOrderBy(new OrderBy("NAME", true));
        // Börje, Kalle, Pelle, Ville
        Assert.assertEquals("Börje",
                container.getContainerProperty(container.firstItemId(), "NAME")
                        .getValue());
        Assert.assertEquals("Ville",
                container.getContainerProperty(container.lastItemId(), "NAME")
                        .getValue());

        EasyMock.verify(delegate);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addOrderBy_freeformIllegalColumn_shouldFail()
            throws SQLException {
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM people", Arrays.asList("ID"), connectionPool));
        container.addOrderBy(new OrderBy("asdf", true));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void sort_freeform_sortsByName() throws SQLException {
        FreeformQuery query = new FreeformQuery("SELECT * FROM people",
                Arrays.asList("ID"), connectionPool);
        FreeformQueryDelegate delegate = EasyMock
                .createMock(FreeformQueryDelegate.class);
        final ArrayList<OrderBy> orderBys = new ArrayList<OrderBy>();
        delegate.setFilters(null);
        EasyMock.expectLastCall().anyTimes();
        delegate.setFilters(EasyMock.isA(List.class));
        EasyMock.expectLastCall().anyTimes();
        delegate.setOrderBy(null);
        EasyMock.expectLastCall().anyTimes();
        delegate.setOrderBy(EasyMock.isA(List.class));
        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                List<OrderBy> orders = (List<OrderBy>) EasyMock
                        .getCurrentArguments()[0];
                orderBys.clear();
                orderBys.addAll(orders);
                return null;
            }
        }).anyTimes();
        EasyMock.expect(
                delegate.getQueryString(EasyMock.anyInt(), EasyMock.anyInt()))
                .andAnswer(new IAnswer<String>() {
                    public String answer() throws Throwable {
                        Object[] args = EasyMock.getCurrentArguments();
                        int offset = (Integer) (args[0]);
                        int limit = (Integer) (args[1]);
                        StringBuffer query = new StringBuffer(
                                "SELECT * FROM people");
                        if (!orderBys.isEmpty()) {
                            query.append(" ORDER BY ");
                            for (OrderBy orderBy : orderBys) {
                                query.append(orderBy.getColumn());
                                if (orderBy.isAscending()) {
                                    query.append(" ASC");
                                } else {
                                    query.append(" DESC");
                                }
                            }
                        }
                        query.append(" LIMIT ").append(limit)
                                .append(" OFFSET ").append(offset);
                        return query.toString();
                    }
                }).anyTimes();
        EasyMock.expect(delegate.getCountQuery())
                .andThrow(new UnsupportedOperationException()).anyTimes();
        EasyMock.replay(delegate);

        query.setDelegate(delegate);
        SQLContainer container = new SQLContainer(query);
        // Ville, Kalle, Pelle, Börje
        Assert.assertEquals("Ville",
                container.getContainerProperty(container.firstItemId(), "NAME")
                        .getValue());
        Assert.assertEquals("Börje",
                container.getContainerProperty(container.lastItemId(), "NAME")
                        .getValue());

        container.sort(new Object[] { "NAME" }, new boolean[] { true });

        // Börje, Kalle, Pelle, Ville
        Assert.assertEquals("Börje",
                container.getContainerProperty(container.firstItemId(), "NAME")
                        .getValue());
        Assert.assertEquals("Ville",
                container.getContainerProperty(container.lastItemId(), "NAME")
                        .getValue());

        EasyMock.verify(delegate);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void addFilter_freeform_filtersResults() throws SQLException {
        FreeformQuery query = new FreeformQuery("SELECT * FROM people",
                Arrays.asList("ID"), connectionPool);
        FreeformQueryDelegate delegate = EasyMock
                .createMock(FreeformQueryDelegate.class);
        final ArrayList<Filter> filters = new ArrayList<Filter>();
        delegate.setFilters(null);
        EasyMock.expectLastCall().anyTimes();
        delegate.setOrderBy(EasyMock.isA(List.class));
        EasyMock.expectLastCall().anyTimes();
        delegate.setOrderBy(null);
        EasyMock.expectLastCall().anyTimes();
        delegate.setFilters(EasyMock.isA(List.class));
        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                List<Filter> orders = (List<Filter>) EasyMock
                        .getCurrentArguments()[0];
                filters.clear();
                filters.addAll(orders);
                return null;
            }
        }).anyTimes();
        EasyMock.expect(
                delegate.getQueryString(EasyMock.anyInt(), EasyMock.anyInt()))
                .andAnswer(new IAnswer<String>() {
                    public String answer() throws Throwable {
                        Object[] args = EasyMock.getCurrentArguments();
                        int offset = (Integer) (args[0]);
                        int limit = (Integer) (args[1]);
                        StringBuffer query = new StringBuffer(
                                "SELECT * FROM people");
                        if (!filters.isEmpty()) {
                            Filter lastFilter = filters.get(filters.size() - 1);
                            query.append(" WHERE ");
                            for (Filter filter : filters) {
                                query.append(filter.toWhereString());
                                if (lastFilter != filter) {
                                    query.append(" AND ");
                                }
                            }
                        }
                        query.append(" LIMIT ").append(limit)
                                .append(" OFFSET ").append(offset);
                        return query.toString();
                    }
                }).anyTimes();
        EasyMock.expect(delegate.getCountQuery())
                .andAnswer(new IAnswer<String>() {
                    public String answer() throws Throwable {
                        StringBuffer query = new StringBuffer(
                                "SELECT COUNT(*) FROM people");
                        if (!filters.isEmpty()) {
                            Filter lastFilter = filters.get(filters.size() - 1);
                            query.append(" WHERE ");
                            for (Filter filter : filters) {
                                query.append(filter.toWhereString());
                                if (lastFilter != filter) {
                                    query.append(" AND ");
                                }
                            }
                        }
                        return query.toString();
                    }
                }).anyTimes();

        EasyMock.replay(delegate);
        query.setDelegate(delegate);
        SQLContainer container = new SQLContainer(query);
        // Ville, Kalle, Pelle, Börje
        Assert.assertEquals(4, container.size());
        Assert.assertEquals("Börje",
                container.getContainerProperty(container.lastItemId(), "NAME")
                        .getValue());

        container
                .addFilter(new Filter("NAME", ComparisonType.ENDS_WITH, "lle"));
        // Ville, Kalle, Pelle
        Assert.assertEquals(3, container.size());
        Assert.assertEquals("Pelle",
                container.getContainerProperty(container.lastItemId(), "NAME")
                        .getValue());

        EasyMock.verify(delegate);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void addContainerFilter_filtersResults() throws SQLException {
        FreeformQuery query = new FreeformQuery("SELECT * FROM people",
                Arrays.asList("ID"), connectionPool);
        FreeformQueryDelegate delegate = EasyMock
                .createMock(FreeformQueryDelegate.class);
        final ArrayList<Filter> filters = new ArrayList<Filter>();
        delegate.setFilters(null);
        EasyMock.expectLastCall().anyTimes();
        delegate.setOrderBy(EasyMock.isA(List.class));
        EasyMock.expectLastCall().anyTimes();
        delegate.setOrderBy(null);
        EasyMock.expectLastCall().anyTimes();
        delegate.setFilters(EasyMock.isA(List.class));
        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                List<Filter> orders = (List<Filter>) EasyMock
                        .getCurrentArguments()[0];
                filters.clear();
                filters.addAll(orders);
                return null;
            }
        }).anyTimes();
        EasyMock.expect(
                delegate.getQueryString(EasyMock.anyInt(), EasyMock.anyInt()))
                .andAnswer(new IAnswer<String>() {
                    public String answer() throws Throwable {
                        Object[] args = EasyMock.getCurrentArguments();
                        int offset = (Integer) (args[0]);
                        int limit = (Integer) (args[1]);
                        StringBuffer query = new StringBuffer(
                                "SELECT * FROM people");
                        if (!filters.isEmpty()) {
                            Filter lastFilter = filters.get(filters.size() - 1);
                            query.append(" WHERE ");
                            for (Filter filter : filters) {
                                query.append(filter.toWhereString());
                                if (lastFilter != filter) {
                                    query.append(" AND ");
                                }
                            }
                        }
                        query.append(" LIMIT ").append(limit)
                                .append(" OFFSET ").append(offset);
                        return query.toString();
                    }
                }).anyTimes();
        EasyMock.expect(delegate.getCountQuery())
                .andAnswer(new IAnswer<String>() {
                    public String answer() throws Throwable {
                        StringBuffer query = new StringBuffer(
                                "SELECT COUNT(*) FROM people");
                        if (!filters.isEmpty()) {
                            Filter lastFilter = filters.get(filters.size() - 1);
                            query.append(" WHERE ");
                            for (Filter filter : filters) {
                                query.append(filter.toWhereString());
                                if (lastFilter != filter) {
                                    query.append(" AND ");
                                }
                            }
                        }
                        return query.toString();
                    }
                }).anyTimes();

        EasyMock.replay(delegate);
        query.setDelegate(delegate);
        SQLContainer container = new SQLContainer(query);
        // Ville, Kalle, Pelle, Börje
        Assert.assertEquals(4, container.size());

        container.addContainerFilter("NAME", "Vi", false, false);

        // Ville
        Assert.assertEquals(1, container.size());
        Assert.assertEquals("Ville",
                container.getContainerProperty(container.lastItemId(), "NAME")
                        .getValue());

        EasyMock.verify(delegate);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void addContainerFilter_ignoreCase_filtersResults()
            throws SQLException {
        FreeformQuery query = new FreeformQuery("SELECT * FROM people",
                Arrays.asList("ID"), connectionPool);
        FreeformQueryDelegate delegate = EasyMock
                .createMock(FreeformQueryDelegate.class);
        final ArrayList<Filter> filters = new ArrayList<Filter>();
        delegate.setFilters(null);
        EasyMock.expectLastCall().anyTimes();
        delegate.setOrderBy(EasyMock.isA(List.class));
        EasyMock.expectLastCall().anyTimes();
        delegate.setOrderBy(null);
        EasyMock.expectLastCall().anyTimes();
        delegate.setFilters(EasyMock.isA(List.class));
        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                List<Filter> orders = (List<Filter>) EasyMock
                        .getCurrentArguments()[0];
                filters.clear();
                filters.addAll(orders);
                return null;
            }
        }).anyTimes();
        EasyMock.expect(
                delegate.getQueryString(EasyMock.anyInt(), EasyMock.anyInt()))
                .andAnswer(new IAnswer<String>() {
                    public String answer() throws Throwable {
                        Object[] args = EasyMock.getCurrentArguments();
                        int offset = (Integer) (args[0]);
                        int limit = (Integer) (args[1]);
                        StringBuffer query = new StringBuffer(
                                "SELECT * FROM people");
                        if (!filters.isEmpty()) {
                            Filter lastFilter = filters.get(filters.size() - 1);
                            query.append(" WHERE ");
                            for (Filter filter : filters) {
                                query.append(filter.toWhereString());
                                if (lastFilter != filter) {
                                    query.append(" AND ");
                                }
                            }
                        }
                        query.append(" LIMIT ").append(limit)
                                .append(" OFFSET ").append(offset);
                        return query.toString();
                    }
                }).anyTimes();
        EasyMock.expect(delegate.getCountQuery())
                .andAnswer(new IAnswer<String>() {
                    public String answer() throws Throwable {
                        StringBuffer query = new StringBuffer(
                                "SELECT COUNT(*) FROM people");
                        if (!filters.isEmpty()) {
                            Filter lastFilter = filters.get(filters.size() - 1);
                            query.append(" WHERE ");
                            for (Filter filter : filters) {
                                query.append(filter.toWhereString());
                                if (lastFilter != filter) {
                                    query.append(" AND ");
                                }
                            }
                        }
                        return query.toString();
                    }
                }).anyTimes();

        EasyMock.replay(delegate);
        query.setDelegate(delegate);
        SQLContainer container = new SQLContainer(query);
        // Ville, Kalle, Pelle, Börje
        Assert.assertEquals(4, container.size());

        container.addContainerFilter("NAME", "vi", true, false);

        // Ville
        Assert.assertEquals(1, container.size());
        Assert.assertEquals("Ville",
                container.getContainerProperty(container.lastItemId(), "NAME")
                        .getValue());

        EasyMock.verify(delegate);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void removeAllContainerFilters_freeform_noFiltering()
            throws SQLException {
        FreeformQuery query = new FreeformQuery("SELECT * FROM people",
                Arrays.asList("ID"), connectionPool);
        FreeformQueryDelegate delegate = EasyMock
                .createMock(FreeformQueryDelegate.class);
        final ArrayList<Filter> filters = new ArrayList<Filter>();
        delegate.setFilters(null);
        EasyMock.expectLastCall().anyTimes();
        delegate.setOrderBy(EasyMock.isA(List.class));
        EasyMock.expectLastCall().anyTimes();
        delegate.setOrderBy(null);
        EasyMock.expectLastCall().anyTimes();
        delegate.setFilters(EasyMock.isA(List.class));
        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                List<Filter> orders = (List<Filter>) EasyMock
                        .getCurrentArguments()[0];
                filters.clear();
                filters.addAll(orders);
                return null;
            }
        }).anyTimes();
        EasyMock.expect(
                delegate.getQueryString(EasyMock.anyInt(), EasyMock.anyInt()))
                .andAnswer(new IAnswer<String>() {
                    public String answer() throws Throwable {
                        Object[] args = EasyMock.getCurrentArguments();
                        int offset = (Integer) (args[0]);
                        int limit = (Integer) (args[1]);
                        StringBuffer query = new StringBuffer(
                                "SELECT * FROM people");
                        if (!filters.isEmpty()) {
                            Filter lastFilter = filters.get(filters.size() - 1);
                            query.append(" WHERE ");
                            for (Filter filter : filters) {
                                query.append(filter.toWhereString());
                                if (lastFilter != filter) {
                                    query.append(" AND ");
                                }
                            }
                        }
                        query.append(" LIMIT ").append(limit)
                                .append(" OFFSET ").append(offset);
                        return query.toString();
                    }
                }).anyTimes();
        EasyMock.expect(delegate.getCountQuery())
                .andAnswer(new IAnswer<String>() {
                    public String answer() throws Throwable {
                        StringBuffer query = new StringBuffer(
                                "SELECT COUNT(*) FROM people");
                        if (!filters.isEmpty()) {
                            Filter lastFilter = filters.get(filters.size() - 1);
                            query.append(" WHERE ");
                            for (Filter filter : filters) {
                                query.append(filter.toWhereString());
                                if (lastFilter != filter) {
                                    query.append(" AND ");
                                }
                            }
                        }
                        return query.toString();
                    }
                }).anyTimes();

        EasyMock.replay(delegate);
        query.setDelegate(delegate);
        SQLContainer container = new SQLContainer(query);
        // Ville, Kalle, Pelle, Börje
        Assert.assertEquals(4, container.size());

        container.addContainerFilter("NAME", "Vi", false, false);

        // Ville
        Assert.assertEquals(1, container.size());
        Assert.assertEquals("Ville",
                container.getContainerProperty(container.lastItemId(), "NAME")
                        .getValue());

        container.removeAllContainerFilters();

        Assert.assertEquals(4, container.size());
        Assert.assertEquals("Börje",
                container.getContainerProperty(container.lastItemId(), "NAME")
                        .getValue());

        EasyMock.verify(delegate);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void removeContainerFilters_freeform_noFiltering()
            throws SQLException {
        FreeformQuery query = new FreeformQuery("SELECT * FROM people",
                Arrays.asList("ID"), connectionPool);
        FreeformQueryDelegate delegate = EasyMock
                .createMock(FreeformQueryDelegate.class);
        final ArrayList<Filter> filters = new ArrayList<Filter>();
        delegate.setFilters(null);
        EasyMock.expectLastCall().anyTimes();
        delegate.setOrderBy(EasyMock.isA(List.class));
        EasyMock.expectLastCall().anyTimes();
        delegate.setOrderBy(null);
        EasyMock.expectLastCall().anyTimes();
        delegate.setFilters(EasyMock.isA(List.class));
        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                List<Filter> orders = (List<Filter>) EasyMock
                        .getCurrentArguments()[0];
                filters.clear();
                filters.addAll(orders);
                return null;
            }
        }).anyTimes();
        EasyMock.expect(
                delegate.getQueryString(EasyMock.anyInt(), EasyMock.anyInt()))
                .andAnswer(new IAnswer<String>() {
                    public String answer() throws Throwable {
                        Object[] args = EasyMock.getCurrentArguments();
                        int offset = (Integer) (args[0]);
                        int limit = (Integer) (args[1]);
                        StringBuffer query = new StringBuffer(
                                "SELECT * FROM people");
                        if (!filters.isEmpty()) {
                            Filter lastFilter = filters.get(filters.size() - 1);
                            query.append(" WHERE ");
                            for (Filter filter : filters) {
                                query.append(filter.toWhereString());
                                if (lastFilter != filter) {
                                    query.append(" AND ");
                                }
                            }
                        }
                        query.append(" LIMIT ").append(limit)
                                .append(" OFFSET ").append(offset);
                        return query.toString();
                    }
                }).anyTimes();
        EasyMock.expect(delegate.getCountQuery())
                .andAnswer(new IAnswer<String>() {
                    public String answer() throws Throwable {
                        StringBuffer query = new StringBuffer(
                                "SELECT COUNT(*) FROM people");
                        if (!filters.isEmpty()) {
                            Filter lastFilter = filters.get(filters.size() - 1);
                            query.append(" WHERE ");
                            for (Filter filter : filters) {
                                query.append(filter.toWhereString());
                                if (lastFilter != filter) {
                                    query.append(" AND ");
                                }
                            }
                        }
                        return query.toString();
                    }
                }).anyTimes();

        EasyMock.replay(delegate);
        query.setDelegate(delegate);
        SQLContainer container = new SQLContainer(query);
        // Ville, Kalle, Pelle, Börje
        Assert.assertEquals(4, container.size());

        container.addContainerFilter("NAME", "Vi", false, false);

        // Ville
        Assert.assertEquals(1, container.size());
        Assert.assertEquals("Ville",
                container.getContainerProperty(container.lastItemId(), "NAME")
                        .getValue());

        container.removeContainerFilters("NAME");

        Assert.assertEquals(4, container.size());
        Assert.assertEquals("Börje",
                container.getContainerProperty(container.lastItemId(), "NAME")
                        .getValue());

        EasyMock.verify(delegate);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void addFilter_freeformBufferedItems_alsoFiltersBufferedItems()
            throws SQLException {
        FreeformQuery query = new FreeformQuery("SELECT * FROM people",
                Arrays.asList("ID"), connectionPool);
        FreeformQueryDelegate delegate = EasyMock
                .createMock(FreeformQueryDelegate.class);
        final ArrayList<Filter> filters = new ArrayList<Filter>();
        delegate.setFilters(null);
        EasyMock.expectLastCall().anyTimes();
        delegate.setOrderBy(EasyMock.isA(List.class));
        EasyMock.expectLastCall().anyTimes();
        delegate.setOrderBy(null);
        EasyMock.expectLastCall().anyTimes();
        delegate.setFilters(EasyMock.isA(List.class));
        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                List<Filter> orders = (List<Filter>) EasyMock
                        .getCurrentArguments()[0];
                filters.clear();
                filters.addAll(orders);
                return null;
            }
        }).anyTimes();
        EasyMock.expect(
                delegate.getQueryString(EasyMock.anyInt(), EasyMock.anyInt()))
                .andAnswer(new IAnswer<String>() {
                    public String answer() throws Throwable {
                        Object[] args = EasyMock.getCurrentArguments();
                        int offset = (Integer) (args[0]);
                        int limit = (Integer) (args[1]);
                        StringBuffer query = new StringBuffer(
                                "SELECT * FROM people");
                        if (!filters.isEmpty()) {
                            Filter lastFilter = filters.get(filters.size() - 1);
                            query.append(" WHERE ");
                            for (Filter filter : filters) {
                                query.append(filter.toWhereString());
                                if (lastFilter != filter) {
                                    query.append(" AND ");
                                }
                            }
                        }
                        if (limit > 0) {
                            query.append(" LIMIT ").append(limit)
                                    .append(" OFFSET ").append(offset);
                        }
                        return query.toString();
                    }
                }).anyTimes();
        EasyMock.expect(delegate.getCountQuery())
                .andAnswer(new IAnswer<String>() {
                    public String answer() throws Throwable {
                        StringBuffer query = new StringBuffer(
                                "SELECT COUNT(*) FROM people");
                        if (!filters.isEmpty()) {
                            Filter lastFilter = filters.get(filters.size() - 1);
                            query.append(" WHERE ");
                            for (Filter filter : filters) {
                                query.append(filter.toWhereString());
                                if (lastFilter != filter) {
                                    query.append(" AND ");
                                }
                            }
                        }
                        return query.toString();
                    }
                }).anyTimes();

        EasyMock.replay(delegate);
        query.setDelegate(delegate);
        SQLContainer container = new SQLContainer(query);
        // Ville, Kalle, Pelle, Börje
        Assert.assertEquals(4, container.size());
        Assert.assertEquals("Börje",
                container.getContainerProperty(container.lastItemId(), "NAME")
                        .getValue());

        Object id1 = container.addItem();
        container.getContainerProperty(id1, "NAME").setValue("Palle");
        Object id2 = container.addItem();
        container.getContainerProperty(id2, "NAME").setValue("Bengt");

        container
                .addFilter(new Filter("NAME", ComparisonType.ENDS_WITH, "lle"));

        // Ville, Kalle, Pelle, Palle
        Assert.assertEquals(4, container.size());
        Assert.assertEquals(
                "Ville",
                container.getContainerProperty(container.getIdByIndex(0),
                        "NAME").getValue());
        Assert.assertEquals(
                "Kalle",
                container.getContainerProperty(container.getIdByIndex(1),
                        "NAME").getValue());
        Assert.assertEquals(
                "Pelle",
                container.getContainerProperty(container.getIdByIndex(2),
                        "NAME").getValue());
        Assert.assertEquals(
                "Palle",
                container.getContainerProperty(container.getIdByIndex(3),
                        "NAME").getValue());

        Assert.assertNull(container.getIdByIndex(4));
        Assert.assertNull(container.nextItemId(container.getIdByIndex(3)));

        Assert.assertFalse(container.containsId(id2));
        Assert.assertFalse(container.getItemIds().contains(id2));

        Assert.assertNull(container.getItem(id2));
        Assert.assertEquals(-1, container.indexOfId(id2));

        Assert.assertNotSame(id2, container.lastItemId());
        Assert.assertSame(id1, container.lastItemId());

        EasyMock.verify(delegate);
    }
}
