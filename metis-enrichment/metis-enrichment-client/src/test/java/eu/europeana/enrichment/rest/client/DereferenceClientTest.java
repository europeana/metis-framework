package eu.europeana.enrichment.rest.client;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.enrichment.utils.InputValue;
import org.junit.Assert;

@RunWith(MockitoJUnitRunner.class)
public class DereferenceClientTest {
	
	@Test
	public void testDereference() {
		ResponseEntity<EnrichmentResultList> result = new ResponseEntity<EnrichmentResultList>(HttpStatus.OK);
		
	    final RestTemplate restTemplate = mock(RestTemplate.class);
	    
	    doReturn(result).when(restTemplate).exchange(any(URI.class),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(EnrichmentResultList.class));
	    
	    final DereferenceClient dereferenceClient = spy(new DereferenceClient("dummyId"));
	    dereferenceClient.setRestTemplate(restTemplate);

	    EnrichmentResultList res = dereferenceClient.dereference("http://dummy");
	    
	    verify(restTemplate, times(1)).exchange(any(URI.class),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(EnrichmentResultList.class));
	    
	    Assert.assertEquals(res, result.getBody());
	}
}
