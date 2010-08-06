package com.vaadin.addon.sqlcontainer;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EventObject;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.vaadin.addon.sqlcontainer.query.Filter;
import com.vaadin.addon.sqlcontainer.query.OrderBy;
import com.vaadin.addon.sqlcontainer.query.QueryDelegate;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;

public class SQLContainer implements Container, Container.Filterable,
        Container.Indexed, Container.Sortable, Container.ItemSetChangeNotifier {
    private static final long serialVersionUID = -3863564310693712511L;

    /* Query delegate and related settings */
    private QueryDelegate delegate;
    private boolean autoCommit = false;

    /* Page length = number of items fetched in one query */
    public static final int DEFAULT_PAGE_LENGTH = 100;
    private int pageLength = DEFAULT_PAGE_LENGTH;

    /* Cache ratio = number of items to cache = CACHE_RATIO pageLength */
    public static final int CACHE_RATIO = 2;

    /* Item and index caches */
    private Map<Integer, RowId> itemIndexes = new HashMap<Integer, RowId>();
    private CacheMap<RowId, RowItem> cachedItems = new CacheMap<RowId, RowItem>();

    /* Container properties = column names and data types */
    private List<String> propertyIds = new ArrayList<String>();
    private Map<String, Class<?>> propertyTypes = new HashMap<String, Class<?>>();

    /* Filters (WHERE) and sorters (ORDER BY) */
    private List<Filter> filters = new ArrayList<Filter>();
    private List<OrderBy> sorters = new ArrayList<OrderBy>();

    /*
     * Size = total number of items available in the data source using the
     * current query, filters and sorters.
     */
    private int size;

    /*
     * Do not update size from data source if it has been updated in the last n
     * milliseconds
     */
    private final int sizeValidMilliSeconds = 10000;
    private boolean sizeDirty = true;
    private Date sizeUpdated = new Date();

    /* Currently fetched page number */
    private int currentOffset;

    /* Listeners */
    private LinkedList<Container.ItemSetChangeListener> itemSetChangeListeners;

    /* Temporary storage for items to be removed and added */
    private Map<RowId, RowItem> removedItems = new HashMap<RowId, RowItem>();
    private Map<RowId, RowItem> addedItems = new HashMap<RowId, RowItem>();
    private Map<RowId, RowItem> modifiedItems = new HashMap<RowId, RowItem>();

    /**
     * Prevent instantiation without a QueryDelegate.
     */
    private SQLContainer() {
    }

    /**
     * Creates and initializes SQLContainer using the given QueryDelegate
     * 
     * @param delegate
     *            QueryDelegate implementation
     * @throws SQLException
     */
    public SQLContainer(QueryDelegate delegate) throws SQLException {
        if (delegate == null) {
            throw new IllegalArgumentException(
                    "QueryDelegate must not be null.");
        }
        this.delegate = delegate;
        getPropertyIds();
        cachedItems.setCacheLimit(CACHE_RATIO * getPageLength());
    }

    /**************************************/
    /** Methods from interface Container **/
    /**************************************/
    public Object addItem() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
        // TODO Implement write support
    }

    public boolean containsId(Object itemId) {
        if (!cachedItems.containsKey(itemId)) {
            updateOffsetAndCache(indexOfId(itemId));
        }
        return cachedItems.containsKey(itemId);
    }

    public Property getContainerProperty(Object itemId, Object propertyId) {
        if (!cachedItems.containsKey(itemId)) {
            updateOffsetAndCache(indexOfId(itemId));
        }
        if (cachedItems.containsKey(itemId)) {
            return cachedItems.get(itemId).getItemProperty(propertyId);
        }
        return null;
    }

    public Collection<?> getContainerPropertyIds() {
        return Collections.unmodifiableCollection(propertyIds);
    }

    public Item getItem(Object itemId) {
        if (!cachedItems.containsKey(itemId)) {
            updateOffsetAndCache(indexOfId(itemId));
        }
        return cachedItems.get(itemId);
    }

    public Collection<?> getItemIds() {
        getRowIdentifiers();
        return Collections.unmodifiableCollection(itemIndexes.values());
    }

    public Class<?> getType(Object propertyId) {
        if (!propertyIds.contains(propertyId)) {
            return null;
        }
        return propertyTypes.get(propertyId);
    }

    public int size() {
        updateCount();
        return size;
    }

    public boolean removeItem(Object itemId)
            throws UnsupportedOperationException {
        if (!containsId(itemId)) {
            return false;
        }
        /* If auto commit mode is enabled, the row will be instantly removed. */
        if (autoCommit) {
            Item i = getItem(itemId);
            if (i == null) {
                return false;
            }
            try {
                delegate.commit();
                delegate.beginTransaction();
                boolean success = delegate.removeRow((RowItem) i);
                delegate.commit();
                refresh();
                return success;
            } catch (SQLException e) {
                try {
                    delegate.rollback();
                } catch (SQLException ee) {
                    /* Nothing can be done here */
                }
                return false;
            }
        } else {
            removedItems.put((RowId) itemId, (RowItem) getItem(itemId));
            cachedItems.remove(itemId);
            refresh();
            return true;
        }
    }

    public boolean removeAllItems() throws UnsupportedOperationException {
        /*
         * If auto commit mode is enabled, all the rows will be instantly
         * removed. Note that they are still removed within one transaction so
         * if any removal fails, all actions will be rolled back.
         */
        if (autoCommit) {
            try {
                delegate.commit();
                delegate.beginTransaction();
                boolean success = true;
                for (Object id : getItemIds()) {
                    if (!delegate.removeRow((RowItem) getItem(id))) {
                        success = false;
                    }
                }
                if (success) {
                    delegate.commit();
                    refresh();
                } else {
                    delegate.rollback();
                }
                return success;
            } catch (SQLException e) {
                try {
                    delegate.rollback();
                } catch (SQLException ee) {
                    /* Nothing can be done here */
                }
                return false;
            }
        } else {
            for (Object id : getItemIds()) {
                removedItems.put((RowId) id, (RowItem) getItem(id));
                cachedItems.remove(id);
            }
            refresh();
            return true;
        }
    }

    /*************************************************/
    /** Methods from interface Container.Filterable **/
    /*************************************************/
    public void addContainerFilter(Object propertyId, String filterString,
            boolean ignoreCase, boolean onlyMatchPrefix) {
        if (propertyId == null || !propertyIds.contains(propertyId)) {
            return;
        }
        /* Generate Filter -object */
        Filter.ComparisonType ct = Filter.ComparisonType.CONTAINS;
        if (onlyMatchPrefix) {
            ct = Filter.ComparisonType.STARTS_WITH;
        }
        Filter f = new Filter((String) propertyId, ct, filterString);
        f.setNeedsQuotes(true);
        f.setCaseSensitive(!ignoreCase);
        filters.add(f);
        refresh();
    }

    public void removeAllContainerFilters() {
        filters.clear();
        refresh();
    }

    public void removeContainerFilters(Object propertyId) {
        List<Filter> toRemove = new ArrayList<Filter>();
        for (Filter f : filters) {
            if (f.getColumn().equals(propertyId)) {
                toRemove.add(f);
            }
        }
        if (!toRemove.isEmpty()) {
            for (Filter f : toRemove) {
                filters.remove(f);
            }
            refresh();
        }
    }

    /**********************************************/
    /** Methods from interface Container.Indexed **/
    /**********************************************/
    public int indexOfId(Object itemId) {
        if (!itemIndexes.containsValue(itemId)) {
            getRowIdentifiers();
        }
        for (Integer i : itemIndexes.keySet()) {
            if (itemIndexes.get(i).equals(itemId)) {
                return i;
            }
        }
        return -1;
    }

    public Object getIdByIndex(int index) {
        updateCount();
        if (index < 0 || index > size() - 1) {
            return null;
        }
        if (itemIndexes.keySet().contains(index)) {
            return itemIndexes.get(index);
        }
        updateOffsetAndCache(index);
        return itemIndexes.get(index);
    }

    /**********************************************/
    /** Methods from interface Container.Ordered **/
    /**********************************************/
    public Object nextItemId(Object itemId) {
        return getIdByIndex(indexOfId(itemId) + 1);
    }

    public Object prevItemId(Object itemId) {
        return getIdByIndex(indexOfId(itemId) - 1);
    }

    public Object firstItemId() {
        if (!itemIndexes.containsKey(0)) {
            getRowIdentifiers();
        }
        return itemIndexes.get(0);
    }

    public Object lastItemId() {
        if (!itemIndexes.containsKey(size - 1)) {
            getRowIdentifiers();
        }
        return itemIndexes.get(size - 1);
    }

    public boolean isFirstId(Object itemId) {
        if (!itemIndexes.containsKey(0)) {
            getRowIdentifiers();
        }
        if (itemIndexes.get(0).equals(itemId)) {
            return true;
        }
        return false;
    }

    public boolean isLastId(Object itemId) {
        if (!itemIndexes.containsKey(size - 1)) {
            getRowIdentifiers();
        }
        if (itemIndexes.get(size - 1).equals(itemId)) {
            return true;
        }
        return false;
    }

    /***********************************************/
    /** Methods from interface Container.Sortable **/
    /***********************************************/
    public void sort(Object[] propertyId, boolean[] ascending) {
        sorters.clear();
        if (propertyId == null || propertyId.length == 0) {
            refresh();
            return;
        }
        /* Generate OrderBy -objects */
        boolean asc = true;
        for (int i = 0; i < propertyId.length; i++) {
            /* Check that the property id is valid */
            if (propertyId[i] instanceof String
                    && propertyIds.contains(propertyId[i])) {
                try {
                    asc = ascending[i];
                } catch (Exception e) {
                }
                sorters.add(new OrderBy((String) propertyId[i], asc));
            }
        }
        refresh();
    }

    public Collection<?> getSortableContainerPropertyIds() {
        return getContainerPropertyIds();
    }

    /**************************************/
    /** Methods specific to SQLContainer **/
    /**************************************/
    /**
     * Refreshes the container - clears all caches and resets size and offset.
     * Does NOT remove sorting or filtering rules!
     */
    public void refresh() {
        sizeDirty = true;
        currentOffset = 0;
        cachedItems.clear();
        itemIndexes.clear();
        fireContentsChange();
    }

    /**
     * Returns modify state of the container.
     * 
     * @return true if contents of this container have been modified
     */
    public boolean isModified() {
        if (!removedItems.isEmpty() || !addedItems.isEmpty()
                || !modifiedItems.isEmpty()) {
            return true;
        }
        return false;
    }

    /**
     * Set auto commit mode enabled or disabled. Auto commit mode means that all
     * changes made to items of this container will be immediately written to
     * the underlying data source.
     * 
     * @param autoCommitEnabled
     *            true to enable auto commit mode
     */
    public void setAutoCommit(boolean autoCommitEnabled) {
        autoCommit = autoCommitEnabled;
    }

    /**
     * Returns status of the auto commit mode.
     * 
     * @return true if auto commit mode is enabled
     */
    public boolean isAutoCommit() {
        return autoCommit;
    }

    /**
     * Returns the currently set page length.
     * 
     * @return current page length
     */
    public int getPageLength() {
        return pageLength;
    }

    /**
     * Sets the page length used in lazy fetching of items from the data source.
     * Also resets the cache size to match the new page length.
     * 
     * @param pageLength
     *            new page length
     */
    public void setPageLength(int pageLength) {
        this.pageLength = pageLength > 0 ? pageLength : DEFAULT_PAGE_LENGTH;
        cachedItems.setCacheLimit(CACHE_RATIO * getPageLength());
    }

    /**
     * Adds the given Filter to this container and refreshes the container
     * contents with the new filtering rules.
     * 
     * Note that filter.getColumn() must return a column name that exists in
     * this container.
     * 
     * @param filter
     *            Filter to be added to the set of filters of this container
     */
    public void addFilter(Filter filter) {
        if (filter == null) {
            return;
        }
        if (!propertyIds.contains(filter.getColumn())) {
            throw new IllegalArgumentException(
                    "The column given for sorting does not exist in this container.");
        }
        filters.add(filter);
    }

    /**
     * Adds the given OrderBy to this container and refreshes the container
     * contents with the new sorting rules.
     * 
     * Note that orderBy.getColumn() must return a column name that exists in
     * this container.
     * 
     * @param orderBy
     *            OrderBy to be added to the container sorting rules
     */
    public void addOrderBy(OrderBy orderBy) {
        if (orderBy == null) {
            return;
        }
        if (!propertyIds.contains(orderBy.getColumn())) {
            throw new IllegalArgumentException(
                    "The column given for sorting does not exist in this container.");
        }
        sorters.add(orderBy);
        refresh();
    }

    /**
     * Commits all the changes, additions and removals made to the items of this
     * container.
     * 
     * @throws UnsupportedOperationException
     * @throws SQLException
     */
    public void commit() throws UnsupportedOperationException, SQLException {
        try {
            delegate.commit();
            delegate.beginTransaction();
            /* Perform buffered deletions */
            for (RowItem id : removedItems.values()) {
                if (!delegate.removeRow(id)) {
                    throw new SQLException("Removal failed for row with ID: "
                            + id.getId());
                }
            }
            // TODO: Perform updates
            // TODO: Perform inserts
            delegate.commit();
            removedItems.clear();
            addedItems.clear();
            modifiedItems.clear();
            refresh();
        } catch (SQLException e) {
            delegate.rollback();
            throw e;
        }
    }

    /**
     * Rolls back all the changes, additions and removals made to the items of
     * this container.
     * 
     * @throws UnsupportedOperationException
     * @throws SQLException
     */
    public void rollback() throws UnsupportedOperationException, SQLException {
        /* Discard removed, added and modified items */
        removedItems.clear();
        addedItems.clear();
        modifiedItems.clear();
        /*
         * Refresh container to clear item cache ,container size etc. which may
         * contain modifications
         */
        refresh();
    }

    /**
     * Notifies this container that a property in the given item has been
     * modified. The change will be buffered or made instantaneously depending
     * on auto commit mode.
     * 
     * @param changedItem
     *            item that has a modified property
     */
    void itemChangeNotification(RowItem changedItem) {
        if (autoCommit) {
            try {
                delegate.commit();
                delegate.beginTransaction();
                delegate.storeRow(changedItem);
                delegate.commit();
            } catch (SQLException e) {
                try {
                    delegate.rollback();
                } catch (SQLException ee) {
                    /* Nothing can be done here */
                }
            }
        } else {
            modifiedItems.put(changedItem.getId(), changedItem);
            cachedItems.remove(changedItem.getId());
        }
    }

    /**
     * Determines a new offset for updating the row cache. The offset is
     * calculated from the given index, and will be fixed to match the start of
     * a page, based on the value of pageLength.
     * 
     * @param index
     *            Index of the item that was requested, but not found in cache
     */
    private void updateOffsetAndCache(int index) {
        if (index < 0) {
            return;
        }
        currentOffset = index / pageLength;
        currentOffset *= pageLength;
        getPage();
    }

    /**
     * Fetches new count of rows from the data source, if needed.
     */
    private void updateCount() {
        // TODO: Adjust size to reflect added/removed items!
        /*
         * This may be quite complicated due to the filters and sorters; it will
         * be quite annoying to add/remove the non-committed rows into correct
         * indexes.
         */
        if (!sizeDirty
                && new Date().getTime() < sizeUpdated.getTime()
                        + sizeValidMilliSeconds) {
            return;
        }
        try {
            delegate.setFilters(filters);
            delegate.setOrderBy(sorters);
            int newSize = delegate.getCount();
            if (newSize != size) {
                size = newSize;
                refresh();
            }
            sizeUpdated = new Date();
            sizeDirty = false;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update item set size.", e);
        }
    }

    /**
     * Fetches all the row identifiers from the data source.
     */
    private void getRowIdentifiers() {
        // TODO: Discard removed item id's from the identifier list
        updateCount();
        if (itemIndexes.size() != size) {
            ResultSet rs = null;
            try {
                rs = delegate.getIdList();
                List<String> pKeys = delegate.getPrimaryKeyColumns();
                int rowCount = 0;
                while (rs.next()) {
                    /* Generate itemId for the row based on primary key(s) */
                    Object[] itemId = new Object[pKeys.size()];
                    for (int i = 0; i < pKeys.size(); i++) {
                        itemId[i] = rs.getObject(pKeys.get(i));
                    }
                    RowId id = new RowId(itemId);
                    itemIndexes.put(rowCount, id);
                    rowCount++;
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to fetch item indexes.", e);
            }
        }
    }

    /**
     * Fetches property id's (column names and their types) from the data
     * source.
     * 
     * Note that if the table contains (or query returns) no items, it is
     * impossible to determine the data types of the columns.
     */
    private void getPropertyIds() {
        propertyIds.clear();
        propertyTypes.clear();
        delegate.setFilters(null);
        delegate.setOrderBy(null);
        ResultSet rs = null;
        ResultSetMetaData rsmd = null;
        try {
            rs = delegate.getResults(0, 1);
            boolean resultExists = rs.next();
            rsmd = rs.getMetaData();
            Object o = null;
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                propertyIds.add(rsmd.getColumnName(i));
                if (resultExists) {
                    o = rs.getObject(i);
                }
                propertyTypes.put(rsmd.getColumnName(i),
                        o == null ? new Object().getClass() : o.getClass());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch property id's.", e);
        }
    }

    /**
     * Fetches a page from the data source based on the values of pageLenght and
     * currentOffset. Also updates the set of primary keys, used in
     * identification of RowItems.
     */
    private void getPage() {
        updateCount();
        ResultSet rs = null;
        ResultSetMetaData rsmd = null;
        try {
            rs = delegate.getResults(currentOffset, pageLength);
            rsmd = rs.getMetaData();
            List<String> pKeys = delegate.getPrimaryKeyColumns();
            if (pKeys == null || pKeys.isEmpty()) {
                throw new IllegalStateException("No primary key column(s) set.");
            }
            /* Create new items and column properties */
            ColumnProperty cp = null;
            int rowCount = currentOffset;
            while (rs.next()) {
                List<ColumnProperty> itemProperties = new ArrayList<ColumnProperty>();
                int columnNum = 1;
                /* Generate row itemId based on primary key(s) */
                Object[] itemId = new Object[pKeys.size()];
                for (int i = 0; i < pKeys.size(); i++) {
                    itemId[i] = rs.getObject(pKeys.get(i));
                }
                RowId id = new RowId(itemId);
                // TODO: If the item is removed, skip adding it to cache
                // TODO: If the item is modified, skip adding it to cache
                for (String s : propertyIds) {
                    /* Determine column meta data */
                    boolean nullable = rsmd.isNullable(columnNum) == ResultSetMetaData.columnNullable;
                    boolean allowReadOnlyChange = !rsmd.isReadOnly(columnNum)
                            && !rsmd.isAutoIncrement(columnNum);
                    boolean readOnly = rsmd.isReadOnly(columnNum)
                            || rsmd.isAutoIncrement(columnNum);
                    Object value = rs.getObject(columnNum);
                    if (value != null) {
                        cp = new ColumnProperty(s, readOnly,
                                allowReadOnlyChange, nullable, value, value
                                        .getClass());
                    } else {
                        cp = new ColumnProperty(s, readOnly,
                                allowReadOnlyChange, nullable, null, null);
                    }
                    itemProperties.add(cp);
                    columnNum++;
                }
                /* Cache item */
                itemIndexes.put(rowCount, id);
                cachedItems.put(id, new RowItem(this, id, itemProperties));
                rowCount++;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch page.", e);
        }
    }

    /************************************/
    /** UNSUPPORTED CONTAINER FEATURES **/
    /************************************/
    public boolean addContainerProperty(Object propertyId, Class<?> type,
            Object defaultValue) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    public boolean removeContainerProperty(Object propertyId)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    public Item addItem(Object itemId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    public Item addItemAfter(Object previousItemId, Object newItemId)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    public Item addItemAt(int index, Object newItemId)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    public Object addItemAt(int index) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    public Object addItemAfter(Object previousItemId)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /******************************************/
    /** ITEMSETCHANGENOTIFIER IMPLEMENTATION **/
    /******************************************/
    public void addListener(Container.ItemSetChangeListener listener) {
        if (itemSetChangeListeners == null) {
            itemSetChangeListeners = new LinkedList<Container.ItemSetChangeListener>();
        }
        itemSetChangeListeners.add(listener);
    }

    public void removeListener(Container.ItemSetChangeListener listener) {
        if (itemSetChangeListeners != null) {
            itemSetChangeListeners.remove(listener);
        }
    }

    protected void fireContentsChange() {
        if (itemSetChangeListeners != null) {
            final Object[] l = itemSetChangeListeners.toArray();
            final Container.ItemSetChangeEvent event = new SQLContainer.ItemSetChangeEvent(
                    this);
            for (int i = 0; i < l.length; i++) {
                ((Container.ItemSetChangeListener) l[i])
                        .containerItemSetChange(event);
            }
        }
    }

    /**
     * Simple ItemSetChangeEvent implementation.
     */
    public class ItemSetChangeEvent extends EventObject implements
            Container.ItemSetChangeEvent, Serializable {
        private static final long serialVersionUID = -8215550776139145987L;

        private ItemSetChangeEvent(SQLContainer source) {
            super(source);
        }

        public Container getContainer() {
            return (Container) getSource();
        }
    }

    /**
     * CacheMap extends LinkedHashMap, adding the possibility to adjust maximum
     * number of items.
     * 
     * In SQLContainer this is used for RowItem -cache. Cache size will be two
     * times the page length parameter of the container.
     */
    public class CacheMap<K, V> extends LinkedHashMap<K, V> {
        private static final long serialVersionUID = 679999766473555231L;
        private int cacheLimit = CACHE_RATIO * DEFAULT_PAGE_LENGTH;

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > cacheLimit;
        }

        public void setCacheLimit(int limit) {
            cacheLimit = limit > 0 ? limit : DEFAULT_PAGE_LENGTH;
        }

        public int getCacheLimit() {
            return cacheLimit;
        }
    };
}
