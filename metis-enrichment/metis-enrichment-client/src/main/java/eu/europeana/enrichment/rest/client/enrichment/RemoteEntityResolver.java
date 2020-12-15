package eu.europeana.enrichment.rest.client.enrichment;

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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class RemoteEntityResolver implements EntityResolver {

  private final int batchSize;
  private final RestTemplate template;
  private final URL enrichmentServiceUrl;

  public RemoteEntityResolver(URL enrichmentServiceUrl, int batchSize, RestTemplate template) {
    this.enrichmentServiceUrl = enrichmentServiceUrl;
    this.template = template;
    this.batchSize = batchSize;
  }

  @Override
  public Map<SearchTerm, List<EnrichmentBase>> resolveByText(Set<? extends SearchTerm> searchTermSet) {
    final List<SearchTerm> searchTermList = List.copyOf(searchTermSet);
    final Function<List<SearchTerm>, EnrichmentSearch> inputFunction = partition -> {
      final List<SearchValue> searchValues = partition.stream()
          .map(uri -> new SearchValue(uri.getTextValue(), uri.getLanguage()))
          .collect(Collectors.toList());
      final EnrichmentSearch enrichmentSearch = new EnrichmentSearch();
      enrichmentSearch.setSearchValues(searchValues);
      return enrichmentSearch;
    };
    final List<EnrichmentResultBaseWrapper> enrichmentResultBaseWrapperList = performInBatches(
        ENRICH_ENTITY_SEARCH, searchTermList, inputFunction);
    final Map<SearchTerm, List<EnrichmentBase>> result = new HashMap<>();
    for (int i = 0; i < enrichmentResultBaseWrapperList.size(); i++) {
      result.put(searchTermList.get(i),
          enrichmentResultBaseWrapperList.get(i).getEnrichmentBaseList());
    }
    return result;
  }

  @Override
  public Map<ReferenceTerm, EnrichmentBase> resolveById(Set<? extends ReferenceTerm> referenceTermSet) {
    final List<ReferenceTerm> referenceTermList = List.copyOf(referenceTermSet);
    final List<EnrichmentResultBaseWrapper> enrichmentResultBaseWrapperList = performInBatches(
            ENRICH_ENTITY_ID, referenceTermList,
            partition -> partition.stream().map(ReferenceTerm::getReference).map(URL::toString)
                    .toArray(String[]::new));
    final Map<ReferenceTerm, EnrichmentBase> results = new HashMap<>();
    for (int i = 0; i < referenceTermList.size(); i++) {
      final EnrichmentBase result = enrichmentResultBaseWrapperList.get(i).getEnrichmentBaseList()
              .stream().findFirst().orElse(null);
      if (result != null) {
        results.put(referenceTermList.get(i), result);
      }
    }
    return results;
  }

  @Override
  public Map<ReferenceTerm, List<EnrichmentBase>> resolveByUri(
      Set<? extends ReferenceTerm> referenceTermSet) {
    final List<ReferenceTerm> referenceTermList = List.copyOf(referenceTermSet);
    final Function<List<ReferenceTerm>, EnrichmentReference> inputFunction = partition -> {
      final List<ReferenceValue> referenceValues = partition.stream()
              .map(ReferenceTerm::getReference).map(URL::toString)
              .map(uri -> new ReferenceValue(uri, Collections.emptySet()))
              .collect(Collectors.toList());
      final EnrichmentReference enrichmentReference = new EnrichmentReference();
      enrichmentReference.setReferenceValues(referenceValues);
      return enrichmentReference;
    };
    final List<EnrichmentResultBaseWrapper> enrichmentResultBaseWrapperList = performInBatches(
        ENRICH_ENTITY_EQUIVALENCE, referenceTermList, inputFunction);
    final Map<ReferenceTerm, List<EnrichmentBase>> result = new HashMap<>();
    for (int i = 0; i < referenceTermList.size(); i++) {
      result.put(referenceTermList.get(i),
          enrichmentResultBaseWrapperList.get(i).getEnrichmentBaseList());
    }
    return result;
  }

  private <I, B> List<EnrichmentResultBaseWrapper> performInBatches(String endpointPath,
      Collection<I> inputValues, Function<List<I>, B> bodyCreator) {
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
    final List<EnrichmentResultBaseWrapper> result = new ArrayList<>();
    for (List<I> partition : partitions) {
      final B body = bodyCreator.apply(partition);
      final HttpEntity<Object> httpEntity = createRequest(body);
      final EnrichmentResultList enrichmentResultList;
      try {
        enrichmentResultList = template.postForObject(uri, httpEntity, EnrichmentResultList.class);
      } catch (RestClientException e) {
        throw new UnknownException("Enrichment client POST call failed: " + uri + ".", e);
      }
      if (enrichmentResultList == null) {
        throw new UnknownException("Empty body from server (" + uri + ").");
      }
      if (enrichmentResultList.getEnrichmentBaseResultWrapperList().size() != partition.size()) {
        throw new UnknownException("Server returned unexpected number of results (" + uri + ").");
      }
      result.addAll(enrichmentResultList.getEnrichmentBaseResultWrapperList());
    }
    return result;
  }

  private static <B> HttpEntity<B> createRequest(B body) {
    final HttpHeaders headers = new HttpHeaders();
    if (body != null) {
      headers.setContentType(MediaType.APPLICATION_JSON);
    }
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_XML));
    return new HttpEntity<>(body, headers);
  }
}