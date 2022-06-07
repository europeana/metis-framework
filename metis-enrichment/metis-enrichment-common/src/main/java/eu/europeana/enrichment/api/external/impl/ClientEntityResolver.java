package eu.europeana.enrichment.api.external.impl;

import static eu.europeana.metis.network.ExternalRequestUtil.retryableExternalRequestForNetworkExceptionsThrowing;
import static java.lang.String.format;
import static java.util.function.Predicate.not;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.collections4.ListUtils.partition;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.europeana.enrichment.api.exceptions.UnknownException;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.internal.EntityResolver;
import eu.europeana.enrichment.api.internal.ReferenceTerm;
import eu.europeana.enrichment.api.internal.SearchTerm;
import eu.europeana.enrichment.utils.EnrichmentBaseConverter;
import eu.europeana.enrichment.utils.LanguageCodeConverter;
import eu.europeana.entity.client.web.EntityClientApi;
import eu.europeana.entitymanagement.definitions.model.Entity;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;

/**
 * An entity resolver that works by accessing a service via Entity Client API and obtains entities from Entity Management API
 *
 * @author Srishti.singh@europeana.eu
 */
public class ClientEntityResolver implements EntityResolver {

  private final int batchSize;
  private final LanguageCodeConverter languageCodeConverter;
  private final EntityClientApi entityClientApi;


  /**
   * Constructor with required parameters.
   *
   * @param entityClientApi the entity client api
   * @param batchSize the batch size
   */
  public ClientEntityResolver(EntityClientApi entityClientApi, int batchSize) {
    this.batchSize = batchSize;
    this.languageCodeConverter = new LanguageCodeConverter();
    this.entityClientApi = entityClientApi;
  }

  @Override
  public <T extends SearchTerm> Map<T, List<EnrichmentBase>> resolveByText(Set<T> searchTerms) {
    return performInBatches(searchTerms);
  }

  @Override
  public <T extends ReferenceTerm> Map<T, EnrichmentBase> resolveById(Set<T> referenceTerms) {
    final Map<T, List<EnrichmentBase>> batches = performInBatches(referenceTerms);
    return convertToMapWithSingleValues(batches);
  }

  private <T extends ReferenceTerm> HashMap<T, EnrichmentBase> convertToMapWithSingleValues(
      Map<T, List<EnrichmentBase>> batches) {
    return batches.entrySet().stream().collect(HashMap::new, (map, entry) -> map.put(entry.getKey(),
        entry.getValue().stream().findFirst().orElse(null)), HashMap::putAll);
  }

  @Override
  public <T extends ReferenceTerm> Map<T, List<EnrichmentBase>> resolveByUri(Set<T> referenceTerms) {
    return performInBatches(referenceTerms, true);
  }

  private <I> Map<I, List<EnrichmentBase>> performInBatches(Set<I> inputValues) {
    return performInBatches(inputValues, false);
  }

  /**
   * Checks if an entity identifier matches an identifier of the entities provided.
   *
   * @param entityIdToCheck the entity identifier to check
   * @param entities the entity list
   * @return true if it matches otherwise false
   */
  private static boolean doesEntityExist(String entityIdToCheck, List<Entity> entities) {
    return entities.stream().anyMatch(entity -> entity.getEntityId().equals(entityIdToCheck));
  }

  private <I> List<Entity> resolveEntities(I batchItem, boolean uriSearch) {
    if (batchItem instanceof ReferenceTerm) {
      return resolveReference((ReferenceTerm) batchItem, uriSearch);
    } else {
      return resolveTextSearch((SearchTerm) batchItem);
    }
  }

  /**
   * Get entities by text search.
   * <p>
   * The result will always be a list of size 1. Internally the remote request might return more than one entities which in that
   * case the return of this method will be an empty list. That is because the remote request would be ambiguous and therefore we
   * do not know which of the entities is actually intended.
   * </p>
   * <p>
   * ATTENTION: The described discarding of entities applies correctly in the case where the remote request does <b>NOT</b>
   * contain parent entities and that the parent entities are fetched remotely i.e. {@link #extendEntitiesWithParents}.
   * </p>
   *
   * @param searchTerm the text search term
   * @return the list of entities(at this point of size 0 or 1)
   */
  private List<Entity> resolveTextSearch(SearchTerm searchTerm) {
    final String entityTypesConcatenated = searchTerm.getCandidateTypes().stream()
                                                     .map(entityType -> entityType.name().toLowerCase(Locale.US))
                                                     .collect(Collectors.joining(","));
    final String language = languageCodeConverter.convertLanguageCode(searchTerm.getLanguage());
    final List<Entity> entities;
    try {
      entities = retryableExternalRequestForNetworkExceptionsThrowing(
          () -> entityClientApi.getEnrichment(searchTerm.getTextValue(), language, entityTypesConcatenated, null));
      return entities.size() == 1 ? entities : Collections.emptyList();
    } catch (JsonProcessingException e) {
      throw new UnknownException(
          format("SearchTerm request failed for textValue: %s, language: %s, entityTypes: %s.", searchTerm.getTextValue(),
              searchTerm.getLanguage(), entityTypesConcatenated), e);
    }
  }

  /**
   * Get entities based on a reference.
   * <p>We always check first if the reference resembles a euroepeana entity identifier and if so then we search by id..</p>
   * <p>For invocations that are uri searches({@code uriSearch} equals true) then we also invoke the remote uri search.</p>
   * <p>For uri searches, this resembles the metis implementation where the about search is invoked and if no result return then
   * a second invocation on the owlSameAs is performed.</p>
   *
   * @param referenceTerm the reference term
   * @param uriSearch indicates if the search is an uri or an id search
   * @return the list of entities
   */
  private List<Entity> resolveReference(ReferenceTerm referenceTerm, boolean uriSearch) {
    final String referenceValue = referenceTerm.getReference().toString();

    List<Entity> result = new ArrayList<>();
    if (europeanaLinkPattern.matcher(referenceValue).matches()) {
      result = Optional.ofNullable(entityClientApi.getEntityById(referenceValue)).map(List::of).orElse(Collections.emptyList());
    } else if (uriSearch) {
      result = entityClientApi.getEntityByUri(referenceValue);
    }
    return result;
  }

  /**
   * Creates a copy list that is then extended with any parents found.
   *
   * @param entities the entities
   * @return the extended entities
   */
  private List<Entity> extendEntitiesWithParents(List<Entity> entities) {
    //Copy list so that we can extend
    final ArrayList<Entity> copyEntities = new ArrayList<>(entities);
    return findParentEntitiesRecursive(copyEntities, copyEntities);
  }

  /**
   * Perform search in batches.
   *
   * @param <I> the input value type. Can be <T extends SearchTerm> OR <T extends ReferenceTerm>
   * @param inputValues the set of values for which enrichment will be performed
   * @param uriSearch boolean indicating if it is an uri search or not(then it is an id search)
   * @return the results mapped per input value
   */
  private <I> Map<I, List<EnrichmentBase>> performInBatches(Set<I> inputValues, boolean uriSearch) {
    final Map<I, List<EnrichmentBase>> result = new HashMap<>();
    for (List<I> batch : partition(new ArrayList<>(inputValues), batchSize)) {
      result.putAll(performBatch(uriSearch, batch));
    }
    return result;
  }

  private <I> Map<I, List<EnrichmentBase>> performBatch(boolean uriSearch, List<I> batch) {
    final Map<I, List<EnrichmentBase>> result = new HashMap<>();
    // TODO: 02/06/2022 This is actually bypassing the batching.. This is the selected way to perform this for now.
    for (I batchItem : batch) {
      List<EnrichmentBase> enrichmentBaseList = performItem(batchItem, uriSearch);
      result.put(batchItem, enrichmentBaseList);
    }
    return result;
  }

  private <I> List<EnrichmentBase> performItem(I batchItem, boolean uriSearch) {
    List<Entity> entities = resolveEntities(batchItem, uriSearch);
    List<EnrichmentBase> enrichmentBases = new ArrayList<>();
    if (isNotEmpty(entities)) {
      entities = extendEntitiesWithParents(entities);
      enrichmentBases = convertToEnrichmentBase(entities);
    }
    return enrichmentBases;
  }

  /**
   * Converts the list of entities to a list of {@link EnrichmentBase}s.
   *
   * @param entities the entities
   * @return the converted list
   */
  private List<EnrichmentBase> convertToEnrichmentBase(List<Entity> entities) {
    return EnrichmentBaseConverter.convertEntitiesToEnrichmentBase(entities);
  }

  /**
   * Finds parent entities and extends recursively.
   * <p>For each recursion it will, iterate over {@link Entity#getIsPartOfArray} bypassing blank values and entities already
   * encountered. Each recursion will extended list if more parents have been found.
   * </p>
   *
   * @param collectedEntities the collected entities
   * @param children the children to check their parents for
   * @return the extended list of entities
   */
  private List<Entity> findParentEntitiesRecursive(List<Entity> collectedEntities, List<Entity> children) {
    List<Entity> parentEntities =
        Stream.ofNullable(children).flatMap(Collection::stream)
              .map(Entity::getIsPartOfArray).filter(Objects::nonNull).flatMap(Collection::stream)
              .filter(StringUtils::isNotBlank)
              .filter(not(parentEntityId -> doesEntityExist(parentEntityId, collectedEntities)))
              .map(entityClientApi::getEntityById)
              .filter(Objects::nonNull)
              .collect(Collectors.toCollection(ArrayList::new));

    if (isNotEmpty(parentEntities)) {
      collectedEntities.addAll(parentEntities);
      //Now check again parents of parents
      findParentEntitiesRecursive(collectedEntities, parentEntities);
    }
    return collectedEntities;
  }
}
