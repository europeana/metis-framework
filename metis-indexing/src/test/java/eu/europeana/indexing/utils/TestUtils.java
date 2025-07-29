package eu.europeana.indexing.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.indexing.common.persistence.solr.v2.SolrV2Field;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.solr.common.SolrInputDocument;

public class TestUtils {

  public static String readFileToString(String file) throws IOException {
    ClassLoader classLoader = TestUtils.class.getClassLoader();
    InputStream inputStream = classLoader.getResourceAsStream(file);
    if (inputStream == null) {
      throw new IOException("Failed reading file " + file);
    }
    return new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining("\n"));
  }

  public static void verifyMap(SolrInputDocument solrInputDocument, SolrV2Field solrV2Field, Map<String, List<String>> map) {
    map.forEach((key, value) -> assertTrue(solrInputDocument.getFieldValues(computeSolrField(
        solrV2Field, key)).containsAll(value)));
  }

  private static String computeSolrField(SolrV2Field label, String value) {
    return label.toString() + "." + value;
  }

  public static void verifyCollection(SolrInputDocument solrInputDocument, SolrV2Field solrV2Field, Collection<String> collection) {
    final Collection<Object> fieldValues = solrInputDocument.getFieldValues(solrV2Field.toString());
    assertTrue(fieldValues.containsAll(collection));
    assertEquals(collection.size(), fieldValues.size());
  }
}
