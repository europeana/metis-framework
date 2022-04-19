package eu.europeana.enrichment.utils;

import eu.europeana.enrichment.api.exceptions.UnknownException;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EntityClientRequest;
import eu.europeana.enrichment.api.internal.ReferenceTerm;
import eu.europeana.enrichment.api.internal.SearchTerm;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.vocabulary.EntityTypes;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * EntityClientResolver util class
 *
 * @author Srishti.singh@europeana.eu
 */
public class EntityResolverUtils {

    private EntityResolverUtils() {
    }

    public static <I> List<List<I>> createPartition(Set<I> inputValues, int batchSize) {
        // Create partitions - batches of 20
        final List<List<I>> partitions = new ArrayList<>();
        partitions.add(new ArrayList<>());
        inputValues.forEach(item -> {
            List<I> currentPartition = partitions.get(partitions.size() - 1);
            if (currentPartition.size() >= batchSize) {
                currentPartition = new ArrayList<>();
                partitions.add(currentPartition);
            }
            currentPartition.add(item);
        });
        return partitions;
    }

    public static <T extends SearchTerm> Function<T, EntityClientRequest> inputFunctionForTextSearch() {
        return term -> new EntityClientRequest(term.getTextValue(), term.getLanguage(),
                term.getCandidateTypes()
                        .stream()
                        .map(entityType -> entityType.name().toLowerCase())
                        .collect(Collectors.joining(",")), false);
    }

    public static <T extends ReferenceTerm> Function<T, EntityClientRequest> inputFunctionForRefSearch() {
        return term -> new EntityClientRequest(term.getReference().toString(),
                term.getCandidateTypes()
                        .stream()
                        .map(entityType -> entityType.name().toLowerCase())
                        .collect(Collectors.joining(",")), true);
    }

    public static <T>  Map<T, EnrichmentBase> getIdResults(Map<T, List<EnrichmentBase>> results) {
        Map<T, EnrichmentBase> finalValues = new HashMap<>();
        results.entrySet().stream().forEach(
                entry ->
                        finalValues.put(
                                entry.getKey(),
                                entry.getValue().stream().findFirst().orElse(null)));
        return finalValues;
    }

    public static void failSafeCheck(int expected, int actual, String errorMsg) {
        if (expected != actual) {
            throw new UnknownException(errorMsg + "Expected =" +expected + " Actual=" + actual);
        }
    }

    /**
     * Returns true if entity already exists in
     * parentEntities list
     *
     * @param entityIdToCheck id to check
     * @param entities entity list
     * @return
     */
    public static boolean checkIfEntityAlreadyExists(String entityIdToCheck, List<Entity> entities){
        return entities.stream().anyMatch(entity -> entity.getEntityId().equals(entityIdToCheck));
    }

    /**
     * Parent entity functionality is applied only to Agent and Place only
     * @param entity
     * @return
     */
    public static boolean isParentEntityRequired(Entity entity){
        EntityTypes type = EntityTypes.valueOf(entity.getType());
        return (type.equals(EntityTypes.Place) || type.equals(EntityTypes.Agent));
    }
}
