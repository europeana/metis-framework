package eu.europeana.indexing.solr;

import org.apache.solr.common.SolrInputDocument;
import eu.europeana.corelib.definitions.edm.entity.Proxy;

/**
 *
 * @author Yorgos.Mamakis@ europeana.eu
 */
public class ProxySolrCreator {

  public void create(SolrInputDocument doc, Proxy proxy) {
    SolrUtils.addValue(doc, EdmLabel.ORE_PROXY, proxy.getAbout());
    SolrUtils.addValues(doc, EdmLabel.PROXY_EDM_CURRENT_LOCATION, proxy.getEdmCurrentLocation());
    SolrUtils.addValue(doc, EdmLabel.PROXY_EDM_ISREPRESENTATIONOF,
        proxy.getEdmIsRepresentationOf());
    SolrUtils.addValue(doc, EdmLabel.PROXY_ORE_PROXY_FOR, proxy.getProxyFor());
    SolrUtils.addValues(doc, EdmLabel.PROXY_EDM_INCORPORATES, proxy.getEdmIncorporates());
    SolrUtils.addValues(doc, EdmLabel.PROXY_EDM_ISDERIVATIVE_OF,
        proxy.getEdmIsDerivativeOf());
    SolrUtils.addValues(doc, EdmLabel.PROXY_EDM_IS_NEXT_IN_SEQUENCE,
        proxy.getEdmIsNextInSequence());
    SolrUtils.addValues(doc, EdmLabel.PROXY_EDM_ISSIMILARTO, proxy.getEdmIsSimilarTo());
    SolrUtils.addValues(doc, EdmLabel.PROXY_EDM_ISSUCCESSOROF,
        proxy.getEdmIsSuccessorOf());
    SolrUtils.addValues(doc, EdmLabel.PROXY_EDM_REALIZES, proxy.getEdmRealizes());
    SolrUtils.addValues(doc, EdmLabel.PROXY_EDM_WASPRESENTAT, proxy.getEdmWasPresentAt());
    SolrUtils.addValues(doc, EdmLabel.PROXY_ORE_PROXY_IN, proxy.getProxyIn());
    SolrUtils.addValues(doc, EdmLabel.PROXY_DC_CONTRIBUTOR, proxy.getDcContributor());
    SolrUtils.addValues(doc, EdmLabel.PROXY_DC_COVERAGE, proxy.getDcCoverage());
    SolrUtils.addValues(doc, EdmLabel.PROXY_DC_CREATOR, proxy.getDcCreator());
    SolrUtils.addValues(doc, EdmLabel.PROXY_DC_DATE, proxy.getDcDate());
    SolrUtils.addValues(doc, EdmLabel.PROXY_DC_DESCRIPTION, proxy.getDcDescription());
    SolrUtils.addValues(doc, EdmLabel.PROXY_DC_FORMAT, proxy.getDcFormat());
    SolrUtils.addValues(doc, EdmLabel.PROXY_DC_IDENTIFIER, proxy.getDcIdentifier());
    SolrUtils.addValues(doc, EdmLabel.PROXY_DC_LANGUAGE, proxy.getDcLanguage());
    SolrUtils.addValues(doc, EdmLabel.PROXY_DC_PUBLISHER, proxy.getDcPublisher());
    SolrUtils.addValues(doc, EdmLabel.PROXY_DC_RELATION, proxy.getDcRelation());
    SolrUtils.addValues(doc, EdmLabel.PROXY_DC_RIGHTS, proxy.getDcRights());
    SolrUtils.addValues(doc, EdmLabel.PROXY_DC_SOURCE, proxy.getDcSource());
    SolrUtils.addValues(doc, EdmLabel.PROXY_DC_SUBJECT, proxy.getDcSubject());
    SolrUtils.addValues(doc, EdmLabel.PROXY_DC_TITLE, proxy.getDcTitle());
    SolrUtils.addValues(doc, EdmLabel.PROXY_DC_TYPE, proxy.getDcType());
    SolrUtils.addValues(doc, EdmLabel.PROXY_DCTERMS_ALTERNATIVE, proxy.getDctermsAlternative());
    SolrUtils.addValues(doc, EdmLabel.PROXY_DCTERMS_CONFORMS_TO, proxy.getDctermsConformsTo());
    SolrUtils.addValues(doc, EdmLabel.PROXY_DCTERMS_CREATED, proxy.getDctermsCreated());
    SolrUtils.addValues(doc, EdmLabel.PROXY_DCTERMS_EXTENT, proxy.getDctermsExtent());
    SolrUtils.addValues(doc, EdmLabel.PROXY_DCTERMS_HAS_FORMAT, proxy.getDctermsHasFormat());
    SolrUtils.addValues(doc, EdmLabel.PROXY_DCTERMS_HAS_PART, proxy.getDctermsHasPart());
    SolrUtils.addValues(doc, EdmLabel.PROXY_DCTERMS_HAS_VERSION, proxy.getDctermsHasVersion());
    SolrUtils.addValues(doc, EdmLabel.PROXY_DCTERMS_IS_FORMAT_OF, proxy.getDctermsIsFormatOf());
    SolrUtils.addValues(doc, EdmLabel.PROXY_DCTERMS_IS_PART_OF, proxy.getDctermsIsPartOf());
    SolrUtils.addValues(doc, EdmLabel.PROXY_DCTERMS_IS_REFERENCED_BY,
        proxy.getDctermsIsReferencedBy());
    SolrUtils.addValues(doc, EdmLabel.PROXY_DCTERMS_IS_REPLACED_BY,
        proxy.getDctermsIsReplacedBy());
    SolrUtils.addValues(doc, EdmLabel.PROXY_DCTERMS_IS_REQUIRED_BY,
        proxy.getDctermsIsRequiredBy());
    SolrUtils.addValues(doc, EdmLabel.PROXY_DCTERMS_IS_VERSION_OF, proxy.getDctermsIsVersionOf());
    SolrUtils.addValues(doc, EdmLabel.PROXY_DCTERMS_ISSUED, proxy.getDctermsIssued());
    SolrUtils.addValues(doc, EdmLabel.PROXY_DCTERMS_MEDIUM, proxy.getDctermsMedium());
    SolrUtils.addValues(doc, EdmLabel.PROXY_DCTERMS_PROVENANCE, proxy.getDctermsProvenance());
    SolrUtils.addValues(doc, EdmLabel.PROXY_DCTERMS_REFERENCES, proxy.getDctermsReferences());
    SolrUtils.addValues(doc, EdmLabel.PROXY_DCTERMS_REPLACES, proxy.getDctermsReplaces());
    SolrUtils.addValues(doc, EdmLabel.PROXY_DCTERMS_REQUIRES, proxy.getDctermsRequires());
    SolrUtils.addValues(doc, EdmLabel.PROXY_DCTERMS_SPATIAL, proxy.getDctermsSpatial());
    SolrUtils.addValues(doc, EdmLabel.PROXY_DCTERMS_TABLE_OF_CONTENTS, proxy.getDctermsTOC());
    SolrUtils.addValues(doc, EdmLabel.PROXY_DCTERMS_TEMPORAL, proxy.getDctermsTemporal());
    SolrUtils.addValues(doc, EdmLabel.PROXY_EDM_YEAR, proxy.getYear());
    SolrUtils.addValues(doc, EdmLabel.PROXY_EDM_HAS_TYPE, proxy.getEdmHasType());
    SolrUtils.addValues(doc, EdmLabel.PROXY_EDM_ISRELATEDTO, proxy.getEdmIsRelatedTo());
    SolrUtils.addValues(doc, EdmLabel.PROXY_EDM_RIGHTS, proxy.getEdmRights());
    if (proxy.getEdmType() != null) {
      doc.addField(EdmLabel.PROVIDER_EDM_TYPE.toString(), proxy.getEdmType());
    }

    doc.addField(EdmLabel.EDM_ISEUROPEANA_PROXY.toString(), proxy.isEuropeanaProxy());

  }
}
