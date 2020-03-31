package eu.europeana.metis.dereference.service.dao;

import com.mongodb.MongoClient;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.query.Query;
import dev.morphia.query.UpdateOperations;
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

  @Override
  public void delete(String resourceId) {
    ds.delete(ds.createQuery(OriginalEntity.class).filter("URI", resourceId));
  }

  @Override
  public void update(String resourceId, OriginalEntity entity) {
    Query<OriginalEntity> query = ds.createQuery(OriginalEntity.class).filter("URI", resourceId);
    UpdateOperations<OriginalEntity> ops = ds.createUpdateOperations(OriginalEntity.class);
    ops.set("xml", entity.getXml());
    ds.update(query, ops);
  }
}
