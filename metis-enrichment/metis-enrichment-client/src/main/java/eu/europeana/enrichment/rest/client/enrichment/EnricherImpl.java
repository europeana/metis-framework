package eu.europeana.enrichment.rest.client.enrichment;

import static eu.europeana.metis.utils.ExternalRequestUtil.retryableExternalRequestForNetworkExceptions;

import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentBaseWrapper;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.enrichment.rest.client.exceptions.EnrichmentException;
import eu.europeana.enrichment.utils.EnrichmentFields;
import eu.europeana.enrichment.utils.EnrichmentUtils;
import eu.europeana.enrichment.utils.EntityMergeEngine;
import eu.europeana.enrichment.utils.InputValue;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnricherImpl implements Enricher{

  private static final Logger LOGGER = LoggerFactory.getLogger(Enricher.class);

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
    final List<InputValue> valuesForEnrichment = extractValuesForEnrichment(rdf);
    final Map<String, Set<EnrichmentFields>> referencesForEnrichment = extractReferencesForEnrichment(
        rdf);

    // Get the information with which to enrich the RDF using the extracted values and references
    LOGGER.debug("Using extracted values and references to gather enrichment information...");
    final EnrichmentResultList valueEnrichmentInformation = enrichValues(valuesForEnrichment);
    final List<EnrichmentBaseWrapper> referenceEnrichmentInformation = enrichReferences(
        referencesForEnrichment.keySet());

    // Merge the acquired information into the RDF
    LOGGER.debug("Merging Enrichment Information...");
    if (valueEnrichmentInformation != null && CollectionUtils
        .isNotEmpty(valueEnrichmentInformation.getEnrichmentBaseWrapperList())) {
      entityMergeEngine
          .mergeEntities(rdf, valueEnrichmentInformation.getEnrichmentBaseWrapperList());
    }
    if (!referenceEnrichmentInformation.isEmpty()) {
      final List<EnrichmentBase> entities = referenceEnrichmentInformation.stream()
          .map(EnrichmentBaseWrapper::getEnrichmentBase).collect(Collectors.toList());
      entityMergeEngine.mergeEntities(rdf, entities, referencesForEnrichment);
    }

    // Setting additional field values and set them in the RDF.
    LOGGER.debug("Setting additional data in the RDF...");
    EnrichmentUtils.setAdditionalData(rdf);

    // Done.
    LOGGER.debug("Enrichment completed.");

  }

  private EnrichmentResultList enrichValues(List<InputValue> valuesForEnrichment)
      throws EnrichmentException {
    try {
      return CollectionUtils.isEmpty(valuesForEnrichment) ? null
          : retryableExternalRequestForNetworkExceptions(
              () -> enrichmentClient.enrich(valuesForEnrichment));
    } catch (Exception e) {
      throw new EnrichmentException(
          "Exception occurred while trying to perform enrichment.", e);
    }
  }

  private List<EnrichmentBaseWrapper> enrichReferences(Set<String> referencesForEnrichment)
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
  public List<InputValue> extractValuesForEnrichment(RDF rdf) {
    return EnrichmentUtils.extractValuesForEnrichmentFromRDF(rdf);
  }

  @Override
  public Map<String, Set<EnrichmentFields>> extractReferencesForEnrichment(RDF rdf) {
    return EnrichmentUtils.extractReferencesForEnrichmentFromRDF(rdf);

  }
}
