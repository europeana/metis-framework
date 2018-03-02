package eu.europeana.metis.dereference.service.dao;

/**
 * Abstract DAO
 * Created by ymamakis on 2/11/16.
 */
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

    /**
     * Update a Vocabulary or Entity
     * @param uri The uri to update
     * @param entity The entity to update
     */
    void update(String uri, T entity);

}
