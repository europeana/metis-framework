package eu.europeana.enrichment.rest.client.enrichment;

import static eu.europeana.metis.utils.ExternalRequestUtil.retryableExternalRequestForNetworkExceptions;

import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentResultBaseWrapper;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.enrichment.rest.client.exceptions.EnrichmentException;
import eu.europeana.enrichment.utils.EnrichmentFields;
import eu.europeana.enrichment.utils.EnrichmentUtils;
import eu.europeana.enrichment.utils.EntityMergeEngine;
import eu.europeana.enrichment.utils.SearchValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnricherImpl implements Enricher {

  private static final Logger LOGGER = LoggerFactory.getLogger(EnricherImpl.class);

  private final EntityMergeEngine entityMergeEngine;
  private final EnrichmentClient enrichmentClient;

  public EnricherImpl(EntityMergeEngine entityMergeEngine,
      EnrichmentClient enrichmentClient) {
    this.entityMergeEngine = entityMergeEngine;
    this.enrichmentClient = enrichmentClient;
  }


  @Override
  public void enrichment(RDF rdf) throws EnrichmentException {
    // Extract values and references from the RDF for enrichment
    LOGGER.debug("Extracting values and references from RDF for enrichment...");
    final List<Pair<SearchValue, EnrichmentFields>> valuesForEnrichment = extractValuesForEnrichment(
        rdf);
    final Map<String, Set<EnrichmentFields>> referencesForEnrichment = extractReferencesForEnrichment(
        rdf);

    // Get the information with which to enrich the RDF using the extracted values and references
    LOGGER.debug("Using extracted values and references to gather enrichment information...");
    final List<Pair<EnrichmentResultBaseWrapper, EnrichmentFields>> valueEnrichmentInformation = enrichValues(
        valuesForEnrichment);
    final List<EnrichmentResultBaseWrapper> referenceEnrichmentInformation = enrichReferences(
        referencesForEnrichment.keySet());

    // Merge the acquired information into the RDF
    LOGGER.debug("Merging Enrichment Information...");
    if (valueEnrichmentInformation != null) {
      final Set<EnrichmentFields> enrichmentFieldsSet = valueEnrichmentInformation.stream()
          .map(Pair::getSecond).collect(Collectors.toSet());
      final List<EnrichmentResultBaseWrapper> enrichmentResultBaseWrapperList = valueEnrichmentInformation
          .stream().map(Pair::getFirst).collect(Collectors.toList());

      if (CollectionUtils.isNotEmpty(enrichmentResultBaseWrapperList)) {
        entityMergeEngine
            .mergeEntities(rdf, enrichmentResultBaseWrapperList, enrichmentFieldsSet);
      }
      if (!referenceEnrichmentInformation.isEmpty()) {
        final List<List<EnrichmentBase>> entities = referenceEnrichmentInformation.stream()
            .map(EnrichmentResultBaseWrapper::getEnrichmentBaseList).collect(Collectors.toList());
        entityMergeEngine.mergeEntities(rdf, entities, referencesForEnrichment);
      }
    }

    // Setting additional field values and set them in the RDF.
    LOGGER.debug("Setting additional data in the RDF...");
    EnrichmentUtils.setAdditionalData(rdf);

    // Done.
    LOGGER.debug("Enrichment completed.");

  }

  @Override
  //TODO: Input parameter is List<Pair<SearchValue, EnrichmentFields>
  //TODO: Output is List<Pair<EnrichmentResult, EnrichmentFields>>
  public List<Pair<EnrichmentResultBaseWrapper, EnrichmentFields>> enrichValues(
      List<Pair<SearchValue, EnrichmentFields>> valuesForEnrichment)
      throws EnrichmentException {
    try {
      List<SearchValue> searchValues = valuesForEnrichment.stream().map(Pair::getFirst)
          .collect(Collectors.toList());
      EnrichmentResultList result = retryableExternalRequestForNetworkExceptions(
          () -> enrichmentClient.enrich(searchValues));
      List<Pair<EnrichmentResultBaseWrapper, EnrichmentFields>> output = new ArrayList<>();
      for (int i = 0; i < valuesForEnrichment.size(); i++) {
        output.add(new Pair<>(result.getEnrichmentBaseResultWrapperList().get(i),
            valuesForEnrichment.get(i).getSecond()));
      }
      return output;
    } catch (Exception e) {
      throw new EnrichmentException(
          "Exception occurred while trying to perform enrichment.", e);
    }
  }

  @Override
  public List<EnrichmentResultBaseWrapper> enrichReferences(Set<String> referencesForEnrichment)
      throws EnrichmentException {
    try {
      return CollectionUtils.isEmpty(referencesForEnrichment) ? Collections.emptyList()
          : retryableExternalRequestForNetworkExceptions(
              () -> enrichmentClient.getByUri(referencesForEnrichment));
    } catch (Exception e) {
      throw new EnrichmentException(
          "Exception occurred while trying to perform enrichment.", e);
    }
  }

  @Override
  public List<Pair<SearchValue, EnrichmentFields>> extractValuesForEnrichment(RDF rdf) {
    return EnrichmentUtils.extractValuesForEnrichmentFromRDF(rdf);
  }

  @Override
  public Map<String, Set<EnrichmentFields>> extractReferencesForEnrichment(RDF rdf) {
    return EnrichmentUtils.extractReferencesForEnrichmentFromRDF(rdf);

  }
}
