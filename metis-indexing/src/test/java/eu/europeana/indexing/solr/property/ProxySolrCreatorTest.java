package eu.europeana.indexing.solr.property;

import static eu.europeana.indexing.utils.TestUtils.verifyMap;

import eu.europeana.corelib.solr.entity.ProxyImpl;
import eu.europeana.indexing.solr.EdmLabel;
import java.util.List;
import java.util.Map;
import org.apache.solr.common.SolrInputDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link ProxySolrCreator} class
 */
class ProxySolrCreatorTest {

  private ProxySolrCreator proxySolrCreator;
  private SolrInputDocument solrInputDocument;
  private ProxyImpl proxy;

  @BeforeEach
  void setup() {
    solrInputDocument = new SolrInputDocument();
    proxySolrCreator = new ProxySolrCreator();
    proxy = new ProxyImpl();
  }

  @Test
  void addToDocument() {
    proxy = getTestProxy();

    proxySolrCreator.addToDocument(solrInputDocument, proxy);

    assertDocumentContent(proxy, solrInputDocument);
  }

  private void assertDocumentContent(ProxyImpl expectedProxy, SolrInputDocument actualSolrInputDocument) {
    verifyMap(actualSolrInputDocument, EdmLabel.PROXY_EDM_CURRENT_LOCATION, expectedProxy.getEdmCurrentLocation());
    verifyMap(actualSolrInputDocument, EdmLabel.PROXY_DC_CONTRIBUTOR, expectedProxy.getDcContributor());
    verifyMap(actualSolrInputDocument, EdmLabel.PROXY_DC_COVERAGE, expectedProxy.getDcCoverage());
    verifyMap(actualSolrInputDocument, EdmLabel.PROXY_DC_CREATOR, expectedProxy.getDcCreator());
    verifyMap(actualSolrInputDocument, EdmLabel.PROXY_DC_DATE, expectedProxy.getDcDate());
    verifyMap(actualSolrInputDocument, EdmLabel.PROXY_DC_DESCRIPTION, expectedProxy.getDcDescription());
    verifyMap(actualSolrInputDocument, EdmLabel.PROXY_DC_FORMAT, expectedProxy.getDcFormat());
    verifyMap(actualSolrInputDocument, EdmLabel.PROXY_DC_IDENTIFIER, expectedProxy.getDcIdentifier());
    verifyMap(actualSolrInputDocument, EdmLabel.PROXY_DC_LANGUAGE, expectedProxy.getDcLanguage());
    verifyMap(actualSolrInputDocument, EdmLabel.PROXY_DC_PUBLISHER, expectedProxy.getDcPublisher());
    verifyMap(actualSolrInputDocument, EdmLabel.PROXY_DC_RIGHTS, expectedProxy.getDcRights());
    verifyMap(actualSolrInputDocument, EdmLabel.PROXY_DC_SOURCE, expectedProxy.getDcSource());
    verifyMap(actualSolrInputDocument, EdmLabel.PROXY_DC_SUBJECT, expectedProxy.getDcSubject());
    verifyMap(actualSolrInputDocument, EdmLabel.PROXY_DC_TITLE, expectedProxy.getDcTitle());
    verifyMap(actualSolrInputDocument, EdmLabel.PROXY_DC_TYPE, expectedProxy.getDcType());
    verifyMap(actualSolrInputDocument, EdmLabel.PROXY_DCTERMS_ALTERNATIVE, expectedProxy.getDctermsAlternative());
    verifyMap(actualSolrInputDocument, EdmLabel.PROXY_DCTERMS_CREATED, expectedProxy.getDctermsCreated());
    verifyMap(actualSolrInputDocument, EdmLabel.PROXY_DCTERMS_HAS_PART, expectedProxy.getDctermsHasPart());
    verifyMap(actualSolrInputDocument, EdmLabel.PROXY_DCTERMS_IS_PART_OF, expectedProxy.getDctermsIsPartOf());
    verifyMap(actualSolrInputDocument, EdmLabel.PROXY_DCTERMS_ISSUED, expectedProxy.getDctermsIssued());
    verifyMap(actualSolrInputDocument, EdmLabel.PROXY_DCTERMS_MEDIUM, expectedProxy.getDctermsMedium());
    verifyMap(actualSolrInputDocument, EdmLabel.PROXY_DCTERMS_PROVENANCE, expectedProxy.getDctermsProvenance());
    verifyMap(actualSolrInputDocument, EdmLabel.PROXY_DCTERMS_SPATIAL, expectedProxy.getDctermsSpatial());
    verifyMap(actualSolrInputDocument, EdmLabel.PROXY_DCTERMS_TEMPORAL, expectedProxy.getDctermsTemporal());
    verifyMap(actualSolrInputDocument, EdmLabel.PROXY_EDM_YEAR, expectedProxy.getYear());
    verifyMap(actualSolrInputDocument, EdmLabel.PROXY_EDM_HAS_MET, expectedProxy.getEdmHasMet());
    verifyMap(actualSolrInputDocument, EdmLabel.PROXY_EDM_ISRELATEDTO, expectedProxy.getEdmIsRelatedTo());
  }

  private ProxyImpl getTestProxy() {
    ProxyImpl proxy = new ProxyImpl();
    proxy.setAbout("proxy");
    proxy.setEdmCurrentLocation(Map.of("location", List.of("locationA", "locationB")));
    proxy.setDcContributor(Map.of("contributor", List.of("contributorA", "contributorB")));
    proxy.setDcCoverage(Map.of("coverage", List.of("coverage1", "coverage2")));
    proxy.setDcCreator(Map.of("creator", List.of("creator1", "creator2")));
    proxy.setDcDate(Map.of("date", List.of("date1", "date2")));
    proxy.setDcDescription(Map.of("description", List.of("description1", "description2")));
    proxy.setDcFormat(Map.of("format", List.of("format1", "format2")));
    proxy.setDcIdentifier(Map.of("identifier", List.of("identifier1", "identifier2")));
    proxy.setDcLanguage(Map.of("language", List.of("taal1", "taal2")));
    proxy.setDcPublisher(Map.of("publisher", List.of("publisher1", "publisher2")));
    proxy.setDcRights(Map.of("rights", List.of("rights1", "rights2")));
    proxy.setDcSource(Map.of("source", List.of("source1", "source2")));
    proxy.setDcSubject(Map.of("subject", List.of("subject1", "subject2")));
    proxy.setDcTitle(Map.of("title", List.of("title1", "title2")));
    proxy.setDcType(Map.of("type", List.of("type1", "type2")));
    proxy.setDctermsAlternative(Map.of("alternative", List.of("alt1", "alt2")));
    proxy.setDctermsCreated(Map.of("created", List.of("created1", "created2")));
    proxy.setDctermsHasPart(Map.of("hasPart", List.of("hasPart1", "hasPart2")));
    proxy.setDctermsIsPartOf(Map.of("isPartOf", List.of("isPartOf1", "isPartOf2")));
    proxy.setDctermsIssued(Map.of("issued", List.of("issued1", "issued2")));
    proxy.setDctermsMedium(Map.of("medium", List.of("medium1", "medium2")));
    proxy.setDctermsProvenance(Map.of("provenance", List.of("provenance1", "provenance2")));
    proxy.setDctermsSpatial(Map.of("spatial", List.of("spatial1", "spatial2")));
    proxy.setDctermsTemporal(Map.of("temporal", List.of("temp1", "temp2")));
    proxy.setYear(Map.of("year", List.of("year1", "year2")));
    proxy.setEdmHasMet(Map.of("hasMet", List.of("hasMet1", "hasMet2")));
    proxy.setEdmIsRelatedTo(Map.of("isRelatedTo", List.of("isRelatedTo1", "isRelatedTo2")));
    return proxy;
  }
}
