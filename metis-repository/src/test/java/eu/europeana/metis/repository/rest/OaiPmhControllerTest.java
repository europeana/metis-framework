package eu.europeana.metis.repository.rest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

import eu.europeana.metis.repository.dao.Record;
import eu.europeana.metis.repository.dao.RecordDao;
import eu.europeana.metis.utils.RestEndpoints;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Unit test for {@link OaiPmhController}
 */
class OaiPmhControllerTest {

  private static RecordDao recordDaoMock;
  private static MockMvc oaiPmhControllerMock;
  private static OaiPmhController oaiPmhController;

  @BeforeAll
  static void setup() {
    recordDaoMock = mock(RecordDao.class);
    oaiPmhController = new OaiPmhController();
    oaiPmhControllerMock = MockMvcBuilders.standaloneSetup(oaiPmhController)
                                          .build();
  }

  @AfterEach
  void cleanUp() {
    reset(recordDaoMock);
  }

  @Test
  void oaiPmh_GetRecord() throws Exception {
    final Record expectedRecord = getTestRecord("recordId");
    when(recordDaoMock.getRecord("recordId")).thenReturn(expectedRecord);
    oaiPmhController.setRecordDao(recordDaoMock);

    oaiPmhControllerMock.perform(get(RestEndpoints.REPOSITORY_OAI_ENDPOINT)
                            .contentType(MediaType.APPLICATION_XML)
                            .param("verb", "GetRecord")
                            .param("set", "oaipmhset")
                            .param("metadataPrefix", "edm")
                            .param("identifier", "recordId"))
                        .andDo(print())
                        .andExpect(status().is(200))
                        .andExpect(xpath("//OAI-PMH").exists())
                        .andExpect(xpath("//OAI-PMH/responseDate").exists())
                        .andExpect(xpath("//OAI-PMH/request").exists())
                        .andExpect(xpath("//OAI-PMH/GetRecord").exists())
                        .andExpect(xpath("//OAI-PMH/GetRecord/record").exists())
                        .andExpect(xpath("//OAI-PMH/GetRecord/record/header/identifier").exists())
                        .andExpect(xpath("//OAI-PMH/GetRecord/record/header/identifier/text()").string("recordId"))
                        .andExpect(xpath("//OAI-PMH/GetRecord/record/header/datestamp").exists())
                        .andExpect(xpath("//OAI-PMH/GetRecord/record/header/datestamp/text()").string("2022-05-20"))
                        .andExpect(xpath("//OAI-PMH/GetRecord/record/header/setSpec").exists())
                        .andExpect(xpath("//OAI-PMH/GetRecord/record/header/setSpec/text()").string("datasetId"))
                        .andExpect(xpath("//OAI-PMH/GetRecord/record/metadata").exists());

    verify(recordDaoMock, times(1)).getRecord("recordId");
  }

  @Test
  void oaiPmh_GetRecord_deleted() throws Exception {
    final Record expectedRecord = getTestRecordDeleted();
    when(recordDaoMock.getRecord("recordId")).thenReturn(expectedRecord);
    oaiPmhController.setRecordDao(recordDaoMock);

    oaiPmhControllerMock.perform(get(RestEndpoints.REPOSITORY_OAI_ENDPOINT)
                            .contentType(MediaType.APPLICATION_XML)
                            .param("verb", "GetRecord")
                            .param("set", "oaipmhset")
                            .param("metadataPrefix", "edm")
                            .param("identifier", "recordId"))
                        .andDo(print())
                        .andExpect(status().is(200))
                        .andExpect(xpath("//OAI-PMH").exists())
                        .andExpect(xpath("//OAI-PMH/responseDate").exists())
                        .andExpect(xpath("//OAI-PMH/request").exists())
                        .andExpect(xpath("//OAI-PMH/GetRecord").exists())
                        .andExpect(xpath("//OAI-PMH/GetRecord/record").exists())
                        .andExpect(xpath("//OAI-PMH/GetRecord/record/header/identifier").exists())
                        .andExpect(xpath("//OAI-PMH/GetRecord/record/header[@status='deleted']").exists())
                        .andExpect(xpath("//OAI-PMH/GetRecord/record/header/identifier/text()").string("recordId"))
                        .andExpect(xpath("//OAI-PMH/GetRecord/record/header/datestamp").exists())
                        .andExpect(xpath("//OAI-PMH/GetRecord/record/header/datestamp/text()").string("2022-05-20"))
                        .andExpect(xpath("//OAI-PMH/GetRecord/record/header/setSpec").exists())
                        .andExpect(xpath("//OAI-PMH/GetRecord/record/header/setSpec/text()").string("datasetId"))
                        .andExpect(xpath("//OAI-PMH/GetRecord/record/metadata").exists());

    verify(recordDaoMock, times(1)).getRecord("recordId");
  }

  @Test
  void oaiPmh_GetRecord_UnsupportedMetadataPrefix() throws Exception {
    final Record expectedRecord = getTestRecord("recordId");
    when(recordDaoMock.getRecord("recordId")).thenReturn(expectedRecord);
    oaiPmhController.setRecordDao(recordDaoMock);

    oaiPmhControllerMock.perform(get(RestEndpoints.REPOSITORY_OAI_ENDPOINT)
                            .contentType(MediaType.APPLICATION_XML)
                            .param("verb", "GetRecord")
                            .param("set", "oaipmhset")
                            .param("metadataPrefix", "other")
                            .param("identifier", "recordId"))
                        .andDo(print())
                        .andExpect(status().is(400));
    verify(recordDaoMock, times(0)).getRecord("recordId");
  }

  @Test
  void oaiPmh_GetRecord_No_Identifier() throws Exception {
    final Record expectedRecord = getTestRecord("recordId");
    when(recordDaoMock.getRecord("recordId")).thenReturn(expectedRecord);
    oaiPmhController.setRecordDao(recordDaoMock);

    oaiPmhControllerMock.perform(get(RestEndpoints.REPOSITORY_OAI_ENDPOINT)
                            .contentType(MediaType.APPLICATION_XML)
                            .param("verb", "GetRecord")
                            .param("set", "oaipmhset")
                            .param("metadataPrefix", "edm")
                            .param("identifier", ""))
                        .andDo(print())
                        .andExpect(status().is(400));

    verify(recordDaoMock, times(0)).getRecord("recordId");
  }

  @Test
  void oaiPmh_GetRecord_NotFound() throws Exception {
    when(recordDaoMock.getRecord("recordId")).thenReturn(null);
    oaiPmhController.setRecordDao(recordDaoMock);

    oaiPmhControllerMock.perform(get(RestEndpoints.REPOSITORY_OAI_ENDPOINT)
                            .contentType(MediaType.APPLICATION_XML)
                            .param("verb", "GetRecord")
                            .param("set", "oaipmhset")
                            .param("metadataPrefix", "edm")
                            .param("identifier", "recordId"))
                        .andDo(print())
                        .andExpect(status().is(404));

    verify(recordDaoMock, times(1)).getRecord("recordId");
  }

  @Test
  void oaiPmh_ListIdentifiers() throws Exception {
    when(recordDaoMock.getAllRecordsFromDataset("oaipmhset"))
        .thenReturn(Stream.of(getTestRecord("recordId1"), getTestRecord("recordId2")));
    oaiPmhController.setRecordDao(recordDaoMock);

    oaiPmhControllerMock.perform(get(RestEndpoints.REPOSITORY_OAI_ENDPOINT)
                            .contentType(MediaType.APPLICATION_XML)
                            .param("verb", "ListIdentifiers")
                            .param("set", "oaipmhset")
                            .param("metadataPrefix", "edm")
                            .param("identifier", "recordId"))
                        .andDo(print())
                        .andExpect(status().is(200))
                        .andExpect(xpath("//OAI-PMH").exists())
                        .andExpect(xpath("//OAI-PMH/responseDate").exists())
                        .andExpect(xpath("//OAI-PMH/request").exists())
                        .andExpect(xpath("//OAI-PMH/ListIdentifiers").exists())
                        .andExpect(xpath("//OAI-PMH/ListIdentifiers/header/identifier").nodeCount(2))
                        .andExpect(xpath("//OAI-PMH/ListIdentifiers/header[1]/identifier[1]/text()").string("recordId1"))
                        .andExpect(xpath("//OAI-PMH/ListIdentifiers/header[2]/identifier[1]/text()").string("recordId2"))
                        .andExpect(xpath("//OAI-PMH/ListIdentifiers/header/datestamp").nodeCount(2))
                        .andExpect(xpath("//OAI-PMH/ListIdentifiers/header[1]/datestamp[1]/text()").string("2022-05-20"))
                        .andExpect(xpath("//OAI-PMH/ListIdentifiers/header[2]/datestamp[1]/text()").string("2022-05-20"))
                        .andExpect(xpath("//OAI-PMH/ListIdentifiers/header/setSpec").nodeCount(2))
                        .andExpect(xpath("//OAI-PMH/ListIdentifiers/header[1]/setSpec[1]/text()").string("datasetId"))
                        .andExpect(xpath("//OAI-PMH/ListIdentifiers/header[2]/setSpec[1]/text()").string("datasetId"));

    verify(recordDaoMock, times(1)).getAllRecordsFromDataset("oaipmhset");
  }

  @Test
  void oaiPmh_ListIdentifiers_emptySpec() throws Exception {
    when(recordDaoMock.getAllRecordsFromDataset("oaipmhset"))
        .thenReturn(Stream.of(getTestRecord("recordId1"), getTestRecord("recordId2")));
    oaiPmhController.setRecordDao(recordDaoMock);

    oaiPmhControllerMock.perform(get(RestEndpoints.REPOSITORY_OAI_ENDPOINT)
                            .contentType(MediaType.APPLICATION_XML)
                            .param("verb", "ListIdentifiers")
                            .param("set", "")
                            .param("metadataPrefix", "edm")
                            .param("identifier", "recordId"))
                        .andDo(print())
                        .andExpect(status().is(400));

    verify(recordDaoMock, times(0)).getAllRecordsFromDataset("oaipmhset");
  }

  @Test
  void oaiPmh_ListIdentifiers_emptyHeaders() throws Exception {
    when(recordDaoMock.getAllRecordsFromDataset("oaimphset")).thenReturn(null);
    oaiPmhController.setRecordDao(recordDaoMock);

    oaiPmhControllerMock.perform(get(RestEndpoints.REPOSITORY_OAI_ENDPOINT, "recordId")
                            .contentType(MediaType.APPLICATION_XML)
                            .param("verb", "ListIdentifiers")
                            .param("set", "oaipmhset")
                            .param("metadataPrefix", "edm")
                            .param("identifier", "recordId"))
                        .andDo(print())
                        .andExpect(status().is(404));

    verify(recordDaoMock, times(1)).getAllRecordsFromDataset("oaipmhset");
  }

  @NotNull
  private Record getTestRecord(String recordId) throws IOException, URISyntaxException {
    final Record testRecord = new Record();
    final URI pathResourceFile = this.getClass().getClassLoader().getResource("record-test.xml").toURI();
    final String edmRecord = Files.readString(Paths.get(pathResourceFile), Charset.forName("utf-8"));
    testRecord.setRecordId(recordId);
    testRecord.setEdmRecord(edmRecord);
    testRecord.setDatasetId("datasetId");
    testRecord.setDeleted(false);
    testRecord.setDateStamp(Instant.parse("2022-05-20T23:59:59.00Z"));
    return testRecord;
  }

  @NotNull
  private Record getTestRecordDeleted() throws IOException, URISyntaxException {
    final Record testRecord = getTestRecord("recordId");
    testRecord.setDeleted(true);
    return testRecord;
  }
}
