package com.vaadin.addon.sqlcontainer.demo.addressbook.ui;

import com.vaadin.addon.sqlcontainer.demo.addressbook.AddressBookApplication;
import com.vaadin.addon.sqlcontainer.demo.addressbook.data.DatabaseHelper;
import com.vaadin.addon.sqlcontainer.query.FilteringMode;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Table;

@SuppressWarnings("serial")
public class PersonList extends Table {
    public PersonList(final AddressBookApplication app) {
        setSizeFull();

        /* Get the SQLContainer containing the persons in the address book */
        setContainerDataSource(app.getDbHelp().getPersonContainer());

        /*
         * Remove container filters and set container filtering mode to
         * inclusive
         */
        app.getDbHelp().getPersonContainer().removeAllContainerFilters();
        app.getDbHelp().getPersonContainer().setFilteringMode(
                FilteringMode.FILTERING_MODE_INCLUSIVE);

        setColumnCollapsingAllowed(true);
        setColumnReorderingAllowed(true);

        /*
         * Make table selectable, react immediatedly to user events, and pass
         * events to the controller (our main application)
         */
        setSelectable(true);
        setImmediate(true);
        addListener((ValueChangeListener) app);
        /* We don't want to allow users to de-select a row */
        setNullSelectionAllowed(false);

        /* Customize email column to have mailto: links using column generator */
        addGeneratedColumn("EMAIL", new ColumnGenerator() {
            public Component generateCell(Table source, Object itemId,
                    Object columnId) {
                Link l = new Link();
                l.setResource(new ExternalResource("mailto:"
                        + getItem(itemId).getItemProperty("EMAIL").getValue()));
                l.setCaption(getItem(itemId).getItemProperty("EMAIL")
                        .getValue().toString());
                return l;
            }
        });

        /*
         * Create a cityName column that fetches the city name from another
         * SQLContainer through the DatabaseHelper.
         */
        addGeneratedColumn("CITYID", new ColumnGenerator() {
            public Component generateCell(Table source, Object itemId,
                    Object columnId) {
                Label l = new Label();
                int cityId = (Integer) getItem(itemId)
                        .getItemProperty("CITYID").getValue();
                l.setValue(app.getDbHelp().getCityName(cityId));
                l.setSizeUndefined();
                return l;
            }
        });

        /* Set visible columns, their ordering and their headers. */
        setVisibleColumns(DatabaseHelper.NATURAL_COL_ORDER);
        setColumnHeaders(DatabaseHelper.COL_HEADERS_ENGLISH);
    }
}