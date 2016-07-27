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
package eu.europeana.hierarchies.service.cache;

import java.io.IOException;
import java.util.Set;

/**
 * Interface for a cache DAO
 * Created by ymamakis on 1/25/16.
 */
public interface CacheDAO {
    /**
     * Add a parent to a collection
     * @param collection The collection entry in cache
     * @param parent The parent String
     */
    void addParentToSet(String collection, String parent) throws IOException;

    /**
     * Add a set of parents to a collection
     * @param collection The collection entry in cache
     * @param parents The set of parents to append
     */
    void addParentsToSet(String collection, Set<String> parents) throws IOException;
    /**
     * Retrieve the parents of a collection
     * @param collection The collection to search for in cache
     * @return The Cache Entry for this collection
     */
    CacheEntry getByCollection(String collection) throws IOException;
}
