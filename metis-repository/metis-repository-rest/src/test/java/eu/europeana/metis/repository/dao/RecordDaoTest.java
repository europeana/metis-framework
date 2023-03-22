package eu.europeana.metis.repository.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import eu.europeana.metis.mongo.embedded.EmbeddedLocalhostMongo;
import eu.europeana.metis.repository.rest.dao.Record;
import eu.europeana.metis.repository.rest.dao.RecordDao;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RecordDaoTest {

    private final static String DATABASE_NAME = "dbTest";

    private RecordDao recordDao;

    private EmbeddedLocalhostMongo embeddedLocalhostMongo;

    @BeforeEach
    void setup() {
        embeddedLocalhostMongo = new EmbeddedLocalhostMongo();
        embeddedLocalhostMongo.start();
        final String mongoHost = embeddedLocalhostMongo.getMongoHost();
        final int mongoPort = embeddedLocalhostMongo.getMongoPort();
        final MongoClient mongoClient = MongoClients.create(String.format("mongodb://%s:%s", mongoHost, mongoPort));
        recordDao = new RecordDao(mongoClient, DATABASE_NAME);
    }

    @AfterEach
    void tearDown() {
        embeddedLocalhostMongo.stop();
    }

    @Test
    void createRecordTest_expectTrue(){
        Instant dateStamp = Instant.now();
        Record recordToTest =  new Record("recordId", "datasetId", dateStamp, false, "edmRecord");

        assertTrue(recordDao.createRecord(recordToTest));
    }

    @Test
    void createRecordTest_expectFalse(){
        Instant dateStamp = Instant.now();
        Record recordToTest =  new Record("recordId", "datasetId", dateStamp, false, "edmRecord");
        Record otherRecordToTest =  new Record("recordId", "datasetId", dateStamp, false, "newEdmRecord");
        recordDao.createRecord(recordToTest);

        assertFalse(recordDao.createRecord(otherRecordToTest));
    }

    @Test
    void getAllRecordsFromDatasetTest(){
        Instant dateStamp = Instant.now();
        Record recordToTest =  new Record("recordId", "datasetId", dateStamp, false, "edmRecord");
        Record otherRecordToTest =  new Record("otherRecordId", "datasetId", dateStamp, false, "otherEdmRecord");
        recordDao.createRecord(recordToTest);
        recordDao.createRecord(otherRecordToTest);

        List<Record> result = recordDao.getAllRecordsFromDataset("datasetId").collect(Collectors.toUnmodifiableList());
        assertEquals("datasetId", result.get(0).getDatasetId());
        assertEquals("datasetId", result.get(1).getDatasetId());
        assertEquals("recordId", result.get(0).getRecordId());
        assertEquals("otherRecordId", result.get(1).getRecordId());
        assertEquals(dateStamp.toEpochMilli(), result.get(0).getDateStamp().toEpochMilli());
        assertEquals(dateStamp.toEpochMilli(), result.get(1).getDateStamp().toEpochMilli());
        assertFalse(result.get(0).isDeleted());
        assertFalse(result.get(1).isDeleted());
        assertEquals("edmRecord", result.get(0).getEdmRecord());
        assertEquals("otherEdmRecord", result.get(1).getEdmRecord());

    }

    @Test
    void getRecordTest_expectResult(){
        Instant dateStamp = Instant.now();
        Record recordToTest =  new Record("recordId", "datasetId", dateStamp, false, "edmRecord");
        recordDao.createRecord(recordToTest);

        Record result = recordDao.getRecord("recordId");

        assertEquals("recordId", result.getRecordId());
        assertEquals("datasetId", result.getDatasetId());
        assertEquals(dateStamp.toEpochMilli(), result.getDateStamp().toEpochMilli());
        assertFalse(result.isDeleted());
        assertEquals("edmRecord", result.getEdmRecord());

    }

    @Test
    void getRecordTest_expectNull(){
        assertNull(recordDao.getRecord("recordId"));
    }

    @Test
    void deleteRecord_expectTrue(){
        Instant dateStamp = Instant.now();
        Record recordToTest =  new Record("recordId", "datasetId", dateStamp, false, "edmRecord");
        recordDao.createRecord(recordToTest);

        assertTrue(recordDao.deleteRecord("recordId"));
    }

    @Test
    void deleteRecord_expectFalse(){
        assertFalse(recordDao.deleteRecord("recordId"));
    }
}
