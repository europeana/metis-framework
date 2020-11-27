package eu.europeana.enrichment;

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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;


public class RemoteEntityResolver implements EntityResolver {

  private final int batchSize;
  private RestTemplate template;
  private static final Logger LOGGER = LoggerFactory.getLogger(RemoteEntityResolver.class);
  private final URL enrichmentServiceUrl;

  public RemoteEntityResolver(URL enrichmentServiceUrl, int batchSize, RestTemplate template) {
    this.enrichmentServiceUrl = enrichmentServiceUrl;
    this.template = template;
    this.batchSize = batchSize;
  }

  @Override
  public Map<SearchTerm, List<EnrichmentBase>> resolveByText(Set<SearchTerm> searchTermSet) {
    Map<SearchTerm, List<EnrichmentBase>> result = new HashMap<>();

    List<SearchTerm> searchTermList = List.copyOf(searchTermSet);

    List<EnrichmentResultBaseWrapper> enrichmentResultBaseWrapperList = performInBatches(
        ENRICH_ENTITY_SEARCH, searchTermList,
        partition -> {
          List<SearchValue> referenceValues = partition.stream()
              .map(uri -> new SearchValue(uri.getTextValue(), uri.getLanguage().toString()))
              .collect(Collectors.toList());
          final EnrichmentSearch enrichmentReference = new EnrichmentSearch();
          enrichmentReference.setSearchValues(referenceValues);
          return enrichmentReference;
        }, MediaType.APPLICATION_XML);

    for (int i = 0; i < searchTermList.size(); i++) {
      SearchTerm searchTerm = new SearchTermContext(searchTermList.get(i).getTextValue(),
          searchTermList.get(i).getLanguage());
      result.put(searchTerm, enrichmentResultBaseWrapperList.get(i).getEnrichmentBaseList());
    }
    return result;
  }

  @Override
  public Map<ReferenceTerm, EnrichmentBase> resolveById(Set<ReferenceTerm> referenceTermSet) {
    Map<ReferenceTerm, EnrichmentBase> result = new HashMap<>();
    List<URL> urlList = referenceTermSet.stream().map(ReferenceTerm::getReference)
        .collect(Collectors.toList());
    List<EnrichmentResultBaseWrapper> enrichmentResultBaseWrapperList = performInBatches(
        ENRICH_ENTITY_ID, urlList.stream().map(URL::toString).collect(Collectors.toList()),
        partition -> partition.toArray(String[]::new), MediaType.APPLICATION_XML);
    List<EnrichmentBase> enrichmentBaseList = enrichmentResultBaseWrapperList.stream()
        .map(EnrichmentResultBaseWrapper::getEnrichmentBaseList).flatMap(List::stream)
        .collect(Collectors.toList());
    for (int i = 0; i < urlList.size(); i++) {
      ReferenceTerm referenceTerm = new ReferenceTermContext(urlList.get(i));
      result.put(referenceTerm, enrichmentBaseList.get(i));
    }
    return result;
  }

  @Override
  public Map<ReferenceTerm, List<EnrichmentBase>> resolveByUri(
      Set<ReferenceTerm> referenceTermSet) {
    Map<ReferenceTerm, List<EnrichmentBase>> result = new HashMap<>();
    List<URL> urlList = referenceTermSet.stream().map(ReferenceTerm::getReference)
        .collect(Collectors.toList());
    List<EnrichmentResultBaseWrapper> enrichmentResultBaseWrapperList = performInBatches(
        ENRICH_ENTITY_EQUIVALENCE,
        urlList.stream().map(URL::toString).collect(Collectors.toList()),
        partition -> {
          List<ReferenceValue> referenceValues = partition.stream()
              .map(uri -> new ReferenceValue(uri, Collections.emptySet()))
              .collect(Collectors.toList());
          final EnrichmentReference enrichmentReference = new EnrichmentReference();
          enrichmentReference.setReferenceValues(referenceValues);
          return enrichmentReference;
        },
        MediaType.APPLICATION_JSON);
    for (int i = 0; i < urlList.size(); i++) {
      ReferenceTerm referenceTerm = new ReferenceTermContext(urlList.get(i));
      result.put(referenceTerm, enrichmentResultBaseWrapperList.get(i).getEnrichmentBaseList());
    }
    return result;
  }

  private <I, B> List<EnrichmentResultBaseWrapper> performInBatches(String endpointPath,
      Collection<I> inputValues, Function<List<I>, B> bodyCreator, MediaType acceptanceHeader) {
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
      final HttpEntity<Object> httpEntity = createRequest(body, acceptanceHeader);
      final EnrichmentResultList enrichmentResultList;
      try {
        URI uri = enrichmentServiceUrl.toURI().resolve(endpointPath);
        enrichmentResultList = template
            .postForObject(uri, httpEntity, EnrichmentResultList.class);
      } catch (RestClientException | URISyntaxException e) {
        LOGGER.warn("Enrichment client POST call failed: {}.", enrichmentServiceUrl, e);
        throw new UnknownException("Enrichment client call failed.", e);
      }
      result.addAll(Optional.ofNullable(enrichmentResultList)
          .map(EnrichmentResultList::getEnrichmentBaseResultWrapperList).stream()
          .filter(Objects::nonNull).flatMap(Collection::stream)
          .collect(Collectors.toList()));
    }
    return result;
  }

  private static <B> HttpEntity<B> createRequest(B body, MediaType acceptType) {
    final HttpHeaders headers = new HttpHeaders();
    if (body != null) {
      headers.setContentType(MediaType.APPLICATION_JSON);
    }
    headers.setAccept(Collections.singletonList(acceptType));
    return new HttpEntity<>(body, headers);
  }
}