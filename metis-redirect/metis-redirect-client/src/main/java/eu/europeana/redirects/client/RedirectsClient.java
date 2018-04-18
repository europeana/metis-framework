package eu.europeana.redirects.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.europeana.metis.RestEndpoints;
import eu.europeana.redirects.model.RedirectRequest;
import eu.europeana.redirects.model.RedirectRequestList;
import eu.europeana.redirects.model.RedirectResponse;
import eu.europeana.redirects.model.RedirectResponseList;
import java.io.IOException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;


/**
 * Redirects REST client
 * Created by ymamakis on 1/15/16.
 */
public class RedirectsClient {

  private RestTemplate restTemplate;
  private Config config;

  public RedirectsClient() throws IOException {
    restTemplate = new RestTemplate();
    config = new Config();
  }

  /**
   * Request for a redirect for a single record
   *
   * @param request The request for a redirect
   * @return A response with the redirect generated if any
   */

  public RedirectResponse redirectSingle(RedirectRequest request) throws JsonProcessingException {

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<RedirectRequest> req = new HttpEntity<>(request, headers);
    return restTemplate
        .postForEntity(config.getRedirectsPath() + RestEndpoints.REDIRECT_SINGLE, req,
            RedirectResponse.class).getBody();
  }

  /**
   * Request for batch redirects
   *
   * @param requests The list of redirect requests
   * @return A list of responses for each redirect
   */
  public RedirectResponseList redirectBatch(RedirectRequestList requests)
      throws JsonProcessingException {
    return restTemplate
        .postForObject(config.getRedirectsPath() + RestEndpoints.REDIRECT_BATCH, requests,
            RedirectResponseList.class);
  }
}
