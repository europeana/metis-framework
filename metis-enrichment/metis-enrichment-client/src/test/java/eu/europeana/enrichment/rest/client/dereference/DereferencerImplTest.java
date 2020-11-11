package eu.europeana.enrichment.rest.client.dereference;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.*;

import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.enrichment.api.external.model.Agent;
import eu.europeana.enrichment.api.external.model.EnrichmentBaseWrapper;
import eu.europeana.enrichment.api.external.model.EnrichmentResultBaseWrapper;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.enrichment.api.external.model.Place;
import eu.europeana.enrichment.api.external.model.Timespan;
import eu.europeana.enrichment.rest.client.enrichment.EnrichmentClient;
import eu.europeana.enrichment.rest.client.exceptions.DereferenceException;
import eu.europeana.enrichment.utils.EntityMergeEngine;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;


public class DereferencerImplTest {

  private ArgumentCaptor<List<EnrichmentResultBaseWrapper>> enrichmentResultCaptor = ArgumentCaptor
      .forClass(List.class);

  private static final String[] DEREFERENCE_EXTRACT_RESULT =
      {"enrich1", "enrich3", "enrich4"};

  private static final List<EnrichmentResultList> DEREFERENCE_RESULT;
  private static final EnrichmentResultList ENRICHMENT_RESULT;

  static {
    final Agent agent1 = new Agent();
    agent1.setAbout("agent1");
    final Agent agent2 = new Agent();
    agent2.setAbout("agent2");
    final Place place1 = new Place();
    place1.setAbout("place1");
    final Place place2 = new Place();
    place2.setAbout("place2");
    final Timespan timeSpan1 = new Timespan();
    timeSpan1.setAbout("timespan1");
    final Timespan timeSpan2 = new Timespan();
    timeSpan2.setAbout("timespan2");
    final List<EnrichmentBaseWrapper> enrichmentBaseWrapperList1 = EnrichmentBaseWrapper
        .createNullOriginalFieldEnrichmentBaseWrapperList(Arrays.asList(agent1, null, agent2));
    final EnrichmentResultList dereferenceResult1 =
        new EnrichmentResultList(enrichmentBaseWrapperList1);
    final List<EnrichmentBaseWrapper> enrichmentBaseWrapperList2 = EnrichmentBaseWrapper
        .createNullOriginalFieldEnrichmentBaseWrapperList(
            Arrays.asList(timeSpan1, timeSpan2, null));
    final EnrichmentResultList dereferenceResult2 =
        new EnrichmentResultList(enrichmentBaseWrapperList2);
    DEREFERENCE_RESULT = Arrays.asList(dereferenceResult1, null, dereferenceResult2);
    final List<EnrichmentBaseWrapper> enrichmentBaseWrapperList3 = EnrichmentBaseWrapper
        .createNullOriginalFieldEnrichmentBaseWrapperList(Arrays.asList(place1, null, place2));
    ENRICHMENT_RESULT = new EnrichmentResultList(enrichmentBaseWrapperList3);
  }


  @Test
  void testDereferencerHappyFlow() throws DereferenceException {

    // Create mocks of the dependencies
    final EnrichmentClient enrichmentClient = mock(EnrichmentClient.class);
    doReturn(ENRICHMENT_RESULT).when(enrichmentClient).enrich(any());
    final DereferenceClient dereferenceClient = mock(DereferenceClient.class);
    doReturn(DEREFERENCE_RESULT.get(0),
        DEREFERENCE_RESULT.subList(1, DEREFERENCE_RESULT.size()).toArray()).when(dereferenceClient)
        .dereference(any());
    final EntityMergeEngine entityMergeEngine = mock(EntityMergeEngine.class);

    final Dereferencer dereferencer = spy(
        new DereferencerImpl(entityMergeEngine, enrichmentClient, dereferenceClient));
    doReturn(Arrays.stream(DEREFERENCE_EXTRACT_RESULT).collect(Collectors.toSet()))
        .when(dereferencer)
        .extractReferencesForDereferencing(any());

    final RDF inputRdf = new RDF();
    dereferencer.dereference(inputRdf);

    verifyDereferenceHappyFlow(dereferenceClient, dereferencer, inputRdf);
    verifyMergeHappyFlow(entityMergeEngine);

  }

  @Test
  void testDereferencerNullFlow() throws DereferenceException {

    // Create mocks of the dependencies
    final EnrichmentClient enrichmentClient = mock(EnrichmentClient.class);
    final DereferenceClient dereferenceClient = mock(DereferenceClient.class);

    final EntityMergeEngine entityMergeEngine = mock(EntityMergeEngine.class);

    // Create dereferencer.
    final Dereferencer dereferencer = spy(new DereferencerImpl(entityMergeEngine, enrichmentClient, dereferenceClient));
    doReturn(Arrays.stream(new String[0]).collect(Collectors.toSet())).when(dereferencer)
        .extractReferencesForDereferencing(any());

    final RDF inputRdf = new RDF();
    dereferencer.dereference(inputRdf);

    verifyDereferenceNullFlow(dereferenceClient, dereferencer, inputRdf);
    verifyMergeNullFlow(entityMergeEngine);

  }


  private void verifyDereferenceHappyFlow(DereferenceClient dereferenceClient,
      Dereferencer dereferencer,
      RDF inputRdf) {

    // Extracting values for dereferencing
    verify(dereferencer, times(1)).extractReferencesForDereferencing(any());
    verify(dereferencer, times(1)).extractReferencesForDereferencing(inputRdf);

    // Actually dereferencing.
    verify(dereferenceClient, times(DEREFERENCE_EXTRACT_RESULT.length)).dereference(anyString());
    for (String dereferenceUrl : DEREFERENCE_EXTRACT_RESULT) {
      verify(dereferenceClient, times(1)).dereference(dereferenceUrl);
    }

  }

  // Verify merge calls
  private void verifyMergeHappyFlow(EntityMergeEngine entityMergeEngine) {
    final List<List<EnrichmentResultBaseWrapper>> expectedMerges = new ArrayList<>();

    expectedMerges.add(DEREFERENCE_RESULT.stream().filter(Objects::nonNull)
        .map(EnrichmentResultList::getEnrichmentBaseResultWrapperList).flatMap(List::stream)
        .collect(Collectors.toList()));

    verify(entityMergeEngine, times(expectedMerges.size())).mergeEntities(any(),
        enrichmentResultCaptor.capture(), any());
    // Note that the captor returns a linked list, so we don't want to use indices.
    // But the interface gives a generic type List, so we don't want to depend on the
    // linked list functionality either.
    int currentPointer = 0;
    final List<List<EnrichmentResultBaseWrapper>> foundValues = enrichmentResultCaptor.getAllValues()
        .subList(
            enrichmentResultCaptor.getAllValues().size() - expectedMerges.size(),
            enrichmentResultCaptor.getAllValues().size());
    for (List<EnrichmentResultBaseWrapper> capturedMerge : foundValues) {
      assertArrayEquals(expectedMerges.get(currentPointer).toArray(), capturedMerge.toArray());
      currentPointer++;
    }
  }

  private void verifyDereferenceNullFlow(DereferenceClient dereferenceClient,
      Dereferencer dereferencer, RDF inputRdf) {

    // Extracting values for dereferencing
    verify(dereferencer, times(1)).extractReferencesForDereferencing(any());
    verify(dereferencer, times(1)).extractReferencesForDereferencing(inputRdf);

    // Actually dereferencing: don't use the null values.
    final Set<String> dereferenceUrls = Arrays.stream(new String[0])
        .filter(Objects::nonNull).collect(Collectors.toSet());
    verify(dereferenceClient, times(dereferenceUrls.size())).dereference(anyString());
    for (String dereferenceUrl : dereferenceUrls) {
      verify(dereferenceClient, times(1)).dereference(dereferenceUrl);
    }

  }

  private void verifyMergeNullFlow(EntityMergeEngine entityMergeEngine) {
    verify(entityMergeEngine, times(1)).mergeEntities(any(), eq(Collections.emptyList()), anyMap());
    verify(entityMergeEngine, times(1)).mergeEntities(any(), any(), anyMap());
  }

}
