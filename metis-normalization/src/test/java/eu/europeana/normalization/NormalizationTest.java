package eu.europeana.normalization;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import eu.europeana.metis.schema.convert.RdfConversionUtils;
import eu.europeana.metis.schema.convert.SerializationException;
import eu.europeana.metis.schema.jibx.RDF;
import eu.europeana.normalization.model.NormalizationBatchResult;
import eu.europeana.normalization.util.NormalizationConfigurationException;
import eu.europeana.normalization.util.NormalizationException;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class NormalizationTest {

  @Test
  void testNormalization()
      throws NormalizationException, NormalizationConfigurationException, SerializationException {

    final RdfConversionUtils rdfConversionUtils = new RdfConversionUtils();
    //Verify conversion before normalization
    final RDF rdf = rdfConversionUtils.convertInputStreamToRdf(
        getClass().getClassLoader().getResourceAsStream("edm-record-internal.xml"));
    final String record = rdfConversionUtils.convertRdfToString(rdf);

    // Perform actual normalization
    final Normalizer normalizer = new NormalizerFactory().getNormalizer();
    assertNotNull(normalizer);
    final NormalizationBatchResult result = normalizer.normalize(Collections.singletonList(record));
    assertNotNull(result);

    //Verify conversion after normalization
    rdfConversionUtils.convertStringToRdf(result.getNormalizedRecordsInEdmXml().get(0));
  }
}
