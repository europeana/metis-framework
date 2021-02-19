package eu.europeana.indexing.mongo.property;

import eu.europeana.metis.mongo.dao.RecordDao;
import java.util.Date;

/**
 * Updater of mongo objects.
 *
 * @param <R> The type of the object to update.
 * @param <A> The type of the ancestor information (information from parents).
 */
public interface MongoObjectUpdater<R, A> {

  /**
   * Update a mongo object.
   *
   * @param newObject The new object (to take the new values from).
   * @param ancestorInformation The ancestor information for this object.
   * @param recordDate The date that would represent the created/updated date of a record
   * @param recordCreationDate The date that would represent the created date if it already existed,
   * @param mongoServer The mongo server.
   * @return The updated entity.
   */
  R update(R newObject, A ancestorInformation, Date recordDate, Date recordCreationDate,
          RecordDao mongoServer);
}
