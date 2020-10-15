package eu.europeana.enrichment.rest.client.dereference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import eu.europeana.enrichment.api.external.model.Agent;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentBaseWrapper;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.enrichment.rest.client.dereference.DereferenceClient;
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
import org.springframework.web.client.RestTemplate;

class DereferenceClientTest {

  @Test
  void testDereference() throws JAXBException {
    Agent agent1 = new Agent();
    agent1.setAbout("Test Agent 1");

    Agent agent2 = new Agent();
    agent2.setAbout("Test Agent 2");

    ArrayList<EnrichmentBase> agentList = new ArrayList<>();
    agentList.add(agent1);
    agentList.add(agent2);
    final List<EnrichmentBaseWrapper> enrichmentBaseWrapperList = EnrichmentBaseWrapper
        .createNullOriginalFieldEnrichmentBaseWrapperList(agentList);

    EnrichmentResultList enrichmentResultList = new EnrichmentResultList(enrichmentBaseWrapperList);

    final JAXBContext jaxbContext = JAXBContext.newInstance(EnrichmentResultList.class);
    final Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
    jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    jaxbMarshaller.marshal(enrichmentResultList, outputStream);
    ResponseEntity<byte[]> result = new ResponseEntity<>(outputStream.toByteArray(), HttpStatus.OK);

    final RestTemplate restTemplate = mock(RestTemplate.class);
    doReturn(result).when(restTemplate).exchange(any(URI.class),
        any(HttpMethod.class),
        any(HttpEntity.class),
        eq(byte[].class));

    final DereferenceClient dereferenceClient = spy(new DereferenceClient(restTemplate, "http://dummy"));
    EnrichmentResultList res = dereferenceClient.dereference("http://dummy");

    verify(restTemplate, times(1)).exchange(any(URI.class),
        eq(HttpMethod.GET),
        any(HttpEntity.class),
        eq(byte[].class));

    assertEquals(res.getEnrichmentBaseWrapperList().get(0).getEnrichmentBase().getAbout(),
        agent1.getAbout());
    assertEquals(res.getEnrichmentBaseWrapperList().get(1).getEnrichmentBase().getAbout(),
        agent2.getAbout());
  }
}
