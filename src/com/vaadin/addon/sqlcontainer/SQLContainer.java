package com.vaadin.addon.sqlcontainer;

import java.util.Collection;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;

public class SQLContainer implements Container, Container.Filterable,
        Container.Indexed, Container.Ordered, Container.Sortable {
    private static final long serialVersionUID = -3863564310693712511L;

    public void sort(Object[] propertyId, boolean[] ascending) {
        // TODO Auto-generated method stub

    }

    public Collection<?> getSortableContainerPropertyIds() {
        // TODO Auto-generated method stub
        return null;
    }

    public Object nextItemId(Object itemId) {
        // TODO Auto-generated method stub
        return null;
    }

    public Object prevItemId(Object itemId) {
        // TODO Auto-generated method stub
        return null;
    }

    public Object firstItemId() {
        // TODO Auto-generated method stub
        return null;
    }

    public Object lastItemId() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isFirstId(Object itemId) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isLastId(Object itemId) {
        // TODO Auto-generated method stub
        return false;
    }

    public Object addItemAfter(Object previousItemId)
            throws UnsupportedOperationException {
        // TODO Auto-generated method stub
        return null;
    }

    public Item addItemAfter(Object previousItemId, Object newItemId)
            throws UnsupportedOperationException {
        // TODO Auto-generated method stub
        return null;
    }

    public int indexOfId(Object itemId) {
        // TODO Auto-generated method stub
        return 0;
    }

    public Object getIdByIndex(int index) {
        // TODO Auto-generated method stub
        return null;
    }

    public Object addItemAt(int index) throws UnsupportedOperationException {
        // TODO Auto-generated method stub
        return null;
    }

    public Item addItemAt(int index, Object newItemId)
            throws UnsupportedOperationException {
        // TODO Auto-generated method stub
        return null;
    }

    public void addContainerFilter(Object propertyId, String filterString,
            boolean ignoreCase, boolean onlyMatchPrefix) {
        // TODO Auto-generated method stub

    }

    public void removeAllContainerFilters() {
        // TODO Auto-generated method stub

    }

    public void removeContainerFilters(Object propertyId) {
        // TODO Auto-generated method stub

    }

    public Item getItem(Object itemId) {
        // TODO Auto-generated method stub
        return null;
    }

    public Collection<?> getContainerPropertyIds() {
        // TODO Auto-generated method stub
        return null;
    }

    public Collection<?> getItemIds() {
        // TODO Auto-generated method stub
        return null;
    }

    public Property getContainerProperty(Object itemId, Object propertyId) {
        // TODO Auto-generated method stub
        return null;
    }

    public Class<?> getType(Object propertyId) {
        // TODO Auto-generated method stub
        return null;
    }

    public int size() {
        // TODO Auto-generated method stub
        return 0;
    }

    public boolean containsId(Object itemId) {
        // TODO Auto-generated method stub
        return false;
    }

    public Item addItem(Object itemId) throws UnsupportedOperationException {
        // TODO Auto-generated method stub
        return null;
    }

    public Object addItem() throws UnsupportedOperationException {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean removeItem(Object itemId)
            throws UnsupportedOperationException {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean addContainerProperty(Object propertyId, Class<?> type,
            Object defaultValue) throws UnsupportedOperationException {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean removeContainerProperty(Object propertyId)
            throws UnsupportedOperationException {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean removeAllItems() throws UnsupportedOperationException {
        // TODO Auto-generated method stub
        return false;
    }

}
