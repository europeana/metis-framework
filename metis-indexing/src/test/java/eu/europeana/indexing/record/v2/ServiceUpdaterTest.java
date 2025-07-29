package eu.europeana.indexing.record.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import eu.europeana.corelib.solr.entity.ServiceImpl;
import eu.europeana.indexing.record.v2.property.MongoPropertyUpdater;
import org.junit.jupiter.api.Test;

class ServiceUpdaterTest extends MongoEntityUpdaterTest<ServiceImpl> {

  @Override
  ServiceImpl createEmptyMongoEntity() {
    return new ServiceImpl();
  }

  @Test
  void testGetObjectClass() {
    assertEquals(ServiceImpl.class, new ServiceUpdater().getObjectClass());
  }

  @Test
  void testUpdate() {

    // Craete objects for execution
    final ServiceUpdater updater = new ServiceUpdater();
    @SuppressWarnings("unchecked") final MongoPropertyUpdater<ServiceImpl> propertyUpdater = mock(
        MongoPropertyUpdater.class);

    // Make the call
    updater.update(propertyUpdater);

    // Test all the values
    testArrayPropertyUpdate(propertyUpdater, "dctermsConformsTo",
        ServiceImpl::setDcTermsConformsTo);
    testArrayPropertyUpdate(propertyUpdater, "doapImplements", ServiceImpl::setDoapImplements);

    // And that should be it.
    verifyNoMoreInteractions(propertyUpdater);
  }
}
