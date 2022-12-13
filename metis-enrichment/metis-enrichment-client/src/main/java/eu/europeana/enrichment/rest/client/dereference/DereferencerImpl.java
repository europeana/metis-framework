package eu.europeana.enrichment.rest.client.dereference;

import static eu.europeana.metis.network.ExternalRequestUtil.retryableExternalRequestForNetworkExceptions;

import eu.europeana.enrichment.api.external.DereferenceResultStatus;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentResultBaseWrapper;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.enrichment.api.internal.EntityResolver;
import eu.europeana.enrichment.api.internal.ReferenceTerm;
import eu.europeana.enrichment.api.internal.ReferenceTermImpl;
import eu.europeana.enrichment.rest.client.exceptions.DereferenceException;
import eu.europeana.enrichment.rest.client.report.ReportMessage;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
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

  private static URL checkIfUrlIsValid(HashSet<ReportMessage> reportMessages, String id) {
    try {
      return new URL(id);
    } catch (MalformedURLException e) {
      reportMessages.add(ReportMessage
          .buildDereferenceWarn()
          .withStatus(HttpStatus.BAD_REQUEST)
          .withValue(id)
          .withException(e)
          .build());
      LOGGER.debug("Invalid enrichment reference found: {}", id);
      return null;
    }
  }

  private static URL validateIfUrlToDereferenceExists(HashSet<ReportMessage> reportMessages, URL checkedUrl) {
    try {
      HttpURLConnection validationClient = (HttpURLConnection) checkedUrl.openConnection();
      HttpStatus responseCode = HttpStatus.resolve(validationClient.getResponseCode());

      LOGGER.debug("A URL {} response code.: {}", checkedUrl, responseCode);
      if (responseCode == HttpStatus.resolve(HttpURLConnection.HTTP_MOVED_TEMP)
          || responseCode == HttpStatus.resolve(HttpURLConnection.HTTP_MOVED_PERM)
          || responseCode == HttpStatus.resolve(HttpURLConnection.HTTP_SEE_OTHER)) {
        // get redirect url from "location" header field
        String newUrl = validationClient.getHeaderField("Location");
        // get the cookie if needed, for login
        String cookies = validationClient.getHeaderField("Set-Cookie");

        // open the new connnection again
        validationClient = (HttpURLConnection) new URL(newUrl).openConnection();
        validationClient.setRequestProperty("Cookie", cookies);
        validationClient.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
        validationClient.addRequestProperty("User-Agent", "Mozilla");
        validationClient.addRequestProperty("Referer", "europeana.eu");
        responseCode = HttpStatus.resolve(validationClient.getResponseCode());
        LOGGER.debug("Redirect to URL : {}", newUrl);
      }

      if (responseCode == HttpStatus.resolve(HttpURLConnection.HTTP_OK)) {
        LOGGER.debug("A URL to be dereferenced is valid.: {}", checkedUrl);
        return checkedUrl;
      } else {
        reportMessages.add(ReportMessage
            .buildDereferenceWarn()
            .withStatus(responseCode)
            .withValue(checkedUrl.toString())
            .withMessage("A URL to be dereferenced is invalid.")
            .build());
        LOGGER.debug("A URL to be dereferenced is invalid.: {} {}", checkedUrl, responseCode);
        return null;
      }
    } catch (IOException e) {
      reportMessages.add(ReportMessage
          .buildDereferenceWarn()
          .withStatus(HttpStatus.BAD_REQUEST)
          .withValue(checkedUrl.toString())
          .withException(e)
          .build());
      LOGGER.debug("A URL to be dereferenced is invalid.: {}", checkedUrl);
      return null;
    }
  }

  private static void setDereferenceStatusInReport(String resourceId, HashSet<ReportMessage> reportMessages,
      DereferenceResultStatus resultStatus) {
    if (!resultStatus.equals(DereferenceResultStatus.SUCCESS)) {
      String resultMessage;
      switch (resultStatus) {
        case ENTITY_FOUND_XML_XLT_ERROR:
          resultMessage = "Entity was found, applying the XSLT results in an XML error (either because the entity is malformed or the XSLT is malformed).";
          break;
        case INVALID_URL:
          resultMessage = "A URL to be dereferenced is invalid.";
          break;
        case NO_VOCABULARY_MATCHING:
          resultMessage = "Could not find a vocabulary matching the URL.";
          break;
        case UNKNOWN_ENTITY:
          resultMessage = "Dereferencing or Coreferencing: the europeana entity does not exist.";
          break;
        case NO_ENTITY_FOR_VOCABULARY:
          resultMessage = "Could not find an entity for a known vocabulary.";
          break;
        default:
          resultMessage = "";
      }
      if (resultStatus.equals(DereferenceResultStatus.NO_VOCABULARY_MATCHING)) {
        reportMessages.add(ReportMessage
            .buildDereferenceIgnore()
            .withStatus(HttpStatus.OK)
            .withValue(resourceId)
            .withMessage(resultMessage)
            .build());
      } else {
        reportMessages.add(ReportMessage
            .buildDereferenceWarn()
            .withStatus(HttpStatus.OK)
            .withValue(resourceId)
            .withMessage(resultMessage)
            .build());
      }
    }
  }

  @Override
  public Set<ReportMessage> dereference(RDF rdf) {
    HashSet<ReportMessage> reportMessages = new HashSet<>();
    // Extract fields from the RDF for dereferencing
    LOGGER.debug(" Extracting fields from RDF for dereferencing...");
    Set<String> resourceIds = extractReferencesForDereferencing(rdf);

    // Get the dereferenced information to add to the RDF using the extracted fields
    LOGGER.debug("Using extracted fields to gather enrichment-via-dereferencing information...");
    DereferencedEntity dereferenceInformation = dereferenceEntities(resourceIds);
    reportMessages.addAll(dereferenceInformation.getReportMessages());

    // Merge the acquired information into the RDF
    LOGGER.debug("Merging Dereference Information...");
    entityMergeEngine.mergeReferenceEntities(rdf, dereferenceInformation.getEnrichmentBaseList());

    // Done.
    LOGGER.debug("Dereference completed.");
    return reportMessages;
  }

  @Override
  public DereferencedEntity dereferenceEntities(Set<String> resourceIds) {
    HashSet<ReportMessage> reportMessages = new HashSet<>();
    // Sanity check.
    if (resourceIds.isEmpty()) {
      return new DereferencedEntity(Collections.emptyList(), reportMessages);
    }

    // First try to get them from our own entity collection database.
    Set<ReferenceTerm> referenceTermSet = resourceIds
        .stream()
        .map(id -> checkIfUrlIsValid(reportMessages, id))
        .filter(Objects::nonNull)
        .map(checkedUrl -> validateIfUrlToDereferenceExists(reportMessages, checkedUrl))
        .filter(Objects::nonNull)
        .map(validatedUrl -> new ReferenceTermImpl(validatedUrl, new HashSet<>()))
        .collect(Collectors.toSet());

    final DereferencedEntity deferencedOwnEntities = dereferenceOwnEntities(referenceTermSet);
    reportMessages.addAll(deferencedOwnEntities.getReportMessages());

    final Set<String> foundOwnEntityIds = deferencedOwnEntities.getEnrichmentBaseList()
                                                               .stream()
                                                               .map(EnrichmentBase::getAbout)
                                                               .collect(Collectors.toSet());

    // For the remaining ones, get them from the dereference service.
    for (ReferenceTerm resourceId : referenceTermSet) {
      if (!foundOwnEntityIds.contains(resourceId.getReference().toString())) {
        DereferencedEntity deferencedExternalEntities =
            dereferenceExternalEntity(resourceId.getReference().toString());
        reportMessages.addAll(deferencedExternalEntities.getReportMessages());
        deferencedOwnEntities.getEnrichmentBaseList().addAll(deferencedExternalEntities.getEnrichmentBaseList());
      }
    }
    // Done.
    return new DereferencedEntity(deferencedOwnEntities.getEnrichmentBaseList(), reportMessages);
  }

  @Override
  public Set<String> extractReferencesForDereferencing(RDF rdf) {
    return DereferenceUtils.extractReferencesForDereferencing(rdf);
  }

  private DereferencedEntity dereferenceOwnEntities(Set<ReferenceTerm> resourceIds) {
    HashSet<ReportMessage> reportMessages = new HashSet<>();
    if (entityResolver == null) {
      return new DereferencedEntity(Collections.emptyList(), reportMessages);
    }
    try {
      return new DereferencedEntity(new ArrayList<>(entityResolver.resolveById(resourceIds).values()), reportMessages);
    } catch (Exception e) {
      DereferenceException dereferenceException = new DereferenceException(
          "Exception occurred while trying to perform dereferencing.", e);
      reportMessages.add(ReportMessage
          .buildDereferenceWarn()
          .withStatus(HttpStatus.OK)
          .withValue(resourceIds.stream()
                                .map(resourceId -> resourceId.getReference().toString())
                                .collect(Collectors.joining(",")))
          .withException(dereferenceException)
          .build());
      return new DereferencedEntity(new ArrayList<>(), reportMessages);
    }
  }

  private DereferencedEntity dereferenceExternalEntity(String resourceId) {
    HashSet<ReportMessage> reportMessages = new HashSet<>();
    // Check that there is something to do.
    if (dereferenceClient == null) {
      return new DereferencedEntity(Collections.emptyList(), reportMessages);
    }

    // Perform the dereferencing.
    EnrichmentResultList result;
    try {
      LOGGER.debug("== Processing {}", resourceId);
      result = retryableExternalRequestForNetworkExceptions(
          () -> dereferenceClient.dereference(resourceId));
      DereferenceResultStatus resultStatus = Optional.ofNullable(result)
                                                     .map(EnrichmentResultList::getEnrichmentBaseResultWrapperList)
                                                     .orElseGet(Collections::emptyList).stream()
                                                     .map(EnrichmentResultBaseWrapper::getDereferenceStatus)
                                                     .filter(Objects::nonNull).findFirst()
                                                     .orElse(DereferenceResultStatus.UNKNOWN_ENTITY);

      setDereferenceStatusInReport(resourceId, reportMessages, resultStatus);
    } catch (BadRequest e) {
      // We are forgiving for these errors
      LOGGER.warn("ResourceId {}, failed", resourceId, e);
      reportMessages.add(ReportMessage
          .buildDereferenceWarn()
          .withStatus(HttpStatus.BAD_REQUEST)
          .withValue(resourceId)
          .withException(e)
          .build());
      result = null;
    } catch (Exception e) {
      DereferenceException dereferenceException = new DereferenceException(
          "Exception occurred while trying to perform dereferencing.", e);
      reportMessages.add(ReportMessage
          .buildDereferenceWarn()
          .withStatus(HttpStatus.OK)
          .withValue(resourceId)
          .withException(dereferenceException)
          .build());
      result = null;
    }

    // Return the result.
    return new DereferencedEntity(Optional.ofNullable(result).map(EnrichmentResultList::getEnrichmentBaseResultWrapperList)
                                          .orElseGet(Collections::emptyList).stream()
                                          .map(EnrichmentResultBaseWrapper::getEnrichmentBaseList).filter(Objects::nonNull)
                                          .flatMap(List::stream).collect(Collectors.toList()), reportMessages);
  }
}
