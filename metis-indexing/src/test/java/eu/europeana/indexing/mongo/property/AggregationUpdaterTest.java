package eu.europeana.indexing.mongo.property;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import eu.europeana.corelib.solr.entity.AggregationImpl;
import org.junit.jupiter.api.Test;

class AggregationUpdaterTest extends MongoEntityUpdaterTest<AggregationImpl> {

  @Override
  AggregationImpl createEmptyMongoEntity() {
    return new AggregationImpl();
  }

  @Test
  void testUpdate() {

    // Craete objects for execution
    final AggregationUpdater updater = new AggregationUpdater();
    @SuppressWarnings("unchecked") final MongoPropertyUpdater<AggregationImpl> propertyUpdater = mock(
        MongoPropertyUpdater.class);

    // Make the call
    final RootAboutWrapper rootAbout = new RootAboutWrapper("root about");
    updater.update(propertyUpdater, rootAbout);

    // Test all the values
    testStringPropertyUpdate(propertyUpdater, "aggregatedCHO", AggregationImpl::setAggregatedCHO);
    testStringPropertyUpdate(propertyUpdater, "edmIsShownAt", AggregationImpl::setEdmIsShownAt);
    testStringPropertyUpdate(propertyUpdater, "edmIsShownBy", AggregationImpl::setEdmIsShownBy);
    testStringPropertyUpdate(propertyUpdater, "edmObject", AggregationImpl::setEdmObject);
    testStringPropertyUpdate(propertyUpdater, "edmUgc", AggregationImpl::setEdmUgc);
    testMapPropertyUpdate(propertyUpdater, "edmDataProvider", AggregationImpl::setEdmDataProvider);
    testMapPropertyUpdate(propertyUpdater, "edmProvider", AggregationImpl::setEdmProvider);
    testMapPropertyUpdate(propertyUpdater, "edmIntermediateProvider",
        AggregationImpl::setEdmIntermediateProvider);
    testMapPropertyUpdate(propertyUpdater, "dcRights", AggregationImpl::setDcRights);
    testMapPropertyUpdate(propertyUpdater, "edmRights", AggregationImpl::setEdmRights);
    testArrayPropertyUpdate(propertyUpdater, "hasView", AggregationImpl::setHasView);
    testArrayPropertyUpdate(propertyUpdater, "aggregates", AggregationImpl::setAggregates);
    testObjectPropertyUpdate(propertyUpdater, "edmPreviewNoDistribute",
        AggregationImpl::setEdmPreviewNoDistribute, Boolean.TRUE);
    testWebResourcesPropertyUpdate(propertyUpdater, "webResources",
        AggregationImpl::setWebResources, rootAbout);

    // And that should be it.
    verifyNoMoreInteractions(propertyUpdater);
  }
}
