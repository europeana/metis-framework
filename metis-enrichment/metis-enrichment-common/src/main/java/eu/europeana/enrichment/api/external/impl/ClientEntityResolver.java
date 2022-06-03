package eu.europeana.enrichment.api.external.impl;

import static java.lang.String.format;
import static java.util.function.Predicate.not;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.europeana.enrichment.api.exceptions.UnknownException;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.internal.EntityResolver;
import eu.europeana.enrichment.api.internal.ReferenceTerm;
import eu.europeana.enrichment.api.internal.SearchTerm;
import eu.europeana.enrichment.utils.EntityResolverUtils;
import eu.europeana.enrichment.utils.LanguageCodeConverter;
import eu.europeana.entity.client.web.EntityClientApi;
import eu.europeana.entity.client.web.EntityClientApiImpl;
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
import org.apache.commons.collections.CollectionUtils;
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
   * @param batchSize the batch size
   */
  public ClientEntityResolver(int batchSize) {
    this.batchSize = batchSize;
    languageCodeConverter = new LanguageCodeConverter();
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
    return performInBatches(referenceTerms, true);
  }

  private <I> Map<I, List<EnrichmentBase>> performInBatches(Set<I> inputValues) {
    return performInBatches(inputValues, false);
  }

  /**
   * Gets the Enrichment for Multiple input values in batches of batchSize
   *
   * @param requestParser Function to return EntityClientRequest from input value <I>
   * @param <I> Input value Type. Could be <T extends SearchTerm> OR <T extends ReferenceTerm>
   * @param inputValues Set of values for which enrichment will be performed
   * @param uriSearch
   * @return
   */
  private <I> Map<I, List<EnrichmentBase>> performInBatches(Set<I> inputValues, boolean uriSearch) {
    final Map<I, List<EnrichmentBase>> result = new HashMap<>();
    final List<List<I>> batches = EntityResolverUtils.splitInBatches(inputValues, batchSize);

    // Process batches
    for (List<I> batch : batches) {
      // TODO: 02/06/2022 This is actually bypassing the batching..
      for (I batchItem : batch) {
        List<EnrichmentBase> enrichmentBaseList = executeEntityClientRequest(batchItem, uriSearch);
        if (!enrichmentBaseList.isEmpty()) {
          result.put(batchItem, enrichmentBaseList);
        }
      }
    }
    return result;
  }

  private <I> List<EnrichmentBase> executeEntityClientRequest(I batchItem, boolean uriSearch) {
    List<Entity> entities = getEntities(batchItem, uriSearch);
    if (isNotEmpty(entities)) {
      entities = extendEntitiesWithParents(entities);
      List<EnrichmentBase> enrichmentBaseList = convertToEnrichmentBase(entities);

      EntityResolverUtils.failSafeCheck(entities.size(), enrichmentBaseList.size(),
          "Mismatch while converting the EM class to EnrichmentBase.");
      return enrichmentBaseList;
    }
    return Collections.emptyList();
  }

  private <I> List<Entity> getEntities(I batchItem, boolean uriSearch) {
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
      entities = entityClientApi.getEnrichment(searchTerm.getTextValue(), language, entityTypesConcatenated, null);
      if (entities.size() == 1) {
        return entities;
      }
    } catch (JsonProcessingException e) {
      // TODO: 02/06/2022 Check if exception should be thrown or bypassed
      throw new UnknownException(
          format("SearchTerm request failed for textValue: %s, language: %s, entityTypes: %s.", searchTerm.getTextValue(),
              searchTerm.getLanguage(), entityTypesConcatenated), e);
    }
    return Collections.emptyList();
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
  // TODO: 02/06/2022 Add unit tests
  private List<Entity> extendEntitiesWithParents(List<Entity> entities) {
    //Copy list so that we can extend
    final ArrayList<Entity> copyEntities = new ArrayList<>(entities);
    return findParentEntitiesRecursive(copyEntities, copyEntities);
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
              .map(Entity::getIsPartOfArray).flatMap(Collection::stream)
              .filter(StringUtils::isNotBlank)
              .filter(not(parentEntityId -> EntityResolverUtils.checkIfEntityAlreadyExists(parentEntityId, collectedEntities)))
              .map(entityClientApi::getEntityById)
              .filter(Objects::nonNull)
              .collect(Collectors.toCollection(ArrayList::new));

    if (CollectionUtils.isNotEmpty(parentEntities)) {
      collectedEntities.addAll(parentEntities);
      //Now check again parents of parents
      findParentEntitiesRecursive(collectedEntities, parentEntities);
    }
    return collectedEntities;
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
