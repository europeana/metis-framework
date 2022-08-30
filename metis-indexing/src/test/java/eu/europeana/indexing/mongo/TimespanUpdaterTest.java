package eu.europeana.indexing.mongo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import eu.europeana.corelib.solr.entity.TimespanImpl;
import eu.europeana.indexing.mongo.property.MongoPropertyUpdater;
import org.junit.jupiter.api.Test;

class TimespanUpdaterTest extends MongoEntityUpdaterTest<TimespanImpl> {

  @Override
  TimespanImpl createEmptyMongoEntity() {
    return new TimespanImpl();
  }

  @Test
  void testGetObjectClass() {
    assertEquals(TimespanImpl.class, new TimespanUpdater().getObjectClass());
  }

  @Test
  void testUpdate() {

    // Craete objects for execution
    final TimespanUpdater updater = new TimespanUpdater();
    @SuppressWarnings("unchecked") final MongoPropertyUpdater<TimespanImpl> propertyUpdater = mock(
        MongoPropertyUpdater.class);

    // Make the call
    updater.update(propertyUpdater);

    // Test all the values
    testMapPropertyUpdate(propertyUpdater, "begin", TimespanImpl::setBegin);
    testMapPropertyUpdate(propertyUpdater, "end", TimespanImpl::setEnd);
    testMapPropertyUpdate(propertyUpdater, "note", TimespanImpl::setNote);
    testMapPropertyUpdate(propertyUpdater, "altLabel", TimespanImpl::setAltLabel);
    testMapPropertyUpdate(propertyUpdater, "prefLabel", TimespanImpl::setPrefLabel);
    testMapPropertyUpdate(propertyUpdater, "isPartOf", TimespanImpl::setIsPartOf);
    testMapPropertyUpdate(propertyUpdater, "dctermsHasPart", TimespanImpl::setDctermsHasPart);
    testArrayPropertyUpdate(propertyUpdater, "owlSameAs", TimespanImpl::setOwlSameAs);
    testMapPropertyUpdate(propertyUpdater, "notation", TimespanImpl::setSkosNotation);
    // And that should be it.
    verifyNoMoreInteractions(propertyUpdater);
  }
}
