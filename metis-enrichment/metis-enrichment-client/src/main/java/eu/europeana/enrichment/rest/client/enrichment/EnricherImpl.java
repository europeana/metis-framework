package eu.europeana.enrichment.rest.client.enrichment;

import static eu.europeana.metis.network.ExternalRequestUtil.retryableExternalRequestForNetworkExceptions;

import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.internal.EntityResolver;
import eu.europeana.enrichment.api.internal.ProxyFieldType;
import eu.europeana.enrichment.api.internal.RecordParser;
import eu.europeana.enrichment.api.internal.ReferenceTermContext;
import eu.europeana.enrichment.api.internal.SearchTermContext;
import eu.europeana.enrichment.rest.client.exceptions.EnrichmentException;
import eu.europeana.enrichment.utils.EnrichmentUtils;
import eu.europeana.enrichment.utils.EntityMergeEngine;
import eu.europeana.enrichment.utils.RdfEntityUtils;
import eu.europeana.metis.schema.jibx.ProxyType;
import eu.europeana.metis.schema.jibx.RDF;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default implementation for the enrichment functionality.
 */
public class EnricherImpl implements Enricher {

  private static final Logger LOGGER = LoggerFactory.getLogger(EnricherImpl.class);
  private static final Pattern europeanaLinkPattern = Pattern
      .compile("^https?://data.europeana.eu.*$");

  private final RecordParser recordParser;
  private final EntityResolver entityResolver;
  private final EntityMergeEngine entityMergeEngine;

  public EnricherImpl(RecordParser recordParser, EntityResolver entityResolver,
      EntityMergeEngine entityMergeEngine) {
    this.recordParser = recordParser;
    this.entityResolver = entityResolver;
    this.entityMergeEngine = entityMergeEngine;
  }

  @Override
  public void enrichment(RDF rdf) throws EnrichmentException {

    // Extract values and references from the RDF for enrichment
    LOGGER.debug("Extracting values and references from RDF for enrichment...");
    final Set<SearchTermContext> searchTerms = recordParser.parseSearchTerms(rdf);
    final Set<ReferenceTermContext> references = recordParser.parseReferences(rdf);

    // Get the information with which to enrich the RDF using the extracted values and references
    LOGGER.debug("Using extracted values and references to gather enrichment information...");
    final Map<SearchTermContext, List<EnrichmentBase>> enrichedValues = enrichValues(searchTerms);
    final Map<ReferenceTermContext, List<EnrichmentBase>> enrichedReferences = enrichReferences(
        references);

    // Merge the acquired information into the RDF
    LOGGER.debug("Merging Enrichment Information...");
    if (enrichedValues != null) {
      for (Entry<SearchTermContext, List<EnrichmentBase>> entry : enrichedValues.entrySet()) {
        entityMergeEngine.mergeSearchEntities(rdf, entry.getValue(), entry.getKey());
      }
    }
    if (enrichedReferences != null) {
      for (Entry<ReferenceTermContext, List<EnrichmentBase>> entry : enrichedReferences
          .entrySet()) {
        entityMergeEngine.mergeReferenceEntities(rdf, entry.getValue(), entry.getKey());
      }
    }

    // Setting additional field values and set them in the RDF.
    LOGGER.debug("Setting additional data in the RDF...");
    EnrichmentUtils.setAdditionalData(rdf);

    // Done.
    LOGGER.debug("Enrichment completed.");
  }

  @Override
  public Map<SearchTermContext, List<EnrichmentBase>> enrichValues(
      Set<SearchTermContext> searchTerms) throws EnrichmentException {
    if (CollectionUtils.isEmpty(searchTerms)) {
      return Collections.emptyMap();
    }
    try {
      return retryableExternalRequestForNetworkExceptions(
          () -> entityResolver.resolveByText(Set.copyOf(searchTerms)));
    } catch (RuntimeException e) {
      throw new EnrichmentException("Exception occurred while trying to perform enrichment.", e);
    }
  }

  @Override
  public Map<ReferenceTermContext, List<EnrichmentBase>> enrichReferences(
      Set<ReferenceTermContext> references) throws EnrichmentException {
    if (CollectionUtils.isEmpty(references)) {
      return Collections.emptyMap();
    }
    try {
      return retryableExternalRequestForNetworkExceptions(
          () -> entityResolver.resolveByUri(references));
    } catch (RuntimeException e) {
      throw new EnrichmentException("Exception occurred while trying to perform enrichment.", e);
    }
  }

  @Override
  public void cleanupPreviousEnrichmentEntities(RDF rdf) {
    final ProxyType europeanaProxy = RdfEntityUtils.getEuropeanaProxy(rdf);
    //Find the correct links
    final Set<String> europeanaLinks = Arrays.stream(ProxyFieldType.values())
        .map(proxyFieldType -> proxyFieldType.extractFieldLinksForEnrichment(europeanaProxy))
        .flatMap(Collection::stream).filter(europeanaLinkPattern.asPredicate())
        .collect(Collectors.toSet());
    RdfEntityUtils.removeMatchingEntities(rdf, europeanaLinks);
  }
}
