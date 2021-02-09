package eu.europeana.metis.harvesting.oaipmh;

import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.xmlunit.matchers.CompareMatcher;

public class TestHelper {

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
