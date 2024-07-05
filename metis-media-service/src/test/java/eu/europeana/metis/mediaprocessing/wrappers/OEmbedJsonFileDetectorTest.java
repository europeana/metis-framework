package eu.europeana.metis.mediaprocessing.wrappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.junit.jupiter.api.Test;

class OEmbedJsonFileDetectorTest {

  @Test
  void detect() {
    OEmbedJsonFileDetector detector = new OEmbedJsonFileDetector();

    MediaType result = detector.detect(getClass().getClassLoader().getResourceAsStream("__files/oembed.json"), new Metadata());

    assertEquals("application/json+oembed", result.getType() + "/" + result.getSubtype());
  }

  @Test
  void no_detect() {
    OEmbedJsonFileDetector detector = new OEmbedJsonFileDetector();

    MediaType result = detector.detect(getClass().getClassLoader().getResourceAsStream("__files/not_oembed.json"),
        new Metadata());

    assertEquals("application/octet-stream", result.getType() + "/" + result.getSubtype());
  }
}
