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
    SolrPropertyUtils.addValue(doc, EdmLabel.ORE_PROXY, proxy.getAbout());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_EDM_CURRENT_LOCATION, proxy.getEdmCurrentLocation());
    SolrPropertyUtils.addValue(doc, EdmLabel.PROXY_EDM_ISREPRESENTATIONOF,
        proxy.getEdmIsRepresentationOf());
    SolrPropertyUtils.addValue(doc, EdmLabel.PROXY_ORE_PROXY_FOR, proxy.getProxyFor());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_EDM_INCORPORATES, proxy.getEdmIncorporates());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_EDM_ISDERIVATIVE_OF,
        proxy.getEdmIsDerivativeOf());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_EDM_IS_NEXT_IN_SEQUENCE,
        proxy.getEdmIsNextInSequence());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_EDM_ISSIMILARTO, proxy.getEdmIsSimilarTo());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_EDM_ISSUCCESSOROF,
        proxy.getEdmIsSuccessorOf());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_EDM_REALIZES, proxy.getEdmRealizes());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_EDM_WASPRESENTAT, proxy.getEdmWasPresentAt());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_ORE_PROXY_IN, proxy.getProxyIn());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DC_CONTRIBUTOR, proxy.getDcContributor());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DC_COVERAGE, proxy.getDcCoverage());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DC_CREATOR, proxy.getDcCreator());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DC_DATE, proxy.getDcDate());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DC_DESCRIPTION, proxy.getDcDescription());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DC_FORMAT, proxy.getDcFormat());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DC_IDENTIFIER, proxy.getDcIdentifier());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DC_LANGUAGE, proxy.getDcLanguage());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DC_PUBLISHER, proxy.getDcPublisher());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DC_RELATION, proxy.getDcRelation());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DC_RIGHTS, proxy.getDcRights());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DC_SOURCE, proxy.getDcSource());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DC_SUBJECT, proxy.getDcSubject());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DC_TITLE, proxy.getDcTitle());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DC_TYPE, proxy.getDcType());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DCTERMS_ALTERNATIVE, proxy.getDctermsAlternative());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DCTERMS_CONFORMS_TO, proxy.getDctermsConformsTo());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DCTERMS_CREATED, proxy.getDctermsCreated());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DCTERMS_EXTENT, proxy.getDctermsExtent());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DCTERMS_HAS_FORMAT, proxy.getDctermsHasFormat());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DCTERMS_HAS_PART, proxy.getDctermsHasPart());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DCTERMS_HAS_VERSION, proxy.getDctermsHasVersion());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DCTERMS_IS_FORMAT_OF, proxy.getDctermsIsFormatOf());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DCTERMS_IS_PART_OF, proxy.getDctermsIsPartOf());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DCTERMS_IS_REFERENCED_BY,
        proxy.getDctermsIsReferencedBy());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DCTERMS_IS_REPLACED_BY,
        proxy.getDctermsIsReplacedBy());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DCTERMS_IS_REQUIRED_BY,
        proxy.getDctermsIsRequiredBy());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DCTERMS_IS_VERSION_OF, proxy.getDctermsIsVersionOf());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DCTERMS_ISSUED, proxy.getDctermsIssued());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DCTERMS_MEDIUM, proxy.getDctermsMedium());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DCTERMS_PROVENANCE, proxy.getDctermsProvenance());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DCTERMS_REFERENCES, proxy.getDctermsReferences());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DCTERMS_REPLACES, proxy.getDctermsReplaces());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DCTERMS_REQUIRES, proxy.getDctermsRequires());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DCTERMS_SPATIAL, proxy.getDctermsSpatial());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DCTERMS_TABLE_OF_CONTENTS, proxy.getDctermsTOC());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_DCTERMS_TEMPORAL, proxy.getDctermsTemporal());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_EDM_YEAR, proxy.getYear());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_EDM_HAS_TYPE, proxy.getEdmHasType());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_EDM_ISRELATEDTO, proxy.getEdmIsRelatedTo());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROXY_EDM_RIGHTS, proxy.getEdmRights());
    if (proxy.getEdmType() != null) {
      doc.addField(EdmLabel.PROVIDER_EDM_TYPE.toString(), proxy.getEdmType());
    }

    doc.addField(EdmLabel.EDM_ISEUROPEANA_PROXY.toString(), proxy.isEuropeanaProxy());

  }
}
