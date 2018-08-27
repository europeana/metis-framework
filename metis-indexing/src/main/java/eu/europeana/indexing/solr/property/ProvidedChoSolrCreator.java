package eu.europeana.indexing.solr.property;

import org.apache.solr.common.SolrInputDocument;
import eu.europeana.corelib.solr.entity.ProvidedCHOImpl;
import eu.europeana.indexing.solr.EdmLabel;

/**
 * Property Solr Creator for 'edm:providedCHO' tags.
 *
 * @author gmamakis
 */
public class ProvidedChoSolrCreator implements PropertySolrCreator<ProvidedCHOImpl> {

  @Override
  public void addToDocument(SolrInputDocument doc, ProvidedCHOImpl pCho) {
    SolrPropertyUtils.addValue(doc, EdmLabel.EUROPEANA_ID, pCho.getAbout());
  }
}
