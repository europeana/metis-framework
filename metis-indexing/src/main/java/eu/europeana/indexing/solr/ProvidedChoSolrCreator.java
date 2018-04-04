package eu.europeana.indexing.solr;

import org.apache.solr.common.SolrInputDocument;
import org.springframework.util.StringUtils;
import eu.europeana.corelib.solr.entity.ProvidedCHOImpl;

/**
 *
 * @author gmamakis
 */
public class ProvidedChoSolrCreator {
  public void create(SolrInputDocument doc, ProvidedCHOImpl pCho) {
    SolrUtils.addValue(doc, EdmLabel.EUROPEANA_ID,
        StringUtils.replace(pCho.getAbout(), "/item/", "/"));
    SolrUtils.addValues(doc, EdmLabel.PROXY_OWL_SAMEAS, pCho.getOwlSameAs());
  }
}
