import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tomakehurst.wiremock.WireMockServer;
import eu.europeana.metis.utils.NetworkUtil;
import eu.europeana.validation.client.ValidationClient;
import eu.europeana.validation.model.ValidationResult;
import eu.europeana.validation.model.ValidationResultList;
import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Created by pwozniak on 1/17/18
 */
class ValidationClientTest {

  private static int portForWireMock = 9999;

  static {
    try {
      portForWireMock = NetworkUtil.getAvailableLocalPort();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static WireMockServer wireMockServer;

  @BeforeAll
  static void setUp() {
    wireMockServer = new WireMockServer(wireMockConfig().port(portForWireMock));
    wireMockServer.start();
  }

  @AfterAll
  static void destroy() {
    wireMockServer.stop();
  }


  @Test
  void shouldThrowExceptionForMissingPropertiesFile() {
    assertThrows(NullPointerException.class, ValidationClient::new);
  }

  @Test
  void shouldCreateProperValidationResultForMalformedXml() {
    wireMockServer.resetAll();
    wireMockServer.stubFor(post(urlEqualTo("/schema/validate/EDM-INTERNAL"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withHeader("Accept", "application/json")
            .withBody("{\n" +
                "  \"recordId\": null,\n" +
                "  \"message\": null,\n" +
                "  \"success\": false\n" +
                "}")));

    ValidationClient client = new ValidationClient("http://127.0.0.1:" + portForWireMock);
    ValidationResult result = client.validateRecord("EDM-INTERNAL", "malformedXml");
    assertFalse(result.isSuccess());
    assertNull(result.getMessage());
    assertNull(result.getRecordId());
  }


  @Test
  void shouldCreateProperValidationResultForCorrectXml() {
    wireMockServer.resetAll();
    wireMockServer.stubFor(post(urlEqualTo("/schema/validate/EDM-INTERNAL"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withHeader("Accept", "application/json")
            .withBody("{\n" +
                "  \"recordId\": null,\n" +
                "  \"message\": null,\n" +
                "  \"success\": true\n" +
                "}")));

    ValidationClient client = new ValidationClient("http://127.0.0.1:" + portForWireMock);
    ValidationResult result = client.validateRecord("EDM-INTERNAL", "wellFormedXml");
    assertTrue(result.isSuccess());
    assertNull(result.getMessage());
    assertNull(result.getRecordId());
  }

  @Test
  void shouldCreateProperValidationResultForCorrectZipFile() {
    wireMockServer.resetAll();
    wireMockServer.stubFor(post(urlEqualTo("/schema/validate/batch/EDM-INTERNAL"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withHeader("Accept", "application/json")
            .withBody("{\n" +
                "  \"resultList\": [],\n" +
                "  \"success\": true\n" +
                "}")));

    ValidationClient client = new ValidationClient("http://127.0.0.1:" + portForWireMock);
    ValidationResultList result = client.validateRecordsInFile("EDM-INTERNAL", new File("ok"));
    assertTrue(result.isSuccess());
    assertTrue(result.getResultList().size() == 0);
  }

  @Test
  void shouldCreateProperValidationResultForWrongZipFile() {
    wireMockServer.resetAll();
    wireMockServer.stubFor(post(urlEqualTo("/schema/validate/batch/EDM-INTERNAL"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withHeader("Accept", "application/json")
            .withBody("{\n" +
                "  \"resultList\": [\n" +
                "    {\n" +
                "      \"recordId\": \"sampleId\",\n" +
                "      \"message\": \"sampleMessage\",\n" +
                "      \"success\": false\n" +
                "    }],\n" +
                "  \"success\": true\n" +
                "}")));

    ValidationClient client = new ValidationClient("http://127.0.0.1:" + portForWireMock);
    ValidationResultList result = client.validateRecordsInFile("EDM-INTERNAL", new File("ok"));
    assertTrue(result.isSuccess());
    assertTrue(result.getResultList().size() == 1);
    assertFalse(result.getResultList().get(0).isSuccess());
    assertEquals("sampleId", result.getResultList().get(0).getRecordId());
    assertEquals("sampleMessage", result.getResultList().get(0).getMessage());
  }
}