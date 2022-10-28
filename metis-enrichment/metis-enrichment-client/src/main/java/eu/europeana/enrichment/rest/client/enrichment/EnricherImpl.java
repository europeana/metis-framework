package eu.europeana.enrichment.rest.client.enrichment;

import static eu.europeana.enrichment.api.internal.EntityResolver.europeanaLinkPattern;
import static eu.europeana.enrichment.api.internal.EntityResolver.semiumLinkPattern;

import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.internal.EntityResolver;
import eu.europeana.enrichment.api.internal.ProxyFieldType;
import eu.europeana.enrichment.api.internal.RecordParser;
import eu.europeana.enrichment.api.internal.ReferenceTermContext;
import eu.europeana.enrichment.api.internal.SearchTermContext;
import eu.europeana.enrichment.rest.client.EnrichmentWorker.Mode;
import eu.europeana.enrichment.rest.client.exceptions.EnrichmentException;
import eu.europeana.enrichment.rest.client.report.ReportMessage;
import eu.europeana.enrichment.rest.client.report.ReportMessage.ReportMessageBuilder;
import eu.europeana.enrichment.rest.client.report.Type;
import eu.europeana.enrichment.utils.EnrichmentUtils;
import eu.europeana.enrichment.utils.EntityMergeEngine;
import eu.europeana.enrichment.utils.RdfEntityUtils;
import eu.europeana.metis.schema.jibx.ProxyType;
import eu.europeana.metis.schema.jibx.RDF;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default implementation for the enrichment functionality.
 */
public class EnricherImpl implements Enricher {

  private static final Logger LOGGER = LoggerFactory.getLogger(EnricherImpl.class);
  private final RecordParser recordParser;
  private final EntityResolver entityResolver;
  private final EntityMergeEngine entityMergeEngine;

  /**
   * Constructor with required parameters.
   *
   * @param recordParser the record parser
   * @param entityResolver the entity resolver
   * @param entityMergeEngine the entity merge engine
   */
  public EnricherImpl(RecordParser recordParser, EntityResolver entityResolver,
      EntityMergeEngine entityMergeEngine) {
    this.recordParser = recordParser;
    this.entityResolver = entityResolver;
    this.entityMergeEngine = entityMergeEngine;
  }

  @Override
  public HashSet<ReportMessage> enrichment(RDF rdf) throws EnrichmentException {
    HashSet<ReportMessage> reportMessages = new HashSet<>();
    // Extract values and references from the RDF for enrichment
    LOGGER.debug("Extracting values and references from RDF for enrichment...");
    final Set<SearchTermContext> searchTerms = recordParser.parseSearchTerms(rdf);
    final Set<ReferenceTermContext> references = recordParser.parseReferences(rdf);

    // Get the information with which to enrich the RDF using the extracted values and references
    LOGGER.debug("Using extracted values and references to gather enrichment information...");
    final Map<SearchTermContext, List<EnrichmentBase>> enrichedValues = enrichValues(searchTerms);
    final Map<ReferenceTermContext, List<EnrichmentBase>> enrichedReferences = enrichReferences(
        references);

    reportMessages.addAll(getSearchTermsReport(searchTerms, enrichedValues));
    reportMessages.addAll(getSearchReferenceReport(references, enrichedReferences));
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
    return reportMessages;
  }

  private HashSet<ReportMessage> getSearchTermsReport(Set<SearchTermContext> searchTerms,
      Map<SearchTermContext, List<EnrichmentBase>> enrichedValues) {
    HashSet<ReportMessage> reportMessages = new HashSet<>();
    for (SearchTermContext searchTerm : searchTerms) {
      if (enrichedValues.get(searchTerm).isEmpty()) {
        reportMessages.add(new ReportMessageBuilder()
            .withMessage("Enrichment: could not find an entity for the given search term \"" + searchTerm.getTextValue() + "\"")
            .withMode(Mode.ENRICHMENT)
            .withStatus(200)
            .withMessageType(Type.IGNORE)
            .build());
      }
    }
    return reportMessages;
  }

  private HashSet<ReportMessage> getSearchReferenceReport(Set<ReferenceTermContext> references,
      Map<ReferenceTermContext, List<EnrichmentBase>> enrichedReferences) {
    HashSet<ReportMessage> reportMessages = new HashSet<>();
    for (ReferenceTermContext reference : references) {
      if (enrichedReferences.get(reference).isEmpty()) {
        reportMessages.add(new ReportMessageBuilder()
            .withMessage(
                "Enrichment: could not find an entity for the given search reference \"" + reference.getReference() + "\"")
            .withMode(Mode.ENRICHMENT)
            .withStatus(200)
            .withMessageType(Type.IGNORE)
            .build());
      }
    }
    return reportMessages;
  }

  @Override
  public Map<SearchTermContext, List<EnrichmentBase>> enrichValues(
      Set<SearchTermContext> searchTerms) throws EnrichmentException {
    if (CollectionUtils.isEmpty(searchTerms)) {
      return Collections.emptyMap();
    }
    // TODO: handle this exceptions inside the report and return EnrichedRecord as null
    try {
      return entityResolver.resolveByText(Set.copyOf(searchTerms));
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
    // TODO: handle this exceptions inside the report and return EnrichedRecord as null
    try {
      return entityResolver.resolveByUri(references);

    } catch (RuntimeException e) {
      throw new EnrichmentException("Exception occurred while trying to perform enrichment.", e);
    }
  }

  @Override
  public void cleanupPreviousEnrichmentEntities(RDF rdf) {
    final ProxyType europeanaProxy = RdfEntityUtils.getEuropeanaProxy(rdf);
    //Find the correct links
    final Set<String> matchingLinks = Arrays.stream(ProxyFieldType.values())
                                            .map(proxyFieldType -> proxyFieldType.extractFieldLinksForEnrichment(europeanaProxy))
                                            .flatMap(Collection::stream)
                                            .filter(europeanaLinkPattern.asPredicate().or(semiumLinkPattern.asPredicate()))
                                            .collect(Collectors.toSet());
    RdfEntityUtils.removeMatchingEntities(rdf, matchingLinks);
  }
}
