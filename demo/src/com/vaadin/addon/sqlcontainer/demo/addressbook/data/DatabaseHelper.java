package com.vaadin.addon.sqlcontainer.demo.addressbook.data;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

import com.vaadin.addon.sqlcontainer.SQLContainer;
import com.vaadin.addon.sqlcontainer.connection.SimpleJDBCConnectionPool;
import com.vaadin.addon.sqlcontainer.query.TableQuery;

public class DatabaseHelper {

    private SimpleJDBCConnectionPool connectionPool = null;
    private SQLContainer personContainer = null;
    private SQLContainer cityContainer = null;

    private boolean debugMode = true;

    public DatabaseHelper() {
        initConnectionPool();
        initDatabase();
        initContainers();
        fillContainers();
    }

    private void initConnectionPool() {
        try {
            connectionPool = new SimpleJDBCConnectionPool(
                    "org.hsqldb.jdbc.JDBCDriver",
                    "jdbc:hsqldb:mem:sqlcontainer", "SA", "", 2, 5);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void initDatabase() {
        try {
            Connection conn = connectionPool.reserveConnection();
            Statement statement = conn.createStatement();
            try {
                statement.executeQuery("SELECT * FROM PERSONADDRESS");
                statement.executeQuery("SELECT * FROM CITY");
            } catch (SQLException e) {
                // Failed, which means that the database is not initialized
                statement
                        .execute("create table city (id integer generated always as identity, name varchar(64), version integer default 0 not null)");
                statement.execute("alter table city add primary key (id)");
                statement
                        .execute("create table personaddress "
                                + "(id integer generated always as identity, "
                                + "firstname varchar(64), lastname varchar(64), "
                                + "email varchar(64), phonenumber varchar(64), "
                                + "streetaddress varchar(128), postalcode integer, "
                                + "cityId integer not null, version integer default 0 not null , "
                                + "FOREIGN KEY (cityId) REFERENCES city(id))");
                statement
                        .execute("alter table personaddress add primary key (id)");
            }
            statement.close();
            conn.commit();
            connectionPool.releaseConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void initContainers() {
        try {
            TableQuery q1 = new TableQuery("personaddress", connectionPool);
            q1.setVersionColumn("VERSION");
            q1.setDebug(debugMode);
            personContainer = new SQLContainer(q1);
            TableQuery q2 = new TableQuery("city", connectionPool);
            q2.setVersionColumn("VERSION");
            q2.setDebug(debugMode);
            cityContainer = new SQLContainer(q2);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void fillContainers() {
        if (personContainer.size() == 0 && cityContainer.size() == 0) {
            /* Create cities */
            final String cities[] = { "Amsterdam", "Berlin", "Helsinki",
                    "Hong Kong", "London", "Luxemburg", "New York", "Oslo",
                    "Paris", "Rome", "Stockholm", "Tokyo", "Turku" };
            for (int i = 0; i < cities.length; i++) {
                Object id = cityContainer.addItem();
                cityContainer.getContainerProperty(id, "NAME").setValue(
                        cities[i]);
            }
            try {
                cityContainer.commit();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            final String[] fnames = { "Peter", "Alice", "Joshua", "Mike",
                    "Olivia", "Nina", "Alex", "Rita", "Dan", "Umberto",
                    "Henrik", "Rene", "Lisa", "Marge" };
            final String[] lnames = { "Smith", "Gordon", "Simpson", "Brown",
                    "Clavel", "Simons", "Verne", "Scott", "Allison", "Gates",
                    "Rowling", "Barks", "Ross", "Schneider", "Tate" };

            final String streets[] = { "4215 Blandit Av.", "452-8121 Sem Ave",
                    "279-4475 Tellus Road", "4062 Libero. Av.",
                    "7081 Pede. Ave", "6800 Aliquet St.",
                    "P.O. Box 298, 9401 Mauris St.", "161-7279 Augue Ave",
                    "P.O. Box 496, 1390 Sagittis. Rd.", "448-8295 Mi Avenue",
                    "6419 Non Av.", "659-2538 Elementum Street",
                    "2205 Quis St.", "252-5213 Tincidunt St.",
                    "P.O. Box 175, 4049 Adipiscing Rd.", "3217 Nam Ave",
                    "P.O. Box 859, 7661 Auctor St.", "2873 Nonummy Av.",
                    "7342 Mi, Avenue", "539-3914 Dignissim. Rd.",
                    "539-3675 Magna Avenue", "Ap #357-5640 Pharetra Avenue",
                    "416-2983 Posuere Rd.", "141-1287 Adipiscing Avenue",
                    "Ap #781-3145 Gravida St.", "6897 Suscipit Rd.",
                    "8336 Purus Avenue", "2603 Bibendum. Av.",
                    "2870 Vestibulum St.", "Ap #722 Aenean Avenue",
                    "446-968 Augue Ave", "1141 Ultricies Street",
                    "Ap #992-5769 Nunc Street", "6690 Porttitor Avenue",
                    "Ap #105-1700 Risus Street",
                    "P.O. Box 532, 3225 Lacus. Avenue", "736 Metus Street",
                    "414-1417 Fringilla Street",
                    "Ap #183-928 Scelerisque Road", "561-9262 Iaculis Avenue" };
            Random r = new Random(0);
            try {
                for (int i = 0; i < 100; i++) {
                    Person p = new Person();
                    p.setFirstName(fnames[r.nextInt(fnames.length)]);
                    p.setLastName(lnames[r.nextInt(lnames.length)]);
                    // p.setCity(cities[r.nextInt(cities.length)]);
                    p.setEmail(p.getFirstName().toLowerCase() + "."
                            + p.getLastName().toLowerCase() + "@vaadin.com");
                    p.setPhoneNumber("+358 02 555 " + r.nextInt(10)
                            + r.nextInt(10) + r.nextInt(10) + r.nextInt(10));
                    int n = r.nextInt(100000);
                    if (n < 10000) {
                        n += 10000;
                    }
                    p.setPostalCode(n);
                    p.setStreetAddress(streets[r.nextInt(streets.length)]);

                    Object id = personContainer.addItem();
                    personContainer.getContainerProperty(id, "FIRSTNAME")
                            .setValue(p.getFirstName());
                    personContainer.getContainerProperty(id, "LASTNAME")
                            .setValue(p.getLastName());
                    personContainer.getContainerProperty(id, "EMAIL").setValue(
                            p.getEmail());
                    personContainer.getContainerProperty(id, "PHONENUMBER")
                            .setValue(p.getPhoneNumber());
                    personContainer.getContainerProperty(id, "STREETADDRESS")
                            .setValue(p.getStreetAddress());
                    personContainer.getContainerProperty(id, "POSTALCODE")
                            .setValue(p.getPostalCode());
                    personContainer.getContainerProperty(id, "CITYID")
                            .setValue(r.nextInt(cities.length));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                personContainer.commit();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public SQLContainer getPersonContainer() {
        return personContainer;
    }

    public SQLContainer getCityContainer() {
        return cityContainer;
    }
}
