package eu.europeana.enrichment.api.external.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.europeana.enrichment.api.exceptions.UnknownException;
import eu.europeana.enrichment.api.external.model.*;
import eu.europeana.enrichment.api.internal.EntityResolver;
import eu.europeana.enrichment.api.internal.ReferenceTerm;
import eu.europeana.enrichment.api.internal.SearchTerm;
import eu.europeana.enrichment.utils.EntityResolverUtils;
import eu.europeana.entity.client.utils.EntityApiConstants;
import eu.europeana.entity.client.web.EntityClientApi;
import eu.europeana.entity.client.web.EntityClientApiImpl;

import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * An entity resolver that works by accessing a service via Entity Client API and obtains entities from
 * Entity Management API
 *
 * @author Srishti.singh@europeana.eu
 */
public class EntityClientResolver implements EntityResolver {

    private EntityClientApi entityClientApi;
    private final int batchSize;


    public EntityClientResolver(int batchSize) {
        this.batchSize = batchSize;
        entityClientApi = new EntityClientApiImpl();
    }

    @Override
    public <T extends SearchTerm> Map<T, List<EnrichmentBase>> resolveByText(Set<T> searchTerms) {
        final Function<T, EntityClientRequest> inputFunction = EntityResolverUtils.inputFunctionForTextSearch();
        return performInBatches(searchTerms, inputFunction);
    }

    @Override
    public <T extends ReferenceTerm> Map<T, EnrichmentBase> resolveById(Set<T> referenceTerms) {
        final Function<T, EntityClientRequest> inputFunction = EntityResolverUtils.inputFunctionForRefSearch();
        return EntityResolverUtils.getIdResults(performInBatches(referenceTerms, inputFunction));
    }

    @Override
    public <T extends ReferenceTerm> Map<T, List<EnrichmentBase>> resolveByUri(Set<T> referenceTerms) {
        final Function<T, EntityClientRequest> inputFunction = EntityResolverUtils.inputFunctionForRefSearch();
        return performInBatches(referenceTerms, inputFunction);
    }

    /**
     * Gets the Enrichment for Multiple values in batches
     *
     * @param inputValues
     * @param identity
     * @param <I>
     * @return
     */
    private <I> Map<I, List<EnrichmentBase>> performInBatches(Set<I> inputValues, Function<I, EntityClientRequest> identity) {
        final Map<I, List<EnrichmentBase>> result = new HashMap<>();
        // create partitions
        final List<List<I>> partitions = EntityResolverUtils.createPartition(inputValues, batchSize);

        // Process partitions
        for (List<I> partition : partitions) {
            for (I input : partition) {
                EntityClientRequest clientRequest = identity.apply(input);
                List<EnrichmentBase> enrichmentBaseList = executeEntityClientRequest(clientRequest);
                if (!enrichmentBaseList.isEmpty()) {
                    result.put(input, enrichmentBaseList);
                }
            }
        }
        return result;
    }

    private List<EnrichmentBase> executeEntityClientRequest(EntityClientRequest clientRequest) {
        // 1. get Entities
        List<Entity> entities = getEntities(clientRequest);
        if (!entities.isEmpty()) {
            // 2. get the parent entities
            List<Entity> parentEntities = new ArrayList<>();
            entities.stream().forEach(entity -> getParentEntities(entity, parentEntities));
            // 3. add the parent entities
            entities.addAll(parentEntities);
            // 4. convert entity to EnrichmentBase
            List<EnrichmentBase> enrichmentBaseList = entities.stream().map(entity -> {
                EnrichmentBase enrichmentBase = createXmlEntity(entity);
                return enrichmentBase;
            }).collect(Collectors.toList());
            EntityResolverUtils.failSafeCheck(entities.size(), enrichmentBaseList.size(), "Mismatch while converting the EM class to EnrichmentBase.");
            return enrichmentBaseList;
        }
        return Collections.emptyList();
    }


    private List<Entity> getEntities(EntityClientRequest entityClientRequest) {
        try {
            if (entityClientRequest.isReference()) {
                return resolveReferences(entityClientRequest);
            } else {
                // if the enrich method returns more than 1 entity, then no enrichment should be (for now) considered.
                // It works as if no entity was returned. The reason for this is that Metis does not have a disambiguation
                // mechanism in place that can judge which entity should be chosen out of the list of options.
                List<Entity> entities = entityClientApi.getEnrichment(entityClientRequest.getValueToEnrich(), entityClientRequest.getLanguage(), entityClientRequest.getType(), null) ;
                if (entities.size() == 1) {
                    return entities;
                }
            }
        } catch (JsonProcessingException e) {
            throw new UnknownException("Entity Client call to " + (entityClientRequest.isReference() ? "getEntityByUri" : "getSuggestions")
                    + " failed for" + entityClientRequest + ".", e);
        } catch (RuntimeException e) {
            throw new UnknownException("Entity Client call failed for : " + entityClientRequest + ".", e);
        }
        return Collections.emptyList();
    }

    private List<Entity> resolveReferences(EntityClientRequest entityClientRequest) {
        if (entityClientRequest.getValueToEnrich().startsWith(EntityApiConstants.BASE_URL)) {
            Entity entity = entityClientApi.getEntityById(entityClientRequest.getValueToEnrich());
            if (entity != null) {
                // create a mutable list, as we might add parent entities later
                return new ArrayList<>(Arrays.asList(entity));
            }
        } else {
            return entityClientApi.getEntityByUri(entityClientRequest.getValueToEnrich());
        }
        return Collections.emptyList();
    }

    /**
     * Adds the parent Entities to parentEntities list
     *
     * @param entity
     * @param parentEntities
     */
    private void getParentEntities(Entity entity, List<Entity> parentEntities) {
        if (EntityResolverUtils.isParentEntityRequired(entity) && entity.getIsPartOfArray() != null) {
            entity.getIsPartOfArray().stream().forEach(parentEntityId -> {
                if (!EntityResolverUtils.checkIfEntityAlreadyExists(parentEntityId, parentEntities)) {
                    getParent(parentEntityId, parentEntities);
                }
            });
        }
    }

    private void getParent(String parentEntityId, List<Entity> parentEntities) {
        Entity parentEntity = entityClientApi.getEntityById(parentEntityId);
        // parent entity should never be null here, but just in case
        if (parentEntity != null) {
            parentEntities.add(parentEntity);
        }
        if (parentEntity != null && parentEntity.getIsPartOfArray() != null)
            getParentEntities(parentEntity, parentEntities);
    }

    /**
     * Converts the EM model class to Metis XML Entity class
     *
     * @param entity EM Entity
     * @param <T>    class that extends EnrichmentBase
     * @return
     */
    private static <T extends EnrichmentBase> T createXmlEntity(Entity entity) {
        switch (EntityTypes.valueOf(entity.getType())) {
            case Agent:
                return (T) new Agent((eu.europeana.entitymanagement.definitions.model.Agent) entity);
            case Place:
                return (T) new Place((eu.europeana.entitymanagement.definitions.model.Place) entity);
            case Concept:
                return (T) new Concept((eu.europeana.entitymanagement.definitions.model.Concept) entity);
            case TimeSpan:
                return (T) new TimeSpan((eu.europeana.entitymanagement.definitions.model.TimeSpan) entity);
            case Organization:
                return (T) new Organization((eu.europeana.entitymanagement.definitions.model.Organization) entity);
        }
        return null;
    }
}
