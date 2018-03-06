package eu.europeana.metis.dereference.service.dao;

/**
 * Abstract class of DAO that accesses persistence regarding resources.
 * 
 * @param <T> The type of the resource that this DAO manages.
 */
public interface AbstractDao<T> {

  /**
   * Get an Entity by URL
   * 
   * @param resourceId The resource ID (URI) to retrieve
   * @return A list of Entity
   */
  T get(String resourceId);

  /**
   * Save a vocabulary or entity
   * 
   * @param entity The vocabulary or entity to save
   */
  void save(T entity);

  /**
   * Delete a Vocabulary or Entity
   * 
   * @param resourceId The entity to delete
   */
  void delete(String resourceId);

  /**
   * Update a Vocabulary or Entity
   * 
   * @param resourceId The resource to update
   * @param entity The entity to update
   */
  void update(String resourceId, T entity);

}
