package eu.europeana.metis.framework.rest;

import eu.europeana.metis.framework.dao.ZohoClient;
import eu.europeana.metis.framework.dao.ZohoRestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Created by ymamakis on 6/6/16.
 */
@Component
@Profile("production")
public class ZohoRestConfig implements RestConfig {
    @Value("${crm.scope}")
    private String scope;
    @Value("${crm.authtoken}")
    private String authtoken;
    @Value("${crm.baseUrl}")
    private String baseUrl;
    @Override
    public ZohoClient getZohoClient() {
        return new ZohoRestClient(baseUrl,authtoken,scope);
    }
}
