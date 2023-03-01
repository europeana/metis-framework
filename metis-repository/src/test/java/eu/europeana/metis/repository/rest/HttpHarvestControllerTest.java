package eu.europeana.metis.repository.rest;

import eu.europeana.metis.repository.dao.Record;
import eu.europeana.metis.repository.dao.RecordDao;
import eu.europeana.metis.repository.rest.controller.HttpHarvestController;
import eu.europeana.metis.utils.RestEndpoints;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HttpHarvestControllerTest {

    private RecordDao recordDaoMock;
    private MockMvc httpHarvestControllerMock;
    private HttpHarvestController httpHarvestController;

    @BeforeEach
    void setup() {
        recordDaoMock = mock(RecordDao.class);
        httpHarvestController = new HttpHarvestController();
        httpHarvestControllerMock = MockMvcBuilders.standaloneSetup(httpHarvestController)
                .build();
    }

    @AfterEach
    void cleanUp() {
        reset(recordDaoMock);
    }

    @Test
    void testGetDatasetRecords_expectSuccess() throws IOException {
        Stream<Record> recordsStreamMock = makeStreamRecords();
        when(recordDaoMock.getAllRecordsFromDataset("datasetId")).thenReturn(recordsStreamMock);
        httpHarvestController.setRecordDao(recordDaoMock);

        ResponseEntity<byte[]> resultZip = httpHarvestController.getDatasetRecords("datasetId");

        assertTrue(allContentIsReturned(resultZip.getBody()));

    }

    @Test
    void testGetDatasetRecords_expectInternalServerError() {
        when(recordDaoMock.getAllRecordsFromDataset("datasetId")).thenThrow(RuntimeException.class);
        httpHarvestController.setRecordDao(recordDaoMock);

        RuntimeException expectedException = assertThrows(ResponseStatusException.class, () -> {
            httpHarvestController.getDatasetRecords("datasetId");
        });

        assertEquals("500 INTERNAL_SERVER_ERROR; nested exception is java.lang.RuntimeException", expectedException.getMessage());

    }

    @Test
    void testGetDatasetRecords_expectNotFoundError() {
        when(recordDaoMock.getAllRecordsFromDataset("datasetId")).thenReturn(Stream.empty());
        httpHarvestController.setRecordDao(recordDaoMock);

        RuntimeException expectedException = assertThrows(ResponseStatusException.class, () -> {
            httpHarvestController.getDatasetRecords("datasetId");
        });

        assertEquals("404 NOT_FOUND \"No records found for this dataset.\"", expectedException.getMessage());

    }

    @Test
    void getDatasetRecordsViaController() throws Exception {
        Stream<Record> recordsStreamMock = makeStreamRecords();
        when(recordDaoMock.getAllRecordsFromDataset("datasetId")).thenReturn(recordsStreamMock);
        httpHarvestController.setRecordDao(recordDaoMock);
        MvcResult resultMvc = httpHarvestControllerMock.perform(get(RestEndpoints.REPOSITORY_HTTP_ENDPOINT_ZIP, "datasetId")
                        .content(""))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();

        assertTrue(allContentIsReturned(resultMvc.getResponse().getContentAsByteArray()));
        verify(recordDaoMock, times(1)).getAllRecordsFromDataset("datasetId");
    }

    @Test
    void getDatasetRecordsViaController_expectInternalError() throws Exception {
        when(recordDaoMock.getAllRecordsFromDataset("datasetId")).thenThrow(RuntimeException.class);
        httpHarvestController.setRecordDao(recordDaoMock);
        httpHarvestControllerMock.perform(get(RestEndpoints.REPOSITORY_HTTP_ENDPOINT_ZIP, "datasetId")
                        .content(""))
                .andDo(print())
                .andExpect(status().is(500))
                .andExpect(content().string(""));

        verify(recordDaoMock, times(1)).getAllRecordsFromDataset("datasetId");
    }

    @Test
    void getDatasetRecordsViaController_expectNotFoundError() throws Exception {
        when(recordDaoMock.getAllRecordsFromDataset("datasetId")).thenReturn(Stream.empty());
        httpHarvestController.setRecordDao(recordDaoMock);
        httpHarvestControllerMock.perform(get(RestEndpoints.REPOSITORY_HTTP_ENDPOINT_ZIP, "datasetId")
                        .content(""))
                .andDo(print())
                .andExpect(status().is(404))
                .andExpect(content().string(""));

        verify(recordDaoMock, times(1)).getAllRecordsFromDataset("datasetId");
    }

    private Stream<Record> makeStreamRecords(){
        Instant instantForTest = Instant.now();
        Record record1 = new Record("recordId1", "datasetId", instantForTest, false, "edmRecord1");
        Record record2 = new Record("recordId2", "datasetId", instantForTest, false, "edmRecord2");
        Record record3 = new Record("recordId3", "datasetId", instantForTest, false, "edmRecord3");
        return Stream.of(record1, record2, record3);
    }

    private boolean allContentIsReturned(byte[] result) throws IOException {
        ZipEntry zEntry;
        boolean areAllRecordsInZip = true;

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(Objects.requireNonNull(result));
        ZipInputStream zipIs = new ZipInputStream(new BufferedInputStream(byteArrayInputStream));
        while((zEntry = zipIs.getNextEntry()) != null){
            areAllRecordsInZip = areAllRecordsInZip && (zEntry.getName().contains("recordId1") || zEntry.getName().contains("recordId2") || zEntry.getName().contains("recordId3"));
        }
        zipIs.close();

        return areAllRecordsInZip;
    }

}
