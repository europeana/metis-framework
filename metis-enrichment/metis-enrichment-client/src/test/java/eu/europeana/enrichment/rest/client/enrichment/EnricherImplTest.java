package eu.europeana.enrichment.rest.client.enrichment;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
import eu.europeana.enrichment.rest.client.EnrichmentWorker.Mode;
import eu.europeana.enrichment.rest.client.report.ReportMessage;
import eu.europeana.enrichment.rest.client.report.ReportMessage.ReportMessageBuilder;
import eu.europeana.enrichment.rest.client.report.Type;
import eu.europeana.enrichment.utils.EntityMergeEngine;
import eu.europeana.metis.schema.jibx.RDF;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

public class EnricherImplTest {

  private final ArgumentCaptor<Set<SearchTerm>> enrichmentExtractionCaptor = ArgumentCaptor.forClass(Set.class);

  private final ArgumentCaptor<List<EnrichmentBase>> enrichmentResultCaptor = ArgumentCaptor.forClass(List.class);

  private static final Map<SearchTerm, List<EnrichmentBase>> ENRICHMENT_RESULT;

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
    Set<ReportMessage> reportMessages = enricher.enrichment(inputRdf);

    verifyEnricherHappyFlow(recordParser, entityResolver, inputRdf, reportMessages);
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
    Set<ReportMessage> reportMessages = enricher.enrichment(inputRdf);

    verifyEnricherNullFlow(entityResolver, recordParser, inputRdf, reportMessages);
    verifyMergeNullFlow(entityMergeEngine);
  }

  @Test
  void testEnricherHttpExceptionFlow() {
    // Create mocks
    final RecordParser recordParser = Mockito.mock(RecordParser.class);
    final ClientEntityResolver entityResolver = Mockito.mock(ClientEntityResolver.class);
    doThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST)).when(entityResolver).resolveByText(any());
    final EntityMergeEngine entityMergeEngine = Mockito.mock(EntityMergeEngine.class);

    // Create enricher.
    final Enricher enricher = spy(
        new EnricherImpl(recordParser, entityResolver, entityMergeEngine));
    doReturn(ENRICHMENT_EXTRACT_RESULT).when(recordParser).parseSearchTerms(any());
    doReturn(Collections.emptySet()).when(recordParser).parseReferences(any());

    final RDF inputRdf = new RDF();
    Set<ReportMessage> reportMessages = enricher.enrichment(inputRdf);
    verifyEnricherExeptionFlow(recordParser, entityResolver, inputRdf, reportMessages);
  }

  private void verifyEnricherExeptionFlow(RecordParser recordParser, ClientEntityResolver entityResolver,
      RDF inputRdf, Set<ReportMessage> reportMessages) {
    // Extracting values for enrichment
    verify(recordParser, times(1)).parseSearchTerms(any());
    verify(recordParser, times(1)).parseSearchTerms(inputRdf);
    verify(entityResolver, times(0)).resolveById(any());
    assertEquals(getExpectedReportMessagesExceptionFlow(), reportMessages);
  }

  private void verifyEnricherHappyFlow(RecordParser recordParser, ClientEntityResolver remoteEntityResolver,
      RDF inputRdf, Set<ReportMessage> reportMessages) {

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
    assertEquals(getExpectedReportMessagesHappyFlow(), reportMessages);
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
      RecordParser recordParser, RDF inputRdf, Set<ReportMessage> reportMessages) {

    // Extracting values for enrichment
    verify(recordParser, times(1)).parseSearchTerms(any());
    verify(recordParser, times(1)).parseSearchTerms(inputRdf);

    // Actually enriching
    verify(remoteEntityResolver, never()).resolveByText(any());
    assertEquals(getExpectedReportMessagesNullFlow(), reportMessages);
  }

  private void verifyMergeNullFlow(EntityMergeEngine entityMergeEngine) {
    verify(entityMergeEngine, times(0)).mergeReferenceEntities(any(), eq(Collections.emptyList()),
        any(ReferenceTermContext.class));
    verify(entityMergeEngine, times(0)).mergeReferenceEntities(any(), any(), any(ReferenceTermContext.class));
  }

  private HashSet<ReportMessage> getExpectedReportMessagesHappyFlow() {
    HashSet<ReportMessage> reportMessages = new HashSet<>();
    reportMessages.add(new ReportMessageBuilder()
        .withStatus(HttpStatus.OK)
        .withMode(Mode.ENRICHMENT)
        .withMessageType(Type.IGNORE)
        .withValue("value2")
        .withMessage("Could not find an entity for the given search term.")
        .withStackTrace("")
        .build());
    reportMessages.add(new ReportMessageBuilder()
        .withStatus(HttpStatus.OK)
        .withMode(Mode.ENRICHMENT)
        .withMessageType(Type.IGNORE)
        .withValue("[]")
        .withMessage("Empty search reference.")
        .withStackTrace("")
        .build());
    return reportMessages;
  }

  private HashSet<ReportMessage> getExpectedReportMessagesNullFlow() {
    HashSet<ReportMessage> reportMessages = new HashSet<>();
    reportMessages.add(new ReportMessageBuilder()
        .withStatus(HttpStatus.OK)
        .withMode(Mode.ENRICHMENT)
        .withMessageType(Type.IGNORE)
        .withValue("[]")
        .withMessage("Empty search terms.")
        .withStackTrace("")
        .build());
    reportMessages.add(new ReportMessageBuilder()
        .withStatus(HttpStatus.OK)
        .withMode(Mode.ENRICHMENT)
        .withMessageType(Type.IGNORE)
        .withValue("[]")
        .withMessage("Empty search reference.")
        .withStackTrace("")
        .build());
    return reportMessages;
  }

  private HashSet<ReportMessage> getExpectedReportMessagesExceptionFlow() {
    HashSet<ReportMessage> reportMessages = new HashSet<>();
    reportMessages.add(new ReportMessageBuilder()
        .withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        .withMode(Mode.ENRICHMENT)
        .withMessageType(Type.ERROR)
        .withValue("value1,value2,value3")
        .withMessage("HttpClientErrorException: 400 BAD_REQUEST")
        .withStackTrace("org.springframework.web.client.HttpClientErrorException: 400 BAD_REQUEST")
        .build());
    reportMessages.add(new ReportMessageBuilder()
        .withStatus(HttpStatus.OK)
        .withMode(Mode.ENRICHMENT)
        .withMessageType(Type.IGNORE)
        .withValue("[]")
        .withMessage("Empty search reference.")
        .withStackTrace("")
        .build());
    return reportMessages;
  }
}
