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
        return EntityResolverUtils.getIdResults(performInBatches(referenceTerms,inputFunction));
    }

    @Override
    public <T extends ReferenceTerm> Map<T, List<EnrichmentBase>> resolveByUri(Set<T> referenceTerms) {
        final Function<T, EntityClientRequest> inputFunction = EntityResolverUtils.inputFunctionForRefSearch();
        return performInBatches(referenceTerms, inputFunction);
    }


    /**
     * Gets the Enrichment for single Entity request
     * @param clientRequest
     * @return
     */
    public List<EnrichmentBase> getEnrichment(EntityClientRequest clientRequest) {
        return executeEntityClientRequest(clientRequest);
    }

    /**
     * Gets the Enrichment for Multiple values in batches
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
            for (I input: partition) {
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
        List<Entity> entities = getEntities(clientRequest);
        if (!entities.isEmpty()) {
            // convert entity to EnrichmentBase
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
                  if (entityClientRequest.getValueToEnrich().startsWith(EntityApiConstants.BASE_URL)) {
                      Entity entity = entityClientApi.getEntityById(entityClientRequest.getValueToEnrich());
                      if (entity != null) {
                          return Collections.singletonList(entityClientApi.getEntityById(entityClientRequest.getValueToEnrich()));
                      }
                  } else {
                      return  entityClientApi.getEntityByUri(entityClientRequest.getValueToEnrich());
                  }
              } else {
                  return entityClientApi.getSuggestions(entityClientRequest.getValueToEnrich(), entityClientRequest.getLanguage(), null, entityClientRequest.getType(), null, null);
              }
        } catch (JsonProcessingException e) {
            throw new UnknownException("Entity Client GET call to " + (entityClientRequest.isReference() ? "getEntityByUri" : "getSuggestions")
                   + " failed for" + entityClientRequest + ".", e);
        }
        catch (RuntimeException e) {
            throw new UnknownException("Entity Client GET call failed for : "  + entityClientRequest + ".", e);
        }
        return Collections.emptyList();
    }

    /**
     * Converts the EM model class to Metis XML Entity class
     * @param entity EM Entity
     * @param <T> class that extends EnrichmentBase
     * @return
     */
    private static <T extends EnrichmentBase> T createXmlEntity(Entity entity) {
        switch (EntityTypes.valueOf(entity.getType())) {
            case Agent:
                return (T)new Agent((eu.europeana.entitymanagement.definitions.model.Agent)entity);
            case Place:
                return (T)new Place((eu.europeana.entitymanagement.definitions.model.Place) entity);
            case Concept:
                return (T)new Concept((eu.europeana.entitymanagement.definitions.model.Concept) entity);
            case TimeSpan:
                return (T)new TimeSpan((eu.europeana.entitymanagement.definitions.model.TimeSpan) entity);
            case Organization:
                return (T)new Organization((eu.europeana.entitymanagement.definitions.model.Organization) entity);
        }
        return null;
    }
}
