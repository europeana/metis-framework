package eu.europeana.metis.mediaprocessing.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.metis.schema.jibx.ColorSpaceType;
import eu.europeana.metis.schema.jibx.WebResourceType;
import eu.europeana.metis.schema.model.Orientation;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class WebResourceTest {

  @Test
  void testSetWidth() {
    final WebResourceType resourceType = new WebResourceType();

    new WebResource(resourceType).setWidth(123);
    assertNotNull(resourceType.getWidth());
    assertEquals(123L, resourceType.getWidth().getLong());

    new WebResource(resourceType).setWidth(null);
    assertNull(resourceType.getWidth());
  }

  @Test
  void testSetHeight() {
    final WebResourceType resourceType = new WebResourceType();

    new WebResource(resourceType).setHeight(321);
    assertNotNull(resourceType.getHeight());
    assertEquals(321L, resourceType.getHeight().getLong());

    new WebResource(resourceType).setHeight(null);
    assertNull(resourceType.getHeight());
  }

  @Test
  void testSetMimeType() {
    final WebResourceType resourceType = new WebResourceType();
    new WebResource(resourceType).setMimeType("unknown type");
    assertNotNull(resourceType.getHasMimeType());
    assertEquals("unknown type", resourceType.getHasMimeType().getHasMimeType());
  }

  @Test
  void testSetFileSize() {
    final WebResourceType resourceType = new WebResourceType();
    new WebResource(resourceType).setFileSize(456L);
    assertNotNull(resourceType.getFileByteSize());
    assertEquals(456L, resourceType.getFileByteSize().getLong());
  }

  @Test
  void testSetColorspace() {
    final WebResourceType resourceType = new WebResourceType();

    new WebResource(resourceType).setColorspace(ColorSpaceType.CMYK);
    assertNotNull(resourceType.getHasColorSpace());
    assertEquals(ColorSpaceType.CMYK, resourceType.getHasColorSpace().getHasColorSpace());

    new WebResource(resourceType).setColorspace(null);
    assertNull(resourceType.getHasColorSpace());
  }

  @Test
  void testSetOrientation() {
    final WebResourceType resourceType = new WebResourceType();
    final WebResource webResource = new WebResource(resourceType);

    webResource.setOrientation(Orientation.PORTRAIT);
    assertNotNull(resourceType.getOrientation());
    assertEquals("portrait", resourceType.getOrientation().getString());

    webResource.setOrientation(Orientation.LANDSCAPE);
    assertNotNull(resourceType.getOrientation());
    assertEquals("landscape", resourceType.getOrientation().getString());

    webResource.setOrientation(null);
    assertNull(resourceType.getOrientation());
  }

  @Test
  void testSetDominantColors() {
    final WebResourceType resourceType = new WebResourceType();
    final List<String> dominantColors = Arrays.asList("Black", "White");

    new WebResource(resourceType).setDominantColors(dominantColors);
    assertNotNull(resourceType.getComponentColorList());
    assertEquals(2, resourceType.getComponentColorList().size());
    assertEquals(dominantColors.get(0), resourceType.getComponentColorList().get(0).getString());
    assertEquals(dominantColors.get(1), resourceType.getComponentColorList().get(1).getString());

    new WebResource(resourceType).setDominantColors(Collections.emptyList());
    assertNotNull(resourceType.getComponentColorList());
    assertTrue(resourceType.getComponentColorList().isEmpty());
  }

  @Test
  void testSetDuration() {
    final WebResourceType resourceType = new WebResourceType();

    new WebResource(resourceType).setDuration(10.2123);
    assertNotNull(resourceType.getDuration());
    assertEquals("10212", resourceType.getDuration().getDuration());

    new WebResource(resourceType).setDuration(null);
    assertNull(resourceType.getDuration());
  }

  @Test
  void testSetBitrate() {
    final WebResourceType resourceType = new WebResourceType();

    new WebResource(resourceType).setBitrate(654);
    assertNotNull(resourceType.getBitRate());
    assertNotNull(resourceType.getBitRate().getInteger());
    assertEquals(654, resourceType.getBitRate().getInteger().intValue());

    new WebResource(resourceType).setBitrate(null);
    assertNull(resourceType.getBitRate());
  }

  @Test
  void testSetFrameRete() {
    final WebResourceType resourceType = new WebResourceType();

    new WebResource(resourceType).setFrameRate(1.5);
    assertNotNull(resourceType.getFrameRate());
    assertEquals(1.5, resourceType.getFrameRate().getDouble().doubleValue());

    new WebResource(resourceType).setFrameRate(null);
    assertNull(resourceType.getFrameRate());
  }

  @Test
  void testSetCodecName() {
    final WebResourceType resourceType = new WebResourceType();

    new WebResource(resourceType).setCodecName("unknown codec");
    assertNotNull(resourceType.getCodecName());
    assertEquals("unknown codec", resourceType.getCodecName().getCodecName());

    new WebResource(resourceType).setCodecName(null);
    assertNull(resourceType.getCodecName());
  }

  @Test
  void testSetChannels() {
    final WebResourceType resourceType = new WebResourceType();

    new WebResource(resourceType).setChannels(789);
    assertNotNull(resourceType.getAudioChannelNumber());
    assertNotNull(resourceType.getAudioChannelNumber().getInteger());
    assertEquals(789, resourceType.getAudioChannelNumber().getInteger().intValue());

    new WebResource(resourceType).setChannels(null);
    assertNull(resourceType.getAudioChannelNumber());
  }

  @Test
  void testSetSampleRate() {
    final WebResourceType resourceType = new WebResourceType();

    new WebResource(resourceType).setSampleRate(987);
    assertNotNull(resourceType.getSampleRate());
    assertEquals(987L, resourceType.getSampleRate().getLong());

    new WebResource(resourceType).setSampleRate(null);
    assertNull(resourceType.getSampleRate());
  }

  @Test
  void testSetSampleSize() {
    final WebResourceType resourceType = new WebResourceType();

    new WebResource(resourceType).setSampleSize(246);
    assertNotNull(resourceType.getSampleSize());
    assertEquals(246L, resourceType.getSampleSize().getLong());

    new WebResource(resourceType).setSampleSize(null);
    assertNull(resourceType.getSampleSize());
  }

  @Test
  void testSetContainsText() {
    final WebResourceType resourceType = new WebResourceType();
    final WebResource webResource = new WebResource(resourceType);

    webResource.setContainsText(true);
    assertNotNull(resourceType.getType());
    assertEquals(WebResource.FULL_TEXT_RESOURCE, resourceType.getType().getResource());

    webResource.setContainsText(false);
    assertNull(resourceType.getType());
  }

  @Test
  void testSetResolution() {
    final WebResourceType resourceType = new WebResourceType();
    final WebResource webResource = new WebResource(resourceType);

    webResource.setResolution(135);
    assertNotNull(resourceType.getSpatialResolution());
    assertNotNull(resourceType.getSpatialResolution().getInteger());
    assertEquals(135, resourceType.getSpatialResolution().getInteger().intValue());

    webResource.setResolution(null);
    assertNull(resourceType.getSpatialResolution());
  }
}
