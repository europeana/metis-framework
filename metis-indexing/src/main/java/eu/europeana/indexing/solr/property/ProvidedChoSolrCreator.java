package eu.europeana.indexing.solr.property;

import eu.europeana.corelib.definitions.edm.entity.ProvidedCHO;
import eu.europeana.indexing.solr.EdmLabel;
import org.apache.solr.common.SolrInputDocument;

/**
 * Property Solr Creator for 'edm:providedCHO' tags.
 *
 * @author gmamakis
 */
public class ProvidedChoSolrCreator implements PropertySolrCreator<ProvidedCHO> {

  @Override
  public void addToDocument(SolrInputDocument doc, ProvidedCHO pCho) {
    SolrPropertyUtils.addValue(doc, EdmLabel.EUROPEANA_ID, pCho.getAbout());
  }
}
