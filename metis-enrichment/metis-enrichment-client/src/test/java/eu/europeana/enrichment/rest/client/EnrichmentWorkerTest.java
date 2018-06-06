package eu.europeana.enrichment.rest.client;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.enrichment.api.external.model.Agent;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.enrichment.api.external.model.Place;
import eu.europeana.enrichment.api.external.model.Timespan;
import eu.europeana.enrichment.rest.client.EnrichmentWorker.Mode;
import eu.europeana.enrichment.utils.EntityClass;
import eu.europeana.enrichment.utils.EntityMergeEngine;
import eu.europeana.enrichment.utils.InputValue;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.jibx.runtime.JiBXException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EnrichmentWorkerTest {

  @Captor
  private ArgumentCaptor<List<InputValue>> enrichmentExtractionCaptor;

  @Captor
  private ArgumentCaptor<List<EnrichmentBase>> enrichmentResultCaptor;

  private static final InputValue[] ENRICHMENT_EXTRACT_RESULT =
      {new InputValue("orig1", "value1", "lang1", EntityClass.AGENT),
          new InputValue(null, "value2", null, EntityClass.AGENT, EntityClass.CONCEPT),
          new InputValue("orig3", null, "lang2")};

  private static final String[] DEREFERENCE_EXTRACT_RESULT =
      {"enrich1", "enrich3", null, "enrich4"};

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
    final EnrichmentResultList dereferenceResult1 =
        new EnrichmentResultList(Arrays.asList(agent1, null, agent2));
    final EnrichmentResultList dereferenceResult2 =
        new EnrichmentResultList(Arrays.asList(timeSpan1, timeSpan2, null));
    DEREFERENCE_RESULT = Arrays.asList(dereferenceResult1, null, dereferenceResult2);
    ENRICHMENT_RESULT = new EnrichmentResultList(Arrays.asList(place1, null, place2));
  }
  
  @Test
  public void testEnrichmentWorkerHappyFlow() throws DereferenceOrEnrichException {
	  for (Mode mode : Mode.values()) {
		  testEnrichmentWorkerHappyFlow(mode);
	  }
  }
  
  @Test
  public void testEnrichmentWorkerNullFlow() throws DereferenceOrEnrichException {
	  for (Mode mode : Mode.values()) {
		  testEnrichmentWorkerNullFlow(mode);
	  }
  }
  
  private void testEnrichmentWorkerHappyFlow(Mode mode) throws DereferenceOrEnrichException {	  	
    // Create mocks of the dependencies
    final EnrichmentClient enrichmentClient = Mockito.mock(EnrichmentClient.class);
    doReturn(ENRICHMENT_RESULT).when(enrichmentClient).enrich(any());
    final DereferenceClient dereferenceClient = Mockito.mock(DereferenceClient.class);
    doReturn(DEREFERENCE_RESULT.get(0),
        DEREFERENCE_RESULT.subList(1, DEREFERENCE_RESULT.size()).toArray()).when(dereferenceClient)
            .dereference(any());
    final EntityMergeEngine entityMergeEngine = Mockito.mock(EntityMergeEngine.class);

    // Create enrichment worker and mock the enrichment and dereferencing results.
    final EnrichmentWorker worker =
        spy(new EnrichmentWorker(dereferenceClient, enrichmentClient, entityMergeEngine));
    doReturn(Arrays.asList(ENRICHMENT_EXTRACT_RESULT)).when(worker)
        .extractFieldsForEnrichment(any());
    doReturn(Arrays.stream(DEREFERENCE_EXTRACT_RESULT).collect(Collectors.toSet())).when(worker)
        .extractValuesForDereferencing(any());

    // Execute the worker
    final RDF inputRdf = new RDF();
    worker.process(inputRdf, mode);

    // Counters of method calls depend on the mode
    final boolean doDereferencing;
    final boolean doEnrichment;
    switch (mode) {
      case DEREFERENCE_AND_ENRICHMENT:
        doDereferencing = doEnrichment = true;
        break;
      case DEREFERENCE_ONLY:
        doDereferencing = true;
        doEnrichment = false;
        break;
      case ENRICHMENT_ONLY:
        doDereferencing = false;
        doEnrichment = true;
        break;
      default:
        throw new IllegalStateException("Unknown mode: " + mode.name());
    }

    // Check the performed tasks
    verifyDereferencingHappyFlow(doDereferencing, worker, dereferenceClient, inputRdf);
    verifyEnrichmentHappyFlow(doEnrichment, worker, enrichmentClient, inputRdf);
    verifyMergeHappyFlow(doEnrichment, doDereferencing, entityMergeEngine, enrichmentClient);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIllegalArgumentException() throws UnsupportedEncodingException, DereferenceOrEnrichException, JiBXException {
	  EnrichmentWorker enrichmentWorker = new EnrichmentWorker("", "");

	  String input = null;
	  enrichmentWorker.process(input);

  }
  
  private void testEnrichmentWorkerNullFlow(Mode mode) throws DereferenceOrEnrichException {	  	
	    // Create mocks of the dependencies
	    final EnrichmentClient enrichmentClient = Mockito.mock(EnrichmentClient.class);
	    doReturn(null).when(enrichmentClient).enrich(any());
	    final DereferenceClient dereferenceClient = Mockito.mock(DereferenceClient.class);
	 
	    final EntityMergeEngine entityMergeEngine = Mockito.mock(EntityMergeEngine.class);

	    // Create enrichment worker and mock the enrichment and dereferencing results.
	    final EnrichmentWorker worker =
	        spy(new EnrichmentWorker(dereferenceClient, enrichmentClient, entityMergeEngine));
	    doReturn(Arrays.asList(new InputValue[0])).when(worker).extractFieldsForEnrichment(any());
	    doReturn(Arrays.stream(new String[0]).collect(Collectors.toSet())).when(worker)
	    .extractValuesForDereferencing(any());

	    // Execute the worker
	    final RDF inputRdf = new RDF();
	    worker.process(inputRdf, mode);

	    // Counters of method calls depend on the mode
	    final boolean doDereferencing;
	    final boolean doEnrichment;
	    switch (mode) {
	      case DEREFERENCE_AND_ENRICHMENT:
	        doDereferencing = doEnrichment = true;
	        break;
	      case DEREFERENCE_ONLY:
	        doDereferencing = true;
	        doEnrichment = false;
	        break;
	      case ENRICHMENT_ONLY:
	        doDereferencing = false;
	        doEnrichment = true;
	        break;
	      default:
	        throw new IllegalStateException("Unknown mode: " + mode.name());
	    }

	    // Check the performed tasks
	    verifyDereferencingNullFlow(doDereferencing, worker, dereferenceClient, inputRdf);
	    verifyEnrichmentNullFlow(doEnrichment, worker, enrichmentClient, inputRdf);
	    verifyMergeNullFlow(doEnrichment, doDereferencing, entityMergeEngine, enrichmentClient);
	  }

  // Verify dereference related calls
  private void verifyDereferencingHappyFlow(boolean doDereferencing, EnrichmentWorker worker,
      DereferenceClient dereferenceClient, RDF inputRdf) {
    if (doDereferencing) {

      // Extracting values for dereferencing
      verify(worker, times(1)).extractValuesForDereferencing(any());
      verify(worker, times(1)).extractValuesForDereferencing(inputRdf);

      // Actually dereferencing: don't use the null values.
      final Set<String> dereferenceUrls = Arrays.stream(DEREFERENCE_EXTRACT_RESULT)
          .filter(Objects::nonNull).collect(Collectors.toSet());
      verify(dereferenceClient, times(dereferenceUrls.size())).dereference(anyString());
      for (String dereferenceUrl : dereferenceUrls) {
        verify(dereferenceClient, times(1)).dereference(dereferenceUrl);
      }

    } else {
      verify(worker, never()).extractValuesForDereferencing(any());
      verify(dereferenceClient, never()).dereference(anyString());
    }
  }
  
  private void verifyDereferencingNullFlow(boolean doDereferencing, EnrichmentWorker worker,
	      DereferenceClient dereferenceClient, RDF inputRdf) {
	    if (doDereferencing) {

	      // Extracting values for dereferencing
	      verify(worker, times(1)).extractValuesForDereferencing(any());
	      verify(worker, times(1)).extractValuesForDereferencing(inputRdf);

	      // Actually dereferencing: don't use the null values.
	      final Set<String> dereferenceUrls = Arrays.stream(new String[0])
	          .filter(Objects::nonNull).collect(Collectors.toSet());
	      verify(dereferenceClient, times(dereferenceUrls.size())).dereference(anyString());
	      for (String dereferenceUrl : dereferenceUrls) {
	        verify(dereferenceClient, times(1)).dereference(dereferenceUrl);
	      }

	    } else {
	      verify(worker, never()).extractValuesForDereferencing(any());
	      verify(dereferenceClient, never()).dereference(anyString());
	    }
	  }
  
  // Verify enrichment related calls
  private void verifyEnrichmentHappyFlow(boolean doEnrichment, EnrichmentWorker worker,
      EnrichmentClient enrichmentClient, RDF inputRdf) {
    if (doEnrichment) {

      // Extracting values for enrichment
      verify(worker, times(1)).extractFieldsForEnrichment(any());
      verify(worker, times(1)).extractFieldsForEnrichment(inputRdf);

      // Actually enriching
      verify(enrichmentClient, times(1)).enrich(enrichmentExtractionCaptor.capture());
      assertArrayEquals(ENRICHMENT_EXTRACT_RESULT, enrichmentExtractionCaptor.getValue().toArray());

    } else {
      verify(worker, never()).extractFieldsForEnrichment(any());
      verify(enrichmentClient, never()).enrich(any());
    }
  }
  
  private void verifyEnrichmentNullFlow(boolean doEnrichment, EnrichmentWorker worker,
	      EnrichmentClient enrichmentClient, RDF inputRdf) {
	    if (doEnrichment) {

	      // Extracting values for enrichment
	      verify(worker, times(1)).extractFieldsForEnrichment(any());
	      verify(worker, times(1)).extractFieldsForEnrichment(inputRdf);

	      // Actually enriching
	      verify(enrichmentClient, times(1)).enrich(enrichmentExtractionCaptor.capture());
	      assertArrayEquals(new InputValue[0], enrichmentExtractionCaptor.getValue().toArray());

	    } else {
	      verify(worker, never()).extractFieldsForEnrichment(any());
	      verify(enrichmentClient, never()).enrich(any());
	    }
	  }

  // Verify merge calls
  private void verifyMergeHappyFlow(boolean doEnrichment, boolean doDereferencing,
      EntityMergeEngine entityMergeEngine, EnrichmentClient enrichmentClient) {
    final List<EnrichmentResultList> expectedMerges = new ArrayList<>();
    if (doDereferencing) {
      expectedMerges.addAll(
          DEREFERENCE_RESULT.stream().filter(Objects::nonNull).collect(Collectors.toList()));
    }
    if (doEnrichment) {
      expectedMerges.add(ENRICHMENT_RESULT);
    }
    verify(entityMergeEngine, times(expectedMerges.size())).mergeEntities(any(),
        enrichmentResultCaptor.capture());
    // Note that the captor returns a linked list, so we don't want to use indices.
    // But the interface gives a generic type List, so we don't want to depend on the
    // linked list functionality either.
    int currentPointer = 0;
    final List<List<EnrichmentBase>> foundValues = enrichmentResultCaptor.getAllValues().subList(
        enrichmentResultCaptor.getAllValues().size() - expectedMerges.size(),
        enrichmentResultCaptor.getAllValues().size());
    for (List<EnrichmentBase> capturedMerge : foundValues) {
      assertArrayEquals(expectedMerges.get(currentPointer).getResult().toArray(),
          capturedMerge.toArray());
      currentPointer++;
    }
  }

  private void verifyMergeNullFlow(boolean doEnrichment, boolean doDereferencing,
	      EntityMergeEngine entityMergeEngine, EnrichmentClient enrichmentClient) {
	    final List<EnrichmentResultList> expectedMerges = new ArrayList<>();

	    verify(entityMergeEngine, times(expectedMerges.size())).mergeEntities(any(),
	        enrichmentResultCaptor.capture());
	    // Note that the captor returns a linked list, so we don't want to use indices.
	    // But the interface gives a generic type List, so we don't want to depend on the
	    // linked list functionality either.
	    int currentPointer = 0;
	    final List<List<EnrichmentBase>> foundValues = enrichmentResultCaptor.getAllValues().subList(
	        enrichmentResultCaptor.getAllValues().size() - expectedMerges.size(),
	        enrichmentResultCaptor.getAllValues().size());
	    for (List<EnrichmentBase> capturedMerge : foundValues) {
	      assertArrayEquals(expectedMerges.get(currentPointer).getResult().toArray(),
	          capturedMerge.toArray());
	      currentPointer++;
	    }
	  }
  
  @Test
  public void testProcessWrapperMethods()
      throws JiBXException, UnsupportedEncodingException, DereferenceOrEnrichException {

    // Create enrichment worker and mock the actual worker method as well as the RDF conversion
    // methods.
    final EnrichmentWorker worker = spy(new EnrichmentWorker(null, null, null));
    final RDF inputRdf = new RDF();
    final String outputString = "OutputString";
    doReturn(inputRdf).when(worker).convertStringToRdf(anyString());
    doReturn(outputString).when(worker).convertRdfToString(inputRdf);
    doReturn(inputRdf).when(worker).process(any(), any());

    // Perform the operations and verify the result
    final RDF returnedRdf = worker.process(inputRdf);
    assertEquals(inputRdf, returnedRdf);
    final String returnedString = worker.process("");
    assertEquals(outputString, returnedString);

    // Validate the method calls to the actual worker method
    verify(worker, times(2)).process(any(), any());
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
  public void testEnrichmentWorkerNullValues() throws DereferenceOrEnrichException {
    // Create enrichment worker
    final EnrichmentWorker worker = new EnrichmentWorker(null, null, null);

    // Test null string input
    try {
      worker.process(null, Mode.DEREFERENCE_AND_ENRICHMENT);
      fail("Expected an exception to occur.");
    } catch (IllegalArgumentException e) {
      // This is expected
    }

    // Test null string input
    try {
      worker.process(new RDF(), null);
      fail("Expected an exception to occur.");
    } catch (IllegalArgumentException e) {
      // This is expected
    }
  }
}
