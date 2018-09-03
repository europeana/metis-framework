package eu.europeana.indexing.solr.crf;

import eu.europeana.indexing.utils.RdfUtils;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import eu.europeana.corelib.definitions.jibx.ColorSpaceType;
import eu.europeana.corelib.definitions.jibx.Duration;
import eu.europeana.corelib.definitions.jibx.HasColorSpace;
import eu.europeana.corelib.definitions.jibx.HasMimeType;
import eu.europeana.corelib.definitions.jibx.HexBinaryType;
import eu.europeana.corelib.definitions.jibx.OrientationType;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.definitions.jibx.WebResourceType;
import eu.europeana.indexing.utils.MediaType;

/**
 * This class is a wrapper around instances of type {@link WebResourceType}. Its responsibility is
 * to hide the RDF structure and objects needed when extracting technical information from the web
 * resource.
 * 
 * @author jochen
 *
 */
public class WebResourceWrapper {

  private final WebResourceType webResource;

  /**
   * Enum containing the possible values for the color space.
   * 
   * @author jochen
   */
  public enum ColorSpace {
    SRGB, GRAYSCALE;
  }

  /**
   * Enum containing the possible values for the orientation.
   * 
   * @author jochen
   */
  public enum Orientation {
    PORTRAIT, LANDSCAPE;
  }

  /**
   * Constructor.
   * 
   * @param webResource The web resource to wrap.
   */
  public WebResourceWrapper(WebResourceType webResource) {
    this.webResource = webResource;
  }

  /**
   * This method extracts all web resources from the RDF object.
   * 
   * @param rdf The RDF object to extract the web resources from.
   * @return The list of web resources. Is not null, but could be empty.
   */
  public static List<WebResourceWrapper> getListFromRdf(RDF rdf) {
    return RdfUtils.getWebResourcesWithNonemptyAbout(rdf).map(WebResourceWrapper::new).collect(Collectors.toList());
  }

  /**
   * @return The 'ebucore:hasMimeType' value of the web resource as String, or null if none is set.
   */
  public String getMimeType() {
    return Optional.ofNullable(webResource.getHasMimeType()).map(HasMimeType::getHasMimeType)
        .filter(StringUtils::isNotBlank).map(WebResourceWrapper::getBaseType).orElse(null);
  }

  /**
   * This method strips the base type of a mime type declaration (i.e. it removes the parameters).
   * 
   * @param mimeType The mime type, is not null.
   * @return The mime type stripped off all parameters.
   */
  private static String getBaseType(String mimeType) {
    return mimeType.split(";")[0].trim().toLowerCase(Locale.ENGLISH);
  }

  /**
   * Determines the media type of the web resource (based on the MIME type).
   * 
   * @return The media type corresponding to the mime type. Does not return null, but may return
   *         {@link EncodedMediaType#OTHER}.
   */
  public EncodedMediaType getMediaType() {
    final EncodedMediaType result;
    switch (MediaType.getMediaType(getMimeType())) {
      case AUDIO:
        result = EncodedMediaType.AUDIO;
        break;
      case IMAGE:
        result = EncodedMediaType.IMAGE;
        break;
      case TEXT:
        result = EncodedMediaType.TEXT;
        break;
      case VIDEO:
        result = EncodedMediaType.VIDEO;
        break;
      default:
        result = EncodedMediaType.OTHER;
        break;
    }
    return result;
  }

  /**
   * @return The 'rdf:about' attribute of the web resource as String, or null if none is set.
   */
  public String getAbout() {
    return webResource.getAbout();
  }

  /**
   * @return The 'rdf:type' value of the web resource as String, or null if none is set.
   */
  public String getType() {
    return webResource.getType() == null ? null : webResource.getType().getResource();
  }

  /**
   * @return The 'ebucore:width' value of the web resource as long, or 0 if none is set.
   */
  public long getWidth() {
    return webResource.getWidth() == null ? 0L : webResource.getWidth().getLong();
  }

  /**
   * @return The 'ebucore:height' value of the web resource as long, or 0 if none is set.
   */
  public long getHeight() {
    return webResource.getHeight() == null ? 0L : webResource.getHeight().getLong();
  }

  /**
   * @return The 'ebucore:orientation' value of the web resource as enum, or null if none is set or
   *         the value was not recognized.
   */
  public Orientation getOrientation() {
    final OrientationType orientation = webResource.getOrientation();
    final Orientation result;
    if (orientation == null || orientation.getString() == null) {
      result = null;
    } else if ("portrait".equals(orientation.getString())) {
      result = Orientation.PORTRAIT;
    } else if ("landscape".equals(orientation.getString())) {
      result = Orientation.LANDSCAPE;
    } else {
      result = null;
    }
    return result;
  }

  /**
   * @return The 'edm:hasColorSpace' value of the web resource as enum, or null if none is set or
   *         the value was not recognized.
   */
  public ColorSpace getColorSpace() {
    final HasColorSpace colorSpace = webResource.getHasColorSpace();
    final ColorSpace result;
    if (colorSpace == null || colorSpace.getHasColorSpace() == null) {
      result = null;
    } else if (colorSpace.getHasColorSpace() == ColorSpaceType.S_RGB) {
      result = ColorSpace.SRGB;
    } else if (colorSpace.getHasColorSpace() == ColorSpaceType.GRAYSCALE) {
      result = ColorSpace.GRAYSCALE;
    } else {
      result = null;
    }
    return result;
  }

  /**
   * @return All non-null 'edm:componentColor' values of the web resource as a set of Strings, or
   *         the empty set if none are set.
   */
  public Set<String> getColorHexCodes() {
    if (webResource.getComponentColorList() == null) {
      return Collections.emptySet();
    }
    return webResource.getComponentColorList().stream().filter(Objects::nonNull)
        .map(HexBinaryType::getString).collect(Collectors.toSet());
  }

  /**
   * @return The 'ebucore:sampleSize' value of the web resource as long, or 0 if none is set.
   */
  public long getSampleSize() {
    return webResource.getSampleSize() == null ? 0L : webResource.getSampleSize().getLong();
  }

  /**
   * @return The 'ebucore:sampleRate' value of the web resource as long, or 0 if none is set.
   */
  public long getSampleRate() {
    return webResource.getSampleRate() == null ? 0L : webResource.getSampleRate().getLong();
  }

  /**
   * @return The 'edm:codecName' value of the web resource as String, or null if none is set.
   */
  public String getCodecName() {
    return webResource.getCodecName() == null ? null : webResource.getCodecName().getCodecName();
  }

  /**
   * @return The 'ebucore:duration' value of the web resource as long, or 0 if none is set or the
   *         value could not be read as a long.
   */
  public long getDuration() {
    final Duration duration = webResource.getDuration();
    if (duration == null || StringUtils.isBlank(duration.getDuration()))
      return 0L;
    try {
      return Long.parseLong(duration.getDuration());
    } catch (NumberFormatException e) {
      return 0L;
    }
  }
}
