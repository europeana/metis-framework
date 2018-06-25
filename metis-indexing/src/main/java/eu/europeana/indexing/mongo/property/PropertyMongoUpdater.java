package eu.europeana.indexing.mongo.property;

import eu.europeana.corelib.storage.MongoServer;

/**
 * Updater of EDM contextual classes
 * 
 * @author Yorgos.Mamakis@ europeana.eu
 *
 * @param <T> The contextual class to update
 */
public interface PropertyMongoUpdater<T> {

  /**
   * Update a property.
   * 
   * @param mongoEntity The current entity (the entity to update).
   * @param newEntity The new entity (to take the new values from).
   * @param mongoServer The mongo server.
   * @return The updated entity.
   */
  T update(T mongoEntity, T newEntity, MongoServer mongoServer);
}
