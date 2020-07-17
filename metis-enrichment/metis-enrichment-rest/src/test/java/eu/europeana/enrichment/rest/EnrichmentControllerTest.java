package eu.europeana.enrichment.rest;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

import eu.europeana.enrichment.api.external.model.Agent;
import eu.europeana.enrichment.api.external.model.Label;
import eu.europeana.enrichment.rest.exception.RestResponseExceptionHandler;
import eu.europeana.enrichment.service.EnrichmentService;
import eu.europeana.enrichment.utils.InputValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class EnrichmentControllerTest {

  private static MockMvc enrichmentControllerMock;
  private static EnrichmentService enrichmentServiceMock;

  @BeforeAll
  public static void setUp() {
    enrichmentServiceMock = mock(EnrichmentService.class);

    EnrichmentController enrichmentController = new EnrichmentController(enrichmentServiceMock);
    enrichmentControllerMock = MockMvcBuilders.standaloneSetup(enrichmentController)
        .setControllerAdvice(new RestResponseExceptionHandler())
        .build();
  }

  @AfterEach
  public void tearDown() {
    Mockito.reset(enrichmentServiceMock);
  }

  @Test
  public void getByUri_JSON() throws Exception {
    String uri = "http://www.example.com";
    Agent agent = getAgent(uri);
    when(enrichmentServiceMock.enrichByCodeUriOrOwlSameAs(uri))
        .thenReturn(Collections.singletonList(agent));
    enrichmentControllerMock.perform(get("/enrich/code_uri_or_owl_same_as")
        .param("uri", "http://www.example.com")
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is(200))
        .andExpect(jsonPath("$.about", is("http://www.example.com")))
        .andExpect(jsonPath("$.altLabelList[?(@.lang=='en')].value", containsInAnyOrder("labelEn")))
        .andExpect(
            jsonPath("$.altLabelList[?(@.lang=='nl')].value", containsInAnyOrder("labelNl")));
  }

  @Test
  public void getByUri_XML() throws Exception {

    String uri = "http://www.example.com";
    Agent agent = getAgent(uri);
    when(enrichmentServiceMock.enrichByCodeUriOrOwlSameAs(uri))
        .thenReturn(Collections.singletonList(agent));
    Map<String, String> namespaceMap = getNamespaceMap();
    enrichmentControllerMock.perform(get("/enrich/code_uri_or_owl_same_as")
        .param("uri", "http://www.example.com")
        .accept(MediaType.APPLICATION_XML_VALUE))
        .andExpect(status().is(200))
        .andExpect(xpath("edm:Agent/@rdf:about", namespaceMap).string("http://www.example.com"))
        .andExpect(xpath("edm:Agent/skos:altLabel[@xml:lang='en']", namespaceMap).string("labelEn"))
        .andExpect(
            xpath("edm:Agent/skos:altLabel[@xml:lang='nl']", namespaceMap).string("labelNl"));
  }

  @Test
  public void enrich_XML() throws Exception {
    String uri = "http://www.example.com";
    String body =
        "{\n"
            + "  \"inputValueList\": [\n"
            + "    {\n"
            + "      \"value\": \"AgentName\",\n"
            + "      \"vocabularies\": [\n"
            + "        \"AGENT\"\n"
            + "      ]\n"
            + "    }\n"
            + "  ]\n"
            + "}";

    Map<String, String> namespaceMap = getNamespaceMap();
    Agent agent = getAgent(uri);

    when(enrichmentServiceMock.enrichByInputValueList(anyListOf(InputValue.class)))
        .thenReturn(Collections.singletonList(new ImmutablePair<>("DC_CONTRIBUTOR", agent)));

    enrichmentControllerMock.perform(post("/enrich/input_value_list")
        .content(body)
        .accept(MediaType.APPLICATION_XML_VALUE)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is(200))
        .andExpect(xpath("metis:results/metis:enrichmentBaseWrapperList/edm:Agent/@rdf:about", namespaceMap)
            .string("http://www.example.com"))
        .andExpect(xpath("metis:results/metis:enrichmentBaseWrapperList/edm:Agent/skos:altLabel[@xml:lang='en']", namespaceMap)
            .string("labelEn"))
        .andExpect(xpath("metis:results/metis:enrichmentBaseWrapperList/edm:Agent/skos:altLabel[@xml:lang='nl']", namespaceMap)
            .string("labelNl"))
        .andExpect(xpath("metis:results/metis:enrichmentBaseWrapperList/edm:Agent/rdaGr2:dateOfBirth[@xml:lang='en']", namespaceMap)
            .string("10-10-10"));
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
    namespaceMap.put("rdaGr2", "http://rdvocab.info/ElementsGr2/");
    return namespaceMap;
  }
}