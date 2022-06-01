package eu.europeana.enrichment.api.external.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.europeana.enrichment.api.exceptions.UnknownException;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentQuery;
import eu.europeana.enrichment.api.internal.EntityResolver;
import eu.europeana.enrichment.api.internal.ReferenceTerm;
import eu.europeana.enrichment.api.internal.SearchTerm;
import eu.europeana.enrichment.utils.EntityResolverUtils;
import eu.europeana.entity.client.utils.EntityApiConstants;
import eu.europeana.entity.client.web.EntityClientApi;
import eu.europeana.entity.client.web.EntityClientApiImpl;
import eu.europeana.entitymanagement.definitions.model.Entity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * An entity resolver that works by accessing a service via Entity Client API and obtains entities from Entity Management API
 *
 * @author Srishti.singh@europeana.eu
 */
public class ClientEntityResolver implements EntityResolver {

    private final EntityClientApi entityClientApi;
    private final int batchSize;


    public ClientEntityResolver(int batchSize) {
        this.batchSize = batchSize;
        entityClientApi = new EntityClientApiImpl();
    }

    @Override
    public <T extends SearchTerm> Map<T, List<EnrichmentBase>> resolveByText(Set<T> searchTerms) {
        final Function<T, EnrichmentQuery> inputFunction = EntityResolverUtils.createEnrichmentQueryForTextSearch();
        return performInBatches(searchTerms, inputFunction);
    }

    @Override
    public <T extends ReferenceTerm> Map<T, EnrichmentBase> resolveById(Set<T> referenceTerms) {
        final Function<T, EnrichmentQuery> inputFunction = EntityResolverUtils.createEnrichmentQueryForRefSearch();
        return EntityResolverUtils.pickFirstFromTheList(performInBatches(referenceTerms, inputFunction));
    }

    @Override
    public <T extends ReferenceTerm> Map<T, List<EnrichmentBase>> resolveByUri(Set<T> referenceTerms) {
        final Function<T, EnrichmentQuery> inputFunction = EntityResolverUtils.createEnrichmentQueryForRefSearch();
        return performInBatches(referenceTerms, inputFunction);
    }

    /**
     * Gets the Enrichment for Multiple input values in batches of batchSize
     *
     * @param inputValues Set of values for which enrichment will be performed
     * @param requestParser Function to return EntityClientRequest from input value <I>
     * @param <I> Input value Type. Could be <T extends SearchTerm> OR <T extends ReferenceTerm>
     * @return
     */
    private <I> Map<I, List<EnrichmentBase>> performInBatches(Set<I> inputValues, Function<I, EnrichmentQuery> requestParser) {
        final Map<I, List<EnrichmentBase>> result = new HashMap<>();
        // create partitions
        final List<List<I>> partitions = EntityResolverUtils.createBatch(inputValues, batchSize);

        // Process partitions
        for (List<I> partition : partitions) {
            for (I input : partition) {
                EnrichmentQuery clientRequest = requestParser.apply(input);
                List<EnrichmentBase> enrichmentBaseList = executeEntityClientRequest(clientRequest);
                if (!enrichmentBaseList.isEmpty()) {
                    result.put(input, enrichmentBaseList);
                }
            }
        }
        return result;
    }

    private List<EnrichmentBase> executeEntityClientRequest(EnrichmentQuery clientRequest) {
        // 1. get Entities
        List<Entity> entities = getEntities(clientRequest);
        if (!entities.isEmpty()) {
            // 2. get the parent entities
            List<Entity> parentEntities = new ArrayList<>();
            entities.stream().forEach(entity -> {
                if (entity.getIsPartOfArray() != null && EntityResolverUtils.isTextOrUriSearch(clientRequest)) {
                    fetchParentEntities(entity, parentEntities);
                }
            });
            // 3. add the parent entities
            entities.addAll(parentEntities);
            // 4. convert entity to EnrichmentBase
            List<EnrichmentBase> enrichmentBaseList = convertToEnrichmentBase(entities);

            EntityResolverUtils.failSafeCheck(entities.size(), enrichmentBaseList.size(), "Mismatch while converting the EM class to EnrichmentBase.");
            return enrichmentBaseList;
        }
        return Collections.emptyList();
    }


    private List<Entity> getEntities(EnrichmentQuery entityClientRequest) {
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

    private List<Entity> resolveReferences(EnrichmentQuery entityClientRequest) {
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
    private void fetchParentEntities(Entity entity, List<Entity> parentEntities) {
        entity.getIsPartOfArray().stream().forEach(parentEntityId -> {
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
        if (parentEntity != null && parentEntity.getIsPartOfArray() != null)
            fetchParentEntities(parentEntity, parentEntities);
    }

    /**
     * Converts the EM Entity model to Enrichment base.
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
