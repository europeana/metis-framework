package eu.europeana.indexing.solr.property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.corelib.solr.entity.LicenseImpl;
import eu.europeana.indexing.solr.EdmLabel;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import org.apache.solr.common.SolrInputDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link LicenseSolrCreator} class
 */
class LicenseSolrCreatorTest {

  private LicenseSolrCreator licenseSolrCreator;
  private SolrInputDocument solrInputDocument;
  private LicenseImpl license;

  @BeforeEach
  void setup() {
    solrInputDocument = new SolrInputDocument();
    Set<String> defRights = Set.of("license1", "license2", "license3", "license4", "license5");
    licenseSolrCreator = new LicenseSolrCreator(lic -> defRights.contains(license.getAbout()));
    license = new LicenseImpl();
  }

  @Test
  void addToDocument_WithAggregationAndDeprecatedOn() {
    license.setAbout("license2");
    license.setCcDeprecatedOn(Date.from(Instant.parse("2022-06-01T12:22:55.888Z")));
    license.setOdrlInheritFrom("OdrlInheritFrom");

    licenseSolrCreator.addToDocument(solrInputDocument, license);

    assertTrue(solrInputDocument.containsKey(EdmLabel.PROVIDER_AGGREGATION_CC_LICENSE.toString()) &&
        solrInputDocument.containsKey(EdmLabel.PROVIDER_AGGREGATION_CC_DEPRECATED_ON.toString()) &&
        solrInputDocument.containsKey(EdmLabel.PROVIDER_AGGREGATION_ODRL_INHERITED_FROM.toString()));
    assertFalse(solrInputDocument.containsKey(EdmLabel.WR_CC_DEPRECATED_ON.toString()));
    assertEquals("license2", solrInputDocument.getFieldValue(EdmLabel.PROVIDER_AGGREGATION_CC_LICENSE.toString()));
    assertEquals(Date.from(Instant.parse("2022-06-01T12:22:55.888Z")),
        solrInputDocument.getFieldValue(EdmLabel.PROVIDER_AGGREGATION_CC_DEPRECATED_ON.toString()));
    assertEquals("OdrlInheritFrom",
        solrInputDocument.getFieldValue(EdmLabel.PROVIDER_AGGREGATION_ODRL_INHERITED_FROM.toString()));
    assertNull(solrInputDocument.getFieldValue(EdmLabel.WR_CC_DEPRECATED_ON.toString()));

    assertEquals(3, solrInputDocument.size());
  }

  @Test
  void addToDocument_WithAggregationAndWithoutDeprecatedOn() {
    license.setAbout("license2");
    license.setCcDeprecatedOn(null);
    license.setOdrlInheritFrom("OdrlInheritFrom");

    licenseSolrCreator.addToDocument(solrInputDocument, license);

    assertTrue(solrInputDocument.containsKey(EdmLabel.PROVIDER_AGGREGATION_CC_LICENSE.toString()) &&
        solrInputDocument.containsKey(EdmLabel.PROVIDER_AGGREGATION_ODRL_INHERITED_FROM.toString()));
    assertFalse(solrInputDocument.containsKey(EdmLabel.PROVIDER_AGGREGATION_CC_DEPRECATED_ON.toString()) &&
        solrInputDocument.containsKey(EdmLabel.WR_CC_DEPRECATED_ON.toString()));
    assertEquals("license2", solrInputDocument.getFieldValue(EdmLabel.PROVIDER_AGGREGATION_CC_LICENSE.toString()));
    assertNull(solrInputDocument.getFieldValue(EdmLabel.PROVIDER_AGGREGATION_CC_DEPRECATED_ON.toString()));
    assertNull(solrInputDocument.getFieldValue(EdmLabel.WR_CC_DEPRECATED_ON.toString()));
    assertEquals("OdrlInheritFrom",
        solrInputDocument.getFieldValue(EdmLabel.PROVIDER_AGGREGATION_ODRL_INHERITED_FROM.toString()));

    assertEquals(2, solrInputDocument.size());
  }

  @Test
  void addToDocument_WithAggregationAndDeprecatedOnWithoutOdrlInherit() {
    license.setAbout("license5");
    license.setCcDeprecatedOn(Date.from(Instant.parse("2022-06-01T12:22:55.888Z")));
    licenseSolrCreator.addToDocument(solrInputDocument, license);

    assertTrue(solrInputDocument.containsKey(EdmLabel.PROVIDER_AGGREGATION_CC_LICENSE.toString()) &&
        solrInputDocument.containsKey(EdmLabel.PROVIDER_AGGREGATION_CC_DEPRECATED_ON.toString()));
    assertFalse(solrInputDocument.containsKey(EdmLabel.WR_CC_DEPRECATED_ON.toString()) &&
        solrInputDocument.containsKey(EdmLabel.PROVIDER_AGGREGATION_ODRL_INHERITED_FROM.toString()));
    assertEquals("license5", solrInputDocument.getFieldValue(EdmLabel.PROVIDER_AGGREGATION_CC_LICENSE.toString()));
    assertEquals(Date.from(Instant.parse("2022-06-01T12:22:55.888Z")),
        solrInputDocument.getFieldValue(EdmLabel.PROVIDER_AGGREGATION_CC_DEPRECATED_ON.toString()));
    assertNull(solrInputDocument.getFieldValue(EdmLabel.PROVIDER_AGGREGATION_ODRL_INHERITED_FROM.toString()));

    assertEquals(2, solrInputDocument.size());
  }

  @Test
  void addToDocument_WithoutAggregation() {
    license.setAbout("license0");
    license.setCcDeprecatedOn(Date.from(Instant.parse("2022-06-01T12:22:55.888Z")));
    license.setOdrlInheritFrom("OdrlInheritFrom");

    licenseSolrCreator.addToDocument(solrInputDocument, license);

    assertTrue(solrInputDocument.containsKey(EdmLabel.WR_CC_LICENSE.toString()) &&
        solrInputDocument.containsKey(EdmLabel.WR_CC_DEPRECATED_ON.toString()));
    assertFalse(solrInputDocument.containsKey(EdmLabel.PROVIDER_AGGREGATION_CC_LICENSE.toString()) &&
        solrInputDocument.containsKey(EdmLabel.PROVIDER_AGGREGATION_ODRL_INHERITED_FROM.toString()));
    assertEquals("license0", solrInputDocument.getFieldValue(EdmLabel.WR_CC_LICENSE.toString()));
    assertEquals(Date.from(Instant.parse("2022-06-01T12:22:55.888Z")),
        solrInputDocument.getFieldValue(EdmLabel.WR_CC_DEPRECATED_ON.toString()));
    assertNull(solrInputDocument.getFieldValue(EdmLabel.PROVIDER_AGGREGATION_CC_LICENSE.toString()));
    assertNull(solrInputDocument.getFieldValue(EdmLabel.PROVIDER_AGGREGATION_ODRL_INHERITED_FROM.toString()));

    assertEquals(2, solrInputDocument.size());
  }

  @Test
  void addToDocument_WithoutAggregationAndWithoutCcDeprecatedOn() {
    license.setAbout("license0");
    license.setCcDeprecatedOn(null);
    license.setOdrlInheritFrom("OdrlInheritFrom");

    licenseSolrCreator.addToDocument(solrInputDocument, license);

    assertTrue(solrInputDocument.containsKey(EdmLabel.WR_CC_LICENSE.toString()));
    assertFalse(solrInputDocument.containsKey(EdmLabel.WR_CC_DEPRECATED_ON.toString()) &&
        solrInputDocument.containsKey(EdmLabel.PROVIDER_AGGREGATION_CC_LICENSE.toString()) &&
        solrInputDocument.containsKey(EdmLabel.PROVIDER_AGGREGATION_ODRL_INHERITED_FROM.toString()));
    assertEquals("license0", solrInputDocument.getFieldValue(EdmLabel.WR_CC_LICENSE.toString()));
    assertNull(solrInputDocument.getFieldValue(EdmLabel.WR_CC_DEPRECATED_ON.toString()));
    assertNull(solrInputDocument.getFieldValue(EdmLabel.PROVIDER_AGGREGATION_CC_LICENSE.toString()));
    assertNull(solrInputDocument.getFieldValue(EdmLabel.PROVIDER_AGGREGATION_ODRL_INHERITED_FROM.toString()));

    assertEquals(1, solrInputDocument.size());
  }
}
