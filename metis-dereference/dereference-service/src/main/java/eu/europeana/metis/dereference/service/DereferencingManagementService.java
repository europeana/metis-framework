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
package eu.europeana.metis.dereference.service;

import eu.europeana.metis.dereference.Vocabulary;

import java.util.List;

/**
 * Interface for managing vocabularies
 * Created by ymamakis on 2/11/16.
 */
public interface DereferencingManagementService {
    /**
     * Save a vocabulary
     * @param vocabulary The vocabulary to save
     */
    void saveVocabulary(Vocabulary vocabulary);

    /**
     * Update a vocaublary
     * @param vocabulary The vocabulary to update
     */
    void updateVocabulary(Vocabulary vocabulary);

    /**
     * Delete a vocabulary
     * @param name The name of the vocabulary to delete
     */
    void deleteVocabulary(String name);

    /**
     * List all the vocabularies
     * @return The mapped vocabularies
     */
    List<Vocabulary> getAllVocabularies();

    /**
     * Remove an entity by uri
     * @param uri The uri of the entity
     */
    void removeEntity(String uri);

    /**
     * Update entity by uri
     *
     */
    void updateEntity(String uri, String xml);

    /**
     * Retrieve a vocabulary by name
     */
    Vocabulary findByName(String name);

    /**
     * Empty the cache
     */
    void emptyCache();
}
