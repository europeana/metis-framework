package eu.europeana.enrichment.rest.client.dereference;

import static eu.europeana.metis.network.ExternalRequestUtil.retryableExternalRequestForNetworkExceptions;

import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentResultBaseWrapper;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.enrichment.api.internal.EntityResolver;
import eu.europeana.enrichment.api.internal.ReferenceTerm;
import eu.europeana.enrichment.api.internal.ReferenceTermImpl;
import eu.europeana.enrichment.rest.client.EnrichmentWorker.Mode;
import eu.europeana.enrichment.rest.client.exceptions.DereferenceException;
import eu.europeana.enrichment.rest.client.report.ReportMessage;
import eu.europeana.enrichment.rest.client.report.ReportMessage.ReportMessageBuilder;
import eu.europeana.enrichment.rest.client.report.Type;
import eu.europeana.enrichment.utils.DereferenceUtils;
import eu.europeana.enrichment.utils.EntityMergeEngine;
import eu.europeana.metis.schema.jibx.RDF;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException.BadRequest;

/**
 * The default implementation of the dereferencing function that accesses a server through HTTP/REST.
 */
public class DereferencerImpl implements Dereferencer {

  private static final Logger LOGGER = LoggerFactory.getLogger(DereferencerImpl.class);

  private final EntityMergeEngine entityMergeEngine;
  private final EntityResolver entityResolver;
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
    this.dereferenceClient = dereferenceClient;
  }

  @Override
  public HashSet<ReportMessage> dereference(RDF rdf) {
    HashSet<ReportMessage> reportMessages = new HashSet<>();
    // Extract fields from the RDF for dereferencing
    LOGGER.debug(" Extracting fields from RDF for dereferencing...");
    Set<String> resourceIds = extractReferencesForDereferencing(rdf);

    // Get the dereferenced information to add to the RDF using the extracted fields
    LOGGER.debug("Using extracted fields to gather enrichment-via-dereferencing information...");
    Pair<List<EnrichmentBase>, HashSet<ReportMessage>> dereferenceInformation = dereferenceEntities(resourceIds);
    reportMessages.addAll(dereferenceInformation.getRight());

    // Merge the acquired information into the RDF
    LOGGER.debug("Merging Dereference Information...");
    entityMergeEngine.mergeReferenceEntities(rdf, dereferenceInformation.getLeft());

    // Done.
    LOGGER.debug("Dereference completed.");
    return reportMessages;
  }

  @Override
  public Pair<List<EnrichmentBase>, HashSet<ReportMessage>> dereferenceEntities(Set<String> resourceIds) {
    HashSet<ReportMessage> reportMessages = new HashSet<>();
    // Sanity check.
    if (resourceIds.isEmpty()) {
      return new ImmutablePair<>(Collections.emptyList(), reportMessages);
    }

    // First try to get them from our own entity collection database.
    Set<ReferenceTerm> referenceTermSet = resourceIds
        .stream()
        .map(id -> {
          try {
            return new URL(id);
          } catch (MalformedURLException e) {
            reportMessages.add(new ReportMessageBuilder()
                .withMode(Mode.DEREFERENCE)
                .withStatus(400)
                .withValue(id)
                .withMessageType(Type.WARN)
                .withMessage(ExceptionUtils.getMessage(e))
                .withStackTrace(ExceptionUtils.getStackTrace(e))
                .build());
            LOGGER.debug("Invalid enrichment reference found: {}", id);
            return null;
          }
        })
        .filter(Objects::nonNull)
        .map(checkedUrl -> {
              try {
                HttpURLConnection validationClient = (HttpURLConnection) checkedUrl.openConnection();
                int responseCode = validationClient.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                  LOGGER.debug("A URL to be dereferenced is valid.: {}", checkedUrl);
                  return checkedUrl;
                } else {
                  reportMessages.add(new ReportMessageBuilder()
                      .withMode(Mode.DEREFERENCE)
                      .withStatus(responseCode)
                      .withValue(checkedUrl.toString())
                      .withMessageType(Type.WARN)
                      .withMessage("A URL to be dereferenced is invalid.")
                      .withStackTrace("")
                      .build());
                  LOGGER.debug("A URL to be dereferenced is invalid.: {} {}", checkedUrl, responseCode);
                  return null;
                }
              } catch (IOException e) {
                reportMessages.add(new ReportMessageBuilder()
                    .withMode(Mode.DEREFERENCE)
                    .withStatus(400)
                    .withValue(checkedUrl.toString())
                    .withMessageType(Type.WARN)
                    .withMessage(ExceptionUtils.getMessage(e))
                    .withStackTrace(ExceptionUtils.getStackTrace(e))
                    .build());
                LOGGER.debug("A URL to be dereferenced is invalid.: {}", checkedUrl);
                return null;
              }
            }
        )
        .filter(Objects::nonNull)
        .map(validatedUrl -> new ReferenceTermImpl(validatedUrl, new HashSet<>()))
        .collect(Collectors.toSet());

    final Pair<List<EnrichmentBase>, HashSet<ReportMessage>> deferencedOwnEntities = dereferenceOwnEntities(referenceTermSet);
    reportMessages.addAll(deferencedOwnEntities.getRight());

    final Set<String> foundOwnEntityIds = deferencedOwnEntities.getLeft()
                                                               .stream()
                                                               .map(EnrichmentBase::getAbout)
                                                               .collect(Collectors.toSet());

    // For the remaining ones, get them from the dereference service.
    for (ReferenceTerm resourceId : referenceTermSet) {
      if (!foundOwnEntityIds.contains(resourceId.getReference().toString())) {
        Pair<List<EnrichmentBase>, HashSet<ReportMessage>> deferencedExternalEntities =
            dereferenceExternalEntity(resourceId.getReference().toString());
        reportMessages.addAll(deferencedExternalEntities.getRight());
        deferencedOwnEntities.getLeft().addAll(deferencedExternalEntities.getLeft());
      }
    }
    // Done.
    return new ImmutablePair<>(deferencedOwnEntities.getLeft(), reportMessages);
  }

  private Pair<List<EnrichmentBase>, HashSet<ReportMessage>> dereferenceOwnEntities(Set<ReferenceTerm> resourceIds) {
    HashSet<ReportMessage> reportMessages = new HashSet<>();
    if (entityResolver == null) {
      return new ImmutablePair<>(Collections.emptyList(), reportMessages);
    }
    try {
      return new ImmutablePair<>(new ArrayList<>(entityResolver.resolveById(resourceIds).values()), reportMessages);
    } catch (Exception e) {
      DereferenceException dereferenceException = new DereferenceException(
          "Exception occurred while trying to perform dereferencing.", e);
      reportMessages.add(new ReportMessageBuilder()
          .withMode(Mode.DEREFERENCE)
          .withStatus(200)
          .withValue(resourceIds.stream()
                                .map(resourceId -> resourceId.getReference().toString())
                                .collect(Collectors.joining(",")))
          .withMessageType(Type.WARN)
          .withMessage(ExceptionUtils.getMessage(dereferenceException))
          .withStackTrace(ExceptionUtils.getStackTrace(dereferenceException))
          .build());
      return new ImmutablePair<>(new ArrayList<>(), reportMessages);
    }
  }

  private Pair<List<EnrichmentBase>, HashSet<ReportMessage>> dereferenceExternalEntity(String resourceId) {
    HashSet<ReportMessage> reportMessages = new HashSet<>();
    // Check that there is something to do.
    if (dereferenceClient == null) {
      return new ImmutablePair<>(Collections.emptyList(), reportMessages);
    }

    // Perform the dereferencing.
    EnrichmentResultList result;
    try {
      LOGGER.debug("== Processing {}", resourceId);
      result = retryableExternalRequestForNetworkExceptions(
          () -> dereferenceClient.dereference(resourceId));
    } catch (BadRequest e) {
      // We are forgiving for these errors
      LOGGER.warn("ResourceId {}, failed", resourceId, e);
      reportMessages.add(new ReportMessageBuilder()
          .withMode(Mode.DEREFERENCE)
          .withStatus(200)
          .withValue(resourceId)
          .withMessageType(Type.WARN)
          .withMessage(ExceptionUtils.getMessage(e))
          .withStackTrace(ExceptionUtils.getStackTrace(e))
          .build());
      result = null;
    } catch (Exception e) {
      DereferenceException dereferenceException= new DereferenceException("Exception occurred while trying to perform dereferencing.", e);
      reportMessages.add(new ReportMessageBuilder()
          .withMode(Mode.DEREFERENCE)
          .withStatus(200)
          .withValue(resourceId)
          .withMessageType(Type.WARN)
          .withMessage(ExceptionUtils.getMessage(dereferenceException))
          .withStackTrace(ExceptionUtils.getStackTrace(dereferenceException))
          .build());
      result = null;
    }

    // Return the result.
    return new ImmutablePair<>(Optional.ofNullable(result).map(EnrichmentResultList::getEnrichmentBaseResultWrapperList)
                   .orElseGet(Collections::emptyList).stream()
                   .map(EnrichmentResultBaseWrapper::getEnrichmentBaseList).filter(Objects::nonNull)
                   .flatMap(List::stream).collect(Collectors.toList()), reportMessages);
  }

  @Override
  public Set<String> extractReferencesForDereferencing(RDF rdf) {
    return DereferenceUtils.extractReferencesForDereferencing(rdf);
  }
}
