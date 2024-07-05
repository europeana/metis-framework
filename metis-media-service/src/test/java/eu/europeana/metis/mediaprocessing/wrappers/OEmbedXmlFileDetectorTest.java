package eu.europeana.metis.mediaprocessing.wrappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.junit.jupiter.api.Test;

class OEmbedXmlFileDetectorTest {

  @Test
  void detect() {
    OEmbedXmlFileDetector detector = new OEmbedXmlFileDetector();

    MediaType result = detector.detect(getClass().getClassLoader().getResourceAsStream("__files/oembed.xml"), new Metadata());

    assertEquals("application/xml+oembed", result.getType() + "/" + result.getSubtype());
  }

  @Test
  void no_detect() {
    OEmbedXmlFileDetector detector = new OEmbedXmlFileDetector();

    MediaType result = detector.detect(getClass().getClassLoader().getResourceAsStream("__files/not_oembed.xml"), new Metadata());

    assertEquals("application/octet-stream", result.getType() + "/" + result.getSubtype());
  }
}
