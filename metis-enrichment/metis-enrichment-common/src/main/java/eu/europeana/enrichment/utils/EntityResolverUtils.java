package eu.europeana.enrichment.utils;

import eu.europeana.enrichment.api.exceptions.UnknownException;
import eu.europeana.enrichment.api.external.model.*;
import eu.europeana.enrichment.api.internal.ReferenceTerm;
import eu.europeana.enrichment.api.internal.SearchTerm;
import eu.europeana.entity.client.utils.EntityApiConstants;
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

    public static <I> List<List<I>> createBatch(Set<I> inputValues, int batchSize) {
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

    public static <T extends SearchTerm> Function<T, EnrichmentQuery> createEnrichmentQueryForTextSearch() {
        return term -> new EnrichmentQuery(term.getTextValue(), term.getLanguage(),
                term.getCandidateTypes()
                        .stream()
                        .map(entityType -> entityType.name().toLowerCase())
                        .collect(Collectors.joining(",")), false);
    }

    public static <T extends ReferenceTerm> Function<T, EnrichmentQuery> createEnrichmentQueryForRefSearch() {
        return term -> new EnrichmentQuery(term.getReference().toString(),
                term.getCandidateTypes()
                        .stream()
                        .map(entityType -> entityType.name().toLowerCase())
                        .collect(Collectors.joining(",")), true);
    }

    /**
     * Returns the first Entity from the List<EnrichmentBase> for each input value
     * @param results
     * @param <T>
     * @return
     */
    public static <T>  Map<T, EnrichmentBase> pickFirstFromTheList(Map<T, List<EnrichmentBase>> results) {
        Map<T, EnrichmentBase> finalValues = new HashMap<>();
        results.entrySet().stream().forEach(
                entry ->
                        finalValues.put(
                                entry.getKey(),
                                entry.getValue().stream().findFirst().orElse(null)));
        return finalValues;
    }

    /**
     * Converts the EM model class to Metis EnrichmentBase
     *
     * @param entity EM Entity
     * @param <T>    class that extends EnrichmentBase
     * @return
     */
    public static <T extends EnrichmentBase> T convertEntityToEnrichmentBase(Entity entity) {
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
     * Returns true if the Request is Text search or Uri search
     * @param clientRequest
     * @return
     */
    public static boolean isTextOrUriSearch(EnrichmentQuery clientRequest){
        return (clientRequest.isReference() && !clientRequest.getValueToEnrich().startsWith(EntityApiConstants.BASE_URL))
                || !clientRequest.isReference();
    }
}
