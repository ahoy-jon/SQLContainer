package com.vaadin.addon.sqlcontainer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.addon.sqlcontainer.SQLContainer.ItemSetChangeEvent;
import com.vaadin.addon.sqlcontainer.connection.JDBCConnectionPool;
import com.vaadin.addon.sqlcontainer.connection.SimpleJDBCConnectionPool;
import com.vaadin.addon.sqlcontainer.query.Filter;
import com.vaadin.addon.sqlcontainer.query.FreeformQuery;
import com.vaadin.addon.sqlcontainer.query.OrderBy;
import com.vaadin.addon.sqlcontainer.query.TableQuery;
import com.vaadin.addon.sqlcontainer.query.Filter.ComparisonType;
import com.vaadin.data.Item;
import com.vaadin.data.Container.ItemSetChangeListener;

public class SQLContainerTableQueryTest {

    private static final int offset = AllTests.offset;
    private static final String createGarbage = AllTests.createGarbage;
    private JDBCConnectionPool connectionPool;

    @Before
    public void setUp() {

        try {
            connectionPool = new SimpleJDBCConnectionPool(AllTests.dbDriver,
                    AllTests.dbURL, AllTests.dbUser, AllTests.dbPwd, 2, 2);
        } catch (SQLException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        addPeopleToDatabase();
    }

    @After
    public void tearDown() {
        if (connectionPool != null) {
            connectionPool.destroy();
        }
    }

    private void addPeopleToDatabase() {
        try {
            Connection conn = connectionPool.reserveConnection();
            Statement statement = conn.createStatement();
            try {
                statement.execute("drop table people");
            } catch (SQLException e) {
                // Will fail if table doesn't exist, which is OK.
                conn.rollback();
            }
            statement.execute(AllTests.peopleFirst);
            if (AllTests.peopleSecond != null) {
                statement.execute(AllTests.peopleSecond);
            }
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
    public void constructor_withTableQuery_shouldSucceed() throws SQLException {
        new SQLContainer(new TableQuery("people", connectionPool));
    }

    @Test
    public void containsId_withTableQueryAndExistingId_returnsTrue()
            throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Assert.assertTrue(container.containsId(new RowId(
                new Object[] { 1 + offset })));
    }

    @Test
    public void containsId_withTableQueryAndNonexistingId_returnsFalse()
            throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Assert.assertFalse(container.containsId(new RowId(
                new Object[] { 1337 + offset })));
    }

    @Test
    public void getContainerProperty_tableExistingItemIdAndPropertyId_returnsProperty()
            throws SQLException {
        TableQuery t = new TableQuery("people", connectionPool);
        SQLContainer container = new SQLContainer(t);
        Assert.assertEquals("Ville", container.getContainerProperty(
                new RowId(new Object[] { 0 + offset }), "NAME").getValue());
    }

    @Test
    public void getContainerProperty_tableExistingItemIdAndNonexistingPropertyId_returnsNull()
            throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Assert.assertNull(container.getContainerProperty(new RowId(
                new Object[] { 1 + offset }), "asdf"));
    }

    @Test
    public void getContainerProperty_tableNonexistingItemId_returnsNull()
            throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Assert.assertNull(container.getContainerProperty(new RowId(
                new Object[] { 1337 + offset }), "NAME"));
    }

    @Test
    public void getContainerPropertyIds_table_returnsIDAndNAME()
            throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Collection<?> propertyIds = container.getContainerPropertyIds();
        Assert.assertEquals(2, propertyIds.size());
        Assert.assertArrayEquals(new String[] { "ID", "NAME" }, propertyIds
                .toArray());
    }

    @Test
    public void getItem_tableExistingItemId_returnsItem() throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Item item = container.getItem(new RowId(new Object[] { 0 + offset }));
        Assert.assertNotNull(item);
        Assert.assertEquals("Ville", item.getItemProperty("NAME").getValue());
    }

    @Test
    public void getItem_table5000RowsWithParameter1337_returnsItemWithId1337()
            throws SQLException {
        addFiveThousand();
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Item item = container
                .getItem(new RowId(new Object[] { 1337 + offset }));
        Assert.assertNotNull(item);
        Assert.assertEquals(1337 + offset, item.getItemProperty("ID")
                .getValue());
        Assert.assertEquals("Person 1337", item.getItemProperty("NAME")
                .getValue());
    }

    @Test
    public void getItemIds_table_returnsItemIdsWithKeys0through3()
            throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Collection<?> itemIds = container.getItemIds();
        Assert.assertEquals(4, itemIds.size());
        RowId zero = new RowId(new Object[] { 0 + offset });
        RowId one = new RowId(new Object[] { 1 + offset });
        RowId two = new RowId(new Object[] { 2 + offset });
        RowId three = new RowId(new Object[] { 3 + offset });
        Assert.assertArrayEquals(new Object[] { zero, one, two, three },
                itemIds.toArray());
    }

    @Test
    public void getType_tableNAMEPropertyId_returnsString() throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Assert.assertEquals(String.class, container.getType("NAME"));
    }

    @Test
    public void getType_tableIDPropertyId_returnsInteger() throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Assert.assertEquals(Integer.class, container.getType("ID"));
    }

    @Test
    public void getType_tableNonexistingPropertyId_returnsNull()
            throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Assert.assertNull(container.getType("asdf"));
    }

    @Test
    public void size_table_returnsFour() throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Assert.assertEquals(4, container.size());
    }

    @Test
    public void size_tableOneAddedItem_returnsFive() throws SQLException {
        Connection conn = connectionPool.reserveConnection();
        Statement statement = conn.createStatement();
        statement.executeUpdate("insert into people values(default, 'Bengt')");
        statement.close();
        conn.commit();
        connectionPool.releaseConnection(conn);

        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Assert.assertEquals(5, container.size());
    }

    @Test
    public void indexOfId_tableWithParameterThree_returnsThree()
            throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Assert.assertEquals(3, container.indexOfId(new RowId(
                new Object[] { 3 + offset })));
    }

    @Test
    public void indexOfId_table5000RowsWithParameter1337_returns1337()
            throws SQLException {
        addFiveThousand();
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        container.getItem(new RowId(new Object[] { 1337 + offset }));
        Assert.assertEquals(1337, container.indexOfId(new RowId(
                new Object[] { 1337 + offset })));
    }

    @Test
    public void getIdByIndex_table5000rowsIndex1337_returnsRowId1337()
            throws SQLException {
        addFiveThousand();
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Object itemId = container.getIdByIndex(1337);
        Assert.assertEquals(new RowId(new Object[] { 1337 + offset }), itemId);
    }

    @Test
    public void getIdByIndex_tableWithPaging5000rowsIndex1337_returnsRowId1337()
            throws SQLException {
        addFiveThousand();
        TableQuery query = new TableQuery("people", connectionPool);
        SQLContainer container = new SQLContainer(query);
        Object itemId = container.getIdByIndex(1337);
        Assert.assertEquals(new RowId(new Object[] { 1337 + offset }), itemId);
    }

    @Test
    public void nextItemId_tableCurrentItem1337_returnsItem1338()
            throws SQLException {
        addFiveThousand();
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Object itemId = container.getIdByIndex(1337);
        Assert.assertEquals(new RowId(new Object[] { 1338 + offset }),
                container.nextItemId(itemId));
    }

    @Test
    public void prevItemId_tableCurrentItem1337_returns1336()
            throws SQLException {
        addFiveThousand();
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Object itemId = container.getIdByIndex(1337);
        Assert.assertEquals(new RowId(new Object[] { 1336 + offset }),
                container.prevItemId(itemId));
    }

    @Test
    public void firstItemId_table_returnsItemId0() throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Assert.assertEquals(new RowId(new Object[] { 0 + offset }), container
                .firstItemId());
    }

    @Test
    public void lastItemId_table5000Rows_returnsItemId4999()
            throws SQLException {
        addFiveThousand();

        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Assert.assertEquals(new RowId(new Object[] { 4999 + offset }),
                container.lastItemId());
    }

    @Test
    public void isFirstId_tableActualFirstId_returnsTrue() throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Assert.assertTrue(container.isFirstId(new RowId(
                new Object[] { 0 + offset })));
    }

    @Test
    public void isFirstId_tableSecondId_returnsFalse() throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Assert.assertFalse(container.isFirstId(new RowId(
                new Object[] { 1 + offset })));
    }

    @Test
    public void isLastId_tableSecondId_returnsFalse() throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Assert.assertFalse(container.isLastId(new RowId(
                new Object[] { 1 + offset })));
    }

    @Test
    public void isLastId_tableLastId_returnsTrue() throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Assert.assertTrue(container.isLastId(new RowId(
                new Object[] { 3 + offset })));
    }

    @Test
    public void isLastId_table5000RowsLastId_returnsTrue() throws SQLException {
        addFiveThousand();
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Assert.assertTrue(container.isLastId(new RowId(
                new Object[] { 4999 + offset })));
    }

    @Test
    public void refresh_table_sizeShouldUpdate() throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Assert.assertEquals(4, container.size());
        addFiveThousand();
        container.refresh();
        Assert.assertEquals(5000, container.size());
    }

    @Test
    public void refresh_tableWithoutCallingRefresh_sizeShouldNotUpdate()
            throws SQLException {
        // Yeah, this is a weird one. We're testing that the size doesn't update
        // after adding lots of items unless we call refresh inbetween. This to
        // make sure that the refresh method actually refreshes stuff and isn't
        // a NOP.
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Assert.assertEquals(4, container.size());
        addFiveThousand();
        Assert.assertEquals(4, container.size());
    }

    @Test
    public void setAutoCommit_freeform_shouldSucceed() throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        container.setAutoCommit(true);
        Assert.assertTrue(container.isAutoCommit());
        container.setAutoCommit(false);
        Assert.assertFalse(container.isAutoCommit());
    }

    @Test
    public void getPageLength_table_returnsDefault100() throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Assert.assertEquals(100, container.getPageLength());
    }

    @Test
    public void setPageLength_table_shouldSucceed() throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
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
    public void addItem_tableAddOneNewItem_returnsItemId() throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Object itemId = container.addItem();
        Assert.assertNotNull(itemId);
    }

    @Test
    public void addItem_tableAddOneNewItem_shouldChangeSize()
            throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        int size = container.size();
        container.addItem();
        Assert.assertEquals(size + 1, container.size());
    }

    @Test
    public void addItem_tableAddTwoNewItems_shouldChangeSize()
            throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        int size = container.size();
        Object id1 = container.addItem();
        Object id2 = container.addItem();
        Assert.assertEquals(size + 2, container.size());
        Assert.assertNotSame(id1, id2);
        Assert.assertFalse(id1.equals(id2));
    }

    @Test
    public void nextItemId_tableNewlyAddedItem_returnsNewlyAdded()
            throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Object lastId = container.lastItemId();
        Object id = container.addItem();
        Assert.assertEquals(id, container.nextItemId(lastId));
    }

    @Test
    public void lastItemId_tableNewlyAddedItem_returnsNewlyAdded()
            throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Object lastId = container.lastItemId();
        Object id = container.addItem();
        Assert.assertEquals(id, container.lastItemId());
        Assert.assertNotSame(lastId, container.lastItemId());
    }

    @Test
    public void indexOfId_tableNewlyAddedItem_returnsFour() throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Object id = container.addItem();
        Assert.assertEquals(4, container.indexOfId(id));
    }

    @Test
    public void getItem_tableNewlyAddedItem_returnsNewlyAdded()
            throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Object id = container.addItem();
        Assert.assertNotNull(container.getItem(id));
    }

    @Test
    public void getItemIds_tableNewlyAddedItem_containsNewlyAdded()
            throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Object id = container.addItem();
        Assert.assertTrue(container.getItemIds().contains(id));
    }

    @Test
    public void getContainerProperty_tableNewlyAddedItem_returnsPropertyOfNewlyAddedItem()
            throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Object id = container.addItem();
        Item item = container.getItem(id);
        item.getItemProperty("NAME").setValue("asdf");
        Assert.assertEquals("asdf", container.getContainerProperty(id, "NAME")
                .getValue());
    }

    @Test
    public void containsId_tableNewlyAddedItem_returnsTrue()
            throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Object id = container.addItem();
        Assert.assertTrue(container.containsId(id));
    }

    @Test
    public void prevItemId_tableTwoNewlyAddedItems_returnsFirstAddedItem()
            throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Object id1 = container.addItem();
        Object id2 = container.addItem();
        Assert.assertEquals(id1, container.prevItemId(id2));
    }

    @Test
    public void firstItemId_tableEmptyResultSet_returnsFirstAddedItem()
            throws SQLException {
        Connection conn = connectionPool.reserveConnection();
        Statement statement = conn.createStatement();
        try {
            statement.execute("drop table garbage");
        } catch (SQLException e) {
            // Don't worry if the table doesn't exist, since we don't want it to
        }
        statement.execute(createGarbage);
        conn.commit();
        connectionPool.releaseConnection(conn);
        SQLContainer container = new SQLContainer(new TableQuery("garbage",
                connectionPool));
        Object id = container.addItem();
        Assert.assertSame(id, container.firstItemId());
    }

    @Test
    public void isFirstId_tableEmptyResultSet_returnsFirstAddedItem()
            throws SQLException {
        Connection conn = connectionPool.reserveConnection();
        Statement statement = conn.createStatement();
        try {
            statement.execute("drop table garbage");
        } catch (SQLException e) {
            // Don't worry if the table doesn't exist, since we don't want it to
        }
        statement.execute(createGarbage);
        conn.commit();
        connectionPool.releaseConnection(conn);
        SQLContainer container = new SQLContainer(new TableQuery("garbage",
                connectionPool));
        Object id = container.addItem();
        Assert.assertTrue(container.isFirstId(id));
    }

    @Test
    public void isLastId_tableOneItemAdded_returnsTrueForAddedItem()
            throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Object id = container.addItem();
        Assert.assertTrue(container.isLastId(id));
    }

    @Test
    public void isLastId_tableTwoItemsAdded_returnsTrueForLastAddedItem()
            throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        container.addItem();
        Object id2 = container.addItem();
        Assert.assertTrue(container.isLastId(id2));
    }

    @Test
    public void getIdByIndex_tableOneItemAddedLastIndexInContainer_returnsAddedItem()
            throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Object id = container.addItem();
        Assert.assertEquals(id, container.getIdByIndex(container.size() - 1));
    }

    @Test
    public void removeItem_tableNoAddedItems_removesItemFromContainer()
            throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        int size = container.size();
        Object id = container.firstItemId();
        Assert.assertTrue(container.removeItem(id));
        Assert.assertNotSame(id, container.firstItemId());
        Assert.assertEquals(size - 1, container.size());
    }

    @Test
    public void containsId_tableRemovedItem_returnsFalse() throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Object id = container.firstItemId();
        Assert.assertTrue(container.removeItem(id));
        Assert.assertFalse(container.containsId(id));
    }

    @Test
    public void removeItem_tableOneAddedItem_removesTheAddedItem()
            throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Object id = container.addItem();
        int size = container.size();
        Assert.assertTrue(container.removeItem(id));
        Assert.assertFalse(container.containsId(id));
        Assert.assertEquals(size - 1, container.size());
    }

    @Test
    public void getItem_tableItemRemoved_returnsNull() throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Object id = container.firstItemId();
        Assert.assertTrue(container.removeItem(id));
        Assert.assertNull(container.getItem(id));
    }

    @Test
    public void getItem_tableAddedItemRemoved_returnsNull() throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Object id = container.addItem();
        Assert.assertNotNull(container.getItem(id));
        Assert.assertTrue(container.removeItem(id));
        Assert.assertNull(container.getItem(id));
    }

    @Test
    public void getItemIds_tableItemRemoved_shouldNotContainRemovedItem()
            throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Object id = container.firstItemId();
        Assert.assertTrue(container.getItemIds().contains(id));
        Assert.assertTrue(container.removeItem(id));
        Assert.assertFalse(container.getItemIds().contains(id));
    }

    @Test
    public void getItemIds_tableAddedItemRemoved_shouldNotContainRemovedItem()
            throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Object id = container.addItem();
        Assert.assertTrue(container.getItemIds().contains(id));
        Assert.assertTrue(container.removeItem(id));
        Assert.assertFalse(container.getItemIds().contains(id));
    }

    @Test
    public void containsId_tableItemRemoved_returnsFalse() throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Object id = container.firstItemId();
        Assert.assertTrue(container.containsId(id));
        Assert.assertTrue(container.removeItem(id));
        Assert.assertFalse(container.containsId(id));
    }

    @Test
    public void containsId_tableAddedItemRemoved_returnsFalse()
            throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Object id = container.addItem();
        Assert.assertTrue(container.containsId(id));
        Assert.assertTrue(container.removeItem(id));
        Assert.assertFalse(container.containsId(id));
    }

    @Test
    public void nextItemId_tableItemRemoved_skipsRemovedItem()
            throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Object first = container.getIdByIndex(0);
        Object second = container.getIdByIndex(1);
        Object third = container.getIdByIndex(2);
        Assert.assertTrue(container.removeItem(second));
        Assert.assertEquals(third, container.nextItemId(first));
    }

    @Test
    public void nextItemId_tableAddedItemRemoved_skipsRemovedItem()
            throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Object first = container.lastItemId();
        Object second = container.addItem();
        Object third = container.addItem();
        Assert.assertTrue(container.removeItem(second));
        Assert.assertEquals(third, container.nextItemId(first));
    }

    @Test
    public void prevItemId_tableItemRemoved_skipsRemovedItem()
            throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Object first = container.getIdByIndex(0);
        Object second = container.getIdByIndex(1);
        Object third = container.getIdByIndex(2);
        Assert.assertTrue(container.removeItem(second));
        Assert.assertEquals(first, container.prevItemId(third));
    }

    @Test
    public void prevItemId_tableAddedItemRemoved_skipsRemovedItem()
            throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Object first = container.lastItemId();
        Object second = container.addItem();
        Object third = container.addItem();
        Assert.assertTrue(container.removeItem(second));
        Assert.assertEquals(first, container.prevItemId(third));
    }

    @Test
    public void firstItemId_tableFirstItemRemoved_resultChanges()
            throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Object first = container.firstItemId();
        Assert.assertTrue(container.removeItem(first));
        Assert.assertNotSame(first, container.firstItemId());
    }

    @Test
    public void firstItemId_tableNewlyAddedFirstItemRemoved_resultChanges()
            throws SQLException {
        Connection conn = connectionPool.reserveConnection();
        Statement statement = conn.createStatement();
        try {
            statement.execute("drop table garbage");
        } catch (SQLException e) {
            // Don't worry if the table doesn't exist, since we don't want it to
        }
        statement.execute(createGarbage);
        conn.commit();
        connectionPool.releaseConnection(conn);
        SQLContainer container = new SQLContainer(new TableQuery("garbage",
                connectionPool));
        Object first = container.addItem();
        Object second = container.addItem();
        Assert.assertSame(first, container.firstItemId());
        Assert.assertTrue(container.removeItem(first));
        Assert.assertSame(second, container.firstItemId());
    }

    @Test
    public void lastItemId_tableLastItemRemoved_resultChanges()
            throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Object last = container.lastItemId();
        Assert.assertTrue(container.removeItem(last));
        Assert.assertNotSame(last, container.lastItemId());
    }

    @Test
    public void lastItemId_tableAddedLastItemRemoved_resultChanges()
            throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Object last = container.addItem();
        Assert.assertSame(last, container.lastItemId());
        Assert.assertTrue(container.removeItem(last));
        Assert.assertNotSame(last, container.lastItemId());
    }

    @Test
    public void isFirstId_tableFirstItemRemoved_returnsFalse()
            throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Object first = container.firstItemId();
        Assert.assertTrue(container.removeItem(first));
        Assert.assertFalse(container.isFirstId(first));
    }

    @Test
    public void isFirstId_tableAddedFirstItemRemoved_returnsFalse()
            throws SQLException {
        Connection conn = connectionPool.reserveConnection();
        Statement statement = conn.createStatement();
        try {
            statement.execute("drop table garbage");
        } catch (SQLException e) {
            // Don't worry if the table doesn't exist, since we don't want it to
        }
        statement.execute(createGarbage);
        conn.commit();
        connectionPool.releaseConnection(conn);
        SQLContainer container = new SQLContainer(new TableQuery("garbage",
                connectionPool));
        Object first = container.addItem();
        container.addItem();
        Assert.assertSame(first, container.firstItemId());
        Assert.assertTrue(container.removeItem(first));
        Assert.assertFalse(container.isFirstId(first));
    }

    @Test
    public void isLastId_tableLastItemRemoved_returnsFalse()
            throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Object last = container.lastItemId();
        Assert.assertTrue(container.removeItem(last));
        Assert.assertFalse(container.isLastId(last));
    }

    @Test
    public void isLastId_tableAddedLastItemRemoved_returnsFalse()
            throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Object last = container.addItem();
        Assert.assertSame(last, container.lastItemId());
        Assert.assertTrue(container.removeItem(last));
        Assert.assertFalse(container.isLastId(last));
    }

    @Test
    public void indexOfId_tableItemRemoved_returnsNegOne() throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Object id = container.getIdByIndex(2);
        Assert.assertTrue(container.removeItem(id));
        Assert.assertEquals(-1, container.indexOfId(id));
    }

    @Test
    public void indexOfId_tableAddedItemRemoved_returnsNegOne()
            throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Object id = container.addItem();
        Assert.assertTrue(container.indexOfId(id) != -1);
        Assert.assertTrue(container.removeItem(id));
        Assert.assertEquals(-1, container.indexOfId(id));
    }

    @Test
    public void getIdByIndex_tableItemRemoved_resultChanges()
            throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Object id = container.getIdByIndex(2);
        Assert.assertTrue(container.removeItem(id));
        Assert.assertNotSame(id, container.getIdByIndex(2));
    }

    @Test
    public void getIdByIndex_tableAddedItemRemoved_resultChanges()
            throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Object id = container.addItem();
        container.addItem();
        int index = container.indexOfId(id);
        Assert.assertTrue(container.removeItem(id));
        Assert.assertNotSame(id, container.getIdByIndex(index));
    }

    @Test
    public void removeAllItems_table_shouldSucceed() throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Assert.assertTrue(container.removeAllItems());
        Assert.assertEquals(0, container.size());
    }

    @Test
    public void removeAllItems_tableAddedItems_shouldSucceed()
            throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        container.addItem();
        container.addItem();
        Assert.assertTrue(container.removeAllItems());
        Assert.assertEquals(0, container.size());
    }

    @Test
    public void commit_tableAddedItem_shouldBeWrittenToDB() throws SQLException {
        TableQuery query = new TableQuery("people", connectionPool);
        SQLContainer container = new SQLContainer(query);
        Object id = container.addItem();
        container.getContainerProperty(id, "NAME").setValue("New Name");
        Assert.assertTrue(id instanceof TemporaryRowId);
        Assert.assertSame(id, container.lastItemId());
        container.commit();
        Assert.assertFalse(container.lastItemId() instanceof TemporaryRowId);
        Assert.assertEquals("New Name", container.getContainerProperty(
                container.lastItemId(), "NAME").getValue());
    }

    @Test
    public void commit_tableTwoAddedItems_shouldBeWrittenToDB()
            throws SQLException {
        TableQuery query = new TableQuery("people", connectionPool);
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
        Assert.assertEquals("Herbert", container.getContainerProperty(
                nextToLast, "NAME").getValue());
        Assert.assertFalse(container.lastItemId() instanceof TemporaryRowId);
        Assert.assertEquals("Larry", container.getContainerProperty(
                container.lastItemId(), "NAME").getValue());
    }

    @Test
    public void commit_tableRemovedItem_shouldBeRemovedFromDB()
            throws SQLException {
        TableQuery query = new TableQuery("people", connectionPool);
        SQLContainer container = new SQLContainer(query);
        Object last = container.lastItemId();
        container.removeItem(last);
        container.commit();
        Assert.assertFalse(last.equals(container.lastItemId()));
    }

    @Test
    public void commit_tableLastItemUpdated_shouldUpdateRowInDB()
            throws SQLException {
        TableQuery query = new TableQuery("people", connectionPool);
        query.setVersionColumn("ID");
        SQLContainer container = new SQLContainer(query);
        Object last = container.lastItemId();
        container.getContainerProperty(last, "NAME").setValue("Donald");
        container.commit();
        Assert.assertEquals("Donald", container.getContainerProperty(
                container.lastItemId(), "NAME").getValue());
    }

    @Test
    public void rollback_tableItemAdded_discardsAddedItem() throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
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
    public void rollback_tableItemRemoved_restoresRemovedItem()
            throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        int size = container.size();
        Object last = container.lastItemId();
        container.removeItem(last);
        Assert.assertEquals(size - 1, container.size());
        container.rollback();
        Assert.assertEquals(size, container.size());
        Assert.assertEquals(last, container.lastItemId());
    }

    @Test
    public void rollback_tableItemChanged_discardsChanges() throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Object last = container.lastItemId();
        container.getContainerProperty(last, "NAME").setValue("foo");
        container.rollback();
        Assert.assertFalse("foo".equals(container.getContainerProperty(
                container.lastItemId(), "NAME").getValue()));
    }

    @Test
    public void itemChangeNotification_table_isModifiedReturnsTrue()
            throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Assert.assertFalse(container.isModified());
        RowItem last = (RowItem) container.getItem(container.lastItemId());
        container.itemChangeNotification(last);
        Assert.assertTrue(container.isModified());
    }

    @Test
    public void itemSetChangeListeners_table_shouldFire() throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        ItemSetChangeListener listener = EasyMock
                .createMock(ItemSetChangeListener.class);
        listener.containerItemSetChange(EasyMock.isA(ItemSetChangeEvent.class));
        EasyMock.replay(listener);

        container.addListener(listener);
        container.addItem();

        EasyMock.verify(listener);
    }

    @Test
    public void itemSetChangeListeners_tableItemRemoved_shouldFire()
            throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
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
    public void removeListener_table_shouldNotFire() throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        ItemSetChangeListener listener = EasyMock
                .createMock(ItemSetChangeListener.class);
        EasyMock.replay(listener);

        container.addListener(listener);
        container.removeListener(listener);
        container.addItem();

        EasyMock.verify(listener);
    }

    @Test
    public void isModified_tableRemovedItem_returnsTrue() throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Assert.assertFalse(container.isModified());
        container.removeItem(container.lastItemId());
        Assert.assertTrue(container.isModified());
    }

    @Test
    public void isModified_tableAddedItem_returnsTrue() throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Assert.assertFalse(container.isModified());
        container.addItem();
        Assert.assertTrue(container.isModified());
    }

    @Test
    public void isModified_tableChangedItem_returnsTrue() throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Assert.assertFalse(container.isModified());
        container.getContainerProperty(container.lastItemId(), "NAME")
                .setValue("foo");
        Assert.assertTrue(container.isModified());
    }

    @Test
    public void getSortableContainerPropertyIds_table_returnsAllPropertyIds()
            throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        Collection<?> sortableIds = container.getSortableContainerPropertyIds();
        Assert.assertTrue(sortableIds.contains("ID"));
        Assert.assertTrue(sortableIds.contains("NAME"));
        Assert.assertEquals(2, sortableIds.size());
    }

    @Test
    public void addOrderBy_freeform_shouldReorderResults() throws SQLException {
        TableQuery query = new TableQuery("people", connectionPool);
        SQLContainer container = new SQLContainer(query);
        // Ville, Kalle, Pelle, Börje
        Assert.assertEquals("Ville", container.getContainerProperty(
                container.firstItemId(), "NAME").getValue());
        Assert.assertEquals("Börje", container.getContainerProperty(
                container.lastItemId(), "NAME").getValue());

        container.addOrderBy(new OrderBy("NAME", true));
        // Börje, Kalle, Pelle, Ville
        Assert.assertEquals("Börje", container.getContainerProperty(
                container.firstItemId(), "NAME").getValue());
        Assert.assertEquals("Ville", container.getContainerProperty(
                container.lastItemId(), "NAME").getValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void addOrderBy_tableIllegalColumn_shouldFail() throws SQLException {
        SQLContainer container = new SQLContainer(new TableQuery("people",
                connectionPool));
        container.addOrderBy(new OrderBy("asdf", true));
    }

    @Test
    public void sort_freeform_sortsByName() throws SQLException {
        TableQuery query = new TableQuery("people", connectionPool);
        SQLContainer container = new SQLContainer(query);
        // Ville, Kalle, Pelle, Börje
        Assert.assertEquals("Ville", container.getContainerProperty(
                container.firstItemId(), "NAME").getValue());
        Assert.assertEquals("Börje", container.getContainerProperty(
                container.lastItemId(), "NAME").getValue());

        container.sort(new Object[] { "NAME" }, new boolean[] { true });

        // Börje, Kalle, Pelle, Ville
        Assert.assertEquals("Börje", container.getContainerProperty(
                container.firstItemId(), "NAME").getValue());
        Assert.assertEquals("Ville", container.getContainerProperty(
                container.lastItemId(), "NAME").getValue());
    }

    @Test
    public void addFilter_freeform_filtersResults() throws SQLException {
        TableQuery query = new TableQuery("people", connectionPool);
        SQLContainer container = new SQLContainer(query);
        // Ville, Kalle, Pelle, Börje
        Assert.assertEquals(4, container.size());
        Assert.assertEquals("Börje", container.getContainerProperty(
                container.lastItemId(), "NAME").getValue());

        container
                .addFilter(new Filter("NAME", ComparisonType.ENDS_WITH, "lle"));
        // Ville, Kalle, Pelle
        Assert.assertEquals(3, container.size());
        Assert.assertEquals("Pelle", container.getContainerProperty(
                container.lastItemId(), "NAME").getValue());
    }

    @Test
    public void addContainerFilter_filtersResults() throws SQLException {
        TableQuery query = new TableQuery("people", connectionPool);
        SQLContainer container = new SQLContainer(query);
        // Ville, Kalle, Pelle, Börje
        Assert.assertEquals(4, container.size());

        container.addContainerFilter("NAME", "Vi", false, false);

        // Ville
        Assert.assertEquals(1, container.size());
        Assert.assertEquals("Ville", container.getContainerProperty(
                container.lastItemId(), "NAME").getValue());
    }

    @Test
    public void addContainerFilter_ignoreCase_filtersResults()
            throws SQLException {
        TableQuery query = new TableQuery("people", connectionPool);
        SQLContainer container = new SQLContainer(query);
        // Ville, Kalle, Pelle, Börje
        Assert.assertEquals(4, container.size());

        container.addContainerFilter("NAME", "vi", true, false);

        // Ville
        Assert.assertEquals(1, container.size());
        Assert.assertEquals("Ville", container.getContainerProperty(
                container.lastItemId(), "NAME").getValue());
    }

    @Test
    public void removeAllContainerFilters_freeform_noFiltering()
            throws SQLException {
        TableQuery query = new TableQuery("people", connectionPool);
        SQLContainer container = new SQLContainer(query);
        // Ville, Kalle, Pelle, Börje
        Assert.assertEquals(4, container.size());

        container.addContainerFilter("NAME", "Vi", false, false);

        // Ville
        Assert.assertEquals(1, container.size());
        Assert.assertEquals("Ville", container.getContainerProperty(
                container.lastItemId(), "NAME").getValue());

        container.removeAllContainerFilters();

        Assert.assertEquals(4, container.size());
        Assert.assertEquals("Börje", container.getContainerProperty(
                container.lastItemId(), "NAME").getValue());
    }

    @Test
    public void removeContainerFilters_freeform_noFiltering()
            throws SQLException {
        TableQuery query = new TableQuery("people", connectionPool);
        SQLContainer container = new SQLContainer(query);
        // Ville, Kalle, Pelle, Börje
        Assert.assertEquals(4, container.size());

        container.addContainerFilter("NAME", "Vi", false, false);

        // Ville
        Assert.assertEquals(1, container.size());
        Assert.assertEquals("Ville", container.getContainerProperty(
                container.lastItemId(), "NAME").getValue());

        container.removeContainerFilters("NAME");

        Assert.assertEquals(4, container.size());
        Assert.assertEquals("Börje", container.getContainerProperty(
                container.lastItemId(), "NAME").getValue());
    }

    @Test
    public void addFilter_tableBufferedItems_alsoFiltersBufferedItems()
            throws SQLException {
        TableQuery query = new TableQuery("people", connectionPool);
        SQLContainer container = new SQLContainer(query);
        // Ville, Kalle, Pelle, Börje
        Assert.assertEquals(4, container.size());
        Assert.assertEquals("Börje", container.getContainerProperty(
                container.lastItemId(), "NAME").getValue());

        Object id1 = container.addItem();
        container.getContainerProperty(id1, "NAME").setValue("Palle");
        Object id2 = container.addItem();
        container.getContainerProperty(id2, "NAME").setValue("Bengt");

        container
                .addFilter(new Filter("NAME", ComparisonType.ENDS_WITH, "lle"));

        // Ville, Kalle, Pelle, Palle
        Assert.assertEquals(4, container.size());
        Assert.assertEquals("Ville", container.getContainerProperty(
                container.getIdByIndex(0), "NAME").getValue());
        Assert.assertEquals("Kalle", container.getContainerProperty(
                container.getIdByIndex(1), "NAME").getValue());
        Assert.assertEquals("Pelle", container.getContainerProperty(
                container.getIdByIndex(2), "NAME").getValue());
        Assert.assertEquals("Palle", container.getContainerProperty(
                container.getIdByIndex(3), "NAME").getValue());

        Assert.assertNull(container.getIdByIndex(4));
        Assert.assertNull(container.nextItemId(container.getIdByIndex(3)));

        Assert.assertFalse(container.containsId(id2));
        Assert.assertFalse(container.getItemIds().contains(id2));

        Assert.assertNull(container.getItem(id2));
        Assert.assertEquals(-1, container.indexOfId(id2));

        Assert.assertNotSame(id2, container.lastItemId());
        Assert.assertSame(id1, container.lastItemId());
    }

    @Test
    public void sort_tableBufferedItems_sortsBufferedItemsLastInOrderAdded()
            throws SQLException {
        TableQuery query = new TableQuery("people", connectionPool);
        SQLContainer container = new SQLContainer(query);
        // Ville, Kalle, Pelle, Börje
        Assert.assertEquals("Ville", container.getContainerProperty(
                container.firstItemId(), "NAME").getValue());
        Assert.assertEquals("Börje", container.getContainerProperty(
                container.lastItemId(), "NAME").getValue());

        Object id1 = container.addItem();
        container.getContainerProperty(id1, "NAME").setValue("Wilbert");
        Object id2 = container.addItem();
        container.getContainerProperty(id2, "NAME").setValue("Albert");

        container.sort(new Object[] { "NAME" }, new boolean[] { true });

        // Börje, Kalle, Pelle, Ville, Wilbert, Albert
        Assert.assertEquals("Börje", container.getContainerProperty(
                container.firstItemId(), "NAME").getValue());
        Assert.assertEquals("Wilbert", container.getContainerProperty(
                container.getIdByIndex(container.size() - 2), "NAME")
                .getValue());
        Assert.assertEquals("Albert", container.getContainerProperty(
                container.lastItemId(), "NAME").getValue());
    }

}
