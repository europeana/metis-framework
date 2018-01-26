/*
 * Copyright 2007-2013 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */
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
