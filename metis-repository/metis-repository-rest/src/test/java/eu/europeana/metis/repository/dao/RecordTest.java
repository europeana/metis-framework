package eu.europeana.metis.repository.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.metis.repository.rest.dao.Record;
import java.time.Instant;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RecordTest {

    private Record recordToTest;
    private final Instant instantUsedForTest = Instant.now();

    @BeforeEach
    void setUp(){
        recordToTest = new Record("recordId", "datasetId", instantUsedForTest, false, "edmRecord");
    }

    @Test
    void testGetRecordId(){
        assertEquals("recordId",recordToTest.getRecordId());
    }

    @Test
    void testGetDatasetId(){
        assertEquals("datasetId",recordToTest.getDatasetId());
    }

    @Test
    void testGetDateStamp(){
        assertEquals(instantUsedForTest,recordToTest.getDateStamp());
    }

    @Test
    void testIsDeleted(){
        assertFalse(recordToTest.isDeleted());
    }

    @Test
    void testEdmRecord(){
        assertEquals("edmRecord",recordToTest.getEdmRecord());
    }

    @Test
    void testSetAndGetId(){
        assertNull(recordToTest.getId());
        ObjectId objectId = new ObjectId();
        recordToTest.setId(objectId);
        assertEquals(objectId, recordToTest.getId());
    }

    @Test
    void testSetRecordId(){
        assertEquals("recordId",recordToTest.getRecordId());
        recordToTest.setRecordId("newRecordId");
        assertEquals("newRecordId", recordToTest.getRecordId());
    }

    @Test
    void testSetDatasetId(){
        assertEquals("datasetId",recordToTest.getDatasetId());
        recordToTest.setDatasetId("newDatasetId");
        assertEquals("newDatasetId", recordToTest.getDatasetId());
    }

    @Test
    void testSetDateStamp(){
        assertEquals(instantUsedForTest,recordToTest.getDateStamp());
        Instant instantNew = Instant.now();
        recordToTest.setDateStamp(instantNew);
        assertEquals(instantNew, recordToTest.getDateStamp());
    }

    @Test
    void testSetDeleted(){
        assertFalse(recordToTest.isDeleted());
        recordToTest.setDeleted(true);
        assertTrue( recordToTest.isDeleted());
    }

    @Test
    void testSetEdmRecord(){
        assertEquals("edmRecord",recordToTest.getEdmRecord());
        recordToTest.setEdmRecord("newEdmRecord");
        assertEquals("newEdmRecord", recordToTest.getEdmRecord());
    }
}
