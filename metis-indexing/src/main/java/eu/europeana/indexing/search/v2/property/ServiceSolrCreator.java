package eu.europeana.indexing.search.v2.property;

import org.apache.solr.common.SolrInputDocument;
import eu.europeana.corelib.definitions.edm.entity.Service;
import eu.europeana.indexing.common.persistence.solr.v2.SolrV2Field;

/**
 * Property Solr Creator for 'svcs:Service' tags.
 */
public class ServiceSolrCreator implements PropertySolrCreator<Service> {

  @Override
  public void addToDocument(SolrInputDocument doc, Service service) {
    SolrPropertyUtils.addValue(doc, SolrV2Field.SV_SERVICE, service.getAbout());
    SolrPropertyUtils.addValues(doc, SolrV2Field.SV_DCTERMS_CONFORMS_TO, service.getDctermsConformsTo());
  }
}
