package eu.europeana.indexing.solr.property;

import org.apache.solr.common.SolrInputDocument;
import eu.europeana.corelib.definitions.edm.entity.Proxy;
import eu.europeana.indexing.solr.EdmLabel;

/**
 * Property Solr Creator for 'ore:Proxy' tags.
 *
 * @author Yorgos.Mamakis@ europeana.eu
 */
public class ProxySolrCreator implements PropertySolrCreator<Proxy> {

  @Override
  public void addToDocument(SolrInputDocument doc, Proxy proxy) {
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_EDM_CURRENT_LOCATION, proxy.getEdmCurrentLocation());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DC_CONTRIBUTOR, proxy.getDcContributor());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DC_COVERAGE, proxy.getDcCoverage());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DC_CREATOR, proxy.getDcCreator());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DC_DATE, proxy.getDcDate());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DC_DESCRIPTION, proxy.getDcDescription());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DC_FORMAT, proxy.getDcFormat());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DC_IDENTIFIER, proxy.getDcIdentifier());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DC_LANGUAGE, proxy.getDcLanguage());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DC_PUBLISHER, proxy.getDcPublisher());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DC_RIGHTS, proxy.getDcRights());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DC_SOURCE, proxy.getDcSource());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DC_SUBJECT, proxy.getDcSubject());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DC_TITLE, proxy.getDcTitle());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DC_TYPE, proxy.getDcType());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DCTERMS_ALTERNATIVE, proxy.getDctermsAlternative());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DCTERMS_CREATED, proxy.getDctermsCreated());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DCTERMS_HAS_PART, proxy.getDctermsHasPart());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DCTERMS_IS_PART_OF, proxy.getDctermsIsPartOf());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DCTERMS_ISSUED, proxy.getDctermsIssued());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DCTERMS_MEDIUM, proxy.getDctermsMedium());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DCTERMS_PROVENANCE, proxy.getDctermsProvenance());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DCTERMS_SPATIAL, proxy.getDctermsSpatial());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DCTERMS_TEMPORAL, proxy.getDctermsTemporal());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_EDM_YEAR, proxy.getYear());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_EDM_HAS_MET, proxy.getEdmHasMet());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_EDM_ISRELATEDTO, proxy.getEdmIsRelatedTo());
    if (proxy.getEdmType() != null) {
      doc.addField(EdmLabel.PROVIDER_EDM_TYPE.toString(), proxy.getEdmType().getEnumNameValue());
    }
  }
}
