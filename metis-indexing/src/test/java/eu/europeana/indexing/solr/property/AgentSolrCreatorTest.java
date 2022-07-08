package eu.europeana.indexing.solr.property;

import static eu.europeana.indexing.utils.TestUtils.verifyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.corelib.definitions.edm.entity.Agent;
import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.indexing.solr.EdmLabel;
import java.util.List;
import java.util.Map;
import org.apache.solr.common.SolrInputDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link AgentSolrCreator} class
 */
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
    assertEquals("About Agent", solrInputDocument.getFieldValue(EdmLabel.EDM_AGENT.toString()));

    verifyMap(solrInputDocument, EdmLabel.AG_SKOS_PREF_LABEL, agent.getPrefLabel());
    verifyMap(solrInputDocument, EdmLabel.AG_SKOS_ALT_LABEL, agent.getAltLabel());
    verifyMap(solrInputDocument, EdmLabel.AG_FOAF_NAME, agent.getFoafName());
    verifyMap(solrInputDocument, EdmLabel.AG_RDAGR2_DATEOFBIRTH, agent.getRdaGr2DateOfBirth());
    verifyMap(solrInputDocument, EdmLabel.AG_RDAGR2_DATEOFDEATH, agent.getRdaGr2DateOfDeath());
    verifyMap(solrInputDocument, EdmLabel.AG_RDAGR2_PLACEOFBIRTH, agent.getRdaGr2PlaceOfBirth());
    verifyMap(solrInputDocument, EdmLabel.AG_RDAGR2_PLACEOFDEATH, agent.getRdaGr2PlaceOfDeath());
    verifyMap(solrInputDocument, EdmLabel.AG_RDAGR2_PROFESSIONOROCCUPATION, agent.getRdaGr2ProfessionOrOccupation());

    assertEquals(9, solrInputDocument.size());
  }
}
