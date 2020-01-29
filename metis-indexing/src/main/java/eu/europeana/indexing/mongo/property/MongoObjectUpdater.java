package eu.europeana.indexing.mongo.property;

import eu.europeana.corelib.storage.MongoServer;
import java.util.Date;

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
   * @param recordDate The date that would represent the created/updated date of a record
   * @param recordIdToRedirectFrom The record that we are redirecting from, this is used so that the created and updated timestamp can be maintained
   * @param mongoServer The mongo server.
   * @return The updated entity.
   */
  R update(R newEntity, A ancestorInformation, Date recordDate, String recordIdToRedirectFrom, MongoServer mongoServer);
}
