package eu.europeana.indexing.mongo.property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import eu.europeana.corelib.solr.entity.PlaceImpl;
import java.util.function.BiConsumer;
import org.junit.jupiter.api.Test;

class PlaceUpdaterTest extends MongoEntityUpdaterTest<PlaceImpl> {

  @Override
  PlaceImpl createEmptyMongoEntity() {
    return new PlaceImpl();
  }

  @Test
  void testGetObjectClass() {
    assertEquals(PlaceImpl.class, new PlaceUpdater().getObjectClass());
  }

  @Test
  void testUpdate() {

    // Craete objects for execution
    final PlaceUpdater updater = new PlaceUpdater();
    @SuppressWarnings("unchecked") final MongoPropertyUpdater<PlaceImpl> propertyUpdater = mock(
        MongoPropertyUpdater.class);

    // Make the call
    updater.update(propertyUpdater);

    // Test all the non-spatial values
    testMapPropertyUpdate(propertyUpdater, "note", PlaceImpl::setNote);
    testMapPropertyUpdate(propertyUpdater, "altLabel", PlaceImpl::setAltLabel);
    testMapPropertyUpdate(propertyUpdater, "prefLabel", PlaceImpl::setPrefLabel);
    testMapPropertyUpdate(propertyUpdater, "isPartOf", PlaceImpl::setIsPartOf);
    testMapPropertyUpdate(propertyUpdater, "dcTermsHasPart", PlaceImpl::setDcTermsHasPart);
    testArrayPropertyUpdate(propertyUpdater, "owlSameAs", PlaceImpl::setOwlSameAs);

    // Test lat, lon and alt: all three need to be set for any one to be returned non-null.
    final BiConsumer<PlaceImpl, Float> spacialSetter = (place, value) -> {
      place.setLatitude(value);
      place.setLongitude(value);
      place.setAltitude(value);
    };
    testObjectPropertyUpdate(propertyUpdater, "latitude", spacialSetter, 1.0F);
    testObjectPropertyUpdate(propertyUpdater, "longitude", spacialSetter, -1.0F);
    testObjectPropertyUpdate(propertyUpdater, "altitude", spacialSetter, 100.0F);

    // And that should be it.
    verifyNoMoreInteractions(propertyUpdater);
  }
}
