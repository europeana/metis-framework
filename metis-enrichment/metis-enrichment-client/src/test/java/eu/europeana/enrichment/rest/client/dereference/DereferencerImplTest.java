package eu.europeana.enrichment.rest.client.dereference;

import static com.github.tomakehurst.wiremock.client.WireMock.badRequest;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.temporaryRedirect;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.http.JvmProxyConfigurer;
import eu.europeana.enrichment.api.external.DereferenceResultStatus;
import eu.europeana.enrichment.api.external.impl.ClientEntityResolver;
import eu.europeana.enrichment.api.external.model.Agent;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentResultBaseWrapper;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.enrichment.api.external.model.Place;
import eu.europeana.enrichment.api.external.model.TimeSpan;
import eu.europeana.enrichment.api.internal.ReferenceTermImpl;
import eu.europeana.enrichment.api.internal.SearchTerm;
import eu.europeana.enrichment.api.internal.SearchTermImpl;
import eu.europeana.enrichment.rest.client.EnrichmentWorker.Mode;
import eu.europeana.enrichment.rest.client.report.Report;
import eu.europeana.enrichment.rest.client.report.Type;
import eu.europeana.enrichment.utils.EntityMergeEngine;
import eu.europeana.enrichment.utils.EntityType;
import eu.europeana.metis.schema.jibx.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

/**
 * Unit tests for {@link DereferencerImpl} class
 */
public class DereferencerImplTest {

  private static final Map<Class<? extends AboutType>,Set<String>> DEREFERENCE_EXTRACT_RESULT_INVALID = Map.of(
      AboutType.class, Set.of("http://invalid-example.host/about"),
      Concept.class, Set.of("http://invalid-example.host/concept"),
      PlaceType.class, Set.of("http://invalid-example.host/place"));

  private static final Map<Class<? extends AboutType>,Set<String>> DEREFERENCE_EXTRACT_RESULT_VALID = Map.of(
          AboutType.class, Set.of("http://valid-example.host/about"),
          Concept.class,Set.of("http://valid-example.host/concept"),
          PlaceType.class, Set.of("http://valid-example.host/place"));

  private static final List<EnrichmentResultList> DEREFERENCE_RESULT;
  private static final Map<SearchTerm, List<EnrichmentBase>> ENRICHMENT_RESULT = new HashMap<>();

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

  static {
    final Agent agent1 = new Agent();
    agent1.setAbout("agent1");
    final Agent agent2 = new Agent();
    agent2.setAbout("agent2");
    final Place place1 = new Place();
    place1.setAbout("place1");
    final Place place2 = new Place();
    place2.setAbout("place2");
    final TimeSpan timeSpan1 = new TimeSpan();
    timeSpan1.setAbout("timespan1");
    final TimeSpan timeSpan2 = new TimeSpan();
    timeSpan2.setAbout("timespan2");
    final List<EnrichmentResultBaseWrapper> enrichmentResultBaseWrapperList1 = EnrichmentResultBaseWrapper
        .createEnrichmentResultBaseWrapperList(List.of(Arrays.asList(agent1, null, agent2)), DereferenceResultStatus.SUCCESS);
    final EnrichmentResultList dereferenceResult1 = new EnrichmentResultList(
        enrichmentResultBaseWrapperList1);
    final List<EnrichmentResultBaseWrapper> enrichmentResultBaseWrapperList2 = EnrichmentResultBaseWrapper
        .createEnrichmentResultBaseWrapperList(List.of(Arrays.asList(timeSpan1, timeSpan2, null)),
            DereferenceResultStatus.SUCCESS);
    final EnrichmentResultList dereferenceResult2 = new EnrichmentResultList(
        enrichmentResultBaseWrapperList2);
    DEREFERENCE_RESULT = Arrays.asList(dereferenceResult1, null, dereferenceResult2);

    SearchTerm searchTerm1 = new SearchTermImpl("value1", "en", Set.of(EntityType.PLACE));
    SearchTerm searchTerm2 = new SearchTermImpl("value2", "en", Set.of(EntityType.CONCEPT));
    SearchTerm searchTerm3 = new SearchTermImpl("value3", "en", Set.of(EntityType.AGENT));

    ENRICHMENT_RESULT.put(searchTerm1, List.of(place1));
    ENRICHMENT_RESULT.put(searchTerm2, null);
    ENRICHMENT_RESULT.put(searchTerm3, List.of(place2));
  }

  @Test
  void testDereferencerHappyFlow() throws MalformedURLException {
    // Create mocks of the dependencies
    final ClientEntityResolver clientEntityResolver = mock(ClientEntityResolver.class);
    doReturn(ENRICHMENT_RESULT).when(clientEntityResolver).resolveByText(anySet());
    final DereferenceClient dereferenceClient = mock(DereferenceClient.class);
    doReturn(DEREFERENCE_RESULT.get(0),
        DEREFERENCE_RESULT.subList(1, DEREFERENCE_RESULT.size()).toArray()).when(dereferenceClient)
                                                                           .dereference(any());
    final EntityMergeEngine entityMergeEngine = mock(EntityMergeEngine.class);

    final Dereferencer dereferencer = spy(
        new DereferencerImpl(entityMergeEngine, clientEntityResolver, dereferenceClient));
    doReturn(DEREFERENCE_EXTRACT_RESULT_VALID).when(dereferencer).extractReferencesForDereferencing(any());

    wireMockServer.stubFor(get("/about")
        .withHost(equalTo("valid-example.host"))
        .willReturn(ok("about")));
    wireMockServer.stubFor(get("/concept")
        .withHost(equalTo("valid-example.host"))
        .willReturn(ok("concept")));
    wireMockServer.stubFor(get("/place")
        .withHost(equalTo("valid-example.host"))
        .willReturn(ok("place")));

    final RDF inputRdf = new RDF();
    Set<Report> reports = dereferencer.dereference(inputRdf);

    verifyDereferenceHappyFlow(dereferenceClient, dereferencer, inputRdf, reports);
    verifyMergeHappyFlow(entityMergeEngine);
  }

  @Test
  void testDereferencerNullFlow() throws MalformedURLException {
    // Create mocks of the dependencies
    final ClientEntityResolver entityResolver = mock(ClientEntityResolver.class);
    final DereferenceClient dereferenceClient = mock(DereferenceClient.class);

    final EntityMergeEngine entityMergeEngine = mock(EntityMergeEngine.class);

    // Create dereferencer.
    final Dereferencer dereferencer = spy(
        new DereferencerImpl(entityMergeEngine, entityResolver, dereferenceClient));
    doReturn(Collections.emptyMap()).when(dereferencer).extractReferencesForDereferencing(any());

    final RDF inputRdf = new RDF();
    dereferencer.dereference(inputRdf);

    verifyDereferenceNullFlow(dereferenceClient, dereferencer, inputRdf);
    verifyMergeNullFlow(entityMergeEngine);
  }

  @Test
  void testDereferenceInvalidUrl() throws MalformedURLException {
    // Create mocks of the dependencies
    final ClientEntityResolver clientEntityResolver = mock(ClientEntityResolver.class);
    doReturn(ENRICHMENT_RESULT).when(clientEntityResolver).resolveByText(anySet());
    final DereferenceClient dereferenceClient = mock(DereferenceClient.class);
    doReturn(DEREFERENCE_RESULT.get(0),
        DEREFERENCE_RESULT.subList(1, DEREFERENCE_RESULT.size()).toArray()).when(dereferenceClient)
                                                                           .dereference(any());
    final EntityMergeEngine entityMergeEngine = mock(EntityMergeEngine.class);

    final Dereferencer dereferencer = spy(
        new DereferencerImpl(entityMergeEngine, clientEntityResolver, dereferenceClient));
    doReturn(DEREFERENCE_EXTRACT_RESULT_INVALID).when(dereferencer).extractReferencesForDereferencing(any());
    wireMockServer.stubFor(get("/about")
        .withHost(equalTo("invalid-example.host"))
        .willReturn(serverError()));
    wireMockServer.stubFor(get("/concept")
        .withHost(equalTo("invalid-example.host"))
        .willReturn(temporaryRedirect("")));
    wireMockServer.stubFor(get("/place")
        .withHost(equalTo("invalid-example.host"))
        .willReturn(badRequest()));

    final RDF inputRdf = new RDF();
    Set<Report> reports = dereferencer.dereference(inputRdf);

    verifyDereferenceInvalidUrlFlow(dereferenceClient, dereferencer, inputRdf, reports);
    verifyMergeExceptionFlow(entityMergeEngine);
  }

  @Test
  void testDereferenceHttpException() throws MalformedURLException {
    // Create mocks of the dependencies
    final ClientEntityResolver clientEntityResolver = mock(ClientEntityResolver.class);
    doReturn(ENRICHMENT_RESULT).when(clientEntityResolver).resolveByText(anySet());
    final DereferenceClient dereferenceClient = mock(DereferenceClient.class);
    when(dereferenceClient.dereference(any()))
        .thenThrow(HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "", null, null, null));
    final EntityMergeEngine entityMergeEngine = mock(EntityMergeEngine.class);

    final Dereferencer dereferencer = spy(
        new DereferencerImpl(entityMergeEngine, clientEntityResolver, dereferenceClient));
    doReturn(DEREFERENCE_EXTRACT_RESULT_VALID).when(dereferencer).extractReferencesForDereferencing(any());

    final RDF inputRdf = new RDF();
    Set<Report> reports = dereferencer.dereference(inputRdf);

    verifyDereferenceExceptionFlow(dereferenceClient, dereferencer, inputRdf, reports);
    verifyMergeExceptionFlow(entityMergeEngine);
  }

  private void verifyDereferenceHappyFlow(DereferenceClient dereferenceClient,
      Dereferencer dereferencer, RDF inputRdf, Set<Report> reports) {

    verifyDerefencer(dereferencer, inputRdf);

    // Actually dereferencing.
    verify(dereferenceClient, times(DEREFERENCE_EXTRACT_RESULT_VALID.size())).dereference(anyString());

    assertEquals(1, reports.size());
    for (Report report : reports) {
      assertTrue(report.getMessage().contains("Dereferencing or Coreferencing: the europeana entity does not exist"));
      assertEquals(Type.WARN, report.getMessageType());
      assertEquals(Mode.DEREFERENCE, report.getMode());
      assertEquals("http://valid-example.host/concept", report.getValue());
      assertEquals("", report.getStackTrace());
    }
    Set<String> setOfValues = DEREFERENCE_EXTRACT_RESULT_VALID.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
    for (String dereferenceUrl : setOfValues) {
      verify(dereferenceClient, times(1)).dereference(dereferenceUrl);
    }
  }

  private void verifyDereferenceInvalidUrlFlow(DereferenceClient dereferenceClient,
      Dereferencer dereferencer, RDF inputRdf, Set<Report> reports) {

    // Extracting values for dereferencing
    verifyDerefencer(dereferencer, inputRdf);

    // Checking the report.
    assertEquals(3, reports.size());
    Set<String> setOfValues = DEREFERENCE_EXTRACT_RESULT_VALID.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
    for (String dereferenceUrl : setOfValues) {
      verify(dereferenceClient, times(0)).dereference(dereferenceUrl);
    }
  }

  private void verifyDereferenceExceptionFlow(DereferenceClient dereferenceClient,
      Dereferencer dereferencer, RDF inputRdf, Set<Report> reports) {

    // Extracting values for dereferencing
    verifyDerefencer(dereferencer, inputRdf);

    // Actually dereferencing.
    verify(dereferenceClient, times(DEREFERENCE_EXTRACT_RESULT_VALID.size())).dereference(anyString());

    // Checking the report.
    assertEquals(3, reports.size());
    for (Report report : reports) {
      assertTrue(report.getMessage().contains("400 Bad Request"));
      assertEquals(Type.WARN, report.getMessageType());
    }
  }

  // Verify merge calls
  private void verifyMergeHappyFlow(EntityMergeEngine entityMergeEngine) throws MalformedURLException {
    ArgumentCaptor<List<DereferencedEntities>> argumentCaptor = ArgumentCaptor.forClass(List.class);
    List<DereferencedEntities> expectedList = prepareExpectedList();
    verify(entityMergeEngine, times(1))
            .mergeReferenceEntitiesFromDereferencedEntities(any(), argumentCaptor.capture());
    assertEquals(expectedList.size(), argumentCaptor.getValue().size());
    for(int i = 0; i < expectedList.size(); i++){
      DereferencedEntities expectedElement = expectedList.get(i);
      DereferencedEntities capturedElement = argumentCaptor.getValue().get(i);
      assertEquals(expectedElement.getClassType(), capturedElement.getClassType());
      assertEquals(expectedElement.getReportMessages().size(), capturedElement.getReportMessages().size());
      assertTrue(CollectionUtils.isEqualCollection(expectedElement.getReportMessages(), capturedElement.getReportMessages()));
      assertTrue(CollectionUtils.isEqualCollection(expectedElement.getReferenceTermListMap().keySet(),
              capturedElement.getReferenceTermListMap().keySet()));
      assertTrue(CollectionUtils.isEqualCollection(expectedElement.getReferenceTermListMap().values(),
              capturedElement.getReferenceTermListMap().values()));
    }
  }

  private void verifyDereferenceNullFlow(DereferenceClient dereferenceClient,
      Dereferencer dereferencer, RDF inputRdf) {

    // Extracting values for dereferencing
    verifyDerefencer(dereferencer, inputRdf);

    // Actually dereferencing: don't use the null values.
    final Set<String> dereferenceUrls = Arrays.stream(new String[0]).filter(Objects::nonNull)
                                              .collect(Collectors.toSet());
    verify(dereferenceClient, times(dereferenceUrls.size())).dereference(anyString());
    for (String dereferenceUrl : dereferenceUrls) {
      verify(dereferenceClient, times(1)).dereference(dereferenceUrl);
    }
  }

  private void verifyDerefencer(Dereferencer dereferencer, RDF inputRdf) {
    // Extracting values for dereferencing
    verify(dereferencer, times(1)).extractReferencesForDereferencing(any());
    verify(dereferencer, times(1)).extractReferencesForDereferencing(inputRdf);
  }

  private void verifyMergeExceptionFlow(EntityMergeEngine entityMergeEngine) throws MalformedURLException {
    ArgumentCaptor<List<DereferencedEntities>> argumentCaptor = ArgumentCaptor.forClass((Class)List.class);
    List<DereferencedEntities> expectedList = prepareExpectedListMergeNull();
    verify(entityMergeEngine, times(1))
        .mergeReferenceEntitiesFromDereferencedEntities(any(), argumentCaptor.capture());
    assertEquals(expectedList.size(), argumentCaptor.getValue().size());
    for(int i = 0; i < expectedList.size(); i++){
      DereferencedEntities expectedElement = expectedList.get(i);
      DereferencedEntities capturedElement = argumentCaptor.getValue().get(i);
      assertEquals(expectedElement.getClassType(), capturedElement.getClassType());
      assertEquals(expectedElement.getReportMessages().size(), capturedElement.getReportMessages().size());

    }

  }

  private void verifyMergeNullFlow(EntityMergeEngine entityMergeEngine){
    ArgumentCaptor<List<DereferencedEntities>> argumentCaptor = ArgumentCaptor.forClass((Class)List.class);
    verify(entityMergeEngine, times(1))
            .mergeReferenceEntitiesFromDereferencedEntities(any(), argumentCaptor.capture());
    assertEquals(1, argumentCaptor.getValue().size());
    assertNull(argumentCaptor.getValue().get(0).getClassType());
    assertTrue(argumentCaptor.getValue().get(0).getReferenceTermListMap().isEmpty());
    assertTrue(argumentCaptor.getValue().get(0).getReportMessages().isEmpty());
  }

  private List<DereferencedEntities> prepareExpectedList() throws MalformedURLException {
    ReferenceTermImpl expectedReferenceTerm1 = new ReferenceTermImpl(new URL("http://valid-example.host/concept"));
    Set<Report> expectedReports1 = Set.of(Report.buildDereferenceWarn().withStatus(HttpStatus.OK)
            .withValue("http://valid-example.host/concept")
            .withMessage("Dereferencing or Coreferencing: the europeana entity does not exist."));
    DereferencedEntities expectedDereferencedEntities1 = new DereferencedEntities(Map.of(expectedReferenceTerm1, new ArrayList<>()),
            expectedReports1, Concept.class);

    ReferenceTermImpl expectedReferenceTerm2 = new ReferenceTermImpl(new URL("http://valid-example.host/place"));
    List<EnrichmentBase> expectedEnrichmentBaseList2 = new ArrayList<>();
    expectedEnrichmentBaseList2.add(DEREFERENCE_RESULT.get(2).getEnrichmentBaseResultWrapperList().get(0).getEnrichmentBaseList().get(0));
    expectedEnrichmentBaseList2.add(DEREFERENCE_RESULT.get(2).getEnrichmentBaseResultWrapperList().get(0).getEnrichmentBaseList().get(1));
    expectedEnrichmentBaseList2.add(null);
    DereferencedEntities expectedDereferencedEntities2 = new DereferencedEntities(Map.of(expectedReferenceTerm2, expectedEnrichmentBaseList2),
            Collections.emptySet(), PlaceType.class);

    ReferenceTermImpl expectedReferenceTerm3 = new ReferenceTermImpl(new URL("http://valid-example.host/about"));
    List<EnrichmentBase> expectedEnrichmentBaseList3 = new ArrayList<>();
    expectedEnrichmentBaseList3.add(DEREFERENCE_RESULT.get(0).getEnrichmentBaseResultWrapperList().get(0).getEnrichmentBaseList().get(0));
    expectedEnrichmentBaseList3.add(null);
    expectedEnrichmentBaseList3.add(DEREFERENCE_RESULT.get(0).getEnrichmentBaseResultWrapperList().get(0).getEnrichmentBaseList().get(2));
    DereferencedEntities expectedDereferencedEntities3 = new DereferencedEntities(Map.of(expectedReferenceTerm3, expectedEnrichmentBaseList3),
            Collections.emptySet(), AboutType.class);

    return List.of(expectedDereferencedEntities3, expectedDereferencedEntities1, expectedDereferencedEntities2);
  }

  private List<DereferencedEntities> prepareExpectedListMergeNull() throws MalformedURLException {
    Report expectedReportConcept = Report.buildDereferenceWarn()
            .withStatus(HttpStatus.BAD_REQUEST)
            .withValue("http://valid-example.host/concept")
            .withMessage("HttpClientErrorException.BadRequest: 400 Bad Request");
    Report expectedReportPlace = Report.buildDereferenceWarn()
            .withStatus(HttpStatus.BAD_REQUEST)
            .withValue("http://valid-example.host/place")
            .withMessage("HttpClientErrorException.BadRequest: 400 Bad Request");
    Report expectedReportAbout = Report.buildDereferenceWarn()
            .withStatus(HttpStatus.BAD_REQUEST)
            .withValue("http://valid-example.host/about")
            .withMessage("HttpClientErrorException.BadRequest: 400 Bad Request");

    ReferenceTermImpl referenceTerm1 = new ReferenceTermImpl(new URL("http://valid-example.host/about"));
    ReferenceTermImpl referenceTerm2 = new ReferenceTermImpl(new URL("http://valid-example.host/concept"));
    ReferenceTermImpl referenceTerm3 = new ReferenceTermImpl(new URL("http://valid-example.host/place"));

    DereferencedEntities dereferencedEntities1 = new DereferencedEntities(Map.of(referenceTerm1, Collections.emptyList()),
            Set.of(expectedReportAbout), AboutType.class);
    DereferencedEntities dereferencedEntities2 = new DereferencedEntities(Map.of(referenceTerm2, Collections.emptyList()),
            Set.of(expectedReportConcept), Concept.class);
    DereferencedEntities dereferencedEntities3 = new DereferencedEntities(Map.of(referenceTerm3, Collections.emptyList()),
            Set.of(expectedReportPlace), PlaceType.class);

    return List.of(dereferencedEntities1, dereferencedEntities2, dereferencedEntities3);
  }
}
