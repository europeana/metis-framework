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
import eu.europeana.enrichment.api.external.model.Agent;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentBaseWrapper;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

class EnrichmentClientTest {

  @Test
  void testEnrich() throws RestClientException, JAXBException {
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
    final ResponseEntity<byte[]> response = new ResponseEntity<>(marshall(result),
            HttpStatus.OK);
    doReturn(response).when(restTemplate).exchange(eq(ENRICHMENT_ENRICH), eq(HttpMethod.POST),
            any(HttpEntity.class), eq(byte[].class));

    final EnrichmentClient enrichmentClient = spy(new EnrichmentClient(restTemplate, "", 20));
    EnrichmentResultList res = enrichmentClient.enrich(new ArrayList<>());

    verify(restTemplate, times(1)).exchange(eq(ENRICHMENT_ENRICH), eq(HttpMethod.POST),
            any(HttpEntity.class), eq(byte[].class));
    assertEquals(res.getEnrichmentBaseWrapperList().get(0).getEnrichmentBase().getAbout(),
            agent1.getAbout());
    assertEquals(res.getEnrichmentBaseWrapperList().get(1).getEnrichmentBase().getAbout(),
            agent2.getAbout());
  }

  private <T> byte[] marshall(T object) throws JAXBException {
    final JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
    final Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
    final ByteArrayOutputStream output = new ByteArrayOutputStream();
    jaxbMarshaller.marshal(object, output);
    return output.toByteArray();
  }

  @Test
  void testEnrichException() {
    final RestTemplate restTemplate = mock(RestTemplate.class);
    doThrow(new UnknownException("test")).when(restTemplate).exchange(not(eq(ENRICHMENT_ENRICH)),
            eq(HttpMethod.POST), any(HttpEntity.class), eq(byte[].class));

    final EnrichmentClient enrichmentClient = spy(
            new EnrichmentClient(restTemplate, "http://dummy", 20));

    assertThrows(UnknownException.class, () -> enrichmentClient.enrich(new ArrayList<>()));
  }

  @Test
  void testGetByUri() throws RestClientException, JAXBException {
    Agent agent = new Agent();
    agent.setAbout("Test Agent");

    ResponseEntity<byte[]> result = new ResponseEntity<>(marshall(agent), HttpStatus.OK);

    final RestTemplate restTemplate = mock(RestTemplate.class);

    doReturn(result).when(restTemplate).exchange(any(URI.class),
            any(HttpMethod.class),
            any(HttpEntity.class),
            eq(byte[].class));

    final EnrichmentClient enrichmentClient = spy(
            new EnrichmentClient(restTemplate, "http://dummy", 20));

    EnrichmentBase res = enrichmentClient.getByUri("http://test");
    verify(restTemplate, times(1)).exchange(any(URI.class),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(byte[].class));
    assertEquals(res.getAbout(), agent.getAbout());
  }
}
