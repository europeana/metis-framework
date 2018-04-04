package eu.europeana.indexing.solr;

import org.apache.solr.common.SolrInputDocument;
import eu.europeana.corelib.definitions.edm.entity.Service;

/**
 * Created by ymamakis on 1/12/16.
 */
public class ServiceSolrCreator extends PropertySolrCreator<Service> {

  @Override
  public void addToDocument(SolrInputDocument doc, Service service) {
    SolrUtils.addValue(doc, EdmLabel.SV_RDF_ABOUT, service.getAbout());
    SolrUtils.addValues(doc, EdmLabel.SV_DCTERMS_CONFORMS_TO, service.getDctermsConformsTo());
    SolrUtils.addValues(doc, EdmLabel.SV_DOAP_IMPLEMENTS, service.getDoapImplements());
  }
}
