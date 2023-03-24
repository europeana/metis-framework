package eu.europeana.enrichment.rest.client.enrichment;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import eu.europeana.enrichment.api.external.impl.ClientEntityResolver;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.Place;
import eu.europeana.enrichment.api.internal.ProxyFieldType;
import eu.europeana.enrichment.api.internal.RecordParser;
import eu.europeana.enrichment.api.internal.ReferenceTermContext;
import eu.europeana.enrichment.api.internal.SearchTerm;
import eu.europeana.enrichment.api.internal.SearchTermContext;
import eu.europeana.enrichment.rest.client.report.Report;
import eu.europeana.enrichment.utils.EntityMergeEngine;
import eu.europeana.entity.client.exception.TechnicalRuntimeException;
import eu.europeana.metis.schema.jibx.RDF;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

class EnricherImplTest {

  private final ArgumentCaptor<Set<SearchTerm>> enrichmentExtractionCaptor = ArgumentCaptor.forClass(Set.class);

  private final ArgumentCaptor<List<EnrichmentBase>> enrichmentResultCaptor = ArgumentCaptor.forClass(List.class);

  private static final Map<SearchTerm, List<EnrichmentBase>> ENRICHMENT_RESULT;

  private static final Map<ReferenceTermContext, List<EnrichmentBase>> REFERENCE_ENRICHMENT_RESULT;

  private static final Set<SearchTermContext> ENRICHMENT_EXTRACT_RESULT = new HashSet<>();

  static {
    final Place place1 = new Place();
    place1.setAbout("place1");
    final Place place2 = new Place();
    place2.setAbout("place2");

    SearchTerm searchTerm1 = new SearchTermContext("value1", "en",
        Set.of(ProxyFieldType.DC_CREATOR));
    SearchTerm searchTerm2 = new SearchTermContext("value2", null,
        Set.of(ProxyFieldType.DC_SUBJECT));
    SearchTerm searchTerm3 = new SearchTermContext("value3", "pt",
        Set.of(ProxyFieldType.DCTERMS_SPATIAL));

    ENRICHMENT_RESULT = new HashMap<>();
    ENRICHMENT_RESULT.put(searchTerm1, List.of(place1));
    ENRICHMENT_RESULT.put(searchTerm2, Collections.emptyList());
    ENRICHMENT_RESULT.put(searchTerm3, List.of(place2));
    ENRICHMENT_EXTRACT_RESULT
        .add(new SearchTermContext("value1", "en", Set.of(ProxyFieldType.DC_CREATOR)));
    ENRICHMENT_EXTRACT_RESULT
        .add(new SearchTermContext("value2", null, Set.of(ProxyFieldType.DC_SUBJECT)));
    ENRICHMENT_EXTRACT_RESULT
        .add(new SearchTermContext("value3", "pt", Set.of(ProxyFieldType.DCTERMS_SPATIAL)));

    try {
      final URL reference = new URL("http://urlValue");
      REFERENCE_ENRICHMENT_RESULT = new HashMap<>();
      ReferenceTermContext referenceTermContext = new ReferenceTermContext(reference, Set.of(ProxyFieldType.DCTERMS_SPATIAL));
      REFERENCE_ENRICHMENT_RESULT.put(referenceTermContext, List.of(place1));
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void testEnricherHappyFlow() {
    // Create mocks
    final RecordParser recordParser = Mockito.mock(RecordParser.class);
    final ClientEntityResolver entityResolver = Mockito.mock(ClientEntityResolver.class);
    doReturn(ENRICHMENT_RESULT).when(entityResolver).resolveByText(any());
    final EntityMergeEngine entityMergeEngine = Mockito.mock(EntityMergeEngine.class);

    // Create enricher.
    final Enricher enricher = spy(
        new EnricherImpl(recordParser, entityResolver, entityMergeEngine));
    doReturn(ENRICHMENT_EXTRACT_RESULT).when(recordParser).parseSearchTerms(any());
    doReturn(Collections.emptySet()).when(recordParser).parseReferences(any());

    final RDF inputRdf = new RDF();
    Set<Report> reports = enricher.enrichment(inputRdf);

    verifyEnricherHappyFlow(recordParser, entityResolver, inputRdf, reports);
    verifyMergeHappyFlow(entityMergeEngine);
  }

  @Test
  void testEnricherNullFlow() {
    // Create mocks of the dependencies
    final RecordParser recordParser = Mockito.mock(RecordParser.class);
    final EntityMergeEngine entityMergeEngine = Mockito.mock(EntityMergeEngine.class);
    final ClientEntityResolver entityResolver = Mockito.mock(ClientEntityResolver.class);

    //Create enricher
    final Enricher enricher = spy(
        new EnricherImpl(recordParser, entityResolver, entityMergeEngine));
    doReturn(Collections.emptySet()).when(recordParser).parseSearchTerms(any());
    doReturn(Collections.emptySet()).when(recordParser).parseReferences(any());

    final RDF inputRdf = new RDF();
    Set<Report> reports = enricher.enrichment(inputRdf);

    verifyEnricherNullFlow(entityResolver, recordParser, inputRdf, reports);
    verifyMergeNullFlow(entityMergeEngine);
  }

  @Test
  void testEnricherHttpExceptionFlow() {
    // Create mocks
    final RecordParser recordParser = Mockito.mock(RecordParser.class);
    final ClientEntityResolver entityResolver = Mockito.mock(ClientEntityResolver.class);
    doThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR)).when(entityResolver).resolveByText(any());
    final EntityMergeEngine entityMergeEngine = Mockito.mock(EntityMergeEngine.class);

    // Create enricher.
    final Enricher enricher = spy(
        new EnricherImpl(recordParser, entityResolver, entityMergeEngine));
    doReturn(ENRICHMENT_EXTRACT_RESULT).when(recordParser).parseSearchTerms(any());
    doReturn(Collections.emptySet()).when(recordParser).parseReferences(any());

    final RDF inputRdf = new RDF();
    Set<Report> reports = enricher.enrichment(inputRdf);
    verifyEnricherExeptionFlow(recordParser, entityResolver, inputRdf, reports);
  }

  @Test
  void testEnrichReferencesHappyFlow() throws MalformedURLException {
    // Given the mocks
    final RecordParser recordParser = Mockito.mock(RecordParser.class);
    final ClientEntityResolver entityResolver = Mockito.mock(ClientEntityResolver.class);
    final EntityMergeEngine entityMergeEngine = Mockito.mock(EntityMergeEngine.class);
    doReturn(REFERENCE_ENRICHMENT_RESULT).when(entityResolver).resolveByUri(any());

    // When the enricher
    final Enricher enricher = spy(new EnricherImpl(recordParser, entityResolver, entityMergeEngine));
    ReferenceTermContext referenceTermContext = new ReferenceTermContext(new URL("http://urlValue"),
        Set.of(ProxyFieldType.DCTERMS_SPATIAL));
    Pair<Map<ReferenceTermContext, List<EnrichmentBase>>, Set<Report>> enrichReferences = enricher.enrichReferences(
        Set.of(referenceTermContext));

    // Then verify
    assertEquals(REFERENCE_ENRICHMENT_RESULT, enrichReferences.getLeft());
    assertTrue(enrichReferences.getRight().isEmpty());
  }

  @Test
  void testEnrichReferenceWarnFlow() throws MalformedURLException {
    // Given the mocks
    final RecordParser recordParser = Mockito.mock(RecordParser.class);
    final ClientEntityResolver entityResolver = Mockito.mock(ClientEntityResolver.class);
    final EntityMergeEngine entityMergeEngine = Mockito.mock(EntityMergeEngine.class);
    doThrow(new TechnicalRuntimeException("Error", new HttpClientErrorException(HttpStatus.MOVED_PERMANENTLY)),
        new HttpClientErrorException(HttpStatus.BAD_REQUEST))
        .when(entityResolver)
        .resolveByUri(any());

    // When the enricher a 301
    final Enricher enricher = spy(new EnricherImpl(recordParser, entityResolver, entityMergeEngine));

    ReferenceTermContext referenceTermContext1 = new ReferenceTermContext(new URL("http://urlValue1"),
        Set.of(ProxyFieldType.DCTERMS_SPATIAL));

    Pair<Map<ReferenceTermContext, List<EnrichmentBase>>, Set<Report>> enrichReferences = enricher.enrichReferences(
        Set.of(referenceTermContext1));

    // Then verify
    assertNotNull(enrichReferences);
    assertEquals(getExpectedReportMessagesWarning1Flow(), enrichReferences.getRight());

    // When the enricher a 400
    ReferenceTermContext referenceTermContext2 = new ReferenceTermContext(new URL("http://urlValue2"),
        Set.of(ProxyFieldType.DCTERMS_SPATIAL));
    enrichReferences = enricher.enrichReferences(
        Set.of(referenceTermContext2));

    // Then verify
    assertNotNull(enrichReferences);
    assertEquals(getExpectedReportMessagesWarning2Flow(), enrichReferences.getRight());
  }

  @Test
  void testEnrichReferenceExceptionFlow() throws MalformedURLException {
    // Given the mocks
    final RecordParser recordParser = Mockito.mock(RecordParser.class);
    final ClientEntityResolver entityResolver = Mockito.mock(ClientEntityResolver.class);
    final EntityMergeEngine entityMergeEngine = Mockito.mock(EntityMergeEngine.class);
    final Exception nullCause = new RuntimeException();
    nullCause.initCause(null);
    doThrow(new TechnicalRuntimeException("Error", null),
        new TechnicalRuntimeException("Error", new HttpClientErrorException(HttpStatus.TEMPORARY_REDIRECT)),
        new NullPointerException(),
        nullCause)
        .when(entityResolver)
        .resolveByUri(any());

    // When the enricher a null nested exception
    final Enricher enricher = spy(new EnricherImpl(recordParser, entityResolver, entityMergeEngine));

    ReferenceTermContext referenceTermContext1 = new ReferenceTermContext(new URL("http://urlValue1"),
        Set.of(ProxyFieldType.DCTERMS_SPATIAL));

    Pair<Map<ReferenceTermContext, List<EnrichmentBase>>, Set<Report>> enrichReferences = enricher.enrichReferences(
        Set.of(referenceTermContext1));

    // Then verify
    assertNotNull(enrichReferences);
    assertEquals(getExpectedReportMessagesError1Flow(), enrichReferences.getRight());

    // When the enricher a nested 307 exception
    ReferenceTermContext referenceTermContext2 = new ReferenceTermContext(new URL("http://urlValue2"),
        Set.of(ProxyFieldType.DCTERMS_SPATIAL));
    enrichReferences = enricher.enrichReferences(Set.of(referenceTermContext2));

    // Then verify
    assertNotNull(enrichReferences);
    assertEquals(getExpectedReportMessagesWarnError2Flow(), enrichReferences.getRight());

    // When the enricher with a NullPointerException
    ReferenceTermContext referenceTermContext3 = new ReferenceTermContext(new URL("http://urlValue3"),
        Set.of(ProxyFieldType.DCTERMS_SPATIAL));
    enrichReferences = enricher.enrichReferences(Set.of(referenceTermContext3));

    // Then verify
    assertNotNull(enrichReferences);
    assertEquals(getExpectedReportMessagesError3Flow(),enrichReferences.getRight());

    // When the enricher with a Null Cause
    ReferenceTermContext referenceTermContext4 = new ReferenceTermContext(new URL("http://urlValue4"),
        Set.of(ProxyFieldType.DCTERMS_SPATIAL));
    enrichReferences = enricher.enrichReferences(Set.of(referenceTermContext4));

    // Then verify
    assertNotNull(enrichReferences);
    assertEquals(getExpectedReportMessagesError4Flow(),enrichReferences.getRight());
  }

  private void verifyEnricherExeptionFlow(RecordParser recordParser, ClientEntityResolver entityResolver,
      RDF inputRdf, Set<Report> reports) {
    // Extracting values for enrichment
    verify(recordParser, times(1)).parseSearchTerms(any());
    verify(recordParser, times(1)).parseSearchTerms(inputRdf);
    verify(entityResolver, times(0)).resolveById(any());
    assertEquals(getExpectedReportMessagesExceptionFlow(), reports);
  }

  private void verifyEnricherHappyFlow(RecordParser recordParser, ClientEntityResolver remoteEntityResolver,
      RDF inputRdf, Set<Report> reports) {
    // Extracting values for enrichment
    verify(recordParser, times(1)).parseSearchTerms(any());
    verify(recordParser, times(1)).parseSearchTerms(inputRdf);

    // Actually enriching
    verify(remoteEntityResolver, times(1)).resolveByText(enrichmentExtractionCaptor.capture());

    Comparator<SearchTerm> compareValue = Comparator.comparing(SearchTerm::getTextValue);

    List<SearchTerm> expectedValue = new ArrayList<>(ENRICHMENT_EXTRACT_RESULT);
    List<SearchTerm> actualResult = new ArrayList<>(enrichmentExtractionCaptor.getValue());

    expectedValue.sort(compareValue);
    actualResult.sort(compareValue);

    for (int i = 0; i < expectedValue.size(); i++) {
      SearchTerm expected = expectedValue.get(i);
      SearchTerm actual = actualResult.get(i);
      assertEquals(expected.getTextValue(), actual.getTextValue());
      assertEquals(expected.getLanguage(), actual.getLanguage());
      assertArrayEquals(expected.getCandidateTypes().toArray(), actual.getCandidateTypes().toArray());
    }
    assertEquals(getExpectedReportMessagesHappyFlow(), reports);
  }

  // Verify merge calls
  private void verifyMergeHappyFlow(EntityMergeEngine entityMergeEngine) {
    final List<EnrichmentBase> expectedMerges = new ArrayList<>();
    ENRICHMENT_RESULT.forEach((x, y) -> expectedMerges.addAll(y));
    verify(entityMergeEngine, times(ENRICHMENT_RESULT.size()))
        .mergeSearchEntities(any(), enrichmentResultCaptor.capture(), any(SearchTermContext.class));
    // Note that the captor returns a linked list, so we don't want to use indices.
    // But the interface gives a generic type List, so we don't want to depend on the
    // linked list functionality either.
    final List<List<EnrichmentBase>> allValues = enrichmentResultCaptor.getAllValues();
    final List<List<EnrichmentBase>> foundValues = allValues
        .subList(enrichmentResultCaptor.getAllValues().size() - ENRICHMENT_RESULT.size(),
            enrichmentResultCaptor.getAllValues().size());
    for (List<EnrichmentBase> capturedMerge : foundValues) {
      for (EnrichmentBase capturedMergedItem : capturedMerge) {
        assertTrue(expectedMerges.contains(capturedMergedItem));
      }
    }
  }

  private void verifyEnricherNullFlow(ClientEntityResolver remoteEntityResolver,
      RecordParser recordParser, RDF inputRdf, Set<Report> reports) {
    // Extracting values for enrichment
    verify(recordParser, times(1)).parseSearchTerms(any());
    verify(recordParser, times(1)).parseSearchTerms(inputRdf);

    // Actually enriching
    verify(remoteEntityResolver, never()).resolveByText(any());
    assertEquals(getExpectedReportMessagesNullFlow(), reports);
  }

  private void verifyMergeNullFlow(EntityMergeEngine entityMergeEngine) {
    verify(entityMergeEngine, times(0)).mergeReferenceEntities(any(), eq(Collections.emptyList()),
        any(ReferenceTermContext.class));
    verify(entityMergeEngine, times(0)).mergeReferenceEntities(any(), any(), any(ReferenceTermContext.class));
  }

  private HashSet<Report> getExpectedReportMessagesHappyFlow() {
    HashSet<Report> reports = new HashSet<>();
    reports.add(Report
        .buildEnrichmentIgnore()
        .withValue("value2")
        .withMessage("Could not find an entity for the given search term.")
        .build());
    reports.add(Report
        .buildEnrichmentIgnore()
        .withValue("[]")
        .withMessage("Empty search reference.")
        .build());
    return reports;
  }

  private HashSet<Report> getExpectedReportMessagesNullFlow() {
    HashSet<Report> reports = new HashSet<>();
    reports.add(Report
        .buildEnrichmentIgnore()
        .withValue("[]")
        .withMessage("Empty search terms.")
        .build());
    reports.add(Report
        .buildEnrichmentIgnore()
        .withValue("[]")
        .withMessage("Empty search reference.")
        .build());
    return reports;
  }

  private HashSet<Report> getExpectedReportMessagesExceptionFlow() {
    HashSet<Report> reports = new HashSet<>();
    reports.add(Report
        .buildEnrichmentError()
        .withValue("value1,value2,value3")
        .withException(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR))
        .build());
    reports.add(Report
        .buildEnrichmentIgnore()
        .withValue("[]")
        .withMessage("Empty search reference.")
        .build());
    return reports;
  }

  private HashSet<Report> getExpectedReportMessagesWarning1Flow() {
    HashSet<Report> reports = new HashSet<>();
    reports.add(Report
        .buildEnrichmentWarn()
        .withStatus(HttpStatus.MOVED_PERMANENTLY)
        .withValue("http://urlValue1")
        .withException(new HttpClientErrorException(HttpStatus.MOVED_PERMANENTLY))
        .build());
    return reports;
  }

  private HashSet<Report> getExpectedReportMessagesError1Flow() {
    HashSet<Report> reports = new HashSet<>();
    reports.add(Report
        .buildEnrichmentError()
        .withValue("http://urlValue1")
        .withException(new TechnicalRuntimeException("Error", null))
        .build());
    return reports;
  }

  private HashSet<Report> getExpectedReportMessagesWarnError2Flow() {
    HashSet<Report> reports = new HashSet<>();
    reports.add(Report
        .buildEnrichmentWarn()
        .withStatus(HttpStatus.TEMPORARY_REDIRECT)
        .withValue("http://urlValue2")
        .withException(new HttpClientErrorException(HttpStatus.TEMPORARY_REDIRECT))
        .build());
    return reports;
  }

  private HashSet<Report> getExpectedReportMessagesError3Flow() {
    HashSet<Report> reports = new HashSet<>();
    reports.add(Report
        .buildEnrichmentError()
        .withValue("http://urlValue3")
        .withException(new NullPointerException())
        .build());
    return reports;
  }

  private HashSet<Report> getExpectedReportMessagesError4Flow() {
    HashSet<Report> reports = new HashSet<>();
    Exception nullCause = new RuntimeException();
    nullCause.initCause(null);
    reports.add(Report
        .buildEnrichmentError()
        .withValue("http://urlValue4")
        .withException(nullCause)
        .build());
    return reports;
  }

  private HashSet<Report> getExpectedReportMessagesWarning2Flow() {
    HashSet<Report> reports = new HashSet<>();
    reports.add(Report
        .buildEnrichmentWarn()
        .withStatus(HttpStatus.BAD_REQUEST)
        .withValue("http://urlValue2")
        .withException(new HttpClientErrorException(HttpStatus.BAD_REQUEST))
        .build());
    return reports;
  }
}
