package eu.europeana.normalization;

import static org.junit.Assert.assertNotNull;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import eu.europeana.normalization.model.NormalizationBatchResult;
import eu.europeana.normalization.util.NormalizationConfigurationException;
import eu.europeana.normalization.util.NormalizationException;

/**
 * Command line test for the NormalizationLanguageClient
 *
 * @author Nuno Freire (nfreire@gmail.com)
 */
public class NormalizationTest {

  @Test
  public void testNormalization()
      throws IOException, NormalizationException, NormalizationConfigurationException {

    // change to correct uri
    List<String> recs = new ArrayList<>();

    // change to correct file location
    final InputStream in = getClass().getClassLoader().getResourceAsStream("edm-record.xml");
    recs.add(IOUtils.toString(in, "UTF-8"));
    in.close();

    // change to correct file location
    final InputStream in2 =
        getClass().getClassLoader().getResourceAsStream("edm-record-internal.xml");
    recs.add(IOUtils.toString(in2, "UTF-8"));
    in2.close();

    // Perform actual normalization
    final Normalizer normalizer = new NormalizerFactory().getNormalizer();
    assertNotNull(normalizer);
    final NormalizationBatchResult result = normalizer.normalize(recs);
    assertNotNull(result);
  }
}
