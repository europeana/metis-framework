package eu.europeana.enrichment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.corelib.solr.entity.ConceptImpl;
import eu.europeana.corelib.solr.entity.OrganizationImpl;
import eu.europeana.corelib.solr.entity.PlaceImpl;
import eu.europeana.corelib.solr.entity.TimespanImpl;
import eu.europeana.enrichment.api.external.model.Agent;
import eu.europeana.enrichment.api.external.model.Concept;
import eu.europeana.enrichment.api.external.model.Place;
import eu.europeana.enrichment.api.external.model.Timespan;
import eu.europeana.enrichment.api.internal.AgentTermList;
import eu.europeana.enrichment.api.internal.ConceptTermList;
import eu.europeana.enrichment.api.internal.MongoTermList;
import eu.europeana.enrichment.api.internal.OrganizationTermList;
import eu.europeana.enrichment.api.internal.PlaceTermList;
import eu.europeana.enrichment.api.internal.TimespanTermList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

public class ConverterTest {

  @Test
  public void convertAgent() {

    AgentImpl agentImpl = new AgentImpl();
    agentImpl.setId(ObjectId.get());
    agentImpl.setAbout("myAbout");
    agentImpl.setEdmWasPresentAt(new String[]{"a", "b"});
    agentImpl.setOwlSameAs(new String[]{"1", "2"});
    agentImpl.setAltLabel(createAltLabels("en", new String[]{"a_en", "b_en"}));

    final MongoTermList<AgentImpl> agentTermList = new AgentTermList();
    agentTermList.setRepresentation(agentImpl);
    agentTermList.setEntityType(AgentImpl.class.getSimpleName());

    Agent agent = (Agent) Converter.convert(agentTermList);
    assertEquals("myAbout", agent.getAbout());
    assertEquals("a", agent.getWasPresentAt().get(0).getResource());
    assertEquals("b", agent.getWasPresentAt().get(1).getResource());
    assertEquals("1", agent.getSameAs().get(0).getResource());
    assertEquals("2", agent.getSameAs().get(1).getResource());
    assertEquals("a_en",
        agent.getAltLabelList().stream().filter(x -> x.getValue().equals("a_en")).findFirst().get()
            .getValue());
    assertEquals("en",
        agent.getAltLabelList().stream().filter(x -> x.getValue().equals("a_en")).findFirst().get()
            .getLang());
    assertEquals("b_en",
        agent.getAltLabelList().stream().filter(x -> x.getValue().equals("b_en")).findFirst().get()
            .getValue());
    assertEquals("en",
        agent.getAltLabelList().stream().filter(x -> x.getValue().equals("b_en")).findFirst().get()
            .getLang());
  }

  private Map<String, List<String>> createAltLabels(String en, String[] strings) {
    Map<String, List<String>> map = new HashMap<>();
    map.put(en, Arrays.asList(strings));
    return map;
  }

  @Test
  public void convertConcept() {
    ConceptImpl conceptImpl = new ConceptImpl();
    conceptImpl.setId(ObjectId.get());
    conceptImpl.setAbout("myAbout");
    conceptImpl.setRelated(new String[]{"a", "b"});
    conceptImpl.setRelatedMatch(new String[]{"1", "2"});

    final MongoTermList<ConceptImpl> conceptTermList = new ConceptTermList();
    conceptTermList.setRepresentation(conceptImpl);
    conceptTermList.setEntityType(ConceptImpl.class.getSimpleName());

    Concept agent = (Concept) Converter.convert(conceptTermList);
    assertEquals("myAbout", agent.getAbout());
    assertEquals("a", agent.getRelated().get(0).getResource());
    assertEquals("b", agent.getRelated().get(1).getResource());
    assertEquals("1", agent.getRelatedMatch().get(0).getResource());
    assertEquals("2", agent.getRelatedMatch().get(1).getResource());

  }

  @Test
  public void convertTimespan() {
    TimespanImpl timespanImpl = new TimespanImpl();
    timespanImpl.setId(ObjectId.get());
    timespanImpl.setAbout("myAbout");
    timespanImpl.setOwlSameAs(new String[]{"1", "2"});

    final MongoTermList<TimespanImpl> timespanTermList = new TimespanTermList();
    timespanTermList.setRepresentation(timespanImpl);
    timespanTermList.setEntityType(TimespanImpl.class.getSimpleName());

    Timespan agent = (Timespan) Converter.convert(timespanTermList);
    assertEquals("myAbout", agent.getAbout());
    assertEquals("1", agent.getSameAs().get(0).getResource());
    assertEquals("2", agent.getSameAs().get(1).getResource());
  }

  @Test
  public void convertPlace() {
    PlaceImpl placeImpl = new PlaceImpl();
    placeImpl.setId(ObjectId.get());
    placeImpl.setAbout("myAbout");
    placeImpl.setOwlSameAs(new String[]{"1", "2"});

    final MongoTermList<PlaceImpl> placeTermList = new PlaceTermList();
    placeTermList.setRepresentation(placeImpl);
    placeTermList.setEntityType(PlaceImpl.class.getSimpleName());

    Place agent = (Place) Converter.convert(placeTermList);
    assertEquals("myAbout", agent.getAbout());
    assertEquals("1", agent.getSameAs().get(0).getResource());
    assertEquals("2", agent.getSameAs().get(1).getResource());
  }

  @Test
  public void convertOtherObject_returns_null() {
    final OrganizationImpl organizationImpl = new OrganizationImpl();
    organizationImpl.setId(ObjectId.get());
    organizationImpl.setAbout("myAbout");
    organizationImpl.setOwlSameAs(new String[]{"1", "2"});

    final MongoTermList<OrganizationImpl> organizationTermList = new OrganizationTermList();
    organizationTermList.setRepresentation(organizationImpl);
    organizationTermList.setEntityType(OrganizationImpl.class.getSimpleName());

    Place agent = (Place) Converter.convert(organizationTermList);
    assertNull(agent);
  }
}