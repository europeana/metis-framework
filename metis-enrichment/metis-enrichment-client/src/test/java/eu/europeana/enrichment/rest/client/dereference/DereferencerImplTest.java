package eu.europeana.enrichment.rest.client.dereference;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
import eu.europeana.enrichment.api.internal.EntityResolver;
import eu.europeana.enrichment.api.internal.ReferenceTerm;
import eu.europeana.enrichment.api.internal.ReferenceTermImpl;
import eu.europeana.enrichment.api.internal.SearchTerm;
import eu.europeana.enrichment.api.internal.SearchTermImpl;
import eu.europeana.enrichment.rest.client.EnrichmentWorker.Mode;
import eu.europeana.enrichment.rest.client.report.Report;
import eu.europeana.enrichment.rest.client.report.Type;
import eu.europeana.enrichment.utils.EntityMergeEngine;
import eu.europeana.enrichment.utils.EntityType;
import eu.europeana.metis.schema.jibx.RDF;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ServiceUnavailableException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.stream.Stream;
import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

/**
 * Unit tests for {@link DereferencerImpl} class
 */
class DereferencerImplTest {

    private static final Set<String> DEREFERENCE_EXTRACT_RESULT_INVALID = Set.of(
            "htt://invalid-example.host/about",
            "httpx://invalid-example.host/concept",
            "http://invalid-example host/place?val=ab");

    private static final Set<String> DEREFERENCE_EXTRACT_RESULT_VALID = Set.of(
            "http://valid-example.host/about",
            "http://data.europeana.eu.host/concept",
            "http://valid-example.host/place");

    private static final Set<String> DEREFERENCE_EXTRACT_SINGLE_ABOUT_RESULT_VALID = Set.of(
            "http://valid-example.host/place");
    private static final Set<String> DEREFERENCE_EXTRACT_SINGLE_AGGREGATION_RESULT_VALID = Set.of(
            "http://valid-example.host/place");

    private static final List<EnrichmentResultList> DEREFERENCE_RESULT;
    private static final Map<SearchTerm, List<EnrichmentBase>> ENRICHMENT_RESULT = new HashMap<>();

    private static final Map<ReferenceTerm, EnrichmentBase> ENRICHMENT_RESULT_BY_ID = new HashMap<>();
    private static WireMockServer wireMockServer;

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

        try {
            URL url = URI.create("http://valid-example.host/place").toURL();
            ReferenceTerm referenceTerm = new ReferenceTermImpl(url, Set.of(EntityType.PLACE));
            ENRICHMENT_RESULT_BY_ID.put(referenceTerm, place1);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }


    }

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

    private static Stream<Arguments> providedExceptions() {
        return Stream.of(
                Arguments.of(HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "", null, null, null), Type.WARN, "400 Bad Request"),
                Arguments.of(HttpServerErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Error with service", null, null, null), Type.ERROR,
                        "Exception occurred while trying to perform dereferencing."),
                Arguments.of(new RuntimeException(new UnknownHostException("")), Type.ERROR,
                        "Exception occurred while trying to perform dereferencing."),
                Arguments.of(new RuntimeException(new SocketTimeoutException("Time out exceeded")), Type.ERROR,
                        "Exception occurred while trying to perform dereferencing."),
                Arguments.of(new RuntimeException(new ServiceUnavailableException("No service")), Type.ERROR,
                        "Exception occurred while trying to perform dereferencing."),
                Arguments.of(new NotFoundException(), Type.ERROR,
                        "Exception occurred while trying to perform dereferencing."),
                Arguments.of(new RuntimeException(new IllegalArgumentException("argument invalid")), Type.ERROR,
                        "Exception occurred while trying to perform dereferencing.")
        );
    }

    @Test
    void testDereferencerHappyFlow() throws MalformedURLException, URISyntaxException {
        // Create mocks of the dependencies
        final ClientEntityResolver clientEntityResolver = mock(ClientEntityResolver.class);
        doReturn(ENRICHMENT_RESULT).when(clientEntityResolver).resolveByText(anySet());
        final DereferenceClient dereferenceClient = mock(DereferenceClient.class);
        doReturn(DEREFERENCE_RESULT.getFirst(),
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
                .withHost(equalTo("data.europeana.eu.host"))
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
    void testDereferencerNullFlow() {
        // Create mocks of the dependencies
        final ClientEntityResolver entityResolver = mock(ClientEntityResolver.class);
        final DereferenceClient dereferenceClient = mock(DereferenceClient.class);

        final EntityMergeEngine entityMergeEngine = mock(EntityMergeEngine.class);

        // Create dereferencer.
        final Dereferencer dereferencer = spy(
                new DereferencerImpl(entityMergeEngine, entityResolver, dereferenceClient));
        doReturn(Collections.emptySet()).when(dereferencer).extractReferencesForDereferencing(any());

        final RDF inputRdf = new RDF();
        dereferencer.dereference(inputRdf);

        verifyDereferenceNullFlow(dereferenceClient, dereferencer, inputRdf);
        verifyMergeNullFlow(entityMergeEngine);
    }

    @Test
    void testDereferenceInvalidUrl() throws MalformedURLException, URISyntaxException {
        // Create mocks of the dependencies
        final ClientEntityResolver clientEntityResolver = mock(ClientEntityResolver.class);
        doReturn(ENRICHMENT_RESULT).when(clientEntityResolver).resolveByText(anySet());
        final DereferenceClient dereferenceClient = mock(DereferenceClient.class);
        doReturn(DEREFERENCE_RESULT.getFirst(),
                DEREFERENCE_RESULT.subList(1, DEREFERENCE_RESULT.size()).toArray()).when(dereferenceClient)
                .dereference(any());
        final EntityMergeEngine entityMergeEngine = mock(EntityMergeEngine.class);

        final Dereferencer dereferencer = spy(
                new DereferencerImpl(entityMergeEngine, clientEntityResolver, dereferenceClient));
        doReturn(DEREFERENCE_EXTRACT_RESULT_INVALID).when(dereferencer).extractReferencesForDereferencing(any());

        final RDF inputRdf = new RDF();
        Set<Report> reports = dereferencer.dereference(inputRdf);

        verifyDereferenceInvalidUrlFlow(dereferenceClient, dereferencer, inputRdf, reports);
        verifyMergeExceptionFlow(entityMergeEngine);
    }

    @Disabled("TODO: MET-4255 Improve execution time, think feasibility of @Value(\"${max-retries}\")")
    @ParameterizedTest
    @MethodSource("providedExceptions")
    void testDereferenceNetworkException(Exception ex, Type expectedMessageType, String expectedMessage)
        throws MalformedURLException, URISyntaxException {
        // Create mocks of the dependencies
        final ClientEntityResolver clientEntityResolver = mock(ClientEntityResolver.class);
        doReturn(ENRICHMENT_RESULT).when(clientEntityResolver).resolveByText(anySet());
        final DereferenceClient dereferenceClient = mock(DereferenceClient.class);
        doThrow(ex).when(dereferenceClient).dereference(any());
        final EntityMergeEngine entityMergeEngine = mock(EntityMergeEngine.class);

        final Dereferencer dereferencer = spy(
                new DereferencerImpl(entityMergeEngine, clientEntityResolver, dereferenceClient));
        doReturn(DEREFERENCE_EXTRACT_RESULT_VALID).when(dereferencer).extractReferencesForDereferencing(any());

        final RDF inputRdf = new RDF();
        Set<Report> reports = dereferencer.dereference(inputRdf);

        verifyDereferenceExceptionFlow(dereferenceClient, dereferencer, inputRdf, reports, expectedMessageType, expectedMessage);
        verifyMergeExceptionFlow(entityMergeEngine);
    }

    @Test
    void testDereferenceCancellationException() {
        String cancellationExceptionMessage = "Cancellation exception occurred while trying to perform dereferencing.";
        CancellationException cancellationException = new CancellationException(cancellationExceptionMessage);
        // Create mocks of the dependencies
        final ClientEntityResolver clientEntityResolver = mock(ClientEntityResolver.class);
        doReturn(ENRICHMENT_RESULT).when(clientEntityResolver).resolveByText(anySet());
        final DereferenceClient dereferenceClient = mock(DereferenceClient.class);
        final EntityMergeEngine entityMergeEngine = mock(EntityMergeEngine.class);
        final RDF inputRdf = new RDF();
        final Dereferencer dereferencer = spy(
                new DereferencerImpl(entityMergeEngine, clientEntityResolver, dereferenceClient));

        verifyCaseWithInternalEntities(cancellationException, clientEntityResolver, inputRdf,
                dereferencer);

        verifyCaseWithExternalEntities(cancellationException, clientEntityResolver, inputRdf,
                dereferencer, dereferenceClient);

        verifyCaseWithEntitiesWithUris(cancellationException, clientEntityResolver, inputRdf,
                dereferencer);
    }

    @Test
    void testDereferenceWithNoEntityResolver() {
        final DereferenceClient dereferenceClient = mock(DereferenceClient.class);
        final EntityMergeEngine entityMergeEngine = mock(EntityMergeEngine.class);
        final Dereferencer dereferencer = spy(
            new DereferencerImpl(entityMergeEngine, (EntityResolver) null, dereferenceClient));

        DereferencedEntities dereferencedEntities = dereferencer.dereferenceEuropeanaEntities(Set.of(), HashSet.newHashSet(0));

        assertEquals(0, dereferencedEntities.getReferenceTermListMap().size());
        assertEquals(0, dereferencedEntities.getReportMessages().size());
    }

    @Test
    void testDereferenceWithEntityResolverException() {
        final ClientEntityResolver clientEntityResolver = mock(ClientEntityResolver.class);
        final DereferenceClient dereferenceClient = mock(DereferenceClient.class);
        final EntityMergeEngine entityMergeEngine = mock(EntityMergeEngine.class);
        final Dereferencer dereferencer = spy(
            new DereferencerImpl(entityMergeEngine, clientEntityResolver, dereferenceClient));
        doThrow(new RuntimeException("Exception occurred while trying to resolve entities")).when(clientEntityResolver)
                                                                                            .resolveById(any());
        HashSet<Report> reports = HashSet.newHashSet(0);

        DereferencedEntities dereferencedEntities = dereferencer.dereferenceEuropeanaEntities(Set.of(), reports);

        assertEquals(1, reports.size());
        assertEquals("DereferenceException: Exception occurred while trying to perform dereferencing.",
            reports.iterator().next().getMessage());
        assertEquals(0, dereferencedEntities.getReferenceTermListMap().size());
    }

    @Test
    void testDereferenceExternalEntitiesExceptions () {
        final ClientEntityResolver clientEntityResolver = mock(ClientEntityResolver.class);
        final DereferenceClient dereferenceClient = mock(DereferenceClient.class);
        final EntityMergeEngine entityMergeEngine = mock(EntityMergeEngine.class);
        final Dereferencer dereferencer = spy(
            new DereferencerImpl(entityMergeEngine, clientEntityResolver, dereferenceClient));
        doReturn(ENRICHMENT_RESULT_BY_ID)
            .when(clientEntityResolver)
            .resolveById(any());

            when(dereferenceClient
            .dereference(any())).thenThrow(new RuntimeException("External Entity"))
                .thenThrow(HttpClientErrorException.create(HttpStatus.BAD_REQUEST,"External Entity",null, null, null));
        DereferencedEntities dereferencedEntities = dereferencer.dereferenceEntities(Set.of("http://localhost","http://demo"));

        assertEquals(2, dereferencedEntities.getReportMessages().size());
        assertTrue( dereferencedEntities.getReportMessages()
                                        .stream()
                                        .anyMatch(report -> report.getMessage().contains("External Entity")) );
        assertTrue( dereferencedEntities.getReportMessages()
                                        .stream()
                                        .filter( r-> (r.getStatus() != null))
                                        .anyMatch(report -> report.getStatus().equals(HttpStatus.BAD_REQUEST)));
    }

    private static void verifyCaseWithInternalEntities(CancellationException cancellationException,
                                                       ClientEntityResolver clientEntityResolver, RDF inputRdf,
                                                       Dereferencer dereferencer) {
        // Case when cancellation is thrown during dereference own entities
        doReturn(DEREFERENCE_EXTRACT_SINGLE_ABOUT_RESULT_VALID)
                .when(dereferencer)
                .extractReferencesForDereferencing(any());
        doThrow(cancellationException)
                .when(clientEntityResolver)
                .resolveById(any());
        assertThrows(cancellationException.getClass(), () -> dereferencer.dereference(inputRdf));
    }

    private static void verifyCaseWithExternalEntities(CancellationException cancellationException,
                                                       ClientEntityResolver clientEntityResolver, RDF inputRdf,
                                                       Dereferencer dereferencer, DereferenceClient dereferenceClient) {
        // Case when cancellation is thrown during dereference external entities
        doReturn(DEREFERENCE_EXTRACT_SINGLE_ABOUT_RESULT_VALID)
                .when(dereferencer)
                .extractReferencesForDereferencing(any());
        doThrow(cancellationException)
                .when(dereferenceClient)
                .dereference(any());
        doReturn(ENRICHMENT_RESULT_BY_ID)
                .when(clientEntityResolver)
                .resolveById(any());
        assertThrows(cancellationException.getClass(), () -> dereferencer.dereference(inputRdf));
    }

    private static void verifyCaseWithEntitiesWithUris(CancellationException cancellationException,
                                                       ClientEntityResolver clientEntityResolver,
                                                       RDF inputRdf, Dereferencer dereferencer) {
        // Case when cancellation is thrown during dereference entities with Uris
        doReturn(DEREFERENCE_EXTRACT_SINGLE_AGGREGATION_RESULT_VALID)
                .when(dereferencer)
                .extractReferencesForDereferencing(any());
        doReturn(ENRICHMENT_RESULT_BY_ID)
                .when(clientEntityResolver)
                .resolveById(any());
        doThrow(CancellationException.class)
                .when(clientEntityResolver)
                .resolveByUri(any());
        assertThrows(cancellationException.getClass(), () -> dereferencer.dereference(inputRdf));
    }

    private void verifyDereferenceHappyFlow(DereferenceClient dereferenceClient,
                                            Dereferencer dereferencer, RDF inputRdf, Set<Report> reports) {

        verifyDerefencer(dereferencer, inputRdf);

        // Actually dereferencing.
        verify(dereferenceClient, times(DEREFERENCE_EXTRACT_RESULT_VALID.size())).dereference(anyString());

        assertEquals(2, reports.size());

        Set<Report> expectedReports = Set.of(Report.buildDereferenceError()
                        .withValue("http://data.europeana.eu.host/concept")
                        .withMessage("Dereference or Coreferencing failed."),
                Report.buildDereferenceWarn()
                        .withStatus(HttpStatus.OK)
                        .withValue("http://data.europeana.eu.host/concept")
                        .withMessage("Dereferencing or Coreferencing: the europeana entity does not exist."));

        assertTrue(CollectionUtils.isEqualCollection(expectedReports, reports));

        for (String dereferenceUrl : DEREFERENCE_EXTRACT_RESULT_VALID) {
            verify(dereferenceClient, times(1)).dereference(dereferenceUrl);
        }
    }

    private void verifyDereferenceInvalidUrlFlow(DereferenceClient dereferenceClient,
                                                 Dereferencer dereferencer, RDF inputRdf, Set<Report> reports) {

        // Extracting values for dereferencing
        verifyDerefencer(dereferencer, inputRdf);

        // Checking the report.
        assertEquals(3, reports.size());
        for (Report report : reports) {
            assertEquals(Type.IGNORE, report.getMessageType());
            assertEquals(Mode.DEREFERENCE, report.getMode());
        }
        for (String dereferenceUrl : DEREFERENCE_EXTRACT_RESULT_VALID) {
            verify(dereferenceClient, times(0)).dereference(dereferenceUrl);
        }
    }

    private void verifyDereferenceExceptionFlow(DereferenceClient dereferenceClient,
                                                Dereferencer dereferencer, RDF inputRdf,
                                                Set<Report> reports, Type expectedType,
                                                String expectedMessage) {

        // Extracting values for dereferencing
        verifyDerefencer(dereferencer, inputRdf);

        // Actually dereferencing.
        verify(dereferenceClient, atLeast(DEREFERENCE_EXTRACT_RESULT_VALID.size())).dereference(anyString());

        // Checking the report.
        assertEquals(3, reports.size());
        for (Report report : reports) {
            assertTrue(report.getMessage().contains(expectedMessage));
            assertEquals(expectedType, report.getMessageType());
        }
    }

    // Verify merge calls
    private void verifyMergeHappyFlow(EntityMergeEngine entityMergeEngine) throws MalformedURLException, URISyntaxException {
        ArgumentCaptor<DereferencedEntities> argumentCaptor = ArgumentCaptor.forClass(DereferencedEntities.class);
        DereferencedEntities expectedList = prepareExpectedList();
        verify(entityMergeEngine, times(1))
                .convertAndAddAllEntities(any(), argumentCaptor.capture());
        final DereferencedEntities result = argumentCaptor.getValue();
        assertEquals(expectedList.getReportMessages(), result.getReportMessages());
        assertEquals(expectedList.getReferenceTermListMap(), result.getReferenceTermListMap());
    }

    private void verifyDereferenceNullFlow(DereferenceClient dereferenceClient,
                                           Dereferencer dereferencer, RDF inputRdf) {

        // Extracting values for dereferencing
        verifyDerefencer(dereferencer, inputRdf);

        // Actually dereferencing: don't use the null values.
        verify(dereferenceClient, never()).dereference(anyString());
    }

    private void verifyDerefencer(Dereferencer dereferencer, RDF inputRdf) {
        // Extracting values for dereferencing
        verify(dereferencer, times(1)).extractReferencesForDereferencing(any());
        verify(dereferencer, times(1)).extractReferencesForDereferencing(inputRdf);
    }

    private void verifyMergeExceptionFlow(EntityMergeEngine entityMergeEngine) throws MalformedURLException, URISyntaxException {
        ArgumentCaptor<DereferencedEntities> argumentCaptor = ArgumentCaptor.forClass(DereferencedEntities.class);
        DereferencedEntities expectedList = prepareExpectedListMergeNull();
        verify(entityMergeEngine, times(1))
                .convertAndAddAllEntities(any(), argumentCaptor.capture());
        final DereferencedEntities result = argumentCaptor.getValue();
        assertEquals(expectedList.getReferenceTermListMap().size(), result.getReportMessages().size());
        assertTrue(result.getReferenceTermListMap().isEmpty());
    }

    private void verifyMergeNullFlow(EntityMergeEngine entityMergeEngine) {
        ArgumentCaptor<DereferencedEntities> argumentCaptor = ArgumentCaptor.forClass(DereferencedEntities.class);
        verify(entityMergeEngine, times(1))
                .convertAndAddAllEntities(any(), argumentCaptor.capture());
        assertTrue(argumentCaptor.getValue().getReferenceTermListMap().isEmpty());
        assertTrue(argumentCaptor.getValue().getReportMessages().isEmpty());
    }

    private DereferencedEntities prepareExpectedList() throws MalformedURLException, URISyntaxException {
        ReferenceTermImpl expectedReferenceTerm1 = new ReferenceTermImpl(new URI("http://data.europeana.eu.host/concept").toURL());
        Set<Report> expectedReports1 = Set.of(Report.buildDereferenceError()
                        .withValue("http://data.europeana.eu.host/concept")
                        .withMessage("Dereference or Coreferencing failed."),
                Report.buildDereferenceWarn()
                        .withStatus(HttpStatus.OK)
                        .withValue("http://data.europeana.eu.host/concept")
                        .withMessage("Dereferencing or Coreferencing: the europeana entity does not exist."));
        DereferencedEntities expectedDereferencedEntities1 = new DereferencedEntities(
                Map.of(expectedReferenceTerm1, new ArrayList<>()),
                expectedReports1);

        ReferenceTermImpl expectedReferenceTerm2 = new ReferenceTermImpl(new URI("http://valid-example.host/place").toURL());
        List<EnrichmentBase> expectedEnrichmentBaseList2 = new ArrayList<>();
        expectedEnrichmentBaseList2.add(
                DEREFERENCE_RESULT.get(2).getEnrichmentBaseResultWrapperList().getFirst().getEnrichmentBaseList().getFirst());
        expectedEnrichmentBaseList2.add(
                DEREFERENCE_RESULT.get(2).getEnrichmentBaseResultWrapperList().getFirst().getEnrichmentBaseList().get(1));
        expectedEnrichmentBaseList2.add(null);
        DereferencedEntities expectedDereferencedEntities2 = new DereferencedEntities(
                Map.of(expectedReferenceTerm2, expectedEnrichmentBaseList2),
                Collections.emptySet());

        ReferenceTermImpl expectedReferenceTerm3 = new ReferenceTermImpl(new URI("http://valid-example.host/about").toURL());
        List<EnrichmentBase> expectedEnrichmentBaseList3 = new ArrayList<>();
        expectedEnrichmentBaseList3.add(
                DEREFERENCE_RESULT.getFirst().getEnrichmentBaseResultWrapperList().getFirst().getEnrichmentBaseList().get(0));
        expectedEnrichmentBaseList3.add(null);
        expectedEnrichmentBaseList3.add(
                DEREFERENCE_RESULT.getFirst().getEnrichmentBaseResultWrapperList().getFirst().getEnrichmentBaseList().get(2));
        DereferencedEntities expectedDereferencedEntities3 = new DereferencedEntities(
                Map.of(expectedReferenceTerm3, expectedEnrichmentBaseList3),
                Collections.emptySet());

        DereferencedEntities result = DereferencedEntities.emptyInstance();
        result.addAll(expectedDereferencedEntities1);
        result.addAll(expectedDereferencedEntities2);
        result.addAll(expectedDereferencedEntities3);
        return result;
    }

    private DereferencedEntities prepareExpectedListMergeNull() throws MalformedURLException, URISyntaxException {
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

        ReferenceTermImpl referenceTerm1 = new ReferenceTermImpl(new URI("http://valid-example.host/about").toURL());
        ReferenceTermImpl referenceTerm2 = new ReferenceTermImpl(new URI("http://valid-example.host/concept").toURL());
        ReferenceTermImpl referenceTerm3 = new ReferenceTermImpl(new URI("http://valid-example.host/place").toURL());

        return new DereferencedEntities(
            Map.of(referenceTerm1, Collections.emptyList(), referenceTerm2, Collections.emptyList(),
                referenceTerm3, Collections.emptyList()),
            Set.of(expectedReportAbout, expectedReportConcept, expectedReportPlace));
    }
}
