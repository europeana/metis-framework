package eu.europeana.indexing.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.indexing.base.IndexingTestUtils;
import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.tiers.model.MetadataTier;
import eu.europeana.metis.schema.convert.RdfConversionUtils;
import eu.europeana.metis.schema.jibx.RDF;
import org.junit.jupiter.api.Test;

class RdfTierUtilsTest {

  private RDF inputRdf;

  void setUpRdfWithTierCalculated() throws Exception {
    final RdfConversionUtils conversionUtils = new RdfConversionUtils();
    inputRdf = conversionUtils.convertStringToRdf(
        IndexingTestUtils.getResourceFileContent("europeana_record_rdf_with_tier_calculated.xml"));
  }

  void setUpRdfWithoutTierCalculated() throws Exception {
    final RdfConversionUtils conversionUtils = new RdfConversionUtils();
    inputRdf = conversionUtils.convertStringToRdf(
        IndexingTestUtils.getResourceFileContent("europeana_record_tier_calculation_rdf.xml"));
  }

  @Test
  void hasTierEuropeanaMediaTierCalculation() throws Exception {
    setUpRdfWithTierCalculated();
    assertTrue(RdfTierUtils.hasTierEuropeanaCalculation(inputRdf, MediaTier.class));
  }

  @Test
  void hasTierEuropeanaMetadataCalculation() throws Exception {
    setUpRdfWithTierCalculated();
    assertTrue(RdfTierUtils.hasTierEuropeanaCalculation(inputRdf, MetadataTier.class));
  }

  @Test
  void hasTierMediaTierCalculation() throws Exception {
    setUpRdfWithTierCalculated();
    assertTrue(RdfTierUtils.hasTierCalculation(inputRdf, MediaTier.class));
  }

  @Test
  void hasTierMetadataCalculation() throws Exception {
    setUpRdfWithTierCalculated();
    assertTrue(RdfTierUtils.hasTierCalculation(inputRdf, MetadataTier.class));
  }

  @Test
  void hasNotTierEuropeanaMediaTierCalculation() throws Exception {
    setUpRdfWithoutTierCalculated();
    assertFalse(RdfTierUtils.hasTierEuropeanaCalculation(inputRdf, MediaTier.class));
  }

  @Test
  void hasNotTierEuropeanaMetadataCalculation() throws Exception {
    setUpRdfWithoutTierCalculated();
    assertFalse(RdfTierUtils.hasTierEuropeanaCalculation(inputRdf, MetadataTier.class));
  }

  @Test
  void hasNotTierMediaTierCalculation() throws Exception {
    setUpRdfWithoutTierCalculated();
    assertFalse(RdfTierUtils.hasTierCalculation(inputRdf, MediaTier.class));
  }

  @Test
  void hasNotTierMetadataCalculation() throws Exception {
    setUpRdfWithoutTierCalculated();
    assertFalse(RdfTierUtils.hasTierCalculation(inputRdf, MetadataTier.class));
  }
}
