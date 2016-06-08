package eu.europeana.metis.framework.rest;

import eu.europeana.metis.framework.dao.ZohoClient;
import eu.europeana.metis.framework.dao.ZohoMockClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Created by ymamakis on 6/6/16.
 */
@Component
@Profile("development")
public class InMemoryZohoRestConfig implements RestConfig{
    @Override
    public ZohoClient getZohoClient() {
        ZohoMockClient client = new ZohoMockClient();
        client.populate();
        return client;
    }
}
