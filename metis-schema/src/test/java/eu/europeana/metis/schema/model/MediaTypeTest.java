package eu.europeana.metis.schema.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MediaTypeTest {

  @Test
  void testGetMediaType() {
    assertEquals(MediaType.IMAGE, MediaType.getMediaType("image/unknown_type"));
    assertEquals(MediaType.AUDIO, MediaType.getMediaType("audio/unknown_type"));
    assertEquals(MediaType.VIDEO, MediaType.getMediaType("video/unknown_type"));
    assertEquals(MediaType.TEXT, MediaType.getMediaType("text/unknown_type"));
    assertEquals(MediaType.TEXT, MediaType.getMediaType("application/xml"));
    assertEquals(MediaType.TEXT, MediaType.getMediaType("application/rtf"));
    assertEquals(MediaType.TEXT, MediaType.getMediaType("application/epub"));
    assertEquals(MediaType.TEXT, MediaType.getMediaType("application/pdf"));
    assertEquals(MediaType.TEXT, MediaType.getMediaType("application/pdf+xml"));
    assertEquals(MediaType.TEXT, MediaType.getMediaType("application/pdf+xml; key=value"));
    assertEquals(MediaType.TEXT, MediaType.getMediaType("application/xhtml+xml"));
    assertEquals(MediaType.OTHER, MediaType.getMediaType("application/xhtml"));
    assertEquals(MediaType.OTHER, MediaType.getMediaType("unknown_type"));
    assertEquals(MediaType.OTHER, MediaType.getMediaType(null));
  }
}

