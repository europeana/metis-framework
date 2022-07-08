package eu.europeana.metis.harvesting.oaipmh;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.tomakehurst.wiremock.WireMockServer;
import eu.europeana.metis.network.NetworkUtil;
import java.io.IOException;
import java.util.function.Supplier;
import org.dspace.xoai.serviceprovider.exceptions.HttpException;
import org.dspace.xoai.serviceprovider.parameters.Parameters;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CloseableHttpOaiClientTest {

  private static WireMockServer WIREMOCK_SERVER;
  private static final String ENDPOINT = "ENDPOINT";
  private static final String PATH = "/content";
  private static String URL;
  private static Supplier<CloseableHttpOaiClient> CONNECTION_CLIENT_FACTORY;

  @BeforeAll
  static void prepare() throws IOException {
    int portForWireMock = new NetworkUtil().getAvailableLocalPort();
    final String localhostUrl = "http://127.0.0.1:" + portForWireMock;
    URL = localhostUrl + PATH;
    CONNECTION_CLIENT_FACTORY = () -> TestHelper.CONNECTION_CLIENT_FACTORY.apply(ENDPOINT);
    WIREMOCK_SERVER = new WireMockServer(wireMockConfig().port(portForWireMock));
    WIREMOCK_SERVER.start();
  }

  @Test
  void shouldReturnACorrectValue() throws HttpException, IOException {
    final String fileContent = "FILE CONTENT";
    WIREMOCK_SERVER.stubFor(
        get(urlEqualTo(PATH)).willReturn(WiremockHelper.response200XmlContent(fileContent)));
    final Parameters parameters = mock(Parameters.class);
    when(parameters.toUrl(ENDPOINT)).thenReturn(URL);
    try (final CloseableHttpOaiClient client = CONNECTION_CLIENT_FACTORY.get()) {
      assertEquals(fileContent, TestHelper.convertToString(client.execute(parameters)));
    }
  }

  @Test
  void shouldHandleTimeout() throws HttpException {
    WIREMOCK_SERVER.stubFor(get(urlEqualTo(PATH)).willReturn(WiremockHelper
        .responsTimeoutGreaterThanSocketTimeout("FILE CONTENT", TestHelper.TEST_SOCKET_TIMEOUT)));
    final Parameters parameters = mock(Parameters.class);
    when(parameters.toUrl(ENDPOINT)).thenReturn(URL);
    try (final CloseableHttpOaiClient client = CONNECTION_CLIENT_FACTORY.get()) {
      assertThrows(HttpException.class, () -> client.execute(parameters));
    }
  }

  @Test
  void shouldRetryAndFail() throws HttpException {
    WIREMOCK_SERVER.stubFor(get(urlEqualTo(PATH)).willReturn(WiremockHelper.response404()));
    final Parameters parameters = mock(Parameters.class);
    when(parameters.toUrl(ENDPOINT)).thenReturn(URL);
    try (final CloseableHttpOaiClient client = CONNECTION_CLIENT_FACTORY.get()) {
      assertThrows(HttpException.class, () -> client.execute(parameters));
    }
  }

  @Test
  void shouldRetryAndReturnACorrectValue() throws Exception {
    final String fileContent = "FILE CONTENT";
    WIREMOCK_SERVER.stubFor(
        get(urlEqualTo(PATH)).inScenario("Retry and success scenario").whenScenarioStateIs(STARTED)
                             .willSetStateTo("one time requested").willReturn(WiremockHelper.response404()));
    WIREMOCK_SERVER.stubFor(get(urlEqualTo(PATH)).inScenario("Retry and success scenario")
                                                 .whenScenarioStateIs("one time requested")
                                                 .willReturn(WiremockHelper.response200XmlContent(fileContent)));
    final Parameters parameters = mock(Parameters.class);
    when(parameters.toUrl(ENDPOINT)).thenReturn(URL);
    try (final CloseableHttpOaiClient client = CONNECTION_CLIENT_FACTORY.get()) {
      assertEquals(fileContent, TestHelper.convertToString(client.execute(parameters)));
    }
  }

  @AfterAll
  static void destroy() {
    WIREMOCK_SERVER.stop();
  }
}
