package eu.europeana.metis.harvesting.oaipmh;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.entity.ContentType.APPLICATION_XML;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;

public abstract class WiremockHelper {

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(8181));

  protected static ResponseDefinitionBuilder response200XmlContent(String fileContent) {
    return aResponse()
            .withHeader(CONTENT_TYPE, APPLICATION_XML.getMimeType())
            .withStatus(200)
            .withBody(fileContent);
  }

  protected static ResponseDefinitionBuilder response404() {
    return aResponse()
            .withHeader(CONTENT_TYPE, APPLICATION_XML.getMimeType())
            .withStatus(404);
  }

  public static String getFileContent(String name) throws IOException {
    return IOUtils.toString(
            WiremockHelper.class.getResourceAsStream(name),
            StandardCharsets.UTF_8);
  }

  public static ResponseDefinitionBuilder responsTimeoutGreaterThanSocketTimeout(String fileContent,
          int timeout) {
    return aResponse()
            .withHeader(CONTENT_TYPE, APPLICATION_XML.getMimeType())
            .withStatus(200)
            .withBody(fileContent)
            .withFixedDelay((int) (1.1 * (double) timeout));
  }
}
