package eu.europeana.indexing;

import static eu.europeana.indexing.utils.RdfTier.CONTENT_TIER_1;
import static eu.europeana.indexing.utils.RdfTier.CONTENT_TIER_2;
import static eu.europeana.indexing.utils.RdfTier.CONTENT_TIER_3;
import static eu.europeana.indexing.utils.RdfTier.CONTENT_TIER_BASE_URI;
import static eu.europeana.indexing.utils.RdfTier.METADATA_TIER_A;
import static eu.europeana.indexing.utils.RdfTier.METADATA_TIER_B;
import static eu.europeana.indexing.utils.RdfTier.METADATA_TIER_BASE_URI;
import static eu.europeana.indexing.utils.RdfTierUtils.extractTierData;
import static eu.europeana.indexing.utils.RdfTierUtils.extractTierDataByTarget;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.indexing.base.IndexingTestUtils;
import eu.europeana.indexing.exception.IndexingException;
import eu.europeana.indexing.tiers.TierCalculationMode;
import eu.europeana.indexing.tiers.model.TierResults;
import eu.europeana.metis.schema.convert.RdfConversionUtils;
import eu.europeana.metis.schema.convert.SerializationException;
import eu.europeana.metis.schema.jibx.Aggregation;
import eu.europeana.metis.schema.jibx.EdmType;
import eu.europeana.metis.schema.jibx.EuropeanaAggregationType;
import eu.europeana.metis.schema.jibx.RDF;
import java.time.Instant;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * The type Indexer preprocessor test.
 */
class IndexerPreprocessorTest {

  /**
   * Preprocess record tier calculation and initialise.
   *
   * @throws SerializationException the serialization exception
   * @throws IndexingException the indexing exception
   */
  @Test
  void preprocessRecordTierCalculationAndInitialise() throws SerializationException, IndexingException {
    // given
    final RdfConversionUtils conversionUtils = new RdfConversionUtils();
    final RDF inputRdf = conversionUtils.convertStringToRdf(
        IndexingTestUtils.getResourceFileContent("europeana_record_tier_calculation_rdf.xml"));
    final IndexingProperties indexingProperties = new IndexingProperties(Date.from(Instant.now()),
        true,
        List.of(), true, TierCalculationMode.INITIALISE, EnumSet.allOf(EdmType.class));

    // when
    TierResults results = IndexerPreprocessor.preprocessRecord(inputRdf, indexingProperties);

    // then
    List<String> tierProvidedData = extractTierData(inputRdf.getAggregationList(),
        Aggregation::getHasQualityAnnotationList);

    List<String> tierEuropeanaData = extractTierData(inputRdf.getEuropeanaAggregationList(),
        EuropeanaAggregationType::getHasQualityAnnotationList);

    // verify two different aggregation has different calculations
    assertArrayEquals(new String[]{CONTENT_TIER_1.getUri(), METADATA_TIER_A.getUri()}, tierProvidedData.toArray());
    assertArrayEquals(new String[]{CONTENT_TIER_1.getUri(), METADATA_TIER_B.getUri()}, tierEuropeanaData.toArray());

    // verify return of tier calculation
    assertEquals("1", results.getMediaTier().toString());
    assertEquals("A", results.getMetadataTier().toString());

    // verify return is equal to aggregation and not europeana aggregation
    assertTrue(tierProvidedData.contains(CONTENT_TIER_BASE_URI + results.getMediaTier().toString()) &&
        tierProvidedData.contains(METADATA_TIER_BASE_URI + results.getMetadataTier().toString()));
    assertFalse(tierEuropeanaData.contains(CONTENT_TIER_BASE_URI + results.getMediaTier().toString()) &&
        tierEuropeanaData.contains(METADATA_TIER_BASE_URI + results.getMetadataTier().toString()));
  }

  @Test
  void preprocessRecordTierCalculationAndInitialiseExisting() throws SerializationException, IndexingException {
    // given
    final RdfConversionUtils conversionUtils = new RdfConversionUtils();
    final RDF inputRdf = conversionUtils.convertStringToRdf(
        IndexingTestUtils.getResourceFileContent("europeana_record_rdf_with_tier_calculated.xml"));
    final IndexingProperties indexingProperties = new IndexingProperties(Date.from(Instant.now()),
        true,
        List.of(), true, TierCalculationMode.INITIALISE, EnumSet.allOf(EdmType.class));

    // when
    TierResults results = IndexerPreprocessor.preprocessRecord(inputRdf, indexingProperties);

    // then
    List<String> tierProvidedData = extractTierData(inputRdf.getAggregationList(),
        Aggregation::getHasQualityAnnotationList);

    List<String> tierEuropeanaData = extractTierData(inputRdf.getEuropeanaAggregationList(),
        EuropeanaAggregationType::getHasQualityAnnotationList);

    // verify two different aggregation has different calculations
    assertArrayEquals(new String[]{CONTENT_TIER_1.getUri(), METADATA_TIER_A.getUri()}, tierProvidedData.toArray());
    assertArrayEquals(new String[]{CONTENT_TIER_1.getUri(), METADATA_TIER_B.getUri()}, tierEuropeanaData.toArray());

    // verify return of tier calculation
    assertEquals("1", results.getMediaTier().toString());
    assertEquals("A", results.getMetadataTier().toString());

    // verify return is equal to aggregation and not europeana aggregation
    assertTrue(tierProvidedData.contains(CONTENT_TIER_BASE_URI + results.getMediaTier().toString()) &&
        tierProvidedData.contains(METADATA_TIER_BASE_URI + results.getMetadataTier().toString()));
    assertFalse(tierEuropeanaData.contains(CONTENT_TIER_BASE_URI + results.getMediaTier().toString()) &&
        tierEuropeanaData.contains(METADATA_TIER_BASE_URI + results.getMetadataTier().toString()));
  }

  @Test
  void preprocessRecordTierCalculationByTargetAndInitialiseExisting() throws SerializationException, IndexingException {
    // given
    final RdfConversionUtils conversionUtils = new RdfConversionUtils();
    final RDF inputRdf = conversionUtils.convertStringToRdf(
        IndexingTestUtils.getResourceFileContent("europeana_record_rdf_tier_for_overwrite.xml"));

    final IndexingProperties indexingProperties = new IndexingProperties(Date.from(Instant.now()),
        true,
        List.of(), true, TierCalculationMode.INITIALISE, EnumSet.allOf(EdmType.class));

    // when
    TierResults results = IndexerPreprocessor.preprocessRecord(inputRdf, indexingProperties);

    // then
    List<String> tierProvidedData = extractTierDataByTarget(inputRdf.getAggregationList(),
        Aggregation::getHasQualityAnnotationList, "/aggregation/provider/2022502/_KAMRA_356338");

    List<String> tierEuropeanaData = extractTierDataByTarget(inputRdf.getEuropeanaAggregationList(),
        EuropeanaAggregationType::getHasQualityAnnotationList,"/aggregation/europeana/2022502/_KAMRA_356338");

    // verify two different aggregation has different calculations
    assertArrayEquals(new String[]{CONTENT_TIER_3.getUri(), METADATA_TIER_A.getUri()}, tierProvidedData.toArray());
    assertArrayEquals(new String[]{CONTENT_TIER_3.getUri(), METADATA_TIER_A.getUri()}, tierEuropeanaData.toArray());

    // verify return of tier calculation
    assertEquals("2", results.getMediaTier().toString());
    assertEquals("A", results.getMetadataTier().toString());
  }

  @Test
  void preprocessRecordTierCalculationByTargetAndOverWriteExisting() throws SerializationException, IndexingException {
    // given
    final RdfConversionUtils conversionUtils = new RdfConversionUtils();
    final RDF inputRdf = conversionUtils.convertStringToRdf(
        IndexingTestUtils.getResourceFileContent("europeana_record_rdf_tier_for_overwrite.xml"));

    final IndexingProperties indexingProperties = new IndexingProperties(Date.from(Instant.now()),
        true,
        List.of(), true, TierCalculationMode.OVERWRITE, EnumSet.allOf(EdmType.class));

    // when
    TierResults results = IndexerPreprocessor.preprocessRecord(inputRdf, indexingProperties);

    // then
    List<String> tierProvidedData = extractTierDataByTarget(inputRdf.getAggregationList(),
        Aggregation::getHasQualityAnnotationList, "/aggregation/aggregator/2022502/_KAMRA_356338");

    List<String> tierEuropeanaData = extractTierDataByTarget(inputRdf.getEuropeanaAggregationList(),
        EuropeanaAggregationType::getHasQualityAnnotationList,"/aggregation/europeana/2022502/_KAMRA_356338");

    // verify two different aggregation has different calculations
    assertArrayEquals(new String[]{CONTENT_TIER_2.getUri(), METADATA_TIER_B.getUri()}, tierProvidedData.toArray());
    assertArrayEquals(new String[]{CONTENT_TIER_2.getUri(), METADATA_TIER_A.getUri()}, tierEuropeanaData.toArray());

    // verify return of tier calculation
    assertEquals("2", results.getMediaTier().toString());
    assertEquals("A", results.getMetadataTier().toString());
  }

  /**
   * Preprocess record tier calculation and overwrite.
   *
   * @throws SerializationException the serialization exception
   * @throws IndexingException the indexing exception
   */
  @Test
  void preprocessRecordTierCalculationAndOverwrite() throws SerializationException, IndexingException {
    // given
    final RdfConversionUtils conversionUtils = new RdfConversionUtils();
    final RDF inputRdf = conversionUtils.convertStringToRdf(
        IndexingTestUtils.getResourceFileContent("europeana_record_rdf_with_tier_calculated.xml"));
    final IndexingProperties indexingProperties = new IndexingProperties(Date.from(Instant.now()),
        true,
        List.of(), true, TierCalculationMode.OVERWRITE, EnumSet.allOf(EdmType.class));

    // when
    TierResults results = IndexerPreprocessor.preprocessRecord(inputRdf, indexingProperties);

    // then
    List<String> tierProvidedData = extractTierData(inputRdf.getAggregationList(),
        Aggregation::getHasQualityAnnotationList);

    List<String> tierEuropeanaData = extractTierData(inputRdf.getEuropeanaAggregationList(),
        EuropeanaAggregationType::getHasQualityAnnotationList);

    // verify two different aggregation has different calculations
    assertArrayEquals(new String[]{CONTENT_TIER_1.getUri(), METADATA_TIER_A.getUri()}, tierProvidedData.toArray());
    assertArrayEquals(new String[]{CONTENT_TIER_1.getUri(), METADATA_TIER_B.getUri()}, tierEuropeanaData.toArray());

    // verify return of tier calculation
    assertEquals("1", results.getMediaTier().toString());
    assertEquals("A", results.getMetadataTier().toString());

    // verify return is equal to aggregation and not europeana aggregation
    assertTrue(tierProvidedData.contains(CONTENT_TIER_BASE_URI + results.getMediaTier().toString()) &&
        tierProvidedData.contains(METADATA_TIER_BASE_URI + results.getMetadataTier().toString()));
    assertFalse(tierEuropeanaData.contains(CONTENT_TIER_BASE_URI + results.getMediaTier().toString()) &&
        tierEuropeanaData.contains(METADATA_TIER_BASE_URI + results.getMetadataTier().toString()));
  }

  /**
   * Preprocess record tier calculation and skip.
   *
   * @throws SerializationException the serialization exception
   * @throws IndexingException the indexing exception
   */
  @Test
  void preprocessRecordTierCalculationAndSkip() throws SerializationException, IndexingException {
    // given
    final RdfConversionUtils conversionUtils = new RdfConversionUtils();
    final RDF inputRdf = conversionUtils.convertStringToRdf(
        IndexingTestUtils.getResourceFileContent("europeana_record_rdf_with_tier_calculated.xml"));
    final IndexingProperties indexingProperties = new IndexingProperties(Date.from(Instant.now()),
        true,
        List.of(), true, TierCalculationMode.SKIP, EnumSet.allOf(EdmType.class));

    // when
    TierResults results = IndexerPreprocessor.preprocessRecord(inputRdf, indexingProperties);

    // then
    List<String> tierProvidedData = extractTierData(inputRdf.getAggregationList(),
        Aggregation::getHasQualityAnnotationList);

    List<String> tierEuropeanaData = extractTierData(inputRdf.getEuropeanaAggregationList(),
        EuropeanaAggregationType::getHasQualityAnnotationList);

    // verify two different aggregation has different calculations
    assertArrayEquals(new String[]{CONTENT_TIER_1.getUri(), METADATA_TIER_A.getUri()}, tierProvidedData.toArray());
    assertArrayEquals(new String[]{CONTENT_TIER_1.getUri(), METADATA_TIER_B.getUri()}, tierEuropeanaData.toArray());

    // verify return of tier calculation
    assertNull(results.getMediaTier());
    assertNull(results.getMetadataTier());
  }

  /**
   * Preprocess record no tier calculation.
   *
   * @throws SerializationException the serialization exception
   * @throws IndexingException the indexing exception
   */
  @Test
  void preprocessRecordNoTierCalculation() throws SerializationException, IndexingException {
    // given
    final RdfConversionUtils conversionUtils = new RdfConversionUtils();
    final RDF inputRdf = conversionUtils.convertStringToRdf(
        IndexingTestUtils.getResourceFileContent("europeana_record_tier_calculation_rdf.xml"));
    final IndexingProperties indexingProperties = new IndexingProperties(Date.from(Instant.now()),
        true,
        List.of(), true, false);

    // when
    TierResults results = IndexerPreprocessor.preprocessRecord(inputRdf, indexingProperties);

    // then
    List<String> tierProvidedData = extractTierData(inputRdf.getAggregationList(),
        Aggregation::getHasQualityAnnotationList);

    List<String> tierEuropeanaData = extractTierData(inputRdf.getEuropeanaAggregationList(),
        EuropeanaAggregationType::getHasQualityAnnotationList);

    // verify two different aggregation has no calculations
    assertArrayEquals(new String[]{}, tierProvidedData.toArray());
    assertArrayEquals(new String[]{}, tierEuropeanaData.toArray());

    // verify return of tier calculation
    assertNull(results.getMediaTier());
    assertNull(results.getMetadataTier());
  }
}
