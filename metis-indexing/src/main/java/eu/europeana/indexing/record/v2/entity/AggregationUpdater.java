package eu.europeana.indexing.record.v2.entity;

import eu.europeana.corelib.solr.entity.AggregationImpl;
import eu.europeana.indexing.record.v2.property.MongoPropertyUpdater;
import eu.europeana.indexing.record.v2.property.RootAboutWrapper;

/**
 * Field updater for instances of {@link AggregationImpl}.
 */
public class AggregationUpdater extends AbstractEdmEntityUpdater<AggregationImpl, RootAboutWrapper> {

  @Override
  protected Class<AggregationImpl> getObjectClass() {
    return AggregationImpl.class;
  }

  @Override
  protected void update(MongoPropertyUpdater<AggregationImpl> propertyUpdater,
      RootAboutWrapper ancestorInformation) {
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
        ancestorInformation, new WebResourceUpdater());
  }
}
