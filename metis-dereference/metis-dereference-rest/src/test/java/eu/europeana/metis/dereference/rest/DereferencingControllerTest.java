package eu.europeana.metis.dereference.rest;

import static java.util.Map.entry;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

import eu.europeana.enrichment.api.external.DereferenceResultStatus;
import eu.europeana.enrichment.api.external.model.Agent;
import eu.europeana.enrichment.api.external.model.Label;
import eu.europeana.metis.dereference.DereferenceResult;
import eu.europeana.metis.dereference.rest.controller.DereferencingController;
import eu.europeana.metis.dereference.rest.exceptions.RestResponseExceptionHandler;
import eu.europeana.metis.dereference.service.DereferenceService;
import eu.europeana.metis.utils.RestEndpoints;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Unit test {@link DereferencingController} Class
 */
class DereferencingControllerTest {

  private DereferenceService dereferenceServiceMock;
  private MockMvc dereferencingControllerMock;
  private Map<String, String> namespaceMap;

  @BeforeEach
  void setUp() {
    dereferenceServiceMock = mock(DereferenceService.class);

    namespaceMap = getNamespaceMap();
    DereferencingController dereferenceController = new DereferencingController(dereferenceServiceMock);
    dereferencingControllerMock = MockMvcBuilders.standaloneSetup(dereferenceController)
                                                 .setControllerAdvice(new RestResponseExceptionHandler()).build();
  }

  private Map<String, String> getNamespaceMap() {
    return Map.ofEntries(
        entry("metis", "http://www.europeana.eu/schemas/metis"),
        entry("edm", "http://www.europeana.eu/schemas/edm/"),
        entry("skos", "http://www.w3.org/2004/02/skos/core#"),
        entry("dcterms", "http://purl.org/dc/terms/"),
        entry("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#"),
        entry("rdfs", "http://www.w3.org/2000/01/rdf-schema"),
        entry("cc", "http://creativecommons.org/ns"),
        entry("foaf", "http://xmlns.com/foaf/0.1/"),
        entry("wgs84_pos", "http://www.w3.org/2003/01/geo/wgs84_pos#"),
        entry("owl", "http://www.w3.org/2002/07/owl#"),
        entry("xml", "http://www.w3.org/XML/1998/namespace"),
        entry("dc", "http://purl.org/dc/elements/1.1/"),
        entry("rdaGr2", "http://rdvocab.info/ElementsGr2/")
    );
  }

  @Test
  void dereferenceGet_outputXML_expectSuccess() throws Exception {
    when(dereferenceServiceMock.dereference("http://www.example.com")).thenReturn(
        new DereferenceResult(Collections.singletonList(getAgent("http://www.example.com")), DereferenceResultStatus.SUCCESS));

    dereferencingControllerMock.perform(MockMvcRequestBuilders.get(RestEndpoints.DEREFERENCE + "/?uri=http://www.example.com")
                                                              .accept(MediaType.APPLICATION_XML_VALUE))
                               .andExpect(status().is(200)).andExpect(
                                   xpath("metis:results/metis:result/edm:Agent/@rdf:about", namespaceMap).string("http://www.example.com")).andExpect(
                                   xpath("metis:results/metis:result/edm:Agent/skos:altLabel[@xml:lang='en']", namespaceMap).string("labelEn")).andExpect(
                                   xpath("metis:results/metis:result/edm:Agent/skos:altLabel[@xml:lang='nl']", namespaceMap).string("labelNl")).andExpect(
                                   xpath("metis:results/metis:result/edm:Agent/rdaGr2:dateOfBirth[@xml:lang='en']", namespaceMap).string("10-10-10"))
                               .andExpect(
                                   xpath("//metis:results/metis:result/metis:dereferenceStatus", namespaceMap).string("SUCCESS"));
  }

  @Test
  void dereferenceGet_outputXML_expectInternalServerError() throws Exception {
    when(dereferenceServiceMock.dereference("http://www.example.com")).thenThrow(new RuntimeException("Error reason"));

    dereferencingControllerMock.perform(MockMvcRequestBuilders.get(RestEndpoints.DEREFERENCE + "/?uri=http://www.example.com")
                                                              .accept(MediaType.APPLICATION_XML_VALUE)).andDo(print())
                               .andExpect(status().is(500)).andExpect(xpath("//error").exists())
                               .andExpect(xpath("//error/errorMessage").exists()).andExpect(xpath("//error/errorMessage").string(
                                   "Dereferencing failed for uri: http://www.example.com with root cause: Error reason"));
  }

  @Test
  void dereferencePost_outputXML_expectSuccess() throws Exception {
    when(dereferenceServiceMock.dereference("http://www.example.com")).thenReturn(
        new DereferenceResult(Collections.singletonList(getAgent("http://www.example.com")), DereferenceResultStatus.SUCCESS));

    dereferencingControllerMock.perform(
                                   MockMvcRequestBuilders.post(RestEndpoints.DEREFERENCE).accept(MediaType.APPLICATION_XML_VALUE)
                                                         .contentType(MediaType.APPLICATION_JSON).content("[ \"http://www.example.com\" ]")).andDo(print())
                               .andExpect(status().is(200)).andExpect(
                                   xpath("metis:results/metis:result/edm:Agent/@rdf:about", namespaceMap).string("http://www.example.com")).andExpect(
                                   xpath("metis:results/metis:result/edm:Agent/skos:altLabel[@xml:lang='en']", namespaceMap).string("labelEn")).andExpect(
                                   xpath("metis:results/metis:result/edm:Agent/skos:altLabel[@xml:lang='nl']", namespaceMap).string("labelNl")).andExpect(
                                   xpath("metis:results/metis:result/edm:Agent/rdaGr2:dateOfBirth[@xml:lang='en']", namespaceMap).string("10-10-10"))
                               .andExpect(
                                   xpath("//metis:results/metis:result/metis:dereferenceStatus", namespaceMap).string("SUCCESS"));
  }

  @Test
  void dereferencePost_outputXML_expectEmptyList_invalid_url() throws Exception {
    when(dereferenceServiceMock.dereference("http://www.example.com")).thenReturn(
        new DereferenceResult(DereferenceResultStatus.INVALID_URL));

    dereferencingControllerMock.perform(
                                   MockMvcRequestBuilders.post(RestEndpoints.DEREFERENCE).accept(MediaType.APPLICATION_XML_VALUE)
                                                         .contentType(MediaType.APPLICATION_JSON).content("[ \"http://www.example.com\" ]")).andDo(print())
                               .andExpect(status().is(200)).andExpect(xpath("//metis:results", namespaceMap).exists())
                               .andExpect(xpath("//metis:results", namespaceMap).nodeCount(1))
                               .andExpect(xpath("//metis:results/metis:result", namespaceMap).exists())
                               .andExpect(xpath("//metis:results/metis:result/metis:dereferenceStatus", namespaceMap).exists())
                               .andExpect(xpath("//metis:results/metis:result/metis:dereferenceStatus", namespaceMap).string(
                                   "INVALID_URL"));
  }

  @Test
  void exceptionHandling() throws Exception {
    when(dereferenceServiceMock.dereference("http://www.example.com")).thenThrow(new RuntimeException("myException"));
    dereferencingControllerMock.perform(
                                   MockMvcRequestBuilders.post(RestEndpoints.DEREFERENCE).content("[ \"http://www.example.com\" ]")
                                                         .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
                               .andExpect(status().is(500)).andExpect(content().string(
                                   "{\"errorMessage\":\"Dereferencing failed for uri: http://www.example.com with root cause: myException\"}"));
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
}
