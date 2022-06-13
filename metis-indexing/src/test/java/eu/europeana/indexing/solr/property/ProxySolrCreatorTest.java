package eu.europeana.indexing.solr.property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    assertKeys(solrInputDocument);
    assertDocumentContent(solrInputDocument);
  }

  private void assertDocumentContent(SolrInputDocument solrInputDocument) {
    assertEquals(List.of("locationA", "locationB"), solrInputDocument.getFieldValues(EdmLabel.PROXY_EDM_CURRENT_LOCATION + ".location"));
    assertEquals(List.of("contributorA", "contributorB"), solrInputDocument.getFieldValues(EdmLabel.PROXY_DC_CONTRIBUTOR + ".contributor"));
    assertEquals(List.of("coverage1", "coverage2"), solrInputDocument.getFieldValues(EdmLabel.PROXY_DC_COVERAGE + ".coverage"));
    assertEquals(List.of("creator1", "creator2"), solrInputDocument.getFieldValues(EdmLabel.PROXY_DC_CREATOR + ".creator"));
    assertEquals(List.of("date1", "date2"), solrInputDocument.getFieldValues(EdmLabel.PROXY_DC_DATE + ".date"));
    assertEquals(List.of("description1", "description2"), solrInputDocument.getFieldValues(EdmLabel.PROXY_DC_DESCRIPTION + ".description"));
    assertEquals(List.of("format1", "format2"), solrInputDocument.getFieldValues(EdmLabel.PROXY_DC_FORMAT + ".format"));
    assertEquals(List.of("identifier1", "identifier2"), solrInputDocument.getFieldValues(EdmLabel.PROXY_DC_IDENTIFIER + ".identifier"));
    assertEquals(List.of("taal1", "taal2"), solrInputDocument.getFieldValues(EdmLabel.PROXY_DC_LANGUAGE + ".language"));
    assertEquals(List.of("publisher1", "publisher2"), solrInputDocument.getFieldValues(EdmLabel.PROXY_DC_PUBLISHER + ".publisher"));
    assertEquals(List.of("rights1", "rights2"), solrInputDocument.getFieldValues(EdmLabel.PROXY_DC_RIGHTS + ".rights"));
    assertEquals(List.of("source1", "source2"), solrInputDocument.getFieldValues(EdmLabel.PROXY_DC_SOURCE + ".source"));
    assertEquals(List.of("subject1", "subject2"), solrInputDocument.getFieldValues(EdmLabel.PROXY_DC_SUBJECT + ".subject"));
    assertEquals(List.of("title1", "title2"), solrInputDocument.getFieldValues(EdmLabel.PROXY_DC_TITLE + ".title"));
    assertEquals(List.of("type1", "type2"), solrInputDocument.getFieldValues(EdmLabel.PROXY_DC_TYPE + ".type"));
    assertEquals(List.of("alt1", "alt2"), solrInputDocument.getFieldValues(EdmLabel.PROXY_DCTERMS_ALTERNATIVE + ".alternative"));
    assertEquals(List.of("created1", "created2"), solrInputDocument.getFieldValues(EdmLabel.PROXY_DCTERMS_CREATED + ".created"));
    assertEquals(List.of("hasPart1", "hasPart2"), solrInputDocument.getFieldValues(EdmLabel.PROXY_DCTERMS_HAS_PART + ".hasPart"));
    assertEquals(List.of("isPartOf1", "isPartOf2"), solrInputDocument.getFieldValues(EdmLabel.PROXY_DCTERMS_IS_PART_OF + ".isPartOf"));
    assertEquals(List.of("issued1", "issued2"), solrInputDocument.getFieldValues(EdmLabel.PROXY_DCTERMS_ISSUED + ".issued"));
    assertEquals(List.of("medium1", "medium2"), solrInputDocument.getFieldValues(EdmLabel.PROXY_DCTERMS_MEDIUM + ".medium"));
    assertEquals(List.of("provenance1", "provenance2"), solrInputDocument.getFieldValues(EdmLabel.PROXY_DCTERMS_PROVENANCE + ".provenance"));
    assertEquals(List.of("spatial1", "spatial2"), solrInputDocument.getFieldValues(EdmLabel.PROXY_DCTERMS_SPATIAL + ".spatial"));
    assertEquals(List.of("temp1", "temp2"), solrInputDocument.getFieldValues(EdmLabel.PROXY_DCTERMS_TEMPORAL + ".temporal"));
    assertEquals(List.of("year1", "year2"), solrInputDocument.getFieldValues(EdmLabel.PROXY_EDM_YEAR + ".year"));
    assertEquals(List.of("hasMet1", "hasMet2"), solrInputDocument.getFieldValues(EdmLabel.PROXY_EDM_HAS_MET + ".hasMet"));
    assertEquals(List.of("isRelatedTo1", "isRelatedTo2"), solrInputDocument.getFieldValues(EdmLabel.PROXY_EDM_ISRELATEDTO + ".isRelatedTo"));
  }

  private void assertKeys(SolrInputDocument solrInputDocument) {
    assertTrue(solrInputDocument.containsKey(EdmLabel.PROXY_EDM_CURRENT_LOCATION + ".location"));
    assertTrue(solrInputDocument.containsKey(EdmLabel.PROXY_DC_CONTRIBUTOR + ".contributor"));
    assertTrue(solrInputDocument.containsKey(EdmLabel.PROXY_DC_COVERAGE + ".coverage"));
    assertTrue(solrInputDocument.containsKey(EdmLabel.PROXY_DC_CREATOR + ".creator"));
    assertTrue(solrInputDocument.containsKey(EdmLabel.PROXY_DC_DATE + ".date"));
    assertTrue(solrInputDocument.containsKey(EdmLabel.PROXY_DC_DESCRIPTION + ".description"));
    assertTrue(solrInputDocument.containsKey(EdmLabel.PROXY_DC_FORMAT + ".format"));
    assertTrue(solrInputDocument.containsKey(EdmLabel.PROXY_DC_IDENTIFIER + ".identifier"));
    assertTrue(solrInputDocument.containsKey(EdmLabel.PROXY_DC_LANGUAGE + ".language"));
    assertTrue(solrInputDocument.containsKey(EdmLabel.PROXY_DC_PUBLISHER + ".publisher"));
    assertTrue(solrInputDocument.containsKey(EdmLabel.PROXY_DC_RIGHTS + ".rights"));
    assertTrue(solrInputDocument.containsKey(EdmLabel.PROXY_DC_SOURCE + ".source"));
    assertTrue(solrInputDocument.containsKey(EdmLabel.PROXY_DC_SUBJECT + ".subject"));
    assertTrue(solrInputDocument.containsKey(EdmLabel.PROXY_DC_TITLE + ".title"));
    assertTrue(solrInputDocument.containsKey(EdmLabel.PROXY_DC_TYPE + ".type"));
    assertTrue(solrInputDocument.containsKey(EdmLabel.PROXY_DCTERMS_ALTERNATIVE + ".alternative"));
    assertTrue(solrInputDocument.containsKey(EdmLabel.PROXY_DCTERMS_CREATED + ".created"));
    assertTrue(solrInputDocument.containsKey(EdmLabel.PROXY_DCTERMS_HAS_PART + ".hasPart"));
    assertTrue(solrInputDocument.containsKey(EdmLabel.PROXY_DCTERMS_IS_PART_OF + ".isPartOf"));
    assertTrue(solrInputDocument.containsKey(EdmLabel.PROXY_DCTERMS_ISSUED + ".issued"));
    assertTrue(solrInputDocument.containsKey(EdmLabel.PROXY_DCTERMS_MEDIUM + ".medium"));
    assertTrue(solrInputDocument.containsKey(EdmLabel.PROXY_DCTERMS_PROVENANCE + ".provenance"));
    assertTrue(solrInputDocument.containsKey(EdmLabel.PROXY_DCTERMS_SPATIAL + ".spatial"));
    assertTrue(solrInputDocument.containsKey(EdmLabel.PROXY_DCTERMS_TEMPORAL + ".temporal"));
    assertTrue(solrInputDocument.containsKey(EdmLabel.PROXY_EDM_YEAR + ".year"));
    assertTrue(solrInputDocument.containsKey(EdmLabel.PROXY_EDM_HAS_MET + ".hasMet"));
    assertTrue(solrInputDocument.containsKey(EdmLabel.PROXY_EDM_ISRELATEDTO + ".isRelatedTo"));
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
