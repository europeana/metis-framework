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
package eu.europeana.redirects.service;

import eu.europeana.redirects.model.RedirectRequest;
import eu.europeana.redirects.model.RedirectRequestList;
import eu.europeana.redirects.model.RedirectResponse;
import eu.europeana.redirects.model.RedirectResponseList;

/**
 * Europeana Redirects service
 *
 * Created by ymamakis on 1/13/16.
 */
public interface RedirectService {

    /**
     * Generate a redirect for a given identifier based on the parameters supplied
     * @param request The object that describes what Europeana Identifier should be redirected and how
     * @return A response with the result of the redirect action
     */
    RedirectResponse createRedirect(RedirectRequest request);

    /**
     * Generate redirects from a batch request
     * @param requests The object that contains a list of objects to be redirected
     * @return The redirect action response
     */
    RedirectResponseList createRedirects(RedirectRequestList requests);
}
