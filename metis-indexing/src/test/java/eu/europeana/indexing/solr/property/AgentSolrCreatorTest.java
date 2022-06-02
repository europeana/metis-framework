package eu.europeana.indexing.solr.property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.corelib.definitions.edm.entity.Agent;
import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.indexing.solr.EdmLabel;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.solr.common.SolrInputDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AgentSolrCreatorTest {

  private SolrInputDocument solrInputDocument;
  private AgentSolrCreator agentSolrCreator;


  @BeforeEach
  void setup() {
    solrInputDocument = new SolrInputDocument();
    agentSolrCreator = new AgentSolrCreator();
  }


  @Test
  void agentSolrCreatorAddToSolrDocument() {

    Agent agent = new AgentImpl();
    agent.setAbout("About Agent");
    agent.setPrefLabel(Map.of("pref_label", List.of("val1", "val2")));
    agent.setAltLabel(Map.of("alt_label", List.of("val1", "val2")));
    agent.setFoafName(Map.of("foaf_name", List.of("val1", "val2")));
    agent.setRdaGr2DateOfBirth(Map.of("date_of_birth_label", List.of("some_date")));
    agent.setRdaGr2DateOfDeath(Map.of("date_of_death_label", List.of("other_date")));
    agent.setRdaGr2PlaceOfBirth(Map.of("place_of_birth_label", List.of("some_place")));
    agent.setRdaGr2PlaceOfDeath(Map.of("place_of_death_label", List.of("other_place")));
    agent.setRdaGr2ProfessionOrOccupation(Map.of("profession_label", List.of("lawyer", "chef")));

    // the actual method to test
    agentSolrCreator.addToDocument(solrInputDocument, agent);

    // assertions
    assertTrue(solrInputDocument.containsKey(EdmLabel.EDM_AGENT.toString()));
    assertTrue(solrInputDocument.containsKey(EdmLabel.AG_SKOS_PREF_LABEL + ".pref_label"));
    assertTrue(solrInputDocument.containsKey(EdmLabel.AG_SKOS_ALT_LABEL + ".alt_label"));
    assertTrue(solrInputDocument.containsKey(EdmLabel.AG_FOAF_NAME + ".foaf_name"));
    assertTrue(solrInputDocument.containsKey(EdmLabel.AG_RDAGR2_DATEOFBIRTH + ".date_of_birth_label"));
    assertTrue(solrInputDocument.containsKey(EdmLabel.AG_RDAGR2_DATEOFDEATH + ".date_of_death_label"));
    assertTrue(solrInputDocument.containsKey(EdmLabel.AG_RDAGR2_PLACEOFBIRTH + ".place_of_birth_label"));
    assertTrue(solrInputDocument.containsKey(EdmLabel.AG_RDAGR2_PLACEOFDEATH + ".place_of_death_label"));
    assertTrue(solrInputDocument.containsKey(EdmLabel.AG_RDAGR2_PROFESSIONOROCCUPATION + ".profession_label"));

    assertEquals("About Agent", solrInputDocument.getFieldValue(EdmLabel.EDM_AGENT.toString()));
    assertEquals(Arrays.asList("val1", "val2"), solrInputDocument.getFieldValues(EdmLabel.AG_SKOS_PREF_LABEL + ".pref_label"));
    assertEquals(Arrays.asList("val1", "val2"), solrInputDocument.getFieldValues(EdmLabel.AG_SKOS_ALT_LABEL + ".alt_label"));
    assertEquals(Arrays.asList("val1", "val2"), solrInputDocument.getFieldValues(EdmLabel.AG_FOAF_NAME + ".foaf_name"));
    assertEquals(Arrays.asList("some_date"),
        solrInputDocument.getFieldValues(EdmLabel.AG_RDAGR2_DATEOFBIRTH + ".date_of_birth_label"));
    assertEquals(Arrays.asList("other_date"),
        solrInputDocument.getFieldValues(EdmLabel.AG_RDAGR2_DATEOFDEATH + ".date_of_death_label"));
    assertEquals(Arrays.asList("some_place"),
        solrInputDocument.getFieldValues(EdmLabel.AG_RDAGR2_PLACEOFBIRTH + ".place_of_birth_label"));
    assertEquals(Arrays.asList("other_place"),
        solrInputDocument.getFieldValues(EdmLabel.AG_RDAGR2_PLACEOFDEATH + ".place_of_death_label"));
    assertEquals(Arrays.asList("lawyer", "chef"),
        solrInputDocument.getFieldValues(EdmLabel.AG_RDAGR2_PROFESSIONOROCCUPATION + ".profession_label"));

    assertEquals(9, solrInputDocument.size());

  }

}
