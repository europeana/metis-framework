package eu.europeana.metis.repository.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link RecordView} class
 */
class RecordViewTest {

  private RecordView recordView;

  @BeforeEach
  void setup() {
    final Instant instant = Instant.parse("2022-05-19T08:28:00.823118Z");
    recordView = new RecordView("recordId", "datasetId", instant, false, "edmRecord");
  }

  @Test
  void getRecordId() {
    assertEquals("recordId", recordView.getRecordId());
  }

  @Test
  void getDatasetId() {
    assertEquals("datasetId", recordView.getDatasetId());
  }

  @Test
  void getDateStamp() {
    assertEquals("2022-05-19T08:28:00.823118Z", recordView.getDateStamp().toString());
  }

  @Test
  void isMarkedAsDeleted() {
    assertFalse(recordView.isMarkedAsDeleted());
  }

  @Test
  void getEdmRecord() {
    assertEquals("edmRecord", recordView.getEdmRecord());
  }
}
