package eu.europeana.metis.dereference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URISyntaxException;
import org.junit.jupiter.api.Test;

public class ResourceURIGeneratorTest {

  private static final String RECORD_ID = "http://example.com/record/1";

  @Test
  void testIdentityGenerator() throws URISyntaxException {
    assertEquals(RECORD_ID,
        ResourceUriGenerator.identityGenerator().generateUri(RECORD_ID).toString());
    assertEquals(RECORD_ID, ResourceUriGenerator.forSuffix(null).generateUri(RECORD_ID).toString());
    assertEquals(RECORD_ID, ResourceUriGenerator.forSuffix(" ").generateUri(RECORD_ID).toString());
  }

  @Test
  void testSuffixGenerator() throws URISyntaxException {
    final String suffix = ".rdf";
    assertEquals(RECORD_ID + suffix,
        ResourceUriGenerator.forSuffix(suffix).generateUri(RECORD_ID).toString());
  }

  @Test
  void testTemplateGenerator() throws URISyntaxException {
    final String inputId = "https://sws.geonames.org/1123961/";
    final String expectedOutput = "http://geonames.europeana.eu/sparql/?default-graph-uri="
        + "&query=DESCRIBE+%3Chttps%3A%2F%2Fsws.geonames.org%2F1123961%2F%3E"
        + "&format=application%2Frdf%2Bxml&timeout=0&signal_void=on";
    final String template = "http://geonames.europeana.eu/sparql/?default-graph-uri="
        + "&query=DESCRIBE+%3C${ " + ResourceUriGenerator.RESOURCE_ID_FUNCTION
        + " | " + ResourceUriGenerator.URL_QUERY_ESCAPE_FUNCTION + " }%3E"
        + "&format=application%2Frdf%2Bxml&timeout=0&signal_void=on";
    assertEquals(expectedOutput, ResourceUriGenerator.forTemplate(template).generateUri(inputId).toString());
  }

  @Test
  void testInvalidTemplateParameters() {
    assertThrows(IllegalArgumentException.class,
        () -> ResourceUriGenerator.forTemplate("${}").generateUri(RECORD_ID));
    assertThrows(IllegalArgumentException.class,
        () -> ResourceUriGenerator.forTemplate("${ | }").generateUri(RECORD_ID));
    assertThrows(IllegalArgumentException.class, () ->
        ResourceUriGenerator.forTemplate("${" + ResourceUriGenerator.RESOURCE_ID_FUNCTION + "| }")
            .generateUri(RECORD_ID));
    assertThrows(IllegalArgumentException.class, () ->
        ResourceUriGenerator.forTemplate(
            "${|" + ResourceUriGenerator.URL_QUERY_ESCAPE_FUNCTION + "}").generateUri(RECORD_ID));
    assertThrows(IllegalArgumentException.class, () ->
        ResourceUriGenerator.forTemplate(
            "${" + ResourceUriGenerator.URL_QUERY_ESCAPE_FUNCTION + "}").generateUri(RECORD_ID));
    assertThrows(IllegalArgumentException.class, () ->
        ResourceUriGenerator.forTemplate(
            "${" + ResourceUriGenerator.URL_QUERY_ESCAPE_FUNCTION + "| " +
                ResourceUriGenerator.URL_QUERY_ESCAPE_FUNCTION + " }").generateUri(RECORD_ID));
  }
}
