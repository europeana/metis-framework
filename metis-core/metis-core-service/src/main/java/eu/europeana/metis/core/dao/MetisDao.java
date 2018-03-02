package eu.europeana.metis.core.dao;


/**
 * Interface specifying the minimum methods for a DAO persisting
 *
 * @param <T> the type of class that should be used as an entry
 * @param <S> the type of class that should be used for return values
 */
public interface MetisDao<T, S> {

  /**
   * Create an entry in the database.
   *
   * @param t the class to be stored
   * @return a value when the method finishes, can be different than the one stored.
   */
  S create(T t);

  /**
   * Update an entry in the database.
   *
   * @param t the class to be updated
   * @return a value when the method finishes, can be different than the one stored.
   */
  S update(T t);

  /**
   * Get an entry from the database using the identifier used in the database for unique identification.
   *
   * @param id the identifier to find the entry
   * @return the entry in the database
   */
  T getById(S id);

  /**
   * Delete an entry in the database.
   *
   * @param t the class to be delete
   * @return boolean that indicates the status of the deletion
   */
  boolean delete(T t);
}
