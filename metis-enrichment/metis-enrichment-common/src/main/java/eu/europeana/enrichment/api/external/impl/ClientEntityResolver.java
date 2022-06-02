package eu.europeana.enrichment.api.external.impl;

import static java.lang.String.format;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.europeana.enrichment.api.exceptions.UnknownException;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.internal.EntityResolver;
import eu.europeana.enrichment.api.internal.ReferenceTerm;
import eu.europeana.enrichment.api.internal.SearchTerm;
import eu.europeana.enrichment.utils.EntityResolverUtils;
import eu.europeana.entity.client.utils.EntityApiConstants;
import eu.europeana.entity.client.web.EntityClientApi;
import eu.europeana.entity.client.web.EntityClientApiImpl;
import eu.europeana.entitymanagement.definitions.model.Entity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An entity resolver that works by accessing a service via Entity Client API and obtains entities from Entity Management API
 *
 * @author Srishti.singh@europeana.eu
 */
public class ClientEntityResolver implements EntityResolver {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClientEntityResolver.class);

  private final EntityClientApi entityClientApi;
  private final int batchSize;


  public ClientEntityResolver(int batchSize) {
    this.batchSize = batchSize;
    entityClientApi = new EntityClientApiImpl();
  }

  @Override
  public <T extends SearchTerm> Map<T, List<EnrichmentBase>> resolveByText(Set<T> searchTerms) {
    return performInBatches(searchTerms);
  }

  @Override
  public <T extends ReferenceTerm> Map<T, EnrichmentBase> resolveById(Set<T> referenceTerms) {
    return EntityResolverUtils.pickFirstFromTheList(performInBatches(referenceTerms));
  }

  @Override
  public <T extends ReferenceTerm> Map<T, List<EnrichmentBase>> resolveByUri(Set<T> referenceTerms) {
    return performInBatches(referenceTerms);
  }

  /**
   * Gets the Enrichment for Multiple input values in batches of batchSize
   *
   * @param inputValues Set of values for which enrichment will be performed
   * @param requestParser Function to return EntityClientRequest from input value <I>
   * @param <I> Input value Type. Could be <T extends SearchTerm> OR <T extends ReferenceTerm>
   * @return
   */
  private <I> Map<I, List<EnrichmentBase>> performInBatches(Set<I> inputValues) {
    final Map<I, List<EnrichmentBase>> result = new HashMap<>();
    // create batches
    final List<List<I>> batches = EntityResolverUtils.createBatch(inputValues, batchSize);

    // Process batches
    for (List<I> batch : batches) {
      for (I batchItem : batch) {
        List<EnrichmentBase> enrichmentBaseList = executeEntityClientRequest(batchItem);
        if (!enrichmentBaseList.isEmpty()) {
          result.put(batchItem, enrichmentBaseList);
        }
      }
    }
    return result;
  }

  private <I> List<EnrichmentBase> executeEntityClientRequest(I batchItem) {
    // 1. get Entities
    List<Entity> entities = getEntities(batchItem);
    if (isNotEmpty(entities)) {
      // 2. get the parent entities
      List<Entity> parentEntities = new ArrayList<>();
      entities.forEach(entity -> {
        if (entity.getIsPartOfArray() != null && EntityResolverUtils.isSearchTermOrReferenceThatIsNotAEuropeanaEntity(
            batchItem)) {
          fetchParentEntities(entity, parentEntities);
        }
      });
      // 3. add the parent entities
      entities.addAll(parentEntities);
      // 4. convert entity to EnrichmentBase
      List<EnrichmentBase> enrichmentBaseList = convertToEnrichmentBase(entities);

      EntityResolverUtils.failSafeCheck(entities.size(), enrichmentBaseList.size(),
          "Mismatch while converting the EM class to EnrichmentBase.");
      return enrichmentBaseList;
    }
    return Collections.emptyList();
  }

  private <I> List<Entity> getEntities(I batchItem) {
    if (batchItem instanceof ReferenceTerm) {
      return resolveReferences((ReferenceTerm) batchItem);
    } else {
      return resolveTextSearch((SearchTerm) batchItem);
    }
  }

  private List<Entity> resolveTextSearch(SearchTerm searchTerm) {
    final String entityTypesConcatenated = searchTerm.getCandidateTypes().stream()
                                                     .map(entityType -> entityType.name().toLowerCase())
                                                     .collect(Collectors.joining(","));
    final List<Entity> entities;
    try {
      entities = entityClientApi.getEnrichment(searchTerm.getTextValue(), searchTerm.getLanguage(),
          entityTypesConcatenated, null);
    } catch (JsonProcessingException e) {
      // TODO: 02/06/2022 Check if exception should be thrown or bypassed
      throw new UnknownException(
          format("SearchTerm request failed for textValue: %s, language: %s, entityTypes: %s.", searchTerm.getTextValue(),
              searchTerm.getLanguage(), entityTypesConcatenated), e);
    }
    // TODO: 02/06/2022 This check is valid if the entities that are returned do not contain any parent entities.
    // According to the intended implementation the parents are fetched here(remotely).
    // If the parent fetching is moved to the Entity API application then this check will not be valid anymore.

    // if the enrich method returns more than 1 entity, then no enrichment should be (for now) considered.
    // It works as if no entity was returned. The reason for this is that Metis does not have a disambiguation
    // mechanism in place that can judge which entity should be chosen out of the list of options.
    if (entities.size() == 1) {
      return entities;
    }
    return Collections.emptyList();
  }

  private List<Entity> resolveReferences(ReferenceTerm referenceTerm) {
    final String referenceValue = referenceTerm.getReference().toString();
    if (referenceValue.startsWith(EntityApiConstants.BASE_URL)) {
      Entity entity = entityClientApi.getEntityById(referenceValue);
      if (entity != null) {
        // create a mutable list, as we might add parent entities later
        return new ArrayList<>(List.of(entity));
      }
    } else {
      return entityClientApi.getEntityByUri(referenceValue);
    }
    return Collections.emptyList();
  }

  /**
   * Adds the parent Entities to parentEntities list
   *
   * @param entity
   * @param parentEntities
   */
  private void fetchParentEntities(Entity entity, List<Entity> parentEntities) {
    entity.getIsPartOfArray().forEach(parentEntityId -> {
      if (!EntityResolverUtils.checkIfEntityAlreadyExists(parentEntityId, parentEntities)) {
        appendParent(parentEntityId, parentEntities);
      }
    });
  }

  private void appendParent(String parentEntityId, List<Entity> parentEntities) {
    Entity parentEntity = entityClientApi.getEntityById(parentEntityId);
    // parent entity should never be null here, but just in case
    if (parentEntity != null) {
      parentEntities.add(parentEntity);
    }
    if (parentEntity != null && parentEntity.getIsPartOfArray() != null) {
      fetchParentEntities(parentEntity, parentEntities);
    }
  }

  /**
   * Converts the EM Entity model to Enrichment base.
   *
   * @param entities
   * @return
   */
  private List<EnrichmentBase> convertToEnrichmentBase(List<Entity> entities) {
    return entities.stream().map(entity -> {
      EnrichmentBase enrichmentBase = EntityResolverUtils.convertEntityToEnrichmentBase(entity);
      return enrichmentBase;
    }).collect(Collectors.toList());
  }
}
