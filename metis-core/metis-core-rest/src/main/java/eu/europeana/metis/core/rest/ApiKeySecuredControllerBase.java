package eu.europeana.metis.core.rest;

import eu.europeana.metis.core.api.MetisKey;
import eu.europeana.metis.core.api.Options;
import eu.europeana.metis.core.exceptions.ApiKeyNotAuthorizedException;
import eu.europeana.metis.core.exceptions.NoApiKeyFoundException;
import eu.europeana.metis.core.service.MetisAuthorizationService;


public abstract class ApiKeySecuredControllerBase {

  protected final MetisAuthorizationService authorizationService;

  public ApiKeySecuredControllerBase(
      MetisAuthorizationService authorizationService) {
    this.authorizationService = authorizationService;
  }

  protected MetisKey ensureValidKey(String apikey)
      throws NoApiKeyFoundException {
    MetisKey key = authorizationService.getKeyFromId(apikey);
    if (key == null) {
      throw new NoApiKeyFoundException(apikey);
    }
    return key;
  }

  protected void ensureActionAutorized(String apikey, MetisKey key, Options write)
      throws ApiKeyNotAuthorizedException {
    if (!key.getOptions().equals(write)) {
      throw new ApiKeyNotAuthorizedException(apikey);
    }
  }

  protected void ensureReadOrWriteAccess(String apikey, MetisKey key)
      throws ApiKeyNotAuthorizedException {
    if (!key.getOptions().equals(Options.WRITE) && !key.getOptions().equals(Options.READ)) {
      //Cannot happen now since the only two options are WRITE and READ
      throw new ApiKeyNotAuthorizedException(apikey);
    }
  }
}
