/*
 * Copyright 2007-2013 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */
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
    public OriginalEntity get(String resourceId) {
        return ds.find(OriginalEntity.class).filter("URI", resourceId).get();
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
