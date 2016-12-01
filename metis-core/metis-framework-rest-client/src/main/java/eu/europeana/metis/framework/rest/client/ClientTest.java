package eu.europeana.metis.framework.rest.client;

import eu.europeana.metis.framework.organization.Organization;

import java.util.List;

/**
 * Created by ymamakis on 8/3/16.
 */
public class ClientTest {
    public static void main(String[] args){
        DsOrgRestClient client = new DsOrgRestClient("http://metis-framework-test.cfapps.io");
        try {
            List<Organization> organizations = client.getAllOrganizations();
            System.out.println(client.getAllOrganizations().size());
        } catch (ServerException e) {
            e.printStackTrace();
        }
    }
}
