package eu.europeana.indexing.search.v2.property;

import eu.europeana.corelib.definitions.edm.entity.ProvidedCHO;
import eu.europeana.indexing.common.persistence.solr.v2.SolrV2Field;
import org.apache.solr.common.SolrInputDocument;

/**
 * Property Solr Creator for 'edm:providedCHO' tags.
 *
 * @author gmamakis
 */
public class ProvidedChoSolrCreator implements PropertySolrCreator<ProvidedCHO> {

  @Override
  public void addToDocument(SolrInputDocument doc, ProvidedCHO pCho) {
    SolrPropertyUtils.addValue(doc, SolrV2Field.EUROPEANA_ID, pCho.getAbout());
  }
}
