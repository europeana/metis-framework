package eu.europeana.metis.authentication.rest.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.authentication.user.MetisUser;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-12-28
 */
public class AuthenticationClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationClient.class);
  private String baseUrl;

  public AuthenticationClient(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public MetisUser getUserByAccessTokenInHeader(String authorizationHeader) {
    RestTemplate restTemplate = new RestTemplate();
    ObjectMapper objectMapper = new ObjectMapper();
    HttpHeaders headers = new HttpHeaders();
    headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
    headers.add("Authorization", authorizationHeader);
    HttpEntity<String> request = new HttpEntity<>(headers);
    try {
      ResponseEntity<String> response =
          restTemplate
              .exchange(String.format("%s%s", baseUrl, RestEndpoints.AUTHENTICATION_USER_BY_TOKEN),
                  HttpMethod.GET, request, String.class);
      String responseBody = response.getBody();
      return objectMapper.readValue(responseBody, MetisUser.class);
    } catch (HttpClientErrorException e) {
      LOGGER.error("Could not retrieve MetisUser. Exception: {}, ErrorCode: {}, {}",
          e, e.getRawStatusCode(), e.getResponseBodyAsString());
    } catch (IOException e) {
      LOGGER.error("Could not parse response to Object, {}", e);
    }
    return null;
  }
}
