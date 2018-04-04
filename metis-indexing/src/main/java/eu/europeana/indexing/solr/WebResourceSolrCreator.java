package eu.europeana.indexing.solr;

import java.util.Set;
import org.apache.solr.common.SolrInputDocument;
import eu.europeana.corelib.definitions.edm.entity.WebResource;

/**
 *
 * @author Yorgos.Mamakis@ europeana.eu
 */
public class WebResourceSolrCreator {

  public void create(SolrInputDocument doc, WebResource wr, Set<String> licIds) {
    SolrUtils.addValue(doc, EdmLabel.EDM_WEB_RESOURCE, wr.getAbout());
    SolrUtils.addValue(doc, EdmLabel.WR_EDM_IS_NEXT_IN_SEQUENCE, wr.getIsNextInSequence());
    if (SolrUtils.hasLicenseForRights(wr.getWebResourceEdmRights(), licIds)) {
      SolrUtils.addValues(doc, EdmLabel.WR_EDM_RIGHTS, wr.getWebResourceEdmRights());
    }
    SolrUtils.addValues(doc, EdmLabel.WR_DC_RIGHTS, wr.getWebResourceDcRights());
    SolrUtils.addValues(doc, EdmLabel.WR_DC_TYPE, wr.getDcType());
    SolrUtils.addValues(doc, EdmLabel.WR_DC_DESCRIPTION, wr.getDcDescription());
    SolrUtils.addValues(doc, EdmLabel.WR_DC_FORMAT, wr.getDcFormat());
    SolrUtils.addValues(doc, EdmLabel.WR_DC_SOURCE, wr.getDcSource());
    SolrUtils.addValues(doc, EdmLabel.WR_DC_CREATOR, wr.getDcCreator());
    SolrUtils.addValues(doc, EdmLabel.WR_DCTERMS_CONFORMSTO, wr.getDctermsConformsTo());
    SolrUtils.addValues(doc, EdmLabel.WR_DCTERMS_CREATED, wr.getDctermsCreated());
    SolrUtils.addValues(doc, EdmLabel.WR_DCTERMS_EXTENT, wr.getDctermsExtent());
    SolrUtils.addValues(doc, EdmLabel.WR_DCTERMS_HAS_PART, wr.getDctermsHasPart());
    SolrUtils.addValues(doc, EdmLabel.WR_DCTERMS_ISFORMATOF, wr.getDctermsIsFormatOf());
    SolrUtils.addValues(doc, EdmLabel.WR_DCTERMS_ISSUED, wr.getDctermsIssued());
    SolrUtils.addValues(doc, EdmLabel.WR_OWL_SAMEAS, wr.getOwlSameAs());
    SolrUtils.addValues(doc, EdmLabel.WR_SVCS_HAS_SERVICE, wr.getSvcsHasService());
    SolrUtils.addValues(doc, EdmLabel.WR_DCTERMS_ISREFERENCEDBY,
        wr.getDctermsIsReferencedBy());
    SolrUtils.addValue(doc, EdmLabel.WR_EDM_PREVIEW, wr.getEdmPreview());
  }
}
