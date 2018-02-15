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
package eu.europeana.indexing.service.mongo;

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

public class MorphiaDatastoreProvider {

  private Datastore datastore;

  public MorphiaDatastoreProvider(MongoClient mongoClient, String db) {
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

	  datastore = morphia.createDatastore(mongoClient, db);
	  datastore.ensureIndexes();
  }

  public Datastore getDatastore() {
    return datastore;
  }
}
