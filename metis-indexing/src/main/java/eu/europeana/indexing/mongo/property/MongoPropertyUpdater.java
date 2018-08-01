package eu.europeana.indexing.mongo.property;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang.StringUtils;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mongodb.DuplicateKeyException;
import eu.europeana.corelib.definitions.edm.entity.AbstractEdmEntity;
import eu.europeana.corelib.definitions.edm.entity.WebResource;
import eu.europeana.corelib.definitions.edm.model.metainfo.WebResourceMetaInfo;
import eu.europeana.corelib.edm.model.metainfo.WebResourceMetaInfoImpl;
import eu.europeana.corelib.solr.entity.WebResourceImpl;
import eu.europeana.corelib.storage.MongoServer;

/**
 * This class provides functionality to update the properties of a given object. It keeps track of
 * the operations that are required to perform the update.
 *
 * @param <T> The type of the object to update.
 */
final class MongoPropertyUpdater<T> {

  private static final Logger LOGGER = LoggerFactory.getLogger(MongoPropertyUpdater.class);

  private static final String ABOUT_FIELD = "about";

  private static final UnaryOperator<String[]> STRING_ARRAY_PREPROCESSING = array -> Stream
      .of(array).filter(StringUtils::isNotBlank).map(String::trim).toArray(String[]::new);

  private static final Comparator<AbstractEdmEntity> ENTITY_COMPARATOR =
      Comparator.comparing(AbstractEdmEntity::getAbout);

  private final T current;
  private final T updated;
  private final MongoServer mongoServer;
  private final UpdateOperations<T> mongoOperations;
  private final Supplier<Query<T>> queryCreator;

  /**
   * Constructor.
   * 
   * @param updated The updated object (i.e. the object to take the value from). This object will
   *        remain unchanged.
   * @param mongoServer The Mongo connection.
   * @param objectClass The class of the object which is used to create an instance of
   *        {@link UpdateOperations}.
   * @param queryCreator The function that creates the mongo query that can retrieve the object from
   *        Mongo.
   * @param preprocessor This provides the option of performing some preprocessing on the current
   *        and/or the new object before applying the operations. Its two parameters are first the
   *        current bean (found in the database) and second the updated (as passed to this method).
   *        Can be null.
   */
  public MongoPropertyUpdater(T updated, MongoServer mongoServer, Class<T> objectClass,
      Supplier<Query<T>> queryCreator, BiConsumer<T, T> preprocessor) {

    // Sanity checks.
    if (updated == null || mongoServer == null || objectClass == null || queryCreator == null) {
      throw new IllegalArgumentException();
    }

    // Set the properties
    this.updated = updated;
    this.mongoServer = mongoServer;
    this.queryCreator = queryCreator;

    // Initialize the mongo operations: set the class name on insert if needed.
    this.mongoOperations = mongoServer.getDatastore().createUpdateOperations(objectClass);

    // Obtain the current state from the database and perform preprocessing on it.
    this.current = this.queryCreator.get().get();
    if (preprocessor != null) {
      preprocessor.accept(current, updated);
    }
  }

  /**
   * Static constructor for objects that have an about field.
   *
   * @param updated The updated object (i.e. the object to take the value from). This object will
   *        remain unchanged.
   * @param mongoServer The Mongo connection.
   * @param objectClass The class of the object which is used to create an instance of
   *        {@link UpdateOperations}.
   * @param aboutGetter The function that obtains the about value from the object.
   * @param preprocessor This provides the option of performing some preprocessing on the current
   *        and/or the new object before applying the operations. Its two parameters are first the
   *        current bean (found in the database) and second the updated (as passed to this method).
   *        Can be null.
   * 
   * @return The property updater.
   */
  public static <T> MongoPropertyUpdater<T> createForObjectWithAbout(T updated,
      MongoServer mongoServer, Class<T> objectClass, Function<T, String> aboutGetter,
      BiConsumer<T, T> preprocessor) {

    // Sanity checks.
    if (aboutGetter == null) {
      throw new IllegalArgumentException();
    }
    if (StringUtils.isBlank(aboutGetter.apply(updated))) {
      throw new IllegalArgumentException("Object does not have an 'about' value.");
    }

    // Create object.
    final Supplier<Query<T>> queryCreator = () -> mongoServer.getDatastore().find(objectClass)
        .field(ABOUT_FIELD).equal(aboutGetter.apply(updated));
    final MongoPropertyUpdater<T> result =
        new MongoPropertyUpdater<>(updated, mongoServer, objectClass, queryCreator, preprocessor);

    // Set the about.
    result.mongoOperations.setOnInsert(ABOUT_FIELD, aboutGetter.apply(updated));

    // Done
    return result;
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
    Collections.sort(sortedListA, comparator);
    Collections.sort(sortedListB, comparator);
    return sortedListA.equals(sortedListB);
  }

  /**
   * Checks whether two maps contain exactly the same key value combinations. The value lists are
   * said to be the same if they contain the same elements, regardless of the order but counting
   * multiplicity.
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
   * Check if two arrays contain the same values, regardless of the order, but counting
   * multiplicity.
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

  /**
   * <p>
   * This method updates a map property. It does not pre-process the map before updating.
   * </p>
   * <p>
   * This method tests if there is anything to update. If there is, after this method is called,
   * {@link #applyOperations()} will include the update.
   * </p>
   *
   * @param updateField The name of the field to update. This is the name under which they will be
   *        stored in the operations list (see {@link #applyOperations()}).
   * @param getter The getter that obtains the property value from the object.
   */
  public void updateMap(String updateField, Function<T, Map<String, List<String>>> getter) {
    updateProperty(updateField, getter, MongoPropertyUpdater::mapEquals, UnaryOperator.identity());
  }

  /**
   * <p>
   * This method updates an array property. Before doing so, it will remove null or empty values
   * from the array.
   * </p>
   * <p>
   * This method tests if there is anything to update. If there is, after this method is called,
   * {@link #applyOperations()} will include the update.
   * </p>
   *
   * @param updateField The name of the field to update. This is the name under which they will be
   *        stored in the operations list (see {@link #applyOperations()}).
   * @param getter The getter that obtains the property value from the object.
   */
  public void updateArray(String updateField, Function<T, String[]> getter) {
    updateProperty(updateField, getter, MongoPropertyUpdater::arrayEquals,
        STRING_ARRAY_PREPROCESSING);
  }

  /**
   * <p>
   * This method updates a String property. Before doing so, it will trim the string.
   * </p>
   * <p>
   * This method tests if there is anything to update. If there is, after this method is called,
   * {@link #applyOperations()} will include the update.
   * </p>
   *
   * @param updateField The name of the field to update. This is the name under which they will be
   *        stored in the operations list (see {@link #applyOperations()}).
   * @param getter The getter that obtains the property value from the object.
   */
  public void updateString(String updateField, Function<T, String> getter) {
    updateProperty(updateField, getter, Objects::equals, String::trim);
  }

  /**
   * <p>
   * This method updates a generic property without pre-processing.
   * </p>
   * <p>
   * This method tests if there is anything to update. If there is, after this method is called,
   * {@link #applyOperations()} will include the update.
   * </p>
   *
   * @param updateField The name of the field to update. This is the name under which they will be
   *        stored in the operations list (see {@link #applyOperations()}).
   * @param getter The getter that obtains the property value from the object.
   */
  public <P> void updateObject(String updateField, Function<T, P> getter) {
    updateProperty(updateField, getter, Objects::equals, UnaryOperator.identity());
  }

  /**
   * <p>
   * This method updates a list of web resources. It additionally triggers an update for each web
   * resource (using {@link WebResourceUpdater}).
   * </p>
   * <p>
   * This method tests if there is anything to update. If there is, after this method is called,
   * {@link #applyOperations()} will include the update.
   * </p>
   *
   * @param updateField The name of the field to update. This is the name under which they will be
   *        stored in the operations list (see {@link #applyOperations()}).
   * @param getter The getter that obtains the property value from the object.
   * @param ancestorInformation The parent entity's info to be used for updating the web resources.
   */
  public void updateWebResources(String updateField,
      Function<T, List<? extends WebResource>> getter, RootAbout ancestorInformation) {
    final Function<T, List<WebResourceImpl>> castGetter =
        getter.andThen(MongoPropertyUpdater::castWebResourceList);
    updateReferencedEntities(updateField, castGetter, entity -> ancestorInformation,
        new WebResourceUpdater());
  }

  private static List<WebResourceImpl> castWebResourceList(List<? extends WebResource> input) {
    return input == null ? null
        : input.stream().map(webResource -> ((WebResourceImpl) webResource))
            .collect(Collectors.toList());
  }

  /**
   * <p>
   * This method updates a referenced entity (i.e. entity also stored in the database). It
   * additionally triggers an update for the entity (using the supplied
   * {@link AbstractEdmEntityUpdater}).
   * </p>
   * <p>
   * This method tests if there is anything to update. If there is, after this method is called,
   * {@link #applyOperations()} will include the update.
   * </p>
   *
   * @param updateField The name of the field to update. This is the name under which they will be
   *        stored in the operations list (see {@link #applyOperations()}).
   * @param getter The getter that obtains the property value from the object.
   * @param ancestorInfoGetter The parent entity's info to be used for updating the property.
   * @param objectUpdater The updater that may be used to update the referenced objects.
   */
  public <P extends AbstractEdmEntity, A> void updateReferencedEntity(String updateField,
      Function<T, P> getter, Function<T, A> ancestorInfoGetter,
      AbstractEdmEntityUpdater<P, A> objectUpdater) {
    final A ancestorInformation = ancestorInfoGetter.apply(updated);
    final UnaryOperator<P> preprocessing =
        entity -> objectUpdater.update(entity, ancestorInformation, mongoServer);
    updateProperty(updateField, getter, MongoPropertyUpdater::equals, preprocessing);
  }

  private static boolean equals(AbstractEdmEntity entity1, AbstractEdmEntity entity2) {
    if (entity1 == null || entity2 == null) {
      return entity1 == null && entity2 == null;
    }
    return entity1.getAbout().equals(entity2.getAbout());
  }

  /**
   * <p>
   * This method updates a list of referenced entities (i.e. entities also stored in the database).
   * It additionally triggers an update for each entities (using the supplied
   * {@link AbstractEdmEntityUpdater}).
   * </p>
   * <p>
   * This method tests if there is anything to update. If there is, after this method is called,
   * {@link #applyOperations()} will include the update.
   * </p>
   *
   * @param updateField The name of the field to update. This is the name under which they will be
   *        stored in the operations list (see {@link #applyOperations()}).
   * @param getter The getter that obtains the property value from the object.
   * @param ancestorInfoGetter The parent entity's info to be used for updating the properties.
   * @param objectUpdater The updater that may be used to update the referenced objects.
   */
  public <P extends AbstractEdmEntity, A> void updateReferencedEntities(String updateField,
      Function<T, List<P>> getter, Function<T, A> ancestorInfoGetter,
      AbstractEdmEntityUpdater<P, A> objectUpdater) {
    final A ancestorInformation = ancestorInfoGetter.apply(updated);
    final UnaryOperator<List<P>> preprocessing = entities -> entities.stream()
        .map(entity -> objectUpdater.update(entity, ancestorInformation, mongoServer))
        .collect(Collectors.toList());
    final BiPredicate<List<P>, List<P>> equality =
        (w1, w2) -> listEquals(w1, w2, ENTITY_COMPARATOR);
    updateProperty(updateField, getter, equality, preprocessing);
  }

  /**
   * This method updates a given object property. This method tests if there is anything to update.
   * If there is, after this method is called, {@link #applyOperations()} will include the update.
   *
   * @param updateField The name of the field to update. This is the name under which they will be
   *        stored in the operations list (see {@link #applyOperations()}).
   * @param getter The getter that obtains the property value from the object.
   * @param equality Predicate that checks for equality between two property values.
   * @param preprocessing The pre-processing to be applied to the update property value before
   *        comparing and storing.
   */
  private <P> void updateProperty(String updateField, Function<T, P> getter,
      BiPredicate<P, P> equality, UnaryOperator<P> preprocessing) {

    // Get the current (saved) value (or null if there is no current object).
    final P currentValue = Optional.ofNullable(current).map(getter).orElse(null);

    // Get the new value and apply preprocessing.
    final P updatedValue = Optional.of(updated).map(getter).map(preprocessing).orElse(null);

    // Process changes if applicable.
    if (!equality.test(currentValue, updatedValue)) {
      // If there has been a change, either set the value or unset it if it is null.
      if (updatedValue != null) {
        mongoOperations.set(updateField, updatedValue);
      } else {
        mongoOperations.unset(updateField);
      }
    } else if (updatedValue != null) {
      // If there has been no change, set only on insert (only needed if value is not null).
      mongoOperations.setOnInsert(updateField, updatedValue);
    }
  }

  /**
   * <p>
   * This method removes an object if it is no longer needed. It does not trigger an update for the
   * object or its contents. This is useful to check for embedded entities (for the properties of
   * which separate update calls can then be made).
   * </p>
   * <p>
   * This method tests if there is anything to update. If there is, after this method is called,
   * {@link #applyOperations()} will include the update.
   * </p>
   *
   * @param updateField The name of the field to update. This is the name under which they will be
   *        stored in the operations list (see {@link #applyOperations()}).
   * @param getter The getter that obtains the property value from the object.
   * @return Whether or not this field will be marked for deletion.
   */
  public <P> boolean removeObjectIfNecessary(String updateField, Function<T, P> getter) {

    // Get the current (saved) value (or null if there is no current object).
    final P currentValue = Optional.ofNullable(current).map(getter).orElse(null);

    // Get the new value and apply preprocessing.
    final P updatedValue = Optional.of(updated).map(getter).orElse(null);

    // If we need to remove it, do so.
    boolean markForDeletion = currentValue != null && updatedValue == null;
    if (markForDeletion) {
      mongoOperations.unset(updateField);
    }

    // Done
    return markForDeletion;
  }

  /**
   * <p>
   * This method triggers an update for the meta info itself (using
   * {@link WebResourceMetaInfoUpdater}). It doesn't add any operations.
   * </p>
   *
   * @param getter The getter that obtains the property value from the object.
   * @param ancestorInfoGetter The parent entity's info to be used for updating the meta info.
   */
  public void updateWebResourceMetaInfo(Function<T, WebResourceMetaInfo> getter,
      Function<T, WebResourceInformation> ancestorInfoGetter) {
    final WebResourceMetaInfo entity = Optional.of(updated).map(getter).orElse(null);
    final WebResourceInformation ancestorInformation = ancestorInfoGetter.apply(updated);
    if (entity != null) {
      new WebResourceMetaInfoUpdater().update((WebResourceMetaInfoImpl) entity, ancestorInformation,
          mongoServer);
    }
  }

  /**
   * <p>
   * This method applies the operations to the database. After calling this method, the instance
   * should no longer be used.
   * </p>
   * <p>
   * Note that this method attempts the upsert operation twice. This is due to the problem that if
   * separate threads attempt the same upsert simultaneously one of them may fail. For a description
   * of this behavior see the following links:
   * <ul>
   * <li><a href=
   * "https://docs.mongodb.com/manual/reference/method/db.collection.update/#use-unique-indexes">The
   * Mongo documentation</a>, which documents this behavior but is not very clear on the
   * subject.</li>
   * <li><a href="https://jira.mongodb.org/browse/SERVER-14322">This suggested Mongo
   * improvement</a>, which explains this problem a bit better and provides hope that some time in
   * the future this workaround will no longer be necessary.</li>
   * </ul>
   * </p>
   *
   * @return The updated version of the mongo entity (this is the current entity supplied during
   *         construction, but with the required changes made).
   */
  public T applyOperations() {
    try {
      mongoServer.getDatastore().update(queryCreator.get(), mongoOperations, true);
    } catch (DuplicateKeyException e) {
      LOGGER.debug("Received duplicate key exception, trying again once more.", e);
      mongoServer.getDatastore().update(queryCreator.get(), mongoOperations, true);
    }
    return queryCreator.get().get();
  }
}
