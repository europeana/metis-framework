package eu.europeana.enrichment.utils;

import eu.europeana.enrichment.api.external.model.Agent;
import eu.europeana.enrichment.api.external.model.Concept;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.Organization;
import eu.europeana.enrichment.api.external.model.Place;
import eu.europeana.enrichment.api.external.model.TimeSpan;
import eu.europeana.enrichment.api.internal.ReferenceTerm;
import eu.europeana.enrichment.api.internal.SearchTerm;
import eu.europeana.entity.client.utils.EntityApiConstants;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * EntityClientResolver util class
 *
 * @author Srishti.singh@europeana.eu
 */
public final class EntityResolverUtils {

  private EntityResolverUtils() {
  }

  public static <I> List<List<I>> splitInBatches(Set<I> inputValues, int batchSize) {
    final List<List<I>> batch = new ArrayList<>();
    batch.add(new ArrayList<>());
    inputValues.forEach(item -> {
      List<I> currentPartition = batch.get(batch.size() - 1);
      if (currentPartition.size() >= batchSize) {
        currentPartition = new ArrayList<>();
        batch.add(currentPartition);
      }
      currentPartition.add(item);
    });
    return batch;
  }

  /**
   * Returns the first Entity from the List<EnrichmentBase> for each input value
   *
   * @param results
   * @param <T>
   * @return
   */
  public static <T> Map<T, EnrichmentBase> pickFirstFromTheList(Map<T, List<EnrichmentBase>> results) {
    Map<T, EnrichmentBase> finalValues = new HashMap<>();
    results.forEach((key, value) -> finalValues.put(key, value.stream().findFirst().orElse(null)));
    return finalValues;
  }

  /**
   * Converts the EM model class to Metis EnrichmentBase
   *
   * @param entity EM Entity
   * @param <T> class that extends EnrichmentBase
   * @return
   */
  public static EnrichmentBase convertEntityToEnrichmentBase(Entity entity) {
    switch (EntityTypes.valueOf(entity.getType())) {
      case Agent:
        return new Agent((eu.europeana.entitymanagement.definitions.model.Agent) entity);
      case Place:
        return new Place((eu.europeana.entitymanagement.definitions.model.Place) entity);
      case Concept:
        return new Concept((eu.europeana.entitymanagement.definitions.model.Concept) entity);
      case TimeSpan:
        return new TimeSpan((eu.europeana.entitymanagement.definitions.model.TimeSpan) entity);
      case Organization:
        return new Organization((eu.europeana.entitymanagement.definitions.model.Organization) entity);
    }
    return null;
  }

  /**
   * Returns true if entity already exists in parentEntities list
   *
   * @param entityIdToCheck id to check
   * @param entities entity list
   * @return
   */
  public static boolean checkIfEntityAlreadyExists(String entityIdToCheck, List<Entity> entities) {
    return entities.stream().anyMatch(entity -> entity.getEntityId().equals(entityIdToCheck));
  }

  public static <I> boolean isSearchTermOrReferenceThatIsNotAEuropeanaEntity(I batchItem) {
    return batchItem instanceof SearchTerm || !((ReferenceTerm) batchItem).getReference().toString()
                                                                          .startsWith(EntityApiConstants.BASE_URL);
  }
}
