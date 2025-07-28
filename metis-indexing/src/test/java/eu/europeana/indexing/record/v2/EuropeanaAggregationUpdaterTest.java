package eu.europeana.indexing.record.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import eu.europeana.corelib.definitions.edm.entity.EuropeanaAggregation;
import eu.europeana.corelib.solr.entity.EuropeanaAggregationImpl;
import eu.europeana.indexing.record.v2.property.MongoPropertyUpdater;
import eu.europeana.indexing.record.v2.property.RootAboutWrapper;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

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

    final EuropeanaAggregationImpl europeanaAggregation = new EuropeanaAggregationImpl();
    europeanaAggregation.setChangeLog(new ArrayList<>());
    @SuppressWarnings("unchecked")
    final ArgumentCaptor<Function<EuropeanaAggregationImpl, List<Object>>> getterCaptor = ArgumentCaptor
        .forClass(Function.class);
    verify(propertyUpdater, times(1))
        .updateObjectList(eq("changeLog"), getterCaptor.capture());
    assertSame(europeanaAggregation.getChangeLog(), getterCaptor.getValue().apply(europeanaAggregation));

    // And that should be it.
    verifyNoMoreInteractions(propertyUpdater);
  }

}
