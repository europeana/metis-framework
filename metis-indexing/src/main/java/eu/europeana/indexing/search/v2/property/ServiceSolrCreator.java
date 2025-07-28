package eu.europeana.indexing.search.v2.property;

import org.apache.solr.common.SolrInputDocument;
import eu.europeana.corelib.definitions.edm.entity.Service;
import eu.europeana.indexing.search.v2.EdmLabel;

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
