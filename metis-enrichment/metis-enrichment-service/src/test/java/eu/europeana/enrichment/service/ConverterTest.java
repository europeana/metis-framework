package eu.europeana.enrichment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import eu.europeana.enrichment.api.external.model.Agent;
import eu.europeana.enrichment.api.external.model.Concept;
import eu.europeana.enrichment.api.external.model.Place;
import eu.europeana.enrichment.api.external.model.Timespan;
import eu.europeana.enrichment.internal.model.AgentEnrichmentEntity;
import eu.europeana.enrichment.internal.model.ConceptEnrichmentEntity;
import eu.europeana.enrichment.internal.model.EnrichmentTerm;
import eu.europeana.enrichment.internal.model.OrganizationEnrichmentEntity;
import eu.europeana.enrichment.internal.model.PlaceEnrichmentEntity;
import eu.europeana.enrichment.internal.model.TimespanEnrichmentEntity;
import eu.europeana.enrichment.utils.EntityType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class ConverterTest {

  @Test
  public void convertAgent() {

    AgentEnrichmentEntity agentEntityEnrichment = new AgentEnrichmentEntity();
    agentEntityEnrichment.setAbout("myAbout");
    agentEntityEnrichment.setEdmWasPresentAt(new String[]{"a", "b"});
    agentEntityEnrichment.setOwlSameAs(List.of("1", "2"));
    agentEntityEnrichment.setAltLabel(createAltLabels("en", new String[]{"a_en", "b_en"}));

    final EnrichmentTerm enrichmentTerm = new EnrichmentTerm();
    enrichmentTerm.setEnrichmentEntity(agentEntityEnrichment);
    enrichmentTerm.setEntityType(EntityType.AGENT);

    Agent agent = (Agent) Converter.convert(enrichmentTerm);
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
    ConceptEnrichmentEntity conceptEnrichmentEntity = new ConceptEnrichmentEntity();
    conceptEnrichmentEntity.setAbout("myAbout");
    conceptEnrichmentEntity.setRelated(new String[]{"a", "b"});
    conceptEnrichmentEntity.setRelatedMatch(new String[]{"1", "2"});

    final EnrichmentTerm enrichmentTerm = new EnrichmentTerm();
    enrichmentTerm.setEnrichmentEntity(conceptEnrichmentEntity);
    enrichmentTerm.setEntityType(EntityType.CONCEPT);

    Concept agent = (Concept) Converter.convert(enrichmentTerm);
    assertEquals("myAbout", agent.getAbout());
    assertEquals("a", agent.getRelated().get(0).getResource());
    assertEquals("b", agent.getRelated().get(1).getResource());
    assertEquals("1", agent.getRelatedMatch().get(0).getResource());
    assertEquals("2", agent.getRelatedMatch().get(1).getResource());

  }

  @Test
  public void convertTimespan() {
    TimespanEnrichmentEntity timespanEnrichmentEntity = new TimespanEnrichmentEntity();
    timespanEnrichmentEntity.setAbout("myAbout");
    timespanEnrichmentEntity.setOwlSameAs(List.of("1", "2"));

    final EnrichmentTerm enrichmentTerm = new EnrichmentTerm();
    enrichmentTerm.setEnrichmentEntity(timespanEnrichmentEntity);
    enrichmentTerm.setEntityType(EntityType.TIMESPAN);

    Timespan agent = (Timespan) Converter.convert(enrichmentTerm);
    assertEquals("myAbout", agent.getAbout());
    assertEquals("1", agent.getSameAs().get(0).getResource());
    assertEquals("2", agent.getSameAs().get(1).getResource());
  }

  @Test
  public void convertPlace() {
    PlaceEnrichmentEntity placeEnrichmentEntity = new PlaceEnrichmentEntity();
    placeEnrichmentEntity.setAbout("myAbout");
    placeEnrichmentEntity.setOwlSameAs(List.of("1", "2"));

    final EnrichmentTerm enrichmentTerm = new EnrichmentTerm();
    enrichmentTerm.setEnrichmentEntity(placeEnrichmentEntity);
    enrichmentTerm.setEntityType(EntityType.PLACE);

    Place agent = (Place) Converter.convert(enrichmentTerm);
    assertEquals("myAbout", agent.getAbout());
    assertEquals("1", agent.getSameAs().get(0).getResource());
    assertEquals("2", agent.getSameAs().get(1).getResource());
  }

  @Test
  public void convertOtherObject_returns_null() {
    final OrganizationEnrichmentEntity organizationEnrichmentEntity = new OrganizationEnrichmentEntity();
    organizationEnrichmentEntity.setAbout("myAbout");
    organizationEnrichmentEntity.setOwlSameAs(List.of("1", "2"));

    final EnrichmentTerm enrichmentTerm = new EnrichmentTerm();
    enrichmentTerm.setEnrichmentEntity(organizationEnrichmentEntity);
    enrichmentTerm.setEntityType(EntityType.ORGANIZATION);

    Place agent = (Place) Converter.convert(enrichmentTerm);
    assertNull(agent);
  }
}