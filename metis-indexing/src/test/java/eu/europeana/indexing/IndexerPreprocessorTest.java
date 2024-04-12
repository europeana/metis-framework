package eu.europeana.indexing;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

  /**
   * Preprocess record.
   *
   * @throws SerializationException the serialization exception
   * @throws IndexingException the indexing exception
   */
  @Test
  void preprocessRecord() throws SerializationException, IndexingException {
    final RdfConversionUtils conversionUtils = new RdfConversionUtils();
    final RDF inputRdf = conversionUtils.convertStringToRdf(
        IndexingTestUtils.getResourceFileContent("europeana_record_to_sample_index_rdf.xml"));
    final IndexingProperties indexingProperties = new IndexingProperties(Date.from(Instant.now()),
        true,
        List.of(), true, true);

    TierResults results = IndexerPreprocessor.preprocessRecord(inputRdf, indexingProperties);

    assertEquals("4", results.getMediaTier().toString());
    assertEquals("B", results.getMetadataTier().toString());
  }
}
