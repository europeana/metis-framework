package eu.europeana.enrichment.rest.client.enrichment;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.europeana.enrichment.api.exceptions.UnknownException;
import eu.europeana.enrichment.api.external.model.*;
import eu.europeana.enrichment.api.internal.EntityResolver;
import eu.europeana.enrichment.api.internal.ReferenceTerm;
import eu.europeana.enrichment.api.internal.SearchTerm;
import eu.europeana.enrichment.utils.EntityResolverUtils;
import eu.europeana.entity.client.utils.EntityClientUtils;
import eu.europeana.entity.client.web.EntityClientApi;
import eu.europeana.entity.client.web.EntityClientApiImpl;

import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * An entity resolver that works by accessing a service via Entity Client API and obtains entities from
 * Entity Management API
 */
public class EntityClientResolver implements EntityResolver {

    private EntityClientApi entityClientApi;
    private final RestTemplate template;
    private final int batchSize;


    public EntityClientResolver(RestTemplate template, int batchSize) {
        this.template = template;
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

    private <I> Map<I, List<EnrichmentBase>> performInBatches(Set<I> inputValues, Function<I, EntityClientRequest> identity) {
        final Map<I, List<EnrichmentBase>> result = new HashMap<>();
        // create partitions
        final List<List<I>> partitions = EntityResolverUtils.createPartition(inputValues, batchSize);

        // Process partitions
        for (List<I> partition : partitions) {
            for (I input: partition) {
                EntityClientRequest clientRequest = identity.apply(input);
                List<Entity> entities = executeEntityClient(clientRequest);
                if(entities.size() > 0) {
                    // convert entity to EnrichmentBase
                    List<EnrichmentBase> enrichmentBaseList = entities.stream().map(entity -> {
                        EnrichmentBase enrichmentBase = createXmlEntity(entity);
                        return enrichmentBase;
                    }).collect(Collectors.toList());
                    EntityResolverUtils.failSafeCheck(entities.size(), enrichmentBaseList.size(), "Mismatch while converting the EM class to EnrichmentBase.");
                    System.out.println(enrichmentBaseList.size());
                    result.put(input, enrichmentBaseList);
                }
            }
        }
        return result;
    }

    private List<Entity> executeEntityClient(EntityClientRequest entityClientRequest) {
        try {
              if (entityClientRequest.isReference()) {
                  if (entityClientRequest.getValueToEnrich().startsWith(EntityClientUtils.BASE_URL)) {
                     return Collections.singletonList(entityClientApi.getEntityById(entityClientRequest.getValueToEnrich()));
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
