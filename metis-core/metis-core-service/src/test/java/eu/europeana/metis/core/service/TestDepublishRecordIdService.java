package eu.europeana.metis.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;

import eu.europeana.metis.core.dao.DepublishRecordIdDao;
import eu.europeana.metis.exception.BadContentException;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class TestDepublishRecordIdService {

  private static DepublishRecordIdService service;
  private static OrchestratorService orchestratorService;
  private static DepublishRecordIdDao depublishRecordIdDao;
  private static Authorizer authorizer;

  @BeforeAll
  static void setupMocks() {
    depublishRecordIdDao = mock(DepublishRecordIdDao.class);
    authorizer = mock(Authorizer.class);
    orchestratorService = mock(OrchestratorService.class);
    service = spy(new DepublishRecordIdService(authorizer, orchestratorService,
        depublishRecordIdDao));
  }

  @AfterEach
  void resetMocks() {
    reset(depublishRecordIdDao, authorizer, service);
  }

  @Test
  void testCheckAndNormalizeRecordId() throws BadContentException {

    // Empty record IDs
    assertFalse(service.checkAndNormalizeRecordId("dataset1", "").isPresent());
    assertFalse(service.checkAndNormalizeRecordId("dataset1", " ").isPresent());

    // Simple IDs with and without spaces
    assertEquals(Optional.of("id1"), service.checkAndNormalizeRecordId("dataset1", "id1"));
    assertEquals(Optional.of("id2"), service.checkAndNormalizeRecordId("dataset1", " id2"));
    assertEquals(Optional.of("id3"), service.checkAndNormalizeRecordId("dataset1", "id3 "));
    assertThrows(BadContentException.class, ()->service.checkAndNormalizeRecordId("dataset1", "id1/"));

    // IDs with dataset prefix
    assertEquals(Optional.of("id1"), service.checkAndNormalizeRecordId("dataset1", "/id1"));
    assertEquals(Optional.of("id2"), service.checkAndNormalizeRecordId("dataset1", "dataset1/id2"));
    assertEquals(Optional.of("id3"), service.checkAndNormalizeRecordId("dataset1", "/dataset1/id3"));
    assertThrows(BadContentException.class, ()->service.checkAndNormalizeRecordId("dataset1", "/dataset1/id1/"));
    assertThrows(BadContentException.class, ()->service.checkAndNormalizeRecordId("dataset1", "/dataset1//id2"));
    assertThrows(BadContentException.class, ()->service.checkAndNormalizeRecordId("dataset1", "dataset2/id3"));
    assertThrows(BadContentException.class, ()->service.checkAndNormalizeRecordId("dataset1", "/dataset2/id1"));

    // IDs with prefixes
    assertEquals(Optional.of("id1"), service.checkAndNormalizeRecordId("dataset1", "a/dataset1/id1"));
    assertEquals(Optional.of("id2"), service.checkAndNormalizeRecordId("dataset1", "http://a/dataset1/id2"));
    assertEquals(Optional.of("id3"), service.checkAndNormalizeRecordId("dataset1", "file://a/dataset1/id3"));

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
