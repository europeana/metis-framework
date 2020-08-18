package eu.europeana.metis.core.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import eu.europeana.metis.exception.BadContentException;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class TestDepublishRecordIdUtils {

  @Test
  void testCheckAndNormalizeRecordId() throws BadContentException {

    // Empty record IDs
    assertFalse(DepublishRecordIdUtils.checkAndNormalizeRecordId("dataset1", "").isPresent());
    assertFalse(DepublishRecordIdUtils.checkAndNormalizeRecordId("dataset1", " ").isPresent());

    // Simple IDs with and without spaces
    assertEquals(Optional.of("id1"), DepublishRecordIdUtils
            .checkAndNormalizeRecordId("dataset1", "id1"));
    assertEquals(Optional.of("id2"), DepublishRecordIdUtils
            .checkAndNormalizeRecordId("dataset1", " id2"));
    assertEquals(Optional.of("id3"), DepublishRecordIdUtils
            .checkAndNormalizeRecordId("dataset1", "id3 "));
    assertThrows(BadContentException.class, ()-> DepublishRecordIdUtils
            .checkAndNormalizeRecordId("dataset1", "id1/"));

    // IDs with dataset prefix
    assertEquals(Optional.of("id1"), DepublishRecordIdUtils
            .checkAndNormalizeRecordId("dataset1", "/id1"));
    assertEquals(Optional.of("id2"), DepublishRecordIdUtils
            .checkAndNormalizeRecordId("dataset1", "dataset1/id2"));
    assertEquals(Optional.of("id3"), DepublishRecordIdUtils
            .checkAndNormalizeRecordId("dataset1", "/dataset1/id3"));
    assertThrows(BadContentException.class, ()-> DepublishRecordIdUtils
            .checkAndNormalizeRecordId("dataset1", "/dataset1/id1/"));
    assertThrows(BadContentException.class, ()-> DepublishRecordIdUtils
            .checkAndNormalizeRecordId("dataset1", "/dataset1//id2"));
    assertThrows(BadContentException.class, ()-> DepublishRecordIdUtils
            .checkAndNormalizeRecordId("dataset1", "dataset2/id3"));
    assertThrows(BadContentException.class, ()-> DepublishRecordIdUtils
            .checkAndNormalizeRecordId("dataset1", "/dataset2/id1"));

    // IDs with prefixes
    assertEquals(Optional.of("id1"), DepublishRecordIdUtils
            .checkAndNormalizeRecordId("dataset1", "a/dataset1/id1"));
    assertEquals(Optional.of("id2"), DepublishRecordIdUtils
            .checkAndNormalizeRecordId("dataset1", "http://a/dataset1/id2"));
    assertEquals(Optional.of("id3"), DepublishRecordIdUtils
            .checkAndNormalizeRecordId("dataset1", "file://a/dataset1/id3"));

    // IDs with invalid characters
    assertThrows(BadContentException.class, ()-> DepublishRecordIdUtils
            .checkAndNormalizeRecordId("dataset1", "dataset1/ id1"));
    assertThrows(BadContentException.class, ()-> DepublishRecordIdUtils
            .checkAndNormalizeRecordId("dataset1", "dataset1 /id2"));
    assertThrows(BadContentException.class, ()-> DepublishRecordIdUtils
            .checkAndNormalizeRecordId("dataset1", "dataset 1/id3"));
    assertThrows(BadContentException.class, ()-> DepublishRecordIdUtils
            .checkAndNormalizeRecordId("dataset1", "test 1/dataset1/id1"));
    assertThrows(BadContentException.class, ()-> DepublishRecordIdUtils
            .checkAndNormalizeRecordId("dataset1", "dataset1/id-2"));
    assertThrows(BadContentException.class, ()-> DepublishRecordIdUtils
            .checkAndNormalizeRecordId("dataset1", "dataset1/i?d3"));
    assertThrows(BadContentException.class, ()-> DepublishRecordIdUtils
            .checkAndNormalizeRecordId("dataset1", "(dataset1)/id1"));
  }
}
