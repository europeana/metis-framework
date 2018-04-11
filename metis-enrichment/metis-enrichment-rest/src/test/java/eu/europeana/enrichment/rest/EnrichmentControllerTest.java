package eu.europeana.enrichment.rest;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

import eu.europeana.enrichment.api.external.EntityWrapper;
import eu.europeana.enrichment.api.external.model.Agent;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.enrichment.api.external.model.Label;
import eu.europeana.enrichment.rest.exception.RestResponseExceptionHandler;
import eu.europeana.enrichment.service.Converter;
import eu.europeana.enrichment.service.Enricher;
import eu.europeana.enrichment.service.EntityRemover;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class EnrichmentControllerTest {
  private MockMvc enrichmentControllerMock;
  private Enricher enrichmerMock;
  private EntityRemover entityRemoverMock;
  private Converter converterMock;
  private EnrichmentController enrichmentController;

  @Before
  public void setUp() throws Exception {
    enrichmerMock = mock(Enricher.class);
    entityRemoverMock = mock(EntityRemover.class);
    converterMock = mock(Converter.class);

    enrichmentController = new EnrichmentController(enrichmerMock, entityRemoverMock, converterMock);
    enrichmentControllerMock = MockMvcBuilders.standaloneSetup(enrichmentController)
        .setControllerAdvice(new RestResponseExceptionHandler())
        .build();
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testDelete() throws Exception {
    enrichmentControllerMock.perform(delete("/delete")
        .accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
        .content("{\"uris\":[\"myUri\"]}"))
        .andExpect(status().is(200))
        .andExpect(content().string(""));

    ArgumentCaptor<List> argumentCaptor = ArgumentCaptor.forClass(List.class);
    verify(entityRemoverMock,times(1)).remove(argumentCaptor.capture());
    assertEquals("myUri", argumentCaptor.getValue().get(0));
  }

  @Test
  public void getByUri_JSON() throws Exception {
    EntityWrapper wrapper = new EntityWrapper();
    String uri = "http://www.fennek-it.nl";
    when(enrichmerMock.getByUri(uri)).thenReturn(wrapper);

    Agent agent = getAgent(uri);
    when(converterMock.convert(wrapper)).thenReturn(agent);
    enrichmentControllerMock.perform(get("/getByUri")
        .param("uri", "http://www.fennek-it.nl")
        .accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
        .andExpect(status().is(200))
        .andExpect(jsonPath("$.about", is("http://www.fennek-it.nl")))
        .andExpect(jsonPath("$.altLabelList[?(@.lang=='en')].value", containsInAnyOrder("labelEn")))
        .andExpect(jsonPath("$.altLabelList[?(@.lang=='nl')].value", containsInAnyOrder("labelNl")));
  }

  @Test
  public void getByUri_JSON_throwsException() throws Exception {
    EntityWrapper wrapper = new EntityWrapper();
    String uri = "http://www.fennek-it.nl";
    when(enrichmerMock.getByUri(uri)).thenReturn(wrapper);

    Agent agent = getAgent(uri);
    when(converterMock.convert(wrapper)).thenThrow(new IOException("MyException"));
    enrichmentControllerMock.perform(get("/getByUri")
        .param("uri", "http://www.fennek-it.nl")
        .accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
        .andExpect(status().is(400))
        .andExpect(jsonPath("$.errorMessage", is("Error converting object to EnrichmentBase")));
  }

  @Test
  public void getByUri_XML() throws Exception {
    EntityWrapper wrapper = new EntityWrapper();
    String uri = "http://www.fennek-it.nl";
    when(enrichmerMock.getByUri(uri)).thenReturn(wrapper);
    Agent agent = getAgent(uri);
    Map<String, String> namespaceMap = getNamespaceMap();
    when(converterMock.convert(wrapper)).thenReturn(agent);
    enrichmentControllerMock.perform(get("/getByUri")
        .param("uri", "http://www.fennek-it.nl")
        .accept(MediaType.APPLICATION_XML_VALUE))
        .andExpect(status().is(200))
        .andExpect(xpath("edm:Agent/@rdf:about", namespaceMap).string("http://www.fennek-it.nl"))
        .andExpect(xpath("edm:Agent/skos:altLabel[@xml:lang='en']", namespaceMap).string("labelEn"))
        .andExpect(
            xpath("edm:Agent/skos:altLabel[@xml:lang='nl']", namespaceMap).string("labelNl"));
  }

  @Test
  public void enrich_XML() throws Exception {
    String body =
          "{\n"
        + "  \"inputValueList\": [\n"
        + "    {\n"
        + "      \"value\": \"Music\",\n"
        + "      \"vocabularies\": [\n"
        + "        \"CONCEPT\"\n"
        + "      ]\n"
        + "    }\n"
        + "  ]\n"
        + "}";

    Map<String, String> namespaceMap = getNamespaceMap();

    EnrichmentResultList enrichmentResultList = new EnrichmentResultList();
    enrichmentResultList.getResult().add(getAgent("http://www.fennek-it.nl"));

    when(converterMock.convert(anyList())).thenReturn(enrichmentResultList);

    enrichmentControllerMock.perform(post("/enrich")
        .content(body)
        .accept(MediaType.APPLICATION_XML_VALUE)
        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
        .andExpect(status().is(200))
        .andExpect(xpath("metis:results/edm:Agent/@rdf:about", namespaceMap).string("http://www.fennek-it.nl"))
        .andExpect(xpath("metis:results/edm:Agent/skos:altLabel[@xml:lang='en']", namespaceMap).string("labelEn"))
        .andExpect(xpath("metis:results/edm:Agent/skos:altLabel[@xml:lang='nl']", namespaceMap).string("labelNl"))
        .andExpect(xpath("metis:results/edm:Agent/rdaGr2:dateOfBirth[@xml:lang='en']", namespaceMap).string("10-10-10"));
  }

  @Test
  public void enrich_throwsException() throws Exception {
    String body =
        "{\n"
            + "  \"inputValueList\": [\n"
            + "    {\n"
            + "      \"value\": \"Music\",\n"
            + "      \"vocabularies\": [\n"
            + "        \"CONCEPT\"\n"
            + "      ]\n"
            + "    }\n"
            + "  ]\n"
            + "}";

    when(converterMock.convert(anyList())).thenThrow(new IOException("myException"));
    enrichmentControllerMock.perform(post("/enrich")
        .content(body)
        .accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
        .andExpect(status().is(400))
        .andExpect(jsonPath("$.errorMessage", is("Error converting object.")));
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