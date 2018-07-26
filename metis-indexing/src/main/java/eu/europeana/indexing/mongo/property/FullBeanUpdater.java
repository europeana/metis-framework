package eu.europeana.indexing.mongo.property;

import java.util.ArrayList;
import java.util.function.BiConsumer;
import eu.europeana.corelib.definitions.edm.entity.Proxy;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.entity.EuropeanaAggregationImpl;
import eu.europeana.corelib.solr.entity.ProxyImpl;
import eu.europeana.corelib.storage.MongoServer;

/**
 * Field updater for instances of {@link FullBeanImpl}.
 */
public class FullBeanUpdater extends AbstractMongoObjectUpdater<FullBeanImpl> {

  private final BiConsumer<FullBeanImpl, FullBeanImpl> fullBeanPreprocessor;

  /**
   * Constructor.
   * 
   * @param fullBeanPreprocessor This is functionality that will be executed as soon as we have
   *        retrieved the current version of the full bean from the database. It will be called
   *        once. It's first parameter is the current full bean (as retrieved from the database) and
   *        its second parameter is the updated full bean (as passed to
   *        {@link #createPropertyUpdater(FullBeanImpl, MongoServer)}).
   */
  public FullBeanUpdater(BiConsumer<FullBeanImpl, FullBeanImpl> fullBeanPreprocessor) {
    this.fullBeanPreprocessor = fullBeanPreprocessor;
  }

  @Override
  protected MongoPropertyUpdater<FullBeanImpl> createPropertyUpdater(FullBeanImpl newEntity,
      MongoServer mongoServer) {
    return MongoPropertyUpdater.createForFullBean(newEntity, mongoServer, fullBeanPreprocessor);
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
    propertyUpdater.updateArray("year", FullBeanImpl::getYear);
    propertyUpdater.updateArray("provider", FullBeanImpl::getProvider);
    propertyUpdater.updateArray("language", FullBeanImpl::getLanguage);
    propertyUpdater.updateArray("country", FullBeanImpl::getCountry);
    propertyUpdater.updateArray("europeanaCollectionName",
        FullBeanImpl::getEuropeanaCollectionName);
    propertyUpdater.updateObject("timestampCreated", FullBeanImpl::getTimestampCreated);
    propertyUpdater.updateObject("timestampUpdated", FullBeanImpl::getTimestampUpdated);
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
        FullBeanUpdater::getEuropeanaAggregationFromFullBean, new EuropeanaAggregationUpdater());
    propertyUpdater.updateReferencedEntities("proxies", FullBeanImpl::getProxies,
        new ProxyUpdater());
    propertyUpdater.updateReferencedEntities("services", FullBeanImpl::getServices,
        new ServiceUpdater());
    propertyUpdater.updateReferencedEntities("licenses", FullBeanImpl::getLicenses,
        new LicenseUpdater());
  }

  private static EuropeanaAggregationImpl getEuropeanaAggregationFromFullBean(
      FullBeanImpl fullBean) {
    return (EuropeanaAggregationImpl) fullBean.getEuropeanaAggregation();
  }
}
