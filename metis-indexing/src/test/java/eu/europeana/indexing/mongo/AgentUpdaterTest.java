package eu.europeana.indexing.mongo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.indexing.mongo.property.MongoPropertyUpdater;
import org.junit.jupiter.api.Test;

class AgentUpdaterTest extends MongoEntityUpdaterTest<AgentImpl> {

  @Override
  AgentImpl createEmptyMongoEntity() {
    return new AgentImpl();
  }

  @Test
  void testGetObjectClass() {
    assertEquals(AgentImpl.class, new AgentUpdater().getObjectClass());
  }

  @Test
  void testUpdate() {

    // Craete objects for execution
    final AgentUpdater updater = new AgentUpdater();
    @SuppressWarnings("unchecked")
    final MongoPropertyUpdater<AgentImpl> propertyUpdater = mock(MongoPropertyUpdater.class);

    // Make the call
    updater.update(propertyUpdater);

    // Test all the values
    testMapPropertyUpdate(propertyUpdater, "begin", AgentImpl::setBegin);
    testMapPropertyUpdate(propertyUpdater, "dcDate", AgentImpl::setDcDate);
    testMapPropertyUpdate(propertyUpdater, "dcIdentifier", AgentImpl::setDcIdentifier);
    testMapPropertyUpdate(propertyUpdater, "rdaGr2BiographicalInformation", AgentImpl::setRdaGr2BiographicalInformation);
    testMapPropertyUpdate(propertyUpdater, "rdaGr2DateOfBirth", AgentImpl::setRdaGr2DateOfBirth);
    testMapPropertyUpdate(propertyUpdater, "rdaGr2DateOfDeath", AgentImpl::setRdaGr2DateOfDeath);
    testMapPropertyUpdate(propertyUpdater, "rdaGr2PlaceOfBirth", AgentImpl::setRdaGr2PlaceOfBirth);
    testMapPropertyUpdate(propertyUpdater, "rdaGr2PlaceOfDeath", AgentImpl::setRdaGr2PlaceOfDeath);
    testMapPropertyUpdate(propertyUpdater, "rdaGr2DateOfEstablishment", AgentImpl::setRdaGr2DateOfEstablishment);
    testMapPropertyUpdate(propertyUpdater, "rdaGr2DateOfTermination", AgentImpl::setRdaGr2DateOfTermination);
    testMapPropertyUpdate(propertyUpdater, "rdaGr2Gender", AgentImpl::setRdaGr2Gender);
    testMapPropertyUpdate(propertyUpdater, "rdaGr2ProfessionOrOccupation", AgentImpl::setRdaGr2ProfessionOrOccupation);
    testMapPropertyUpdate(propertyUpdater, "edmHasMet", AgentImpl::setEdmHasMet);
    testMapPropertyUpdate(propertyUpdater, "edmIsRelatedTo", AgentImpl::setEdmIsRelatedTo);
    testMapPropertyUpdate(propertyUpdater, "foafName", AgentImpl::setFoafName);
    testArrayPropertyUpdate(propertyUpdater, "owlSameAs", AgentImpl::setOwlSameAs);
    testMapPropertyUpdate(propertyUpdater, "end", AgentImpl::setEnd);
    testMapPropertyUpdate(propertyUpdater, "note", AgentImpl::setNote);
    testMapPropertyUpdate(propertyUpdater, "altLabel", AgentImpl::setAltLabel);
    testMapPropertyUpdate(propertyUpdater, "prefLabel", AgentImpl::setPrefLabel);

    // And that should be it.
    verifyNoMoreInteractions(propertyUpdater);
  }
}
