package eu.europeana.indexing.solr.property;

import org.apache.solr.common.SolrInputDocument;
import eu.europeana.corelib.definitions.edm.entity.Service;
import eu.europeana.indexing.solr.EdmLabel;

/**
 * Property Solr Creator for 'svcs:Service' tags.
 */
public class ServiceSolrCreator implements PropertySolrCreator<Service> {

  @Override
  public void addToDocument(SolrInputDocument doc, Service service) {
    SolrPropertyUtils.addValue(doc, EdmLabel.SV_SERVICE, service.getAbout());
    SolrPropertyUtils.addValues(doc, EdmLabel.SV_DCTERMS_CONFORMS_TO, service.getDctermsConformsTo());
  }
}
