package eu.europeana.enrichment.rest.client;

import static eu.europeana.metis.RestEndpoints.ENRICHMENT_ENRICH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import eu.europeana.enrichment.api.exceptions.UnknownException;
import eu.europeana.enrichment.api.external.InputValueList;
import eu.europeana.enrichment.api.external.model.Agent;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.enrichment.utils.InputValue;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@RunWith(MockitoJUnitRunner.class)
public class EnrichmentClientTest {
	
	@Test
	public void testEnrich() {
		Agent agent1 = new Agent();
		agent1.setAbout("Test Agent 1");
		
		Agent agent2 = new Agent();
		agent2.setAbout("Test Agent 2");
		
		ArrayList<EnrichmentBase> agentList = new ArrayList<EnrichmentBase>();
		agentList.add(agent1);
		agentList.add(agent2);
		
		EnrichmentResultList result = new EnrichmentResultList(agentList);
		
	    final RestTemplate restTemplate = mock(RestTemplate.class);
	    doReturn(result).when(restTemplate).postForObject(eq(ENRICHMENT_ENRICH), any(InputValueList.class), eq(EnrichmentResultList.class));
	    
	    final EnrichmentClient enrichmentClient = spy(new EnrichmentClient(""));
	    enrichmentClient.setRestTemplate(restTemplate);	    
	  
	    List<InputValue> values = new ArrayList<InputValue>();
	    EnrichmentResultList res = enrichmentClient.enrich(values);	    
	    
	    verify(enrichmentClient).setRestTemplate(restTemplate);
	    verify(enrichmentClient).enrich(values);	    
	    verify(restTemplate, times(1)).postForObject(eq(ENRICHMENT_ENRICH), any(InputValueList.class), eq(EnrichmentResultList.class));	    
	    Assert.assertEquals(res.getResult().get(0).getAbout(), agent1.getAbout());
	    Assert.assertEquals(res.getResult().get(1).getAbout(), agent2.getAbout());
	}
	
	@Test(expected = UnknownException.class)
	public void testEnrichException() {
	    final RestTemplate restTemplate = mock(RestTemplate.class);
	    doThrow(new UnknownException("test")).when(restTemplate).postForObject(not(eq(ENRICHMENT_ENRICH)), any(InputValueList.class), eq(EnrichmentResultList.class));
	    
	    final EnrichmentClient enrichmentClient = spy(new EnrichmentClient("http://dummy"));
	    enrichmentClient.setRestTemplate(restTemplate);	    
	    
	    List<InputValue> values = new ArrayList<InputValue>();
	    enrichmentClient.enrich(values);
	}

	@Test
	public void testGetByUri() {		
		Agent agent = new Agent();
		agent.setAbout("Test Agent");
		
		ResponseEntity<EnrichmentBase> result = new ResponseEntity<EnrichmentBase>(agent, HttpStatus.OK);
		
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
	    Assert.assertEquals(res.getAbout(), agent.getAbout());
	}
}
