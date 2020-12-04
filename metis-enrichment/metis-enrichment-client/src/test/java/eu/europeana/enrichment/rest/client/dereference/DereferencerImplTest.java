package eu.europeana.enrichment.rest.client.dereference;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import eu.europeana.enrichment.rest.client.enrichment.RemoteEntityResolver;
import eu.europeana.metis.schema.jibx.RDF;
import eu.europeana.enrichment.api.external.model.Agent;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentResultBaseWrapper;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.enrichment.api.external.model.Place;
import eu.europeana.enrichment.api.external.model.Timespan;
import eu.europeana.enrichment.rest.client.exceptions.DereferenceException;
import eu.europeana.enrichment.utils.EntityMergeEngine;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;


public class DereferencerImplTest {

  private static final String[] DEREFERENCE_EXTRACT_RESULT =
      {"enrich1", "enrich3", "enrich4"};

  private static final List<EnrichmentResultList> DEREFERENCE_RESULT;
  private static final EnrichmentResultList ENRICHMENT_RESULT;

  static {
    final Agent agent1 = new Agent();
    agent1.setAbout("agent1");
    final Agent agent2 = new Agent();
    agent2.setAbout("agent2");
    final Place place1 = new Place();
    place1.setAbout("place1");
    final Place place2 = new Place();
    place2.setAbout("place2");
    final Timespan timeSpan1 = new Timespan();
    timeSpan1.setAbout("timespan1");
    final Timespan timeSpan2 = new Timespan();
    timeSpan2.setAbout("timespan2");
    final List<EnrichmentResultBaseWrapper> enrichmentResultBaseWrapperList1 =
        EnrichmentResultBaseWrapper
        .createEnrichmentResultBaseWrapperList(List.of(Arrays.asList(agent1, null, agent2)));
    final EnrichmentResultList dereferenceResult1 =
        new EnrichmentResultList(enrichmentResultBaseWrapperList1);
    final List<EnrichmentResultBaseWrapper> enrichmentResultBaseWrapperList2 =
        EnrichmentResultBaseWrapper
        .createEnrichmentResultBaseWrapperList(
            List.of(Arrays.asList(timeSpan1, timeSpan2, null)));
    final EnrichmentResultList dereferenceResult2 =
        new EnrichmentResultList(enrichmentResultBaseWrapperList2);
    DEREFERENCE_RESULT = Arrays.asList(dereferenceResult1, null, dereferenceResult2);
    final List<EnrichmentResultBaseWrapper> enrichmentResultBaseWrapperList3 =
        EnrichmentResultBaseWrapper
        .createEnrichmentResultBaseWrapperList(List.of(Arrays.asList(place1, null, place2)));
    ENRICHMENT_RESULT = new EnrichmentResultList(enrichmentResultBaseWrapperList3);
  }

  @Test
  void testDereferencerHappyFlow() throws DereferenceException {

    // Create mocks of the dependencies
    final RemoteEntityResolver enrichmentClient = mock(RemoteEntityResolver.class);
    doReturn(ENRICHMENT_RESULT).when(enrichmentClient).resolveByText(any());
    final DereferenceClient dereferenceClient = mock(DereferenceClient.class);
    doReturn(DEREFERENCE_RESULT.get(0),
        DEREFERENCE_RESULT.subList(1, DEREFERENCE_RESULT.size()).toArray()).when(dereferenceClient)
        .dereference(any());
    final EntityMergeEngine entityMergeEngine = mock(EntityMergeEngine.class);

    final Dereferencer dereferencer = spy(
        new DereferencerImpl(entityMergeEngine, enrichmentClient, dereferenceClient));
    doReturn(Arrays.stream(DEREFERENCE_EXTRACT_RESULT).collect(Collectors.toSet()))
        .when(dereferencer)
        .extractReferencesForDereferencing(any());

    final RDF inputRdf = new RDF();
    dereferencer.dereference(inputRdf);

    verifyDereferenceHappyFlow(dereferenceClient, dereferencer, inputRdf);
    verifyMergeHappyFlow(entityMergeEngine);

  }

  @Test
  void testDereferencerNullFlow() throws DereferenceException {

    // Create mocks of the dependencies
    final RemoteEntityResolver remoteEntityResolver = mock(RemoteEntityResolver.class);
    final DereferenceClient dereferenceClient = mock(DereferenceClient.class);

    final EntityMergeEngine entityMergeEngine = mock(EntityMergeEngine.class);

    // Create dereferencer.
    final Dereferencer dereferencer = spy(new DereferencerImpl(entityMergeEngine, remoteEntityResolver, dereferenceClient));
    doReturn(Arrays.stream(new String[0]).collect(Collectors.toSet())).when(dereferencer)
        .extractReferencesForDereferencing(any());

    final RDF inputRdf = new RDF();
    dereferencer.dereference(inputRdf);

    verifyDereferenceNullFlow(dereferenceClient, dereferencer, inputRdf);
    verifyMergeNullFlow(entityMergeEngine);

  }


  private void verifyDereferenceHappyFlow(DereferenceClient dereferenceClient,
      Dereferencer dereferencer,
      RDF inputRdf) {

    // Extracting values for dereferencing
    verify(dereferencer, times(1)).extractReferencesForDereferencing(any());
    verify(dereferencer, times(1)).extractReferencesForDereferencing(inputRdf);

    // Actually dereferencing.
    verify(dereferenceClient, times(DEREFERENCE_EXTRACT_RESULT.length)).dereference(anyString());
    for (String dereferenceUrl : DEREFERENCE_EXTRACT_RESULT) {
      verify(dereferenceClient, times(1)).dereference(dereferenceUrl);
    }

  }

  // Verify merge calls
  private void verifyMergeHappyFlow(EntityMergeEngine entityMergeEngine) {
    final List<EnrichmentBase> expectedMerges = new ArrayList<>();
    DEREFERENCE_RESULT.stream().filter(Objects::nonNull)
            .map(EnrichmentResultList::getEnrichmentBaseResultWrapperList)
            .filter(Objects::nonNull).flatMap(List::stream)
            .forEach(list -> expectedMerges.addAll(list.getEnrichmentBaseList()));
    verify(entityMergeEngine, times(1)).mergeEntities(any(),
        eq(expectedMerges), anySet());
  }

  private void verifyDereferenceNullFlow(DereferenceClient dereferenceClient,
      Dereferencer dereferencer, RDF inputRdf) {

    // Extracting values for dereferencing
    verify(dereferencer, times(1)).extractReferencesForDereferencing(any());
    verify(dereferencer, times(1)).extractReferencesForDereferencing(inputRdf);

    // Actually dereferencing: don't use the null values.
    final Set<String> dereferenceUrls = Arrays.stream(new String[0])
        .filter(Objects::nonNull).collect(Collectors.toSet());
    verify(dereferenceClient, times(dereferenceUrls.size())).dereference(anyString());
    for (String dereferenceUrl : dereferenceUrls) {
      verify(dereferenceClient, times(1)).dereference(dereferenceUrl);
    }

  }

  private void verifyMergeNullFlow(EntityMergeEngine entityMergeEngine) {
    verify(entityMergeEngine, times(1)).mergeEntities(any(), eq(Collections.emptyList()), anySet());
    verify(entityMergeEngine, times(1)).mergeEntities(any(), any(), anySet());
  }
}
