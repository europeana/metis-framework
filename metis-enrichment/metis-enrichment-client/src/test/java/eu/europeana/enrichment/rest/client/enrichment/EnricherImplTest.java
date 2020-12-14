package eu.europeana.enrichment.rest.client.enrichment;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import eu.europeana.enrichment.api.external.SearchValue;
import eu.europeana.enrichment.api.internal.SearchTerm;
import eu.europeana.enrichment.api.internal.SearchTermContext;
import eu.europeana.enrichment.utils.EntityType;
import eu.europeana.metis.schema.jibx.RDF;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.Place;
import eu.europeana.enrichment.rest.client.exceptions.EnrichmentException;
import eu.europeana.enrichment.api.internal.FieldType;
import eu.europeana.enrichment.utils.EntityMergeEngine;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class EnricherImplTest {

  private final ArgumentCaptor<Set<SearchTerm>> enrichmentExtractionCaptor = ArgumentCaptor
      .forClass(Set.class);

  private final ArgumentCaptor<List<EnrichmentBase>> enrichmentResultCaptor = ArgumentCaptor
      .forClass(List.class);

  private static final Map<SearchTerm, List<EnrichmentBase>> ENRICHMENT_RESULT;

  private static final List<Pair<SearchValue, FieldType>> ENRICHMENT_EXTRACT_RESULT = new ArrayList<>();

  static {
    final Place place1 = new Place();
    place1.setAbout("place1");
    final Place place2 = new Place();
    place2.setAbout("place2");

    SearchTerm searchTerm1 = new SearchTermContext("value1", "en",
        Set.of(FieldType.DC_CREATOR));
    SearchTerm searchTerm2 = new SearchTermContext("value2", null,
        Set.of(FieldType.DC_SUBJECT));
    SearchTerm searchTerm3 = new SearchTermContext("value3", "pt",
        Set.of(FieldType.DCTERMS_SPATIAL));

    ENRICHMENT_RESULT = new HashMap<>();
    ENRICHMENT_RESULT.put(searchTerm1, List.of(place1));
    ENRICHMENT_RESULT.put(searchTerm2, Collections.emptyList());
    ENRICHMENT_RESULT.put(searchTerm3, List.of(place2));
    ENRICHMENT_EXTRACT_RESULT.add(
        new MutablePair<>(
            new SearchValue("value1", "en", EntityType.AGENT),
            FieldType.DC_CREATOR));
    ENRICHMENT_EXTRACT_RESULT.add(
        new MutablePair<>(new SearchValue("value2", null, EntityType.PLACE, EntityType.CONCEPT),
            FieldType.DC_SUBJECT));
    ENRICHMENT_EXTRACT_RESULT.add(
        new MutablePair<>(
            new SearchValue("value3", "pt"),
            FieldType.DCTERMS_SPATIAL));
  }

  @Test
  void testEnricherHappyFlow() throws EnrichmentException {
    final RemoteEntityResolver remoteEntityResolver = Mockito.mock(RemoteEntityResolver.class);
    doReturn(ENRICHMENT_RESULT).when(remoteEntityResolver).resolveByText(any());
    final EntityMergeEngine entityMergeEngine = Mockito.mock(EntityMergeEngine.class);

    // Create enricher.
    final Enricher enricher = spy(new EnricherImpl(entityMergeEngine, remoteEntityResolver));
    doReturn(ENRICHMENT_EXTRACT_RESULT).when(enricher).extractValuesForEnrichment(any());
    doReturn(Collections.emptyMap()).when(enricher).extractReferencesForEnrichment(any());

    final RDF inputRdf = new RDF();
    enricher.enrichment(inputRdf);

    verifyEnricherHappyFlow(enricher, remoteEntityResolver, inputRdf);
    verifyMergeHappyFlow(entityMergeEngine);
  }

  @Test
  void testEnricherNullFlow() throws EnrichmentException {

    // Create mocks of the dependencies
    final RemoteEntityResolver remoteEntityResolver = Mockito.mock(RemoteEntityResolver.class);

    final EntityMergeEngine entityMergeEngine = Mockito.mock(EntityMergeEngine.class);

    //Create enricher
    final Enricher enricher = spy(new EnricherImpl(entityMergeEngine, remoteEntityResolver));
    doReturn(Collections.emptyList()).when(enricher).extractValuesForEnrichment(any());
    doReturn(Collections.emptyMap()).when(enricher).extractReferencesForEnrichment(any());

    final RDF inputRdf = new RDF();
    enricher.enrichment(inputRdf);

    verifyEnricherNullFlow(remoteEntityResolver, enricher, inputRdf);
    verifyMergeNullFlow(entityMergeEngine);
  }

  private void verifyEnricherHappyFlow(Enricher enricher, RemoteEntityResolver remoteEntityResolver,
      RDF inputRdf) {

    // Extracting values for enrichment
    verify(enricher, times(1)).extractValuesForEnrichment(any());
    verify(enricher, times(1)).extractValuesForEnrichment(inputRdf);

    // Actually enriching
    verify(remoteEntityResolver, times(1)).resolveByText(enrichmentExtractionCaptor.capture());

    Comparator<SearchTerm> compareValue = Comparator.comparing(SearchTerm::getTextValue);

    List<SearchTerm> expectedValue = ENRICHMENT_EXTRACT_RESULT.stream()
        .map(pair -> new SearchTermContext(pair.getKey().getValue(), pair.getKey().getLanguage(),
            Set.of(pair.getValue()))).collect(Collectors.toList());

    List<SearchTerm> actualResult = new ArrayList<>(
        List.copyOf(enrichmentExtractionCaptor.getValue()));

    expectedValue.sort(compareValue);
    actualResult.sort(compareValue);

    for(int i = 0; i < expectedValue.size(); i++){
      SearchTerm expected = expectedValue.get(i);
      SearchTerm actual = actualResult.get(i);
      assertEquals(expected.getTextValue(), actual.getTextValue());
      assertEquals(expected.getLanguage(), actual.getLanguage());
      assertArrayEquals(expected.getCandidateTypes().toArray(), actual.getCandidateTypes().toArray());
    }

  }

  // Verify merge calls
  private void verifyMergeHappyFlow(EntityMergeEngine entityMergeEngine) {
    final List<EnrichmentBase> expectedMerges = new ArrayList<>();
    ENRICHMENT_RESULT.forEach((x, y) -> expectedMerges.addAll(y));

    verify(entityMergeEngine, times(expectedMerges.size()))
        .mergeEntities(any(), enrichmentResultCaptor.capture(), anySet());
    // Note that the captor returns a linked list, so we don't want to use indices.
    // But the interface gives a generic type List, so we don't want to depend on the
    // linked list functionality either.
    int currentPointer = 0;
    final List<List<EnrichmentBase>> allValues = enrichmentResultCaptor.getAllValues();
    final List<List<EnrichmentBase>> foundValues = allValues
        .subList(enrichmentResultCaptor.getAllValues().size() - expectedMerges.size(),
            enrichmentResultCaptor.getAllValues().size());
    for (List<EnrichmentBase> capturedMerge : foundValues) {
      for (EnrichmentBase capturedMergedItem : capturedMerge) {
        assertTrue(expectedMerges.contains(capturedMergedItem));
        currentPointer++;
      }
    }
  }

  private void verifyEnricherNullFlow(RemoteEntityResolver remoteEntityResolver, Enricher enricher,
      RDF inputRdf) {

    // Extracting values for enrichment
    verify(enricher, times(1)).extractValuesForEnrichment(any());
    verify(enricher, times(1)).extractValuesForEnrichment(inputRdf);

    // Actually enriching
    verify(remoteEntityResolver, times(1)).resolveByUri(any());

  }

  private void verifyMergeNullFlow(EntityMergeEngine entityMergeEngine) {
    verify(entityMergeEngine, times(0)).mergeEntities(any(), eq(Collections.emptyList()), anyMap());
    verify(entityMergeEngine, times(0)).mergeEntities(any(), any(), anyMap());
  }
}
