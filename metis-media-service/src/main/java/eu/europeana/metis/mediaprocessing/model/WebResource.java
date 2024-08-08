package eu.europeana.metis.mediaprocessing.model;

import eu.europeana.metis.schema.jibx.AudioChannelNumber;
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
import eu.europeana.metis.schema.jibx.IntegerType;
import eu.europeana.metis.schema.jibx.LongType;
import eu.europeana.metis.schema.jibx.NonNegativeIntegerType;
import eu.europeana.metis.schema.jibx.OrientationType;
import eu.europeana.metis.schema.jibx.SampleRate;
import eu.europeana.metis.schema.jibx.SampleSize;
import eu.europeana.metis.schema.jibx.SpatialResolution;
import eu.europeana.metis.schema.jibx.StringType;
import eu.europeana.metis.schema.jibx.Type1;
import eu.europeana.metis.schema.jibx.Type2;
import eu.europeana.metis.schema.jibx.WebResourceType;
import eu.europeana.metis.schema.jibx.Width;
import eu.europeana.metis.schema.model.Orientation;
import java.math.BigInteger;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Helper class for manipulating {@link WebResourceType} in RDF files.
 */
class WebResource {

  protected static final String FULL_TEXT_RESOURCE = "http://www.europeana.eu/schemas/edm/FullTextResource";

  private final WebResourceType resource;

  /**
   * Constructor.
   *
   * @param resource The resource that is being manipulated.
   */
  WebResource(WebResourceType resource) {
    this.resource = resource;
  }

  void setWidth(Integer width) {
    resource.setWidth(intVal(Width::new, width));
  }

  void setHeight(Integer height) {
    resource.setHeight(intVal(Height::new, height));
  }

  void setMimeType(String mimeType) {
    HasMimeType hasMimeType = new HasMimeType();
    hasMimeType.setHasMimeType(mimeType);
    resource.setHasMimeType(hasMimeType);
  }

  void setFileSize(Long fileSize) {
    if (fileSize == null) {
      resource.setFileByteSize(null);
    } else {
      resource.setFileByteSize(longVal(fileSize));
    }
  }

  void setColorspace(ColorSpaceType colorSpace) {
    if (colorSpace == null) {
      resource.setHasColorSpace(null);
    } else {
      HasColorSpace hasColorSpace = new HasColorSpace();
      hasColorSpace.setHasColorSpace(colorSpace);
      resource.setHasColorSpace(hasColorSpace);
    }
  }

  void setOrientation(Orientation orientation) {
    resource.setOrientation(orientation == null ? null
        : stringVal(OrientationType::new, orientation.getNameLowercase()));
  }

  void setDominantColors(List<String> dominantColors) {
    resource.setComponentColorList(dominantColors.stream().map(c -> {
      HexBinaryType hex = new HexBinaryType();
      hex.setString(c);
      hex.setDatatype("http://www.w3.org/2001/XMLSchema#hexBinary");
      return hex;
    }).collect(Collectors.toList()));
  }

  void setDuration(Double seconds) {
    if (seconds == null) {
      resource.setDuration(null);
    } else {
      final int millisInSecond = 1000;
      Duration duration2 = new Duration();
      duration2.setDuration(Integer.toString((int) Math.round(seconds * millisInSecond)));
      resource.setDuration(duration2);
    }
  }

  void setBitrate(Integer bitrate) {
    resource.setBitRate(uintVal(BitRate::new, bitrate));
  }

  void setFrameRate(Double frameRate) {
    resource.setFrameRate(doubleVal(frameRate));
  }

  void setCodecName(String codecName) {
    if (codecName == null) {
      resource.setCodecName(null);
    } else {
      CodecName codecName2 = new CodecName();
      codecName2.setCodecName(codecName);
      resource.setCodecName(codecName2);
    }
  }

  void setChannels(Integer channels) {
    resource.setAudioChannelNumber(uintVal(AudioChannelNumber::new, channels));
  }

  void setSampleRate(Integer sampleRate) {
    resource.setSampleRate(intVal(SampleRate::new, sampleRate));
  }

  void setSampleSize(Integer sampleSize) {
    resource.setSampleSize(intVal(SampleSize::new, sampleSize));
  }

  void setContainsText(boolean containsText) {
    if (containsText) {
      Type1 type = new Type1();
      type.setResource(FULL_TEXT_RESOURCE);
      resource.setType(type);
    } else {
      resource.setType(null);
    }
  }

  void setResolution(Integer resolution) {
    resource.setSpatialResolution(uintVal(SpatialResolution::new, resolution));
  }

  void setEdmType(EdmType edmType) {
    if (edmType == null) {
      resource.setType1(null);
    } else if (isValidEdmType(edmType)) {
      Type2 type2 = new Type2();
      type2.setType(edmType);
      resource.setType1(type2);
    } else {
      resource.setType1(null);
    }
  }

  boolean isValidEdmType(EdmType edmType) {
    return edmType.equals(EdmType.IMAGE) || edmType.equals(EdmType.VIDEO);
  }

  private static <T extends IntegerType> T intVal(Supplier<T> constructor, Integer value) {
    if (value == null) {
      return null;
    }
    final T element = constructor.get();
    element.setLong(value);
    element.setDatatype("http://www.w3.org/2001/XMLSchema#integer");
    return element;
  }

  private static <T extends NonNegativeIntegerType> T uintVal(Supplier<T> constructor,
      Integer value) {
    if (value == null) {
      return null;
    }
    final T element = constructor.get();
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

  private static DoubleType doubleVal(Double value) {
    if (value == null) {
      return null;
    }
    DoubleType element = new DoubleType();
    element.setDouble(value);
    element.setDatatype("http://www.w3.org/2001/XMLSchema#double");
    return element;
  }

  private static <T extends StringType> T stringVal(Supplier<T> constructor, String value) {
    final T element = constructor.get();
    element.setString(value);
    element.setDatatype("http://www.w3.org/2001/XMLSchema#string");
    return element;
  }
}
