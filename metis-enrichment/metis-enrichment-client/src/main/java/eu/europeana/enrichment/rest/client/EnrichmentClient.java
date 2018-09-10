package eu.europeana.enrichment.rest.client;

import static eu.europeana.metis.RestEndpoints.ENRICHMENT_BYURI;
import static eu.europeana.metis.RestEndpoints.ENRICHMENT_ENRICH;

import eu.europeana.enrichment.api.exceptions.UnknownException;
import eu.europeana.enrichment.api.external.InputValueList;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.enrichment.utils.InputValue;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * REST API wrapper class abstracting the REST calls and providing a clean POJO implementation
 *
 * @author Yorgos.Mamakis@ europeana.eu
 */
public class EnrichmentClient {

  private final String endpoint;
  private RestTemplate template = new RestTemplate();

  /**
   * Constructor with required endpoint prefix.
   *
   * @param endpoint the endpoint of the rest api
   */
  public EnrichmentClient(String endpoint) {
    this.endpoint = endpoint;
  }

  /**
   * Enrich REST call invocation
   *
   * @param values The values to be enriched
   * @return The enrichments generated for the input values
   */
  public EnrichmentResultList enrich(List<InputValue> values) {
    InputValueList inList = new InputValueList();
    inList.setInputValues(values);

    try {
      return template
          .postForObject(endpoint + ENRICHMENT_ENRICH, inList, EnrichmentResultList.class);
    } catch (RestClientException e) {
      throw new UnknownException("Enrichment client call failed.", e);
    }
  }

  /**
   * Get enrichment information based on a specified URI.
   *
   * @param uri the URI to enrich
   * @return the enriched information
   */
  public EnrichmentBase getByUri(String uri) {

    String encodedUri;
    try {
      encodedUri = URLEncoder.encode(uri, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new IllegalStateException(e);
    }

    UriComponentsBuilder builder;
    builder = UriComponentsBuilder.fromHttpUrl(endpoint + ENRICHMENT_BYURI)
        .queryParam("uri", encodedUri);

    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", MediaType.APPLICATION_XML_VALUE);
    final HttpEntity<Void> request = new HttpEntity<>(headers);

    final ResponseEntity<EnrichmentBase> response = template
        .exchange(builder.build(true).toUri(), HttpMethod.GET,
            request, EnrichmentBase.class);

    return response.getBody();
  }

  void setRestTemplate(RestTemplate template) {
    this.template = template;
  }
}
