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
  void hasTierEuropeanaMediaTierCalculationByTarget() throws Exception {
    setUpRdfWithTierCalculated();
    assertTrue(RdfTierUtils.hasTierEuropeanaCalculationByTarget(inputRdf, MediaTier.T1));
  }

  @Test
  void hasTierEuropeanaMetadataCalculationByTarget() throws Exception {
    setUpRdfWithTierCalculated();
    assertTrue(RdfTierUtils.hasTierEuropeanaCalculationByTarget(inputRdf, MetadataTier.TA));
  }

  @Test
  void hasTierEuropeanaMediaTierCalculation() throws Exception {
    setUpRdfWithTierCalculated();
    assertTrue(RdfTierUtils.hasTierEuropeanaCalculation(inputRdf, MediaTier.T1));
  }

  @Test
  void hasTierEuropeanaMetadataCalculation() throws Exception {
    setUpRdfWithTierCalculated();
    assertTrue(RdfTierUtils.hasTierEuropeanaCalculation(inputRdf, MetadataTier.TA));
  }

  @Test
  void hasTierMediaTierCalculationByTarget() throws Exception {
    setUpRdfWithTierCalculated();
    assertTrue(RdfTierUtils.hasTierCalculationByTarget(inputRdf, MediaTier.T1));
  }

  @Test
  void hasTierMetadataCalculationByTarget() throws Exception {
    setUpRdfWithTierCalculated();
    assertTrue(RdfTierUtils.hasTierCalculationByTarget(inputRdf, MetadataTier.TA));
  }

  @Test
  void hasTierMediaTierCalculation() throws Exception {
    setUpRdfWithTierCalculated();
    assertTrue(RdfTierUtils.hasTierCalculation(inputRdf, MediaTier.T1));
  }

  @Test
  void hasTierMetadataCalculation() throws Exception {
    setUpRdfWithTierCalculated();
    assertTrue(RdfTierUtils.hasTierCalculation(inputRdf, MetadataTier.TA));
  }

  @Test
  void hasNotTierEuropeanaMediaTierCalculation() throws Exception {
    setUpRdfWithoutTierCalculated();
    assertFalse(RdfTierUtils.hasTierEuropeanaCalculation(inputRdf, MediaTier.T1));
  }

  @Test
  void hasNotTierEuropeanaMetadataCalculation() throws Exception {
    setUpRdfWithoutTierCalculated();
    assertFalse(RdfTierUtils.hasTierEuropeanaCalculation(inputRdf, MetadataTier.TA));
  }

  @Test
  void hasNotTierEuropeanaMediaTierCalculationByTarget() throws Exception {
    setUpRdfWithoutTierCalculated();
    assertFalse(RdfTierUtils.hasTierEuropeanaCalculationByTarget(inputRdf, MediaTier.T1));
  }

  @Test
  void hasNotTierEuropeanaMetadataCalculationByTarget() throws Exception {
    setUpRdfWithoutTierCalculated();
    assertFalse(RdfTierUtils.hasTierEuropeanaCalculationByTarget(inputRdf, MetadataTier.TA));
  }

  @Test
  void hasNotTierMediaTierCalculation() throws Exception {
    setUpRdfWithoutTierCalculated();
    assertFalse(RdfTierUtils.hasTierCalculation(inputRdf, MediaTier.T1));
  }

  @Test
  void hasNotTierMetadataCalculation() throws Exception {
    setUpRdfWithoutTierCalculated();
    assertFalse(RdfTierUtils.hasTierCalculation(inputRdf, MetadataTier.TA));
  }

  @Test
  void hasNotTierMediaTierCalculationByTarget() throws Exception {
    setUpRdfWithoutTierCalculated();
    assertFalse(RdfTierUtils.hasTierCalculationByTarget(inputRdf, MediaTier.T1));
  }

  @Test
  void hasNotTierMetadataCalculationByTarget() throws Exception {
    setUpRdfWithoutTierCalculated();
    assertFalse(RdfTierUtils.hasTierCalculationByTarget(inputRdf, MetadataTier.TA));
  }
}
