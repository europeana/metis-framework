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

}
