package eu.europeana.metis.harvesting;

import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import eu.europeana.metis.harvesting.oaipmh.CloseableHttpOaiClient;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;
import org.apache.commons.io.IOUtils;
import org.xmlunit.matchers.CompareMatcher;

public class TestHelper {

  protected static final int TEST_RETRIES = 1;
  protected static final int TEST_SLEEP_TIME = 500; /* = 0.5sec */
  protected static final int TEST_SOCKET_TIMEOUT = 1_500; /* = 1.5sec */

  protected static final Function<String, CloseableHttpOaiClient> CONNECTION_CLIENT_FACTORY =
          endpoint -> new CloseableHttpOaiClient(endpoint, new HarvestingClientSettings()
                          .setUserAgent(null)
                          .setNumberOfRetries(TEST_RETRIES)
                          .setTimeBetweenRetries(TEST_SLEEP_TIME)
                          .setRequestTimeout(TEST_SOCKET_TIMEOUT)
                          .setConnectionTimeout(TEST_SOCKET_TIMEOUT)
                          .setSocketTimeout(TEST_SOCKET_TIMEOUT));

  private TestHelper() {
    throw new UnsupportedOperationException("Pure static class!");
  }

  public static String convertToString(InputStream result) throws IOException {
    try (result) {
      return IOUtils.toString(result, StandardCharsets.UTF_8);
    }
  }

  public static CompareMatcher isSimilarXml(String fileContent) {
    return isSimilarTo(fileContent)
            .ignoreComments()
            .ignoreWhitespace()
            .normalizeWhitespace()
            .throwComparisonFailure();
  }
}
