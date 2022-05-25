package eu.europeana.enrichment.service;

import eu.europeana.enrichment.api.external.ReferenceValue;
import eu.europeana.enrichment.api.external.SearchValue;
import eu.europeana.enrichment.api.external.impl.EntityClientResolver;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentResultBaseWrapper;
import eu.europeana.enrichment.api.internal.ReferenceTerm;
import eu.europeana.enrichment.api.internal.ReferenceTermImpl;
import eu.europeana.enrichment.api.internal.SearchTerm;
import eu.europeana.enrichment.api.internal.SearchTermImpl;
import eu.europeana.enrichment.internal.model.OrganizationEnrichmentEntity;
import eu.europeana.enrichment.service.dao.EnrichmentDao;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Contains functionality for accessing entities from the enrichment database using {@link
 * EnrichmentDao}.
 *
 * @author Simon Tzanakis
 * @since 2020-07-16
 */
@Service
public class EnrichmentService {
  private static final Logger LOGGER = LoggerFactory.getLogger(EnrichmentService.class);

  private final PersistentEntityResolver persistentEntityResolver;
  private final EntityClientResolver entityClientResolver;

  /**
   * Parameter constructor.
   *
   * @param persistentEntityResolver the entity resolver
   * @param entityClientResolver the ebt5ty client resolver
   */
  @Autowired
  public EnrichmentService(PersistentEntityResolver persistentEntityResolver, EntityClientResolver entityClientResolver) {
    this.persistentEntityResolver = persistentEntityResolver;
    this.entityClientResolver = entityClientResolver;
  }

  /**
   * Get an enrichment by providing a list of {@link SearchValue}s.
   *
   * @param searchValues a list of structured search values with parameters
   * @return the enrichment values in a structured list
   */
  public List<EnrichmentResultBaseWrapper> enrichByEnrichmentSearchValues(
      List<SearchValue> searchValues) {
    final List<SearchTerm> orderedSearchTerms = searchValues.stream().map(
            search -> new SearchTermImpl(search.getValue(), search.getLanguage(),
                    Set.copyOf(search.getEntityTypes()))).collect(Collectors.toList());
    final Map<SearchTerm, List<EnrichmentBase>> result = entityClientResolver
            .resolveByText(new HashSet<>(orderedSearchTerms));
    return orderedSearchTerms.stream().map(searchTerm -> result.getOrDefault(searchTerm, Collections.emptyList())).map(EnrichmentResultBaseWrapper::new)
              .collect(Collectors.toList());
  }

  /**
   * Get an enrichment by providing a URI, might match owl:sameAs.
   *
   * @param referenceValue The URI to check for match
   * @return the structured result of the enrichment
   */
  public List<EnrichmentBase> enrichByEquivalenceValues(ReferenceValue referenceValue) {
    try {
      final ReferenceTerm referenceTerm = new ReferenceTermImpl(
              new URL(referenceValue.getReference()), Set.copyOf(referenceValue.getEntityTypes()));
      return entityClientResolver.resolveByUri(Set.of(referenceTerm))
              .getOrDefault(referenceTerm, Collections.emptyList());
    } catch (MalformedURLException e) {
      LOGGER.debug("There was a problem converting the input to ReferenceTermType");
      throw new IllegalArgumentException("The input values are invalid", e);
    }
  }

  /**
   * Get an enrichment by providing a URI.
   *
   * @param entityAbout The URI to check for match
   * @return the structured result of the enrichment
   */
  public EnrichmentBase enrichById(String entityAbout) {
    try {
      final ReferenceTerm referenceTerm = new ReferenceTermImpl(new URL(entityAbout),
              new HashSet<>());
      return entityClientResolver.resolveById(Set.of(referenceTerm)).get(referenceTerm);
    } catch (MalformedURLException e) {
      LOGGER.debug("There was a problem converting the input to ReferenceTermType");
      throw new IllegalArgumentException("The input values are invalid", e);
    }
  }

  /* --- Organization specific methods, used by the annotations api --- */

  /**
   * Save an organization to the database
   *
   * @param organizationEnrichmentEntity the organization to save
   * @param created the created date to be used
   * @param updated the updated date to be used
   * @return the saved organization
   */
  public OrganizationEnrichmentEntity saveOrganization(
      OrganizationEnrichmentEntity organizationEnrichmentEntity, Date created, Date updated) {
    return persistentEntityResolver.saveOrganization(organizationEnrichmentEntity, created, updated);
  }

  /**
   * Return the list of ids for existing organizations from database
   *
   * @param organizationIds The organization ids to check existence
   * @return list of ids of existing organizations
   */
  public List<String> findExistingOrganizations(List<String> organizationIds) {
   return persistentEntityResolver.findExistingOrganizations(organizationIds);
  }

  /**
   * Get an organization by uri
   *
   * @param uri The EDM organization uri
   * @return OrganizationImpl object
   */
  public Optional<OrganizationEnrichmentEntity> getOrganizationByUri(String uri) {
   return persistentEntityResolver.getOrganizationByUri(uri);
  }

  /**
   * Delete organizations from database by given organization ids
   *
   * @param organizationIds The organization ids
   */
  public void deleteOrganizations(List<String> organizationIds) {
    persistentEntityResolver.deleteOrganizations(organizationIds);
  }

  /**
   * This method removes organization from database by given organization id.
   *
   * @param organizationId The organization id
   */
  public void deleteOrganization(String organizationId) {
    persistentEntityResolver.deleteOrganization(organizationId);
  }

  /**
   * Get the date of the latest updated organization.
   *
   * @return the date of the latest updated organization
   */
  public Date getDateOfLastUpdatedOrganization() {
    return persistentEntityResolver.getDateOfLastUpdatedOrganization();
  }

}
