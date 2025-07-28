package eu.europeana.indexing.common.fullbean;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import eu.europeana.corelib.solr.entity.WebResourceImpl;
import eu.europeana.metis.schema.jibx.BitRate;
import eu.europeana.metis.schema.jibx.CodecName;
import eu.europeana.metis.schema.jibx.ColorSpaceType;
import eu.europeana.metis.schema.jibx.DoubleType;
import eu.europeana.metis.schema.jibx.Duration;
import eu.europeana.metis.schema.jibx.EdmType;
import eu.europeana.metis.schema.jibx.HasColorSpace;
import eu.europeana.metis.schema.jibx.HasMimeType;
import eu.europeana.metis.schema.jibx.Height;
import eu.europeana.metis.schema.jibx.HexBinaryType;
import eu.europeana.metis.schema.jibx.LongType;
import eu.europeana.metis.schema.jibx.OrientationType;
import eu.europeana.metis.schema.jibx.Type2;
import eu.europeana.metis.schema.jibx.WebResourceType;
import eu.europeana.metis.schema.jibx.Width;
import eu.europeana.metis.schema.model.Orientation;
import java.math.BigInteger;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

class WebResourceFieldInputTest {

  @Test
  void testImageWebResource() {
    final WebResourceType webResourceType = getWebResourceImage();
    WebResourceImpl webResourceImpl = new WebResourceFieldInput().apply(webResourceType);

    assertImageWebResourceImpl(webResourceType, webResourceImpl);
  }

  @Test
  void testOEmbedJsonImageWebResource() {
    final WebResourceType webResourceType = getWebResourceImage();
    final HasMimeType hasMimeType = new HasMimeType();
    hasMimeType.setHasMimeType("application/json+oembed");
    webResourceType.setHasMimeType(hasMimeType);
    WebResourceImpl webResourceImpl = new WebResourceFieldInput().apply(webResourceType);

    assertImageWebResourceImpl(webResourceType, webResourceImpl);
  }

  @Test
  void testOEmbedXmlImageWebResource() {
    final WebResourceType webResourceType = getWebResourceImage();
    final HasMimeType hasMimeType = new HasMimeType();
    hasMimeType.setHasMimeType("application/xml+oembed");
    webResourceType.setHasMimeType(hasMimeType);
    WebResourceImpl webResourceImpl = new WebResourceFieldInput().apply(webResourceType);

    assertImageWebResourceImpl(webResourceType, webResourceImpl);
  }

  @Test
  void testOEmbedJsonImageWebResource_Null_WebResourceMetaInfo() {
    final WebResourceType webResourceType = getWebResourceImage();
    final HasMimeType hasMimeType = new HasMimeType();
    hasMimeType.setHasMimeType("application/json");
    webResourceType.setHasMimeType(hasMimeType);
    WebResourceImpl webResourceImpl = new WebResourceFieldInput().apply(webResourceType);

    assertNull(webResourceImpl.getWebResourceMetaInfo());
  }

  @Test
  void testOEmbedXmlImageWebResource_NotNull_WebResourceMetaInfo() {
    final WebResourceType webResourceType = getWebResourceImage();
    final HasMimeType hasMimeType = new HasMimeType();
    hasMimeType.setHasMimeType("application/xml");
    webResourceType.setHasMimeType(hasMimeType);
    WebResourceImpl webResourceImpl = new WebResourceFieldInput().apply(webResourceType);

    assertNotNull(webResourceImpl.getWebResourceMetaInfo().getTextMetaInfo());
  }

  @Test
  void testVideoWebResource() {
    final WebResourceType webResourceType = getWebResourceVideo();
    WebResourceImpl webResourceImpl = new WebResourceFieldInput().apply(webResourceType);

    assertVideoWebResourceImpl(webResourceType, webResourceImpl);
  }

  @Test
  void testOEmbedJsonVideoVideoWebResource() {
    final WebResourceType webResourceType = getWebResourceVideo();
    final HasMimeType hasMimeType = new HasMimeType();
    hasMimeType.setHasMimeType("application/json+oembed");
    webResourceType.setHasMimeType(hasMimeType);
    WebResourceImpl webResourceImpl = new WebResourceFieldInput().apply(webResourceType);

    assertVideoWebResourceImpl(webResourceType, webResourceImpl);
  }

  @Test
  void testOEmbedXmlVideoVideoWebResource() {
    final WebResourceType webResourceType = getWebResourceVideo();
    final HasMimeType hasMimeType = new HasMimeType();
    hasMimeType.setHasMimeType("application/xml+oembed");
    webResourceType.setHasMimeType(hasMimeType);
    WebResourceImpl webResourceImpl = new WebResourceFieldInput().apply(webResourceType);

    assertVideoWebResourceImpl(webResourceType, webResourceImpl);
  }

  @Test
  void testOEmbedJsonVideoWebResource_Null_WebResourceMetaInfo() {
    final WebResourceType webResourceType = getWebResourceVideo();
    final HasMimeType hasMimeType = new HasMimeType();
    hasMimeType.setHasMimeType("application/json");
    webResourceType.setHasMimeType(hasMimeType);
    WebResourceImpl webResourceImpl = new WebResourceFieldInput().apply(webResourceType);

    assertNull(webResourceImpl.getWebResourceMetaInfo());
  }

  @Test
  void testOEmbedXmlVideoWebResource_NotNull_WebResourceMetaInfo() {
    final WebResourceType webResourceType = getWebResourceVideo();
    final HasMimeType hasMimeType = new HasMimeType();
    hasMimeType.setHasMimeType("application/xml");
    webResourceType.setHasMimeType(hasMimeType);
    WebResourceImpl webResourceImpl = new WebResourceFieldInput().apply(webResourceType);

    assertNotNull(webResourceImpl.getWebResourceMetaInfo().getTextMetaInfo());
  }

  private static void assertImageWebResourceImpl(WebResourceType webResourceType, WebResourceImpl webResourceImpl) {
    assertEquals(webResourceType.getHasMimeType().getHasMimeType(), webResourceImpl.getWebResourceMetaInfo().getImageMetaInfo().getMimeType());
    assertEquals(webResourceType.getFileByteSize().getLong(), webResourceImpl.getWebResourceMetaInfo().getImageMetaInfo().getFileSize());
    assertEquals(Long.valueOf(webResourceType.getHeight().getLong()).intValue(), webResourceImpl.getWebResourceMetaInfo().getImageMetaInfo().getHeight());
    assertEquals(Long.valueOf(webResourceType.getWidth().getLong()).intValue(), webResourceImpl.getWebResourceMetaInfo().getImageMetaInfo().getWidth());
    assertEquals(webResourceType.getHasColorSpace().getHasColorSpace().xmlValue(), webResourceImpl.getWebResourceMetaInfo().getImageMetaInfo().getColorSpace());
    assertEquals(webResourceType.getOrientation().getString(), webResourceImpl.getWebResourceMetaInfo().getImageMetaInfo().getOrientation().name());
    assertArrayEquals(webResourceType.getComponentColorList().stream().map(HexBinaryType::getString).toList().toArray(), webResourceImpl.getWebResourceMetaInfo().getImageMetaInfo().getColorPalette());
  }

  private static void assertVideoWebResourceImpl(WebResourceType webResourceType, WebResourceImpl webResourceImpl) {
    assertEquals(webResourceType.getHasMimeType().getHasMimeType(), webResourceImpl.getWebResourceMetaInfo().getVideoMetaInfo().getMimeType());
    assertEquals(webResourceType.getFileByteSize().getLong(), webResourceImpl.getWebResourceMetaInfo().getVideoMetaInfo().getFileSize());
    assertEquals(Long.valueOf(webResourceType.getHeight().getLong()).intValue(), webResourceImpl.getWebResourceMetaInfo().getVideoMetaInfo().getHeight());
    assertEquals(Long.valueOf(webResourceType.getWidth().getLong()).intValue(), webResourceImpl.getWebResourceMetaInfo().getVideoMetaInfo().getWidth());
    assertEquals(webResourceType.getBitRate().getInteger().intValue(), webResourceImpl.getWebResourceMetaInfo().getVideoMetaInfo().getBitRate());
    assertEquals(webResourceType.getCodecName().getCodecName(), webResourceImpl.getWebResourceMetaInfo().getVideoMetaInfo().getCodec());
    assertEquals(webResourceType.getFrameRate().getDouble(), webResourceImpl.getWebResourceMetaInfo().getVideoMetaInfo().getFrameRate());
    assertEquals(Long.valueOf(webResourceType.getDuration().getDuration()), webResourceImpl.getWebResourceMetaInfo().getVideoMetaInfo().getDuration());
  }

  private static @NotNull WebResourceType getWebResourceImage() {
    WebResourceType webResourceType = new WebResourceType();
    final Type2 type2 = new Type2();
    type2.setType(EdmType.IMAGE);
    webResourceType.setType1(type2);
    final LongType longType = new LongType();
    longType.setLong(1000L);
    webResourceType.setFileByteSize(longType);
    final Height height = new Height();
    height.setLong(500L);
    webResourceType.setHeight(height);
    final Width width = new Width();
    width.setLong(500L);
    webResourceType.setWidth(width);
    final HasColorSpace hasColorSpace = new HasColorSpace();
    hasColorSpace.setHasColorSpace(ColorSpaceType.CMY);
    webResourceType.setHasColorSpace(hasColorSpace);
    final OrientationType orientationType = new OrientationType();
    orientationType.setString(Orientation.LANDSCAPE.name());
    webResourceType.setOrientation(orientationType);
    final HexBinaryType hexBinaryType = new HexBinaryType();
    hexBinaryType.setString("#8FBC8F");
    webResourceType.setComponentColorList(List.of(hexBinaryType));

    final HasMimeType hasMimeType = new HasMimeType();
    hasMimeType.setHasMimeType("image/jpeg");
    webResourceType.setHasMimeType(hasMimeType);
    return webResourceType;
  }

  private static @NotNull WebResourceType getWebResourceVideo() {
    WebResourceType webResourceType = new WebResourceType();
    final Type2 type2 = new Type2();
    type2.setType(EdmType.VIDEO);
    webResourceType.setType1(type2);
    final LongType longType = new LongType();
    longType.setLong(1000L);
    webResourceType.setFileByteSize(longType);
    final Height height = new Height();
    height.setLong(500L);
    webResourceType.setHeight(height);
    final Width width = new Width();
    width.setLong(500L);
    webResourceType.setWidth(width);
    final BitRate bitRate = new BitRate();
    bitRate.setInteger(new BigInteger("1000"));
    webResourceType.setBitRate(bitRate);
    final CodecName codecName = new CodecName();
    codecName.setCodecName("codec");
    webResourceType.setCodecName(codecName);
    final DoubleType doubleType = new DoubleType();
    doubleType.setDouble((double) 1000L);
    webResourceType.setFrameRate(doubleType);
    final Duration duration = new Duration();
    duration.setDuration("1000");
    webResourceType.setDuration(duration);

    final HasMimeType hasMimeType = new HasMimeType();
    hasMimeType.setHasMimeType("video/mpeg");
    webResourceType.setHasMimeType(hasMimeType);
    return webResourceType;
  }
}
