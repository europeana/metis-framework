package eu.europeana.indexing.solr.property;

import org.apache.solr.common.SolrInputDocument;
import eu.europeana.corelib.definitions.edm.entity.Agent;
import eu.europeana.indexing.solr.EdmLabel;

/**
 * Property Solr Creator for 'edm:Agent' tags.
 *
 * @author Yorgos.Mamakis@ europeana.eu
 *
 */
public class AgentSolrCreator implements PropertySolrCreator<Agent> {

  @Override
  public void addToDocument(SolrInputDocument doc, Agent agent) {

    SolrPropertyUtils.addValue(doc, EdmLabel.EDM_AGENT, agent.getAbout());
    SolrPropertyUtils.addValues(doc, EdmLabel.AG_SKOS_PREF_LABEL, agent.getPrefLabel());
    SolrPropertyUtils.addValues(doc, EdmLabel.AG_SKOS_ALT_LABEL, agent.getAltLabel());
    SolrPropertyUtils.addValues(doc, EdmLabel.AG_FOAF_NAME, agent.getFoafName());
    SolrPropertyUtils.addValues(doc, EdmLabel.AG_RDAGR2_DATEOFBIRTH, agent.getRdaGr2DateOfBirth());
    SolrPropertyUtils.addValues(doc, EdmLabel.AG_RDAGR2_DATEOFDEATH, agent.getRdaGr2DateOfDeath());
    SolrPropertyUtils.addValues(doc, EdmLabel.AG_RDAGR2_PLACEOFBIRTH, agent.getRdaGr2PlaceOfBirth());
    SolrPropertyUtils.addValues(doc, EdmLabel.AG_RDAGR2_PLACEOFDEATH, agent.getRdaGr2PlaceOfDeath());
    SolrPropertyUtils.addValues(doc, EdmLabel.AG_RDAGR2_PROFESSIONOROCCUPATION,
        agent.getRdaGr2ProfessionOrOccupation());
  }
}
