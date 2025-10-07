package eu.europeana.indexing.record.v2.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import eu.europeana.corelib.solr.entity.ProvidedCHOImpl;
import eu.europeana.indexing.record.v2.property.MongoPropertyUpdater;
import org.junit.jupiter.api.Test;

class ProvidedChoUpdaterTest extends MongoEntityUpdaterTest<ProvidedCHOImpl> {

  @Override
  ProvidedCHOImpl createEmptyMongoEntity() {
    return new ProvidedCHOImpl();
  }

  @Test
  void testGetObjectClass() {
    assertEquals(ProvidedCHOImpl.class, new ProvidedChoUpdater().getObjectClass());
  }

  @Test
  void testUpdate() {

    // Craete objects for execution
    final ProvidedChoUpdater updater = new ProvidedChoUpdater();
    @SuppressWarnings("unchecked") final MongoPropertyUpdater<ProvidedCHOImpl> propertyUpdater = mock(
        MongoPropertyUpdater.class);

    // Make the call
    updater.update(propertyUpdater);

    // Test all the values
    testArrayPropertyUpdate(propertyUpdater, "owlSameAs", ProvidedCHOImpl::setOwlSameAs);

    // And that should be it.
    verifyNoMoreInteractions(propertyUpdater);
  }
}
