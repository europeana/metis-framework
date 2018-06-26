package eu.europeana.indexing.mongo.property;

import java.util.ArrayList;
import eu.europeana.corelib.definitions.edm.entity.Proxy;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.entity.ProxyImpl;
import eu.europeana.corelib.storage.MongoServer;

/**
 * Field updater for instances of {@link FullBeanImpl}.
 */
public class FullBeanUpdater extends AbstractMongoObjectUpdater<FullBeanImpl> {

  @Override
  protected MongoPropertyUpdater<FullBeanImpl> createPropertyUpdater(FullBeanImpl newEntity,
      MongoServer mongoServer) {
    return MongoPropertyUpdater.createForFullBean(newEntity, mongoServer);
  }

  @Override
  protected void preprocessEntity(FullBeanImpl fullBean) {
    // To avoid potential index out of bounds
    if (fullBean.getProxies().isEmpty()) {
      ArrayList<Proxy> proxyList = new ArrayList<>();
      ProxyImpl proxy = new ProxyImpl();
      proxyList.add(proxy);
      fullBean.setProxies(proxyList);
    }
  }

  @Override
  protected void update(MongoPropertyUpdater<FullBeanImpl> propertyUpdater) {
    propertyUpdater.updateArray("title", FullBeanImpl::getTitle);
    propertyUpdater.updateArray("title", FullBeanImpl::getTitle);
    propertyUpdater.updateArray("year", FullBeanImpl::getYear);
    propertyUpdater.updateArray("provider", FullBeanImpl::getProvider);
    propertyUpdater.updateArray("language", FullBeanImpl::getLanguage);
    propertyUpdater.updateArray("country", FullBeanImpl::getCountry);
    propertyUpdater.updateArray("europeanaCollectionName",
        FullBeanImpl::getEuropeanaCollectionName);
    propertyUpdater.updateObject("type", FullBeanImpl::getType);
    propertyUpdater.updateObject("europeanaCompleteness", FullBeanImpl::getEuropeanaCompleteness);
    propertyUpdater.updateReferencedEntities("places", FullBeanImpl::getPlaces, new PlaceUpdater());
    propertyUpdater.updateReferencedEntities("agents", FullBeanImpl::getAgents, new AgentUpdater());
    propertyUpdater.updateReferencedEntities("timespans", FullBeanImpl::getTimespans,
        new TimespanUpdater());
    propertyUpdater.updateReferencedEntities("concepts", FullBeanImpl::getConcepts,
        new ConceptUpdater());
    propertyUpdater.updateReferencedEntities("providedCHOs", FullBeanImpl::getProvidedCHOs,
        new ProvidedChoUpdater());
    propertyUpdater.updateReferencedEntities("aggregations", FullBeanImpl::getAggregations,
        new AggregationUpdater());
    propertyUpdater.updateReferencedEntity("europeanaAggregation",
        FullBeanImpl::getEuropeanaAggregation, new EuropeanaAggregationUpdater());
    propertyUpdater.updateReferencedEntities("proxies", FullBeanImpl::getProxies,
        new ProxyUpdater());
    propertyUpdater.updateReferencedEntities("services", FullBeanImpl::getServices,
        new ServiceUpdater());
    propertyUpdater.updateReferencedEntities("licenses", FullBeanImpl::getLicenses,
        new LicenseUpdater());
  }
}
