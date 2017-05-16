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
package eu.europeana.metis.core.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.metis.core.common.Contact;
import eu.europeana.metis.core.common.Country;
import eu.europeana.metis.core.common.Domain;
import eu.europeana.metis.core.common.GeographicLevel;
import eu.europeana.metis.core.common.OrganizationRole;
import eu.europeana.metis.core.common.Sector;
import eu.europeana.metis.core.crm.ContactResponse;
import eu.europeana.metis.core.crm.ContactResult;
import eu.europeana.metis.core.crm.Field;
import eu.europeana.metis.core.crm.Module;
import eu.europeana.metis.core.crm.OrganizationResponse;
import eu.europeana.metis.core.crm.OrganizationResult;
import eu.europeana.metis.core.crm.Row;
import eu.europeana.metis.core.crm.ZohoContactResponse;
import eu.europeana.metis.core.crm.ZohoFields;
import eu.europeana.metis.core.crm.ZohoOrganizationResponse;
import eu.europeana.metis.core.exceptions.UserNotFoundException;
import eu.europeana.metis.core.organization.Organization;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * Zoho Mock Client for the inmemory implementation
 * Created by ymamakis on 4/25/16.
 */
public class ZohoMockClient extends ZohoClient {

  private static List<Organization> organizationList = new ArrayList<>();
  private static List<Contact> contactList = new ArrayList<>();
  private final String organizationsFileName = "ZohoResponseOrganization.json";
  private final String contactsFileName = "ZohoResponseContact.json";

  public void populate() {
    try {
      File organizationsFile = new File(organizationsFileName);
      File contactsFile = new File(contactsFileName);
      generateOrganizations();
      generateContacts();

      String organizationsJsonString = FileUtils.readFileToString(organizationsFile);
      ObjectMapper mapper = new ObjectMapper();
      ZohoOrganizationResponse zohoOrganizationResponse = mapper.readValue(organizationsJsonString, ZohoOrganizationResponse.class);
      organizationList.add(readResponsetoOrganization(
          zohoOrganizationResponse.getOrganizationResponse().getOrganizationResult().getModule().getRows().get(0)));

      String contactsJsonString = FileUtils.readFileToString(contactsFile);
      ZohoContactResponse zohoContactResponse = mapper.readValue(contactsJsonString, ZohoContactResponse.class);
      contactList.add(readResponseToContact(
          zohoContactResponse.getContactResponse().getContactResult().getModule().getRows().get(0)));
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ParseException e) {
      e.printStackTrace();
    }
  }

  private void generateOrganizations() throws IOException {
    ZohoOrganizationResponse resp = new ZohoOrganizationResponse();
    OrganizationResponse orgResp = new OrganizationResponse();
    orgResp.setUri("testuri");
    OrganizationResult result = new OrganizationResult();
    Module organizationModule = new Module();
    List<Row> rows = new ArrayList<>();
    Row row = new Row();
    row.setRowNum("0");
    List<Field> fields = new ArrayList<>();
    Field idField = new Field();
    idField.setVal(ZohoFields.ID);
    idField.setContent("testId");
    fields.add(idField);
    Field acronymField = new Field();
    acronymField.setVal(ZohoFields.ACRONYM);
    acronymField.setContent("testAcronym");
    fields.add(acronymField);
    Field nameField = new Field();
    nameField.setVal(ZohoFields.NAME);
    nameField.setContent("testName");
    fields.add(nameField);
    Field createdTime = new Field();
    createdTime.setVal(ZohoFields.CREATEDTIME);
    createdTime.setContent("1970-01-01 00:00:00");
    fields.add(createdTime);
    Field updatedTime = new Field();
    updatedTime.setVal(ZohoFields.MODIFIEDTIME);
    updatedTime.setContent("2016-01-01 00:00:00");
    fields.add(updatedTime);
    Field role = new Field();
    role.setVal(ZohoFields.ROLE);
    role.setContent(OrganizationRole.CONTENT_PROVIDER.getName());
    fields.add(role);
    Field country = new Field();
    country.setVal(ZohoFields.COUNTRY);
    country.setContent(Country.ALBANIA.getName());
    fields.add(country);
    Field domain = new Field();
    domain.setVal(ZohoFields.DOMAIN);
    domain.setContent(Domain.CONSULTANT.getName());
    fields.add(domain);
    Field geographicLevel = new Field();
    geographicLevel.setVal(ZohoFields.GEOGRAPHICLEVEL);
    geographicLevel.setContent(GeographicLevel.EUROPEAN.getName());
    fields.add(geographicLevel);
    Field website = new Field();
    website.setVal(ZohoFields.WEBSITE);
    website.setContent("http://test.com");
    fields.add(website);
    Field sector = new Field();
    sector.setVal(ZohoFields.SECTOR);
    sector.setContent(Sector.PRIVATE.getName());
    fields.add(sector);
    row.setFields(fields);
    rows.add(row);
    organizationModule.setRows(rows);
    result.setModule(organizationModule);
    orgResp.setOrganizationResult(result);
    resp.setOrganizationResponse(orgResp);

    ObjectMapper mapper = new ObjectMapper();
    String str = mapper.writeValueAsString(resp);
    File f = new File(organizationsFileName);
    f.createNewFile();
    IOUtils.write(str, new FileOutputStream(f));
  }

  private void generateContacts() throws IOException {
    ZohoContactResponse contactResponse = new ZohoContactResponse();
    ContactResponse cResp = new ContactResponse();
    ContactResult contactResult = new ContactResult();
    Module contactModule = new Module();
    List<Row> contactRows = new ArrayList<>();
    Row contactRow = new Row();
    List<Field> contactFields = new ArrayList<>();
    Field email = new Field();
    email.setVal(ZohoFields.EMAIL);
    email.setContent("lemmy@god.com");
    contactFields.add(email);
    Field firstName = new Field();
    firstName.setVal(ZohoFields.FIRSTNAME);
    firstName.setContent("Lemmy");
    contactFields.add(firstName);
    Field lastName = new Field();
    lastName.setVal(ZohoFields.LASTNAME);
    lastName.setContent("Killminster");
    contactFields.add(lastName);
    Field skypeId = new Field();
    skypeId.setVal(ZohoFields.SKYPEID);
    skypeId.setContent("lemmy");
    contactFields.add(skypeId);
    Field website = new Field();
    website.setVal(ZohoFields.WEBSITE);
    website.setContent("http://lemmy.is.god");
    contactFields.add(website);
    contactRow.setFields(contactFields);
    contactRow.setRowNum("0");
    contactRows.add(contactRow);
    contactModule.setRows(contactRows);
    contactResult.setModule(contactModule);
    cResp.setUri("http://lemmy.is.god");
    cResp.setContactResult(contactResult);
    contactResponse.setContactResponse(cResp);
    ObjectMapper mapper = new ObjectMapper();
    String contactString = mapper.writeValueAsString(contactResponse);
    File contactJson = new File(contactsFileName);
    contactJson.createNewFile();
    IOUtils.write(contactString, new FileOutputStream(contactJson));
  }

  @Override
  public List<Organization> getAllOrganizations() throws ParseException, IOException {
    return organizationList;
  }

  @Override
  public Organization getOrganizationById(String organizationId) throws ParseException, IOException {
    for (Organization organization :
        organizationList) {
      if (organization.getOrganizationId().equalsIgnoreCase(organizationId))
      {
        return organization;
      }
    }
    return null;
  }

  @Override
  public Contact getContactByEmail(String email) throws UserNotFoundException {
    if(email == null || !email.equalsIgnoreCase(contactList.get(0).getEmail()))
      throw new UserNotFoundException(email);

    return contactList.get(0);
  }


}
