package eu.europeana.indexing.mongo.property;

import eu.europeana.corelib.storage.MongoServer;

/**
 * Updater of mongo objects.
 *
 * @param <R> The type of the record to update.
 * @param <A> The type of the ancestor information (information from parents).
 */
public interface MongoObjectUpdater<R, A> {

  /**
   * Update a property.
   *
   * @param newEntity The new entity (to take the new values from).
   * @param ancestorInformation The ancestor information for this entity.
   * @param mongoServer The mongo server.
   * @return The updated entity.
   */
  R update(R newEntity, A ancestorInformation, MongoServer mongoServer);
}
