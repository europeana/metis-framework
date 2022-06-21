package eu.europeana.indexing.solr.property;

import static eu.europeana.indexing.utils.TestUtils.verifyCollection;
import static eu.europeana.indexing.utils.TestUtils.verifyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.corelib.definitions.edm.entity.License;
import eu.europeana.corelib.definitions.edm.entity.WebResource;
import eu.europeana.corelib.solr.entity.LicenseImpl;
import eu.europeana.corelib.solr.entity.WebResourceImpl;
import eu.europeana.indexing.solr.EdmLabel;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apache.solr.common.SolrInputDocument;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link WebResourceSolrCreator} class
 */
class WebResourceSolrCreatorTest {

  @Test
  void addToDocument() {
    // given
    License license1 = new LicenseImpl();
    license1.setAbout("license1About");
    license1.setOdrlInheritFrom("license1OdrlInheritFrom");
    License license2 = new LicenseImpl();
    license2.setAbout("license2About");
    license2.setOdrlInheritFrom("license2OdrlInheritFrom");

    WebResource webResource = new WebResourceImpl();
    webResource.setAbout("webResourceAbout");
    webResource.setIsNextInSequence("webResourceIsNextInSequence");
    webResource.setWebResourceEdmRights(
        Map.of("webResourceEdmRightsKey", List.of("webResourceEdmRightsValue", license2.getAbout())));
    webResource.setWebResourceDcRights(
        Map.of("webResourceDcRightsKey", List.of("webResourceDcRightsValue", license1.getAbout())));
    final List<String> webResourceSvcsHasService = List.of("webResourceSvcsHasService");
    webResource.setSvcsHasService(webResourceSvcsHasService.toArray(new String[0]));
    final List<String> webResourceDctermsIsReferencedBy = List.of("webResourceDctermsIsReferencedBy");
    webResource.setDctermsIsReferencedBy(webResourceDctermsIsReferencedBy.toArray(new String[0]));

    final WebResourceSolrCreator webResourceSolrCreator = new WebResourceSolrCreator(List.of(license1, license2));
    final SolrInputDocument solrInputDocument = new SolrInputDocument();

    // When
    webResourceSolrCreator.addToDocument(solrInputDocument, webResource);

    // Then
    assertEquals(webResource.getAbout(), solrInputDocument.getFieldValue(EdmLabel.EDM_WEB_RESOURCE.toString()));
    assertEquals(webResource.getIsNextInSequence(),
        solrInputDocument.getFieldValue(EdmLabel.WR_EDM_IS_NEXT_IN_SEQUENCE.toString()));
    verifyMap(solrInputDocument, EdmLabel.WR_EDM_RIGHTS, webResource.getWebResourceEdmRights());
    verifyMap(solrInputDocument, EdmLabel.WR_DC_RIGHTS, webResource.getWebResourceDcRights());
    verifyCollection(solrInputDocument, EdmLabel.WR_SVCS_HAS_SERVICE, webResourceSvcsHasService);
    verifyCollection(solrInputDocument, EdmLabel.WR_DCTERMS_ISREFERENCEDBY, webResourceDctermsIsReferencedBy);
    verifyCollection(solrInputDocument, EdmLabel.WR_CC_ODRL_INHERITED_FROM,
        List.of(license1.getOdrlInheritFrom(), license2.getOdrlInheritFrom()));
  }
}
