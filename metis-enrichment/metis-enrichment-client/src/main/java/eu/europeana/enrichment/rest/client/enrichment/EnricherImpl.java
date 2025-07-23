package eu.europeana.enrichment.rest.client.enrichment;

import static eu.europeana.enrichment.api.internal.EntityResolver.europeanaLinkPattern;
import static eu.europeana.enrichment.api.internal.EntityResolver.semiumLinkPattern;

import eu.europeana.enrichment.api.external.impl.ClientEntityResolver;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.internal.AbstractSearchTerm;
import eu.europeana.enrichment.api.internal.EntityResolver;
import eu.europeana.enrichment.api.internal.FieldType;
import eu.europeana.enrichment.api.internal.ProxyFieldType;
import eu.europeana.enrichment.api.internal.RecordParser;
import eu.europeana.enrichment.api.internal.ReferenceTermContext;
import eu.europeana.enrichment.api.internal.SearchTermContext;
import eu.europeana.enrichment.rest.client.report.Report;
import eu.europeana.enrichment.utils.EnrichmentUtils;
import eu.europeana.enrichment.utils.EntityMergeEngine;
import eu.europeana.enrichment.utils.EntityType;
import eu.europeana.enrichment.utils.RdfEntityUtils;
import eu.europeana.entity.client.EntityApiClient;
import eu.europeana.entity.client.config.EntityClientConfiguration;
import eu.europeana.entity.client.exception.EntityClientException;
import eu.europeana.metis.schema.jibx.AboutType;
import eu.europeana.metis.schema.jibx.ProxyType;
import eu.europeana.metis.schema.jibx.RDF;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
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
  private static final List<HttpStatus> WARNING_STATUSES = Arrays.stream(HttpStatus.values())
                                                                 .filter(httpStatus ->
                                                                     httpStatus.value() >= HttpStatus.MULTIPLE_CHOICES.value()
                                                                         && httpStatus.value()
                                                                         < HttpStatus.INTERNAL_SERVER_ERROR.value())
                                                                 .toList();
  private final RecordParser recordParser;
  private final EntityResolver entityResolver;
  private final EntityClientConfiguration entityApiClientConfiguration;
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
    this.entityApiClientConfiguration = null;
    this.entityMergeEngine = entityMergeEngine;
  }

  /**
   * Constructor with required parameters.
   *
   * @param recordParser the record parser
   * @param entityApiClientConfiguration the configuration to create entity resolvers
   * @param entityMergeEngine the entity merge engine
   */
  public EnricherImpl(RecordParser recordParser, EntityClientConfiguration entityApiClientConfiguration, EntityMergeEngine entityMergeEngine) {
    this.recordParser = recordParser;
    this.entityResolver = null;
    this.entityApiClientConfiguration = entityApiClientConfiguration;
    this.entityMergeEngine = entityMergeEngine;
  }

  private static HttpStatus containsWarningStatus(String message) {
    for (HttpStatus status : WARNING_STATUSES) {
      if (message != null && message.contains(status.toString())) {
        return status;
      }
    }
    return null;
  }

  /**
   * Enrichment
   *
   * @param rdf The RDF to be processed
   * @return Set of a report containing messages during the enrichment processing.
   */
  @Override
  public Set<Report> enrichment(RDF rdf) {
    final HashSet<Report> reports = new HashSet<>();

    // Extract values and references from the RDF for enrichment
    LOGGER.debug("Extracting values and references from RDF for enrichment...");
    final Set<SearchTermContext> searchTerms = recordParser.parseSearchTerms(rdf);
    final Set<ReferenceTermContext> references = recordParser.parseReferences(rdf);

    // Get the information with which to enrich the RDF using the extracted values and references
    LOGGER.debug("Using extracted values and references to gather enrichment information...");
    final Pair<Map<SearchTermContext, List<EnrichmentBase>>, Set<Report>> enrichedValues = enrichValues(searchTerms);
    final Pair<Map<ReferenceTermContext, List<EnrichmentBase>>, Set<Report>> enrichedReferences = enrichReferences(
        references);

    // Add the report messages from the enrichment process
    reports.addAll(enrichedValues.getRight());
    reports.addAll(enrichedReferences.getRight());

    // Merge the acquired information into the RDF
    LOGGER.debug("Merging Enrichment Information...");
    if (enrichedValues.getLeft() != null) {
      for (Entry<SearchTermContext, List<EnrichmentBase>> entry : enrichedValues.getLeft().entrySet()) {
        entityMergeEngine.mergeEntities(rdf, entry.getValue(), entry.getKey());
      }
    }
    if (enrichedReferences.getLeft() != null) {
      for (Entry<ReferenceTermContext, List<EnrichmentBase>> entry : enrichedReferences.getLeft().entrySet()) {
        entityMergeEngine.mergeEntities(rdf, entry.getValue(), entry.getKey());
      }
    }

    // Setting additional field values and set them in the RDF.
    LOGGER.debug("Setting additional data in the RDF...");
    EnrichmentUtils.setAdditionalData(rdf);

    // Done.
    LOGGER.debug("Enrichment completed.");
    return reports;
  }

  private EntityResolver getEntityResolver() {
    return Optional.ofNullable(this.entityResolver).orElseGet(()-> {
      try {
        return new ClientEntityResolver(new EntityApiClient(this.entityApiClientConfiguration));
      } catch (EntityClientException e) {
        throw new RuntimeException(e);
      }
    });
  }

  @Override
  public Pair<Map<SearchTermContext, List<EnrichmentBase>>, Set<Report>> enrichValues(
      Set<SearchTermContext> searchTerms) {
    return enrichValues(searchTerms, getEntityResolver());
  }

  private Pair<Map<SearchTermContext, List<EnrichmentBase>>, Set<Report>> enrichValues(
      Set<SearchTermContext> searchTerms, EntityResolver entityResolverToUse) {
    HashSet<Report> reports = new HashSet<>();
    if (CollectionUtils.isEmpty(searchTerms)) {
      reports.add(Report
          .buildEnrichmentIgnore()
          .withMessage("Empty search terms.")
          .withValue(searchTerms.toString())
          .build());
      return new ImmutablePair<>(Collections.emptyMap(), reports);
    }
    try {
      Map<SearchTermContext, List<EnrichmentBase>> enrichedValues = entityResolverToUse.resolveByText(Set.copyOf(searchTerms));
      return new ImmutablePair<>(enrichedValues, getSearchTermsReport(searchTerms, enrichedValues));
    } catch (RuntimeException runtimeException) {
      reports.add(Report
          .buildEnrichmentError()
          .withMessage("Error while resolving values by text when enriching values")
          .withValue(searchTerms.stream()
                                .map(AbstractSearchTerm::getTextValue)
                                .sorted(String::compareToIgnoreCase)
                                .collect(Collectors.joining(",")))
          .withException(runtimeException)
          .build());
      return new ImmutablePair<>(null, reports);
    }
  }

  @Override
  public Pair<Map<ReferenceTermContext, List<EnrichmentBase>>, Set<Report>> enrichReferences(
      Set<ReferenceTermContext> references) {
    return this.enrichReferences(references, getEntityResolver());
  }

  private Pair<Map<ReferenceTermContext, List<EnrichmentBase>>, Set<Report>> enrichReferences(
      Set<ReferenceTermContext> references, EntityResolver entityResolverToUse) {
    HashSet<Report> reports = new HashSet<>();
    if (CollectionUtils.isEmpty(references)) {
      reports.add(Report
          .buildEnrichmentIgnore()
          .withValue(references.toString())
          .withMessage("Empty search reference.")
          .build());
      return new ImmutablePair<>(Collections.emptyMap(), reports);
    }
    try {
      Map<ReferenceTermContext, List<EnrichmentBase>> enrichedReferences = entityResolverToUse.resolveByUri(references);
      return new ImmutablePair<>(enrichedReferences, getSearchReferenceReport(references, enrichedReferences));

    } catch (RuntimeException runtimeException) {
      String referenceValue = references.stream()
                                        .map(ReferenceTermContext::getReferenceAsString)
                                        .sorted(String::compareToIgnoreCase)
                                        .collect(Collectors.joining(","));
      reports.addAll(getWarningsOrErrors(runtimeException, referenceValue));
      return new ImmutablePair<>(null, reports);
    }
  }

  private Set<Report> getWarningsOrErrors(Throwable exception, String referenceValue) {
    HttpStatus warningStatus;
    Throwable rootCause = ExceptionUtils.getRootCause(exception);
    HashSet<Report> reports = new HashSet<>();
    if (rootCause == null) {
      warningStatus = containsWarningStatus(exception.getMessage());
    } else {
      warningStatus = containsWarningStatus(rootCause.getMessage());
      if (warningStatus != null) {
        exception = rootCause;
      }
    }
    reports.add(warningStatus == null ?
        Report.buildEnrichmentError()
              .withMessage("Error while resolving values by uri when enriching references")
              .withValue(referenceValue)
              .withException(exception)
              .build() :
        Report.buildEnrichmentWarn()
              .withStatus(warningStatus)
              .withValue(referenceValue)
              .withException(exception)
              .build());
    return reports;
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

  private HashSet<Report> getSearchTermsReport(Set<SearchTermContext> searchTerms,
      Map<SearchTermContext, List<EnrichmentBase>> enrichedValues) {
    HashSet<Report> reports = new HashSet<>();
    for (SearchTermContext searchTerm : searchTerms) {
      if (fieldTypeContainsOrganizationType(searchTerm.getFieldTypes())
          && enrichedValues.get(searchTerm).isEmpty()) {
        reports.add(Report
            .buildEnrichmentWarn()
            .withMessage("Could not find an entity for the given search term with type Organization.")
            .withValue(searchTerm.getTextValue())
            .build());
      } else if (enrichedValues.get(searchTerm).isEmpty()) {
        reports.add(Report
            .buildEnrichmentIgnore()
            .withMessage("Could not find an entity for the given search term.")
            .withValue(searchTerm.getTextValue())
            .build());
      }
    }
    return reports;
  }

  private HashSet<Report> getSearchReferenceReport(Set<ReferenceTermContext> references,
      Map<ReferenceTermContext, List<EnrichmentBase>> enrichedReferences) {
    HashSet<Report> reports = new HashSet<>();
    for (ReferenceTermContext reference : references) {
      if (enrichedReferences.get(reference).isEmpty()) {
        reports.add(Report
            .buildEnrichmentIgnore()
            .withMessage("Could not find an entity for the given search reference.")
            .withValue(reference.getReferenceAsString())
            .build());
      }
    }
    return reports;
  }

  private boolean fieldTypeContainsOrganizationType(Set<FieldType<? extends AboutType>> fieldTypes) {
    for(FieldType<? extends AboutType> fieldType : fieldTypes){
      if(fieldType.getEntityType().equals(EntityType.ORGANIZATION))
        return true;
    }
    
    return false;
  }
}
