package com.vaadin.addon.sqlcontainer.query;

import java.util.ArrayList;

import junit.framework.Assert;

import org.junit.Test;

import com.vaadin.addon.sqlcontainer.filters.Between;
import com.vaadin.addon.sqlcontainer.filters.Like;
import com.vaadin.addon.sqlcontainer.query.generator.FilterToWhereTranslator;
import com.vaadin.addon.sqlcontainer.query.generator.StatementHelper;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.And;
import com.vaadin.data.util.filter.Compare.Equal;
import com.vaadin.data.util.filter.Compare.Greater;
import com.vaadin.data.util.filter.Compare.GreaterOrEqual;
import com.vaadin.data.util.filter.Compare.Less;
import com.vaadin.data.util.filter.Compare.LessOrEqual;
import com.vaadin.data.util.filter.Or;

public class FilterToWhereTranslatorTest {

    // escape bad characters and wildcards

    @Test
    public void getWhereStringForFilter_equals() {
        Equal f = new Equal("NAME", "Fido");
        Assert.assertEquals("\"NAME\" = ?", FilterToWhereTranslator
                .getWhereStringForFilter(f, new StatementHelper()));
    }

    @Test
    public void getWhereStringForFilter_greater() {
        Greater f = new Greater("AGE", 18);
        Assert.assertEquals("\"AGE\" > ?", FilterToWhereTranslator
                .getWhereStringForFilter(f, new StatementHelper()));
    }

    @Test
    public void getWhereStringForFilter_less() {
        Less f = new Less("AGE", 65);
        Assert.assertEquals("\"AGE\" < ?", FilterToWhereTranslator
                .getWhereStringForFilter(f, new StatementHelper()));
    }

    @Test
    public void getWhereStringForFilter_greaterOrEqual() {
        GreaterOrEqual f = new GreaterOrEqual("AGE", 18);
        Assert.assertEquals("\"AGE\" >= ?", FilterToWhereTranslator
                .getWhereStringForFilter(f, new StatementHelper()));
    }

    @Test
    public void getWhereStringForFilter_lessOrEqual() {
        LessOrEqual f = new LessOrEqual("AGE", 65);
        Assert.assertEquals("\"AGE\" <= ?", FilterToWhereTranslator
                .getWhereStringForFilter(f, new StatementHelper()));
    }

    @Test
    public void getWhereStringForFilter_startsWith() {
        Like f = new Like("NAME", "Vi%");
        Assert.assertEquals("\"NAME\" LIKE ?", FilterToWhereTranslator
                .getWhereStringForFilter(f, new StatementHelper()));
    }

    @Test
    public void getWhereStringForFilter_startsWithNumber() {
        Like f = new Like("AGE", "1%");
        Assert.assertEquals("\"AGE\" LIKE ?", FilterToWhereTranslator
                .getWhereStringForFilter(f, new StatementHelper()));
    }

    @Test
    public void getWhereStringForFilter_endsWith() {
        Like f = new Like("NAME", "%lle");
        Assert.assertEquals("\"NAME\" LIKE ?", FilterToWhereTranslator
                .getWhereStringForFilter(f, new StatementHelper()));
    }

    @Test
    public void getWhereStringForFilter_contains() {
        Like f = new Like("NAME", "%ill%");
        Assert.assertEquals("\"NAME\" LIKE ?", FilterToWhereTranslator
                .getWhereStringForFilter(f, new StatementHelper()));
    }

    @Test
    public void getWhereStringForFilter_between() {
        Between f = new Between("AGE", 18, 65);
        Assert.assertEquals("\"AGE\" BETWEEN ? AND ?", FilterToWhereTranslator
                .getWhereStringForFilter(f, new StatementHelper()));
    }

    @Test
    public void getWhereStringForFilter_caseInsensitive_equals() {
        Like f = new Like("NAME", "Fido");
        f.setCaseSensitive(false);
        Assert.assertEquals("UPPER(\"NAME\") LIKE ?", FilterToWhereTranslator
                .getWhereStringForFilter(f, new StatementHelper()));
    }

    @Test
    public void getWhereStringForFilter_caseInsensitive_startsWith() {
        Like f = new Like("NAME", "Vi%");
        f.setCaseSensitive(false);
        Assert.assertEquals("UPPER(\"NAME\") LIKE ?", FilterToWhereTranslator
                .getWhereStringForFilter(f, new StatementHelper()));
    }

    @Test
    public void getWhereStringForFilter_caseInsensitive_endsWith() {
        Like f = new Like("NAME", "%lle");
        f.setCaseSensitive(false);
        Assert.assertEquals("UPPER(\"NAME\") LIKE ?", FilterToWhereTranslator
                .getWhereStringForFilter(f, new StatementHelper()));
    }

    @Test
    public void getWhereStringForFilter_caseInsensitive_contains() {
        Like f = new Like("NAME", "%ill%");
        f.setCaseSensitive(false);
        Assert.assertEquals("UPPER(\"NAME\") LIKE ?", FilterToWhereTranslator
                .getWhereStringForFilter(f, new StatementHelper()));
    }

    @Test
    public void getWhereStringForFilters_listOfFilters() {
        ArrayList<Filter> filters = new ArrayList<Filter>();
        filters.add(new Like("NAME", "%lle"));
        filters.add(new Greater("AGE", "18"));
        Assert.assertEquals(" WHERE \"NAME\" LIKE ? AND \"AGE\" > ?",
                FilterToWhereTranslator.getWhereStringForFilters(filters,
                        new StatementHelper()));
    }

    @Test
    public void getWhereStringForFilters_oneAndFilter() {
        ArrayList<Filter> filters = new ArrayList<Filter>();
        filters.add(new And(new Like("NAME", "%lle"), new Greater("AGE", "18")));
        Assert.assertEquals(" WHERE (\"NAME\" LIKE ? AND \"AGE\" > ?)",
                FilterToWhereTranslator.getWhereStringForFilters(filters,
                        new StatementHelper()));
    }

    @Test
    public void getWhereStringForFilters_oneOrFilter() {
        ArrayList<Filter> filters = new ArrayList<Filter>();
        filters.add(new Or(new Like("NAME", "%lle"), new Greater("AGE", "18")));
        Assert.assertEquals(" WHERE (\"NAME\" LIKE ? OR \"AGE\" > ?)",
                FilterToWhereTranslator.getWhereStringForFilters(filters,
                        new StatementHelper()));
    }

    @Test
    public void getWhereStringForFilters_complexCompoundFilters() {
        ArrayList<Filter> filters = new ArrayList<Filter>();
        filters.add(new Or(new And(new Like("NAME", "%lle"), new Or(new Less(
                "AGE", "18"), new Greater("AGE", "65"))), new Equal("NAME",
                "Pelle")));
        Assert.assertEquals(
                " WHERE ((\"NAME\" LIKE ? AND (\"AGE\" < ? OR \"AGE\" > ?)) OR \"NAME\" = ?)",
                FilterToWhereTranslator.getWhereStringForFilters(filters,
                        new StatementHelper()));
    }

    @Test
    public void getWhereStringForFilters_complexCompoundFiltersAndSingleFilter() {
        ArrayList<Filter> filters = new ArrayList<Filter>();
        filters.add(new Or(new And(new Like("NAME", "%lle"), new Or(new Less(
                "AGE", "18"), new Greater("AGE", "65"))), new Equal("NAME",
                "Pelle")));
        filters.add(new Equal("LASTNAME", "Virtanen"));
        Assert.assertEquals(
                " WHERE ((\"NAME\" LIKE ? AND (\"AGE\" < ? OR \"AGE\" > ?)) OR \"NAME\" = ?) AND \"LASTNAME\" = ?",
                FilterToWhereTranslator.getWhereStringForFilters(filters,
                        new StatementHelper()));
    }

    @Test
    public void getWhereStringForFilters_emptyList_shouldReturnEmptyString() {
        ArrayList<Filter> filters = new ArrayList<Filter>();
        Assert.assertEquals("", FilterToWhereTranslator
                .getWhereStringForFilters(filters, new StatementHelper()));
    }

}
