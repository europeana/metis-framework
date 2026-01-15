package eu.europeana.metis.repository.rest;

import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import eu.europeana.metis.repository.rest.controller.RecordController;
import eu.europeana.metis.repository.rest.dao.Record;
import eu.europeana.metis.repository.rest.dao.RecordDao;
import eu.europeana.metis.utils.RestEndpoints;
import java.io.InputStream;
import java.time.Instant;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.ElementSelectors;

@WebMvcTest(RecordController.class)
class RecordControllerTest {

  @MockitoBean
  private RecordDao recordDaoMock;

  private static MockMvc mockMvc;

  @BeforeAll
  static void setup(WebApplicationContext context) {
    mockMvc = MockMvcBuilders.webAppContextSetup(context)
                             .defaultRequest(get("/"))
                             .build();
  }

  @AfterEach
  void cleanUp() {
    reset(recordDaoMock);
  }


  @Test
  void getRecordViaController() throws Exception {
    Record expectedRecord = getTestRecord();

    when(recordDaoMock.getRecord("recordId")).thenReturn(expectedRecord);
    mockMvc.perform(get(RestEndpoints.REPOSITORY_RECORDS_RECORD_ID, "recordId")
               .accept(MediaType.APPLICATION_XML))
           .andDo(print())
           .andExpect(status().is(HttpStatus.OK.value()))
           .andExpect(result -> {
             String actual = result.getResponse().getContentAsString();
             Diff diff = DiffBuilder.compare(getXMLTestRecord())
                                    .withTest(actual)
                                    .ignoreWhitespace()
                                    .ignoreComments()
                                    .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byNameAndText))
                                    .checkForSimilar()
                                    .build();
             assertFalse(diff.hasDifferences(), () -> "XMLs differ: " + diff);
           });

    verify(recordDaoMock, times(1)).getRecord("recordId");
  }

  @Test
  void getRecordViaController_notFound() throws Exception {
    when(recordDaoMock.getRecord("recordId")).thenReturn(null);
    mockMvc.perform(get(RestEndpoints.REPOSITORY_RECORDS_RECORD_ID, "recordId")
               .content(""))
           .andDo(print())
           .andExpect(status().is(HttpStatus.NOT_FOUND.value()))
           .andExpect(content().string(""));

    verify(recordDaoMock, times(1)).getRecord("recordId");
  }

  @Test
  void saveRecord() throws Exception {
    when(recordDaoMock.createRecord(any(Record.class))).thenReturn(true);
    mockMvc.perform(post(RestEndpoints.REPOSITORY_RECORDS_RECORD_ID, "recordId")
               .contentType(MediaType.APPLICATION_XML)
               .param("datasetId", "datasetId")
               .param("datestamp", "+1000000000-12-31T23:59:59.999999999Z")
               .param("markAsDeleted", "false")
               .content("edmRecord"))
           .andDo(print())
           .andExpect(status().is(HttpStatus.OK.value()))
           .andExpect(jsonPath("$.datasetId", is("datasetId")))
           .andExpect(jsonPath("$.dateStamp").exists())
           .andExpect(jsonPath("$.insertedRecords", is(1)))
           .andExpect(jsonPath("$.updatedRecords", is(0)))
           .andExpect(jsonPath("$.insertedRecordIds").isArray())
           .andExpect(jsonPath("$.insertedRecordIds").isNotEmpty())
           .andExpect(jsonPath("$.updatedRecordIds").isArray())
           .andExpect(jsonPath("$.updatedRecordIds").isEmpty());
    verify(recordDaoMock, times(1)).createRecord(any());
  }

  @Test
  void saveRecord_Exception() throws Exception {
    when(recordDaoMock.createRecord(any(Record.class))).thenThrow(new RuntimeException("Fail to save record"));
    mockMvc.perform(post(RestEndpoints.REPOSITORY_RECORDS_RECORD_ID, "recordId")
               .contentType(MediaType.APPLICATION_XML)
               .param("datasetId", "datasetId")
               .param("dateStamp", "+1000000000-12-31T23:59:59.999999999Z")
               .param("markAsDeleted", "false")
               .content("edmRecord"))
           .andDo(print())
           .andExpect(status().is(HttpStatus.INTERNAL_SERVER_ERROR.value()));
    verify(recordDaoMock, times(1)).createRecord(any());
  }

  @Test
  void saveRecords() throws Exception {
    InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("repository-test.zip");
    MockMultipartFile recordsFile = new MockMultipartFile("recordsZipFile",
        "repository-test.zip",
        "application/zip",
        inputStream);
    when(recordDaoMock.createRecord(any(Record.class))).thenReturn(true);
    mockMvc.perform(multipart(RestEndpoints.REPOSITORY_RECORDS)
               .file(recordsFile)
               .param("datasetId", "datasetId")
               .param("dateStamp", "+1000000000-12-31T23:59:59.999999999Z")
               .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
           .andDo(print())
           .andExpect(status().is(HttpStatus.OK.value()))
           .andExpect(jsonPath("$.datasetId", is("datasetId")))
           .andExpect(jsonPath("$.dateStamp").exists())
           .andExpect(jsonPath("$.insertedRecords", is(2)))
           .andExpect(jsonPath("$.updatedRecords", is(0)))
           .andExpect(jsonPath("$.insertedRecordIds").isArray())
           .andExpect(jsonPath("$.insertedRecordIds").isNotEmpty())
           .andExpect(jsonPath("$.updatedRecordIds").isArray())
           .andExpect(jsonPath("$.updatedRecordIds").isEmpty());
    verify(recordDaoMock, times(2)).createRecord(any());
  }

  @Test
  void saveRecords_EmptyZip() throws Exception {
    InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("repository-test-empty.zip");
    MockMultipartFile recordsFile = new MockMultipartFile("recordsZipFile",
        "repository-test-empty.zip",
        "application/zip",
        inputStream);
    when(recordDaoMock.createRecord(any(Record.class))).thenReturn(true);
    mockMvc.perform(multipart(RestEndpoints.REPOSITORY_RECORDS)
               .file(recordsFile)
               .param("datasetId", "datasetId")
               .param("dateStamp", "+1000000000-12-31T23:59:59.999999999Z")
               .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
           .andDo(print())
           .andExpect(status().is(HttpStatus.OK.value()))
           .andReturn();
    verify(recordDaoMock, times(0)).createRecord(any());
  }

  @Test
  void updateRecordHeader() throws Exception {
    when(recordDaoMock.getRecord("recordId")).thenReturn(getTestRecord());
    when(recordDaoMock.createRecord(any(Record.class))).thenReturn(false);
    mockMvc.perform(put(RestEndpoints.REPOSITORY_RECORDS_RECORD_ID_HEADER, "recordId")
               .contentType(MediaType.APPLICATION_XML)
               .param("datasetId", "datasetId")
               .param("datestamp", "+1000000000-12-31T23:59:59.999999999Z")
               .param("markAsDeleted", "false")
               .content("edmRecord"))
           .andDo(print())
           .andExpect(status().is(HttpStatus.OK.value()))
           .andExpect(jsonPath("$.datasetId", is("datasetId")))
           .andExpect(jsonPath("$.dateStamp").exists())
           .andExpect(jsonPath("$.insertedRecords", is(0)))
           .andExpect(jsonPath("$.updatedRecords", is(1)))
           .andExpect(jsonPath("$.insertedRecordIds").isArray())
           .andExpect(jsonPath("$.insertedRecordIds").isEmpty())
           .andExpect(jsonPath("$.updatedRecordIds").isArray())
           .andExpect(jsonPath("$.updatedRecordIds").isNotEmpty());
    verify(recordDaoMock, times(1)).getRecord("recordId");
    verify(recordDaoMock, times(1)).createRecord(any());
  }

  @Test
  void updateRecordHeader_Exception() throws Exception {
    when(recordDaoMock.getRecord("recordId")).thenReturn(null);

    mockMvc.perform(put(RestEndpoints.REPOSITORY_RECORDS_RECORD_ID_HEADER, "recordId")
               .contentType(MediaType.APPLICATION_XML)
               .param("datasetId", "datasetId")
               .param("datestamp", "+1000000000-12-31T23:59:59.999999999Z")
               .param("markAsDeleted", "false")
               .content("edmRecord"))
           .andDo(print())
           .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
    verify(recordDaoMock, times(1)).getRecord("recordId");
  }

  @Test
  void deleteRecord() throws Exception {
    when(recordDaoMock.deleteRecord("recordId")).thenReturn(true);
    mockMvc.perform(delete(RestEndpoints.REPOSITORY_RECORDS_RECORD_ID, "recordId")
               .content(""))
           .andExpect(status().is(HttpStatus.OK.value()))
           .andExpect(content().string(""));
  }

  @Test
  void deleteRecord_notFound() throws Exception {
    when(recordDaoMock.deleteRecord("recordId")).thenReturn(false);
    mockMvc.perform(delete(RestEndpoints.REPOSITORY_RECORDS_RECORD_ID, "recordId")
               .content(""))
           .andExpect(status().is(HttpStatus.NOT_FOUND.value()))
           .andExpect(content().string(""));
  }

  @NotNull
  private Record getTestRecord() {
    Record testRecord = new Record();
    testRecord.setRecordId("recordId");
    testRecord.setEdmRecord("edmRecord");
    testRecord.setDatasetId("datasetId");
    testRecord.setDeleted(false);
    testRecord.setDateStamp(Instant.MAX);
    return testRecord;
  }

  @NotNull
  private String getXMLTestRecord() {
    return "<RecordView>" +
        "<recordId>recordId</recordId>" +
        "<datasetId>datasetId</datasetId>" +
        "<dateStamp>+1000000000-12-31T23:59:59.999999999Z</dateStamp>" +
        "<markedAsDeleted>false</markedAsDeleted>" +
        "<edmRecord>edmRecord</edmRecord>" +
        "</RecordView>";
  }
}
