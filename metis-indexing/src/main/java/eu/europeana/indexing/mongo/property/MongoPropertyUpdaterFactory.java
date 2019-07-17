package eu.europeana.indexing.mongo.property;

import eu.europeana.corelib.storage.MongoServer;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.apache.commons.lang.StringUtils;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

/**
 * This class is a factory class for instances of {@link MongoPropertyUpdater}.
 */
public final class MongoPropertyUpdaterFactory {

  private static final String ABOUT_FIELD = "about";

  private MongoPropertyUpdaterFactory() {
  }

  private static <T> MongoPropertyUpdater<T> create(T updated,
      MongoServer mongoServer, Class<T> objectClass, Supplier<Query<T>> queryCreator,
      BiConsumer<T, T> dataPreprocessor, Consumer<UpdateOperations<T>> operationsPreprocessor) {

    // Sanity checks.
    if (updated == null || mongoServer == null || objectClass == null || queryCreator == null) {
      throw new IllegalArgumentException();
    }

    // Initialize the mongo operations: set the class name on insert if needed and perform preprocessing on it.
    final UpdateOperations<T> mongoOperations = mongoServer.getDatastore()
        .createUpdateOperations(objectClass);
    if (operationsPreprocessor != null) {
      operationsPreprocessor.accept(mongoOperations);
    }

    // Obtain the current state from the database and perform preprocessing on it.
    final T current = queryCreator.get().get();
    if (dataPreprocessor != null) {
      dataPreprocessor.accept(current, updated);
    }

    // Done
    return new MongoPropertyUpdaterImpl<>(current, updated, mongoServer, mongoOperations,
        queryCreator);
  }

  /**
   * Static constructor for objects that do not have an about field.
   *
   * @param updated The updated object (i.e. the object to take the value from). This object will
   * remain unchanged.
   * @param mongoServer The Mongo connection.
   * @param objectClass The class of the object which is used to create an instance of {@link
   * UpdateOperations}.
   * @param queryCreator The function that creates the mongo query that can retrieve the object from
   * Mongo.
   * @param preprocessor This provides the option of performing some preprocessing on the current
   * and/or the new object before applying the operations. Its two parameters are first the current
   * bean (found in the database) and second the updated (as passed to this method). Can be null.
   * @return The property updater.
   */
  public static <T> MongoPropertyUpdater<T> createForObjectWithoutAbout(T updated,
      MongoServer mongoServer, Class<T> objectClass, Supplier<Query<T>> queryCreator,
      BiConsumer<T, T> preprocessor) {
    return create(updated, mongoServer, objectClass, queryCreator, preprocessor, null);
  }

  /**
   * Static constructor for objects that have an about field.
   *
   * @param updated The updated object (i.e. the object to take the value from). This object will
   * remain unchanged.
   * @param mongoServer The Mongo connection.
   * @param objectClass The class of the object which is used to create an instance of {@link
   * UpdateOperations}.
   * @param aboutGetter The function that obtains the about value from the object.
   * @param preprocessor This provides the option of performing some preprocessing on the current
   * and/or the new object before applying the operations. Its two parameters are first the current
   * bean (found in the database) and second the updated (as passed to this method). Can be null.
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

    // Set the about.
    final Consumer<UpdateOperations<T>> operationsPreprocessor = operations -> operations
        .setOnInsert(ABOUT_FIELD, aboutGetter.apply(updated));

    // Done
    return create(updated, mongoServer, objectClass, queryCreator, preprocessor,
        operationsPreprocessor);
  }
}
