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
package eu.europeana.indexing.utils;

public interface  AbstractDao<T> {

    /**
     * Get an Entityby URL
     * @param uri The uri to retrieve from
     * @return A list of Entity
     */
     T getByUri(String uri);

    /**
     * Save a vocabulary or entity
     * @param entity The vocabulary or entity to save
     */
    void save(T entity);

    /**
     * Delete a Vocabulary or Entity
     * @param uri The entity to delete
     */
    void delete(String uri);
}
