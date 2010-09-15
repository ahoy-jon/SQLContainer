package com.vaadin.addon.sqlcontainer.demo.addressbook.ui;

import com.vaadin.addon.sqlcontainer.SQLContainer;
import com.vaadin.addon.sqlcontainer.demo.addressbook.AddressBookApplication;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Table;

@SuppressWarnings("serial")
public class PersonList extends Table {
    /**
     * Natural property order for Person bean. Used in tables and forms.
     */
    public static final Object[] NATURAL_COL_ORDER = new Object[] {
            "FIRSTNAME", "LASTNAME", "EMAIL", "PHONENUMBER", "STREETADDRESS",
            "POSTALCODE", "CITYID" };

    /**
     * "Human readable" captions for properties in same order as in
     * NATURAL_COL_ORDER.
     */
    public static final String[] COL_HEADERS_ENGLISH = new String[] {
            "First name", "Last name", "Email", "Phone number",
            "Street Address", "Postal Code", "City" };

    public PersonList(AddressBookApplication app) {
        setSizeFull();

        /* Get the SQLContainer containing the persons in the address book */
        setContainerDataSource(app.getDbHelp().getPersonContainer());

        /* Get SQLContainer containing Cities and set it as data in this Table */
        setData(app.getDbHelp().getCityContainer());

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
         * SQLContainer
         */
        addGeneratedColumn("CITYID", new ColumnGenerator() {
            public Component generateCell(Table source, Object itemId,
                    Object columnId) {
                Label l = new Label();
                int cityId = (Integer) getItem(itemId)
                        .getItemProperty("CITYID").getValue();
                Object cityItemId = ((SQLContainer) source.getData())
                        .getIdByIndex(cityId);
                String cityName = ((SQLContainer) source.getData()).getItem(
                        cityItemId).getItemProperty("NAME").getValue()
                        .toString();

                l.setValue(cityName);
                l.setSizeUndefined();
                return l;
            }
        });

        /* Set visible columns, their ordering and their headers. */
        setVisibleColumns(NATURAL_COL_ORDER);
        setColumnHeaders(COL_HEADERS_ENGLISH);
    }
}