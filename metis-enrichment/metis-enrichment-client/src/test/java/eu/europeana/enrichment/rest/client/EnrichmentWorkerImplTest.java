package eu.europeana.enrichment.rest.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import eu.europeana.metis.schema.jibx.RDF;
import eu.europeana.enrichment.rest.client.EnrichmentWorker.Mode;
import eu.europeana.enrichment.rest.client.dereference.Dereferencer;
import eu.europeana.enrichment.rest.client.dereference.DereferencerImpl;
import eu.europeana.enrichment.rest.client.enrichment.Enricher;
import eu.europeana.enrichment.rest.client.enrichment.EnricherImpl;
import eu.europeana.enrichment.rest.client.exceptions.DereferenceException;
import eu.europeana.enrichment.rest.client.exceptions.EnrichmentException;
import eu.europeana.enrichment.rest.client.exceptions.SerializationException;
import java.util.Set;
import java.util.TreeSet;
import org.jibx.runtime.JiBXException;
import org.junit.jupiter.api.Test;

class EnrichmentWorkerImplTest {


  @Test
  void testEnrichmentWorkerHappyFlow()
      throws DereferenceException, EnrichmentException {
    TreeSet<Mode> modeSetWithOnlyEnrichment = new TreeSet<>();
    TreeSet<Mode> modeSetWithOnlyDereference = new TreeSet<>();
    TreeSet<Mode> modeSetWithBoth = new TreeSet<>();

    modeSetWithOnlyEnrichment.add(Mode.ENRICHMENT);
    testEnrichmentWorkerHappyFlow(modeSetWithOnlyEnrichment);
    modeSetWithOnlyDereference.add(Mode.DEREFERENCE);
    testEnrichmentWorkerHappyFlow(modeSetWithOnlyDereference);
    modeSetWithBoth.add(Mode.ENRICHMENT);
    modeSetWithBoth.add(Mode.DEREFERENCE);
    testEnrichmentWorkerHappyFlow(modeSetWithBoth);
  }

  @Test
  void testEnrichmentWorkerNullFlow()
      throws DereferenceException, EnrichmentException {
    TreeSet<Mode> modeSetWithOnlyEnrichment = new TreeSet<>();
    TreeSet<Mode> modeSetWithOnlyDereference = new TreeSet<>();
    TreeSet<Mode> modeSetWithBoth = new TreeSet<>();

    modeSetWithOnlyEnrichment.add(Mode.ENRICHMENT);
    testEnrichmentWorkerNullFlow(modeSetWithOnlyEnrichment);
    modeSetWithOnlyDereference.add(Mode.DEREFERENCE);
    testEnrichmentWorkerNullFlow(modeSetWithOnlyDereference);
    modeSetWithBoth.add(Mode.ENRICHMENT);
    modeSetWithBoth.add(Mode.DEREFERENCE);
    testEnrichmentWorkerNullFlow(modeSetWithBoth);
  }

  private void testEnrichmentWorkerHappyFlow(Set<Mode> modes)
      throws DereferenceException, EnrichmentException {

    // Create enricher and mock it.
    final Enricher enricher = mock(EnricherImpl.class);

    final Dereferencer dereferencer = mock(DereferencerImpl.class);

    // Execute the worker
    final EnrichmentWorkerImpl worker = new EnrichmentWorkerImpl(dereferencer, enricher);
    final RDF inputRdf = new RDF();
    worker.process(inputRdf, modes);

    // Counters of method calls depend on the mode
    final boolean doDereferencing = modes.contains(Mode.DEREFERENCE);
    final boolean doEnrichment = modes.contains(Mode.ENRICHMENT);

    // Check the performed tasks
    verifyDereferencingHappyFlow(doDereferencing, dereferencer, inputRdf);
    verifyEnrichmentHappyFlow(doEnrichment, enricher, inputRdf);
//    verifyMergeHappyFlow(doEnrichment, doDereferencing, entityMergeEngine);
  }

  private void testEnrichmentWorkerNullFlow(Set<Mode> modes)
      throws DereferenceException, EnrichmentException {

    // Create enrichment worker and mock the enrichment and dereferencing results.
    final Enricher enricher = mock(EnricherImpl.class);

    final Dereferencer dereferencer = mock(DereferencerImpl.class);

    // Execute the worker
    final EnrichmentWorkerImpl worker =
        spy(new EnrichmentWorkerImpl(dereferencer, enricher));
    final RDF inputRdf = new RDF();
    worker.process(inputRdf, modes);

    // Counters of method calls depend on the mode
    final boolean doDereferencing = modes.contains(Mode.DEREFERENCE);
    final boolean doEnrichment = modes.contains(Mode.ENRICHMENT);

    // Check the performed tasks
    verifyDereferencingNullFlow(doDereferencing, dereferencer, inputRdf);
    verifyEnrichmentNullFlow(doEnrichment, enricher, inputRdf);

  }

  // Verify dereference related calls
  private void verifyDereferencingHappyFlow(boolean doDereferencing, Dereferencer dereferencer,
      RDF inputRdf) throws DereferenceException {
    if (doDereferencing) {
      verify(dereferencer, times(1)).dereference(inputRdf);

    } else {
      verify(dereferencer, never()).dereference(any());
    }
  }

  private void verifyDereferencingNullFlow(boolean doDereferencing, Dereferencer dereferencer,
      RDF inputRdf) throws DereferenceException {
    if (doDereferencing) {

      verify(dereferencer, times(1)).dereference(inputRdf);

    } else {
      verify(dereferencer, never()).dereference(any());
    }
  }

  // Verify enrichment related calls
  private void verifyEnrichmentHappyFlow(boolean doEnrichment, Enricher enricher,
      RDF inputRdf) throws EnrichmentException {
    if (doEnrichment) {
      verify(enricher, times(1)).enrichment(inputRdf);

    } else {
      verify(enricher, never()).enrichment(any());
    }
  }

  private void verifyEnrichmentNullFlow(boolean doEnrichment, Enricher worker, RDF inputRdf)
      throws EnrichmentException {
    if (doEnrichment) {
      verify(worker, times(1)).enrichment(inputRdf);

    } else {
      verify(worker, never()).enrichment(any());
    }
  }


  @Test
  void testProcessWrapperMethods()
      throws JiBXException, DereferenceException, EnrichmentException, SerializationException {

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

    TreeSet<Mode> modeSetWithBoth = new TreeSet<>();
    modeSetWithBoth.add(Mode.ENRICHMENT);
    modeSetWithBoth.add(Mode.DEREFERENCE);

    // Validate the method calls to the actual worker method
    verify(worker, times(2)).process(any(RDF.class), any());
    verify(worker, times(2)).process(inputRdf, modeSetWithBoth);

    // Test null string input
    try {
      worker.process((String) null);
      fail("Expected an exception to occur.");
    } catch (IllegalArgumentException e) {
      // This is expected
    }
  }

  @Test
  void testEnrichmentWorkerNullValues(){

    // Create enrichment worker
    final EnrichmentWorkerImpl worker = new EnrichmentWorkerImpl(null, null);

    TreeSet<Mode> modeSetWithBoth = new TreeSet<>();
    modeSetWithBoth.add(Mode.ENRICHMENT);
    modeSetWithBoth.add(Mode.DEREFERENCE);

    // Test null string input
    try {
      worker.process((String) null, modeSetWithBoth);
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
