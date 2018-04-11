package eu.europeana.indexing.solr.property;

import org.apache.solr.common.SolrInputDocument;
import org.springframework.util.StringUtils;
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
    SolrPropertyUtils.addValue(doc, EdmLabel.EUROPEANA_ID,
        StringUtils.replace(pCho.getAbout(), "/item/", "/"));
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_OWL_SAMEAS, pCho.getOwlSameAs());
  }
}
