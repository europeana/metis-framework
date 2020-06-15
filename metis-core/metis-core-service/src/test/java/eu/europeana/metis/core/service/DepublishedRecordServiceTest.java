package eu.europeana.metis.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;

import eu.europeana.metis.core.dao.DepublishedRecordDao;
import eu.europeana.metis.exception.BadContentException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class DepublishedRecordServiceTest {

  private static DepublishedRecordService service;
  private static DepublishedRecordDao dao;
  private static Authorizer authorizer;

  @BeforeAll
  static void setupMocks() {
    dao = mock(DepublishedRecordDao.class);
    authorizer = mock(Authorizer.class);
    service = spy(new DepublishedRecordService(authorizer, dao));
  }

  @AfterEach
  void resetMocks() {
    reset(dao, authorizer, service);
  }

  @Test
  void testCheckAndNormalizeRecordId() throws BadContentException {

    // Empty record IDs
    assertNull(service.checkAndNormalizeRecordId("dataset1", ""));
    assertNull(service.checkAndNormalizeRecordId("dataset1", " "));

    // Simple IDs with and without spaces
    assertEquals("id1", service.checkAndNormalizeRecordId("dataset1", "id1"));
    assertEquals("id2", service.checkAndNormalizeRecordId("dataset1", " id2"));
    assertEquals("id3", service.checkAndNormalizeRecordId("dataset1", "id3 "));
    assertThrows(BadContentException.class, ()->service.checkAndNormalizeRecordId("dataset1", "id1/"));

    // IDs with dataset prefix
    assertEquals("id1", service.checkAndNormalizeRecordId("dataset1", "/id1"));
    assertEquals("id2", service.checkAndNormalizeRecordId("dataset1", "dataset1/id2"));
    assertEquals("id3", service.checkAndNormalizeRecordId("dataset1", "/dataset1/id3"));
    assertThrows(BadContentException.class, ()->service.checkAndNormalizeRecordId("dataset1", "/dataset1/id1/"));
    assertThrows(BadContentException.class, ()->service.checkAndNormalizeRecordId("dataset1", "/dataset1//id2"));
    assertThrows(BadContentException.class, ()->service.checkAndNormalizeRecordId("dataset1", "dataset2/id3"));
    assertThrows(BadContentException.class, ()->service.checkAndNormalizeRecordId("dataset1", "/dataset2/id1"));

    // IDs with prefixes
    assertEquals("id1", service.checkAndNormalizeRecordId("dataset1", "a/dataset1/id1"));
    assertEquals("id2", service.checkAndNormalizeRecordId("dataset1", "http://a/dataset1/id2"));
    assertEquals("id3", service.checkAndNormalizeRecordId("dataset1", "file://a/dataset1/id3"));

    // IDs with invalid characters
    assertThrows(BadContentException.class, ()->service.checkAndNormalizeRecordId("dataset1", "dataset1/ id1"));
    assertThrows(BadContentException.class, ()->service.checkAndNormalizeRecordId("dataset1", "dataset1 /id2"));
    assertThrows(BadContentException.class, ()->service.checkAndNormalizeRecordId("dataset1", "dataset 1/id3"));
    assertThrows(BadContentException.class, ()->service.checkAndNormalizeRecordId("dataset1", "test 1/dataset1/id1"));
    assertThrows(BadContentException.class, ()->service.checkAndNormalizeRecordId("dataset1", "dataset1/id-2"));
    assertThrows(BadContentException.class, ()->service.checkAndNormalizeRecordId("dataset1", "dataset1/i?d3"));
    assertThrows(BadContentException.class, ()->service.checkAndNormalizeRecordId("dataset1", "(dataset1)/id1"));
  }
}
