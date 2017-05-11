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
 * Created by ymamakis on 2/17/16.
 */
public interface MetisDao<T, S> {

    /**
     * Create a dataset or organization
     * @param t The dataset or organization to create
     */
    S create(T t);

    /**
     * Update a dataset or organization
     * @param t The dataset or organization to update
     */
    S update(T t);

    /**
     * Get a dataset or organization by id
     * @param id The id to look for
     * @return The entity to return
     */
    T getById(S id);

    /**
     * Delete an entity form the database
     * @param t
     */
    boolean delete(T t);


}
