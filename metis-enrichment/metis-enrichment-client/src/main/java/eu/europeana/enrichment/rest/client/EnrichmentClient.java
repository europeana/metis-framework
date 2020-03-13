package eu.europeana.enrichment.rest.client;

import static eu.europeana.metis.RestEndpoints.ENRICHMENT_BYID;
import static eu.europeana.metis.RestEndpoints.ENRICHMENT_BYURI;
import static eu.europeana.metis.RestEndpoints.ENRICHMENT_ENRICH;

import eu.europeana.enrichment.api.exceptions.UnknownException;
import eu.europeana.enrichment.api.external.InputValueList;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.enrichment.utils.InputValue;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  private static final Logger LOGGER = LoggerFactory.getLogger(EnrichmentClient.class);

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
    final String url = endpoint + ENRICHMENT_ENRICH;
    try {
      return template.postForObject(url, inList, EnrichmentResultList.class);
    } catch (RestClientException e) {
      LOGGER.warn("Enrichment client POST call failed: {}.", url, e);
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
      //URLEncoder converts spaces to "+" signs.
      // Replace any plus "+" characters to a proper space encoding "%20".
      encodedUri = URLEncoder.encode(uri, StandardCharsets.UTF_8.name()).replace("+", "%20");
    } catch (UnsupportedEncodingException e) {
      throw new IllegalStateException(e);
    }

    UriComponentsBuilder builder;
    builder = UriComponentsBuilder.fromHttpUrl(endpoint + ENRICHMENT_BYURI)
        .queryParam("uri", encodedUri);

    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", MediaType.APPLICATION_XML_VALUE);
    final HttpEntity<Void> request = new HttpEntity<>(headers);

    final ResponseEntity<EnrichmentBase> response;
    final URI fullUri = builder.build(true).toUri();
    try {
      response = template.exchange(fullUri, HttpMethod.GET,request, EnrichmentBase.class);
    } catch (RestClientException e) {
      LOGGER.warn("Enrichment client GET call failed: {}.", fullUri, e);
      throw new UnknownException("Enrichment client call failed.", e);
    }

    return response.getBody();
  }

  /**
   * Get enrichment information based on a specified list of URIs.
   *
   * @param uriList the list of URIs to enrich
   * @return the enriched information
   */
  public EnrichmentResultList getByUri(Collection<String> uriList) {
    final String url = endpoint + ENRICHMENT_BYURI;
    try {
      return template.postForObject(url, new ArrayList<>(uriList), EnrichmentResultList.class);
    } catch (RestClientException e) {
      LOGGER.warn("Enrichment client POST call failed: {}.", url, e);
      throw new UnknownException("Enrichment client call failed.", e);
    }
  }

  /**
   * Get enrichment information based on a specified list of IDs
   *
   * @param uriList the list of IDs to enrich
   * @return the enriched information
   */
  public EnrichmentResultList getById(Collection<String> uriList) {
    final String url = endpoint + ENRICHMENT_BYID;
    try {
      return template.postForObject(url, new ArrayList<>(uriList), EnrichmentResultList.class);
    } catch (RestClientException e) {
      LOGGER.warn("Enrichment client POST call failed: {}.", url, e);
      throw new UnknownException("Enrichment client call failed.", e);
    }
  }

  void setRestTemplate(RestTemplate template) {
    this.template = template;
  }
}
