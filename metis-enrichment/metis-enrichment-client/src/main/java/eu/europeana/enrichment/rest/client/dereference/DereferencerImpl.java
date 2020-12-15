package eu.europeana.enrichment.rest.client.dereference;

import static eu.europeana.metis.network.ExternalRequestUtil.retryableExternalRequestForNetworkExceptions;

import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentResultBaseWrapper;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.enrichment.api.internal.EntityResolver;
import eu.europeana.enrichment.api.internal.ReferenceTerm;
import eu.europeana.enrichment.api.internal.ReferenceTermImpl;
import eu.europeana.enrichment.rest.client.exceptions.DereferenceException;
import eu.europeana.enrichment.utils.DereferenceUtils;
import eu.europeana.enrichment.utils.EntityMergeEngine;
import eu.europeana.metis.schema.jibx.RDF;
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
import org.springframework.web.client.HttpClientErrorException.BadRequest;

public class DereferencerImpl implements Dereferencer {

  private static final Logger LOGGER = LoggerFactory.getLogger(DereferencerImpl.class);

  private final EntityMergeEngine entityMergeEngine;
  private final EntityResolver remoteEntityResolver;
  private final DereferenceClient dereferenceClient;

  public DereferencerImpl(EntityMergeEngine entityMergeEngine, EntityResolver remoteEntityResolver,
          DereferenceClient dereferenceClient) {
    this.entityMergeEngine = entityMergeEngine;
    this.remoteEntityResolver = remoteEntityResolver;
    this.dereferenceClient = dereferenceClient;
  }

  @Override
  public void dereference(RDF rdf) throws DereferenceException {
    // Extract fields from the RDF for dereferencing
    LOGGER.debug(" Extracting fields from RDF for dereferencing...");
    Set<String> resourceIds = extractReferencesForDereferencing(rdf);

    // Get the dereferenced information to add to the RDF using the extracted fields
    LOGGER.debug("Using extracted fields to gather enrichment-via-dereferencing information...");
    final List<EnrichmentBase> dereferenceInformation = dereferenceEntities(resourceIds);

    // Merge the acquired information into the RDF
    LOGGER.debug("Merging Dereference Information...");
    entityMergeEngine.mergeEntities(rdf, dereferenceInformation, Collections.emptySet());

    // Done.
    LOGGER.debug("Dereference completed.");

  }

  @Override
  public List<EnrichmentBase> dereferenceEntities(Set<String> resourceIds)
      throws DereferenceException {

    // Sanity check.
    if (resourceIds.isEmpty()) {
      return Collections.emptyList();
    }

    // First try to get them from our own entity collection database.
    Set<ReferenceTerm> referenceTermSet = new HashSet<>();

    for(String id : resourceIds){
      final ReferenceTerm referenceTerm;
      try {
        referenceTerm = new ReferenceTermImpl(new URL(id), new HashSet<>());
      } catch (MalformedURLException e) {
        LOGGER.debug("There is a problem with the input values");
        throw new DereferenceException("There was some problem with the input values while dereferencing", e);
      }

      referenceTermSet.add(referenceTerm);
    }

    final List<EnrichmentBase> result = new ArrayList<>(dereferenceOwnEntities(referenceTermSet));

    final Set<String> foundOwnEntityIds = result.stream().map(EnrichmentBase::getAbout)
        .collect(Collectors.toSet());

    // For the remaining ones, get them from the dereference service.
    for (ReferenceTerm resourceId : referenceTermSet) {
      if (!foundOwnEntityIds.contains(resourceId.getReference().toString())) {
        result.addAll(dereferenceExternalEntity(resourceId.getReference().toString()));
      }
    }
    // Done.
    return result;

  }

  private List<EnrichmentBase> dereferenceOwnEntities(Set<ReferenceTerm> resourceIds)
      throws DereferenceException {
    try {
      return new ArrayList<>(retryableExternalRequestForNetworkExceptions(
          () -> remoteEntityResolver.resolveById(resourceIds)).values());
    } catch (Exception e) {
      throw new DereferenceException("Exception occurred while trying to perform dereferencing.",
          e);
    }
  }


  private List<EnrichmentBase> dereferenceExternalEntity(String resourceId)
      throws DereferenceException {

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
      throw new DereferenceException("Exception occurred while trying to perform dereferencing.",
          e);
    }

    // Return the result.
    return Optional.ofNullable(result).map(EnrichmentResultList::getEnrichmentBaseResultWrapperList)
        .orElseGet(Collections::emptyList).stream()
        .map(EnrichmentResultBaseWrapper::getEnrichmentBaseList).filter(Objects::nonNull)
        .flatMap(List::stream).collect(Collectors.toList());
  }

  @Override
  public Set<String> extractReferencesForDereferencing(RDF rdf) {
    return DereferenceUtils.extractReferencesForDereferencing(rdf);
  }
}
