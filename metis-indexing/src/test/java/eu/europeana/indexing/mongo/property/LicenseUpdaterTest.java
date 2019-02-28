package eu.europeana.indexing.mongo.property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import eu.europeana.corelib.solr.entity.LicenseImpl;
import java.util.Date;
import org.junit.jupiter.api.Test;

class LicenseUpdaterTest extends MongoEntityUpdaterTest<LicenseImpl> {

  @Override
  LicenseImpl createEmptyMongoEntity() {
    return new LicenseImpl();
  }

  @Test
  void testGetObjectClass() {
    assertEquals(LicenseImpl.class, new LicenseUpdater().getObjectClass());
  }

  @Test
  void testUpdate() {

    // Craete objects for execution
    final LicenseUpdater updater = new LicenseUpdater();
    @SuppressWarnings("unchecked") final MongoPropertyUpdater<LicenseImpl> propertyUpdater = mock(
        MongoPropertyUpdater.class);

    // Make the call
    updater.update(propertyUpdater);

    // Test all the values
    testObjectPropertyUpdate(propertyUpdater, "odrlInheritFrom", LicenseImpl::setOdrlInheritFrom,
        "test");
    testObjectPropertyUpdate(propertyUpdater, "ccDeprecatedOn", LicenseImpl::setCcDeprecatedOn,
        new Date(), LicenseUpdater.DATE_PREPROCESSING);

    // And that should be it.
    verifyNoMoreInteractions(propertyUpdater);
  }

  @Test
  void testDatePreprocessing() {
    final long timestamp = 1234567890L;
    final java.sql.Date input = new java.sql.Date(timestamp);
    final Date output = LicenseUpdater.DATE_PREPROCESSING.apply(input);
    assertEquals(timestamp, output.getTime());
    assertEquals(Date.class, output.getClass());
  }
}
