package eu.europeana.indexing.solr;

import org.apache.solr.common.SolrInputDocument;
import eu.europeana.corelib.definitions.edm.entity.Concept;

/**
 * Generate Concept SOLR fields from Mongo
 * 
 * @author Yorgos.Mamakis@ europeana.eu
 *
 */
public class ConceptSolrCreator {
  
  /**
   * Create SOLR fields from a Mongo concept
   * 
   * @param doc The solr document to modify
   * @param concept The concept mongo entity to append
   */
  public void create(SolrInputDocument doc, Concept concept) {
    SolrUtils.addValue(doc, EdmLabel.SKOS_CONCEPT, concept.getAbout());
    SolrUtils.addValues(doc, EdmLabel.CC_SKOS_PREF_LABEL, concept.getPrefLabel());
    SolrUtils.addValues(doc, EdmLabel.CC_SKOS_ALT_LABEL, concept.getAltLabel());
    SolrUtils.addValues(doc, EdmLabel.CC_SKOS_HIDDEN_LABEL, concept.getHiddenLabel());
    SolrUtils.addValues(doc, EdmLabel.CC_SKOS_NOTE, concept.getNote());
    SolrUtils.addValues(doc, EdmLabel.CC_SKOS_NOTATIONS, concept.getNotation());
    SolrUtils.addValues(doc, EdmLabel.CC_SKOS_BROADER, concept.getBroader());
    SolrUtils.addValues(doc, EdmLabel.CC_SKOS_BROADMATCH, concept.getBroadMatch());
    SolrUtils.addValues(doc, EdmLabel.CC_SKOS_NARROWER, concept.getNarrower());
    SolrUtils.addValues(doc, EdmLabel.CC_SKOS_NARROWMATCH, concept.getNarrowMatch());
    SolrUtils.addValues(doc, EdmLabel.CC_SKOS_RELATED, concept.getRelated());
    SolrUtils.addValues(doc, EdmLabel.CC_SKOS_RELATEDMATCH, concept.getRelatedMatch());
    SolrUtils.addValues(doc, EdmLabel.CC_SKOS_EXACTMATCH, concept.getExactMatch());
    SolrUtils.addValues(doc, EdmLabel.CC_SKOS_CLOSEMATCH, concept.getCloseMatch());
    SolrUtils.addValues(doc, EdmLabel.CC_SKOS_INSCHEME, concept.getInScheme());
  }
}
