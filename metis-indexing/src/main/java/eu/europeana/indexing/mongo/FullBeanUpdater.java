package eu.europeana.indexing.mongo;

import eu.europeana.corelib.definitions.edm.beans.FullBean;
import eu.europeana.corelib.definitions.edm.entity.Proxy;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.entity.EuropeanaAggregationImpl;
import eu.europeana.corelib.solr.entity.ProxyImpl;
import eu.europeana.corelib.storage.MongoServer;
import eu.europeana.indexing.mongo.property.MongoPropertyUpdater;
import eu.europeana.indexing.mongo.property.MongoPropertyUpdaterFactory;
import eu.europeana.indexing.mongo.property.RootAboutWrapper;
import eu.europeana.indexing.utils.TriConsumer;

import java.util.ArrayList;
import java.util.Date;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Field updater for instances of {@link FullBeanImpl}.
 */
public class FullBeanUpdater extends AbstractMongoObjectUpdater<FullBeanImpl, Void> {

  private final TriConsumer<FullBeanImpl, FullBeanImpl, Pair<Date, Date>> fullBeanPreprocessor;

  /**
   * Constructor.
   *
   * @param fullBeanPreprocessor This is functionality that will be executed as soon as we have
   * retrieved the current version of the full bean from the database. It will be called once. It's
   * first parameter is the current full bean (as retrieved from the database) and its second
   * parameter is the updated full bean (as passed to {@link AbstractMongoObjectUpdater#createPropertyUpdater(Object,
   * Object, Date, Date, MongoServer)}).
   */
  public FullBeanUpdater(TriConsumer<FullBeanImpl, FullBeanImpl, Pair<Date, Date>> fullBeanPreprocessor) {
    this.fullBeanPreprocessor = fullBeanPreprocessor;
  }

  /**
   * Update the full bean. Convenience method for .
   *
   * @param newEntity The new entity (to take the new values from).
   * @param recordDate The date that would represent the created/updated date of a record
   * @param recordCreationDate The date that would represent the created date if it already existed,
   * e.g. from a redirected record
   * @param mongoServer The mongo server.
   * @return The updated entity.
   */
  public final FullBeanImpl update(FullBeanImpl newEntity, Date recordDate, Date recordCreationDate,
      MongoServer mongoServer) {
    return update(newEntity, null, recordDate, recordCreationDate, mongoServer);
  }

  @Override
  protected MongoPropertyUpdater<FullBeanImpl> createPropertyUpdater(FullBeanImpl newEntity,
      Void ancestorInformation, Date recordDate, Date recordCreationDate,
      MongoServer mongoServer) {
    return MongoPropertyUpdaterFactory.createForObjectWithAbout(newEntity, mongoServer,
        FullBeanImpl.class, FullBeanImpl::getAbout, fullBeanPreprocessor, recordDate,
        recordCreationDate);
  }

  @Override
  protected void preprocessEntity(FullBeanImpl fullBean, Void ancestorInformation) {
    // To avoid potential index out of bounds
    if (fullBean.getProxies().isEmpty()) {
      ArrayList<Proxy> proxyList = new ArrayList<>();
      ProxyImpl proxy = new ProxyImpl();
      proxyList.add(proxy);
      fullBean.setProxies(proxyList);
    }
  }

  @Override
  protected void update(MongoPropertyUpdater<FullBeanImpl> propertyUpdater,
      Void ancestorInformation) {
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
    propertyUpdater.updateReferencedEntities("places", FullBeanImpl::getPlaces, fullBean -> null,
        new PlaceUpdater());
    propertyUpdater.updateReferencedEntities("agents", FullBeanImpl::getAgents, fullBean -> null,
        new AgentUpdater());
    propertyUpdater.updateReferencedEntities("timespans", FullBeanImpl::getTimespans,
        fullBean -> null, new TimespanUpdater());
    propertyUpdater.updateReferencedEntities("concepts", FullBeanImpl::getConcepts,
        fullBean -> null, new ConceptUpdater());
    propertyUpdater.updateReferencedEntities("providedCHOs", FullBeanImpl::getProvidedCHOs,
        fullBean -> null, new ProvidedChoUpdater());
    propertyUpdater.updateReferencedEntities("aggregations", FullBeanImpl::getAggregations,
        FullBeanUpdater::createRootAbout, new AggregationUpdater());
    propertyUpdater.updateReferencedEntity("europeanaAggregation",
        FullBeanUpdater::getEuropeanaAggregationFromFullBean, FullBeanUpdater::createRootAbout,
        new EuropeanaAggregationUpdater());
    propertyUpdater.updateReferencedEntities("proxies", FullBeanImpl::getProxies, fullBean -> null,
        new ProxyUpdater());
    propertyUpdater.updateReferencedEntities("services", FullBeanImpl::getServices,
        fullBean -> null, new ServiceUpdater());
    propertyUpdater.updateReferencedEntities("licenses", FullBeanImpl::getLicenses,
        fullBean -> null, new LicenseUpdater());

    propertyUpdater.updateObjectList("qualityAnnotations", FullBeanImpl::getQualityAnnotations);
  }

  private static EuropeanaAggregationImpl getEuropeanaAggregationFromFullBean(
      FullBean fullBean) {
    return (EuropeanaAggregationImpl) fullBean.getEuropeanaAggregation();
  }

  private static RootAboutWrapper createRootAbout(FullBean fullBean) {
    return new RootAboutWrapper(fullBean.getAbout());
  }
}
