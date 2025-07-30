package eu.europeana.enrichment.rest.client.dereference;

import static eu.europeana.metis.network.ExternalRequestUtil.retryableExternalRequestForNetworkExceptions;

import eu.europeana.enrichment.api.external.DereferenceResultStatus;
import eu.europeana.enrichment.api.external.impl.ClientEntityResolver;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentResultBaseWrapper;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.enrichment.api.internal.EntityResolver;
import eu.europeana.enrichment.api.internal.ReferenceTerm;
import eu.europeana.enrichment.api.internal.ReferenceTermImpl;
import eu.europeana.enrichment.rest.client.exceptions.DereferenceException;
import eu.europeana.enrichment.rest.client.report.Report;
import eu.europeana.enrichment.utils.DereferenceUtils;
import eu.europeana.enrichment.utils.EntityMergeEngine;
import eu.europeana.entity.client.config.EntityClientConfiguration;
import eu.europeana.entity.client.exception.EntityClientException;
import eu.europeana.metis.schema.jibx.RDF;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException.BadRequest;

/**
 * The default implementation of the dereferencing function that accesses a server through HTTP/REST.
 */
public class DereferencerImpl implements Dereferencer {

  private static final Logger LOGGER = LoggerFactory.getLogger(DereferencerImpl.class);
  private static final String CANCELLATION_EXCEPTION_WARN_MESSAGE = "Cancellation exception occurred while trying to perform dereferencing, rethrowing.";

  private final EntityMergeEngine entityMergeEngine;
  private final EntityResolver entityResolver;
  private final EntityClientConfiguration entityApiClientConfiguration;
  private final DereferenceClient dereferenceClient;

  /**
   * Constructor.
   *
   * @param entityMergeEngine The entity merge engine. Cannot be null.
   * @param entityResolver Remove entity resolver: Can be null if we only dereference own entities.
   * @param dereferenceClient Dereference client. Can be null if we don't dereference own entities.
   */
  public DereferencerImpl(EntityMergeEngine entityMergeEngine, EntityResolver entityResolver,
      DereferenceClient dereferenceClient) {
    this.entityMergeEngine = entityMergeEngine;
    this.entityResolver = entityResolver;
    this.entityApiClientConfiguration = null;
    this.dereferenceClient = dereferenceClient;
  }

  /**
   * Constructor.
   *
   * @param entityMergeEngine The entity merge engine. Cannot be null.
   * @param entityApiClientConfiguration the configuration to create entity resolvers
   * @param dereferenceClient Dereference client. Can be null if we don't dereference own entities.
   */
  public DereferencerImpl(EntityMergeEngine entityMergeEngine, EntityClientConfiguration entityApiClientConfiguration,
      DereferenceClient dereferenceClient) {
    this.entityMergeEngine = entityMergeEngine;
    this.entityResolver = null;
    this.entityApiClientConfiguration = new EntityClientConfiguration(entityApiClientConfiguration);
    this.dereferenceClient = dereferenceClient;
  }

  private static URL checkIfUrlIsValid(HashSet<Report> reports, String id) {
    try {
      URI uri = new URI(id);
      return new URI(uri.toString()).toURL();
    } catch (URISyntaxException | MalformedURLException | IllegalArgumentException e) {
      reports.add(Report
          .buildDereferenceIgnore()
          .withStatus(HttpStatus.OK)
          .withValue(id)
          .withException(e)
          .build());
      LOGGER.debug("Invalid enrichment reference found: {}", id);
      return null;
    }
  }

  private static void setDereferenceStatusInReport(String resourceId, HashSet<Report> reports,
      DereferenceResultStatus resultStatus) {
    if (!resultStatus.equals(DereferenceResultStatus.SUCCESS)) {
      final String resultMessage = getResultStatusMessage(resultStatus);
      if (resultStatus.equals(DereferenceResultStatus.FAILURE)) {
        reports.add(Report.buildDereferenceError()
                          .withValue(resourceId)
                          .withMessage(resultMessage)
                          .build());
      } else if (resultStatus.equals(DereferenceResultStatus.INVALID_URL) ||
          resultStatus.equals(DereferenceResultStatus.NO_VOCABULARY_MATCHING) ||
          resultStatus.equals(DereferenceResultStatus.ENTITY_FOUND_XML_XSLT_PRODUCE_NO_CONTEXTUAL_CLASS)) {
        reports.add(Report
            .buildDereferenceIgnore()
            .withStatus(HttpStatus.OK)
            .withValue(resourceId)
            .withMessage(resultMessage)
            .build());
      } else {
        reports.add(Report
            .buildDereferenceWarn()
            .withStatus(HttpStatus.OK)
            .withValue(resourceId)
            .withMessage(resultMessage)
            .build());
      }
    }
  }

  @NotNull
  private static String getResultStatusMessage(DereferenceResultStatus resultStatus) {
    return switch (resultStatus) {
      case ENTITY_FOUND_XML_XSLT_ERROR -> "Entity was found, applying the XSLT results in an XML error"
          .concat(" either because the entity is malformed or the XSLT is malformed.");
      case ENTITY_FOUND_XML_XSLT_PRODUCE_NO_CONTEXTUAL_CLASS ->
          "Entity was found, but the XSLT mapping did not produce a contextual class.";
      case INVALID_URL -> "A URL to be dereferenced is invalid.";
      case NO_VOCABULARY_MATCHING -> "Could not find a vocabulary matching the URL.";
      case UNKNOWN_EUROPEANA_ENTITY -> "Dereferencing or Coreferencing: the europeana entity does not exist.";
      case NO_ENTITY_FOR_VOCABULARY -> "Could not find an entity for a known vocabulary.";
      case FAILURE -> "Dereference or Coreferencing failed.";
      default -> "";
    };
  }

  @Override
  public Set<Report> dereference(RDF rdf) {

    // Extract fields from the RDF for dereferencing, grouped by the source type.
    LOGGER.debug(" Extracting fields from RDF for dereferencing...");
    Set<String> resourceIds = extractReferencesForDereferencing(rdf);

    // Get the dereferenced information to add to the RDF using the extracted fields
    LOGGER.debug("Using extracted fields to gather enrichment-via-dereferencing information...");
    DereferencedEntities dereferenceInformation = dereferenceEntities(resourceIds);

    // Merge the acquired information into the RDF
    LOGGER.debug("Merging Dereference Information...");
    entityMergeEngine.convertAndAddAllEntities(rdf, dereferenceInformation);

    // Done.
    LOGGER.debug("Dereference completed.");
    return dereferenceInformation.getReportMessages();
  }

  @Override
  public DereferencedEntities dereferenceEntities(Set<String> resourceIds) {

    // Sanity check.
    if (resourceIds.isEmpty()) {
      return DereferencedEntities.emptyInstance();
    }

    // First try to get them from our own entity collection database.
    DereferencedEntities result = DereferencedEntities.emptyInstance();
    HashSet<Report> reports = new HashSet<>();
    Set<ReferenceTerm> referenceTerms = setUpReferenceTermSet(resourceIds, reports);
    result.addAll(dereferenceEuropeanaEntities(referenceTerms, reports));
    final Set<String> foundOwnEntityIds = result.getReferenceTermListMap().values().stream()
        .flatMap(Collection::stream).map(EnrichmentBase::getAbout).collect(Collectors.toSet());

    // For the remaining ones, get them from the dereference service.
    Set<ReferenceTerm> notFoundOwnReferenceTerms = referenceTerms.stream().filter(
        referenceTerm -> !foundOwnEntityIds.contains(referenceTerm.getReference().toString())).collect(
        Collectors.toSet());
    if (!notFoundOwnReferenceTerms.isEmpty()) {
      result.addAll(dereferenceExternalEntity(notFoundOwnReferenceTerms));
    }

    // Done.
    return result;
  }

  @Override
  public Set<String> extractReferencesForDereferencing(RDF rdf) {
    return DereferenceUtils.extractReferencesForDereferencing(rdf);
  }

  @Override
  public DereferencedEntities dereferenceEuropeanaEntities(Set<ReferenceTerm> resourceIds,
      HashSet<Report> reports) {

    if (this.entityResolver == null && this.entityApiClientConfiguration == null) {
      return DereferencedEntities.emptyInstance();
    }
    final EntityResolver entityResolverToUse = Optional.ofNullable(this.entityResolver)
        .orElseGet(() -> {
          try {
            return ClientEntityResolver.create(entityApiClientConfiguration);
          } catch (EntityClientException e) {
            throw new IllegalArgumentException(e);
          }
        });

    try {
      Map<ReferenceTerm, List<EnrichmentBase>> result = new HashMap<>();
      Set<ReferenceTerm> ownEntities = resourceIds.stream()
                                                  .filter(id -> EntityResolver.europeanaLinkPattern.matcher(
                                                      id.getReference().toString()).matches())
                                                  .collect(Collectors.toSet());
      entityResolverToUse.resolveById(ownEntities)
                    .forEach((key, value) -> result.put(key, List.of(value)));
      ownEntities.stream().filter(id -> result.get(id) == null || result.get(id).isEmpty())
                 .forEach(notFoundOwnId -> {
                   setDereferenceStatusInReport(notFoundOwnId.getReference().toString(),
                       reports, DereferenceResultStatus.UNKNOWN_EUROPEANA_ENTITY);
                   result.putIfAbsent(notFoundOwnId, Collections.emptyList());
                 });
      entityResolverToUse.close();
      return new DereferencedEntities(result, reports);
    } catch (CancellationException e){
      LOGGER.warn(CANCELLATION_EXCEPTION_WARN_MESSAGE);
      throw e;
    } catch (Exception e) {
      return handleDereferencingException(resourceIds, reports, e);
    }
  }

  private DereferencedEntities dereferenceExternalEntity(Set<ReferenceTerm> referenceTerms) {

    // Check that there is something to do.
    if (dereferenceClient == null) {
      return DereferencedEntities.emptyInstance();
    }

    // Perform the dereferencing.
    HashSet<Report> reports = new HashSet<>();
    EnrichmentResultList result;
    Map<ReferenceTerm, List<EnrichmentBase>> resultMap = new HashMap<>();
    for (ReferenceTerm referenceTerm : referenceTerms) {
      String resourceId = referenceTerm.getReference().toString();
      try {
        LOGGER.debug("Dereference external entity processing {}", resourceId);
        result = retryableExternalRequestForNetworkExceptions(
            () -> dereferenceClient.dereference(resourceId));
        DereferenceResultStatus resultStatus = Optional.ofNullable(result)
                                                       .map(EnrichmentResultList::getEnrichmentBaseResultWrapperList)
                                                       .orElseGet(Collections::emptyList).stream()
                                                       .map(EnrichmentResultBaseWrapper::getDereferenceStatus)
                                                       .filter(Objects::nonNull).findFirst()
                                                       .orElse(DereferenceResultStatus.FAILURE);

        setDereferenceStatusInReport(resourceId, reports, resultStatus);
      } catch (BadRequest e) {
        // We are forgiving for these errors
        LOGGER.warn("ResourceId {}, failed", resourceId, e);
        reports.add(Report
            .buildDereferenceWarn()
            .withStatus(HttpStatus.BAD_REQUEST)
            .withValue(resourceId)
            .withException(e)
            .build());
        result = null;
      } catch (CancellationException e){
        LOGGER.warn(CANCELLATION_EXCEPTION_WARN_MESSAGE);
        throw e;
      } catch (Exception e) {
        DereferenceException dereferenceException = new DereferenceException(
            "Exception occurred while trying to perform dereferencing.", e);
        reports.add(Report
            .buildDereferenceError()
            .withValue(resourceId)
            .withException(dereferenceException)
            .build());
        result = null;
      }
      resultMap.put(referenceTerm, Optional.ofNullable(result).map(EnrichmentResultList::getEnrichmentBaseResultWrapperList)
                                           .orElseGet(Collections::emptyList).stream()
                                           .map(EnrichmentResultBaseWrapper::getEnrichmentBaseList).filter(Objects::nonNull)
                                           .flatMap(List::stream).toList());
    }

    // Return the result.
    return new DereferencedEntities(resultMap, reports);
  }

  private Set<ReferenceTerm> setUpReferenceTermSet(Set<String> resourcesIds, HashSet<Report> reports) {
    return resourcesIds.stream()
                       .map(id -> checkIfUrlIsValid(reports, id))
                       .filter(Objects::nonNull)
                       .map(validateUrl -> new ReferenceTermImpl(validateUrl, new HashSet<>()))
                       .collect(Collectors.toSet());
  }

  private DereferencedEntities handleDereferencingException(Set<ReferenceTerm> resourceIds, HashSet<Report> reports,
      Exception exception) {
    DereferenceException dereferenceException = new DereferenceException(
        "Exception occurred while trying to perform dereferencing.", exception);
    reports.add(Report
        .buildDereferenceWarn()
        .withStatus(HttpStatus.OK)
        .withValue(resourceIds.stream()
                              .map(resourceId -> resourceId.getReference().toString())
                              .collect(Collectors.joining(",")))
        .withException(dereferenceException)
        .build());
    return new DereferencedEntities(new HashMap<>(), reports);
  }
}
