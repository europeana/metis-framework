package eu.europeana.enrichment.rest.client.enrichment;

import static eu.europeana.metis.network.ExternalRequestUtil.retryableExternalRequestForNetworkExceptions;

import eu.europeana.enrichment.api.external.SearchValue;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.enrichment.rest.client.exceptions.EnrichmentException;
import eu.europeana.enrichment.utils.FieldType;
import eu.europeana.enrichment.utils.EnrichmentUtils;
import eu.europeana.enrichment.utils.EntityMergeEngine;
import eu.europeana.metis.schema.jibx.RDF;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnricherImpl implements Enricher {

  private static final Logger LOGGER = LoggerFactory.getLogger(EnricherImpl.class);

  private final EntityMergeEngine entityMergeEngine;
  private final EnrichmentClient enrichmentClient;

  public EnricherImpl(EntityMergeEngine entityMergeEngine, EnrichmentClient enrichmentClient) {
    this.entityMergeEngine = entityMergeEngine;
    this.enrichmentClient = enrichmentClient;
  }


  @Override
  public void enrichment(RDF rdf) throws EnrichmentException {
    // Extract values and references from the RDF for enrichment
    LOGGER.debug("Extracting values and references from RDF for enrichment...");
    final List<Pair<SearchValue, FieldType>> valuesForEnrichment = extractValuesForEnrichment(
        rdf);
    final Map<String, Set<FieldType>> referencesForEnrichment = extractReferencesForEnrichment(
        rdf);

    // Get the information with which to enrich the RDF using the extracted values and references
    LOGGER.debug("Using extracted values and references to gather enrichment information...");
    final List<Pair<EnrichmentBase, FieldType>> valueEnrichmentInformation = enrichValues(
        valuesForEnrichment);
    final List<EnrichmentBase> referenceEnrichmentInformation = enrichReferences(
        referencesForEnrichment.keySet());

    // Merge the acquired information into the RDF
    LOGGER.debug("Merging Enrichment Information...");
    if (valueEnrichmentInformation != null) {
      for (Pair<EnrichmentBase, FieldType> pair : valueEnrichmentInformation) {
        entityMergeEngine.mergeEntities(rdf, List.of(pair.getKey()), Set.of(pair.getValue()));

      }
    }

    if (!referenceEnrichmentInformation.isEmpty()) {
      entityMergeEngine.mergeEntities(rdf, referenceEnrichmentInformation, referencesForEnrichment);
    }

    // Setting additional field values and set them in the RDF.
    LOGGER.debug("Setting additional data in the RDF...");
    EnrichmentUtils.setAdditionalData(rdf);

    // Done.
    LOGGER.debug("Enrichment completed.");

  }

  @Override
  public List<Pair<EnrichmentBase, FieldType>> enrichValues(
      List<Pair<SearchValue, FieldType>> valuesForEnrichment) throws EnrichmentException {
    try {
      if (CollectionUtils.isEmpty(valuesForEnrichment)) {
        return Collections.emptyList();
      }
      List<SearchValue> searchValues = valuesForEnrichment.stream().map(Pair::getKey)
          .collect(Collectors.toList());
      EnrichmentResultList result = retryableExternalRequestForNetworkExceptions(
          () -> enrichmentClient.enrich(searchValues));
      final List<Pair<EnrichmentBase, FieldType>> output = new ArrayList<>();
      for (int index = 0; index < searchValues.size(); index++) {
        final FieldType fieldType = valuesForEnrichment.get(index).getValue();
        final List<EnrichmentBase> enrichmentBaseList = result.getEnrichmentBaseResultWrapperList()
            .get(index).getEnrichmentBaseList();
        Optional.ofNullable(enrichmentBaseList).stream().flatMap(Collection::stream)
            .forEach(base -> output.add(new MutablePair<>(base, fieldType)));
      }
      return output;

    } catch (Exception e) {
      throw new EnrichmentException("Exception occurred while trying to perform enrichment.", e);
    }
  }

  @Override
  public List<EnrichmentBase> enrichReferences(Set<String> referencesForEnrichment)
      throws EnrichmentException {
    try {
      return CollectionUtils.isEmpty(referencesForEnrichment) ? Collections.emptyList()
          : retryableExternalRequestForNetworkExceptions(
              () -> enrichmentClient.getByUri(referencesForEnrichment));
    } catch (Exception e) {
      throw new EnrichmentException("Exception occurred while trying to perform enrichment.", e);
    }
  }

  @Override
  public List<Pair<SearchValue, FieldType>> extractValuesForEnrichment(RDF rdf) {
    return EnrichmentUtils.extractValuesForEnrichmentFromRDF(rdf);
  }

  @Override
  public Map<String, Set<FieldType>> extractReferencesForEnrichment(RDF rdf) {
    return EnrichmentUtils.extractReferencesForEnrichmentFromRDF(rdf);

  }
}
