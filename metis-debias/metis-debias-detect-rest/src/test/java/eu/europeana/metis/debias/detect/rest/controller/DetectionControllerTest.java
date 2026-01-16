package eu.europeana.metis.debias.detect.rest.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import eu.europeana.metis.debias.detect.client.DeBiasClient;
import eu.europeana.metis.debias.detect.exceptions.DeBiasBadRequestException;
import eu.europeana.metis.debias.detect.exceptions.DeBiasInternalServerException;
import eu.europeana.metis.debias.detect.model.error.ErrorDeBiasResult;
import eu.europeana.metis.debias.detect.model.request.BiasInputLiterals;
import eu.europeana.metis.debias.detect.model.response.DetectionDeBiasResult;
import eu.europeana.metis.debias.detect.model.response.Metadata;
import eu.europeana.metis.debias.detect.model.response.Tag;
import eu.europeana.metis.debias.detect.model.response.ValueDetection;
import eu.europeana.metis.debias.detect.rest.exceptions.ExceptionResponseHandler;
import eu.europeana.metis.debias.detect.service.BiasDetectService;
import eu.europeana.metis.utils.RestEndpoints;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

class DetectionControllerTest {

  private MockMvc mockMvc;
  private BiasDetectService biasDetectService;

  private static String getDetectionParameterJson() {
    BiasInputLiterals biasInputLiterals = new BiasInputLiterals();
    biasInputLiterals.setUseLLM(true);
    biasInputLiterals.setUseNER(true);
    biasInputLiterals.setValues(List.of(
        "sample title of aboriginal and addict",
        "a second addict sample title",
        "this is a demo of master and slave branch"));
    biasInputLiterals.setLanguage("en");
    ObjectMapper mapper = new ObjectMapper();

    return mapper.writeValueAsString(biasInputLiterals);
  }

  @BeforeEach
  void setUp() {
    biasDetectService = mock(DeBiasClient.class);
    DetectionController detectionController = new DetectionController(biasDetectService);
    mockMvc = MockMvcBuilders.standaloneSetup(detectionController)
                             .setControllerAdvice(new ExceptionResponseHandler())
                             .alwaysDo(MockMvcResultHandlers.print())
                             .build();
  }

  @Test
  void debias_detect_completeRequest_expectSuccess() throws Exception {
    DetectionDeBiasResult detectionResult = new DetectionDeBiasResult();
    ValueDetection valueDetection1 = new ValueDetection();
    valueDetection1.setLanguage("en");
    valueDetection1.setLiteral("sample title of aboriginal and addict");
    Tag tag1 = new Tag();
    tag1.setLength(10);
    tag1.setStart(16);
    tag1.setEnd(26);
    tag1.setUri("http://www.example.org/debias#t_2_en");
    Tag tag2 = new Tag();
    tag2.setStart(31);
    tag2.setEnd(37);
    tag2.setUri("http://www.example.org/debias#t_3_en");
    tag2.setLength(6);
    valueDetection1.setTags(List.of(tag1, tag2));

    detectionResult.setDetections(List.of(valueDetection1));
    Metadata metadata = new Metadata();
    detectionResult.setMetadata(metadata);

    String detectionParameterJson = getDetectionParameterJson();
    when(biasDetectService.detect(any(BiasInputLiterals.class))).thenReturn(detectionResult);

    mockMvc.perform(MockMvcRequestBuilders.post(RestEndpoints.DEBIAS_DETECTION)
                                          .contentType(MediaType.APPLICATION_JSON)
                                          .characterEncoding("utf-8")
                                          .content(detectionParameterJson))
           .andExpect(status().is(200))
           .andExpect(content().contentType(MediaType.APPLICATION_JSON))
           .andExpect(jsonPath("$.results[0].language").value(valueDetection1.getLanguage()))
           .andExpect(jsonPath("$.results[0].literal").value(valueDetection1.getLiteral()))
           .andExpect(jsonPath("$.results[0].tags[0].length").value(tag1.getLength()))
           .andExpect(jsonPath("$.results[0].tags[0].start").value(tag1.getStart()))
           .andExpect(jsonPath("$.results[0].tags[0].end").value(tag1.getEnd()));
  }

  @Test
  void debias_detect_NoLanguageRequest_expectSuccess() throws Exception {
    ErrorDeBiasResult errorResult = new ErrorDeBiasResult();
    errorResult.setDetailList(List.of());

    String detectionParameterJson = getDetectionParameterJson();
    when(biasDetectService.detect(any(BiasInputLiterals.class))).thenReturn(errorResult);

    mockMvc.perform(MockMvcRequestBuilders.post(RestEndpoints.DEBIAS_DETECTION)
                                          .contentType(MediaType.APPLICATION_JSON)
                                          .characterEncoding("utf-8")
                                          .content(detectionParameterJson))
           .andExpect(status().is(200))
           .andExpect(content().contentType(MediaType.APPLICATION_JSON))
           .andExpect(jsonPath("$.detail").value(empty()));
  }

  @Test
  void debias_detect_emptyContentTypeRequest_expectContentTypeNotSupported() throws Exception {
    DetectionDeBiasResult detectionResult = new DetectionDeBiasResult();
    detectionResult.setDetections(List.of());
    Metadata metadata = new Metadata();
    detectionResult.setMetadata(metadata);

    when(biasDetectService.detect(any(BiasInputLiterals.class))).thenReturn(detectionResult);

    mockMvc.perform(MockMvcRequestBuilders.post(RestEndpoints.DEBIAS_DETECTION)
                                          .characterEncoding("utf-8"))
           .andExpect(status().is(400))
           .andExpect(jsonPath("$.errorMessage", containsString("Content-Type is not supported")));
  }

  @Test
  void debias_detect_noBodyRequest_expectBadRequest() throws Exception {
    DetectionDeBiasResult detectionResult = new DetectionDeBiasResult();
    detectionResult.setDetections(List.of());
    Metadata metadata = new Metadata();
    detectionResult.setMetadata(metadata);

    when(biasDetectService.detect(any(BiasInputLiterals.class))).thenReturn(detectionResult);

    mockMvc.perform(MockMvcRequestBuilders.post(RestEndpoints.DEBIAS_DETECTION)
                                          .contentType(MediaType.APPLICATION_JSON)
                                          .content("")
                                          .characterEncoding("utf-8"))
           .andExpect(status().is(400))
           .andExpect(jsonPath("$.errorMessage", containsString("Required request body is missing")));
  }

  @Test
  void debias_detect_expectBadRequest() throws Exception {
    String detectionParameterJson = getDetectionParameterJson();

    when(biasDetectService.detect(any(BiasInputLiterals.class))).thenThrow(new DeBiasBadRequestException("Unprocessable Entity"));

    mockMvc.perform(MockMvcRequestBuilders.post(RestEndpoints.DEBIAS_DETECTION)
                                          .contentType(MediaType.APPLICATION_JSON)
                                          .content(detectionParameterJson)
                                          .characterEncoding("utf-8"))
           .andExpect(status().is(400))
           .andExpect(jsonPath("$.errorMessage", containsString("Unprocessable Entity")));
  }

  @Test
  void debias_detect_expectInternalServerError() throws Exception {
    String detectionParameterJson = getDetectionParameterJson();

    when(biasDetectService.detect(any(BiasInputLiterals.class))).thenThrow(
        new DeBiasInternalServerException("Internal Server Error"));

    mockMvc.perform(MockMvcRequestBuilders.post(RestEndpoints.DEBIAS_DETECTION)
                                          .contentType(MediaType.APPLICATION_JSON)
                                          .content(detectionParameterJson)
                                          .characterEncoding("utf-8"))
           .andExpect(status().is(500))
           .andExpect(jsonPath("$.errorMessage", containsString("Internal Server Error")));
  }
}
