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
