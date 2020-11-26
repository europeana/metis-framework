package eu.europeana.enrichment.rest.client.enrichment;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import eu.europeana.metis.schema.jibx.RDF;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentResultBaseWrapper;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.enrichment.api.external.model.Place;
import eu.europeana.enrichment.rest.client.exceptions.EnrichmentException;
import eu.europeana.enrichment.utils.EnrichmentFields;
import eu.europeana.enrichment.utils.EntityMergeEngine;
import eu.europeana.enrichment.utils.EntityType;
import eu.europeana.enrichment.api.external.SearchValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class EnricherImplTest {

  private final ArgumentCaptor<List<SearchValue>> enrichmentExtractionCaptor = ArgumentCaptor
      .forClass(List.class);

  private final ArgumentCaptor<List<EnrichmentBase>> enrichmentResultCaptor = ArgumentCaptor
      .forClass(List.class);

  private static final EnrichmentResultList ENRICHMENT_RESULT;

  private static final List<Pair<SearchValue, EnrichmentFields>> ENRICHMENT_EXTRACT_RESULT = new ArrayList<>();

  static {
    final Place place1 = new Place();
    place1.setAbout("place1");
    final Place place2 = new Place();
    place2.setAbout("place2");

    final List<EnrichmentResultBaseWrapper> enrichmentResultBaseWrapperList = EnrichmentResultBaseWrapper
        .createEnrichmentResultBaseWrapperList(
            List.of(List.of(place1), Collections.emptyList(), List.of(place2)));
    ENRICHMENT_RESULT = new EnrichmentResultList(enrichmentResultBaseWrapperList);
    ENRICHMENT_EXTRACT_RESULT.add(
        new MutablePair<>(new SearchValue("value1", "lang1", EntityType.AGENT),
            EnrichmentFields.DC_CREATOR));
    ENRICHMENT_EXTRACT_RESULT.add(
        new MutablePair<>(new SearchValue("value2", null, EntityType.AGENT, EntityType.CONCEPT),
            EnrichmentFields.DC_SUBJECT));
    ENRICHMENT_EXTRACT_RESULT.add(
        new MutablePair<>(new SearchValue("value3", "lang2"), EnrichmentFields.DCTERMS_SPATIAL));
  }

  @Test
  void testEnricherHappyFlow() throws EnrichmentException {
    final EnrichmentClient enrichmentClient = Mockito.mock(EnrichmentClient.class);
    doReturn(ENRICHMENT_RESULT).when(enrichmentClient).enrich(any());
    final EntityMergeEngine entityMergeEngine = Mockito.mock(EntityMergeEngine.class);

    // Create enricher.
    final Enricher enricher = spy(new EnricherImpl(entityMergeEngine, enrichmentClient));
    doReturn(ENRICHMENT_EXTRACT_RESULT).when(enricher).extractValuesForEnrichment(any());
    doReturn(Collections.emptyMap()).when(enricher).extractReferencesForEnrichment(any());

    final RDF inputRdf = new RDF();
    enricher.enrichment(inputRdf);

    verifyEnricherHappyFlow(enricher, enrichmentClient, inputRdf);
    verifyMergeHappyFlow(entityMergeEngine);
  }

  @Test
  void testEnricherNullFlow() throws EnrichmentException {

    // Create mocks of the dependencies
    final EnrichmentClient enrichmentClient = Mockito.mock(EnrichmentClient.class);

    final EntityMergeEngine entityMergeEngine = Mockito.mock(EntityMergeEngine.class);

    //Create enricher
    final Enricher enricher = spy(new EnricherImpl(entityMergeEngine, enrichmentClient));
    doReturn(Collections.emptyList()).when(enricher).extractValuesForEnrichment(any());
    doReturn(Collections.emptyMap()).when(enricher).extractReferencesForEnrichment(any());

    final RDF inputRdf = new RDF();
    enricher.enrichment(inputRdf);

    verifyEnricherNullFlow(enrichmentClient, enricher, inputRdf);
    verifyMergeNullFlow(entityMergeEngine);
  }

  private void verifyEnricherHappyFlow(Enricher enricher, EnrichmentClient enrichmentClient,
      RDF inputRdf) {

    // Extracting values for enrichment
    verify(enricher, times(1)).extractValuesForEnrichment(any());
    verify(enricher, times(1)).extractValuesForEnrichment(inputRdf);

    // Actually enriching
    verify(enrichmentClient, times(1)).enrich(enrichmentExtractionCaptor.capture());
    assertArrayEquals(ENRICHMENT_EXTRACT_RESULT.stream().map(Pair::getKey).toArray(),
        enrichmentExtractionCaptor.getValue().toArray());


  }

  // Verify merge calls
  private void verifyMergeHappyFlow(EntityMergeEngine entityMergeEngine) {
    final List<EnrichmentBase> expectedMerges = new ArrayList<>();
    ENRICHMENT_RESULT.getEnrichmentBaseResultWrapperList()
        .forEach(list -> expectedMerges.addAll(list.getEnrichmentBaseList()));

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
        assertSame(expectedMerges.get(currentPointer), capturedMergedItem);
        currentPointer++;
      }
    }
  }

  private void verifyEnricherNullFlow(EnrichmentClient enrichmentClient, Enricher enricher,
      RDF inputRdf) {

    // Extracting values for enrichment
    verify(enricher, times(1)).extractValuesForEnrichment(any());
    verify(enricher, times(1)).extractValuesForEnrichment(inputRdf);

    // Actually enriching
    verify(enrichmentClient, times(0)).enrich(any());

  }

  private void verifyMergeNullFlow(EntityMergeEngine entityMergeEngine) {
    verify(entityMergeEngine, times(0)).mergeEntities(any(), eq(Collections.emptyList()), anyMap());
    verify(entityMergeEngine, times(0)).mergeEntities(any(), any(), anyMap());
  }
}
