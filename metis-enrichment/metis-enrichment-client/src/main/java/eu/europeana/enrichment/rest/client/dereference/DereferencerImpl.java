package eu.europeana.enrichment.rest.client.dereference;

import static eu.europeana.metis.utils.ExternalRequestUtil.retryableExternalRequestForNetworkExceptions;

import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentBaseWrapper;
import eu.europeana.enrichment.api.external.model.EnrichmentResultBaseWrapper;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.enrichment.rest.client.enrichment.EnrichmentClient;
import eu.europeana.enrichment.rest.client.exceptions.DereferenceException;
import eu.europeana.enrichment.utils.DereferenceUtils;
import eu.europeana.enrichment.utils.EntityMergeEngine;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException.BadRequest;

public class DereferencerImpl implements Dereferencer{

  private static final Logger LOGGER = LoggerFactory.getLogger(DereferencerImpl.class);

  private final EntityMergeEngine entityMergeEngine;
  private final EnrichmentClient enrichmentClient;
  private final DereferenceClient dereferenceClient;

  public DereferencerImpl(EntityMergeEngine entityMergeEngine,
      EnrichmentClient enrichmentClient,
      DereferenceClient dereferenceClient) {
    this.entityMergeEngine = entityMergeEngine;
    this.enrichmentClient = enrichmentClient;
    this.dereferenceClient = dereferenceClient;

  }

  @Override
  public void dereference(RDF rdf) throws DereferenceException {
    // Extract fields from the RDF for dereferencing
    LOGGER.debug(" Extracting fields from RDF for dereferencing...");
    Set<String> resourceIds = extractReferencesForDereferencing(rdf);

    // Get the dereferenced information to add to the RDF using the extracted fields
    LOGGER.debug("Using extracted fields to gather enrichment-via-dereferencing information...");
    final List<EnrichmentResultBaseWrapper> dereferenceInformation = dereferenceEntities(resourceIds);

    // Merge the acquired information into the RDF
    LOGGER.debug("Merging Dereference Information...");
    entityMergeEngine.mergeEntities(rdf, dereferenceInformation, null);

    // Done.
    LOGGER.debug("Dereference completed.");

  }

  @Override
  public List<EnrichmentResultBaseWrapper> dereferenceEntities(Set<String> resourceIds)
      throws DereferenceException {

    // Sanity check.
    if (resourceIds.isEmpty()) {
      return Collections.emptyList();
    }

    // First try to get them from our own entity collection database.
    final List<EnrichmentResultBaseWrapper> result = new ArrayList<>(dereferenceOwnEntities(resourceIds));
    final List<List<EnrichmentBase>> listOfListEnrichmentBase = result.stream()
        .map(EnrichmentResultBaseWrapper::getEnrichmentBaseList)
        .collect(Collectors.toList());

    final Set<String> foundOwnEntityIds = listOfListEnrichmentBase.stream()
        .map(this::getAllAboutField)
        .flatMap(List::stream).collect(Collectors.toSet());

    // For the remaining ones, get them from the dereference service.
    for (String resourceId : resourceIds) {
      if (!foundOwnEntityIds.contains(resourceId)) {
        result.addAll(dereferenceExternalEntity(resourceId));
      }
    }

    // Done.
    return result;
  }

  private List<String> getAllAboutField(List<EnrichmentBase> listEnrichmentBase){
    return listEnrichmentBase.stream().map(EnrichmentBase::getAbout).collect(Collectors.toList());
  }


  private List<EnrichmentResultBaseWrapper> dereferenceOwnEntities(Set<String> resourceIds) throws DereferenceException {
    try {
      return retryableExternalRequestForNetworkExceptions(
          () -> enrichmentClient.getById(resourceIds));
    } catch (Exception e) {
      throw new DereferenceException(
          "Exception occurred while trying to perform dereferencing.", e);
    }
  }


  private List<EnrichmentResultBaseWrapper> dereferenceExternalEntity(String resourceId) throws DereferenceException {

    // Perform the dereferencing.
    EnrichmentResultList result;
    try {
      LOGGER.debug("== Processing {}", resourceId);
      result = retryableExternalRequestForNetworkExceptions(
          () -> dereferenceClient.dereference(resourceId));
    } catch (BadRequest e) {
      // We are forgiving for these errors
      LOGGER.warn("ResourceId {}, failed", resourceId, e);
      result = null;
    } catch (Exception e) {
      throw new DereferenceException(
          "Exception occurred while trying to perform dereferencing.", e);
    }

    // Return the result.
    return Optional.ofNullable(result).map(EnrichmentResultList::getEnrichmentBaseResultWrapperList)
        .orElseGet(Collections::emptyList);
  }

  @Override
  public Set<String> extractReferencesForDereferencing(RDF rdf) {
    return DereferenceUtils.extractReferencesForDereferencing(rdf);
  }
}
