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

import com.mongodb.MongoClient;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;

import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.corelib.solr.entity.AggregationImpl;
import eu.europeana.corelib.solr.entity.BasicProxyImpl;
import eu.europeana.corelib.solr.entity.ConceptImpl;
import eu.europeana.corelib.solr.entity.ConceptSchemeImpl;
import eu.europeana.corelib.solr.entity.EuropeanaAggregationImpl;
import eu.europeana.corelib.solr.entity.EventImpl;
import eu.europeana.corelib.solr.entity.PhysicalThingImpl;
import eu.europeana.corelib.solr.entity.PlaceImpl;
import eu.europeana.corelib.solr.entity.ProvidedCHOImpl;
import eu.europeana.corelib.solr.entity.ProxyImpl;
import eu.europeana.corelib.solr.entity.TimespanImpl;
import eu.europeana.corelib.solr.entity.WebResourceImpl;


import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import java.util.List;

public class FullBeanDao {
    private Datastore ds;
    private MongoClient mongo;
    private String db;

    public FullBeanDao(MongoClient mongo, String db) {
    	this.mongo = mongo;
    	this.db = db;
    	
        Morphia morphia = new Morphia();
        
        morphia.map(FullBeanImpl.class);
        morphia.map(ProvidedCHOImpl.class);
        morphia.map(AgentImpl.class);
        morphia.map(AggregationImpl.class);
        morphia.map(ConceptImpl.class);
        morphia.map(ProxyImpl.class);
        morphia.map(PlaceImpl.class);
        morphia.map(TimespanImpl.class);
        morphia.map(WebResourceImpl.class);
        morphia.map(EuropeanaAggregationImpl.class);
        morphia.map(EventImpl.class);
        morphia.map(PhysicalThingImpl.class);
        morphia.map(ConceptSchemeImpl.class);
        morphia.map(BasicProxyImpl.class);
        
        ds = morphia.createDatastore(mongo, db);
        
        ds.ensureIndexes();
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

    public void setDs(Datastore ds) {
        this.ds = ds;
    }
    
    public Datastore getDS() {
    	return ds;
    }
    
    public MongoClient getMongoClient() {
    	return mongo;
    }
    
    public String getDB() {
    	return db;
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
}
