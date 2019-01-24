package eu.europeana.metis.mediaprocessing.model;

import java.math.BigInteger;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import eu.europeana.corelib.definitions.jibx.AudioChannelNumber;
import eu.europeana.corelib.definitions.jibx.BitRate;
import eu.europeana.corelib.definitions.jibx.CodecName;
import eu.europeana.corelib.definitions.jibx.ColorSpaceType;
import eu.europeana.corelib.definitions.jibx.DoubleType;
import eu.europeana.corelib.definitions.jibx.Duration;
import eu.europeana.corelib.definitions.jibx.HasColorSpace;
import eu.europeana.corelib.definitions.jibx.HasMimeType;
import eu.europeana.corelib.definitions.jibx.Height;
import eu.europeana.corelib.definitions.jibx.HexBinaryType;
import eu.europeana.corelib.definitions.jibx.IntegerType;
import eu.europeana.corelib.definitions.jibx.LongType;
import eu.europeana.corelib.definitions.jibx.NonNegativeIntegerType;
import eu.europeana.corelib.definitions.jibx.OrientationType;
import eu.europeana.corelib.definitions.jibx.SampleRate;
import eu.europeana.corelib.definitions.jibx.SampleSize;
import eu.europeana.corelib.definitions.jibx.SpatialResolution;
import eu.europeana.corelib.definitions.jibx.StringType;
import eu.europeana.corelib.definitions.jibx.Type1;
import eu.europeana.corelib.definitions.jibx.WebResourceType;
import eu.europeana.corelib.definitions.jibx.Width;

/**
 * Helper class for manipulating {@link WebResourceType} in RDF files.
 */
class WebResource {

  /**
   * Enum for the permissible values of image orientation.
   */
  public enum Orientation {
    PORTRAIT, LANDSCAPE
  }

  private final WebResourceType resource;

  /**
   * Constructor.
   *
   * @param resource The resource that is being manipulated.
   */
  WebResource(WebResourceType resource) {
    this.resource = resource;
  }

  void setWidth(int width) {
    resource.setWidth(intVal(new Width(), width));
  }

  void setHeight(int height) {
    resource.setHeight(intVal(new Height(), height));
  }

  void setMimeType(String mimeType) {
    HasMimeType hasMimeType = new HasMimeType();
    hasMimeType.setHasMimeType(mimeType);
    resource.setHasMimeType(hasMimeType);
  }

  void setFileSize(long fileSize) {
    resource.setFileByteSize(longVal(fileSize));
  }

  void setColorspace(ColorSpaceType colorSpace) {
    HasColorSpace hasColorSpace = new HasColorSpace();
    hasColorSpace.setHasColorSpace(colorSpace);
    resource.setHasColorSpace(hasColorSpace);
  }

  void setOrientation(Orientation orientation) {
    resource.setOrientation(
        stringVal(new OrientationType(), orientation.name().toLowerCase(Locale.ENGLISH)));
  }

  void setDominantColors(List<String> dominantColors) {
    resource.setComponentColorList(dominantColors.stream().map(c -> {
      HexBinaryType hex = new HexBinaryType();
      hex.setString(c);
      hex.setDatatype("http://www.w3.org/2001/XMLSchema#hexBinary");
      return hex;
    }).collect(Collectors.toList()));
  }

  void setDuration(double seconds) {
    final int millisInSecond = 1000;
    Duration duration2 = new Duration();
    duration2.setDuration(Integer.toString((int) Math.round(seconds * millisInSecond)));
    resource.setDuration(duration2);
  }

  void setBitrate(int bitrate) {
    resource.setBitRate(uintVal(new BitRate(), bitrate));
  }

  void setFrameRete(double frameRate) {
    resource.setFrameRate(doubleVal(frameRate));
  }

  void setCodecName(String codecName) {
    CodecName codecName2 = new CodecName();
    codecName2.setCodecName(codecName);
    resource.setCodecName(codecName2);
  }

  void setChannels(int channels) {
    resource.setAudioChannelNumber(uintVal(new AudioChannelNumber(), channels));
  }

  void setSampleRate(int sampleRate) {
    resource.setSampleRate(intVal(new SampleRate(), sampleRate));
  }

  void setSampleSize(int sampleSize) {
    resource.setSampleSize(intVal(new SampleSize(), sampleSize));
  }

  void setContainsText(boolean containsText) {
    if (containsText) {
      Type1 type = new Type1();
      type.setResource("http://www.europeana.eu/schemas/edm/FullTextResource");
      resource.setType(type);
    } else {
      resource.setType(null);
    }
  }

  void setResolution(Integer resolution) {
    resource.setSpatialResolution(
        resolution == null ? null : uintVal(new SpatialResolution(), resolution));
  }

  private static <T extends IntegerType> T intVal(T element, int value) {
    element.setLong(value);
    element.setDatatype("http://www.w3.org/2001/XMLSchema#integer");
    return element;
  }

  private static <T extends NonNegativeIntegerType> T uintVal(T element, int value) {
    element.setInteger(BigInteger.valueOf(value));
    element.setDatatype("http://www.w3.org/2001/XMLSchema#nonNegativeInteger");
    return element;
  }

  private static LongType longVal(long value) {
    LongType element = new LongType();
    element.setLong(value);
    element.setDatatype("http://www.w3.org/2001/XMLSchema#long");
    return element;
  }

  private static DoubleType doubleVal(double value) {
    DoubleType element = new DoubleType();
    element.setDouble(value);
    element.setDatatype("http://www.w3.org/2001/XMLSchema#double");
    return element;
  }

  private static <T extends StringType> T stringVal(T element, String value) {
    element.setString(value);
    element.setDatatype("http://www.w3.org/2001/XMLSchema#string");
    return element;
  }
}
