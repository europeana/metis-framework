package eu.europeana.enrichment.rest.client;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import eu.europeana.enrichment.api.external.model.Agent;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import java.net.URI;
import java.util.ArrayList;
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
public class DereferenceClientTest {
	
	@Test
	public void testDereference() {
		Agent agent1 = new Agent();
		agent1.setAbout("Test Agent 1");
		
		Agent agent2 = new Agent();
		agent2.setAbout("Test Agent 2");
		
		ArrayList<EnrichmentBase> agentList = new ArrayList<EnrichmentBase>();
		agentList.add(agent1);
		agentList.add(agent2);
		
		EnrichmentResultList enrichmentResultList = new EnrichmentResultList(agentList);				
		ResponseEntity<EnrichmentResultList> result = new ResponseEntity<EnrichmentResultList>(enrichmentResultList, HttpStatus.OK);
		
	    final RestTemplate restTemplate = mock(RestTemplate.class);
	    
	    doReturn(result).when(restTemplate).exchange(any(URI.class),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(EnrichmentResultList.class));
	    
	    final DereferenceClient dereferenceClient = spy(new DereferenceClient("dummyId"));

	    dereferenceClient.setRestTemplate(restTemplate);
	    EnrichmentResultList res = dereferenceClient.dereference("http://dummy");
	    
	    verify(dereferenceClient).setRestTemplate(restTemplate);
	    verify(dereferenceClient).dereference("http://dummy");	    	    
	    verify(restTemplate, times(1)).exchange(any(URI.class),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(EnrichmentResultList.class));
	    
	    Assert.assertEquals(res.getResult().get(0).getAbout(), agent1.getAbout());
	    Assert.assertEquals(res.getResult().get(1).getAbout(), agent2.getAbout());
	}
}
