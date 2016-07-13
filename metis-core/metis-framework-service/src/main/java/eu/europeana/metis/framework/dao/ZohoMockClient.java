package eu.europeana.metis.framework.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.metis.framework.common.*;
import eu.europeana.metis.framework.crm.*;
import eu.europeana.metis.framework.organization.Organization;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ymamakis on 4/25/16.
 */
public class ZohoMockClient extends ZohoClient {

    private static List<Organization> organizationList = new ArrayList<>();
    private static List<Contact> contactList = new ArrayList<>();

    public void populate() {
        try {
            File f = new File("ZohoResponseOrganization.json");
            if (!f.exists()) {
                generate();
            }
            String str = FileUtils.readFileToString(f);
            ObjectMapper mapper = new ObjectMapper();
            ZohoOrganizationResponse resp = mapper.readValue(str, ZohoOrganizationResponse.class);
            organizationList.add(readResponsetoOrganization(resp.getOrganizationResponse().getOrganizationResult().getModule().getRows().get(0)));

            String str1 = FileUtils.readFileToString(new File("ZohoResponseContact.json"));
            ZohoContactResponse resp1 = mapper.readValue(str1, ZohoContactResponse.class);
            contactList.add(readResponseToContact(resp1.getContactResponse().getContactResult().getModule().getRows().get(0)));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void generate() {
        ZohoOrganizationResponse resp = new ZohoOrganizationResponse();
        OrganizationResponse orgResp = new OrganizationResponse();
        orgResp.setUri("testuri");
        OrganizationResult result = new OrganizationResult();
        Module module = new Module();
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
        role.setContent(Role.CONTENT_PROVIDER.getName());
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
        module.setRows(rows);
        result.setModule(module);
        orgResp.setOrganizationResult(result);
        resp.setOrganizationResponse(orgResp);


        //CONTACT
        ZohoContactResponse contactResponse = new ZohoContactResponse();
        ContactResponse cResp = new ContactResponse();
        ContactResult contactResult = new ContactResult();
        Module module1 = new Module();
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
        contactRow.setFields(contactFields);
        contactRow.setRowNum("0");
        contactRows.add(contactRow);
        module1.setRows(rows);
        contactResult.setModule(module1);
        cResp.setUri("http://lemmy.is.god");
        cResp.setContactResult(contactResult);
        contactResponse.setContactResponse(cResp);
        ObjectMapper mapper = new ObjectMapper();
        try {
            String str = mapper.writeValueAsString(resp);
            File f = new File("ZohoResponseOrganization.json");
            f.createNewFile();
            IOUtils.write(str, new FileOutputStream(f));
            String contactString = mapper.writeValueAsString(contactResponse);
            File contactJson = new File("ZohoResponseContact.json");
            contactJson.createNewFile();
            IOUtils.write(contactString, new FileOutputStream(contactJson));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Organization> getAllOrganizations() throws ParseException, IOException {
        return organizationList;
    }

    @Override
    public Organization getOrganizationById(String id) throws ParseException, IOException {
        return organizationList.get(0);
    }

    @Override
    public Contact getContactByEmail(String email) throws ParseException, IOException {
        return contactList.get(0);
    }




}
