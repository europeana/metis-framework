package eu.europeana.indexing.mongo.property;

import eu.europeana.metis.mongo.dao.RecordDao;

/**
 * Manager of mongo objects.
 *
 * @param <R> The type of the object to manage.
 * @param <A> The type of the ancestor information (information from parents).
 */
public interface MongoObjectManager<R, A> extends MongoObjectUpdater<R, A> {

  /**
   * Delete object.
   *
   * @param ancestorInformation The ancestor information for this object, which should be enough to
   * locate the object.
   * @param mongoServer The mongo server.
   */
  void delete(A ancestorInformation, RecordDao mongoServer);

}
