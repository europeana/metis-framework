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
package eu.europeana.indexing.service.dao;

import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.indexing.service.mongo.MorphiaDatastoreProvider;
import java.util.List;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

public class FullBeanDao {
    private final MorphiaDatastoreProvider morphiaDatastoreProvider;
    private final Datastore ds;

    public FullBeanDao(MorphiaDatastoreProvider morphiaDatastoreProvider) {
    	this.morphiaDatastoreProvider = morphiaDatastoreProvider;
    	ds = getDS();
    }

    public  <T> T searchByAbout(Class<T> clazz, String about) {

		return ds.find(clazz).filter("about", about).get();
	}

    public void delete(String name) {
        ds.delete(ds.createQuery(FullBeanImpl.class).filter("about", name));
    }

    public void update(FullBeanImpl entity) {
        Query<FullBeanImpl> query = ds.createQuery(FullBeanImpl.class).filter("about", entity.getAbout());
        UpdateOperations<FullBeanImpl> ops = ds.createUpdateOperations(FullBeanImpl.class);

        ds.update(query, ops);
    }

    public List<FullBeanImpl> getAll() {
        return ds.find(FullBeanImpl.class).asList();
    }

    public FullBeanImpl findByName(String name) {
        return ds.find(FullBeanImpl.class).filter("about", name).get();
    }

    public  FullBeanImpl getFullBean(String id) {
		if (ds.find(FullBeanImpl.class).field("about").equal(id).get() != null) {
			return ds.find(FullBeanImpl.class).field("about").equal(id)
					.get();
		}
		return null;
	}

	public void save(FullBeanImpl fBean) {
		ds.save(fBean);
	}
	
	public Datastore getDS() {
		return morphiaDatastoreProvider.getDatastore();
	}	
}
