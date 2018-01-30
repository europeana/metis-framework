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
package eu.europeana.indexing.utils;

import com.mongodb.MongoClient;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

public class FullBeanDao implements AbstractDao<FullBeanImpl> {
    private Datastore ds;

    public FullBeanDao(MongoClient mongo, String db) {
        Morphia morphia = new Morphia();
        morphia.map(FullBeanImpl.class);
        ds = morphia.createDatastore(mongo, db);

    }

    @Override
    public FullBeanImpl getByUri(String uri) {
        return ds.find(FullBeanImpl.class).filter("URI", uri).get();
    }
    
    public FullBeanImpl getById(String id) {
    	
    	if (ds.find(FullBeanImpl.class).field("about").equal(id).get() != null)
    		return ds.find(FullBeanImpl.class).field("about").equal(id).get();
    	
    	return null;
    }

    @Override
    public void save(FullBeanImpl entity) {
        ds.save(entity);
    }

    @Override
    public void delete(String uri) {
        ds.delete(ds.createQuery(FullBeanImpl.class).filter("URI", uri));
    }
}
