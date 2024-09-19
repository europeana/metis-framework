package eu.europeana.metis.debias.detect.rest.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.http.JvmProxyConfigurer;
import eu.europeana.metis.debias.detect.model.request.DetectionParameter;
import eu.europeana.metis.debias.detect.model.response.DetectionDeBiasResult;
import eu.europeana.metis.debias.detect.rest.exceptions.DeBiasBadRequestException;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.client.ResourceAccessException;

class DeBiasClientTest {

  public static final String DEBIASCLIENT_HOST = "debiasclient.host";
  private static WireMockServer wireMockServer;
  private final DeBiasClient debiasClient = new DeBiasClient("http://" + DEBIASCLIENT_HOST, 300, 300);

  private static void assertMetadata(DetectionDeBiasResult detectionResult) {
    assertEquals("de-bias", detectionResult.getMetadata().getAnnotator());
    assertNull(detectionResult.getMetadata().getThesaurus());
    assertNotNull(detectionResult.getMetadata().getDate());
  }

  private static void assertDetections(DetectionDeBiasResult detectionResult) {
    assertEquals(3, detectionResult.getDetections().size());
    assertNotNull(detectionResult.getDetections().getFirst());
    assertEquals("en", detectionResult.getDetections().getFirst().getLanguage());
    assertEquals("sample title of aboriginal and addict", detectionResult.getDetections().getFirst().getLiteral());
  }

  private static void assertFirstTag(DetectionDeBiasResult detectionResult) {
    assertNotNull(detectionResult.getDetections().getFirst().getTags());
    assertNotNull(detectionResult.getDetections().getFirst().getTags().getFirst());
    assertEquals(16, detectionResult.getDetections().getFirst().getTags().getFirst().getStart());
    assertEquals(26, detectionResult.getDetections().getFirst().getTags().getFirst().getEnd());
    assertEquals(10, detectionResult.getDetections().getFirst().getTags().getFirst().getLength());
    assertEquals("http://www.example.org/debias#t_2_en",
        detectionResult.getDetections().getFirst().getTags().getFirst().getUri());
    assertEquals(0.0, detectionResult.getDetections().getFirst().getTags().getFirst().getScore());
  }

  @BeforeAll
  static void createWireMock() {
    wireMockServer = new WireMockServer(wireMockConfig()
        .dynamicPort()
        .enableBrowserProxying(true)
        .notifier(new ConsoleNotifier(true)));
    wireMockServer.start();
    JvmProxyConfigurer.configureFor(wireMockServer);
  }

  @AfterAll
  static void tearDownWireMock() {
    wireMockServer.stop();
  }

  @Test
  void detect_successResponse() throws IOException {
    final String successResponse = new String(
        Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("sample_success_response.json"))
               .readAllBytes());
    wireMockServer.stubFor(post("/")
        .withHost(equalTo(DEBIASCLIENT_HOST))
        .willReturn(aResponse()
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withBody(successResponse)
            .withStatus(200)));
    DetectionParameter detectionParameter = new DetectionParameter();
    detectionParameter.setLanguage("en");
    detectionParameter.setValues(List.of(
        "sample title of aboriginal and addict",
        "a second addict sample title",
        "this is a demo of master and slave branch"));

    DetectionDeBiasResult detectionResult = (DetectionDeBiasResult) debiasClient.detect(detectionParameter);

    assertNotNull(detectionResult);
    assertMetadata(detectionResult);
    assertDetections(detectionResult);
    assertFirstTag(detectionResult);
  }

  @Test
  void detect_errorResponse() throws IOException {
    final String errorResponse = new String(
        Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("sample_error_null_language.json"))
               .readAllBytes());
    wireMockServer.stubFor(post("/")
        .withHost(equalTo(DEBIASCLIENT_HOST))
        .willReturn(aResponse()
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withBody(errorResponse)
            .withStatus(422)));
    DetectionParameter detectionParameter = new DetectionParameter();
    detectionParameter.setLanguage(null);
    detectionParameter.setValues(List.of(
        "sample title of aboriginal and addict",
        "a second addict sample title",
        "this is a demo of master and slave branch"));

    DeBiasBadRequestException deBiasBadRequestException = assertThrows(DeBiasBadRequestException.class,
        () -> debiasClient.detect(detectionParameter));

    assertNotNull(deBiasBadRequestException);
    assertEquals("422 UNPROCESSABLE_ENTITY string_type Input should be a valid string", deBiasBadRequestException.getMessage());
  }

  @Test
  void detect_noService_errorResponse() {

    DetectionParameter detectionParameter = new DetectionParameter();
    detectionParameter.setLanguage("en");
    detectionParameter.setValues(List.of(
        "sample title of aboriginal and addict",
        "a second addict sample title",
        "this is a demo of master and slave branch"));

    assertThrows(ResourceAccessException.class, () -> debiasClient.detect(detectionParameter));
  }
}

