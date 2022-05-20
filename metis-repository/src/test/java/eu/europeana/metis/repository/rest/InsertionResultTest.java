package eu.europeana.metis.repository.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InsertionResultTest {

    private InsertionResult insertionResultToTest;
    private final Instant instantForTest = Instant.now();

    @BeforeEach
    void setUp(){
        insertionResultToTest = new InsertionResult("datasetId", instantForTest);
    }

    @Test
    void testGetDatasetId(){
        assertEquals("datasetId",insertionResultToTest.getDatasetId());
    }

    @Test
    void testGetDateStamp(){
        assertEquals(instantForTest, insertionResultToTest.getDateStamp());
    }

    @Test
    void testAddInsertedRecordAndGetInsertedRecords(){
        setInsertedRecords();
        assertEquals(3, insertionResultToTest.getInsertedRecords());
    }

    @Test
    void testAddUpdatedRecordAndGetUpdatedRecords(){
        setUpdatedRecords();
        assertEquals(3, insertionResultToTest.getUpdatedRecords());
    }

    @Test
    void testGetInsertedRecordIds(){
        setInsertedRecords();
        Set<String> result = insertionResultToTest.getInsertedRecordIds();
        assertTrue(result.contains("recordId1"));
        assertTrue(result.contains("recordId2"));
        assertTrue(result.contains("recordId3"));

    }

    @Test
    void testGetUpdatedRecordIds(){
        setUpdatedRecords();
        Set<String> result = insertionResultToTest.getUpdatedRecordIds();
        assertTrue(result.contains("recordId1"));
        assertTrue(result.contains("recordId2"));
        assertTrue(result.contains("recordId3"));

    }

    private void setInsertedRecords(){
        insertionResultToTest.addInsertedRecord("recordId1");
        insertionResultToTest.addInsertedRecord("recordId2");
        insertionResultToTest.addInsertedRecord("recordId3");
    }

    private void setUpdatedRecords(){
        insertionResultToTest.addUpdatedRecord("recordId1");
        insertionResultToTest.addUpdatedRecord("recordId2");
        insertionResultToTest.addUpdatedRecord("recordId3");
    }
}
