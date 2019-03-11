package eu.europeana.enrichment.rest.client;

import static eu.europeana.metis.RestEndpoints.ENRICHMENT_ENRICH;
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
import eu.europeana.enrichment.api.external.InputValueList;
import eu.europeana.enrichment.api.external.model.Agent;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentBaseWrapper;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.enrichment.utils.InputValue;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class EnrichmentClientTest {

  @Test
  public void testEnrich() {
    Agent agent1 = new Agent();
    agent1.setAbout("Test Agent 1");

    Agent agent2 = new Agent();
    agent2.setAbout("Test Agent 2");

    ArrayList<EnrichmentBase> agentList = new ArrayList<>();
    agentList.add(agent1);
    agentList.add(agent2);

    final List<EnrichmentBaseWrapper> enrichmentBaseWrapperList = EnrichmentBaseWrapper
        .createNullOriginalFieldEnrichmentBaseWrapperList(agentList);
    EnrichmentResultList result = new EnrichmentResultList(enrichmentBaseWrapperList);

    final RestTemplate restTemplate = mock(RestTemplate.class);
    doReturn(result).when(restTemplate)
        .postForObject(eq(ENRICHMENT_ENRICH), any(InputValueList.class),
            eq(EnrichmentResultList.class));

    final EnrichmentClient enrichmentClient = spy(new EnrichmentClient(""));
    enrichmentClient.setRestTemplate(restTemplate);

    List<InputValue> values = new ArrayList<InputValue>();
    EnrichmentResultList res = enrichmentClient.enrich(values);

    verify(enrichmentClient).setRestTemplate(restTemplate);
    verify(enrichmentClient).enrich(values);
    verify(restTemplate, times(1)).postForObject(eq(ENRICHMENT_ENRICH), any(InputValueList.class),
        eq(EnrichmentResultList.class));
    assertEquals(res.getEnrichmentBaseWrapperList().get(0).getEnrichmentBase().getAbout(),
        agent1.getAbout());
    assertEquals(res.getEnrichmentBaseWrapperList().get(1).getEnrichmentBase().getAbout(),
        agent2.getAbout());
  }

  @Test
  public void testEnrichException() {
    final RestTemplate restTemplate = mock(RestTemplate.class);
    doThrow(new UnknownException("test")).when(restTemplate)
        .postForObject(not(eq(ENRICHMENT_ENRICH)), any(InputValueList.class),
            eq(EnrichmentResultList.class));

    final EnrichmentClient enrichmentClient = spy(new EnrichmentClient("http://dummy"));
    enrichmentClient.setRestTemplate(restTemplate);

    List<InputValue> values = new ArrayList<>();
    assertThrows(UnknownException.class, () -> enrichmentClient.enrich(values));
  }

  @Test
  public void testGetByUri() {
    Agent agent = new Agent();
    agent.setAbout("Test Agent");

    ResponseEntity<EnrichmentBase> result = new ResponseEntity<EnrichmentBase>(agent,
        HttpStatus.OK);

    final RestTemplate restTemplate = mock(RestTemplate.class);

    doReturn(result).when(restTemplate).exchange(any(URI.class),
        any(HttpMethod.class),
        any(HttpEntity.class),
        eq(EnrichmentBase.class));

    final EnrichmentClient enrichmentClient = spy(new EnrichmentClient("http://dummy"));

    enrichmentClient.setRestTemplate(restTemplate);
    EnrichmentBase res = enrichmentClient.getByUri("http://test");

    verify(enrichmentClient).setRestTemplate(restTemplate);
    verify(enrichmentClient).getByUri("http://test");
    verify(restTemplate, times(1)).exchange(any(URI.class),
        eq(HttpMethod.GET),
        any(HttpEntity.class),
        eq(EnrichmentBase.class));
    assertEquals(res.getAbout(), agent.getAbout());
  }
}
