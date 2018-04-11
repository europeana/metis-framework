package eu.europeana.indexing.solr.property;

import java.util.function.Predicate;
import org.apache.solr.common.SolrInputDocument;
import eu.europeana.corelib.definitions.edm.entity.WebResource;
import eu.europeana.indexing.solr.EdmLabel;

/**
 * Property Solr Creator for 'edm:WebResource' tags.
 *
 * @author Yorgos.Mamakis@ europeana.eu
 */
public class WebResourceSolrCreator implements PropertySolrCreator<WebResource> {

  private final Predicate<String> hasLicense;

  /**
   * Constructor.
   * 
   * @param hasLicense Predicate to evaluate whether there is a license available for any given web
   *        resource (URI).
   */
  public WebResourceSolrCreator(Predicate<String> hasLicense) {
    this.hasLicense = hasLicense;
  }

  @Override
  public void addToDocument(SolrInputDocument doc, WebResource wr) {
    SolrPropertyUtils.addValue(doc, EdmLabel.EDM_WEB_RESOURCE, wr.getAbout());
    SolrPropertyUtils.addValue(doc, EdmLabel.WR_EDM_IS_NEXT_IN_SEQUENCE, wr.getIsNextInSequence());
    if (SolrPropertyUtils.hasLicenseForRights(wr.getWebResourceEdmRights(), hasLicense)) {
      SolrPropertyUtils.addValues(doc, EdmLabel.WR_EDM_RIGHTS, wr.getWebResourceEdmRights());
    }
    SolrPropertyUtils.addValues(doc, EdmLabel.WR_DC_RIGHTS, wr.getWebResourceDcRights());
    SolrPropertyUtils.addValues(doc, EdmLabel.WR_DC_TYPE, wr.getDcType());
    SolrPropertyUtils.addValues(doc, EdmLabel.WR_DC_DESCRIPTION, wr.getDcDescription());
    SolrPropertyUtils.addValues(doc, EdmLabel.WR_DC_FORMAT, wr.getDcFormat());
    SolrPropertyUtils.addValues(doc, EdmLabel.WR_DC_SOURCE, wr.getDcSource());
    SolrPropertyUtils.addValues(doc, EdmLabel.WR_DC_CREATOR, wr.getDcCreator());
    SolrPropertyUtils.addValues(doc, EdmLabel.WR_DCTERMS_CONFORMSTO, wr.getDctermsConformsTo());
    SolrPropertyUtils.addValues(doc, EdmLabel.WR_DCTERMS_CREATED, wr.getDctermsCreated());
    SolrPropertyUtils.addValues(doc, EdmLabel.WR_DCTERMS_EXTENT, wr.getDctermsExtent());
    SolrPropertyUtils.addValues(doc, EdmLabel.WR_DCTERMS_HAS_PART, wr.getDctermsHasPart());
    SolrPropertyUtils.addValues(doc, EdmLabel.WR_DCTERMS_ISFORMATOF, wr.getDctermsIsFormatOf());
    SolrPropertyUtils.addValues(doc, EdmLabel.WR_DCTERMS_ISSUED, wr.getDctermsIssued());
    SolrPropertyUtils.addValues(doc, EdmLabel.WR_OWL_SAMEAS, wr.getOwlSameAs());
    SolrPropertyUtils.addValues(doc, EdmLabel.WR_SVCS_HAS_SERVICE, wr.getSvcsHasService());
    SolrPropertyUtils.addValues(doc, EdmLabel.WR_DCTERMS_ISREFERENCEDBY,
        wr.getDctermsIsReferencedBy());
    SolrPropertyUtils.addValue(doc, EdmLabel.WR_EDM_PREVIEW, wr.getEdmPreview());
  }
}
