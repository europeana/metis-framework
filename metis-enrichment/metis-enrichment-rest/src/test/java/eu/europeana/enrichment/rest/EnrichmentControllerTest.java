package eu.europeana.enrichment.rest;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import eu.europeana.enrichment.api.external.EnrichmentReference;
import eu.europeana.enrichment.api.external.EnrichmentSearch;
import eu.europeana.enrichment.api.external.ReferenceValue;
import eu.europeana.enrichment.api.external.SearchValue;
import eu.europeana.enrichment.api.external.model.Agent;
import eu.europeana.enrichment.api.external.model.Concept;
import eu.europeana.enrichment.api.external.model.EnrichmentResultBaseWrapper;
import eu.europeana.enrichment.api.external.model.Label;
import eu.europeana.enrichment.api.external.model.Part;
import eu.europeana.enrichment.api.external.model.Place;
import eu.europeana.enrichment.api.external.model.Resource;
import eu.europeana.enrichment.api.external.model.Timespan;
import eu.europeana.enrichment.rest.exception.RestResponseExceptionHandler;
import eu.europeana.enrichment.service.EnrichmentService;
import eu.europeana.enrichment.utils.EntityType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
  private static Map<String, String> namespaceMap;

  @BeforeAll
  public static void setUp() {
    namespaceMap = getNamespaceMap();
    enrichmentServiceMock = mock(EnrichmentService.class);

    EnrichmentController enrichmentController = new EnrichmentController(enrichmentServiceMock);
    enrichmentControllerMock = MockMvcBuilders.standaloneSetup(enrichmentController)
        .setControllerAdvice(new RestResponseExceptionHandler()).build();
  }

  @AfterEach
  public void tearDown() {
    Mockito.reset(enrichmentServiceMock);
  }

  @Test
  public void testEquivalenceInputURIInputJSON() throws Exception {
    final Agent agent = getAgent();
    final EnrichmentReference enrichmentReference = new EnrichmentReference();
    final ReferenceValue referenceValue = new ReferenceValue(agent.getAbout(),
        Collections.emptySet());
    enrichmentReference.setReferenceValues(List.of(referenceValue));
    when(enrichmentServiceMock.enrichByEquivalenceValues(referenceValue))
        .thenReturn(List.of(agent));
    enrichmentControllerMock.perform(
        get("/enrich/entity/equivalence").param("uri", agent.getAbout())
            .accept(MediaType.APPLICATION_JSON)).andExpect(status().is(200))
        .andExpect(jsonPath("$.about", is(agent.getAbout())))
        .andExpect(jsonPath("$.altLabelList[?(@.lang=='en')].value", containsInAnyOrder("labelEn")))
        .andExpect(
            jsonPath("$.altLabelList[?(@.lang=='nl')].value", containsInAnyOrder("labelNl")));
  }

  @Test
  public void testEquivalenceInputURIInputXML() throws Exception {
    final Agent agent = getAgent();
    final EnrichmentReference enrichmentReference = new EnrichmentReference();
    final ReferenceValue referenceValue = new ReferenceValue(agent.getAbout(),
        Collections.emptySet());
    enrichmentReference.setReferenceValues(List.of(referenceValue));

    when(enrichmentServiceMock.enrichByEquivalenceValues(referenceValue))
        .thenReturn(List.of(agent));
    enrichmentControllerMock.perform(
        get("/enrich/entity/equivalence").param("uri", agent.getAbout())
            .accept(MediaType.APPLICATION_XML_VALUE)).andExpect(status().is(200))
        .andExpect(xpath("edm:Agent/@rdf:about", namespaceMap).string(agent.getAbout()))
        .andExpect(xpath("edm:Agent/skos:altLabel[@xml:lang='en']", namespaceMap).string("labelEn"))
        .andExpect(
            xpath("edm:Agent/skos:altLabel[@xml:lang='nl']", namespaceMap).string("labelNl"));
  }

  @Test
  public void testEquivalenceWithEnrichmentReference() throws Exception {
    final Agent agent = getAgent();
    final EnrichmentReference enrichmentReference = new EnrichmentReference();
    final ReferenceValue referenceValue = new ReferenceValue(agent.getAbout(),
        Set.of(EntityType.AGENT));
    enrichmentReference.setReferenceValues(List.of(referenceValue));

    String requestJson = convertToJson(enrichmentReference);

    when(enrichmentServiceMock.enrichByEquivalenceValues(referenceValue))
        .thenReturn(List.of(agent));
    enrichmentControllerMock.perform(post("/enrich/entity/equivalence").content(requestJson)
        .accept(MediaType.APPLICATION_XML_VALUE).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is(200)).andExpect(
        xpath("metis:results/metis:result/edm:Agent/@rdf:about", namespaceMap)
            .string(agent.getAbout())).andExpect(
        xpath("metis:results/metis:result/edm:Agent/skos:altLabel[@xml:lang='en']", namespaceMap)
            .string("labelEn")).andExpect(
        xpath("metis:results/metis:result/edm:Agent/skos:altLabel[@xml:lang='nl']", namespaceMap)
            .string("labelNl")).andExpect(
        xpath("metis:results/metis:result/edm:Agent/rdaGr2:dateOfBirth[@xml:lang='en']",
            namespaceMap).string("10-10-10"));
  }

  @Test
  public void testEntityId() throws Exception {
    final Agent agent = getAgent();
    String body = "[\"" + agent.getAbout() + "\"]";

    when(enrichmentServiceMock.enrichById(agent.getAbout())).thenReturn(agent);
    enrichmentControllerMock.perform(
        post("/enrich/entity/id").content(body).accept(MediaType.APPLICATION_XML_VALUE)
            .contentType(MediaType.APPLICATION_JSON)).andExpect(status().is(200)).andExpect(
        xpath("metis:results/metis:result/edm:Agent/@rdf:about", namespaceMap)
            .string(agent.getAbout())).andExpect(
        xpath("metis:results/metis:result/edm:Agent/skos:altLabel[@xml:lang='en']", namespaceMap)
            .string("labelEn")).andExpect(
        xpath("metis:results/metis:result/edm:Agent/skos:altLabel[@xml:lang='nl']", namespaceMap)
            .string("labelNl")).andExpect(
        xpath("metis:results/metis:result/edm:Agent/rdaGr2:dateOfBirth[@xml:lang='en']",
            namespaceMap).string("10-10-10"));
  }

  @Test
  public void testSearchInputXML_Agent() throws Exception {
    final Agent agent = getAgent();
    final EnrichmentSearch enrichmentSearch = new EnrichmentSearch();
    final SearchValue searchValue = new SearchValue("value", null, EntityType.AGENT);
    enrichmentSearch.setSearchValues(List.of(searchValue));
    String requestJson = convertToJson(enrichmentSearch);

    when(enrichmentServiceMock.enrichByEnrichmentSearchValues(List.of(searchValue)))
        .thenReturn(List.of(new EnrichmentResultBaseWrapper(List.of(agent))));

    enrichmentControllerMock.perform(
        post("/enrich/entity/search").content(requestJson).accept(MediaType.APPLICATION_XML_VALUE)
            .contentType(MediaType.APPLICATION_JSON)).andExpect(status().is(200)).andExpect(
        xpath("metis:results/metis:result/edm:Agent/@rdf:about", namespaceMap)
            .string(agent.getAbout())).andExpect(
        xpath("metis:results/metis:result/edm:Agent/skos:altLabel[@xml:lang='en']", namespaceMap)
            .string("labelEn")).andExpect(
        xpath("metis:results/metis:result/edm:Agent/skos:altLabel[@xml:lang='nl']", namespaceMap)
            .string("labelNl")).andExpect(
        xpath("metis:results/metis:result/edm:Agent/rdaGr2:dateOfBirth[@xml:lang='en']",
            namespaceMap).string("10-10-10"));
  }

  @Test
  public void testSearchInputXML_Concept() throws Exception {
    final Concept concept = getConcept();
    final EnrichmentSearch enrichmentSearch = new EnrichmentSearch();
    final SearchValue searchValue = new SearchValue("value", null, EntityType.CONCEPT);
    enrichmentSearch.setSearchValues(List.of(searchValue));
    String requestJson = convertToJson(enrichmentSearch);

    when(enrichmentServiceMock.enrichByEnrichmentSearchValues(List.of(searchValue)))
        .thenReturn(List.of(new EnrichmentResultBaseWrapper(List.of(concept))));

    enrichmentControllerMock.perform(
        post("/enrich/entity/search").content(requestJson).accept(MediaType.APPLICATION_XML_VALUE)
            .contentType(MediaType.APPLICATION_JSON)).andExpect(status().is(200)).andExpect(
        xpath("metis:results/metis:result/skos:Concept/@rdf:about", namespaceMap)
            .string(concept.getAbout())).andExpect(
        xpath("metis:results/metis:result/skos:Concept/skos:altLabel[@xml:lang='en']", namespaceMap)
            .string("labelEn")).andExpect(
        xpath("metis:results/metis:result/skos:Concept/skos:altLabel[@xml:lang='nl']", namespaceMap)
            .string("labelNl")).andExpect(
        xpath("metis:results/metis:result/skos:Concept/skos:broader/@rdf:resource", namespaceMap)
            .string(concept.getBroader().get(0).getResource()));
  }

  @Test
  public void testSearchInputXML_Timespan() throws Exception {
    final Timespan timespan = getTimespan();
    final EnrichmentSearch enrichmentSearch = new EnrichmentSearch();
    final SearchValue searchValue = new SearchValue("value", null, EntityType.TIMESPAN);
    enrichmentSearch.setSearchValues(List.of(searchValue));
    String requestJson = convertToJson(enrichmentSearch);

    when(enrichmentServiceMock.enrichByEnrichmentSearchValues(List.of(searchValue)))
        .thenReturn(List.of(new EnrichmentResultBaseWrapper(List.of(timespan))));

    enrichmentControllerMock.perform(
        post("/enrich/entity/search").content(requestJson).accept(MediaType.APPLICATION_XML_VALUE)
            .contentType(MediaType.APPLICATION_JSON)).andExpect(status().is(200)).andExpect(
        xpath("metis:results/metis:result/edm:Timespan/@rdf:about", namespaceMap)
            .string(timespan.getAbout())).andExpect(
        xpath("metis:results/metis:result/edm:Timespan/skos:altLabel[@xml:lang='en']", namespaceMap)
            .string("labelEn")).andExpect(
        xpath("metis:results/metis:result/edm:Timespan/skos:altLabel[@xml:lang='nl']", namespaceMap)
            .string("labelNl")).andExpect(
        xpath("metis:results/metis:result/edm:Timespan/dcterms:isPartOf/@rdf:resource", namespaceMap)
            .string(timespan.getIsPartOf().getResource()));
  }

  @Test
  public void testSearchInputXML_Place() throws Exception {
    final Place place = getPlace();
    final EnrichmentSearch enrichmentSearch = new EnrichmentSearch();
    final SearchValue searchValue = new SearchValue("value", null, EntityType.PLACE);
    enrichmentSearch.setSearchValues(List.of(searchValue));
    String requestJson = convertToJson(enrichmentSearch);

    when(enrichmentServiceMock.enrichByEnrichmentSearchValues(List.of(searchValue)))
        .thenReturn(List.of(new EnrichmentResultBaseWrapper(List.of(place))));

    enrichmentControllerMock.perform(
        post("/enrich/entity/search").content(requestJson).accept(MediaType.APPLICATION_XML_VALUE)
            .contentType(MediaType.APPLICATION_JSON)).andExpect(status().is(200)).andExpect(
        xpath("metis:results/metis:result/edm:Place/@rdf:about", namespaceMap)
            .string(place.getAbout())).andExpect(
        xpath("metis:results/metis:result/edm:Place/skos:altLabel[@xml:lang='en']", namespaceMap)
            .string("labelEn")).andExpect(
        xpath("metis:results/metis:result/edm:Place/skos:altLabel[@xml:lang='nl']", namespaceMap)
            .string("labelNl")).andExpect(
        xpath("metis:results/metis:result/edm:Place/dcterms:isPartOf/@rdf:resource", namespaceMap)
            .string(place.getIsPartOf().getResource()));
  }

  private String convertToJson(Object object) throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
    ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
    return ow.writeValueAsString(object);
  }

  private Agent getAgent() {
    Agent agent = new Agent();
    agent.setAbout("http://agent.org");
    List<Label> altLabels = new ArrayList<>();
    altLabels.add(new Label("en", "labelEn"));
    altLabels.add(new Label("nl", "labelNl"));

    agent.setAltLabelList(altLabels);
    agent.setDateOfBirth(List.of(new Label("en", "10-10-10")));

    return agent;
  }

  private Concept getConcept() {
    final Concept concept = new Concept();
    concept.setAbout("http://concept.org");

    List<Label> altLabels = new ArrayList<>();
    altLabels.add(new Label("en", "labelEn"));
    altLabels.add(new Label("nl", "labelNl"));

    concept.setAltLabelList(altLabels);
    concept.setBroader(List.of(new Resource("http://concept_broader.org")));

    return concept;
  }

  private Timespan getTimespan() {
    final Timespan timespan = new Timespan();
    timespan.setAbout("http://timespan.org");

    List<Label> altLabels = new ArrayList<>();
    altLabels.add(new Label("en", "labelEn"));
    altLabels.add(new Label("nl", "labelNl"));

    timespan.setAltLabelList(altLabels);
    timespan.setIsPartOf(new Part("http://timespan_is_part.org"));

    return timespan;
  }

  private Place getPlace() {
    final Place place = new Place();
    place.setAbout("http://place.org");

    List<Label> altLabels = new ArrayList<>();
    altLabels.add(new Label("en", "labelEn"));
    altLabels.add(new Label("nl", "labelNl"));

    place.setAltLabelList(altLabels);
    place.setIsPartOf(new Part("http://place_is_part.org"));

    return place;
  }

  private static Map<String, String> getNamespaceMap() {
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