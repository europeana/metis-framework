package eu.europeana.metis.framework.dao;


/**
 * Interface specifying the minimum methods for a DAO persisting
 * Created by ymamakis on 2/17/16.
 */
public interface MetisDao<T> {

    /**
     * Create a dataset or organization
     * @param t The dataset or organization to create
     */
    void create(T t);

    /**
     * Update a dataset or organization
     * @param t The dataset or organization to update
     */
    void update(T t);

    /**
     * Get a dataset or organization by id
     * @param id The id to look for
     * @return The entity to return
     */
    T getById(String id);

    /**
     * Delete an entity form the database
     * @param t
     */
    void delete(T t);


}
