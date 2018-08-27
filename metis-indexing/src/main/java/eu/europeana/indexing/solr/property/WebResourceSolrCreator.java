package eu.europeana.indexing.solr.property;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.SolrInputDocument;
import eu.europeana.corelib.definitions.edm.entity.WebResource;
import eu.europeana.corelib.solr.entity.LicenseImpl;
import eu.europeana.indexing.solr.EdmLabel;

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
    SolrPropertyUtils.addValues(doc, EdmLabel.WR_SVCS_HAS_SERVICE, wr.getSvcsHasService());
    SolrPropertyUtils.addValues(doc, EdmLabel.WR_DCTERMS_ISREFERENCEDBY,
        wr.getDctermsIsReferencedBy());
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

    // Collect the edm rights and the dc rights in one set.
    final Collection<List<String>> webResourceDcRights = Optional
        .ofNullable(wr.getWebResourceDcRights()).map(Map::values).orElse(Collections.emptyList());
    final Collection<List<String>> webResourceEdmRights = Optional
        .ofNullable(wr.getWebResourceEdmRights()).map(Map::values).orElse(Collections.emptyList());
    final Set<String> rights =
        Stream.concat(webResourceDcRights.stream(), webResourceEdmRights.stream())
            .flatMap(List::stream).filter(StringUtils::isNotBlank).collect(Collectors.toSet());

    // Go through the licenses to see which of these are referenced and return the
    // odrlInheritedFrom for each of these.
    return licenses.stream().filter(license -> StringUtils.isNotBlank(license.getAbout()))
        .filter(license -> rights.contains(license.getAbout())).map(LicenseImpl::getOdrlInheritFrom)
        .distinct().toArray(String[]::new);
  }
}
