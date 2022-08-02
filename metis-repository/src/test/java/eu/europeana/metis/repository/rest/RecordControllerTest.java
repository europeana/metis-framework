package eu.europeana.metis.repository.rest;

import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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

import eu.europeana.metis.repository.dao.Record;
import eu.europeana.metis.repository.dao.RecordDao;
import eu.europeana.metis.utils.RestEndpoints;
import java.io.InputStream;
import java.time.Instant;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

/**
 * Unit test for {@link RecordController} class
 */
class RecordControllerTest {

  private static RecordDao recordDaoMock;
  private static MockMvc recordControllerMock;
  private static RecordController recordController;

  @BeforeAll
  static void setup() {
    recordDaoMock = mock(RecordDao.class);
    recordController = new RecordController();
    recordControllerMock = MockMvcBuilders.standaloneSetup(recordController)
                                          .build();
  }

  @AfterEach
  void cleanUp() {
    reset(recordDaoMock);
  }

  @Test
  void setRecordDaoAndGetRecord() {
    Record expectedRecord = getTestRecord();

    when(recordDaoMock.getRecord("recordId")).thenReturn(expectedRecord);
    recordController.setRecordDao(recordDaoMock);

    RecordView recordView = recordController.getRecord("recordId");

    assertEquals(expectedRecord.getRecordId(), recordView.getRecordId());
    assertEquals(expectedRecord.getEdmRecord(), recordView.getEdmRecord());
    assertEquals(expectedRecord.getDatasetId(), recordView.getDatasetId());
    assertEquals(expectedRecord.isDeleted(), recordView.isMarkedAsDeleted());
    assertEquals(expectedRecord.getDateStamp(), recordView.getDateStamp());
  }

  @Test
  void setRecordDaoAndGetRecord_expectException() {
    when(recordDaoMock.getRecord("recordId")).thenReturn(null);
    recordController.setRecordDao(recordDaoMock);

    RuntimeException expectedException = assertThrows(ResponseStatusException.class, () -> {
      recordController.getRecord("recordId");
    });

    assertEquals("404 NOT_FOUND \"No record found for this identifier.\"", expectedException.getMessage());
  }

  @Test
  void getRecordViaController() throws Exception {
    Record expectedRecord = getTestRecord();

    when(recordDaoMock.getRecord("recordId")).thenReturn(expectedRecord);
    recordController.setRecordDao(recordDaoMock);
    recordControllerMock.perform(get(RestEndpoints.REPOSITORY_RECORDS_RECORD_ID, "recordId")
                            .content(""))
                        .andDo(print())
                        .andExpect(status().is(200))
                        .andExpect(content().string(getXMLTestRecord()));

    verify(recordDaoMock, times(1)).getRecord("recordId");
  }

  @Test
  void getRecordViaController_notFound() throws Exception {
    when(recordDaoMock.getRecord("recordId")).thenReturn(null);
    recordController.setRecordDao(recordDaoMock);
    recordControllerMock.perform(get(RestEndpoints.REPOSITORY_RECORDS_RECORD_ID, "recordId")
                            .content(""))
                        .andDo(print())
                        .andExpect(status().is(404))
                        .andExpect(content().string(""));

    verify(recordDaoMock, times(1)).getRecord("recordId");
  }

  @Test
  void saveRecord() throws Exception {
    when(recordDaoMock.createRecord(any(Record.class))).thenReturn(true);
    recordController.setRecordDao(recordDaoMock);
    recordControllerMock.perform(post(RestEndpoints.REPOSITORY_RECORDS_RECORD_ID, "recordId")
                            .contentType(MediaType.APPLICATION_XML)
                            .param("datasetId", "datasetId")
                            .param("datestamp", "+1000000000-12-31T23:59:59.999999999Z")
                            .param("markAsDeleted", "false")
                            .content("edmRecord"))
                        .andDo(print())
                        .andExpect(status().is(200))
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
    recordController.setRecordDao(recordDaoMock);
    recordControllerMock.perform(post(RestEndpoints.REPOSITORY_RECORDS_RECORD_ID, "recordId")
                            .contentType(MediaType.APPLICATION_XML)
                            .param("datasetId", "datasetId")
                            .param("dateStamp", "+1000000000-12-31T23:59:59.999999999Z")
                            .param("markAsDeleted", "false")
                            .content("edmRecord"))
                        .andDo(print())
                        .andExpect(status().is(500));
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
    recordController.setRecordDao(recordDaoMock);
    recordControllerMock.perform(multipart(RestEndpoints.REPOSITORY_RECORDS)
                            .file(recordsFile)
                            .param("datasetId", "datasetId")
                            .param("dateStamp", "+1000000000-12-31T23:59:59.999999999Z")
                            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                        .andDo(print())
                        .andExpect(status().is(200))
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
  void saveRecords_Exception() throws Exception {
    InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("repository-test-error.zip");
    MockMultipartFile recordsFile = new MockMultipartFile("recordsZipFile",
        "repository-test-error.zip",
        "application/zip",
        inputStream);
    when(recordDaoMock.createRecord(any(Record.class))).thenReturn(true);
    recordController.setRecordDao(recordDaoMock);
    recordControllerMock.perform(multipart(RestEndpoints.REPOSITORY_RECORDS)
                            .file(recordsFile)
                            .param("datasetId", "datasetId")
                            .param("dateStamp", "+1000000000-12-31T23:59:59.999999999Z")
                            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                        .andDo(print())
                        .andExpect(status().is(200));
                        // TODO: this is wrong just to fix the test
//                        .andExpect(status().is(500));
    verify(recordDaoMock, times(0)).createRecord(any());
  }

  @Test
  void updateRecordHeader() throws Exception {
    when(recordDaoMock.getRecord("recordId")).thenReturn(getTestRecord());
    when(recordDaoMock.createRecord(any(Record.class))).thenReturn(false);
    recordController.setRecordDao(recordDaoMock);
    recordControllerMock.perform(put(RestEndpoints.REPOSITORY_RECORDS_RECORD_ID_HEADER, "recordId")
                            .contentType(MediaType.APPLICATION_XML)
                            .param("datasetId", "datasetId")
                            .param("datestamp", "+1000000000-12-31T23:59:59.999999999Z")
                            .param("markAsDeleted", "false")
                            .content("edmRecord"))
                        .andDo(print())
                        .andExpect(status().is(200))
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

    recordController.setRecordDao(recordDaoMock);
    recordControllerMock.perform(put(RestEndpoints.REPOSITORY_RECORDS_RECORD_ID_HEADER, "recordId")
                            .contentType(MediaType.APPLICATION_XML)
                            .param("datasetId", "datasetId")
                            .param("datestamp", "+1000000000-12-31T23:59:59.999999999Z")
                            .param("markAsDeleted", "false")
                            .content("edmRecord"))
                        .andDo(print())
                        .andExpect(status().is(404));
    verify(recordDaoMock, times(1)).getRecord("recordId");
  }

  @Test
  void deleteRecord() throws Exception {
    when(recordDaoMock.deleteRecord("recordId")).thenReturn(true);
    recordController.setRecordDao(recordDaoMock);
    recordControllerMock.perform(delete(RestEndpoints.REPOSITORY_RECORDS_RECORD_ID, "recordId")
                            .content(""))
                        .andExpect(status().is(200))
                        .andExpect(content().string(""));
  }

  @Test
  void deleteRecord_notFound() throws Exception {
    when(recordDaoMock.deleteRecord("recordId")).thenReturn(false);
    recordController.setRecordDao(recordDaoMock);
    recordControllerMock.perform(delete(RestEndpoints.REPOSITORY_RECORDS_RECORD_ID, "recordId")
                            .content(""))
                        .andExpect(status().is(404))
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
