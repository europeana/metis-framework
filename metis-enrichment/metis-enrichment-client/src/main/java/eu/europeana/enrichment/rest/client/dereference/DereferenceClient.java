package eu.europeana.enrichment.rest.client.dereference;

import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.metis.utils.RestEndpoints;
import eu.europeana.metis.dereference.Vocabulary;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

/**
 * A REST wrapper client to be used for dereferencing
 * <p>
 * Created by ymamakis on 2/15/16.
 */
public class DereferenceClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(DereferenceClient.class);

  private final String hostUrl;
  private final RestTemplate restTemplate;

  public DereferenceClient(RestTemplate restTemplate, String hostUrl) {
    this.restTemplate = restTemplate;
    this.hostUrl = hostUrl;
  }

  /**
   * Retrieve all the vocabularies
   *
   * @return The list of all vocabularies
   */
  public List<Vocabulary> getAllVocabularies() {
    final HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    final HttpEntity<String> entity = new HttpEntity<>(headers);
    final String uriString = hostUrl + RestEndpoints.VOCABULARIES;
    final URI uri;
    try {
      // Normalize to remove double '/' between host and path.
      uri = new URI(uriString).normalize();
    } catch (URISyntaxException e) {
      // Cannot really happen.
      LOGGER.warn("URL [{}] is not valid.", uriString, e);
      throw new IllegalStateException("Could not contact dereference service.");
    }
    final Vocabulary[] result = restTemplate.exchange(uri,
            HttpMethod.GET, entity, Vocabulary[].class).getBody();
    return Optional.ofNullable(result).map(Arrays::asList).orElseGet(Collections::emptyList);
  }

  /**
   * Dereference an entity
   *
   * @param resourceId the resource ID (URI) to dereference
   * @return A string of the referenced response
   */
  public EnrichmentResultList dereference(String resourceId) {

    // Encode the resource ID.
    String resourceString;
    try {
      //URLEncoder converts spaces to "+" signs.
      // Replace any plus "+" characters to a proper space encoding "%20".
      resourceString = URLEncoder.encode(resourceId, StandardCharsets.UTF_8.name())
          .replace("+", "%20");
    } catch (UnsupportedEncodingException e) {
      throw new IllegalStateException(e);
    }

    // Check that it has the right scheme.
    if (!resourceString.startsWith("http") && !resourceString.startsWith("https")) {
      return null;
    }

    // Compile the dereference URI (normalize to remove double '/' between host and path).
    final String dereferenceUrlString =
        hostUrl + RestEndpoints.DEREFERENCE + "?uri=" + resourceString;
    URI dereferenceUrl;
    try {
      dereferenceUrl = new URI(dereferenceUrlString).normalize();
    } catch (URISyntaxException e) {
      // Cannot really happen.
      LOGGER.warn("URL [{}] is not valid.", dereferenceUrlString, e);
      throw new IllegalStateException("Could not contact dereference service.", e);
    }

    // Execute the dereference call.
    final HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_XML));
    final HttpEntity<String> entity = new HttpEntity<>(headers);

    // Make the call and return the result.
    return restTemplate
            .exchange(dereferenceUrl, HttpMethod.GET, entity, EnrichmentResultList.class).getBody();
  }
}
