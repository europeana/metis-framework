package eu.europeana.enrichment.rest.client;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.enrichment.api.external.model.Agent;
import eu.europeana.enrichment.api.external.model.EnrichmentBaseWrapper;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.enrichment.api.external.model.Place;
import eu.europeana.enrichment.api.external.model.Timespan;
import eu.europeana.enrichment.rest.client.EnrichmentWorker.Mode;
import eu.europeana.enrichment.rest.client.dereference.DereferenceClient;
import eu.europeana.enrichment.rest.client.dereference.Dereferencer;
import eu.europeana.enrichment.rest.client.dereference.DereferencerImpl;
import eu.europeana.enrichment.rest.client.enrichment.Enricher;
import eu.europeana.enrichment.rest.client.enrichment.EnricherImpl;
import eu.europeana.enrichment.rest.client.enrichment.EnrichmentClient;
import eu.europeana.enrichment.rest.client.exceptions.DereferenceException;
import eu.europeana.enrichment.rest.client.exceptions.DereferenceOrEnrichException;
import eu.europeana.enrichment.rest.client.exceptions.EnrichmentException;
import eu.europeana.enrichment.rest.client.exceptions.SerializationException;
import eu.europeana.enrichment.utils.EntityType;
import eu.europeana.enrichment.utils.EntityMergeEngine;
import eu.europeana.enrichment.utils.InputValue;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.jibx.runtime.JiBXException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class EnrichmentWorkerImplTest {

  private ArgumentCaptor<List<InputValue>> enrichmentExtractionCaptor = ArgumentCaptor
      .forClass(List.class);

  private ArgumentCaptor<List<EnrichmentBaseWrapper>> enrichmentResultCaptor = ArgumentCaptor
      .forClass(List.class);

  private static final InputValue[] ENRICHMENT_EXTRACT_RESULT =
      {new InputValue("orig1", "value1", "lang1", EntityType.AGENT),
          new InputValue(null, "value2", null, EntityType.AGENT, EntityType.CONCEPT),
          new InputValue("orig3", null, "lang2")};

  private static final String[] DEREFERENCE_EXTRACT_RESULT =
      {"enrich1", "enrich3", "enrich4"};

  private static final EnrichmentResultList ENRICHMENT_RESULT;

  private static final List<EnrichmentResultList> DEREFERENCE_RESULT;

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

  @BeforeEach
  public void setUp(){


  }

  @Test
  void testEnrichmentWorkerHappyFlow()
      throws DereferenceException, EnrichmentException {
    for (Mode mode : Mode.values()) {
      testEnrichmentWorkerHappyFlow(mode);
    }
  }

  @Test
  void testEnrichmentWorkerNullFlow()
      throws DereferenceException, EnrichmentException {
    for (Mode mode : Mode.values()) {
      testEnrichmentWorkerNullFlow(mode);
    }
  }

  private void testEnrichmentWorkerHappyFlow(Mode mode)
      throws DereferenceException, EnrichmentException {
    // Create mocks of the dependencies
    final EnrichmentClient enrichmentClient = Mockito.mock(EnrichmentClient.class);
    doReturn(ENRICHMENT_RESULT).when(enrichmentClient).enrich(any());
    final DereferenceClient dereferenceClient = Mockito.mock(DereferenceClient.class);
    doReturn(DEREFERENCE_RESULT.get(0),
        DEREFERENCE_RESULT.subList(1, DEREFERENCE_RESULT.size()).toArray()).when(dereferenceClient)
        .dereference(any());
    final EntityMergeEngine entityMergeEngine = Mockito.mock(EntityMergeEngine.class);

    // Create enrichment worker and mock the enrichment and dereferencing results.
    final Enricher enricher = spy(new EnricherImpl(entityMergeEngine, enrichmentClient));
    doReturn(Arrays.asList(ENRICHMENT_EXTRACT_RESULT)).when(enricher)
            .extractValuesForEnrichment(any());
    // TODO test the result of this method better
    doReturn(Collections.emptyMap()).when(enricher).extractReferencesForEnrichment(any());

    final Dereferencer dereferencer =
        spy(new DereferencerImpl(entityMergeEngine, enrichmentClient, dereferenceClient));
    doReturn(Arrays.stream(DEREFERENCE_EXTRACT_RESULT).collect(Collectors.toSet())).when(dereferencer)
        .extractReferencesForDereferencing(any());

    // Execute the worker
    final EnrichmentWorkerImpl worker =
        spy(new EnrichmentWorkerImpl(dereferencer, enricher));
    final RDF inputRdf = new RDF();
    worker.process(inputRdf, mode);

    // Counters of method calls depend on the mode
    final boolean doDereferencing =
            mode == Mode.DEREFERENCE_AND_ENRICHMENT || mode == Mode.DEREFERENCE_ONLY;
    final boolean doEnrichment =
            mode == Mode.DEREFERENCE_AND_ENRICHMENT || mode == Mode.ENRICHMENT_ONLY;

    // Check the performed tasks
    verifyDereferencingHappyFlow(doDereferencing, dereferencer, dereferenceClient, inputRdf);
    verifyEnrichmentHappyFlow(doEnrichment, enricher, enrichmentClient, inputRdf);
    verifyMergeHappyFlow(doEnrichment, doDereferencing, entityMergeEngine);
  }

  private void testEnrichmentWorkerNullFlow(Mode mode)
      throws DereferenceException, EnrichmentException {
    // Create mocks of the dependencies
    final EnrichmentClient enrichmentClient = Mockito.mock(EnrichmentClient.class);
    final DereferenceClient dereferenceClient = Mockito.mock(DereferenceClient.class);

    final EntityMergeEngine entityMergeEngine = Mockito.mock(EntityMergeEngine.class);

    // Create enrichment worker and mock the enrichment and dereferencing results.
    final Enricher enricher =
        spy(new EnricherImpl(entityMergeEngine, enrichmentClient));
    doReturn(Collections.emptyList()).when(enricher).extractValuesForEnrichment(any());
    doReturn(Collections.emptyMap()).when(enricher).extractReferencesForEnrichment(any());

    final Dereferencer dereferencer =
        spy(new DereferencerImpl(entityMergeEngine, enrichmentClient, dereferenceClient));
    doReturn(Arrays.stream(new String[0]).collect(Collectors.toSet())).when(dereferencer)
        .extractReferencesForDereferencing(any());

    // Execute the worker
    final EnrichmentWorkerImpl worker =
        spy(new EnrichmentWorkerImpl(dereferencer, enricher));
    final RDF inputRdf = new RDF();
    worker.process(inputRdf, mode);

    // Counters of method calls depend on the mode
    final boolean doDereferencing =
            mode == Mode.DEREFERENCE_AND_ENRICHMENT || mode == Mode.DEREFERENCE_ONLY;
    final boolean doEnrichment =
            mode == Mode.DEREFERENCE_AND_ENRICHMENT || mode == Mode.ENRICHMENT_ONLY;

    // Check the performed tasks
    verifyDereferencingNullFlow(doDereferencing, dereferencer, dereferenceClient, inputRdf);
    verifyEnrichmentNullFlow(doEnrichment, enricher, enrichmentClient, inputRdf);
    verifyMergeNullFlow(doDereferencing, entityMergeEngine);
  }

  // Verify dereference related calls
  private void verifyDereferencingHappyFlow(boolean doDereferencing, Dereferencer dereferencer,
      DereferenceClient dereferenceClient, RDF inputRdf) {
    if (doDereferencing) {

      // Extracting values for dereferencing
      verify(dereferencer, times(1)).extractReferencesForDereferencing(any());
      verify(dereferencer, times(1)).extractReferencesForDereferencing(inputRdf);

      // Actually dereferencing.
      verify(dereferenceClient, times(DEREFERENCE_EXTRACT_RESULT.length)).dereference(anyString());
      for (String dereferenceUrl : DEREFERENCE_EXTRACT_RESULT) {
        verify(dereferenceClient, times(1)).dereference(dereferenceUrl);
      }

    } else {
      verify(dereferencer, never()).extractReferencesForDereferencing(any());
      verify(dereferenceClient, never()).dereference(anyString());
    }
  }

  private void verifyDereferencingNullFlow(boolean doDereferencing, Dereferencer dereferencer,
      DereferenceClient dereferenceClient, RDF inputRdf) {
    if (doDereferencing) {

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

    } else {
      verify(dereferencer, never()).extractReferencesForDereferencing(any());
      verify(dereferenceClient, never()).dereference(anyString());
    }
  }

  // Verify enrichment related calls
  private void verifyEnrichmentHappyFlow(boolean doEnrichment, Enricher enricher,
      EnrichmentClient enrichmentClient, RDF inputRdf) {
    if (doEnrichment) {

      // Extracting values for enrichment
      verify(enricher, times(1)).extractValuesForEnrichment(any());
      verify(enricher, times(1)).extractValuesForEnrichment(inputRdf);

      // Actually enriching
      verify(enrichmentClient, times(1)).enrich(enrichmentExtractionCaptor.capture());
      assertArrayEquals(ENRICHMENT_EXTRACT_RESULT, enrichmentExtractionCaptor.getValue().toArray());

    } else {
      verify(enricher, never()).extractValuesForEnrichment(any());
      verify(enrichmentClient, never()).enrich(any());
    }
  }

  private void verifyEnrichmentNullFlow(boolean doEnrichment, Enricher worker,
      EnrichmentClient enrichmentClient, RDF inputRdf) {
    if (doEnrichment) {

      // Extracting values for enrichment
      verify(worker, times(1)).extractValuesForEnrichment(any());
      verify(worker, times(1)).extractValuesForEnrichment(inputRdf);

      // Actually enriching
      verify(enrichmentClient, times(0)).enrich(any());

    } else {
      verify(worker, never()).extractValuesForEnrichment(any());
      verify(enrichmentClient, never()).enrich(any());
    }
  }

  // Verify merge calls
  private void verifyMergeHappyFlow(boolean doEnrichment, boolean doDereferencing,
      EntityMergeEngine entityMergeEngine) {
    final List<List<EnrichmentBaseWrapper>> expectedMerges = new ArrayList<>();
    if (doDereferencing) {
      expectedMerges.add(DEREFERENCE_RESULT.stream().filter(Objects::nonNull)
              .map(EnrichmentResultList::getEnrichmentBaseWrapperList).flatMap(List::stream)
              .collect(Collectors.toList()));
    }
    if (doEnrichment) {
      expectedMerges.add(ENRICHMENT_RESULT.getEnrichmentBaseWrapperList());
    }
    verify(entityMergeEngine, times(expectedMerges.size())).mergeEntities(any(),
        enrichmentResultCaptor.capture());
    // Note that the captor returns a linked list, so we don't want to use indices.
    // But the interface gives a generic type List, so we don't want to depend on the
    // linked list functionality either.
    int currentPointer = 0;
    final List<List<EnrichmentBaseWrapper>> foundValues = enrichmentResultCaptor.getAllValues()
        .subList(
            enrichmentResultCaptor.getAllValues().size() - expectedMerges.size(),
            enrichmentResultCaptor.getAllValues().size());
    for (List<EnrichmentBaseWrapper> capturedMerge : foundValues) {
      assertArrayEquals(expectedMerges.get(currentPointer).toArray(), capturedMerge.toArray());
      currentPointer++;
    }
  }

  private void verifyMergeNullFlow(boolean doDereferencing, EntityMergeEngine entityMergeEngine) {
    int mergeCount = doDereferencing?1:0;
    verify(entityMergeEngine, times(mergeCount)).mergeEntities(any(), eq(Collections.emptyList()));
    verify(entityMergeEngine, times(mergeCount)).mergeEntities(any(), any());
  }

  @Test
  void testProcessWrapperMethods()
      throws JiBXException, UnsupportedEncodingException, DereferenceOrEnrichException, DereferenceException, EnrichmentException, SerializationException {

    // Create enrichment worker and mock the actual worker method as well as the RDF conversion
    // methods.
    final EnrichmentWorkerImpl worker = spy(new EnrichmentWorkerImpl(null, null));
    final RDF inputRdf = new RDF();
    final String outputString = "OutputString";
    doReturn(inputRdf).when(worker).convertStringToRdf(anyString());
    doReturn(outputString).when(worker).convertRdfToString(inputRdf);
    doReturn(inputRdf).when(worker).process(any(RDF.class), any());

    // Perform the operations and verify the result
    final RDF returnedRdf = worker.process(inputRdf);
    assertEquals(inputRdf, returnedRdf);
    final String returnedString = worker.process("");
    assertEquals(outputString, returnedString);

    // Validate the method calls to the actual worker method
    verify(worker, times(2)).process(any(RDF.class), any());
    verify(worker, times(2)).process(inputRdf, Mode.DEREFERENCE_AND_ENRICHMENT);

    // Test null string input
    try {
      worker.process((String) null);
      fail("Expected an exception to occur.");
    } catch (IllegalArgumentException e) {
      // This is expected
    }
  }

  @Test
  void testEnrichmentWorkerNullValues() throws DereferenceOrEnrichException {

    // Create enrichment worker
    final EnrichmentWorkerImpl worker = new EnrichmentWorkerImpl(null, null);

    // Test null string input
    try {
      worker.process((String) null, Mode.DEREFERENCE_AND_ENRICHMENT);
      fail("Expected an exception to occur.");
    } catch (IllegalArgumentException e) {
      // This is expected
    } catch (DereferenceException e) {
      e.printStackTrace();
    } catch (SerializationException e) {
      e.printStackTrace();
    } catch (EnrichmentException e) {
      e.printStackTrace();
    }

    // Test empty RDF input
    try {
      worker.process(new RDF(), null);
      fail("Expected an exception to occur.");
    } catch (IllegalArgumentException | EnrichmentException | DereferenceException e) {
      // This is expected
    }
  }
}
