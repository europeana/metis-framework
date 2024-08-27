package eu.europeana.metis.debias.detect.rest.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.metis.debias.detect.model.DetectionParameter;
import eu.europeana.metis.debias.detect.model.DetectionResult;
import eu.europeana.metis.debias.detect.model.Metadata;
import eu.europeana.metis.debias.detect.model.Tag;
import eu.europeana.metis.debias.detect.model.ValueDetection;
import eu.europeana.metis.debias.detect.rest.client.DebiasClient;
import eu.europeana.metis.debias.detect.rest.exceptions.ExceptionResponseHandler;
import eu.europeana.metis.debias.detect.service.DetectService;
import eu.europeana.metis.utils.RestEndpoints;
import java.util.List;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class DetectionControllerTest {

  private MockMvc mockMvc;
  private DetectService detectService;

  @BeforeEach
  void setUp() {
    detectService = mock(DebiasClient.class);
    DetectionController detectionController = new DetectionController(detectService);
    mockMvc = MockMvcBuilders.standaloneSetup(detectionController)
                             .setControllerAdvice(new ExceptionResponseHandler())
                             .alwaysDo(MockMvcResultHandlers.print())
                             .build();
  }

  @Test
  void debias_detect_completeRequest_expectSuccess() throws Exception {
    DetectionResult detectionResult = new DetectionResult();
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
    DetectionParameter detectionParameter = new DetectionParameter();
    detectionParameter.setValues(List.of(
        "sample title of aboriginal and addict",
        "a second addict sample title",
        "this is a demo of master and slave branch"));
    detectionParameter.setLanguage("en");
    ObjectMapper mapper = new ObjectMapper();
    String detectionParameterJson = mapper.writeValueAsString(detectionParameter);
    String expectedJson = mapper.writeValueAsString(detectionResult);
    when(detectService.detect(any(DetectionParameter.class))).thenReturn(detectionResult);

    mockMvc.perform(MockMvcRequestBuilders.post(RestEndpoints.DEBIAS_DETECTION)
                                          .contentType(MediaType.APPLICATION_JSON)
                                          .characterEncoding("utf-8")
                                          .content(detectionParameterJson))
           .andExpect(status().is(200))
           .andExpect(content().contentType(MediaType.APPLICATION_JSON))
           .andExpect(content().string(expectedJson));
  }

  @Test
  void debias_detect_NoLanguageRequest_expectSuccess() throws Exception {
    DetectionResult detectionResult = new DetectionResult();
    detectionResult.setDetections(List.of());
    Metadata metadata = new Metadata();
    detectionResult.setMetadata(metadata);
    DetectionParameter detectionParameter = new DetectionParameter();
    detectionParameter.setValues(List.of(
        "sample title of aboriginal and addict",
        "a second addict sample title",
        "this is a demo of master and slave branch"));
    detectionParameter.setLanguage("en");
    ObjectMapper mapper = new ObjectMapper();
    String detectionParameterJson = mapper.writeValueAsString(detectionParameter);
    String expectedJson = mapper.writeValueAsString(detectionResult);
    when(detectService.detect(any(DetectionParameter.class))).thenReturn(detectionResult);

    mockMvc.perform(MockMvcRequestBuilders.post(RestEndpoints.DEBIAS_DETECTION)
                                          .contentType(MediaType.APPLICATION_JSON)
                                          .characterEncoding("utf-8")
                                          .content(detectionParameterJson))
           .andExpect(status().is(200))
           .andExpect(content().contentType(MediaType.APPLICATION_JSON))
           .andExpect(content().string(expectedJson));
  }

  @Test
  void debias_detect_emptyContentTypeRequest_expectContentTypeNotSupported() throws Exception {
    DetectionResult detectionResult = new DetectionResult();
    detectionResult.setDetections(List.of());
    Metadata metadata = new Metadata();
    detectionResult.setMetadata(metadata);

    when(detectService.detect(any(DetectionParameter.class))).thenReturn(detectionResult);

    mockMvc.perform(MockMvcRequestBuilders.post(RestEndpoints.DEBIAS_DETECTION)
                                          .characterEncoding("utf-8"))
           .andExpect(status().is(400))
           .andExpect(jsonPath("$.errorMessage", containsString("Content-Type is not supported")));
  }

  @Test
  void debias_detect_noBodyRequest_expectBadRequest() throws Exception {
    DetectionResult detectionResult = new DetectionResult();
    detectionResult.setDetections(List.of());
    Metadata metadata = new Metadata();
    detectionResult.setMetadata(metadata);

    when(detectService.detect(any(DetectionParameter.class))).thenReturn(detectionResult);

    mockMvc.perform(MockMvcRequestBuilders.post(RestEndpoints.DEBIAS_DETECTION)
                                          .contentType(MediaType.APPLICATION_JSON)
                                          .content("")
                                          .characterEncoding("utf-8"))
           .andExpect(status().is(400))
           .andExpect(jsonPath("$.errorMessage", containsString("Required request body is missing")));
  }
}
