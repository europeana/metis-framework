package eu.europeana.metis.dereference.service.dao;

import com.mongodb.MongoClient;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import eu.europeana.metis.dereference.ProcessedEntity;

/**
 * DAO for processed entities
 */
public class ProcessedEntityDao {

  private final Datastore ds;

  /**
   * Constructor.
   *
   * @param mongo Client to the mongo database.
   * @param databaseName The name of the database.
   */
  public ProcessedEntityDao(MongoClient mongo, String databaseName) {
    final Morphia morphia = new Morphia();
    morphia.map(ProcessedEntity.class);
    this.ds = morphia.createDatastore(mongo, databaseName);
  }

  /**
   * Get an entity by resource ID.
   *
   * @param resourceId The resource ID (URI) to retrieve
   * @return The entity with the given resource ID.
   */
  public ProcessedEntity get(String resourceId) {
    return ds.find(ProcessedEntity.class).filter("resourceId", resourceId).first();
  }

  /**
   * Save an entity.
   *
   * @param entity The vocabulary or entity to save
   */
  public void save(ProcessedEntity entity) {
    ds.save(entity);
  }

  /**
   * Remove all entities.
   */
  public void purgeAll() {
    ds.delete(ds.find(ProcessedEntity.class));
  }
}
