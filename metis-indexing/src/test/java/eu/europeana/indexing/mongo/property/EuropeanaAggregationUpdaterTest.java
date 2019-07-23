package eu.europeana.indexing.mongo.property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import eu.europeana.corelib.definitions.edm.entity.EuropeanaAggregation;
import eu.europeana.corelib.solr.entity.EuropeanaAggregationImpl;
import org.junit.jupiter.api.Test;

class EuropeanaAggregationUpdaterTest extends MongoEntityUpdaterTest<EuropeanaAggregationImpl> {

  @Override
  EuropeanaAggregationImpl createEmptyMongoEntity() {
    return new EuropeanaAggregationImpl();
  }

  @Test
  void testGetObjectClass() {
    assertEquals(EuropeanaAggregationImpl.class,
        new EuropeanaAggregationUpdater().getObjectClass());
  }

  @Test
  void testUpdate() {

    // Craete objects for execution
    final EuropeanaAggregationUpdater updater = new EuropeanaAggregationUpdater();
    @SuppressWarnings("unchecked") final MongoPropertyUpdater<EuropeanaAggregationImpl> propertyUpdater = mock(
        MongoPropertyUpdater.class);

    // Make the call
    final RootAboutWrapper rootAbout = new RootAboutWrapper("root about");
    updater.update(propertyUpdater, rootAbout);

    // Test all the values
    testStringPropertyUpdate(propertyUpdater, "aggregatedCHO",
        EuropeanaAggregation::setAggregatedCHO);
    testStringPropertyUpdate(propertyUpdater, "edmIsShownBy",
        EuropeanaAggregation::setEdmIsShownBy);
    testMapPropertyUpdate(propertyUpdater, "edmRights", EuropeanaAggregation::setEdmRights);
    testMapPropertyUpdate(propertyUpdater, "edmCountry", EuropeanaAggregation::setEdmCountry);
    testMapPropertyUpdate(propertyUpdater, "edmLanguage", EuropeanaAggregation::setEdmLanguage);
    testMapPropertyUpdate(propertyUpdater, "dcCreator", EuropeanaAggregation::setDcCreator);
    testStringPropertyUpdate(propertyUpdater, "edmPreview", EuropeanaAggregation::setEdmPreview);
    testArrayPropertyUpdate(propertyUpdater, "aggregates", EuropeanaAggregation::setAggregates);
    testWebResourcesPropertyUpdate(propertyUpdater, "webResources",
        EuropeanaAggregationImpl::setWebResources, rootAbout);
    testArrayPropertyUpdate(propertyUpdater, "dqvHasQualityAnnotation",
        EuropeanaAggregation::setDqvHasQualityAnnotation, true);

    // And that should be it.
    verifyNoMoreInteractions(propertyUpdater);
  }

}
