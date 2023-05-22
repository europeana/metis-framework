package eu.europeana.enrichment.rest.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
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

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.http.JvmProxyConfigurer;
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
import eu.europeana.enrichment.rest.client.report.ProcessedResult.RecordStatus;
import eu.europeana.enrichment.rest.client.report.Report;
import eu.europeana.enrichment.rest.client.report.Type;
import eu.europeana.metis.schema.convert.SerializationException;
import eu.europeana.metis.schema.jibx.RDF;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

class EnrichmentWorkerImplTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(EnrichmentWorkerImplTest.class);
  private static WireMockServer wireMockServer;

  @BeforeAll
  static void createWireMock() {
    wireMockServer = new WireMockServer(wireMockConfig()
        .dynamicPort()
        .enableBrowserProxying(true)
        .notifier(new ConsoleNotifier(true)));
    wireMockServer.start();
    JvmProxyConfigurer.configureFor(wireMockServer);
  }

  @AfterAll
  static void tearDownWireMock() {
    wireMockServer.stop();
  }

  private static Stream<Arguments> providedInputRecords() {
    return Stream.of(
        Arguments.of(getResourceFileContent("enrichment/sample_enrichment_exception.rdf"), RecordStatus.STOP),
        Arguments.of(getResourceFileContent("enrichment/sample_dereference_not_found.rdf"), RecordStatus.CONTINUE),
        Arguments.of(getResourceFileContent("enrichment/sample_dereference_redirect.rdf"), RecordStatus.CONTINUE),
        Arguments.of(getResourceFileContent("enrichment/sample_enrichment_noentity.rdf"), RecordStatus.CONTINUE),
        Arguments.of(getResourceFileContent("enrichment/sample_enrichment_failure.rdf"), RecordStatus.STOP),
        Arguments.of(getResourceFileContent("enrichment/sample_enrichment_success.rdf"), RecordStatus.CONTINUE)
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
  void testEnrichmentWorkerHappyFlow(String inputRecord, RecordStatus recordStatus)
      throws DereferenceException, EnrichmentException {
    setDereferenceMocks();
    setEntityAPIMocks();

    TreeSet<Mode> modeSetWithBoth = new TreeSet<>();
    modeSetWithBoth.add(Mode.ENRICHMENT);
    modeSetWithBoth.add(Mode.DEREFERENCE);

    EnricherProvider enricherProvider = new EnricherProvider();
    enricherProvider.setEnrichmentPropertiesValues("http://localhost:" + wireMockServer.port() + "/entitymgmt",
        "http://localhost:" + wireMockServer.port() + "/entity",
        "api2demo");

    final Enricher enricher = enricherProvider.create();

    DereferencerProvider dereferencerProvider = new DereferencerProvider();
    dereferencerProvider.setEnrichmentPropertiesValues("http://entity-api.mock/entity",
        "http://entity-api.mock/entity",
        "api2demo");

    dereferencerProvider.setDereferenceUrl("http://dereference-rest.mock");
    final Dereferencer dereferencer = dereferencerProvider.create();

    // Execute the worker
    final EnrichmentWorkerImpl worker = new EnrichmentWorkerImpl(dereferencer, enricher);

    ProcessedResult<String> output = worker.process(inputRecord, modeSetWithBoth);

    LOGGER.info("REPORT: {}\n\n", output.getReport());
    LOGGER.info("RECORD: {}\n\n", output.getProcessedRecord());
    LOGGER.info("STATUS: {}", output.getRecordStatus());
    assertEquals(recordStatus, output.getRecordStatus());
  }

  private static void setEntityAPIMocks() {
    wireMockServer.stubFor(get(urlEqualTo("/entitymgmt/concept/base/84?wskey=api2demo"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(getResourceFileContent("entity-api/entity-api-response-concept-base.json"))
            .withStatus(HttpStatus.OK.value())));

    wireMockServer.stubFor(get(urlEqualTo("/entity/enrich?wskey=api2demo&text=Religion&lang=en&type=concept"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(getResourceFileContent("entity-api/entity-api-response-concept.json"))
            .withStatus(HttpStatus.OK.value())));

    wireMockServer.stubFor(get(urlEqualTo("/entity/enrich?wskey=api2demo&text=Piotras%20Kalabuchovas&type=agent"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(getResourceFileContent("entity-api/entity-api-response-agent-nomatch.json"))
            .withStatus(HttpStatus.OK.value())));

    wireMockServer.stubFor(get(urlEqualTo("/entity/enrich?wskey=api2demo&text=Paranguaricutirimicuaro&lang=en&type=concept"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(getResourceFileContent("entity-api/entity-api-response-concept-nomatch.json"))
            .withStatus(HttpStatus.OK.value())));
    wireMockServer.stubFor(
        get(urlEqualTo("/entity/enrich?wskey=api2demo&text=Lietuvos%20Centrinis%20Valstyb%C3%A9s%20Archyvas&type=organization"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(getResourceFileContent("entity-api/entity-api-response-organization.json"))
                .withStatus(HttpStatus.OK.value())));
    wireMockServer.stubFor(
        get(urlEqualTo("/entity/enrich?wskey=api2demo&text=EFG%20-%20The%20European%20Film%20Gateway&lang=en&type=organization"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(getResourceFileContent("entity-api/entity-api-response-organization.json"))
                .withStatus(HttpStatus.OK.value())));

    wireMockServer.stubFor(get(urlEqualTo("/entitymgmt/organization/base/1482250000004671158?wskey=api2demo"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(getResourceFileContent("entity-api/entity-api-response-organization-base.json"))
            .withStatus(HttpStatus.OK.value())));

    wireMockServer.stubFor(get(urlEqualTo("/entity/enrich?wskey=api2demo&text=1957&lang=en&type=timespan"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(getResourceFileContent("entity-api/entity-api-response-timespan.json"))
            .withStatus(HttpStatus.OK.value())));

    wireMockServer.stubFor(get(urlEqualTo(
        "/entity/resolve?uri=http://dbpedia.org/resource/Lithuanian_Soviet_Socialist_Republic_%25281918%25E2%2580%25931919%2529&wskey=api2demo"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(getResourceFileContent("entity-api/entity-api-response-resolve-uri-nomatch.json"))
            .withStatus(HttpStatus.OK.value())));

    wireMockServer.stubFor(get(urlEqualTo("/entity/resolve?uri=https://sws.geonames.org/597427/&wskey=api2demo"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(getResourceFileContent("entity-api/entity-api-response-resolve-uri-concept.json"))
            .withStatus(HttpStatus.OK.value())));

    wireMockServer.stubFor(get(urlEqualTo("/entity/resolve?uri=http://vocab.getty.edu/aat/300136900&wskey=api2demo"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(getResourceFileContent("entity-api/entity-api-response-resolve-uri-concept.json"))
            .withStatus(HttpStatus.OK.value())));
    wireMockServer.stubFor(get(urlEqualTo("/entity/resolve?uri=http://dbpedia.org/resource/Lithuania&wskey=api2demo"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(getResourceFileContent("entity-api/entity-api-response-resolve-uri-place.json"))
            .withStatus(HttpStatus.OK.value())));
    wireMockServer.stubFor(get(urlEqualTo("/entity/enrich?wskey=api2demo&text=Muziek&lang=nl&type=concept"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(getResourceFileContent("entity-api/entity-api-response-concept-ii.json"))
            .withStatus(HttpStatus.OK.value())));
    wireMockServer.stubFor(get(urlEqualTo("/entitymgmt/concept/base/62?wskey=api2demo"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(getResourceFileContent("entity-api/entity-api-response-concept-ii-base.json"))
            .withStatus(HttpStatus.OK.value())));
    wireMockServer.stubFor(get(urlEqualTo("/entity/enrich?wskey=api2demo&text=G%C3%BCiro&lang=es&type=concept"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(getResourceFileContent("entity-api/entity-api-response-concept-iii.json"))
            .withStatus(HttpStatus.OK.value())));
    wireMockServer.stubFor(get(urlEqualTo("/entity/resolve?uri=http://www.mimo-db.eu/InstrumentsKeywords/0&wskey=api2demo"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(getResourceFileContent("entity-api/entity-api-response-resolve-uri-concept-ii.json"))
            .withStatus(HttpStatus.OK.value())));
    wireMockServer.stubFor(get(urlEqualTo("/entity/resolve?uri=http://www.mimo-db.eu/InstrumentsKeywords/3052&wskey=api2demo"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(getResourceFileContent("entity-api/entity-api-response-resolve-uri-concept-ii.json"))
            .withStatus(HttpStatus.OK.value())));
    wireMockServer.stubFor(get(urlEqualTo("/entity/resolve?uri=http://vocab.getty.edu/aat/400136800&wskey=api2demo"))
            .willReturn(aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody(getResourceFileContent("entity-api/entity-api-response-resolve-uri-concept.json"))
                    .withStatus(HttpStatus.OK.value())));
  }

  private static void setDereferenceMocks() {
    wireMockServer.stubFor(get(urlEqualTo("/dereference?uri=https%3A%2F%2Fsws.geonames.org%2F597427%2F"))
        .withHost(equalTo("dereference-rest.mock"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/xml")
            .withBody(getResourceFileContent("dereference/dereference-geoname.xml"))
            .withStatus(HttpStatus.OK.value())));
    wireMockServer.stubFor(get(urlEqualTo("/dereference?uri=http%3A%2F%2Fvocab.getty.edu%2Faat%2F300136900"))
        .withHost(equalTo("dereference-rest.mock"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/xml")
            .withBody(getResourceFileContent("dereference/dereference-vocabulary.xml"))
            .withStatus(HttpStatus.OK.value())));
    wireMockServer.stubFor(get(urlEqualTo("/dereference?uri=http%3A%2F%2Fwww.mimo-db.eu%2FInstrumentsKeywords%2F3052"))
        .withHost(equalTo("dereference-rest.mock"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/xml")
            .withBody(getResourceFileContent("dereference/dereference-normal.xml"))
            .withStatus(HttpStatus.OK.value())));
    wireMockServer.stubFor(get(urlEqualTo("/dereference?uri=http%3A%2F%2Fwww.mimo-db.eu%2FInstrumentsKeywords%2F0"))
        .withHost(equalTo("dereference-rest.mock"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/xml")
            .withBody(getResourceFileContent("dereference/dereference-no-entity.xml"))
            .withStatus(HttpStatus.OK.value())));
    wireMockServer.stubFor(get(urlEqualTo("/dereference?uri=http%3A%2F%2Fsemantics.gr%2Fauthorities%2Fthematic_tags%2F994210004"))
        .withHost(equalTo("dereference-rest.mock"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/xml")
            .withBody(getResourceFileContent("dereference/dereference-normal-redirect.xml"))
            .withStatus(HttpStatus.OK.value())));
    wireMockServer.stubFor(get(urlEqualTo("/dereference?uri=http%3A%2F%2Fvocab.getty.edu%2Faat%2F400136800"))
            .withHost(equalTo("dereference-rest.mock"))
            .willReturn(aResponse()
                    .withHeader("Content-Type", "application/xml")
                    .withBody(getResourceFileContent("dereference/dereference-failure.xml"))
                    .withStatus(HttpStatus.OK.value())));
  }

  @Test
  void testEnrichmentWorkerHappyFlow() throws DereferenceException, EnrichmentException {
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
  void testEnrichmentWorkerNullFlow() throws DereferenceException, EnrichmentException {
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
    for (Report report : resultString.getReport()) {
      assertEquals(Type.ERROR, report.getMessageType());
      assertTrue(report.getMessage().contains("Input RDF string cannot be null."));
      assertTrue(report.getStackTrace().contains("IllegalArgumentException"));
    }
    assertEquals(1, resultString.getReport().size());
    assertEquals(RecordStatus.STOP, resultString.getRecordStatus());
  }

  @Test
  void testEnrichmentWorkerSerializationException() {
    final EnrichmentWorkerImpl worker = spy(new EnrichmentWorkerImpl(null, null));

    final ProcessedResult<String> stringProcessedResult = worker.process(
        "<?xml version=\"1.0\"?>\n"
        + "<rdf:RDF\n"
        + "xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"
        + "xmlns:si=\"https://www.europeana.eu/rdf/\">\n"
        + "<rdf:Description rdf:about=\"https://www.europeana.eu\">\n"
        + "  <si:title>Europe Cultural Heritage</si:title>\n"
        + "  <si:author>Europeana</si:author>\n"
        + "</rdf:Description>\n"
        + "</rdf:RDF>");
    final String returnedString = stringProcessedResult.getProcessedRecord();
    assertEquals(null, returnedString);
    for (Report report : stringProcessedResult.getReport()) {
      assertEquals(Type.ERROR, report.getMessageType());
      assertTrue(report.getMessage().contains("Error serializing rdf"));
      assertTrue(report.getStackTrace().contains("SerializationException: Something went wrong with converting to or from the RDF format."));
    }
    assertEquals(1, stringProcessedResult.getReport().size());
    assertEquals(RecordStatus.STOP, stringProcessedResult.getRecordStatus());

    // Validate the method calls to the actual worker method
    verify(worker, times(1)).process(any(String.class), any());
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
    for (Report report : resultString.getReport()) {
      assertEquals(Type.ERROR, report.getMessageType());
      assertTrue(report.getMessage().contains("Input RDF string cannot be null."));
      assertTrue(report.getStackTrace().contains("IllegalArgumentException"));
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
    for (Report report : resultRdf.getReport()) {
      assertEquals(Type.ERROR, report.getMessageType());
      assertTrue(report.getMessage().contains("Set of Modes cannot be null"));
      assertTrue(report.getStackTrace().contains("IllegalArgumentException"));
    }
    assertEquals(1, resultRdf.getReport().size());
    assertEquals(RecordStatus.STOP, resultRdf.getRecordStatus());
  }
}
