package eu.europeana.enrichment.rest.client.enrichment;

import static eu.europeana.metis.utils.RestEndpoints.ENRICH_ENTITY_SEARCH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import eu.europeana.enrichment.api.exceptions.UnknownException;
import eu.europeana.enrichment.api.external.model.Agent;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentResultBaseWrapper;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.enrichment.api.external.model.Place;
import eu.europeana.enrichment.api.internal.ReferenceTerm;
import eu.europeana.enrichment.api.internal.ReferenceTermImpl;
import eu.europeana.enrichment.api.internal.SearchTerm;
import eu.europeana.enrichment.api.internal.SearchTermImpl;
import eu.europeana.enrichment.utils.EntityType;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class RemoteEntityResolverTest {

  @Test
  @Disabled
  void testResolveByText() throws RestClientException, MalformedURLException, URISyntaxException {
    Place place1 = new Place();
    place1.setAbout("Paris");

    Place place2 = new Place();
    place2.setAbout("London");

    ArrayList<EnrichmentBase> agentList = new ArrayList<>();
    agentList.add(place1);
    agentList.add(place2);

    final List<EnrichmentResultBaseWrapper> enrichmentResultBaseWrapperList = EnrichmentResultBaseWrapper
        .createEnrichmentResultBaseWrapperList(
            Collections.singletonList(new ArrayList<>(agentList)));
    EnrichmentResultList result = new EnrichmentResultList(enrichmentResultBaseWrapperList);

    final RestTemplate restTemplate = mock(RestTemplate.class);
    doReturn(result).when(restTemplate)
        .postForObject(eq(ENRICH_ENTITY_SEARCH), any(HttpEntity.class),
            eq(EnrichmentResultList.class));

    final RemoteEntityResolver remoteEntityResolver = spy(new RemoteEntityResolver(new URL("http://example.com"), 20, restTemplate));
    Set<SearchTerm> setToTest = new HashSet<>();
    setToTest.add(new SearchTermImpl("Paris", "en", Set.of(EntityType.PLACE)));
    setToTest.add(new SearchTermImpl("London", "en", Set.of(EntityType.PLACE)));

    Map<SearchTerm, List<EnrichmentBase>> res = remoteEntityResolver.resolveByText(setToTest);

    verify(restTemplate, times(1)).postForObject(eq(new URI("http://example.com" + ENRICH_ENTITY_SEARCH)), any(HttpEntity.class),
        eq(EnrichmentResultList.class));
    assertEquals(res.entrySet().iterator().next().getValue().get(0).getAbout(), place1.getAbout());
    assertEquals(res.entrySet().iterator().next().getValue().get(1).getAbout(), place2.getAbout());
  }

  @Test
  void testEnrichException() throws MalformedURLException {
    final RestTemplate restTemplate = mock(RestTemplate.class);
    doThrow(new UnknownException("test")).when(restTemplate)
        .postForObject(not(eq(ENRICH_ENTITY_SEARCH)), any(HttpEntity.class),
            eq(EnrichmentResultList.class));

    final RemoteEntityResolver remoteEntityResolver = spy(
        new RemoteEntityResolver(new URL("http://"), 20, restTemplate));

    assertThrows(UnknownException.class, () -> remoteEntityResolver.resolveByText(new HashSet<>()));
  }

  @Test
  @Disabled
  void testResolveByUri() throws RestClientException, MalformedURLException {
    Agent agent = new Agent();
    agent.setAbout("Test Agent");

    ResponseEntity<EnrichmentBase> result = new ResponseEntity<>(agent, HttpStatus.OK);

    final RestTemplate restTemplate = mock(RestTemplate.class);

    doReturn(result).when(restTemplate)
        .exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class),
            eq(EnrichmentBase.class));

    final RemoteEntityResolver remoteEntityResolver = spy(
        new RemoteEntityResolver(new URL("http://dummy"), 20, restTemplate));

    Map<ReferenceTerm, List<EnrichmentBase>> res = remoteEntityResolver
        .resolveByUri(Set.of(new ReferenceTermImpl(new URL("http://test"), new HashSet<>())));
    verify(restTemplate, times(1))
        .exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class),
            eq(EnrichmentBase.class));
    assertEquals(res.entrySet().iterator().next().getValue().get(0).getAbout(), agent.getAbout());
  }

}
