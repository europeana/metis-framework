package eu.europeana.indexing.record.v2.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import eu.europeana.corelib.solr.entity.ConceptImpl;
import eu.europeana.indexing.record.v2.property.MongoPropertyUpdater;
import org.junit.jupiter.api.Test;

class ConceptUpdaterTest extends MongoEntityUpdaterTest<ConceptImpl> {

  @Override
  ConceptImpl createEmptyMongoEntity() {
    return new ConceptImpl();
  }

  @Test
  void testGetObjectClass() {
    assertEquals(ConceptImpl.class, new ConceptUpdater().getObjectClass());
  }

  @Test
  void testUpdate() {

    // Craete objects for execution
    final ConceptUpdater updater = new ConceptUpdater();
    @SuppressWarnings("unchecked") final MongoPropertyUpdater<ConceptImpl> propertyUpdater = mock(
        MongoPropertyUpdater.class);

    // Make the call
    updater.update(propertyUpdater);

    // Test all the values
    testMapPropertyUpdate(propertyUpdater, "altLabel", ConceptImpl::setAltLabel);
    testMapPropertyUpdate(propertyUpdater, "prefLabel", ConceptImpl::setPrefLabel);
    testMapPropertyUpdate(propertyUpdater, "hiddenLabel", ConceptImpl::setHiddenLabel);
    testMapPropertyUpdate(propertyUpdater, "notation", ConceptImpl::setNotation);
    testMapPropertyUpdate(propertyUpdater, "note", ConceptImpl::setNote);
    testArrayPropertyUpdate(propertyUpdater, "broader", ConceptImpl::setBroader);
    testArrayPropertyUpdate(propertyUpdater, "broadMatch", ConceptImpl::setBroadMatch);
    testArrayPropertyUpdate(propertyUpdater, "closeMatch", ConceptImpl::setCloseMatch);
    testArrayPropertyUpdate(propertyUpdater, "exactMatch", ConceptImpl::setExactMatch);
    testArrayPropertyUpdate(propertyUpdater, "inScheme", ConceptImpl::setInScheme);
    testArrayPropertyUpdate(propertyUpdater, "narrower", ConceptImpl::setNarrower);
    testArrayPropertyUpdate(propertyUpdater, "narrowMatch", ConceptImpl::setNarrowMatch);
    testArrayPropertyUpdate(propertyUpdater, "relatedMatch", ConceptImpl::setRelatedMatch);
    testArrayPropertyUpdate(propertyUpdater, "related", ConceptImpl::setRelated);

    // And that should be it.
    verifyNoMoreInteractions(propertyUpdater);
  }

}
