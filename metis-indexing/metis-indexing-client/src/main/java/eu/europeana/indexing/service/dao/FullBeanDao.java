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

import eu.europeana.corelib.definitions.edm.entity.EuropeanaAggregation;
import eu.europeana.corelib.definitions.edm.entity.WebResource;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.corelib.solr.entity.AggregationImpl;
import eu.europeana.corelib.solr.entity.ConceptImpl;
import eu.europeana.corelib.solr.entity.LicenseImpl;
import eu.europeana.corelib.solr.entity.PlaceImpl;
import eu.europeana.corelib.solr.entity.ProvidedCHOImpl;
import eu.europeana.corelib.solr.entity.ProxyImpl;
import eu.europeana.corelib.solr.entity.ServiceImpl;
import eu.europeana.corelib.solr.entity.TimespanImpl;
import eu.europeana.indexing.service.mongo.MorphiaDatastoreProvider;
import java.util.List;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import com.mongodb.MongoClient;

public class FullBeanDao {
    private final MorphiaDatastoreProvider morphiaDatastoreProvider;    
    private static final String ABOUT = "about";

    public FullBeanDao(MorphiaDatastoreProvider morphiaDatastoreProvider) {
    	this.morphiaDatastoreProvider = morphiaDatastoreProvider;
    }

    public  <T> T searchByAbout(Class<T> clazz, String about) {
		return morphiaDatastoreProvider.getDatastore().find(clazz).filter(ABOUT, about).get();
	}

    public void delete(String name) {
    	morphiaDatastoreProvider.getDatastore().delete(morphiaDatastoreProvider.getDatastore().createQuery(FullBeanImpl.class).filter(ABOUT, name));
    }

    public void update(FullBeanImpl entity) {
        Query<FullBeanImpl> query = morphiaDatastoreProvider.getDatastore().createQuery(FullBeanImpl.class).filter(ABOUT, entity.getAbout());
        UpdateOperations<FullBeanImpl> ops = morphiaDatastoreProvider.getDatastore().createUpdateOperations(FullBeanImpl.class);

        morphiaDatastoreProvider.getDatastore().update(query, ops);
    }

    public List<FullBeanImpl> getAll() {
        return morphiaDatastoreProvider.getDatastore().find(FullBeanImpl.class).asList();
    }

    public FullBeanImpl findByName(String name) {
        return morphiaDatastoreProvider.getDatastore().find(FullBeanImpl.class).filter(ABOUT, name).get();
    }

    public  FullBeanImpl getFullBean(String id) {
		if (morphiaDatastoreProvider.getDatastore().find(FullBeanImpl.class).field(ABOUT).equal(id).get() != null) {
			return morphiaDatastoreProvider.getDatastore().find(FullBeanImpl.class).field(ABOUT).equal(id).get();
		}
		return null;
	}

	public void save(FullBeanImpl fBean) {
		morphiaDatastoreProvider.getDatastore().save(fBean);
	}
	
	public void save(AgentImpl agent) {
		morphiaDatastoreProvider.getDatastore().save(agent);
	}
	
	public void save(PlaceImpl place) {
		morphiaDatastoreProvider.getDatastore().save(place);
	}
	
	public void save(TimespanImpl timespan) {
		morphiaDatastoreProvider.getDatastore().save(timespan);
	}
	
	public void save(LicenseImpl license) {
		morphiaDatastoreProvider.getDatastore().save(license);
	}
	
	public void save(ServiceImpl service) {
		morphiaDatastoreProvider.getDatastore().save(service);
	}
	
	public void save(ConceptImpl concept) {
		morphiaDatastoreProvider.getDatastore().save(concept);
	}
	
	public void save(EuropeanaAggregation europeanaAggregation) {
		morphiaDatastoreProvider.getDatastore().save(europeanaAggregation);
	}
	
	public void saveProvidedCHOs(List<ProvidedCHOImpl> providedCHOs) {
		morphiaDatastoreProvider.getDatastore().save(providedCHOs);		
	}

	public void saveProxies(List<ProxyImpl> proxies) {
		morphiaDatastoreProvider.getDatastore().save(proxies);		
	}
       
	public void saveAggregations(List<AggregationImpl> aggregations) {
		morphiaDatastoreProvider.getDatastore().save(aggregations);		
	}
	
	public void saveWebResources(List<? extends WebResource> webResources) {
		morphiaDatastoreProvider.getDatastore().save(webResources);		
	}

	public MongoClient getMongo() {
		return morphiaDatastoreProvider.getDatastore().getMongo();
	}
	
	public String getDBName() {
		return morphiaDatastoreProvider.getDatastore().getDB().getName();
	}
}
