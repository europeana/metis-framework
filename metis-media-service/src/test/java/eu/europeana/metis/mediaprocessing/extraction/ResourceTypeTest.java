package eu.europeana.metis.mediaprocessing.extraction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ResourceTypeTest {

  @Test
  void testGetResourceType() {
    assertEquals(ResourceType.IMAGE, ResourceType.getResourceType("image/unknown_type"));
    assertEquals(ResourceType.AUDIO, ResourceType.getResourceType("audio/unknown_type"));
    assertEquals(ResourceType.VIDEO, ResourceType.getResourceType("video/unknown_type"));
    assertEquals(ResourceType.TEXT, ResourceType.getResourceType("text/unknown_type"));
    assertEquals(ResourceType.TEXT, ResourceType.getResourceType("application/xml"));
    assertEquals(ResourceType.TEXT, ResourceType.getResourceType("application/rtf"));
    assertEquals(ResourceType.TEXT, ResourceType.getResourceType("application/epub"));
    assertEquals(ResourceType.TEXT, ResourceType.getResourceType("application/pdf"));
    assertEquals(ResourceType.TEXT, ResourceType.getResourceType("application/xhtml+xml"));
    assertEquals(ResourceType.UNKNOWN, ResourceType.getResourceType("unknown_type"));
    assertEquals(ResourceType.UNKNOWN, ResourceType.getResourceType(null));
  }

  @Test
  void testShouldDownloadMimetype() {
    assertTrue(ResourceType.shouldDownloadMimetype("image/unknown_type"));
    assertTrue(ResourceType.shouldDownloadMimetype("text/unknown_type"));
    assertFalse(ResourceType.shouldDownloadMimetype("audio/unknown_type"));
    assertFalse(ResourceType.shouldDownloadMimetype("video/unknown_type"));
    assertFalse(ResourceType.shouldDownloadMimetype("unknown_type"));
  }
}
