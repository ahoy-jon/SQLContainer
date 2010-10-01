package com.vaadin.addon.sqlcontainer.query;

import junit.framework.Assert;

import org.junit.Test;

import com.vaadin.addon.sqlcontainer.query.Filter.ComparisonType;

public class FilterTest {

    // escape bad characters and wildcards

    @Test
    public void toWhereString_equals() {
        Filter f = new Filter("NAME", ComparisonType.EQUALS, "Fido");
        Assert.assertEquals("\"NAME\" = 'Fido'", f.toWhereString());
    }

    @Test
    public void toWhereString_greater() {
        Filter f = new Filter("AGE", ComparisonType.GREATER, 18);
        Assert.assertEquals("\"AGE\" > 18", f.toWhereString());
    }

    @Test
    public void toWhereString_less() {
        Filter f = new Filter("AGE", ComparisonType.LESS, 65);
        Assert.assertEquals("\"AGE\" < 65", f.toWhereString());
    }

    @Test
    public void toWhereString_greaterOrEqual() {
        Filter f = new Filter("AGE", ComparisonType.GREATER_OR_EQUAL, 18);
        Assert.assertEquals("\"AGE\" >= 18", f.toWhereString());
    }

    @Test
    public void toWhereString_lessOrEqual() {
        Filter f = new Filter("AGE", ComparisonType.LESS_OR_EQUAL, 65);
        Assert.assertEquals("\"AGE\" <= 65", f.toWhereString());
    }

    @Test
    public void toWhereString_startsWith() {
        Filter f = new Filter("NAME", ComparisonType.STARTS_WITH, "Vi");
        Assert.assertEquals("\"NAME\" LIKE 'Vi%'", f.toWhereString());
    }

    @Test
    public void toWhereString_startsWithNumber() {
        Filter f = new Filter("AGE", ComparisonType.STARTS_WITH, 1);
        Assert.assertEquals("\"AGE\" LIKE '1%'", f.toWhereString());
    }

    @Test
    public void toWhereString_endsWith() {
        Filter f = new Filter("NAME", ComparisonType.ENDS_WITH, "lle");
        Assert.assertEquals("\"NAME\" LIKE '%lle'", f.toWhereString());
    }

    @Test
    public void toWhereString_contains() {
        Filter f = new Filter("NAME", ComparisonType.CONTAINS, "ill");
        Assert.assertEquals("\"NAME\" LIKE '%ill%'", f.toWhereString());
    }

    @Test
    public void toWhereString_between() {
        Filter f = new Filter("AGE", ComparisonType.BETWEEN, 18, 65);
        Assert.assertEquals("\"AGE\" BETWEEN 18 AND 65", f.toWhereString());
    }

    @Test
    public void toWhereString_caseInsensitive_equals() {
        Filter f = new Filter("NAME", ComparisonType.EQUALS, "Fido");
        f.setCaseSensitive(false);
        Assert.assertEquals("UPPER(\"NAME\") = 'FIDO'", f.toWhereString());
    }

    @Test
    public void toWhereString_caseInsensitive_startsWith() {
        Filter f = new Filter("NAME", ComparisonType.STARTS_WITH, "Vi");
        f.setCaseSensitive(false);
        Assert.assertEquals("UPPER(\"NAME\") LIKE 'VI%'", f.toWhereString());
    }

    @Test
    public void toWhereString_caseInsensitive_endsWith() {
        Filter f = new Filter("NAME", ComparisonType.ENDS_WITH, "lle");
        f.setCaseSensitive(false);
        Assert.assertEquals("UPPER(\"NAME\") LIKE '%LLE'", f.toWhereString());
    }

    @Test
    public void toWhereString_caseInsensitive_contains() {
        Filter f = new Filter("NAME", ComparisonType.CONTAINS, "ill");
        f.setCaseSensitive(false);
        Assert.assertEquals("UPPER(\"NAME\") LIKE '%ILL%'", f.toWhereString());
    }

    @Test
    public void passes_equals_equalStrings_returnsTrue() {
        Filter f = new Filter("NAME", ComparisonType.EQUALS, "Ville");
        Assert.assertTrue(f.passes("Ville"));
    }

    @Test
    public void passes_equals_equalStringsCaseInsensitive_returnsTrue() {
        Filter f = new Filter("NAME", ComparisonType.EQUALS, "Ville");
        f.setCaseSensitive(false);
        Assert.assertTrue(f.passes("ViLLE"));
    }

    @Test
    public void passes_equals_differentStrings_returnsFalse() {
        Filter f = new Filter("NAME", ComparisonType.EQUALS, "Ville");
        Assert.assertFalse(f.passes("Börje"));
    }

    @Test
    public void passes_equals_equalNumbers_returnsTrue() {
        Filter f = new Filter("AGE", ComparisonType.EQUALS, 18);
        Assert.assertTrue(f.passes(18));
    }

    @Test
    public void passes_equals_differentNumbers_returnsFalse() {
        Filter f = new Filter("AGE", ComparisonType.EQUALS, 18);
        Assert.assertFalse(f.passes(65));
    }

    @Test
    public void passes_greater_greaterStrings_returnsTrue() {
        Filter f = new Filter("NAME", ComparisonType.GREATER, "Ville");
        Assert.assertTrue(f.passes("Wilbert"));
    }

    @Test
    public void passes_greater_lessStrings_returnsFalse() {
        Filter f = new Filter("NAME", ComparisonType.GREATER, "Ville");
        Assert.assertFalse(f.passes("Albert"));
    }

    @Test
    public void passes_greater_sameStrings_returnsFalse() {
        Filter f = new Filter("NAME", ComparisonType.GREATER, "Ville");
        Assert.assertFalse(f.passes("Ville"));
    }

    @Test
    public void passes_greater_greaterNumbers_returnsTrue() {
        Filter f = new Filter("AGE", ComparisonType.GREATER, 18);
        Assert.assertTrue(f.passes(19));
    }

    @Test
    public void passes_greater_lessNumbers_returnsFalse() {
        Filter f = new Filter("AGE", ComparisonType.GREATER, 18);
        Assert.assertFalse(f.passes(17));
    }

    @Test
    public void passes_less_lessStrings_returnsTrue() {
        Filter f = new Filter("NAME", ComparisonType.LESS, "Ville");
        Assert.assertTrue(f.passes("Albert"));
    }

    @Test
    public void passes_less_greaterStrings_returnsFalse() {
        Filter f = new Filter("NAME", ComparisonType.LESS, "Ville");
        Assert.assertFalse(f.passes("Wilbert"));
    }

    @Test
    public void passes_less_sameStrings_returnsFalse() {
        Filter f = new Filter("NAME", ComparisonType.LESS, "Ville");
        Assert.assertFalse(f.passes("Ville"));
    }

    @Test
    public void passes_less_lessNumbers_returnsTrue() {
        Filter f = new Filter("AGE", ComparisonType.LESS, 18);
        Assert.assertTrue(f.passes(17));
    }

    @Test
    public void passes_less_greaterNumbers_returnsFalse() {
        Filter f = new Filter("AGE", ComparisonType.LESS, 18);
        Assert.assertFalse(f.passes(19));
    }

    @Test
    public void passes_greaterOrEqual_greaterStrings_returnsTrue() {
        Filter f = new Filter("NAME", ComparisonType.GREATER_OR_EQUAL, "Ville");
        Assert.assertTrue(f.passes("Wilbert"));
    }

    @Test
    public void passes_greaterOrEqual_lessStrings_returnsFalse() {
        Filter f = new Filter("NAME", ComparisonType.GREATER_OR_EQUAL, "Ville");
        Assert.assertFalse(f.passes("Albert"));
    }

    @Test
    public void passes_greaterOrEqual_sameStrings_returnsTrue() {
        Filter f = new Filter("NAME", ComparisonType.GREATER_OR_EQUAL, "Ville");
        Assert.assertTrue(f.passes("Ville"));
    }

    @Test
    public void passes_greaterOrEqual_greaterNumbers_returnsTrue() {
        Filter f = new Filter("AGE", ComparisonType.GREATER_OR_EQUAL, 18);
        Assert.assertTrue(f.passes(19));
    }

    @Test
    public void passes_greaterOrEqual_lessNumbers_returnsFalse() {
        Filter f = new Filter("AGE", ComparisonType.GREATER_OR_EQUAL, 18);
        Assert.assertFalse(f.passes(17));
    }

    @Test
    public void passes_greaterOrEqual_equalNumbers_returnsTrue() {
        Filter f = new Filter("AGE", ComparisonType.GREATER_OR_EQUAL, 18);
        Assert.assertTrue(f.passes(18));
    }

    @Test
    public void passes_lessOrEqual_greaterStrings_returnsFalse() {
        Filter f = new Filter("NAME", ComparisonType.LESS_OR_EQUAL, "Ville");
        Assert.assertFalse(f.passes("Wilbert"));
    }

    @Test
    public void passes_lessOrEqual_lessStrings_returnsTrue() {
        Filter f = new Filter("NAME", ComparisonType.LESS_OR_EQUAL, "Ville");
        Assert.assertTrue(f.passes("Albert"));
    }

    @Test
    public void passes_lessOrEqual_sameStrings_returnsTrue() {
        Filter f = new Filter("NAME", ComparisonType.LESS_OR_EQUAL, "Ville");
        Assert.assertTrue(f.passes("Ville"));
    }

    @Test
    public void passes_lessOrEqual_greaterNumbers_returnsFalse() {
        Filter f = new Filter("AGE", ComparisonType.LESS_OR_EQUAL, 18);
        Assert.assertFalse(f.passes(19));
    }

    @Test
    public void passes_lessOrEqual_lessNumbers_returnsTrue() {
        Filter f = new Filter("AGE", ComparisonType.LESS_OR_EQUAL, 18);
        Assert.assertTrue(f.passes(17));
    }

    @Test
    public void passes_lessOrEqual_equalNumbers_returnsTrue() {
        Filter f = new Filter("AGE", ComparisonType.LESS_OR_EQUAL, 18);
        Assert.assertTrue(f.passes(18));
    }

    @Test
    public void passes_startsWith_stringStartsWithValue_returnsTrue() {
        Filter f = new Filter("NAME", ComparisonType.STARTS_WITH, "Vi");
        Assert.assertTrue(f.passes("Ville"));
    }

    @Test
    public void passes_startsWith_stringStartsWithValueCaseSensitive_returnsFalse() {
        Filter f = new Filter("NAME", ComparisonType.STARTS_WITH, "vi");
        Assert.assertFalse(f.passes("Ville"));
    }

    @Test
    public void passes_startsWith_stringStartsWithValueCaseInsensitive_returnsTrue() {
        Filter f = new Filter("NAME", ComparisonType.STARTS_WITH, "vi");
        f.setCaseSensitive(false);
        Assert.assertTrue(f.passes("Ville"));
    }

    @Test
    public void passes_startsWith_stringDoesntStartWithValueCaseInsensitive_returnsFalse() {
        Filter f = new Filter("NAME", ComparisonType.STARTS_WITH, "bö");
        Assert.assertFalse(f.passes("Ville"));
    }

    @Test
    public void passes_endsWith_stringEndsWithValue_returnsTrue() {
        Filter f = new Filter("NAME", ComparisonType.ENDS_WITH, "lle");
        Assert.assertTrue(f.passes("Ville"));
    }

    @Test
    public void passes_endsWith_stringEndsWithValueCaseSensitive_returnsFalse() {
        Filter f = new Filter("NAME", ComparisonType.ENDS_WITH, "LLE");
        Assert.assertFalse(f.passes("Ville"));
    }

    @Test
    public void passes_endsWith_stringEndsWithValueCaseInsensitive_returnsTrue() {
        Filter f = new Filter("NAME", ComparisonType.ENDS_WITH, "LLE");
        f.setCaseSensitive(false);
        Assert.assertTrue(f.passes("Ville"));
    }

    // CONTAINS, BETWEEN;
    @Test
    public void passes_contains_stringContainsValue_returnsTrue() {
        Filter f = new Filter("NAME", ComparisonType.CONTAINS, "ill");
        Assert.assertTrue(f.passes("Ville"));
    }

    @Test
    public void passes_contains_stringDoesNotContainValue_returnsFalse() {
        Filter f = new Filter("NAME", ComparisonType.CONTAINS, "örj");
        Assert.assertFalse(f.passes("Ville"));
    }

    @Test
    public void passes_contains_stringContainsValueCaseSensitive_returnsFalse() {
        Filter f = new Filter("NAME", ComparisonType.CONTAINS, "IlL");
        Assert.assertFalse(f.passes("Ville"));
    }

    @Test
    public void passes_contains_stringContainsValueCaseInsensitive_returnsTrue() {
        Filter f = new Filter("NAME", ComparisonType.CONTAINS, "IlL");
        f.setCaseSensitive(false);
        Assert.assertTrue(f.passes("Ville"));
    }
}
