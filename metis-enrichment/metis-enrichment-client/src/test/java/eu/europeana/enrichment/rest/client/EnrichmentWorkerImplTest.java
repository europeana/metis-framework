package eu.europeana.enrichment.rest.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import eu.europeana.enrichment.rest.client.EnrichmentWorker.Mode;
import eu.europeana.enrichment.rest.client.dereference.Dereferencer;
import eu.europeana.enrichment.rest.client.dereference.DereferencerImpl;
import eu.europeana.enrichment.rest.client.dereference.DereferencerProvider;
import eu.europeana.enrichment.rest.client.enrichment.Enricher;
import eu.europeana.enrichment.rest.client.enrichment.EnricherImpl;
import eu.europeana.enrichment.rest.client.enrichment.EnricherProvider;
import eu.europeana.enrichment.rest.client.exceptions.DereferenceException;
import eu.europeana.enrichment.rest.client.exceptions.EnrichmentException;
import eu.europeana.enrichment.rest.client.report.ProcessedResult;
import eu.europeana.enrichment.rest.client.report.RecordStatus;
import eu.europeana.enrichment.rest.client.report.ReportMessage;
import eu.europeana.enrichment.rest.client.report.Type;
import eu.europeana.metis.schema.convert.SerializationException;
import eu.europeana.metis.schema.jibx.RDF;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class EnrichmentWorkerImplTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(EnrichmentWorkerImplTest.class);

  private static Stream<Arguments> providedInputRecords() {
    return Stream.of(
        Arguments.of(getResourceFileContent("sample_for_exception.rdf")),
        Arguments.of(getResourceFileContent("sample_no_dereference.rdf")),
        Arguments.of(getResourceFileContent("sample_no_reference.rdf"))
    );
  }

  private static String getResourceFileContent(String fileName) {
    try {
      return new String(
          EnrichmentWorkerImplTest.class.getClassLoader().getResourceAsStream(fileName).readAllBytes()
      );
    } catch (IOException ioException) {
      return "";
    }
  }

  @ParameterizedTest
  @MethodSource("providedInputRecords")
  void testEnrichmentWorkerHappyFlow(String inputRecord)
      throws DereferenceException, EnrichmentException, SerializationException {
    TreeSet<Mode> modeSetWithBoth = new TreeSet<>();
    modeSetWithBoth.add(Mode.ENRICHMENT);
    modeSetWithBoth.add(Mode.DEREFERENCE);
    EnricherProvider enricherProvider = new EnricherProvider();
    enricherProvider.setEnrichmentPropertiesValues("https://entity-management-production.eanadev.org/entity",
        "https://entity-api-v2-production.eanadev.org/entity",
        "api2demo");

    final Enricher enricher = enricherProvider.create();

    DereferencerProvider dereferencerProvider = new DereferencerProvider();
    dereferencerProvider.setEnrichmentPropertiesValues("https://entity-management-production.eanadev.org/entity",
        "https://entity-api-v2-production.eanadev.org/entity",
        "api2demo");
    dereferencerProvider.setDereferenceUrl("https://metis-dereference-rest-production.eanadev.org/");

    final Dereferencer dereferencer = dereferencerProvider.create();

    // Execute the worker
    final EnrichmentWorkerImpl worker = new EnrichmentWorkerImpl(dereferencer, enricher);
    //  RdfConversionUtils rdfConversionUtils = new RdfConversionUtils();
    //  final RDF inputRdf = rdfConversionUtils.convertStringToRdf(inputRecord);
    //  worker.cleanupPreviousEnrichmentEntities(inputRdf);
    ProcessedResult<String> output = worker.process(inputRecord, modeSetWithBoth);

    LOGGER.info("REPORT: {}\n\n", output.getReport());
    LOGGER.info("RECORD: {}\n\n", output.getProcessedRecord());
    LOGGER.info("STATUS: {}", output.getRecordStatus());
    assertEquals(RecordStatus.CONTINUE, output.getRecordStatus());
  }

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
  void testProcessWrapperMethods() throws SerializationException {

    // Create enrichment worker and mock the actual worker method as well as the RDF conversion
    // methods.
    final EnrichmentWorkerImpl worker = spy(new EnrichmentWorkerImpl(null, null));
    final RDF inputRdf = new RDF();
    final String outputString = "OutputString";
    doReturn(inputRdf).when(worker).convertStringToRdf(anyString());
    doReturn(outputString).when(worker).convertRdfToString(inputRdf);

    doReturn(new ProcessedResult<>(inputRdf)).when(worker).process(any(RDF.class), any());

    // Perform the operations and verify the result
    final ProcessedResult<RDF> rdfProcessedResult = worker.process(inputRdf);
    final RDF returnedRdf = rdfProcessedResult.getProcessedRecord();
    assertEquals(inputRdf, returnedRdf);
    assertTrue(rdfProcessedResult.getReport().isEmpty());
    assertEquals(RecordStatus.CONTINUE, rdfProcessedResult.getRecordStatus());

    final ProcessedResult<String> stringProcessedResult = worker.process("");
    final String returnedString = stringProcessedResult.getProcessedRecord();
    assertEquals(outputString, returnedString);
    assertTrue(rdfProcessedResult.getReport().isEmpty());
    assertEquals(RecordStatus.CONTINUE, rdfProcessedResult.getRecordStatus());

    TreeSet<Mode> modeSetWithBoth = new TreeSet<>();
    modeSetWithBoth.add(Mode.ENRICHMENT);
    modeSetWithBoth.add(Mode.DEREFERENCE);

    // Validate the method calls to the actual worker method
    verify(worker, times(2)).process(any(RDF.class), any());
    verify(worker, times(2)).process(inputRdf, modeSetWithBoth);

    // Test null string input
    ProcessedResult<String> resultString = worker.process((String) null);
    assertEquals(RecordStatus.STOP, resultString.getRecordStatus());
    for (ReportMessage reportMessage : resultString.getReport()) {
      assertEquals(Type.ERROR, reportMessage.getMessageType());
      assertTrue(reportMessage.getMessage().contains("Input RDF string cannot be null."));
      assertTrue(reportMessage.getStackTrace().contains("IllegalArgumentException"));
    }
    assertEquals(1, resultString.getReport().size());
    assertEquals(RecordStatus.STOP, resultString.getRecordStatus());
  }

  @Test
  void testEnrichmentWorkerInputNullValues() {
    // Create enrichment worker
    final EnrichmentWorkerImpl worker = new EnrichmentWorkerImpl(null, null);

    TreeSet<Mode> modeSetWithBoth = new TreeSet<>();
    modeSetWithBoth.add(Mode.ENRICHMENT);
    modeSetWithBoth.add(Mode.DEREFERENCE);

    // Test null string input
    ProcessedResult<String> resultString = worker.process((String) null, modeSetWithBoth);
    assertEquals(RecordStatus.STOP, resultString.getRecordStatus());
    for (ReportMessage reportMessage : resultString.getReport()) {
      assertEquals(Type.ERROR, reportMessage.getMessageType());
      assertTrue(reportMessage.getMessage().contains("Input RDF string cannot be null."));
      assertTrue(reportMessage.getStackTrace().contains("IllegalArgumentException"));
    }
    assertEquals(1, resultString.getReport().size());
    assertEquals(RecordStatus.STOP, resultString.getRecordStatus());
  }

  @Test
  void testEnrichmentWorkerModeNullValues() {
    // Create enrichment worker
    final EnrichmentWorkerImpl worker = new EnrichmentWorkerImpl(null, null);

    // Test empty RDF input
    ProcessedResult<RDF> resultRdf = worker.process(new RDF(), null);
    assertEquals(RecordStatus.STOP, resultRdf.getRecordStatus());
    for (ReportMessage reportMessage : resultRdf.getReport()) {
      assertEquals(Type.ERROR, reportMessage.getMessageType());
      assertTrue(reportMessage.getMessage().contains("Set of Modes cannot be null"));
      assertTrue(reportMessage.getStackTrace().contains("IllegalArgumentException"));
    }
    assertEquals(1, resultRdf.getReport().size());
    assertEquals(RecordStatus.STOP, resultRdf.getRecordStatus());
  }
}
