package com.vaadin.addon.sqlcontainer;

import junit.framework.Assert;

import org.junit.Test;

public class UtilTest {

    @Test
    public void escapeSQL_noQuotes_returnsSameString() {
        Assert.assertEquals("asdf", Util.escapeSQL("asdf"));
    }

    @Test
    public void escapeSQL_singleQuotes_returnsEscapedString() {
        Assert.assertEquals("O''Brien", Util.escapeSQL("O'Brien"));
    }

    @Test
    public void escapeSQL_severalQuotes_returnsEscapedString() {
        Assert.assertEquals("asdf''ghjk''qwerty",
                Util.escapeSQL("asdf'ghjk'qwerty"));
    }

    @Test
    public void escapeSQL_doubleQuotes_returnsEscapedString() {
        Assert.assertEquals("asdf\\\"foo", Util.escapeSQL("asdf\"foo"));
    }

    @Test
    public void escapeSQL_multipleDoubleQuotes_returnsEscapedString() {
        Assert.assertEquals("asdf\\\"foo\\\"bar",
                Util.escapeSQL("asdf\"foo\"bar"));
    }

    @Test
    public void escapeSQL_backslashes_returnsEscapedString() {
        Assert.assertEquals("foo\\\\nbar\\\\r", Util.escapeSQL("foo\\nbar\\r"));
    }

    @Test
    public void escapeSQL_x00_removesX00() {
        Assert.assertEquals("foobar", Util.escapeSQL("foo\\x00bar"));
    }

    @Test
    public void escapeSQL_x1a_removesX1a() {
        Assert.assertEquals("foobar", Util.escapeSQL("foo\\x1abar"));
    }
}
