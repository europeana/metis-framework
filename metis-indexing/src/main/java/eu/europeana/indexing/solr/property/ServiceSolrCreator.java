package eu.europeana.indexing.solr.property;

import org.apache.solr.common.SolrInputDocument;
import eu.europeana.corelib.definitions.edm.entity.Service;
import eu.europeana.indexing.solr.EdmLabel;

/**
 * Created by ymamakis on 1/12/16.
 */
public class ServiceSolrCreator extends PropertySolrCreator<Service> {

  @Override
  public void addToDocument(SolrInputDocument doc, Service service) {
    SolrPropertyUtils.addValue(doc, EdmLabel.SV_RDF_ABOUT, service.getAbout());
    SolrPropertyUtils.addValues(doc, EdmLabel.SV_DCTERMS_CONFORMS_TO, service.getDctermsConformsTo());
    SolrPropertyUtils.addValues(doc, EdmLabel.SV_DOAP_IMPLEMENTS, service.getDoapImplements());
  }
}
