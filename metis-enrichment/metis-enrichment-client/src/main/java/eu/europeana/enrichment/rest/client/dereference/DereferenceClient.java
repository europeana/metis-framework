package eu.europeana.enrichment.rest.client.dereference;

import eu.europeana.enrichment.api.exceptions.UnknownException;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.enrichment.rest.client.TemporaryResponseConverter;
import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.dereference.Vocabulary;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    return restTemplate.getForObject(hostUrl + RestEndpoints.VOCABULARIES, List.class);
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

    // Compile the dereference URI.
    final String dereferenceUrlString =
        hostUrl + RestEndpoints.DEREFERENCE + "?uri=" + resourceString;
    URI dereferenceUrl;
    try {
      dereferenceUrl = new URI(hostUrl + RestEndpoints.DEREFERENCE + "?uri=" + resourceString);
    } catch (URISyntaxException e) {
      // Cannot really happen.
      LOGGER.warn("URL [" + dereferenceUrlString + "] is not valid.", e);
      return null;
    }

    // Execute the dereference call.
    final HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_XML));
    final HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

    // Make the call and return the result.
    final ResponseEntity<byte[]> result = restTemplate
            .exchange(dereferenceUrl, HttpMethod.GET, entity, byte[].class);
    try {
      return TemporaryResponseConverter.convert(result);
    } catch (JAXBException e) {
      LOGGER.warn("URL [{}] could not be deserialized.", dereferenceUrlString, e);
      throw new UnknownException("Dereference client call failed.", e);
    }
  }
}
