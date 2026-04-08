package eu.europeana.enrichment.rest.client.dereference;

import static eu.europeana.metis.network.ExternalRequestUtil.retryableExternalRequestForNetworkExceptions;

import eu.europeana.enrichment.api.external.DereferenceResultStatus;
import eu.europeana.enrichment.api.external.impl.ClientEntityResolverFactory;
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
import eu.europeana.entity.client.exception.EntityClientException;
import eu.europeana.metis.schema.jibx.RDF;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.function.Consumer;
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
  private final DereferenceClient dereferenceClient;

  /**
   * Instantiates a new Dereferencer.
   *
   * @param entityMergeEngine The entity merge engine. Cannot be null. Is required.
   * @param entityResolverFactory the client entity resolver factory. Cannot be null. Is required.
   * @param dereferenceClient Dereference client. Can be null if we don't dereference own entities.
   */
  public DereferencerImpl(EntityMergeEngine entityMergeEngine,
      ClientEntityResolverFactory entityResolverFactory,
      DereferenceClient dereferenceClient) {
    if (entityMergeEngine == null) {
      throw new IllegalArgumentException("You need to specify an entityMergeEngine");
    }
    if (entityResolverFactory == null) {
      throw new IllegalArgumentException("You need to specify an entityResolverFactory");
    }
    try {
      this.entityResolver = entityResolverFactory.create();
    } catch (EntityClientException e) {
      throw new IllegalArgumentException("Could not create entity resolver from factory.", e);
    }
    this.entityMergeEngine = entityMergeEngine;
    this.dereferenceClient = dereferenceClient;
  }

  private static URL checkIfUrlIsValid(Consumer<Report> reportAction, String id) {
    try {
      URI uri = new URI(id);
      return new URI(uri.toString()).toURL();
    } catch (URISyntaxException | MalformedURLException | IllegalArgumentException e) {
      reportAction.accept(Report
          .buildDereferenceIgnore()
          .withStatus(HttpStatus.OK)
          .withValue(id)
          .withException(e)
          .build());
      LOGGER.debug("Invalid enrichment reference found: {}", id);
      return null;
    }
  }

  private static void setDereferenceStatusInReport(String resourceId, Consumer<Report> reportAction,
      DereferenceResultStatus resultStatus) {
    if (!resultStatus.equals(DereferenceResultStatus.SUCCESS)) {
      final String resultMessage = getResultStatusMessage(resultStatus);
      if (resultStatus.equals(DereferenceResultStatus.FAILURE)) {
        reportAction.accept(Report.buildDereferenceError()
            .withValue(resourceId)
            .withMessage(resultMessage)
            .build());
      } else if (resultStatus.equals(DereferenceResultStatus.INVALID_URL) ||
          resultStatus.equals(DereferenceResultStatus.NO_VOCABULARY_MATCHING) ||
          resultStatus.equals(DereferenceResultStatus.ENTITY_FOUND_XML_XSLT_PRODUCE_NO_CONTEXTUAL_CLASS)) {
        reportAction.accept(Report
            .buildDereferenceIgnore()
            .withStatus(HttpStatus.OK)
            .withValue(resourceId)
            .withMessage(resultMessage)
            .build());
      } else {
        reportAction.accept(Report
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
    Map<String, PermittedEntityType> resourceIds = extractReferencesForDereferencing(rdf);

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
  public DereferencedEntities dereferenceEntities(Map<String, PermittedEntityType> resourceIds) {

    // Sanity check.
    if (resourceIds == null || resourceIds.isEmpty()) {
      return DereferencedEntities.emptyInstance();
    }

    // Create reference terms for all valid resource IDs
    final HashSet<Report> reports = new HashSet<>();
    final Map<String, ReferenceTerm> referenceTerms = toReferenceTerms(resourceIds.keySet(),
        reports::add);
    final DereferencedEntities result = new DereferencedEntities(Collections.emptyMap(), reports);

    // First, try to get entities from the Europeana entity collection database.
    final Set<PermittedEntityType> europeanaTypes = EnumSet.of(PermittedEntityType.EUROPEANA_ENTITY,
        PermittedEntityType.ANY_ENTITY);
    final Set<ReferenceTerm> termsForEuropeanaDereference = resourceIds.entrySet().stream()
        .filter(entry -> europeanaTypes.contains(entry.getValue()))
        .map(Entry::getKey).map(referenceTerms::get).filter(Objects::nonNull)
        .collect(Collectors.toSet());
    if (!termsForEuropeanaDereference.isEmpty()) {
      result.addAll(dereferenceEuropeanaEntities(termsForEuropeanaDereference));
    }

    // For the remaining ones, get them from the dereference service.
    final Set<PermittedEntityType> externalTypes = EnumSet.of(PermittedEntityType.EXTERNAL_ENTITY,
        PermittedEntityType.ANY_ENTITY);
    final Set<String> europeanaEntityIds = result.getReferenceTermListMap().values().stream()
        .flatMap(Collection::stream).map(EnrichmentBase::getAbout).collect(Collectors.toSet());
    final Set<ReferenceTerm> termsForExternalDereference = resourceIds.entrySet().stream()
        .filter(entry -> externalTypes.contains(entry.getValue()))
        .map(Entry::getKey)
        .filter(id -> !europeanaEntityIds.contains(id))
        .map(referenceTerms::get).filter(Objects::nonNull).collect(Collectors.toSet());
    if (!termsForExternalDereference.isEmpty()) {
      result.addAll(dereferenceExternalEntity(termsForExternalDereference));
    }

    // Done.
    return result;
  }

  @Override
  public Map<String, PermittedEntityType> extractReferencesForDereferencing(RDF rdf) {
    return DereferenceUtils.extractReferencesForDereferencing(rdf);
  }

  @Override
  public DereferencedEntities dereferenceEuropeanaEntities(Set<ReferenceTerm> resourceIds) {

    // Sanity check.
    if (resourceIds == null || resourceIds.isEmpty()) {
      return DereferencedEntities.emptyInstance();
    }

    // Set up the dereference process: only attempt for Europeana entities.
    final Set<Report> reports = new HashSet<>();
    final Map<ReferenceTerm, List<EnrichmentBase>> result = new HashMap<>();
    final Set<ReferenceTerm> europeanaEntities = resourceIds.stream()
        .filter(term -> EntityResolver.europeanaLinkPattern.matcher(
            term.getReference().toString()).matches())
        .collect(Collectors.toSet());

    // Resolve the references and collect the result.
    try {
      entityResolver.resolveById(europeanaEntities)
          .forEach((key, value) -> result.put(key, List.of(value)));
      europeanaEntities.stream()
          .filter(id -> result.get(id) == null || result.get(id).isEmpty())
          .forEach(nonEuropeanaId -> {
            setDereferenceStatusInReport(nonEuropeanaId.getReference().toString(),
                reports::add, DereferenceResultStatus.UNKNOWN_EUROPEANA_ENTITY);
            result.putIfAbsent(nonEuropeanaId, Collections.emptyList());
          });
    } catch (CancellationException e) {
      LOGGER.warn(CANCELLATION_EXCEPTION_WARN_MESSAGE);
      throw e;
    } catch (Exception e) {
      handleDereferencingException(resourceIds, reports::add, e);
      return new DereferencedEntities(Collections.emptyMap(), reports);
    }

    // Done.
    return new DereferencedEntities(result, reports);
  }

  @Override
  public void close() throws Exception {
    this.entityResolver.close();
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

        setDereferenceStatusInReport(resourceId, reports::add, resultStatus);
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
      } catch (CancellationException e) {
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
      resultMap.put(referenceTerm,
          Optional.ofNullable(result).map(EnrichmentResultList::getEnrichmentBaseResultWrapperList)
              .orElseGet(Collections::emptyList).stream()
              .map(EnrichmentResultBaseWrapper::getEnrichmentBaseList).filter(Objects::nonNull)
              .flatMap(List::stream).toList());
    }

    // Return the result.
    return new DereferencedEntities(resultMap, reports);
  }

  private Map<String, ReferenceTerm> toReferenceTerms(Set<String> resourceIds,
      Consumer<Report> reportAction) {
    final Map<String, ReferenceTerm> result = new HashMap<>();
    resourceIds.forEach(id -> Optional.ofNullable(checkIfUrlIsValid(reportAction, id))
        .ifPresent(url -> result.put(id, new ReferenceTermImpl(url, Collections.emptySet()))));
    return result;
  }

  private void handleDereferencingException(Set<ReferenceTerm> resourceIds,
      Consumer<Report> reportAction, Exception exception) {
    DereferenceException dereferenceException = new DereferenceException(
        "Exception occurred while trying to perform dereferencing.", exception);
    reportAction.accept(Report
        .buildDereferenceWarn()
        .withStatus(HttpStatus.OK)
        .withValue(resourceIds.stream()
            .map(resourceId -> resourceId.getReference().toString())
            .collect(Collectors.joining(",")))
        .withException(dereferenceException)
        .build());
  }
}
