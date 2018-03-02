package eu.europeana.metis.dereference.service.dao;

import com.mongodb.MongoClient;
import eu.europeana.metis.dereference.OriginalEntity;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

/**
 * DAO for original Entities (Mongo)
 * Created by ymamakis on 2/11/16.
 */

public class EntityDao implements AbstractDao<OriginalEntity> {

    private Datastore ds;

    public EntityDao(MongoClient mongo, String db) {
        Morphia morphia = new Morphia();
        morphia.map(OriginalEntity.class);
        ds = morphia.createDatastore(mongo, db);

    }

    @Override
    public OriginalEntity getByUri(String uri) {
        return ds.find(OriginalEntity.class).filter("URI", uri).get();
    }

    @Override
    public void save(OriginalEntity entity) {
        ds.save(entity);
    }

    @Override
    public void delete(String uri) {
        ds.delete(ds.createQuery(OriginalEntity.class).filter("URI", uri));
    }

    @Override
    public void update(String uri, OriginalEntity entity) {
        Query<OriginalEntity> query = ds.createQuery(OriginalEntity.class).filter("URI", uri);
        UpdateOperations<OriginalEntity> ops = ds.createUpdateOperations(OriginalEntity.class);
        ops.set("xml", entity.getXml());
        ds.update(query, ops);
    }


}
