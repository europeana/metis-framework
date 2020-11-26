package eu.europeana.enrichment;

import static eu.europeana.metis.utils.RestEndpoints.ENRICH_ENTITY_EQUIVALENCE;
import static eu.europeana.metis.utils.RestEndpoints.ENRICH_ENTITY_ID;

import eu.europeana.enrichment.api.exceptions.UnknownException;
import eu.europeana.enrichment.api.external.EnrichmentReference;
import eu.europeana.enrichment.api.external.ReferenceValue;
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
    return null;
  }

  @Override
  public Map<ReferenceTerm, EnrichmentBase> resolveById(Set<ReferenceTerm> referenceTermSet) {

    Map<ReferenceTerm, EnrichmentBase> result = new HashMap<>();

    List<URL> urlList = referenceTermSet.stream().map(ReferenceTerm::getReference)
        .collect(Collectors.toList());
    List<EnrichmentResultBaseWrapper> enrichmentResultBaseWrapperList = performInBatches(
        ENRICH_ENTITY_ID, urlList.stream().map(URL::toString).collect(Collectors.toList()),
        MediaType.APPLICATION_XML);

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
        MediaType.APPLICATION_JSON);

    for (int i = 0; i < urlList.size(); i++) {
      ReferenceTerm referenceTerm = new ReferenceTermContext(urlList.get(i));
      result.put(referenceTerm, enrichmentResultBaseWrapperList.get(i).getEnrichmentBaseList());
    }

    return result;

  }

  private List<EnrichmentResultBaseWrapper> performInBatches(String endpointPath,
      Collection<String> inputValues, MediaType acceptanceHeader) {

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
    final List<EnrichmentResultBaseWrapper> result = new ArrayList<>();

    for (List<String> partition : partitions) {
      final HttpEntity<Object> httpEntity;
      if (endpointPath.equals(ENRICH_ENTITY_ID)) {
        httpEntity = createRequest(partition.toArray(String[]::new), acceptanceHeader);
      } else {
        List<ReferenceValue> referenceValues = partition.stream()
            .map(uri -> new ReferenceValue(uri, Collections.emptySet()))
            .collect(Collectors.toList());
        final EnrichmentReference enrichmentReference = new EnrichmentReference();
        enrichmentReference.setReferenceValues(referenceValues);
        httpEntity = createRequest(enrichmentReference, acceptanceHeader);
      }
      final EnrichmentResultList enrichmentResultList;
      try {
        URI uri = enrichmentServiceUrl.toURI().resolve(endpointPath);
        System.out.println("URI: " + uri);
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

  private static <T> HttpEntity<T> createRequest(T body, MediaType acceptType) {
    final HttpHeaders headers = new HttpHeaders();
    if (body != null) {
      headers.setContentType(MediaType.APPLICATION_JSON);
    }
    headers.setAccept(Collections.singletonList(acceptType));
    return new HttpEntity<>(body, headers);
  }
}
