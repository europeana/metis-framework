package eu.europeana.enrichment.rest.client.dereference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import eu.europeana.enrichment.api.external.DereferenceResultStatus;
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
import org.springframework.web.client.RestTemplate;

class DereferenceClientTest {

  @Test
  void testDereference() {
    Agent agent1 = new Agent();
    agent1.setAbout("Test Agent 1");

    Agent agent2 = new Agent();
    agent2.setAbout("Test Agent 2");

    ArrayList<EnrichmentBase> agentList = new ArrayList<>();
    agentList.add(agent1);
    agentList.add(agent2);
    final List<EnrichmentResultBaseWrapper> enrichmentResultBaseWrapperList = EnrichmentResultBaseWrapper
        .createEnrichmentResultBaseWrapperList(
            Collections.singletonList(new ArrayList<>(agentList)), DereferenceResultStatus.SUCCESS);

    final EnrichmentResultList enrichmentResultList = new EnrichmentResultList(
        enrichmentResultBaseWrapperList);
    final ResponseEntity<EnrichmentResultList> result = new ResponseEntity<>(enrichmentResultList,
        HttpStatus.OK);

    final RestTemplate restTemplate = mock(RestTemplate.class);
    doReturn(result).when(restTemplate)
                    .exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class),
                        eq(EnrichmentResultList.class));

    final DereferenceClient dereferenceClient = spy(
        new DereferenceClient(restTemplate, "http://dummy"));
    EnrichmentResultList res = dereferenceClient.dereference("http://dummy");

    verify(restTemplate, times(1))
        .exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class),
            eq(EnrichmentResultList.class));

    assertEquals(
        res.getEnrichmentBaseResultWrapperList().getFirst().getEnrichmentBaseList().get(0).getAbout(),
        agent1.getAbout());
    assertEquals(
        res.getEnrichmentBaseResultWrapperList().getFirst().getEnrichmentBaseList().get(1).getAbout(),
        agent2.getAbout());
  }
}
