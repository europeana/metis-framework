package eu.europeana.enrichment.rest.client.enrichment;

import static eu.europeana.metis.utils.RestEndpoints.ENRICH_ENTITY_ABOUT;
import static eu.europeana.metis.utils.RestEndpoints.ENRICH_ENTITY_ABOUT_OR_OWLSAMEAS;
import static eu.europeana.metis.utils.RestEndpoints.ENRICH_INPUT_VALUE_LIST;

import eu.europeana.enrichment.api.exceptions.UnknownException;
import eu.europeana.enrichment.api.external.InputValueList;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentBaseWrapper;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.enrichment.utils.InputValue;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
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
  private final RestTemplate template;
  private final int batchSize;

  /**
   * Constructor with required endpoint prefix.
   *
   * @param template The rest template to use.
   * @param endpoint the endpoint of the rest api.
   * @param batchSize The batch size.
   */
  public EnrichmentClient(RestTemplate template, String endpoint, int batchSize) {
    this.template = template;
    this.endpoint = endpoint;
    this.batchSize = batchSize;
  }

  private static <T> HttpEntity<T> createRequest(T body) {
    final HttpHeaders headers = new HttpHeaders();
    if (body != null) {
      headers.setContentType(MediaType.APPLICATION_JSON);
    }
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_XML));
    return new HttpEntity<>(body, headers);
  }

  /**
   * Enrich REST call invocation
   *
   * @param values The values to be enriched
   * @return The enrichments generated for the input values
   */
  public EnrichmentResultList enrich(List<InputValue> values) {
    final InputValueList inList = new InputValueList();
    inList.setInputValues(values);
    final String url = endpoint + ENRICH_INPUT_VALUE_LIST;
    try {
      return template.postForObject(endpoint + ENRICH_INPUT_VALUE_LIST, createRequest(inList),
              EnrichmentResultList.class);
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

    final UriComponentsBuilder builder = UriComponentsBuilder
            .fromHttpUrl(endpoint + ENRICH_ENTITY_ABOUT_OR_OWLSAMEAS).queryParam("uri", encodedUri);
    final URI fullUri = builder.build(true).toUri();
    try {
      return template.exchange(fullUri, HttpMethod.GET, createRequest(null), EnrichmentBase.class)
              .getBody();
    } catch (RestClientException e) {
      LOGGER.warn("Enrichment client GET call failed: {}.", fullUri, e);
      throw new UnknownException("Enrichment client call failed.", e);
    }
  }

  /**
   * Get enrichment information based on a specified list of URIs.
   *
   * @param uriList the list of URIs to enrich
   * @return the enriched information. Does not return null, but could return an empty list.
   */
  public List<EnrichmentBaseWrapper> getByUri(Collection<String> uriList) {
    return performInBatches(endpoint + ENRICH_ENTITY_ABOUT_OR_OWLSAMEAS, uriList);
  }

  /**
   * Get enrichment information based on a specified list of IDs
   *
   * @param uriList the list of IDs to enrich
   * @return the enriched information
   */
  public List<EnrichmentBaseWrapper> getById(Collection<String> uriList) {
    return performInBatches(endpoint + ENRICH_ENTITY_ABOUT, uriList);
  }

  private List<EnrichmentBaseWrapper> performInBatches(String url, Collection<String> inputValues) {

    // Create partitions
    final List<List<String>> partitions = new ArrayList<>();
    partitions.add(new ArrayList<>());
    inputValues.forEach(item -> {
      List<String> currentPartition = partitions.get(partitions.size() - 1);
      if (currentPartition.size() >= batchSize) {
        currentPartition = new ArrayList<>();
        partitions.add(currentPartition);
      }
      currentPartition.add(item);
    });

    // Process partitions
    final List<EnrichmentBaseWrapper> result = new ArrayList<>();
    partitions.stream().map(list -> list.toArray(String[]::new)).map(inputValue -> {
      try {
        return template.postForObject(url, createRequest(inputValue), EnrichmentResultList.class);
      } catch (RestClientException e) {
        LOGGER.warn("Enrichment client POST call failed: {}.", url, e);
        throw new UnknownException("Enrichment client call failed.", e);
      }
    }).filter(Objects::nonNull).map(EnrichmentResultList::getEnrichmentBaseWrapperList)
            .filter(Objects::nonNull).flatMap(List::stream).forEach(result::add);
    return result;
  }
}
