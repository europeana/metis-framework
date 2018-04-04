package eu.europeana.indexing.solr;

import org.apache.solr.common.SolrInputDocument;
import eu.europeana.corelib.definitions.edm.entity.Agent;

/**
 * Generate Agent SOLR fields from Mongo
 *
 * @author Yorgos.Mamakis@ europeana.eu
 *
 */
public class AgentSolrCreator {

  /**
   * Create SOLR fields from a Mongo Agent
   *
   * @param doc The solr document to modify
   * @param agent The agent mongo entity to append
   */
  public void create(SolrInputDocument doc, Agent agent) {

    SolrUtils.addValue(doc, EdmLabel.EDM_AGENT, agent.getAbout());
    SolrUtils.addValues(doc, EdmLabel.AG_SKOS_PREF_LABEL, agent.getPrefLabel());
    SolrUtils.addValues(doc, EdmLabel.AG_SKOS_ALT_LABEL, agent.getAltLabel());
    SolrUtils.addValues(doc, EdmLabel.AG_SKOS_NOTE, agent.getNote());
    SolrUtils.addValues(doc, EdmLabel.AG_DC_DATE, agent.getDcDate());
    SolrUtils.addValues(doc, EdmLabel.AG_OWL_SAMEAS, agent.getOwlSameAs());
    SolrUtils.addValues(doc, EdmLabel.AG_DC_IDENTIFIER, agent.getDcIdentifier());
    SolrUtils.addValues(doc, EdmLabel.AG_EDM_BEGIN, agent.getBegin());
    SolrUtils.addValues(doc, EdmLabel.AG_EDM_END, agent.getEnd());
    SolrUtils.addValues(doc, EdmLabel.AG_EDM_WASPRESENTAT, agent.getEdmWasPresentAt());
    SolrUtils.addValues(doc, EdmLabel.AG_EDM_HASMET, agent.getEdmHasMet());
    SolrUtils.addValues(doc, EdmLabel.AG_EDM_ISRELATEDTO, agent.getEdmIsRelatedTo());
    SolrUtils.addValues(doc, EdmLabel.AG_FOAF_NAME, agent.getFoafName());
    SolrUtils.addValues(doc, EdmLabel.AG_RDAGR2_DATEOFBIRTH, agent.getRdaGr2DateOfBirth());
    SolrUtils.addValues(doc, EdmLabel.AG_RDAGR2_DATEOFDEATH, agent.getRdaGr2DateOfDeath());
    SolrUtils.addValues(doc, EdmLabel.AG_RDAGR2_PLACEOFBIRTH, agent.getRdaGr2PlaceOfBirth());
    SolrUtils.addValues(doc, EdmLabel.AG_RDAGR2_PLACEOFDEATH, agent.getRdaGr2PlaceOfDeath());
    SolrUtils.addValues(doc, EdmLabel.AG_RDAGR2_DATEOFESTABLISHMENT,
        agent.getRdaGr2DateOfEstablishment());
    SolrUtils.addValues(doc, EdmLabel.AG_RDAGR2_DATEOFTERMINATION,
        agent.getRdaGr2DateOfTermination());
    SolrUtils.addValues(doc, EdmLabel.AG_RDAGR2_GENDER, agent.getRdaGr2Gender());
    SolrUtils.addValues(doc, EdmLabel.AG_RDAGR2_PROFESSIONOROCCUPATION,
        agent.getRdaGr2ProfessionOrOccupation());
    SolrUtils.addValues(doc, EdmLabel.AG_RDAGR2_BIOGRAPHICALINFORMATION,
        agent.getRdaGr2BiographicalInformation());
  }
}
