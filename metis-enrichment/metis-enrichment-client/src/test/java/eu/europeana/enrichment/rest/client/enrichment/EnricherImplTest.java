package eu.europeana.enrichment.rest.client.enrichment;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.enrichment.api.external.model.EnrichmentBaseWrapper;
import eu.europeana.enrichment.api.external.model.EnrichmentResultBaseWrapper;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.enrichment.api.external.model.Place;
import eu.europeana.enrichment.rest.client.exceptions.EnrichmentException;
import eu.europeana.enrichment.utils.EntityMergeEngine;
import eu.europeana.enrichment.utils.EntityType;
import eu.europeana.enrichment.utils.InputValue;
import eu.europeana.enrichment.utils.SearchValue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class EnricherImplTest {

  private ArgumentCaptor<List<SearchValue>> enrichmentExtractionCaptor = ArgumentCaptor
      .forClass(List.class);

  private ArgumentCaptor<List<EnrichmentResultBaseWrapper>> enrichmentResultCaptor = ArgumentCaptor
      .forClass(List.class);

  private static final EnrichmentResultList ENRICHMENT_RESULT;

  private static final InputValue[] ENRICHMENT_EXTRACT_RESULT =
      {new InputValue("orig1", "value1", "lang1", EntityType.AGENT),
          new InputValue(null, "value2", null, EntityType.AGENT, EntityType.CONCEPT),
          new InputValue("orig3", null, "lang2")};

  static {
    final Place place1 = new Place();
    place1.setAbout("place1");
    final Place place2 = new Place();
    place2.setAbout("place2");
    final List<EnrichmentBaseWrapper> enrichmentBaseWrapperList3 = EnrichmentBaseWrapper
        .createNullOriginalFieldEnrichmentBaseWrapperList(Arrays.asList(place1, null, place2));
    ENRICHMENT_RESULT = new EnrichmentResultList(enrichmentBaseWrapperList3);
  }

  @Test
  void testEnricherHappyFlow() throws EnrichmentException {
    final EnrichmentClient enrichmentClient = Mockito.mock(EnrichmentClient.class);
    doReturn(ENRICHMENT_RESULT).when(enrichmentClient).enrich(any());
    final EntityMergeEngine entityMergeEngine = Mockito.mock(EntityMergeEngine.class);

    // Create enricher.
    final Enricher enricher = spy(new EnricherImpl(entityMergeEngine, enrichmentClient));
    doReturn(Arrays.asList(ENRICHMENT_EXTRACT_RESULT)).when(enricher)
        .extractValuesForEnrichment(any());
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

  private void verifyEnricherHappyFlow(Enricher enricher,
      EnrichmentClient enrichmentClient, RDF inputRdf) {

    // Extracting values for enrichment
    verify(enricher, times(1)).extractValuesForEnrichment(any());
    verify(enricher, times(1)).extractValuesForEnrichment(inputRdf);

    // Actually enriching
    verify(enrichmentClient, times(1)).enrich(enrichmentExtractionCaptor.capture());
    assertArrayEquals(ENRICHMENT_EXTRACT_RESULT, enrichmentExtractionCaptor.getValue().toArray());


  }

  // Verify merge calls
  private void verifyMergeHappyFlow(EntityMergeEngine entityMergeEngine) {
    final List<List<EnrichmentResultBaseWrapper>> expectedMerges = new ArrayList<>();

    expectedMerges.add(ENRICHMENT_RESULT.getEnrichmentBaseResultWrapperList());

    verify(entityMergeEngine, times(expectedMerges.size())).mergeEntities(any(),
        enrichmentResultCaptor.capture(), any());
    // Note that the captor returns a linked list, so we don't want to use indices.
    // But the interface gives a generic type List, so we don't want to depend on the
    // linked list functionality either.
    int currentPointer = 0;
    final List<List<EnrichmentResultBaseWrapper>> foundValues = enrichmentResultCaptor.getAllValues()
        .subList(
            enrichmentResultCaptor.getAllValues().size() - expectedMerges.size(),
            enrichmentResultCaptor.getAllValues().size());
    for (List<EnrichmentResultBaseWrapper> capturedMerge : foundValues) {
      assertArrayEquals(expectedMerges.get(currentPointer).toArray(), capturedMerge.toArray());
      currentPointer++;
    }
  }

  private void verifyEnricherNullFlow(EnrichmentClient enrichmentClient, Enricher enricher, RDF inputRdf) {

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
