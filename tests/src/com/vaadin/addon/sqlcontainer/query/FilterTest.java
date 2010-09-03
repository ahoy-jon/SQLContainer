package com.vaadin.addon.sqlcontainer.query;

import junit.framework.Assert;

import org.junit.Test;

import com.vaadin.addon.sqlcontainer.query.Filter.ComparisonType;

public class FilterTest {

    // EQUALS, GREATER, LESS, GREATER_OR_EQUAL, LESS_OR_EQUAL, STARTS_WITH,
    // ENDS_WITH, CONTAINS, BETWEEN;

    // escape bad characters and wildcards

    @Test
    public void toWhereString_equals() {
        Filter f = new Filter("NAME", ComparisonType.EQUALS, "Fido");
        Assert.assertEquals("NAME = 'Fido'", f.toWhereString());
    }

    @Test
    public void toWhereString_greater() {
        Filter f = new Filter("AGE", ComparisonType.GREATER, 18);
        Assert.assertEquals("AGE > 18", f.toWhereString());
    }

    @Test
    public void toWhereString_less() {
        Filter f = new Filter("AGE", ComparisonType.LESS, 65);
        Assert.assertEquals("AGE < 65", f.toWhereString());
    }

    @Test
    public void toWhereString_greaterOrEqual() {
        Filter f = new Filter("AGE", ComparisonType.GREATER_OR_EQUAL, 18);
        Assert.assertEquals("AGE >= 18", f.toWhereString());
    }

    @Test
    public void toWhereString_lessOrEqual() {
        Filter f = new Filter("AGE", ComparisonType.LESS_OR_EQUAL, 65);
        Assert.assertEquals("AGE <= 65", f.toWhereString());
    }

    @Test
    public void toWhereString_startsWith() {
        Filter f = new Filter("NAME", ComparisonType.STARTS_WITH, "Vi");
        Assert.assertEquals("NAME LIKE 'Vi%'", f.toWhereString());
    }

    @Test
    public void toWhereString_startsWithNumber() {
        Filter f = new Filter("AGE", ComparisonType.STARTS_WITH, 1);
        Assert.assertEquals("AGE LIKE '1%'", f.toWhereString());
    }

    @Test
    public void toWhereString_endsWith() {
        Filter f = new Filter("NAME", ComparisonType.ENDS_WITH, "lle");
        Assert.assertEquals("NAME LIKE '%lle'", f.toWhereString());
    }

    @Test
    public void toWhereString_contains() {
        Filter f = new Filter("NAME", ComparisonType.CONTAINS, "ill");
        Assert.assertEquals("NAME LIKE '%ill%'", f.toWhereString());
    }

    @Test
    public void toWhereString_between() {
        Filter f = new Filter("AGE", ComparisonType.BETWEEN, 18, 65);
        Assert.assertEquals("AGE BETWEEN 18 AND 65", f.toWhereString());
    }

    @Test
    public void toWhereString_caseInsensitive_equals() {
        Filter f = new Filter("NAME", ComparisonType.EQUALS, "Fido");
        f.setCaseSensitive(false);
        Assert.assertEquals("UPPER(NAME) = 'FIDO'", f.toWhereString());
    }

    @Test
    public void toWhereString_caseInsensitive_startsWith() {
        Filter f = new Filter("NAME", ComparisonType.STARTS_WITH, "Vi");
        f.setCaseSensitive(false);
        Assert.assertEquals("UPPER(NAME) LIKE 'VI%'", f.toWhereString());
    }

    @Test
    public void toWhereString_caseInsensitive_endsWith() {
        Filter f = new Filter("NAME", ComparisonType.ENDS_WITH, "lle");
        f.setCaseSensitive(false);
        Assert.assertEquals("UPPER(NAME) LIKE '%LLE'", f.toWhereString());
    }

    @Test
    public void toWhereString_caseInsensitive_contains() {
        Filter f = new Filter("NAME", ComparisonType.CONTAINS, "ill");
        f.setCaseSensitive(false);
        Assert.assertEquals("UPPER(NAME) LIKE '%ILL%'", f.toWhereString());
    }
}
