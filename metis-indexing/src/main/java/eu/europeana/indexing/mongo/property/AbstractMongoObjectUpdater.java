package eu.europeana.indexing.mongo.property;

import eu.europeana.corelib.storage.MongoServer;

/**
 * Updater of mongo objects.
 * 
 * @param <R> The type of the record to update.
 * @param <A> The type of the ancestor information (information from parents).
 */
public abstract class AbstractMongoObjectUpdater<R, A> {

  /**
   * Update a property.
   * 
   * @param newEntity The new entity (to take the new values from).
   * @param ancestorInformation The ancestor information for this entity.
   * @param mongoServer The mongo server.
   * @return The updated entity.
   */
  public final R update(R newEntity, A ancestorInformation, MongoServer mongoServer) {
    preprocessEntity(newEntity, ancestorInformation);
    final MongoPropertyUpdater<R> propertyUpdater =
        createPropertyUpdater(newEntity, ancestorInformation, mongoServer);
    update(propertyUpdater, ancestorInformation);
    return propertyUpdater.applyOperations();
  }

  /**
   * Create a property updater for this mongo object.
   * 
   * @param newEntity The new entity (to take the values from).
   * @param ancestorInformation The ancestor information for this entity.
   * @param mongoServer The mongo server.
   * @return The property updater for the given entity.
   */
  protected abstract MongoPropertyUpdater<R> createPropertyUpdater(R newEntity,
      A ancestorInformation, MongoServer mongoServer);

  /**
   * This method allows subclasses to perform preprocessing on the entity before saving it to the
   * database. The default behaviour is to do nothing.
   * 
   * @param newEntity The entity
   * @param ancestorInformation The ancestor information for this entity.
   */
  protected void preprocessEntity(R newEntity, A ancestorInformation) {
    // Nothing to do.
  }

  /**
   * This method performs the actual updates on the property updater.
   * 
   * @param propertyUpdater The updater to update.
   * @param ancestorInformation The ancestor information for this entity.
   */
  protected abstract void update(MongoPropertyUpdater<R> propertyUpdater, A ancestorInformation);
}
