package eu.europeana.metis.dereference.service.dao;

import com.mongodb.MongoClient;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import eu.europeana.metis.dereference.OriginalEntity;

/**
 * DAO for original Entities (Mongo)
 */
public class EntityDao {

  private final Datastore ds;

  /**
   * Constructor.
   * 
   * @param mongo Client to the mongo database.
   * @param databaseName The name of the database.
   */
  public EntityDao(MongoClient mongo, String databaseName) {
    Morphia morphia = new Morphia();
    morphia.map(OriginalEntity.class);
    ds = morphia.createDatastore(mongo, databaseName);

  }

  /**
   * Get an Entity by URL
   *
   * @param resourceId The resource ID (URI) to retrieve
   * @return A list of Entity
   */
  public OriginalEntity get(String resourceId) {
    return ds.find(OriginalEntity.class).filter("URI", resourceId).first();
  }

  /**
   * Save a vocabulary or entity
   *
   * @param entity The vocabulary or entity to save
   */
  public void save(OriginalEntity entity) {
    ds.save(entity);
  }

  protected Datastore getDatastore() {
    return ds;
  }
}
