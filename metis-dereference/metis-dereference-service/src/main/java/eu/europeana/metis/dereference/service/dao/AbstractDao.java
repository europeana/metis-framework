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
package eu.europeana.metis.dereference.service.dao;

/**
 * Abstract DAO
 * Created by ymamakis on 2/11/16.
 */
public interface  AbstractDao<T> {

    /**
     * Get an Entity by URL
     * @param resourceId The resource ID (URI) to retrieve
     * @return A list of Entity
     */
     T get(String resourceId);

    /**
     * Save a vocabulary or entity
     * @param entity The vocabulary or entity to save
     */
    void save(T entity);

    /**
     * Delete a Vocabulary or Entity
     * @param resourceId The entity to delete
     */
    void delete(String resourceId);

    /**
     * Update a Vocabulary or Entity
     * @param resourceId The resource to update
     * @param entity The entity to update
     */
    void update(String resourceId, T entity);

}
