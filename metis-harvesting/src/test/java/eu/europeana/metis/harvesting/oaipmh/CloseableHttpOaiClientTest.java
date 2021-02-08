package eu.europeana.metis.harvesting.oaipmh;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.function.Supplier;
import org.dspace.xoai.serviceprovider.exceptions.HttpException;
import org.dspace.xoai.serviceprovider.parameters.Parameters;
import org.junit.Test;

public class CloseableHttpOaiClientTest extends WiremockHelper {

  private static final String ENDPOINT = "ENDPOINT";
  private static final String PATH = "/content";
  private static final String URL = "http://localhost:8181" + PATH;

  protected static final int TEST_RETRIES = 1;
  protected static final int TEST_SLEEP_TIME = 2_000; /* = 2sec */
  private static final int TEST_SOCKET_TIMEOUT = 5_000; /* = 5sec */

  private static final Supplier<CloseableHttpOaiClient> CONNECTION_CLIENT_FACTORY = () ->
          new CloseableHttpOaiClient(ENDPOINT, null, TEST_RETRIES, TEST_SLEEP_TIME,
                  TEST_SOCKET_TIMEOUT, TEST_SOCKET_TIMEOUT, TEST_SOCKET_TIMEOUT);

  @Test
  public void shouldReturnACorrectValue() throws HttpException, IOException {
    final String fileContent = "FILE CONTENT";
    stubFor(get(urlEqualTo(PATH)).willReturn(response200XmlContent(fileContent)));
    final Parameters parameters = mock(Parameters.class);
    when(parameters.toUrl(ENDPOINT)).thenReturn(URL);
    try (final CloseableHttpOaiClient client = CONNECTION_CLIENT_FACTORY.get()) {
      assertEquals(fileContent, TestHelper.convertToString(client.execute(parameters)));
    }
  }

  @Test(expected = HttpException.class)
  public void shouldHandleTimeout() throws HttpException {
    stubFor(get(urlEqualTo(PATH)).willReturn(
            responsTimeoutGreaterThanSocketTimeout("FILE CONTENT", TEST_SOCKET_TIMEOUT)));
    final Parameters parameters = mock(Parameters.class);
    when(parameters.toUrl(ENDPOINT)).thenReturn(URL);
    try (final CloseableHttpOaiClient client = CONNECTION_CLIENT_FACTORY.get()) {
      client.execute(parameters);
    }
  }

  @Test(expected = HttpException.class)
  public void shouldRetryAndFail() throws HttpException {
    stubFor(get(urlEqualTo(PATH)).willReturn(response404()));
    final Parameters parameters = mock(Parameters.class);
    when(parameters.toUrl(ENDPOINT)).thenReturn(URL);
    try (final CloseableHttpOaiClient client = CONNECTION_CLIENT_FACTORY.get()) {
      client.execute(parameters);
    }
  }

  @Test
  public void shouldRetryAndReturnACorrectValue() throws Exception {
    final String fileContent = "FILE CONTENT";
    stubFor(get(urlEqualTo(PATH)).inScenario("Retry and success scenario")
            .whenScenarioStateIs(STARTED).willSetStateTo("one time requested")
            .willReturn(response404()));
    stubFor(get(urlEqualTo(PATH)).inScenario("Retry and success scenario")
            .whenScenarioStateIs("one time requested")
            .willReturn(response200XmlContent(fileContent)));
    final Parameters parameters = mock(Parameters.class);
    when(parameters.toUrl(ENDPOINT)).thenReturn(URL);
    try(final CloseableHttpOaiClient client = CONNECTION_CLIENT_FACTORY.get()){
      assertEquals(fileContent, TestHelper.convertToString(client.execute(parameters)));
    }
  }
}
