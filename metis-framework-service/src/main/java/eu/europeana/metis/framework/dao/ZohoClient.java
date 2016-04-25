package eu.europeana.metis.framework.dao;

import eu.europeana.metis.framework.common.Contact;
import eu.europeana.metis.framework.organization.Organization;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

/**
 * Created by ymamakis on 4/25/16.
 */
public interface ZohoClient {

    List<Organization> getAllOrganizations() throws ParseException, IOException;

    Organization getOrganizationById(String id) throws ParseException, IOException;

    Contact getContactByEmail(String email) throws ParseException,IOException;
}
