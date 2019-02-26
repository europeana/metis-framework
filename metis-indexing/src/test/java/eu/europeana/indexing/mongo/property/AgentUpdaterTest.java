package eu.europeana.indexing.mongo.property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import eu.europeana.corelib.solr.entity.AgentImpl;

class AgentUpdaterTest {

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

    // Create objects for verification  
    final AgentImpl testAgent = new AgentImpl();
    @SuppressWarnings("unchecked")
    final ArgumentCaptor<Function<AgentImpl, Map<String, List<String>>>> mapCaptor =
        ArgumentCaptor.forClass(Function.class);
    @SuppressWarnings("unchecked")
    final ArgumentCaptor<Function<AgentImpl, String[]>> arrayCaptor =
        ArgumentCaptor.forClass(Function.class);
    
    // Make the call
    updater.update(propertyUpdater);

    // Test begin
    testAgent.setBegin(new HashMap<>());
    verify(propertyUpdater, times(1)).updateMap(eq("begin"), mapCaptor.capture());
    assertSame(testAgent.getBegin(), mapCaptor.getValue().apply(testAgent));

    // Test dcDate
    testAgent.setDcDate(new HashMap<>());
    verify(propertyUpdater, times(1)).updateMap(eq("dcDate"), mapCaptor.capture());
    assertSame(testAgent.getDcDate(), mapCaptor.getValue().apply(testAgent));

    // Test dcIdentifier
    testAgent.setDcIdentifier(new HashMap<>());
    verify(propertyUpdater, times(1)).updateMap(eq("dcIdentifier"), mapCaptor.capture());
    assertSame(testAgent.getDcIdentifier(), mapCaptor.getValue().apply(testAgent));

    // Test rdaGr2BiographicalInformation
    testAgent.setRdaGr2BiographicalInformation(new HashMap<>());
    verify(propertyUpdater, times(1)).updateMap(eq("rdaGr2BiographicalInformation"), mapCaptor.capture());
    assertSame(testAgent.getRdaGr2BiographicalInformation(), mapCaptor.getValue().apply(testAgent));

    // Test rdaGr2DateOfBirth
    testAgent.setRdaGr2DateOfBirth(new HashMap<>());
    verify(propertyUpdater, times(1)).updateMap(eq("rdaGr2DateOfBirth"), mapCaptor.capture());
    assertSame(testAgent.getRdaGr2DateOfBirth(), mapCaptor.getValue().apply(testAgent));

    // Test rdaGr2DateOfDeath
    testAgent.setRdaGr2DateOfDeath(new HashMap<>());
    verify(propertyUpdater, times(1)).updateMap(eq("rdaGr2DateOfDeath"), mapCaptor.capture());
    assertSame(testAgent.getRdaGr2DateOfDeath(), mapCaptor.getValue().apply(testAgent));

    // Test rdaGr2PlaceOfBirth
    testAgent.setRdaGr2PlaceOfBirth(new HashMap<>());
    verify(propertyUpdater, times(1)).updateMap(eq("rdaGr2PlaceOfBirth"), mapCaptor.capture());
    assertSame(testAgent.getRdaGr2PlaceOfBirth(), mapCaptor.getValue().apply(testAgent));

    // Test rdaGr2PlaceOfDeath
    testAgent.setRdaGr2PlaceOfDeath(new HashMap<>());
    verify(propertyUpdater, times(1)).updateMap(eq("rdaGr2PlaceOfDeath"), mapCaptor.capture());
    assertSame(testAgent.getRdaGr2PlaceOfDeath(), mapCaptor.getValue().apply(testAgent));

    // Test rdaGr2DateOfEstablishment
    testAgent.setRdaGr2DateOfEstablishment(new HashMap<>());
    verify(propertyUpdater, times(1)).updateMap(eq("rdaGr2DateOfEstablishment"), mapCaptor.capture());
    assertSame(testAgent.getRdaGr2DateOfEstablishment(), mapCaptor.getValue().apply(testAgent));

    // Test rdaGr2DateOfTermination
    testAgent.setRdaGr2DateOfTermination(new HashMap<>());
    verify(propertyUpdater, times(1)).updateMap(eq("rdaGr2DateOfTermination"), mapCaptor.capture());
    assertSame(testAgent.getRdaGr2DateOfTermination(), mapCaptor.getValue().apply(testAgent));

    // Test rdaGr2Gender
    testAgent.setRdaGr2Gender(new HashMap<>());
    verify(propertyUpdater, times(1)).updateMap(eq("rdaGr2Gender"), mapCaptor.capture());
    assertSame(testAgent.getRdaGr2Gender(), mapCaptor.getValue().apply(testAgent));

    // Test rdaGr2ProfessionOrOccupation
    testAgent.setRdaGr2ProfessionOrOccupation(new HashMap<>());
    verify(propertyUpdater, times(1)).updateMap(eq("rdaGr2ProfessionOrOccupation"), mapCaptor.capture());
    assertSame(testAgent.getRdaGr2ProfessionOrOccupation(), mapCaptor.getValue().apply(testAgent));

    // Test edmHasMet
    testAgent.setEdmHasMet(new HashMap<>());
    verify(propertyUpdater, times(1)).updateMap(eq("edmHasMet"), mapCaptor.capture());
    assertSame(testAgent.getEdmHasMet(), mapCaptor.getValue().apply(testAgent));

    // Test edmIsRelatedTo
    testAgent.setEdmIsRelatedTo(new HashMap<>());
    verify(propertyUpdater, times(1)).updateMap(eq("edmIsRelatedTo"), mapCaptor.capture());
    assertSame(testAgent.getEdmIsRelatedTo(), mapCaptor.getValue().apply(testAgent));

    // Test foafName
    testAgent.setFoafName(new HashMap<>());
    verify(propertyUpdater, times(1)).updateMap(eq("foafName"), mapCaptor.capture());
    assertSame(testAgent.getFoafName(), mapCaptor.getValue().apply(testAgent));

    // Test owlSameAs
    testAgent.setOwlSameAs(new String[0]);
    verify(propertyUpdater, times(1)).updateArray(eq("owlSameAs"), arrayCaptor.capture());
    assertSame(testAgent.getOwlSameAs(), arrayCaptor.getValue().apply(testAgent));

    // Test end
    testAgent.setEnd(new HashMap<>());
    verify(propertyUpdater, times(1)).updateMap(eq("end"), mapCaptor.capture());
    assertSame(testAgent.getEnd(), mapCaptor.getValue().apply(testAgent));

    // Test note
    testAgent.setNote(new HashMap<>());
    verify(propertyUpdater, times(1)).updateMap(eq("note"), mapCaptor.capture());
    assertSame(testAgent.getNote(), mapCaptor.getValue().apply(testAgent));

    // Test altLabel
    testAgent.setAltLabel(new HashMap<>());
    verify(propertyUpdater, times(1)).updateMap(eq("altLabel"), mapCaptor.capture());
    assertSame(testAgent.getAltLabel(), mapCaptor.getValue().apply(testAgent));

    // Test prefLabel
    testAgent.setPrefLabel(new HashMap<>());
    verify(propertyUpdater, times(1)).updateMap(eq("prefLabel"), mapCaptor.capture());
    assertSame(testAgent.getPrefLabel(), mapCaptor.getValue().apply(testAgent));

    // And that should be it.
    verifyNoMoreInteractions(propertyUpdater);
  }
}
