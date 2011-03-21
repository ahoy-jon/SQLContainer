package com.vaadin.addon.sqlcontainer.filters;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.data.Item;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;

public class LikeTest {

    @Test
    public void passesFilter_valueIsNotStringType_shouldFail() {
        Like like = new Like("test", "%foo%");

        Item item = new PropertysetItem();
        item.addItemProperty("test", new ObjectProperty<Integer>(5));

        Assert.assertFalse(like.passesFilter("id", item));
    }

    @Test
    public void passesFilter_containsLikeQueryOnStringContainingValue_shouldSucceed() {
        Like like = new Like("test", "%foo%");

        Item item = new PropertysetItem();
        item.addItemProperty("test", new ObjectProperty<String>("asdfooghij"));

        Assert.assertTrue(like.passesFilter("id", item));
    }

    @Test
    public void passesFilter_containsLikeQueryOnStringContainingValueCaseInsensitive_shouldSucceed() {
        Like like = new Like("test", "%foo%");
        like.setCaseSensitive(false);

        Item item = new PropertysetItem();
        item.addItemProperty("test", new ObjectProperty<String>("asdfOOghij"));

        Assert.assertTrue(like.passesFilter("id", item));
    }

    @Test
    public void passesFilter_containsLikeQueryOnStringNotContainingValue_shouldFail() {
        Like like = new Like("test", "%foo%");

        Item item = new PropertysetItem();
        item.addItemProperty("test", new ObjectProperty<String>("asdbarghij"));

        Assert.assertFalse(like.passesFilter("id", item));
    }

    @Test
    public void passesFilter_containsLikeQueryOnStringExactlyEqualToValue_shouldSucceed() {
        Like like = new Like("test", "%foo%");

        Item item = new PropertysetItem();
        item.addItemProperty("test", new ObjectProperty<String>("foo"));

        Assert.assertTrue(like.passesFilter("id", item));
    }

    @Test
    public void passesFilter_containsLikeQueryOnStringEqualToValueMinusOneCharAtTheEnd_shouldFail() {
        Like like = new Like("test", "%foo%");

        Item item = new PropertysetItem();
        item.addItemProperty("test", new ObjectProperty<String>("fo"));

        Assert.assertFalse(like.passesFilter("id", item));
    }

    @Test
    public void passesFilter_beginsWithLikeQueryOnStringBeginningWithValue_shouldSucceed() {
        Like like = new Like("test", "foo%");

        Item item = new PropertysetItem();
        item.addItemProperty("test", new ObjectProperty<String>("foobar"));

        Assert.assertTrue(like.passesFilter("id", item));
    }

    @Test
    public void passesFilter_beginsWithLikeQueryOnStringNotBeginningWithValue_shouldFail() {
        Like like = new Like("test", "foo%");

        Item item = new PropertysetItem();
        item.addItemProperty("test", new ObjectProperty<String>("barfoo"));

        Assert.assertFalse(like.passesFilter("id", item));
    }

    @Test
    public void passesFilter_endsWithLikeQueryOnStringEndingWithValue_shouldSucceed() {
        Like like = new Like("test", "%foo");

        Item item = new PropertysetItem();
        item.addItemProperty("test", new ObjectProperty<String>("barfoo"));

        Assert.assertTrue(like.passesFilter("id", item));
    }

    @Test
    public void passesFilter_endsWithLikeQueryOnStringNotEndingWithValue_shouldFail() {
        Like like = new Like("test", "%foo");

        Item item = new PropertysetItem();
        item.addItemProperty("test", new ObjectProperty<String>("foobar"));

        Assert.assertFalse(like.passesFilter("id", item));
    }

    @Test
    public void passesFilter_startsWithAndEndsWithOnMatchingValue_shouldSucceed() {
        Like like = new Like("test", "foo%bar");

        Item item = new PropertysetItem();
        item.addItemProperty("test", new ObjectProperty<String>("fooASDFbar"));

        Assert.assertTrue(like.passesFilter("id", item));
    }
}
