package com.vaadin.addon.sqlcontainer;

import java.sql.SQLException;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.Test;

public class ReferenceTest {

    @Test
    public void getReferencedContainer_shouldReturnValueProvidedInConstructor()
            throws SQLException {
        SQLContainer container = EasyMock.createMock(SQLContainer.class);
        EasyMock.replay(container);
        Reference ref = new Reference(container, "foo", "bar");
        Assert.assertEquals(container, ref.getReferencedContainer());
    }

    @Test
    public void getReferencedColumn_shouldReturnValueProvidedInConstructor()
            throws SQLException {
        SQLContainer container = EasyMock.createMock(SQLContainer.class);
        EasyMock.replay(container);
        Reference ref = new Reference(container, "foo", "bar");
        Assert.assertEquals("bar", ref.getReferencedColumn());
    }

    @Test
    public void getReferencingColumn_shouldReturnValueProvidedInConstructor()
            throws SQLException {
        SQLContainer container = EasyMock.createMock(SQLContainer.class);
        EasyMock.replay(container);
        Reference ref = new Reference(container, "foo", "bar");
        Assert.assertEquals("foo", ref.getReferencingColumn());
    }

}
