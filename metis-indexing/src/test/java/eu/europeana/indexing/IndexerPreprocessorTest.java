package eu.europeana.indexing;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.indexing.base.IndexingTestUtils;
import eu.europeana.indexing.exception.IndexingException;
import eu.europeana.indexing.tiers.model.TierResults;
import eu.europeana.metis.schema.convert.RdfConversionUtils;
import eu.europeana.metis.schema.convert.SerializationException;
import eu.europeana.metis.schema.jibx.RDF;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * The type Indexer preprocessor test.
 */
class IndexerPreprocessorTest {

  private static final String CONTENT_TIER_URI = "http://www.europeana.eu/schemas/epf/contentTier";
  private static final String METADATA_TIER_URI = "http://www.europeana.eu/schemas/epf/metadataTier";

  /**
   * Preprocess record.
   *
   * @throws SerializationException the serialization exception
   * @throws IndexingException the indexing exception
   */
  @Test
  void preprocessRecord() throws SerializationException, IndexingException {
    // given
    final RdfConversionUtils conversionUtils = new RdfConversionUtils();
    final RDF inputRdf = conversionUtils.convertStringToRdf(
        IndexingTestUtils.getResourceFileContent("europeana_record_to_sample_index_rdf.xml"));
    final IndexingProperties indexingProperties = new IndexingProperties(Date.from(Instant.now()),
        true,
        List.of(), true, true);

    // when
    TierResults results = IndexerPreprocessor.preprocessRecord(inputRdf, indexingProperties);

    // then
    List<String> tierProvidedData = inputRdf.getAggregationList()
                                            .stream()
                                            .map(provideddata -> provideddata.getHasQualityAnnotationList()
                                                                             .stream()
                                                                             .map(q -> q.getQualityAnnotation().getHasBody()
                                                                                        .getResource()).toList())
                                            .findFirst().orElse(null);

    List<String> tierEuropeanaData = inputRdf.getEuropeanaAggregationList()
                                             .stream()
                                             .map(eudata -> eudata.getHasQualityAnnotationList()
                                                                  .stream()
                                                                  .map(q -> q.getQualityAnnotation().getHasBody().getResource())
                                                                  .toList())
                                             .findFirst().orElse(null);

    // verify two different aggregation has different calculations
    assertArrayEquals(new String[]{CONTENT_TIER_URI + "1", METADATA_TIER_URI + "A"}, tierProvidedData.toArray());
    assertArrayEquals(new String[]{CONTENT_TIER_URI + "1", METADATA_TIER_URI + "B"}, tierEuropeanaData.toArray());

    // verify return of tier calculation
    assertEquals("1", results.getMediaTier().toString());
    assertEquals("A", results.getMetadataTier().toString());

    // verify return is equal to aggregation and not europeana aggregation
    assertTrue(tierProvidedData.contains(CONTENT_TIER_URI + results.getMediaTier().toString()) &&
        tierProvidedData.contains(METADATA_TIER_URI + results.getMetadataTier().toString()));
    assertFalse(tierEuropeanaData.contains(CONTENT_TIER_URI + results.getMediaTier().toString()) &&
        tierEuropeanaData.contains(METADATA_TIER_URI + results.getMetadataTier().toString()));
  }
}
