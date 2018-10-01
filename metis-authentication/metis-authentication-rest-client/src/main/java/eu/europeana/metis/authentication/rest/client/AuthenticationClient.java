package eu.europeana.metis.authentication.rest.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.metis.CommonStringValues;
import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.authentication.user.MetisUser;
import eu.europeana.metis.exception.GenericMetisException;
import eu.europeana.metis.exception.UserUnauthorizedException;
import eu.europeana.metis.utils.ExternalRequestUtil;
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
  private final String baseUrl;

  /**
   * Constructs an {@link AuthenticationClient}
   *
   * @param baseUrl the base url endpoint to the authentication REST API
   */
  public AuthenticationClient(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  /**
   * Retrieves a user from the remote REST API using an authorization header that contains an access
   * token.
   *
   * @param authorizationHeader the authorization header containing the access token
   *        <p>
   *        The expected input should follow the rule Bearer accessTokenHere
   *        </p>
   * @return {@link MetisUser}.
   * @throws GenericMetisException which can be one of:
   *         <ul>
   *         <li>{@link UserUnauthorizedException} if the authorization header is un-parsable or the
   *         user cannot be authenticated.</li>
   *         </ul>
   */
  public MetisUser getUserByAccessTokenInHeader(String authorizationHeader)
      throws GenericMetisException {
    RestTemplate restTemplate = new RestTemplate();
    ObjectMapper objectMapper = new ObjectMapper();
    HttpHeaders headers = new HttpHeaders();
    headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
    headers.add("Authorization", authorizationHeader);
    HttpEntity<String> request = new HttpEntity<>(headers);
    try {
      ResponseEntity<String> response =
          ExternalRequestUtil.retryableExternalRequestConnectionReset(() -> restTemplate
              .exchange(String.format("%s%s", baseUrl, RestEndpoints.AUTHENTICATION_USER_BY_TOKEN),
                  HttpMethod.GET, request, String.class));
      return objectMapper.readValue(response != null ? response.getBody() : null, MetisUser.class);
    } catch (HttpClientErrorException e) {
      LOGGER.error("Could not retrieve MetisUser. Exception: {}, ErrorCode: {}, {}",
          e, e.getRawStatusCode(), e.getResponseBodyAsString());
      throw new UserUnauthorizedException(CommonStringValues.WRONG_ACCESS_TOKEN, e);
    } catch (IOException e) {
      LOGGER.error("Could not parse response to Object, {}", e);
      throw new UserUnauthorizedException("Could not parse response to Object, {}", e);
    }
  }
}
