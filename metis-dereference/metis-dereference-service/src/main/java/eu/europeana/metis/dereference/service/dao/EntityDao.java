package eu.europeana.metis.dereference.service.dao;

import com.mongodb.MongoClient;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import eu.europeana.metis.dereference.OriginalEntity;

/**
 * DAO for original Entities (Mongo)
 */
public class EntityDao implements AbstractDao<OriginalEntity> {

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

  @Override
  public OriginalEntity get(String resourceId) {
    return ds.find(OriginalEntity.class).filter("URI", resourceId).first();
  }

  @Override
  public void save(OriginalEntity entity) {
    ds.save(entity);
  }

  protected Datastore getDatastore() {
    return ds;
  }
}
