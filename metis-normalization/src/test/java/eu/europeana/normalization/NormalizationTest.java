package eu.europeana.normalization;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import eu.europeana.normalization.model.NormalizationBatchResult;
import eu.europeana.normalization.util.NormalizationConfigurationException;
import eu.europeana.normalization.util.NormalizationException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

/**
 * Command line test for the NormalizationLanguageClient
 *
 * @author Nuno Freire (nfreire@gmail.com)
 */
class NormalizationTest {

  @Test
  void testNormalization()
      throws IOException, NormalizationException, NormalizationConfigurationException {

    // change to correct file location
    final String record = IOUtils
        .toString(getClass().getClassLoader().getResourceAsStream("edm-record-internal.xml"),
            StandardCharsets.UTF_8);

    // Perform actual normalization
    final Normalizer normalizer = new NormalizerFactory().getNormalizer();
    assertNotNull(normalizer);
    final NormalizationBatchResult result = normalizer.normalize(Collections.singletonList(record));
    assertNotNull(result);
  }
}
