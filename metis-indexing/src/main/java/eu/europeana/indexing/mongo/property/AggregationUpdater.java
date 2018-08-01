package eu.europeana.indexing.mongo.property;

import eu.europeana.corelib.solr.entity.AggregationImpl;

/**
 * Field updater for instances of {@link AggregationImpl}.
 */
public class AggregationUpdater extends AbstractEdmEntityUpdater<AggregationImpl, RootAbout> {

  @Override
  protected Class<AggregationImpl> getObjectClass() {
    return AggregationImpl.class;
  }

  @Override
  protected void update(MongoPropertyUpdater<AggregationImpl> propertyUpdater,
      RootAbout ancestorInformation) {
    propertyUpdater.updateString("aggregatedCHO", AggregationImpl::getAggregatedCHO);
    propertyUpdater.updateString("edmIsShownAt", AggregationImpl::getEdmIsShownAt);
    propertyUpdater.updateString("edmIsShownBy", AggregationImpl::getEdmIsShownBy);
    propertyUpdater.updateString("edmObject", AggregationImpl::getEdmObject);
    propertyUpdater.updateString("edmUgc", AggregationImpl::getEdmUgc);
    propertyUpdater.updateMap("edmDataProvider", AggregationImpl::getEdmDataProvider);
    propertyUpdater.updateMap("edmProvider", AggregationImpl::getEdmProvider);
    propertyUpdater.updateMap("edmIntermediateProvider",
        AggregationImpl::getEdmIntermediateProvider);
    propertyUpdater.updateMap("dcRights", AggregationImpl::getDcRights);
    propertyUpdater.updateMap("edmRights", AggregationImpl::getEdmRights);
    propertyUpdater.updateArray("hasView", AggregationImpl::getHasView);
    propertyUpdater.updateArray("aggregates", AggregationImpl::getAggregates);
    propertyUpdater.updateObject("edmPreviewNoDistribute",
        AggregationImpl::getEdmPreviewNoDistribute);
    propertyUpdater.updateWebResources("webResources", AggregationImpl::getWebResources,
        ancestorInformation);
  }
}
