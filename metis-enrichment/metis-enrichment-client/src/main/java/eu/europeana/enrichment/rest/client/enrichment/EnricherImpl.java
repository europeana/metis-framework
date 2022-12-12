package eu.europeana.enrichment.rest.client.enrichment;

import static eu.europeana.enrichment.api.internal.EntityResolver.europeanaLinkPattern;
import static eu.europeana.enrichment.api.internal.EntityResolver.semiumLinkPattern;

import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.internal.AbstractSearchTerm;
import eu.europeana.enrichment.api.internal.EntityResolver;
import eu.europeana.enrichment.api.internal.ProxyFieldType;
import eu.europeana.enrichment.api.internal.RecordParser;
import eu.europeana.enrichment.api.internal.ReferenceTermContext;
import eu.europeana.enrichment.api.internal.SearchTermContext;
import eu.europeana.enrichment.rest.client.report.ReportMessage;
import eu.europeana.enrichment.rest.client.report.ReportMessageBuilder;
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
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

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
  public EnricherImpl(RecordParser recordParser, EntityResolver entityResolver, EntityMergeEngine entityMergeEngine) {
    this.recordParser = recordParser;
    this.entityResolver = entityResolver;
    this.entityMergeEngine = entityMergeEngine;
  }

  /**
   * Enrichment
   *
   * @param rdf The RDF to be processed
   * @return Set of a report containing messages during the enrichment processing.
   */
  @Override
  public Set<ReportMessage> enrichment(RDF rdf) {
    HashSet<ReportMessage> reportMessages = new HashSet<>();
    // Extract values and references from the RDF for enrichment
    LOGGER.debug("Extracting values and references from RDF for enrichment...");
    final Set<SearchTermContext> searchTerms = recordParser.parseSearchTerms(rdf);
    final Set<ReferenceTermContext> references = recordParser.parseReferences(rdf);

    // Get the information with which to enrich the RDF using the extracted values and references
    LOGGER.debug("Using extracted values and references to gather enrichment information...");
    final Pair<Map<SearchTermContext, List<EnrichmentBase>>, Set<ReportMessage>> enrichedValues = enrichValues(searchTerms);
    final Pair<Map<ReferenceTermContext, List<EnrichmentBase>>, Set<ReportMessage>> enrichedReferences = enrichReferences(
        references);

    //Add the report messages from the enrichment process
    reportMessages.addAll(enrichedValues.getRight());
    reportMessages.addAll(enrichedReferences.getRight());

    // Merge the acquired information into the RDF
    LOGGER.debug("Merging Enrichment Information...");
    if (enrichedValues.getLeft() != null) {
      for (Entry<SearchTermContext, List<EnrichmentBase>> entry : enrichedValues.getLeft().entrySet()) {
        entityMergeEngine.mergeSearchEntities(rdf, entry.getValue(), entry.getKey());
      }
    }
    if (enrichedReferences.getLeft() != null) {
      for (Entry<ReferenceTermContext, List<EnrichmentBase>> entry : enrichedReferences.getLeft().entrySet()) {
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
        reportMessages.add(ReportMessageBuilder
            .buildEnrichmentIgnore()
            .withMessage("Could not find an entity for the given search term.")
            .withValue(searchTerm.getTextValue())
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
        reportMessages.add(ReportMessageBuilder
            .buildEnrichmentIgnore()
            .withMessage("Could not find an entity for the given search reference.")
            .withValue(reference.getReferenceAsString())
            .build());
      }
    }
    return reportMessages;
  }

  @Override
  public Pair<Map<SearchTermContext, List<EnrichmentBase>>, Set<ReportMessage>> enrichValues(
      Set<SearchTermContext> searchTerms) {
    HashSet<ReportMessage> reportMessages = new HashSet<>();
    if (CollectionUtils.isEmpty(searchTerms)) {
      reportMessages.add(ReportMessageBuilder
          .buildEnrichmentIgnore()
          .withMessage("Empty search terms.")
          .withValue(searchTerms.toString())
          .build());
      return new ImmutablePair<>(Collections.emptyMap(), reportMessages);
    }
    try {
      Map<SearchTermContext, List<EnrichmentBase>> enrichedValues = entityResolver.resolveByText(Set.copyOf(searchTerms));
      return new ImmutablePair<>(enrichedValues, getSearchTermsReport(searchTerms, enrichedValues));
    } catch (RuntimeException e) {
      reportMessages.add(ReportMessageBuilder
          .buildEnrichmentError()
          .withValue(searchTerms.stream()
                                .map(AbstractSearchTerm::getTextValue)
                                .sorted(String::compareToIgnoreCase)
                                .collect(Collectors.joining(",")))
          .withException(e)
          .build());
      return new ImmutablePair<>(null, reportMessages);
    }
  }

  @Override
  public Pair<Map<ReferenceTermContext, List<EnrichmentBase>>, Set<ReportMessage>> enrichReferences(
      Set<ReferenceTermContext> references) {
    HashSet<ReportMessage> reportMessages = new HashSet<>();
    if (CollectionUtils.isEmpty(references)) {
      reportMessages.add(ReportMessageBuilder
          .buildEnrichmentIgnore()
          .withValue(references.toString())
          .withMessage("Empty search reference.")
          .build());
      return new ImmutablePair<>(Collections.emptyMap(), reportMessages);
    }
    try {
      Map<ReferenceTermContext, List<EnrichmentBase>> enrichedReferences = entityResolver.resolveByUri(references);
      return new ImmutablePair<>(enrichedReferences, getSearchReferenceReport(references, enrichedReferences));

    } catch (RuntimeException e) {
      Throwable rootCause = findRootCause(e);
      HttpStatus warningStatus = containsWarningStatus(rootCause.getMessage());
      String referenceValue = references.stream()
                                        .map(ReferenceTermContext::getReferenceAsString)
                                        .sorted(String::compareToIgnoreCase)
                                        .collect(Collectors.joining(","));
      if (warningStatus == null) {
        reportMessages.add(ReportMessageBuilder
            .buildEnrichmentError()
            .withValue(referenceValue)
            .withException(e)
            .build());
      } else {
        reportMessages.add(ReportMessageBuilder
            .buildEnrichmentWarn()
            .withStatus(warningStatus)
            .withValue(referenceValue)
            .withException(rootCause)
            .build());
      }
      return new ImmutablePair<>(null, reportMessages);
    }
  }

  private static HttpStatus containsWarningStatus(String message) {
    List<HttpStatus> warningStatuses = Arrays.stream(HttpStatus.values())
                                             .filter(httpStatus -> httpStatus.value() >= HttpStatus.MULTIPLE_CHOICES.value()
                                                 && httpStatus.value() < HttpStatus.INTERNAL_SERVER_ERROR.value())
                                             .collect(Collectors.toList());
    for (HttpStatus status : warningStatuses) {
      if (message.contains(status.toString())) {
        return status;
      }
    }
    return null;
  }

  private static Throwable findRootCause(Throwable throwable) {
    Objects.requireNonNull(throwable);
    Throwable rootCause = throwable;
    while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
      rootCause = rootCause.getCause();
    }
    return rootCause;
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
