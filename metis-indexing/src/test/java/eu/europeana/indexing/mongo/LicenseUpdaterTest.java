package eu.europeana.indexing.mongo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import eu.europeana.corelib.solr.entity.LicenseImpl;
import eu.europeana.indexing.mongo.property.MongoPropertyUpdater;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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
        new Date(), LicenseUpdater.DEPRECATED_ON_PREPROCESSING);

    // And that should be it.
    verifyNoMoreInteractions(propertyUpdater);
  }

  @Test
  void testDatePreprocessing() {

    // Convert from local time to UTC time: this is what we expect to come out.
    final long utcTimestamp = 1234567890L;
    final ZonedDateTime utcTime = Instant.ofEpochMilli(utcTimestamp).atZone(ZoneId.systemDefault());
    final ZonedDateTime timeHere = utcTime.withZoneSameLocal(ZoneOffset.UTC);
    final long timestampHere = timeHere.toInstant().toEpochMilli();

    // Perform the call.
    final java.sql.Date input = new java.sql.Date(utcTimestamp);
    final Date output = LicenseUpdater.DEPRECATED_ON_PREPROCESSING.apply(input);
    assertEquals(timestampHere, output.getTime());
    assertEquals(Date.class, output.getClass());
  }
}
