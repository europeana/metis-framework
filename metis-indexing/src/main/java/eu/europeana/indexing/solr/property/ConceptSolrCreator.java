package eu.europeana.indexing.solr.property;

import org.apache.solr.common.SolrInputDocument;
import eu.europeana.corelib.definitions.edm.entity.Concept;
import eu.europeana.indexing.solr.EdmLabel;

/**
 * Property Solr Creator for 'skos:Concept' tags.
 * 
 * @author Yorgos.Mamakis@ europeana.eu
 *
 */
public class ConceptSolrCreator implements PropertySolrCreator<Concept> {

  @Override
  public void addToDocument(SolrInputDocument doc, Concept concept) {
    SolrPropertyUtils.addValue(doc, EdmLabel.SKOS_CONCEPT, concept.getAbout());
    SolrPropertyUtils.addValues(doc, EdmLabel.CC_SKOS_PREF_LABEL, concept.getPrefLabel());
    SolrPropertyUtils.addValues(doc, EdmLabel.CC_SKOS_ALT_LABEL, concept.getAltLabel());
  }
}
