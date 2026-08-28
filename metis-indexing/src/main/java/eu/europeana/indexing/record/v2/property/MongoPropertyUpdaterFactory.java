package eu.europeana.indexing.record.v2.property;

import dev.morphia.query.Query;
import dev.morphia.query.filters.Filters;
import dev.morphia.query.updates.UpdateOperator;
import dev.morphia.query.updates.UpdateOperators;
import eu.europeana.metis.mongo.dao.RecordDao;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.apache.commons.lang3.StringUtils;

/**
 * This class is a factory class for instances of {@link MongoPropertyUpdater}.
 */
public final class MongoPropertyUpdaterFactory {

  public static final String ID = "_id";
  private static final String ABOUT_FIELD = "about";

  private MongoPropertyUpdaterFactory() {
  }

  /**
   * Static constructor for objects that have an about field.
   *
   * @param updated The updated object (i.e. the object to take the value from). This object will remain unchanged.
   * @param mongoServer The Mongo connection.
   * @param objectClass The class of the object which is used to create an instance of {@link Query}.
   * @param aboutGetter The function that obtains the about value from the object.
   * @param preprocessor This provides the option of performing some preprocessing on the current and/or the new object before
   * applying the operations. Its parameters are first the current bean (found in the database) and second the updated (as passed
   * to this method). This parameter can be null, in which no preprocessing takes place.
   * @return The property updater.
   */
  public static <T> MongoPropertyUpdater<T> createForObjectWithAbout(T updated,
      RecordDao mongoServer, Class<T> objectClass, Function<T, String> aboutGetter,
      BiConsumer<T, T> preprocessor) {
    return createForObjectWithField(updated, mongoServer, objectClass, ABOUT_FIELD, aboutGetter.apply(updated), preprocessor);
  }

  /**
   * Static constructor for objects that do not have an {@code about} field but instead use an {@code id} field.
   * <p>
   * The id field is provided and it is not part of the record itself.
   *
   * @param updated The updated object (i.e., the object to take the value from). This object will remain unchanged.
   * @param mongoServer The Mongo connection.
   * @param objectClass The class of the object to be updated.
   * @param id The id of the object to be updated.
   * @param preprocessor This provides the option of performing some preprocessing on the current and/or the new object before
   * applying the operations. Its parameters are first the current bean (found in the database) and second the updated (as passed
   * to this method). This parameter can be null, in which no preprocessing takes place.
   * @return The property updater.
   */
  public static <T> MongoPropertyUpdater<T> createForObjectWithId(T updated,
      RecordDao mongoServer, Class<T> objectClass, String id, BiConsumer<T, T> preprocessor) {
    return createForObjectWithField(updated, mongoServer, objectClass, ID, id, preprocessor);
  }

  private static <T> MongoPropertyUpdater<T> createForObjectWithField(
      T updated, RecordDao mongoServer, Class<T> objectClass, String fieldName, String fieldValue,
      BiConsumer<T, T> preprocessor) {

    if (StringUtils.isBlank(fieldName) || StringUtils.isBlank(fieldValue)) {
      throw new IllegalArgumentException("Field name and value are required.");
    }

    final Supplier<Query<T>> queryCreator =
        () -> mongoServer.getDatastore().find(objectClass).filter(Filters.eq(fieldName, fieldValue));

    final List<UpdateOperator> updateOperators =
        List.of(UpdateOperators.setOnInsert(Map.of(fieldName, fieldValue)));

    return create(updated, mongoServer, queryCreator, preprocessor, updateOperators);
  }

  private static <T> MongoPropertyUpdater<T> create(T updated, RecordDao mongoServer,
      Supplier<Query<T>> queryCreator, BiConsumer<T, T> dataPreprocessor,
      List<UpdateOperator> updateOperators) {

    // Sanity checks.
    if (updated == null || mongoServer == null || queryCreator == null) {
      throw new IllegalArgumentException();
    }

    // Get the current state from the database and perform preprocessing on it.
    final T current = queryCreator.get().first();
    if (dataPreprocessor != null) {
      dataPreprocessor.accept(current, updated);
    }

    // Done
    return new MongoPropertyUpdaterImpl<>(current, updated, mongoServer, updateOperators,
        queryCreator);
  }

}
