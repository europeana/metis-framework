package eu.europeana.metis.dereference.rest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.transform.TransformerException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import eu.europeana.enrichment.api.external.model.Agent;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.enrichment.api.external.model.Label;
import eu.europeana.metis.dereference.rest.exceptions.RestResponseExceptionHandler;
import eu.europeana.metis.dereference.service.DereferenceService;

public class DereferencingControllerTest {
  private DereferenceService dereferenceServiceMock;
  private MockMvc dereferencingControllerMock;
  private Map<String, String> namespaceMap;

  @Before
  public void setUp() throws Exception {
    dereferenceServiceMock = mock(DereferenceService.class);

    namespaceMap = getNamespaceMap();
    DereferencingController dereferenceController = new DereferencingController(dereferenceServiceMock);
    dereferencingControllerMock = MockMvcBuilders.standaloneSetup(dereferenceController)
        .setControllerAdvice(new RestResponseExceptionHandler())
        .build();
  }

  @Test
  public void dereferenceGet_outputXML() throws Exception {
    EnrichmentResultList list = new EnrichmentResultList();
    list.getResult().add(getAgent("http://www.fennek-it.nl"));
    when(dereferenceServiceMock.dereference("http://www.fennek-it.nl")).thenReturn(list);

    dereferencingControllerMock.perform(get("/dereference/?uri=http://www.fennek-it.nl")
      .accept(MediaType.APPLICATION_XML_VALUE))
        .andExpect(status().is(200))
      //  .andExpect(content().string(""))
        .andExpect(xpath("metis:results/edm:Agent/@rdf:about", namespaceMap).string("http://www.fennek-it.nl"))
        .andExpect(xpath("metis:results/edm:Agent/skos:altLabel[@xml:lang='en']", namespaceMap).string("labelEn"))
        .andExpect(xpath("metis:results/edm:Agent/skos:altLabel[@xml:lang='nl']", namespaceMap).string("labelNl"))
        .andExpect(xpath("metis:results/edm:Agent/rdaGr2:dateOfBirth[@xml:lang='en']", namespaceMap).string("10-10-10"));
  }

  @Test
  public void dereferencePost_outputXML() throws Exception {
    EnrichmentResultList list = new EnrichmentResultList();
    list.getResult().add(getAgent("http://www.fennek-it.nl"));
    when(dereferenceServiceMock.dereference("http://www.fennek-it.nl")).thenReturn(list);

    dereferencingControllerMock.perform(post("/dereference")
        .accept(MediaType.APPLICATION_XML_VALUE)
        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
        .content("[ \"http://www.fennek-it.nl\" ]"))
        .andExpect(status().is(200))
        //  .andExpect(content().string(""))
        .andExpect(xpath("metis:results/edm:Agent/@rdf:about", namespaceMap).string("http://www.fennek-it.nl"))
        .andExpect(xpath("metis:results/edm:Agent/skos:altLabel[@xml:lang='en']", namespaceMap).string("labelEn"))
        .andExpect(xpath("metis:results/edm:Agent/skos:altLabel[@xml:lang='nl']", namespaceMap).string("labelNl"))
        .andExpect(xpath("metis:results/edm:Agent/rdaGr2:dateOfBirth[@xml:lang='en']", namespaceMap).string("10-10-10"));
  }

  @Test
  public void exceptionHandling() throws Exception {
    when(dereferenceServiceMock.dereference("http://www.fennek-it.nl")).thenThrow(new TransformerException("myException"));
    dereferencingControllerMock.perform(post("/dereference")
        .content("[ \"http://www.fennek-it.nl\" ]")
        .accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
        .andExpect(status().is(500))
        .andExpect(content().string("{\"errorMessage\":\"Dereferencing failed for uri: http://www.fennek-it.nl with root cause: myException\"}"));
  }

  private Agent getAgent(String uri) {
    Agent agent = new Agent();
    agent.setAbout(uri);
    List<Label> altLabels = new ArrayList<>();
    altLabels.add(new Label("en", "labelEn"));
    altLabels.add(new Label("nl", "labelNl"));

    agent.setAltLabelList(altLabels);
    List<Label> dob = new ArrayList<>();
    dob.add(new Label("en", "10-10-10"));
    agent.setDateOfBirth(dob);

    return agent;
  }

  private Map<String, String> getNamespaceMap() {
    Map<String, String> namespaceMap = new HashMap<>();
    namespaceMap.put("metis", "http://www.europeana.eu/schemas/metis");
    namespaceMap.put("edm", "http://www.europeana.eu/schemas/edm/");
    namespaceMap.put("skos", "http://www.w3.org/2004/02/skos/core#");
    namespaceMap.put("dcterms", "http://purl.org/dc/terms/");
    namespaceMap.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
    namespaceMap.put("rdfs", "http://www.w3.org/2000/01/rdf-schema");
    namespaceMap.put("cc", "http://creativecommons.org/ns");
    namespaceMap.put("foaf", "http://xmlns.com/foaf/0.1/");
    namespaceMap.put("wgs84_pos", "http://www.w3.org/2003/01/geo/wgs84_pos#");
    namespaceMap.put("owl", "http://www.w3.org/2002/07/owl#");
    namespaceMap.put("xml", "http://www.w3.org/XML/1998/namespace");
    namespaceMap.put("dc", "http://purl.org/dc/elements/1.1/");
    namespaceMap.put("rdaGr2", "http://RDVocab.info/ElementsGr2/");
    return namespaceMap;
  }
}