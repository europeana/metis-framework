package eu.europeana.metis.repository.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class RecordTest {

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
}
