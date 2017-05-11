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
package eu.europeana.metis.core.rest;

import eu.europeana.metis.core.dao.ZohoClient;
import eu.europeana.metis.core.dao.ZohoRestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Production configuration for Zoho
 * Created by ymamakis on 6/6/16.
 */
@Component
@Profile("production")
public class DefaultZohoZohoRestConfig implements ZohoRestConfig {
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
