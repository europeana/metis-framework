package eu.europeana.indexing.solr.property;

import eu.europeana.corelib.definitions.edm.entity.WebResource;
import eu.europeana.corelib.solr.entity.LicenseImpl;
import eu.europeana.indexing.solr.EdmLabel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.SolrInputDocument;

/**
 * Property Solr Creator for 'edm:WebResource' tags.
 *
 * @author Yorgos.Mamakis@ europeana.eu
 */
public class WebResourceSolrCreator implements PropertySolrCreator<WebResource> {

  private final List<LicenseImpl> licenses;

  /**
   * Constructor.
   *
   * @param licenses the list of licenses for the record.
   */
  public WebResourceSolrCreator(List<LicenseImpl> licenses) {
    this.licenses = new ArrayList<>(licenses);
  }

  @Override
  public void addToDocument(SolrInputDocument doc, WebResource wr) {
    SolrPropertyUtils.addValue(doc, EdmLabel.EDM_WEB_RESOURCE, wr.getAbout());
    SolrPropertyUtils.addValue(doc, EdmLabel.WR_EDM_IS_NEXT_IN_SEQUENCE, wr.getIsNextInSequence());
    SolrPropertyUtils.addValues(doc, EdmLabel.WR_EDM_RIGHTS, wr.getWebResourceEdmRights());
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
    SolrPropertyUtils.addValues(doc, EdmLabel.WR_CC_ODRL_INHERITED_FROM,
        getArrayOfOdrlInheritFromMatches(wr));
  }

  /**
   * Get for a WebResource the rdf:resource of dc:rights and edm:rights. If the value of it matches
   * with one of the rdf:about of a License, then pick the value in the odrl:inheritFrom
   * rdf:resource and add it to the wr_cc_odrl_inherited_from.
   *
   * The values should be unique and not repeated in the solr field.
   *
   * @param wr the web resource
   * @return the array containing the unique values of odrlInheritFrom
   */
  private String[] getArrayOfOdrlInheritFromMatches(WebResource wr) {
    Stream<String> webResourceDcRightsStream = Stream.empty();
    Stream<String> webResourceEdmRightsStream = Stream.empty();
    if (wr.getWebResourceDcRights() != null) {
      webResourceDcRightsStream = wr.getWebResourceDcRights().values().stream()
          .flatMap(Collection::stream).filter(StringUtils::isNotBlank);
    }
    if (wr.getWebResourceEdmRights() != null) {
      webResourceEdmRightsStream = wr.getWebResourceEdmRights().values().stream()
          .flatMap(Collection::stream).filter(StringUtils::isNotBlank);
    }

    //Combine the two required streams
    Stream<String> dcAndEdmRightsRdfAboutsStream = Stream
        .concat(webResourceDcRightsStream, webResourceEdmRightsStream);

    //Use Set to get unique values and also check for non Blank.
    Set<String> odrlInheritFromMatches = new HashSet<>();
    dcAndEdmRightsRdfAboutsStream.forEach(rightsRdfAbout -> licenses.stream()
        .filter(license -> StringUtils.isNotBlank(license.getAbout()) && license.getAbout()
            .equals(rightsRdfAbout))
        .forEach(license -> odrlInheritFromMatches.add(license.getOdrlInheritFrom())));
    String[] odrlInheritFromMatchesArray = new String[odrlInheritFromMatches.size()];
    return odrlInheritFromMatches.toArray(odrlInheritFromMatchesArray);
  }
}
