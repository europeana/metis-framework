package eu.europeana.enrichment.api.external.impl;

import static java.lang.String.format;
import static java.util.function.Predicate.not;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import eu.europeana.enrichment.api.external.exceptions.EntityApiException;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.internal.EntityResolver;
import eu.europeana.enrichment.api.internal.ReferenceTerm;
import eu.europeana.enrichment.api.internal.SearchTerm;
import eu.europeana.enrichment.utils.EnrichmentBaseConverter;
import eu.europeana.enrichment.utils.LanguageCodeConverter;
import eu.europeana.entity.client.EntityApiClient;
import eu.europeana.entity.client.exception.EntityClientException;
import eu.europeana.entitymanagement.definitions.model.Entity;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;


/**
 * An entity resolver that works by accessing a service via Entity Client API and obtains entities from Entity Management API
 *
 * @author Srishti.singh @europeana.eu
 */
public class ClientEntityResolver implements EntityResolver {

  private static final int CACHE_MAX_ENTRIES = 5000;
  private static final ClientEntityResolverCache<String, Entity> entityResolverCache = new ClientEntityResolverCache<>(CACHE_MAX_ENTRIES);
  private static final ClientEntityResolverCache<String, List<Entity>> referenceTermResolverCache = new ClientEntityResolverCache<>(CACHE_MAX_ENTRIES);
  private static final ClientEntityResolverCache<SearchTerm, List<Entity>> searchTermResolverCache = new ClientEntityResolverCache<>(CACHE_MAX_ENTRIES);
  private final LanguageCodeConverter languageCodeConverter;
  private final EntityApiClient entityClientApi;
  private final OperationMode operationMode;

  /**
   * Constructor with required parameters.
   *
   * @param entityClientApi the entity client api
   */
  public ClientEntityResolver(EntityApiClient entityClientApi) {
    this.languageCodeConverter = new LanguageCodeConverter();
    this.entityClientApi = entityClientApi;
    this.operationMode = OperationMode.NON_CACHED;
  }

  /**
   * Constructor with required parameters.
   *
   * @param entityClientApi the entity client api
   * @param mode the mode
   */
  public ClientEntityResolver(EntityApiClient entityClientApi, OperationMode mode) {
    this.languageCodeConverter = new LanguageCodeConverter();
    this.entityClientApi = entityClientApi;
    this.operationMode = mode;
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

  /**
   * Cache entity stats .
   *
   * @return the cache stats
   */
  public CacheStats cacheEntityStats() {
    return entityResolverCache.stats();
  }

  /**
   * Cache reference term stats.
   *
   * @return the cache stats
   */
  public CacheStats cacheReferenceTermStats() {
    return referenceTermResolverCache.stats();
  }

  /**
   * Cache search term stats.
   *
   * @return the cache stats
   */
  public CacheStats cacheSearchTermStats() {
    return searchTermResolverCache.stats();
  }

  @Override
  public <T extends SearchTerm> Map<T, List<EnrichmentBase>> resolveByText(Set<T> searchTerms) {
    return searchTerms.stream().collect(Collectors.toMap(Function.identity(), this::resolveTextSearch));
  }

  @Override
  public <T extends ReferenceTerm> Map<T, List<EnrichmentBase>> resolveByUri(Set<T> referenceTerms) {
    return referenceTerms.stream().collect(Collectors.toMap(Function.identity(), this::resolveEquivalency));
  }

  @Override
  public <T extends ReferenceTerm> Map<T, EnrichmentBase> resolveById(Set<T> referenceTerms) {
    return referenceTerms.stream()
                         .map(term -> new ImmutablePair<>(term, this.resolveId(term)))
                         .filter(pair -> pair.getValue() != null)
                         .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
  }

  /**
   * Get entity by ID. Only supported for Europeana IDs, and no equivalences (e.g. old IDs) are honored.
   *
   * @param referenceTerm the reference term
   * @return The entity with the given ID, or null.
   */
  private EnrichmentBase resolveId(ReferenceTerm referenceTerm) {
    final String referenceValue = referenceTerm.getReference().toString();
    if (europeanaLinkPattern.matcher(referenceValue).matches()) {
      final Entity entity = switch (operationMode) {
        case NON_CACHED -> getResolveId(referenceValue);
        case CACHED -> entityResolverCache.computeIfAbsent(referenceValue, key -> getResolveId(referenceValue));
        case null -> throw new EntityApiException("Operation mode not defined", null);
      };

      return (entity.getEntityId().equals(referenceValue))
          ? EnrichmentBaseConverter.convertEntitiesToEnrichmentBase(entity) : null;
    }
    return null;
  }

  private Entity getResolveId(String referenceValue) {
    final Entity entity;
    try {
      entity = entityClientApi.getEntity(referenceValue);
    } catch (EntityClientException e) {
      throw new EntityApiException("Issue trying to get entity with ID" + referenceValue, e);
    }
    return entity;
  }

  /**
   * Get equivalent entities based on a reference. If the reference is to a Europeana entity identifier, we search for an entity
   * with this Europeana id or equivalent to this Europeana id (i.e. in case of an ID change). Else, if we have a remote URI (e.g.
   * wikidata, VIAF, ...) we search for an equivalence with the remote URI.
   *
   * @param referenceTerm the reference term
   * @return the list of entities
   */
  private List<EnrichmentBase> resolveEquivalency(ReferenceTerm referenceTerm) {
    final String referenceValue = referenceTerm.getReference().toString();
    final List<Entity> entities;

    switch (operationMode) {
      case NON_CACHED -> {
        try {
          entities = getResolveEquivalency(referenceValue);
        } catch (EntityClientException e) {
          throw new EntityApiException("Issue trying to get/resolve entity for URL " + referenceValue, e);
        }
      }
      case CACHED -> entities = referenceTermResolverCache.computeIfAbsent(referenceValue, reference -> {
        try {
          return getResolveEquivalency(referenceValue);
        } catch (EntityClientException e) {
          throw new EntityApiException("Issue trying to get/resolve entity for URL " + referenceValue, e);
        }
      });
      case null -> throw new EntityApiException("Operation mode not defined", null);
    }

    return convertToEnrichmentBase(extendEntitiesWithParents(entities));
  }

  private List<Entity> getResolveEquivalency(String referenceValue) throws EntityClientException {
    final List<Entity> entities;
    if (europeanaLinkPattern.matcher(referenceValue).matches()) {
      entities = Optional.ofNullable(entityClientApi.getEntity(referenceValue)).map(List::of)
                         .orElse(Collections.emptyList());
    } else {
      entities = Optional.ofNullable(entityClientApi.resolveEntity(referenceValue)).orElse(Collections.emptyList());
    }
    return entities;
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
  private List<EnrichmentBase> resolveTextSearch(SearchTerm searchTerm) {
    final String entityTypesConcatenated = searchTerm.getCandidateTypes().stream()
                                                     .map(entityType -> entityType.name().toLowerCase(Locale.US))
                                                     .collect(Collectors.joining(","));
    final String language = languageCodeConverter.convertLanguageCode(searchTerm.getLanguage());
    final List<Entity> result;
    switch (operationMode) {
      case NON_CACHED -> result = getResolveTextSearch(searchTerm, language, entityTypesConcatenated);
      case CACHED -> result = searchTermResolverCache.computeIfAbsent(searchTerm,
          term -> getResolveTextSearch(searchTerm, language, entityTypesConcatenated));
      case null -> throw new EntityApiException("Operation mode not defined", null);
    }
    final List<Entity> singleResult = result.size() == 1 ? result : Collections.emptyList();
    return convertToEnrichmentBase(extendEntitiesWithParents(singleResult));
  }

  private List<Entity> getResolveTextSearch(SearchTerm searchTerm, String language, String entityTypesConcatenated) {
    final List<Entity> result;
    try {
      result = entityClientApi.enrichEntity(searchTerm.getTextValue(), language, entityTypesConcatenated, null);
    } catch (EntityClientException e) {
      throw new EntityApiException(format("Enrichment request failed for textValue: %s, language: %s, entityTypes: %s.",
          searchTerm.getTextValue(), searchTerm.getLanguage(), entityTypesConcatenated), e);
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
   * Converts the list of entities to a list of {@link EnrichmentBase}s.
   *
   * @param entities the entities
   * @return the converted list. List is not null, not containing null values.
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
              .map(parentEntityId -> {
                Entity parentEntity;
                switch (operationMode) {
                  case NON_CACHED -> parentEntity = getResolveId(parentEntityId);
                  case CACHED ->
                      parentEntity = entityResolverCache.computeIfAbsent(parentEntityId, key -> getResolveId(parentEntityId));
                  case null -> throw new EntityApiException("Operation mode not defined", null);
                }
                return parentEntity;
              })
              .filter(Objects::nonNull)
              .collect(Collectors.toCollection(ArrayList::new));

    if (isNotEmpty(parentEntities)) {
      collectedEntities.addAll(parentEntities);
      //Now check again parents of parents
      findParentEntitiesRecursive(collectedEntities, parentEntities);
    }
    return collectedEntities;
  }

  /**
   * The enum Operation mode.
   */
  public enum OperationMode {
    /**
     * Normal operation mode.
     */
    NON_CACHED,
    /**
     * Cached operation mode.
     */
    CACHED
  }
}
