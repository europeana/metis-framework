/*
 * Copyright 2007-2013 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */
package eu.europeana.metis.framework.dao;

import eu.europeana.metis.framework.common.*;
import eu.europeana.metis.framework.crm.Field;
import eu.europeana.metis.framework.crm.Row;
import eu.europeana.metis.framework.crm.ZohoFields;
import eu.europeana.metis.framework.organization.Organization;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Zoho client
 * Created by ymamakis on 4/25/16.
 */
public abstract class ZohoClient {

    public abstract List<Organization> getAllOrganizations() throws ParseException, IOException;

    public abstract Organization getOrganizationById(String id) throws ParseException, IOException;

    public abstract Contact getContactByEmail(String email) throws ParseException, IOException;

    protected Organization readResponsetoOrganization(Row row) throws ParseException, MalformedURLException {
        Organization org = new Organization();
        DateFormat fd = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
        for (Field field : row.getFields()) {

            switch (field.getVal()) {
                case ZohoFields.ID:
                    org.setOrganizationId(field.getContent());
                    break;
                case ZohoFields.ACRONYM:
                    org.setAcronym(field.getContent());
                    break;
                case ZohoFields.NAME:
                    org.setName(field.getContent());
                    break;
                case ZohoFields.CREATEDTIME:
                    org.setCreated(fd.parse(field.getContent()));
                    break;
                case ZohoFields.MODIFIEDTIME:
                    org.setModified(fd.parse(field.getContent()));
                    break;
                case ZohoFields.ROLE:
                    List<String> roles = Arrays.asList(field.getContent().split(";"));
                    List<Role> metisRoles = new ArrayList<>();
                    for (String role : roles) {
                        metisRoles.add(Role.getRoleFromName(role));
                    }
                    org.setRoles(metisRoles);
                    break;
                case ZohoFields.COUNTRY:
                    org.setCountry(Country.getCountryFromName(field.getContent()));
                    break;
                case ZohoFields.DOMAIN:
                    org.setDomain(Domain.getDomainFromName(field.getContent()));
                    break;
                case ZohoFields.GEOGRAPHICLEVEL:
                    org.setGeographicLevel(GeographicLevel.getGeographicLevelFromName(field.getContent()));
                    break;
                case ZohoFields.WEBSITE:
                    org.setWebsite(new URL(field.getContent()));
                    break;
                case ZohoFields.SECTOR:
                    org.setSector(Sector.getSectorFromName(field.getContent()));
            }

        }


        return org;
    }

    protected Contact readResponseToContact(Row row) {
        Contact contact = new Contact();
        for (Field field : row.getFields()) {
            switch (field.getVal()) {
                case ZohoFields.EMAIL:
                    contact.setEmail(field.getContent());
                    break;
                case ZohoFields.FIRSTNAME:
                    contact.setFirstName(field.getContent());
                    break;
                case ZohoFields.LASTNAME:
                    contact.setLastName(field.getContent());
                    break;
            }
        }
        return contact;
    }
}
