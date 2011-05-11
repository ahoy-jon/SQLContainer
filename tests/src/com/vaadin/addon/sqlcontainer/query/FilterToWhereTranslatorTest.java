package com.vaadin.addon.sqlcontainer.query;

import java.util.ArrayList;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.Test;

import com.vaadin.addon.sqlcontainer.filters.Between;
import com.vaadin.addon.sqlcontainer.filters.Like;
import com.vaadin.addon.sqlcontainer.query.generator.StatementHelper;
import com.vaadin.addon.sqlcontainer.query.generator.filter.FilterToWhereTranslator;
import com.vaadin.addon.sqlcontainer.query.generator.filter.StringDecorator;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.And;
import com.vaadin.data.util.filter.Compare.Equal;
import com.vaadin.data.util.filter.Compare.Greater;
import com.vaadin.data.util.filter.Compare.GreaterOrEqual;
import com.vaadin.data.util.filter.Compare.Less;
import com.vaadin.data.util.filter.Compare.LessOrEqual;
import com.vaadin.data.util.filter.IsNull;
import com.vaadin.data.util.filter.Not;
import com.vaadin.data.util.filter.Or;
import com.vaadin.data.util.filter.SimpleStringFilter;

public class FilterToWhereTranslatorTest {

    private StatementHelper mockedStatementHelper(Object... values) {
        StatementHelper sh = EasyMock.createMock(StatementHelper.class);
        for (Object val : values) {
            sh.addParameterValue(val);
            EasyMock.expectLastCall();
        }
        EasyMock.replay(sh);
        return sh;
    }

    // escape bad characters and wildcards

    @Test
    public void getWhereStringForFilter_equals() {
        StatementHelper sh = mockedStatementHelper("Fido");
        Equal f = new Equal("NAME", "Fido");
        Assert.assertEquals("\"NAME\" = ?",
                FilterToWhereTranslator.getWhereStringForFilter(f, sh));
        EasyMock.verify(sh);
    }

    @Test
    public void getWhereStringForFilter_greater() {
        StatementHelper sh = mockedStatementHelper(18);
        Greater f = new Greater("AGE", 18);
        Assert.assertEquals("\"AGE\" > ?",
                FilterToWhereTranslator.getWhereStringForFilter(f, sh));
        EasyMock.verify(sh);
    }

    @Test
    public void getWhereStringForFilter_less() {
        StatementHelper sh = mockedStatementHelper(65);
        Less f = new Less("AGE", 65);
        Assert.assertEquals("\"AGE\" < ?",
                FilterToWhereTranslator.getWhereStringForFilter(f, sh));
        EasyMock.verify(sh);
    }

    @Test
    public void getWhereStringForFilter_greaterOrEqual() {
        StatementHelper sh = mockedStatementHelper(18);
        GreaterOrEqual f = new GreaterOrEqual("AGE", 18);
        Assert.assertEquals("\"AGE\" >= ?",
                FilterToWhereTranslator.getWhereStringForFilter(f, sh));
        EasyMock.verify(sh);
    }

    @Test
    public void getWhereStringForFilter_lessOrEqual() {
        StatementHelper sh = mockedStatementHelper(65);
        LessOrEqual f = new LessOrEqual("AGE", 65);
        Assert.assertEquals("\"AGE\" <= ?",
                FilterToWhereTranslator.getWhereStringForFilter(f, sh));
        EasyMock.verify(sh);
    }

    @Test
    public void getWhereStringForFilter_simpleStringFilter() {
        StatementHelper sh = mockedStatementHelper("Vi%");
        SimpleStringFilter f = new SimpleStringFilter("NAME", "Vi", false, true);
        Assert.assertEquals("\"NAME\" LIKE ?",
                FilterToWhereTranslator.getWhereStringForFilter(f, sh));
        EasyMock.verify(sh);
    }

    @Test
    public void getWhereStringForFilter_simpleStringFilterMatchAnywhere() {
        StatementHelper sh = mockedStatementHelper("%Vi%");
        SimpleStringFilter f = new SimpleStringFilter("NAME", "Vi", false,
                false);
        Assert.assertEquals("\"NAME\" LIKE ?",
                FilterToWhereTranslator.getWhereStringForFilter(f, sh));
        EasyMock.verify(sh);
    }

    @Test
    public void getWhereStringForFilter_simpleStringFilterMatchAnywhereIgnoreCase() {
        StatementHelper sh = mockedStatementHelper("%VI%");
        SimpleStringFilter f = new SimpleStringFilter("NAME", "Vi", true, false);
        Assert.assertEquals("UPPER(\"NAME\") LIKE ?",
                FilterToWhereTranslator.getWhereStringForFilter(f, sh));
        EasyMock.verify(sh);
    }

    @Test
    public void getWhereStringForFilter_startsWith() {
        StatementHelper sh = mockedStatementHelper("Vi%");
        Like f = new Like("NAME", "Vi%");
        Assert.assertEquals("\"NAME\" LIKE ?",
                FilterToWhereTranslator.getWhereStringForFilter(f, sh));
        EasyMock.verify(sh);
    }

    @Test
    public void getWhereStringForFilter_startsWithNumber() {
        StatementHelper sh = mockedStatementHelper("1%");
        Like f = new Like("AGE", "1%");
        Assert.assertEquals("\"AGE\" LIKE ?",
                FilterToWhereTranslator.getWhereStringForFilter(f, sh));
        EasyMock.verify(sh);
    }

    @Test
    public void getWhereStringForFilter_endsWith() {
        StatementHelper sh = mockedStatementHelper("%lle");
        Like f = new Like("NAME", "%lle");
        Assert.assertEquals("\"NAME\" LIKE ?",
                FilterToWhereTranslator.getWhereStringForFilter(f, sh));
        EasyMock.verify(sh);
    }

    @Test
    public void getWhereStringForFilter_contains() {
        StatementHelper sh = mockedStatementHelper("%ill%");
        Like f = new Like("NAME", "%ill%");
        Assert.assertEquals("\"NAME\" LIKE ?",
                FilterToWhereTranslator.getWhereStringForFilter(f, sh));
        EasyMock.verify(sh);
    }

    @Test
    public void getWhereStringForFilter_between() {
        StatementHelper sh = mockedStatementHelper(18, 65);
        Between f = new Between("AGE", 18, 65);
        Assert.assertEquals("\"AGE\" BETWEEN ? AND ?",
                FilterToWhereTranslator.getWhereStringForFilter(f, sh));
        EasyMock.verify(sh);
    }

    @Test
    public void getWhereStringForFilter_caseInsensitive_equals() {
        StatementHelper sh = mockedStatementHelper("FIDO");
        Like f = new Like("NAME", "Fido");
        f.setCaseSensitive(false);
        Assert.assertEquals("UPPER(\"NAME\") LIKE ?",
                FilterToWhereTranslator.getWhereStringForFilter(f, sh));
        EasyMock.verify(sh);
    }

    @Test
    public void getWhereStringForFilter_caseInsensitive_startsWith() {
        StatementHelper sh = mockedStatementHelper("VI%");
        Like f = new Like("NAME", "Vi%");
        f.setCaseSensitive(false);
        Assert.assertEquals("UPPER(\"NAME\") LIKE ?",
                FilterToWhereTranslator.getWhereStringForFilter(f, sh));
        EasyMock.verify(sh);
    }

    @Test
    public void getWhereStringForFilter_caseInsensitive_endsWith() {
        StatementHelper sh = mockedStatementHelper("%LLE");
        Like f = new Like("NAME", "%lle");
        f.setCaseSensitive(false);
        Assert.assertEquals("UPPER(\"NAME\") LIKE ?",
                FilterToWhereTranslator.getWhereStringForFilter(f, sh));
        EasyMock.verify(sh);
    }

    @Test
    public void getWhereStringForFilter_caseInsensitive_contains() {
        StatementHelper sh = mockedStatementHelper("%ILL%");
        Like f = new Like("NAME", "%ill%");
        f.setCaseSensitive(false);
        Assert.assertEquals("UPPER(\"NAME\") LIKE ?",
                FilterToWhereTranslator.getWhereStringForFilter(f, sh));
        EasyMock.verify(sh);
    }

    @Test
    public void getWhereStringForFilters_listOfFilters() {
        StatementHelper sh = mockedStatementHelper("%lle", 18);
        ArrayList<Filter> filters = new ArrayList<Filter>();
        filters.add(new Like("NAME", "%lle"));
        filters.add(new Greater("AGE", 18));
        Assert.assertEquals(" WHERE \"NAME\" LIKE ? AND \"AGE\" > ?",
                FilterToWhereTranslator.getWhereStringForFilters(filters, sh));
        EasyMock.verify(sh);
    }

    @Test
    public void getWhereStringForFilters_oneAndFilter() {
        StatementHelper sh = mockedStatementHelper("%lle", 18);
        ArrayList<Filter> filters = new ArrayList<Filter>();
        filters.add(new And(new Like("NAME", "%lle"), new Greater("AGE", 18)));
        Assert.assertEquals(" WHERE (\"NAME\" LIKE ? AND \"AGE\" > ?)",
                FilterToWhereTranslator.getWhereStringForFilters(filters, sh));
        EasyMock.verify(sh);
    }

    @Test
    public void getWhereStringForFilters_oneOrFilter() {
        StatementHelper sh = mockedStatementHelper("%lle", 18);
        ArrayList<Filter> filters = new ArrayList<Filter>();
        filters.add(new Or(new Like("NAME", "%lle"), new Greater("AGE", 18)));
        Assert.assertEquals(" WHERE (\"NAME\" LIKE ? OR \"AGE\" > ?)",
                FilterToWhereTranslator.getWhereStringForFilters(filters, sh));
        EasyMock.verify(sh);
    }

    @Test
    public void getWhereStringForFilters_complexCompoundFilters() {
        StatementHelper sh = mockedStatementHelper("%lle", 18, 65, "Pelle");
        ArrayList<Filter> filters = new ArrayList<Filter>();
        filters.add(new Or(new And(new Like("NAME", "%lle"), new Or(new Less(
                "AGE", 18), new Greater("AGE", 65))),
                new Equal("NAME", "Pelle")));
        Assert.assertEquals(
                " WHERE ((\"NAME\" LIKE ? AND (\"AGE\" < ? OR \"AGE\" > ?)) OR \"NAME\" = ?)",
                FilterToWhereTranslator.getWhereStringForFilters(filters, sh));
        EasyMock.verify(sh);
    }

    @Test
    public void getWhereStringForFilters_complexCompoundFiltersAndSingleFilter() {
        StatementHelper sh = mockedStatementHelper("%lle", 18, 65, "Pelle",
                "Virtanen");
        ArrayList<Filter> filters = new ArrayList<Filter>();
        filters.add(new Or(new And(new Like("NAME", "%lle"), new Or(new Less(
                "AGE", 18), new Greater("AGE", 65))),
                new Equal("NAME", "Pelle")));
        filters.add(new Equal("LASTNAME", "Virtanen"));
        Assert.assertEquals(
                " WHERE ((\"NAME\" LIKE ? AND (\"AGE\" < ? OR \"AGE\" > ?)) OR \"NAME\" = ?) AND \"LASTNAME\" = ?",
                FilterToWhereTranslator.getWhereStringForFilters(filters, sh));
        EasyMock.verify(sh);
    }

    @Test
    public void getWhereStringForFilters_emptyList_shouldReturnEmptyString() {
        ArrayList<Filter> filters = new ArrayList<Filter>();
        Assert.assertEquals("", FilterToWhereTranslator
                .getWhereStringForFilters(filters, new StatementHelper()));
    }

    @Test
    public void getWhereStringForFilters_NotFilter() {
        StatementHelper sh = mockedStatementHelper(18);
        ArrayList<Filter> filters = new ArrayList<Filter>();
        filters.add(new Not(new Equal("AGE", 18)));
        Assert.assertEquals(" WHERE NOT \"AGE\" = ?",
                FilterToWhereTranslator.getWhereStringForFilters(filters, sh));
        EasyMock.verify(sh);
    }

    @Test
    public void getWhereStringForFilters_complexNegatedFilter() {
        StatementHelper sh = mockedStatementHelper(65, 18);
        ArrayList<Filter> filters = new ArrayList<Filter>();
        filters.add(new Not(new Or(new Equal("AGE", 65), new Equal("AGE", 18))));
        Assert.assertEquals(" WHERE NOT (\"AGE\" = ? OR \"AGE\" = ?)",
                FilterToWhereTranslator.getWhereStringForFilters(filters, sh));
        EasyMock.verify(sh);
    }

    @Test
    public void getWhereStringForFilters_isNull() {
        ArrayList<Filter> filters = new ArrayList<Filter>();
        filters.add(new IsNull("NAME"));
        Assert.assertEquals(" WHERE \"NAME\" IS NULL", FilterToWhereTranslator
                .getWhereStringForFilters(filters, new StatementHelper()));
    }

    @Test
    public void getWhereStringForFilters_isNotNull() {
        ArrayList<Filter> filters = new ArrayList<Filter>();
        filters.add(new Not(new IsNull("NAME")));
        Assert.assertEquals(" WHERE \"NAME\" IS NOT NULL",
                FilterToWhereTranslator.getWhereStringForFilters(filters,
                        new StatementHelper()));
    }

    @Test
    public void getWhereStringForFilters_customStringDecorator() {
        FilterToWhereTranslator
                .setStringDecorator(new StringDecorator("[", "]"));
        ArrayList<Filter> filters = new ArrayList<Filter>();
        filters.add(new Not(new IsNull("NAME")));
        Assert.assertEquals(" WHERE [NAME] IS NOT NULL",
                FilterToWhereTranslator.getWhereStringForFilters(filters,
                        new StatementHelper()));
    }
}
