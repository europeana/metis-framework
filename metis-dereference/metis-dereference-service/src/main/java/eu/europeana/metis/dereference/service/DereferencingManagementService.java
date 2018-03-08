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
     * @param uri The uri of the entity.
     * @param xml The entity as XML string.
     */
    void updateEntity(String uri, String xml);

    /**
     * Retrieve a vocabulary by name
     * @param name The name of the vocabulary that we are looking for.
     * @return the Vocabulary that has that name, or null if no such vocabulary could be found.
     */
    Vocabulary findByName(String name);

    /**
     * Empty the cache
     */
    void emptyCache();
}
