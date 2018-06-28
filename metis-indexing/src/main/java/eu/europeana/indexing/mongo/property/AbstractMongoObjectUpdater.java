package eu.europeana.indexing.mongo.property;

import eu.europeana.corelib.storage.MongoServer;

/**
 * Updater of mongo objects.
 * 
 * @param <T> The type of the object to update.
 */
public abstract class AbstractMongoObjectUpdater<T> {

  /**
   * Update a property.
   * 
   * @param newEntity The new entity (to take the new values from).
   * @param mongoServer The mongo server.
   * @return The updated entity.
   */
  public final T update(T newEntity, MongoServer mongoServer) {
    preprocessEntity(newEntity);
    final MongoPropertyUpdater<T> propertyUpdater = createPropertyUpdater(newEntity, mongoServer);
    update(propertyUpdater);
    return propertyUpdater.applyOperations();
  }

  /**
   * Create a property updater for this mongo object.
   * 
   * @param newEntity The new entity (to take the values from).
   * @param mongoServer The mongo server.
   * @return The property updater for the given entity.
   */
  protected abstract MongoPropertyUpdater<T> createPropertyUpdater(T newEntity,
      MongoServer mongoServer);

  /**
   * This method allows subclasses to perform preprocessing on the entity before saving it to the
   * database. The default behaviour is to do nothing.
   * 
   * @param newEntity The entity
   */
  protected void preprocessEntity(T newEntity) {
    // Nothing to do.
  }

  /**
   * This method performs the actual updates on the property updater.
   * 
   * @param propertyUpdater The updater to update.
   */
  protected abstract void update(MongoPropertyUpdater<T> propertyUpdater);

}
