package eu.europeana.indexing.solr.crf;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import eu.europeana.corelib.definitions.jibx.ColorSpaceType;
import eu.europeana.corelib.definitions.jibx.Duration;
import eu.europeana.corelib.definitions.jibx.HasColorSpace;
import eu.europeana.corelib.definitions.jibx.HexBinaryType;
import eu.europeana.corelib.definitions.jibx.OrientationType;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.definitions.jibx.WebResourceType;

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

  public enum ColorSpace {
    SRGB, GRAYSCALE;
  }

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
    final List<WebResourceWrapper> result;
    if (rdf.getWebResourceList() == null) {
      result = Collections.emptyList();
    } else {
      result = rdf.getWebResourceList().stream().map(WebResourceWrapper::new)
          .collect(Collectors.toList());
    }
    return result;
  }

  public String getMimeType() {

    // If there is no mime type, return the generic one.
    if (webResource.getHasMimeType() == null
        || StringUtils.isBlank(webResource.getHasMimeType().getHasMimeType())) {
      return "application/octet-stream";
    }

    // Otherwise, we return the base type (without parameters).
    return webResource.getHasMimeType().getHasMimeType().split(";")[0].trim()
        .toLowerCase(Locale.ENGLISH);
  }

  // TODO JOCHEN Merge and make library method for existing methods isText, isAudio/Video and
  // isImage in class MediaProcessor. Use this class there.
  /**
   * Determines the media type of the web resource (based on the MIME type).
   * 
   * @return The media type corresponding to the mime type. Does not return null, but may return
   *         {@link MediaType#OTHER}.
   */
  public MediaType getMediaType() {
    final String mimeType = getMimeType();
    final MediaType result;
    if (mimeType == null) {
      result = MediaType.OTHER;
    } else if (mimeType.startsWith("image/")) {
      result = MediaType.IMAGE;
    } else if (mimeType.startsWith("audio/")) {
      result = MediaType.AUDIO;
    } else if (mimeType.startsWith("video/")) {
      result = MediaType.VIDEO;
    } else if (isText(mimeType)) {
      result = MediaType.TEXT;
    } else {
      result = MediaType.OTHER;
    }
    return result;
  }

  private static boolean isText(String mimeType) {
    switch (mimeType) {
      case "application/xml":
      case "application/rtf":
      case "application/epub":
      case "application/pdf":
        return true;
      default:
        return mimeType.startsWith("text/");
    }
  }

  public String getAbout() {
    return webResource.getAbout();
  }

  public String getType() {
    return webResource.getType() == null ? null : webResource.getType().getResource();
  }

  public long getWidth() {
    return webResource.getWidth() == null ? 0L : webResource.getWidth().getLong();
  }

  public long getHeight() {
    return webResource.getHeight() == null ? 0L : webResource.getHeight().getLong();
  }

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

  public ColorSpace getColorSpace() {
    final HasColorSpace colorSpace = webResource.getHasColorSpace();
    final ColorSpace result;
    if (colorSpace == null || colorSpace.getHasColorSpace() == null) {
      result = null;
    } else if (ColorSpaceType.S_RGB.equals(colorSpace.getHasColorSpace())) {
      result = ColorSpace.SRGB;
    } else if (ColorSpaceType.GRAYSCALE.equals(colorSpace.getHasColorSpace())) {
      result = ColorSpace.GRAYSCALE;
    } else {
      result = null;
    }
    return result;
  }

  public Set<String> getColorHexCodes() {
    if (webResource.getComponentColorList() == null) {
      return Collections.emptySet();
    }
    return webResource.getComponentColorList().stream().filter(Objects::nonNull)
        .map(HexBinaryType::getString).collect(Collectors.toSet());
  }

  public long getSampleSize() {
    return webResource.getSampleSize() == null ? 0L : webResource.getSampleSize().getLong();
  }

  public long getSampleRate() {
    return webResource.getSampleRate() == null ? 0L : webResource.getSampleRate().getLong();
  }

  public String getCodecName() {
    return webResource.getCodecName() == null ? null : webResource.getCodecName().getCodecName();
  }

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
