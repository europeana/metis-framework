package eu.europeana.indexing.search.v2.property;

import org.apache.solr.common.SolrInputDocument;
import eu.europeana.corelib.definitions.edm.entity.Concept;
import eu.europeana.indexing.common.persistence.solr.v2.SolrV2Field;

/**
 * Property Solr Creator for 'skos:Concept' tags.
 * 
 * @author Yorgos.Mamakis@ europeana.eu
 *
 */
public class ConceptSolrCreator implements PropertySolrCreator<Concept> {

  @Override
  public void addToDocument(SolrInputDocument doc, Concept concept) {
    SolrPropertyUtils.addValue(doc, SolrV2Field.SKOS_CONCEPT, concept.getAbout());
    SolrPropertyUtils.addValues(doc, SolrV2Field.CC_SKOS_PREF_LABEL, concept.getPrefLabel());
    SolrPropertyUtils.addValues(doc, SolrV2Field.CC_SKOS_ALT_LABEL, concept.getAltLabel());
  }
}
