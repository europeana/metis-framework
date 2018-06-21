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

  T update(T mongoEntity, T newEntity, MongoServer mongoServer);
}
