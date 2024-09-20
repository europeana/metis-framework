package eu.europeana.metis.debias.detect.rest.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.http.JvmProxyConfigurer;
import eu.europeana.metis.debias.detect.model.request.DetectionParameter;
import eu.europeana.metis.debias.detect.client.DeBiasClient;
import eu.europeana.metis.debias.detect.rest.exceptions.ExceptionResponseHandler;
import eu.europeana.metis.debias.detect.service.DetectService;
import eu.europeana.metis.utils.RestEndpoints;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class DetectionControllerTestIT {

  private static final String DEBIAS_HOST = "debias.host";
  private static WireMockServer wireMockServer;
  private MockMvc mockMvc;

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

  @BeforeEach
  void setUp() {
    final DetectService detectService = new DeBiasClient("http://debias.host", 300, 300);
    final DetectionController detectionController = new DetectionController(detectService);
    mockMvc = MockMvcBuilders.standaloneSetup(detectionController)
                             .setControllerAdvice(new ExceptionResponseHandler())
                             .alwaysDo(MockMvcResultHandlers.print())
                             .build();
  }

  @Test
  void detection_successResponse() throws Exception {
    final String successResponse = new String(
        Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("sample_success_response.json"))
               .readAllBytes());
    wireMockServer.stubFor(post("/")
        .withHost(equalTo(DEBIAS_HOST))
        .willReturn(aResponse()
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withBody(successResponse)
            .withStatus(200)));
    DetectionParameter detectionParameter = new DetectionParameter();
    detectionParameter.setValues(List.of(
        "sample title of aboriginal and addict",
        "a second addict sample title",
        "this is a demo of master and slave branch"));
    detectionParameter.setLanguage("en");
    ObjectMapper mapper = new ObjectMapper();
    String detectionParameterJson = mapper.writeValueAsString(detectionParameter);

    mockMvc.perform(MockMvcRequestBuilders.post(RestEndpoints.DEBIAS_DETECTION)
                                          .contentType(MediaType.APPLICATION_JSON)
                                          .characterEncoding("utf-8")
                                          .content(detectionParameterJson))
           .andExpect(status().is(200))
           .andExpect(content().contentType(MediaType.APPLICATION_JSON))
           .andExpect(content().json(successResponse));
  }

  @Test
  void detection_error_null_language_successResponse() throws Exception {
    final String errorResponse = new String(
        Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("sample_error_null_language.json"))
               .readAllBytes());
    wireMockServer.stubFor(post("/")
        .withHost(equalTo(DEBIAS_HOST))
        .willReturn(aResponse()
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withStatus(422)
            .withBody(errorResponse)
        ));
    DetectionParameter detectionParameter = new DetectionParameter();
    detectionParameter.setValues(List.of(
        "sample title of aboriginal and addict",
        "a second addict sample title",
        "this is a demo of master and slave branch"));
    detectionParameter.setLanguage(null);
    ObjectMapper mapper = new ObjectMapper();
    String detectionParameterJson = mapper.writeValueAsString(detectionParameter);

    mockMvc.perform(MockMvcRequestBuilders.post(RestEndpoints.DEBIAS_DETECTION)
                                          .contentType(MediaType.APPLICATION_JSON)
                                          .characterEncoding("utf-8")
                                          .content(detectionParameterJson))
           .andExpect(status().is(400))
           .andExpect(content().contentType(MediaType.APPLICATION_JSON))
           .andExpect(content().json(
               "{\"statusCode\":400,\"errorMessage\":\"422 UNPROCESSABLE_ENTITY string_type Input should be a valid string\"}"));
  }

  @Test
  void detection_error_null_values_successResponse() throws Exception {
    final String errorResponse = new String(
        Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("sample_error_null_values.json"))
               .readAllBytes());
    wireMockServer.stubFor(post("/")
        .withHost(equalTo(DEBIAS_HOST))
        .willReturn(aResponse()
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withStatus(422)
            .withBody(errorResponse)
        ));
    DetectionParameter detectionParameter = new DetectionParameter();
    detectionParameter.setValues(null);
    detectionParameter.setLanguage("en");
    ObjectMapper mapper = new ObjectMapper();
    String detectionParameterJson = mapper.writeValueAsString(detectionParameter);

    mockMvc.perform(MockMvcRequestBuilders.post(RestEndpoints.DEBIAS_DETECTION)
                                          .contentType(MediaType.APPLICATION_JSON)
                                          .characterEncoding("utf-8")
                                          .content(detectionParameterJson))
           .andExpect(status().is(400))
           .andExpect(content().contentType(MediaType.APPLICATION_JSON))
           .andExpect(content().json(
               "{\"statusCode\":400,\"errorMessage\":\"422 UNPROCESSABLE_ENTITY list_type Input should be a valid list\"}"));
  }

  @Test
  void detection_error_null_body_successResponse() throws Exception {
    String errorResponse = new String(
        Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("sample_error_null_body.json"))
               .readAllBytes());
    wireMockServer.stubFor(post("/")
        .withHost(equalTo(DEBIAS_HOST))
        .willReturn(aResponse()
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withStatus(422)
            .withBody(errorResponse)
        ));

    mockMvc.perform(MockMvcRequestBuilders.post(RestEndpoints.DEBIAS_DETECTION)
                                          .contentType(MediaType.APPLICATION_JSON)
                                          .characterEncoding("utf-8")
                                          .content("{}"))
           .andExpect(status().is(400))
           .andExpect(content().contentType(MediaType.APPLICATION_JSON))
           .andExpect(
               content().json("{\"statusCode\":400,\"errorMessage\":\"422 UNPROCESSABLE_ENTITY missing Field required\"}"));
  }

  @Test
  void detection_error_bad_gateway_successResponse() throws Exception {
    final String errorResponse = new String(
        Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("sample_error_bad_gateway.json"))
               .readAllBytes());
    final String errorAPIResponse = new String(
        Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("sample_error_bad_gateway.html"))
               .readAllBytes());
    wireMockServer.stubFor(post("/")
        .withHost(equalTo(DEBIAS_HOST))
        .willReturn(aResponse()
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withStatus(502)
            .withBody(errorAPIResponse)));
    DetectionParameter detectionParameter = new DetectionParameter();
    detectionParameter.setValues(List.of(
        "sample title of aboriginal and addict",
        "a second addict sample title",
        "this is a demo of master and slave branch"));
    detectionParameter.setLanguage(null);
    ObjectMapper mapper = new ObjectMapper();
    String detectionParameterJson = mapper.writeValueAsString(detectionParameter);

    mockMvc.perform(MockMvcRequestBuilders.post(RestEndpoints.DEBIAS_DETECTION)
                                          .contentType(MediaType.APPLICATION_JSON)
                                          .characterEncoding("utf-8")
                                          .content(detectionParameterJson))
           .andExpect(status().is(500))
           .andExpect(content().contentType(MediaType.APPLICATION_JSON))
           .andExpect(content().json(errorResponse));
  }

  @Test
  void detection_error_missing_body_successResponse() throws Exception {
    String errorResponse = new String(
        Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("sample_error_missing_body.json"))
               .readAllBytes());
    wireMockServer.stubFor(post("/")
        .withHost(equalTo(DEBIAS_HOST))
        .willReturn(aResponse()
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withStatus(422)
        ));

    mockMvc.perform(MockMvcRequestBuilders.post(RestEndpoints.DEBIAS_DETECTION)
                                          .contentType(MediaType.APPLICATION_JSON)
                                          .characterEncoding("utf-8")
                                          .content("{}"))
           .andExpect(status().is(400))
           .andExpect(content().contentType(MediaType.APPLICATION_JSON))
           .andExpect(content().json(errorResponse));
  }
}
