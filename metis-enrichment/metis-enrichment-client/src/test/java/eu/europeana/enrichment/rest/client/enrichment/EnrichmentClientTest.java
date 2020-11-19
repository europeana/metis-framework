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
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

class EnrichmentClientTest {

  @Test
  void testEnrich() throws RestClientException {
    Agent agent1 = new Agent();
    agent1.setAbout("Test Agent 1");

    Agent agent2 = new Agent();
    agent2.setAbout("Test Agent 2");

    ArrayList<EnrichmentBase> agentList = new ArrayList<>();
    agentList.add(agent1);
    agentList.add(agent2);

    final List<EnrichmentResultBaseWrapper> enrichmentBaseWrapperList = EnrichmentResultBaseWrapper
        .createNullOriginalFieldEnrichmentBaseWrapperList(
            Collections.singletonList(new ArrayList<>(agentList)));
    EnrichmentResultList result = new EnrichmentResultList(enrichmentBaseWrapperList);

    final RestTemplate restTemplate = mock(RestTemplate.class);
    doReturn(result).when(restTemplate)
        .postForObject(eq(ENRICH_ENTITY_SEARCH), any(HttpEntity.class),
            eq(EnrichmentResultList.class));

    final EnrichmentClient enrichmentClient = spy(new EnrichmentClient(restTemplate, "", 20));
    EnrichmentResultList res = enrichmentClient.enrich(new ArrayList<>());

    verify(restTemplate, times(1)).postForObject(eq(ENRICH_ENTITY_SEARCH), any(HttpEntity.class),
        eq(EnrichmentResultList.class));
    assertEquals(
        res.getEnrichmentBaseResultWrapperList().get(0).getEnrichmentBaseList().get(0).getAbout(),
        agent1.getAbout());
    assertEquals(
        res.getEnrichmentBaseResultWrapperList().get(0).getEnrichmentBaseList().get(1).getAbout(),
        agent2.getAbout());
  }

  @Test
  void testEnrichException() {
    final RestTemplate restTemplate = mock(RestTemplate.class);
    doThrow(new UnknownException("test")).when(restTemplate)
        .postForObject(not(eq(ENRICH_ENTITY_SEARCH)), any(HttpEntity.class),
            eq(EnrichmentResultList.class));

    final EnrichmentClient enrichmentClient = spy(
        new EnrichmentClient(restTemplate, "http://dummy", 20));

    assertThrows(UnknownException.class, () -> enrichmentClient.enrich(new ArrayList<>()));
  }

  @Test
  void testGetByUri() throws RestClientException {
    Agent agent = new Agent();
    agent.setAbout("Test Agent");

    ResponseEntity<EnrichmentBase> result = new ResponseEntity<>(agent, HttpStatus.OK);

    final RestTemplate restTemplate = mock(RestTemplate.class);

    doReturn(result).when(restTemplate)
        .exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class),
            eq(EnrichmentBase.class));

    final EnrichmentClient enrichmentClient = spy(
        new EnrichmentClient(restTemplate, "http://dummy", 20));

    EnrichmentBase res = enrichmentClient.getByUri("http://test");
    verify(restTemplate, times(1))
        .exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class),
            eq(EnrichmentBase.class));
    assertEquals(res.getAbout(), agent.getAbout());
  }
}
