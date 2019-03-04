package eu.europeana.indexing.mongo.property;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import eu.europeana.corelib.definitions.edm.entity.Agent;
import eu.europeana.corelib.definitions.edm.entity.Aggregation;
import eu.europeana.corelib.definitions.edm.entity.Concept;
import eu.europeana.corelib.definitions.edm.entity.EuropeanaAggregation;
import eu.europeana.corelib.definitions.edm.entity.License;
import eu.europeana.corelib.definitions.edm.entity.Place;
import eu.europeana.corelib.definitions.edm.entity.ProvidedCHO;
import eu.europeana.corelib.definitions.edm.entity.Proxy;
import eu.europeana.corelib.definitions.edm.entity.Service;
import eu.europeana.corelib.definitions.edm.entity.Timespan;
import eu.europeana.corelib.definitions.solr.DocType;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.corelib.solr.entity.AggregationImpl;
import eu.europeana.corelib.solr.entity.ConceptImpl;
import eu.europeana.corelib.solr.entity.EuropeanaAggregationImpl;
import eu.europeana.corelib.solr.entity.LicenseImpl;
import eu.europeana.corelib.solr.entity.PlaceImpl;
import eu.europeana.corelib.solr.entity.ProvidedCHOImpl;
import eu.europeana.corelib.solr.entity.ProxyImpl;
import eu.europeana.corelib.solr.entity.ServiceImpl;
import eu.europeana.corelib.solr.entity.TimespanImpl;
import java.util.Date;
import org.junit.jupiter.api.Test;

class FullBeanUpdaterTest extends MongoEntityUpdaterTest<FullBeanImpl> {

  @Override
  FullBeanImpl createEmptyMongoEntity() {
    return new FullBeanImpl();
  }

  @Test
  void testUpdate() {

    // Craete objects for execution
    final FullBeanUpdater updater = new FullBeanUpdater(null);
    @SuppressWarnings("unchecked") final MongoPropertyUpdater<FullBeanImpl> propertyUpdater = mock(
        MongoPropertyUpdater.class);

    // Make the call
    updater.update(propertyUpdater, null);

    // Test all the values
    testArrayPropertyUpdate(propertyUpdater, "title", FullBeanImpl::setTitle);
    testArrayPropertyUpdate(propertyUpdater, "year", FullBeanImpl::setYear);
    testArrayPropertyUpdate(propertyUpdater, "provider", FullBeanImpl::setProvider);
    testArrayPropertyUpdate(propertyUpdater, "language", FullBeanImpl::setLanguage);
    testArrayPropertyUpdate(propertyUpdater, "country", FullBeanImpl::setCountry);
    testArrayPropertyUpdate(propertyUpdater, "europeanaCollectionName",
        FullBeanImpl::setEuropeanaCollectionName);
    testObjectPropertyUpdate(propertyUpdater, "timestampCreated", FullBeanImpl::setTimestampCreated,
        new Date());
    testObjectPropertyUpdate(propertyUpdater, "timestampUpdated", FullBeanImpl::setTimestampUpdated,
        new Date());
    testObjectPropertyUpdate(propertyUpdater, "type", FullBeanImpl::setType, DocType.IMAGE);
    testObjectPropertyUpdate(propertyUpdater, "europeanaCompleteness",
        FullBeanImpl::setEuropeanaCompleteness, 5);
    this.<Place, Void>testReferencedEntitiesPropertyUpdate(propertyUpdater, "places",
        FullBeanImpl::setPlaces, null, PlaceImpl::new);
    this.<Agent, Void>testReferencedEntitiesPropertyUpdate(propertyUpdater, "agents",
        FullBeanImpl::setAgents, null, AgentImpl::new);
    this.<Timespan, Void>testReferencedEntitiesPropertyUpdate(propertyUpdater, "timespans",
        FullBeanImpl::setTimespans, null, TimespanImpl::new);
    this.<Concept, Void>testReferencedEntitiesPropertyUpdate(propertyUpdater, "concepts",
        FullBeanImpl::setConcepts, null, ConceptImpl::new);
    this.<ProvidedCHO, Void>testReferencedEntitiesPropertyUpdate(propertyUpdater, "providedCHOs",
        FullBeanImpl::setProvidedCHOs, null, ProvidedCHOImpl::new);
    this.<Aggregation, RootAboutWrapper>testReferencedEntitiesPropertyUpdate(propertyUpdater,
        "aggregations",
        FullBeanImpl::setAggregations, RootAboutWrapper.class, AggregationImpl::new);
    this.<EuropeanaAggregation, RootAboutWrapper>testReferencedEntityPropertyUpdate(propertyUpdater,
        "europeanaAggregation", FullBeanImpl::setEuropeanaAggregation, RootAboutWrapper.class,
        EuropeanaAggregationImpl::new);
    this.<Proxy, Void>testReferencedEntitiesPropertyUpdate(propertyUpdater, "proxies",
        FullBeanImpl::setProxies, null, ProxyImpl::new);
    this.<Service, Void>testReferencedEntitiesPropertyUpdate(propertyUpdater, "services",
        FullBeanImpl::setServices, null, ServiceImpl::new);
    this.<License, Void>testReferencedEntitiesPropertyUpdate(propertyUpdater, "licenses",
        FullBeanImpl::setLicenses, null, LicenseImpl::new);

    // And that should be it.
    verifyNoMoreInteractions(propertyUpdater);
  }
}
