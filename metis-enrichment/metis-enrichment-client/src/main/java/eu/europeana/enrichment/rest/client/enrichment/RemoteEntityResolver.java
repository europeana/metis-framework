package eu.europeana.enrichment.rest.client.enrichment;

import static eu.europeana.metis.network.ExternalRequestUtil.retryableExternalRequestForNetworkExceptions;
import static eu.europeana.metis.utils.RestEndpoints.ENRICH_ENTITY_EQUIVALENCE;
import static eu.europeana.metis.utils.RestEndpoints.ENRICH_ENTITY_ID;
import static eu.europeana.metis.utils.RestEndpoints.ENRICH_ENTITY_SEARCH;

import eu.europeana.enrichment.api.exceptions.UnknownException;
import eu.europeana.enrichment.api.external.EnrichmentReference;
import eu.europeana.enrichment.api.external.EnrichmentSearch;
import eu.europeana.enrichment.api.external.ReferenceValue;
import eu.europeana.enrichment.api.external.SearchValue;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentResultBaseWrapper;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.enrichment.api.internal.EntityResolver;
import eu.europeana.enrichment.api.internal.ReferenceTerm;
import eu.europeana.enrichment.api.internal.SearchTerm;
import eu.europeana.enrichment.profile.TrackTime;
import eu.europeana.enrichment.utils.EntityType;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * An entity resolver that works by accessing a service through HTTP/REST and obtains entities from
 * there.
 */
public class RemoteEntityResolver implements EntityResolver {

  private final int batchSize;
  private final RestTemplate template;
  private final URL enrichmentServiceUrl;

  public RemoteEntityResolver(URL enrichmentServiceUrl, int batchSize, RestTemplate template) {
    this.enrichmentServiceUrl = enrichmentServiceUrl;
    this.template = template;
    this.batchSize = batchSize;
  }

  @TrackTime
  @Override
  public <T extends SearchTerm> Map<T, List<EnrichmentBase>> resolveByText(Set<T> searchTerms) {
    final Function<List<T>, EnrichmentSearch> inputFunction = partition -> {
      final List<SearchValue> searchValues = partition.stream()
              .map(term -> new SearchValue(term.getTextValue(), term.getLanguage(),
                      term.getCandidateTypes().toArray(EntityType[]::new)))
              .collect(Collectors.toList());
      final EnrichmentSearch enrichmentSearch = new EnrichmentSearch();
      enrichmentSearch.setSearchValues(searchValues);
      return enrichmentSearch;
    };
    return performInBatches(ENRICH_ENTITY_SEARCH, searchTerms, inputFunction, Function.identity());
  }

  @TrackTime
  @Override
  public <T extends ReferenceTerm> Map<T, EnrichmentBase> resolveById(Set<T> referenceTerms) {
    return performInBatches(ENRICH_ENTITY_ID, referenceTerms,
            partition -> partition.stream().map(ReferenceTerm::getReference).map(URL::toString)
                    .toArray(String[]::new),
            resultItem -> resultItem.stream().findFirst().orElse(null));
  }

  @TrackTime
  @Override
  public <T extends ReferenceTerm> Map<T, List<EnrichmentBase>> resolveByUri(
          Set<T> referenceTerms) {
    final Function<List<T>, EnrichmentReference> inputFunction = partition -> {
      final List<ReferenceValue> referenceValues = partition.stream()
              .map(ReferenceTerm::getReference).map(URL::toString)
              .map(uri -> new ReferenceValue(uri, Collections.emptySet()))
              .collect(Collectors.toList());
      final EnrichmentReference enrichmentReference = new EnrichmentReference();
      enrichmentReference.setReferenceValues(referenceValues);
      return enrichmentReference;
    };
    return performInBatches(ENRICH_ENTITY_EQUIVALENCE, referenceTerms, inputFunction,
            Function.identity());
  }

  private <I, B, R> Map<I, R> performInBatches(String endpointPath, Set<I> inputValues,
          Function<List<I>, B> bodyCreator, Function<List<EnrichmentBase>, R> resultParser) {

    // Determine the URI
    final URI uri;
    try {
      final URI parentUri = enrichmentServiceUrl.toURI();
      uri = new URI(parentUri.getScheme(), parentUri.getUserInfo(), parentUri.getHost(),
              parentUri.getPort(), parentUri.getPath() + "/" + endpointPath,
              parentUri.getQuery(), parentUri.getFragment()).normalize();
    } catch (URISyntaxException e) {
      throw new UnknownException(
              "URL syntax issue with service url: " + enrichmentServiceUrl + ".", e);
    }

    // Create partitions
    final List<List<I>> partitions = new ArrayList<>();
    partitions.add(new ArrayList<>());
    inputValues.forEach(item -> {
      List<I> currentPartition = partitions.get(partitions.size() - 1);
      if (currentPartition.size() >= batchSize) {
        currentPartition = new ArrayList<>();
        partitions.add(currentPartition);
      }
      currentPartition.add(item);
    });

    // Process partitions
    final Map<I, R> result = new HashMap<>();
    for (List<I> partition : partitions) {
      final EnrichmentResultList enrichmentResultList = executeRequest(uri, bodyCreator, partition);
      for (int i = 0; i < partition.size(); i++) {
        final I inputItem = partition.get(i);
        Optional.ofNullable(enrichmentResultList
                .getEnrichmentBaseResultWrapperList().get(i))
                .map(EnrichmentResultBaseWrapper::getEnrichmentBaseList)
                .filter(list -> !list.isEmpty())
                .map(resultParser).ifPresent(resultItem -> result.put(inputItem, resultItem));
      }
    }

    // Done.
    return result;
  }

  private <I, B> EnrichmentResultList executeRequest(URI uri, Function<List<I>, B> bodyCreator,
          List<I> partition) {

    // Create the request
    final B body = bodyCreator.apply(partition);
    final HttpHeaders headers = new HttpHeaders();
    if (body != null) {
      headers.setContentType(MediaType.APPLICATION_JSON);
    }
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_XML));
    final HttpEntity<B> httpEntity = new HttpEntity<>(body, headers);

    // Send the request.
    final EnrichmentResultList enrichmentResultList;
    try {
       enrichmentResultList = retryableExternalRequestForNetworkExceptions(
                () -> template.postForObject(uri, httpEntity, EnrichmentResultList.class));

    } catch (RestClientException e) {
      throw new UnknownException("Enrichment client POST call failed: " + uri + ".", e);
    }
    if (enrichmentResultList == null) {
      throw new UnknownException("Empty body from server (" + uri + ").");
    }
    if (enrichmentResultList.getEnrichmentBaseResultWrapperList().size() != partition.size()) {
      throw new UnknownException("Server returned unexpected number of results (" + uri + ").");
    }

    // Done
    return enrichmentResultList;
  }
}
