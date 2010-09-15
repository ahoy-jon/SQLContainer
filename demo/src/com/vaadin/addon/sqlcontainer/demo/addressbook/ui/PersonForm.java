package com.vaadin.addon.sqlcontainer.demo.addressbook.ui;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import com.vaadin.addon.sqlcontainer.SQLContainer;
import com.vaadin.addon.sqlcontainer.demo.addressbook.AddressBookApplication;
import com.vaadin.addon.sqlcontainer.demo.addressbook.data.Person;
import com.vaadin.data.Buffered;
import com.vaadin.data.Item;
import com.vaadin.data.validator.EmailValidator;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

@SuppressWarnings("serial")
public class PersonForm extends Form implements ClickListener {

    private Button save = new Button("Save", (ClickListener) this);
    private Button cancel = new Button("Cancel", (ClickListener) this);
    private Button edit = new Button("Edit", (ClickListener) this);
    private final ComboBox cities = new ComboBox("City");

    private AddressBookApplication app;
    private boolean newContactMode = false;
    private Person newPerson = null;

    private Object tempItemId = null;

    public PersonForm(AddressBookApplication app) {
        this.app = app;

        /*
         * Enable buffering so that commit() must be called for the form before
         * input is written to the data. (Form input is not written immediately
         * through to the underlying object.)
         */
        setWriteThrough(false);

        HorizontalLayout footer = new HorizontalLayout();
        footer.setSpacing(true);
        footer.addComponent(save);
        footer.addComponent(cancel);
        footer.addComponent(edit);
        footer.setVisible(false);

        setFooter(footer);

        /* Allow the user to enter new cities */
        cities.setNewItemsAllowed(true);
        /* We do not want to use null values */
        cities.setNullSelectionAllowed(false);
        /* Add an empty city used for selecting no city */
        cities.addItem("");

        /* Populate cities select using the cities in the data container */
        SQLContainer ds = app.getDbHelp().getCityContainer();
        for (Object cityItemId : ds.getItemIds()) {
            int cityId = (Integer) ds.getItem(cityItemId).getItemProperty("ID")
                    .getValue();
            String city = ds.getItem(cityItemId).getItemProperty("NAME")
                    .getValue().toString();
            cities.addItem(cityId);
            cities.setItemCaption(cityId, city);
        }
        /*
         * Field factory for overriding how the component for city selection is
         * created
         */
        setFormFieldFactory(new DefaultFieldFactory() {
            @Override
            public Field createField(Item item, Object propertyId,
                    Component uiContext) {
                if (propertyId.equals("CITYID")) {
                    cities.setWidth("200px");
                    return cities;
                }

                Field field = super.createField(item, propertyId, uiContext);
                if (propertyId.equals("POSTALCODE")) {
                    TextField tf = (TextField) field;
                    /* Add a validator for postalCode and make it required */
                    tf
                            .addValidator(new RegexpValidator("[1-9][0-9]{4}",
                                    "Postal code must be a five digit number and cannot start with a zero."));
                    tf.setRequired(true);
                } else if (propertyId.equals("EMAIL")) {
                    /* Add a validator for email and make it required */
                    field.addValidator(new EmailValidator(
                            "Email must contain '@' and have full domain."));
                    field.setRequired(true);

                }
                /* Set null representation of all text fields to empty string */
                if (field instanceof TextField) {
                    ((TextField) field).setNullRepresentation("");
                }
                field.setWidth("200px");
                return field;
            }
        });
    }

    public void buttonClick(ClickEvent event) {
        Button source = event.getButton();
        if (source == save) {
            /* If the given input is not valid there is no point in continuing */
            if (!isValid()) {
                return;
            }
            commit();
        } else if (source == cancel) {
            discard();
        } else if (source == edit) {
            setReadOnly(false);
        }
    }

    @Override
    public void setItemDataSource(Item newDataSource) {
        newContactMode = false;
        if (newDataSource != null) {
            List<Object> orderedProperties = Arrays
                    .asList(PersonList.NATURAL_COL_ORDER);
            super.setItemDataSource(newDataSource, orderedProperties);
            setReadOnly(true);
            getFooter().setVisible(true);
        } else {
            super.setItemDataSource(null);
            getFooter().setVisible(false);
        }
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        save.setVisible(!readOnly);
        cancel.setVisible(!readOnly);
        edit.setVisible(readOnly);
    }

    public void addContact() {
        tempItemId = app.getDbHelp().getPersonContainer().addItem();
        setItemDataSource(app.getDbHelp().getPersonContainer().getItem(
                tempItemId));
        newContactMode = true;
        setReadOnly(false);
    }

    @Override
    public void commit() throws Buffered.SourceException {
        super.commit();
        // TODO: Here we should determine whether the city was an existing one
        // or was it just added. If it is new, we must add it to the city -table
        // in the DB prior to adding the person into the DB.

        try {
            app.getDbHelp().getPersonContainer().commit();
        } catch (UnsupportedOperationException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (newContactMode) {
            newContactMode = false;
            tempItemId = null;
        }
        setReadOnly(true);
    }

    @Override
    public void discard() throws Buffered.SourceException {
        super.discard();
        if (newContactMode) {
            app.getDbHelp().getPersonContainer().removeItem(tempItemId);
            tempItemId = null;
            newContactMode = false;
            /* Clear the form and make it invisible */
            setItemDataSource(null);
        }
        setReadOnly(true);
    }
}