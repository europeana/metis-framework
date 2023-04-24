package eu.europeana.indexing.mongo.property;

import static eu.europeana.metis.network.ExternalRequestUtil.retryableExternalRequestForNetworkExceptions;

import com.mongodb.DuplicateKeyException;
import dev.morphia.UpdateOptions;
import dev.morphia.query.Query;
import dev.morphia.query.updates.UpdateOperator;
import dev.morphia.query.updates.UpdateOperators;
import eu.europeana.corelib.definitions.edm.entity.AbstractEdmEntity;
import eu.europeana.corelib.definitions.edm.entity.WebResource;
import eu.europeana.corelib.definitions.edm.model.metainfo.WebResourceMetaInfo;
import eu.europeana.corelib.edm.model.metainfo.WebResourceMetaInfoImpl;
import eu.europeana.corelib.solr.entity.WebResourceImpl;
import eu.europeana.indexing.mongo.AbstractEdmEntityUpdater;
import eu.europeana.indexing.mongo.WebResourceInformation;
import eu.europeana.metis.mongo.dao.RecordDao;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides the base implementation of {@link MongoPropertyUpdater}.
 *
 * @param <T> The type of the object to update.
 */
class MongoPropertyUpdaterImpl<T> implements MongoPropertyUpdater<T> {

  private static final Logger LOGGER = LoggerFactory.getLogger(MongoPropertyUpdaterImpl.class);

  private static final UnaryOperator<String[]> STRING_ARRAY_PREPROCESSING = array -> Stream
      .of(array).filter(StringUtils::isNotBlank).map(String::trim).toArray(String[]::new);

  private static final Comparator<AbstractEdmEntity> ENTITY_COMPARATOR = Comparator
      .comparing(AbstractEdmEntity::getAbout);

  private final T current;
  private final T updated;
  private final RecordDao mongoServer;
  private final List<UpdateOperator> updateOperators;
  private final Supplier<Query<T>> queryCreator;

  MongoPropertyUpdaterImpl(T current, T updated, RecordDao mongoServer,
      List<UpdateOperator> updateOperators, Supplier<Query<T>> queryCreator) {
    this.current = current;
    this.updated = updated;
    this.mongoServer = mongoServer;
    this.updateOperators = Optional.ofNullable(updateOperators).stream().flatMap(Collection::stream)
                                   .collect(Collectors.toList());
    this.queryCreator = queryCreator;
  }

  private static <I extends Comparable<I>> boolean listEquals(List<I> listA, List<I> listB) {
    return listEquals(listA, listB, Comparable::compareTo);
  }

  private static <I> boolean listEquals(List<I> listA, List<I> listB,
      Comparator<? super I> comparator) {

    // Check for null
    if (listA == null || listB == null) {
      return listA == null && listB == null;
    }

    // Sort and compare
    final List<I> sortedListA = new ArrayList<>(listA);
    final List<I> sortedListB = new ArrayList<>(listB);
    sortedListA.sort(comparator);
    sortedListB.sort(comparator);
    return sortedListA.equals(sortedListB);
  }

  /**
   * Checks whether two maps contain exactly the same key value combinations. The value lists are said to be the same if they
   * contain the same elements, regardless of the order but counting multiplicity.
   *
   * @param mapA The first map.
   * @param mapB The second map.
   * @return Whether the maps contain the same key value combinations.
   */
  static boolean mapEquals(Map<String, List<String>> mapA, Map<String, List<String>> mapB) {

    // Check for null and key set equality
    if (mapA == null || mapB == null || !mapA.keySet().equals(mapB.keySet())) {
      return mapA == null && mapB == null;
    }

    // So key set is equal. Compare values.
    return mapA.keySet().stream().allMatch(key -> listEquals(mapA.get(key), mapB.get(key)));
  }

  /**
   * Check if two arrays contain the same values, regardless of the order, but counting multiplicity.
   *
   * @param arrA The first array.
   * @param arrB The second array.
   * @return Whether the arrays contain the same values.
   */
  static boolean arrayEquals(String[] arrA, String[] arrB) {
    if (arrA == null || arrB == null) {
      return arrA == null && arrB == null;
    }
    return listEquals(Arrays.asList(arrA), Arrays.asList(arrB));
  }

  @Override
  public void updateMap(String updateField, Function<T, Map<String, List<String>>> getter) {
    updateProperty(updateField, getter, MongoPropertyUpdaterImpl::mapEquals,
        UnaryOperator.identity());
  }

  @Override
  public void updateArray(String updateField, Function<T, String[]> getter) {
    updateArray(updateField, getter, false);
  }

  @Override
  public void updateArray(String updateField, Function<T, String[]> getter,
      boolean makeEmptyArrayNull) {
    final UnaryOperator<String[]> nullChecker = array -> (makeEmptyArrayNull && array.length == 0)
        ? null : array;
    updateProperty(updateField, getter, MongoPropertyUpdaterImpl::arrayEquals,
        STRING_ARRAY_PREPROCESSING.andThen(nullChecker)::apply);
  }

  @Override
  public <P> void updateObjectList(String updateField, Function<T, List<P>> getter) {
    updateProperty(updateField, getter, (input1, input2) -> false,
        list -> list.isEmpty() ? null : list);
  }

  @Override
  public void updateString(String updateField, Function<T, String> getter) {
    updateProperty(updateField, getter, Objects::equals, String::trim);
  }

  @Override
  public <P> void updateObject(String updateField, Function<T, P> getter) {
    updateObject(updateField, getter, UnaryOperator.identity());
  }

  @Override
  public <P> void updateObject(String updateField, Function<T, P> getter,
      UnaryOperator<P> preprocessing) {
    updateProperty(updateField, getter, Objects::equals, preprocessing);
  }

  @Override
  public void updateWebResources(String updateField,
      Function<T, List<? extends WebResource>> getter, RootAboutWrapper ancestorInformation,
      AbstractEdmEntityUpdater<WebResourceImpl, RootAboutWrapper> webResourceUpdater) {
    final Function<T, List<WebResourceImpl>> castGetter = getter
        .andThen(MongoPropertyUpdaterImpl::castWebResourceList);
    updateReferencedEntities(updateField, castGetter, entity -> ancestorInformation,
        webResourceUpdater);
  }

  private static List<WebResourceImpl> castWebResourceList(List<? extends WebResource> input) {
    return input == null ? null : input.stream().map(WebResourceImpl.class::cast)
                                       .collect(Collectors.toList());
  }

  @Override
  public <P extends AbstractEdmEntity, A> void updateReferencedEntity(String updateField,
      Function<T, P> getter, Function<T, A> ancestorInfoGetter,
      MongoObjectUpdater<P, A> objectUpdater) {
    final A ancestorInformation = ancestorInfoGetter.apply(updated);
    final UnaryOperator<P> preprocessing = entity -> objectUpdater
        .update(entity, ancestorInformation, null, null, mongoServer);
    updateProperty(updateField, getter, MongoPropertyUpdaterImpl::equals, preprocessing);
  }

  private static boolean equals(AbstractEdmEntity entity1, AbstractEdmEntity entity2) {
    if (entity1 == null || entity2 == null) {
      return entity1 == null && entity2 == null;
    }
    return entity1.getAbout().equals(entity2.getAbout());
  }

  @Override
  public <P extends AbstractEdmEntity, A> void updateReferencedEntities(String updateField,
      Function<T, List<P>> getter, Function<T, A> ancestorInfoGetter,
      MongoObjectUpdater<P, A> objectUpdater) {
    final A ancestorInformation = ancestorInfoGetter.apply(updated);
    final UnaryOperator<List<P>> preprocessing = entities -> new ArrayList<>(entities.stream()
                                                                                     .map(entity -> objectUpdater.update(entity,
                                                                                         ancestorInformation, null, null,
                                                                                         mongoServer))
                                                                                     .collect(Collectors.toMap(
                                                                                         AbstractEdmEntity::getAbout,
                                                                                         Function.identity(), (o1, o2) -> o2))
                                                                                     .values());
    final BiPredicate<List<P>, List<P>> equality = (w1, w2) -> listEquals(w1, w2,
        ENTITY_COMPARATOR);
    updateProperty(updateField, getter, equality, preprocessing);
  }

  /**
   * This method updates a given object property. This method tests if there is anything to update. If there is, after this method
   * is called, {@link #applyOperations()} will include the update.
   *
   * @param updateField The name of the field to update. This is the name under which they will be stored in the operations list
   * (see {@link #applyOperations()}).
   * @param getter The getter that obtains the property value from the object.
   * @param equality Predicate that checks for equality between two property values.
   * @param preprocessing The pre-processing to be applied to the update property value before comparing and storing.
   */
  private <P> void updateProperty(String updateField, Function<T, P> getter,
      BiPredicate<P, P> equality, UnaryOperator<P> preprocessing) {

    // Get the current (saved) value (or null if there is no current object).
    final P currentValue = Optional.ofNullable(current).map(getter).orElse(null);

    // Get the new value and apply preprocessing.
    final P updatedValue = Optional.of(updated).map(getter).map(preprocessing).orElse(null);

    // Process changes if applicable.
    if (equality.test(currentValue, updatedValue)) {
      if (updatedValue != null) {
        // If there has been no change, set only on insert (only needed if value is not null).
        updateOperators.add(UpdateOperators.setOnInsert(Map.of(updateField, updatedValue)));
      }
    } else {
      // If there has been a change, either set the value or unset it if it is null.
      if (updatedValue == null) {
        updateOperators.add(UpdateOperators.unset(updateField));
      } else {
        updateOperators.add(UpdateOperators.set(updateField, updatedValue));
      }
    }
  }

  @Override
  public <P> boolean removeObjectIfNecessary(String updateField, Function<T, P> getter) {

    // Get the current (saved) value (or null if there is no current object).
    final P currentValue = Optional.ofNullable(current).map(getter).orElse(null);

    // Get the new value.
    final P updatedValue = Optional.of(updated).map(getter).orElse(null);

    // If we need to remove it, do so.
    boolean markForDeletion = currentValue != null && updatedValue == null;
    if (markForDeletion) {
      updateOperators.add(UpdateOperators.unset(updateField));
    }

    // Done
    return markForDeletion;
  }

  @Override
  public void updateWebResourceMetaInfo(Function<T, WebResourceMetaInfo> getter,
      Function<T, WebResourceInformation> ancestorInfoGetter,
      Supplier<MongoObjectManager<WebResourceMetaInfoImpl, WebResourceInformation>> updaterSupplier) {
    final WebResourceMetaInfo entity = Optional.of(updated).map(getter).orElse(null);
    final WebResourceInformation ancestorInformation = ancestorInfoGetter.apply(updated);
    if (entity == null) {
      updaterSupplier.get().delete(ancestorInformation, mongoServer);
    } else {
      updaterSupplier.get().update((WebResourceMetaInfoImpl) entity, ancestorInformation,
          null, null, mongoServer);
    }
  }

  @Override
  public T applyOperations() {
    final UpdateOperator[] extraUpdateOperators = this.updateOperators.toArray(UpdateOperator[]::new);
    final UpdateOptions updateOptions = new UpdateOptions().upsert(true).multi(true);
    final Query<T> update = queryCreator.get();
    try {
      retryableExternalRequestForNetworkExceptions(() -> update.update(updateOptions, extraUpdateOperators));
    } catch (DuplicateKeyException e) {
      LOGGER.debug("Received duplicate key exception, trying again once more.", e);
      update.update(updateOptions, extraUpdateOperators);
    }
    return queryCreator.get().first();
  }
}
