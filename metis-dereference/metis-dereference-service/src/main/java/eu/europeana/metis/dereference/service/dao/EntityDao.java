package eu.europeana.metis.dereference.service.dao;

import com.mongodb.MongoClient;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import eu.europeana.metis.dereference.OriginalEntity;
import eu.europeana.metis.dereference.ProcessedEntity;

/**
 * DAO for entities
 */
public class EntityDao<T> {

  private final Datastore ds;
  private final Class<T> entityType;
  private final String resourceIdField;

  /**
   * Constructor.
   *
   * @param mongo Client to the mongo database.
   * @param databaseName The name of the database.
   * @param entityType The type of entity.
   * @param resourceIdField The name of the field containing the resource ID.
   */
  public EntityDao(MongoClient mongo, String databaseName, Class<T> entityType,
          String resourceIdField) {
    final Morphia morphia = new Morphia();
    morphia.map(entityType);
    this.ds = morphia.createDatastore(mongo, databaseName);
    this.entityType = entityType;
    this.resourceIdField = resourceIdField;
  }

  /**
   * Get an entity by resource ID.
   *
   * @param resourceId The resource ID (URI) to retrieve
   * @return The entity with the given resource ID.
   */
  public T get(String resourceId) {
    return ds.find(entityType).filter(resourceIdField, resourceId).first();
  }

  /**
   * Save an entity.
   *
   * @param entity The vocabulary or entity to save
   */
  public void save(T entity) {
    ds.save(entity);
  }

  /**
   * Remove all entities.
   */
  public void purgeAll() {
    ds.delete(ds.find(entityType));
  }

  /**
   * Create an instance for original entities.
   *
   * @param mongo Client to the mongo database.
   * @param databaseName The name of the database.
   * @return The DAO instance.
   */
  public static EntityDao<OriginalEntity> createForOriginalEntity(MongoClient mongo,
          String databaseName) {
    return new EntityDao<>(mongo, databaseName, OriginalEntity.class, "URI");
  }

  /**
   * Create an instance for processed entities.
   *
   * @param mongo Client to the mongo database.
   * @param databaseName The name of the database.
   * @return The DAO instance.
   */
  public static EntityDao<ProcessedEntity> createForProcessedEntity(MongoClient mongo,
          String databaseName) {
    return new EntityDao<>(mongo, databaseName, ProcessedEntity.class, "resourceId");
  }
}
