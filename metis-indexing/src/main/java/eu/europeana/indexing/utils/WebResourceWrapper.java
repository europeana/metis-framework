package eu.europeana.indexing.utils;

import eu.europeana.corelib.definitions.jibx.Duration;
import eu.europeana.corelib.definitions.jibx.HasColorSpace;
import eu.europeana.corelib.definitions.jibx.HasMimeType;
import eu.europeana.corelib.definitions.jibx.HexBinaryType;
import eu.europeana.corelib.definitions.jibx.OrientationType;
import eu.europeana.corelib.definitions.jibx.SpatialResolution;
import eu.europeana.corelib.definitions.jibx.WebResourceType;
import eu.europeana.indexing.solr.facet.EncodedMediaType;
import eu.europeana.metis.utils.MediaType;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

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
  private final Set<WebResourceLinkType> linkTypes;

  /**
   * Enum containing the possible values for the color space.
   * 
   * @author jochen
   */
  public enum ColorSpace {
    COLOR, GRAYSCALE, OTHER
  }

  /**
   * Enum containing the possible values for the orientation.
   * 
   * @author jochen
   */
  public enum Orientation {
    PORTRAIT, LANDSCAPE
  }

  /**
   * Constructor.
   * 
   * @param webResource The web resource to wrap.
   * @param  linkTypes The link types with which the web resource is linked from the entity that contains it.
   */
  WebResourceWrapper(WebResourceType webResource, Set<WebResourceLinkType> linkTypes) {
    this.webResource = webResource;
    this.linkTypes = linkTypes == null ? Collections.emptySet() : new HashSet<>(linkTypes);
  }

  /**
   * @return The link types with which the web resource is linked from the entity that contains it.
   */
  public Set<WebResourceLinkType> getLinkTypes() {
    return Collections.unmodifiableSet(linkTypes);
  }

  /**
   * @return The 'ebucore:hasMimeType' value of the web resource as String, or null if none is set.
   */
  public String getMimeType() {
    return getMimeType(webResource);
  }

  /**
   * @param webResource The web resource for which to get the mime type.
   * @return The 'ebucore:hasMimeType' value of the web resource as String, or null if none is set.
   */
  static String getMimeType(WebResourceType webResource) {
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
   * @return The size of the image, defined as {@link #getWidth()} times {@link #getHeight()}.
   */
  public long getSize() {
    return getWidth() * getHeight();
  }

  /**
   * @return The 'edm:spatialResolution' value of the web resource as long, or 0 if none is set.
   */
  public long getSpatialResolution() {
    return Optional.of(webResource).map(WebResourceType::getSpatialResolution).map(
        SpatialResolution::getInteger).map(BigInteger::longValue).orElse(0L);
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

    // Sanity check.
    final HasColorSpace colorSpace = webResource.getHasColorSpace();
    if (colorSpace == null || colorSpace.getHasColorSpace() == null) {
      return null;
    }

    // Determine color space type.
    final ColorSpace result;
    switch (colorSpace.getHasColorSpace()) {
      case OTHER:
        result = ColorSpace.OTHER;
        break;
      case GRAYSCALE:
      case REC601_LUMA:
      case REC709_LUMA:
        result = ColorSpace.GRAYSCALE;
        break;
      default:
        result = ColorSpace.COLOR;
        break;
    }

    // Done.
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

  /**
   * @return The license of this entity.
   */
  public LicenseType getLicenseType() {
    return Optional.of(webResource).map(WebResourceType::getRights).map(LicenseType::getLicenseType)
        .orElse(null);
  }
}
