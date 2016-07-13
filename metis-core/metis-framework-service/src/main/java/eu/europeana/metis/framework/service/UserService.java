package eu.europeana.metis.framework.service;

import eu.europeana.metis.framework.common.Contact;
import eu.europeana.metis.framework.dao.ZohoClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.text.ParseException;

/**
 * Created by ymamakis on 4/5/16.
 */
public class UserService {
    @Autowired
    private ZohoClient restClient;

    public Contact getUserByEmail(String email) throws IOException, ParseException {
        return restClient.getContactByEmail(email);
    }
}
