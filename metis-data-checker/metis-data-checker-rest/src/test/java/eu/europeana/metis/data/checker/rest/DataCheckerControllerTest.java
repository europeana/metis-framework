package eu.europeana.metis.data.checker.rest;

import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;
import eu.europeana.metis.data.checker.common.exception.DataCheckerServiceException;
import eu.europeana.metis.data.checker.common.exception.ZipFileException;
import eu.europeana.metis.data.checker.common.model.ExtendedValidationResult;
import eu.europeana.metis.data.checker.exceptions.handler.ValidationExceptionHandler;
import eu.europeana.metis.data.checker.service.DataCheckerService;
import eu.europeana.metis.data.checker.service.ZipService;

/**
 * Created by erikkonijnenburg on 27/07/2017.
 */
public class DataCheckerControllerTest {

  public static final MediaType APPLICATION_JSON_UTF8 =
      new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(),
          StandardCharsets.UTF_8);

  public static String DATASET_ID = "datasetId";

  private DataCheckerService dataCheckerService;
  private MockMvc datasetControllerMock;
  private ZipService zipService;


  @Before
  public void setUp() throws Exception {
    dataCheckerService = mock(DataCheckerService.class);
    zipService = mock(ZipService.class);

    DataCheckerController dataCheckerController =
        new DataCheckerController(dataCheckerService, zipService, () -> DATASET_ID);
    datasetControllerMock = MockMvcBuilders.standaloneSetup(dataCheckerController)
        .setControllerAdvice(new ValidationExceptionHandler()).build();
  }

  @Test
  public void dataCheckerUpload_withOkzipfile_returnsResults() throws Exception {
    MockMultipartFile fileMock = createMockMultipartFile();
    List<String> list = new ArrayList<>();

    ExtendedValidationResult result = getExtendedValidationResult();

    when(zipService.readFileToStringList(any(MultipartFile.class))).thenReturn(list);
    when(dataCheckerService.createRecords(eq(list), any(), anyBoolean(), anyBoolean()))
        .thenReturn(result);

    datasetControllerMock
        .perform(fileUpload("/upload").file(fileMock).contentType(MediaType.MULTIPART_FORM_DATA)
            .accept(APPLICATION_JSON_UTF8).param("organizationId", "myOrg"))
        .andExpect(status().is(200)).andExpect(jsonPath("$.resultList", is((String) null)))
        .andExpect(jsonPath("$.success", is(true))).andExpect(jsonPath("$.portalUrl", is("myUri")))
        .andExpect(jsonPath("$.records[0]", is("myRecord")));

    verify(zipService, times(1)).readFileToStringList(any(MultipartFile.class));
    verify(dataCheckerService, times(1)).createRecords(anyList(), any(), eq(true), eq(true));
    verifyNoMoreInteractions(dataCheckerService, zipService);
  }

  @Test
  public void dataCheckerUpload_zipServiceFails_throwsZipException() throws Exception {
    MockMultipartFile fileMock = createMockMultipartFile();

    when(zipService.readFileToStringList(any(MultipartFile.class)))
        .thenThrow(new ZipFileException("myZipException"));

    datasetControllerMock
        .perform(fileUpload("/upload").file(fileMock).contentType(MediaType.MULTIPART_FORM_DATA)
            .accept(APPLICATION_JSON_UTF8).param("organizationId", "myOrg"))
        .andExpect(status().is(400)).andExpect(jsonPath("$.errorMessage", is("myZipException")));
  }

  @Test
  public void dataCheckerUpload_dataCheckerServiceFails_throwsValidationService() throws Exception {
    MockMultipartFile fileMock = createMockMultipartFile();
    List<String> list = new ArrayList<>();

    when(zipService.readFileToStringList(any(MultipartFile.class))).thenReturn(list);
    when(dataCheckerService.createRecords(eq(list), any(), anyBoolean(), anyBoolean()))
        .thenThrow(new DataCheckerServiceException("myException"));

    datasetControllerMock
        .perform(fileUpload("/upload").file(fileMock).contentType(MediaType.MULTIPART_FORM_DATA)
            .accept(APPLICATION_JSON_UTF8).param("organizationId", "myOrg"))
        .andExpect(status().is(500)).andExpect(jsonPath("$.errorMessage", is("myException")));
  }

  @NotNull
  private MockMultipartFile createMockMultipartFile() throws IOException {
    Resource resource = new ClassPathResource("ValidExternalOk.zip");
    return new MockMultipartFile("file", resource.getInputStream());
  }

  @NotNull
  private ExtendedValidationResult getExtendedValidationResult() {
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(0);
    cal.set(2017, 7, 12);
    Date date = cal.getTime();

    ExtendedValidationResult result = new ExtendedValidationResult();
    result.setSuccess(true);
    result.setPortalUrl("myUri");
    result.setDate(date);
    List<String> records = new ArrayList<>();
    result.setRecords(records);
    records.add("myRecord");
    return result;
  }
}
