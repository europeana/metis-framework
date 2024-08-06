package eu.europeana.metis.mediaprocessing.extraction.oembed;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Model based on the standard https://oembed.com/
 */
public class OEmbedModel {

  private static final String MAX_HEIGHT = "maxheight";
  private static final String MAX_WIDTH = "maxwidth";
  private static final String INVALID_URL = "Invalid url";
  private static final Logger LOGGER = LoggerFactory.getLogger(OEmbedModel.class);
  private String type;
  private String version;
  private String title;
  private int height;
  private int width;
  private String url;
  private String author_name;
  private String author_url;
  private String provider_name;
  private String provider_url;
  private String cache_age;
  private String thumbnail_url;
  private String thumbnail_height;
  private String thumbnail_width;
  private String html;
  private String duration;

  /**
   * Gets oembed model from json.
   *
   * @param jsonResource byte[]
   * @return the oembed model from json
   * @throws IOException the io exception
   */
  public static OEmbedModel getOEmbedModelFromJson(byte[] jsonResource) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    return objectMapper.readValue(jsonResource, OEmbedModel.class);
  }

  /**
   * Gets oembed model from xml.
   *
   * @param xmlResource byte[]
   * @return the oembed model from xml
   * @throws IOException the io exception
   */
  public static OEmbedModel getOEmbedModelFromXml(byte[] xmlResource) throws IOException {
    XmlMapper xmlMapper = new XmlMapper();
    xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    return xmlMapper.readValue(xmlResource, OEmbedModel.class);
  }

  /**
   * Is valid oembed photo or video boolean.
   *
   * @param oEmbedModel the oembed model
   * @return the boolean true complies the minimum required fields for each type
   */
  public static boolean isValidOEmbedPhotoOrVideo(OEmbedModel oEmbedModel) {
    return hasValidVersion(oEmbedModel) && hasValidType(oEmbedModel);
  }

  /**
   * Has valid height size url boolean.
   *
   * @param oEmbedModel the oEmbed model
   * @param url the url
   * @return the boolean
   */
  public static boolean hasValidHeightSizeUrl(OEmbedModel oEmbedModel, String url) {
    boolean result = false;
    Map<String, String> params;
    if (oEmbedModel != null) {
      try {
        params = UriComponentsBuilder.fromUri(new URI(url))
                                     .build()
                                     .getQueryParams()
                                     .toSingleValueMap();

        if (containsMaxHeightAndMaxWidth(params) && hasValidMaxHeight(params)) {
          if (isOEmbedValidHeight(oEmbedModel, params)) {
            result = true;
          } else {
            LOGGER.warn("Not valid height according to max height");
          }
        }
      } catch (URISyntaxException e) {
        LOGGER.warn(INVALID_URL, e);
      } catch (NumberFormatException e) {
        LOGGER.warn("Not valid height dimension size", e);
      }
    }
    return result;
  }

  /**
   * Has valid height size thumbnail boolean.
   *
   * @param oEmbedModel the oEmbed model
   * @param url the url
   * @return the boolean
   */
  public static boolean hasValidHeightSizeThumbnail(OEmbedModel oEmbedModel, String url) {
    boolean result = false;
    Map<String, String> params;
    if (oEmbedModel != null) {
      try {
        params = UriComponentsBuilder.fromUri(new URI(url)).
                                     build()
                                     .getQueryParams()
                                     .toSingleValueMap();

        if (containsMaxHeightAndMaxWidth(params) && hasValidMaxHeight(params)) {
          if (hasThumbnailUrl(oEmbedModel) && isOEmbedValidThumbnailHeight(oEmbedModel, params)) {
            result = true;
          } else {
            LOGGER.warn("Not valid thumbnail size for max height parameter");
          }
        }
      } catch (URISyntaxException e) {
        LOGGER.warn(INVALID_URL, e);
      } catch (NumberFormatException e) {
        LOGGER.warn("Not valid height thumbnail dimension size", e);
      }
    }
    return result;
  }

  /**
   * Has valid width size url boolean.
   *
   * @param oEmbedModel the oEmbed model
   * @param url the url
   * @return the boolean
   */
  public static boolean hasValidWidthSizeUrl(OEmbedModel oEmbedModel, String url) {
    boolean result = false;
    Map<String, String> params;
    if (oEmbedModel != null) {
      try {
        params = UriComponentsBuilder.fromUri(new URI(url)).
                                     build()
                                     .getQueryParams()
                                     .toSingleValueMap();

        if (containsMaxHeightAndMaxWidth(params) && hasValidMaxWidth(params)) {

          if (isOEmbedValidWidth(oEmbedModel, params)) {
            result = true;
          } else {
            LOGGER.warn("Not valid width according to max width");
          }

        }
      } catch (URISyntaxException e) {
        LOGGER.warn(INVALID_URL, e);
      } catch (NumberFormatException e) {
        LOGGER.warn("Not valid width dimension size", e);
      }
    }
    return result;
  }

  /**
   * Has valid width size thumbnail boolean.
   *
   * @param oEmbedModel the oEmbed model
   * @param url the url
   * @return the boolean
   */
  public static boolean hasValidWidthSizeThumbnail(OEmbedModel oEmbedModel, String url) {
    boolean result = false;
    Map<String, String> params;
    if (oEmbedModel != null) {
      try {
        params = UriComponentsBuilder.fromUri(new URI(url)).
                                     build()
                                     .getQueryParams()
                                     .toSingleValueMap();

        if (containsMaxHeightAndMaxWidth(params) && hasValidMaxWidth(params)) {

          if (hasThumbnailUrl(oEmbedModel) && isOEmbedValidThumbnailWidth(oEmbedModel, params)) {
            result = true;
          } else {
            LOGGER.warn("Not valid thumbnail size for max width parameter");
          }
        }
      } catch (URISyntaxException e) {
        LOGGER.warn("Invalid url ", e);
      } catch (NumberFormatException e) {
        LOGGER.warn("Not valid thumbnail width dimension size", e);
      }
    }
    return result;
  }

  /**
   * Check valid width and height dimensions.
   *
   * @param oEmbedModel the oEmbed model
   * @param url the url
   */
  public static void checkValidWidthAndHeightDimensions(OEmbedModel oEmbedModel, String url) {
    if (hasValidHeightSizeUrl(oEmbedModel, url)) {
      LOGGER.info("Valid url dimensions of height");
    } else {
      LOGGER.warn("Not valid url dimensions of height");
    }
    if (hasValidWidthSizeUrl(oEmbedModel, url)) {
      LOGGER.info("Valid url dimensions of width");
    } else {
      LOGGER.warn("Not valid url dimensions of width");
    }
    if (hasValidHeightSizeThumbnail(oEmbedModel, url)) {
      LOGGER.info("Valid thumbnail dimensions of height");
    } else {
      LOGGER.warn("Not valid thumbnail dimensions of height");
    }
    if (hasValidWidthSizeThumbnail(oEmbedModel, url)) {
      LOGGER.info("Valid thumbnail dimensions of width");
    } else {
      LOGGER.warn("Not valid thumbnail dimensions of width");
    }
  }

  /**
   * Gets duration from model.
   *
   * @param oEmbedModel the oEmbed model
   * @return the duration from model
   */
  public static double getDurationFromModel(OEmbedModel oEmbedModel) {
    double duration;
    try {
      duration = Double.parseDouble(oEmbedModel.getDuration());
    } catch (NumberFormatException e) {
      duration = 0.0;
    }
    return duration;
  }

  /**
   * Is oEmbed valid thumbnail height boolean.
   *
   * @param oEmbedModel the oEmbed model
   * @param params the params
   * @return the boolean
   */
  private static boolean isOEmbedValidThumbnailHeight(OEmbedModel oEmbedModel, Map<String, String> params) {
    return Integer.parseInt(oEmbedModel.getThumbnail_height()) <= Integer.parseInt(params.get(MAX_HEIGHT));
  }

  /**
   * Is oEmbed valid thumbnail width boolean.
   *
   * @param oEmbedModel the oEmbed model
   * @param params the params
   * @return the boolean
   */
  private static boolean isOEmbedValidThumbnailWidth(OEmbedModel oEmbedModel, Map<String, String> params) {
    return Integer.parseInt(oEmbedModel.getThumbnail_width()) <= Integer.parseInt(params.get(MAX_WIDTH));
  }

  /**
   * Is oEmbed valid width boolean.
   *
   * @param oEmbedModel the oEmbed model
   * @param params the params
   * @return the boolean
   */
  private static boolean isOEmbedValidWidth(OEmbedModel oEmbedModel, Map<String, String> params) {
    return oEmbedModel.getWidth() <= Integer.parseInt(params.get(MAX_WIDTH));
  }

  /**
   * Is oEmbed valid height boolean.
   *
   * @param oEmbedModel the oEmbed model
   * @param params the params
   * @return the boolean
   */
  private static boolean isOEmbedValidHeight(OEmbedModel oEmbedModel, Map<String, String> params) {
    return oEmbedModel.getHeight() <= Integer.parseInt(params.get(MAX_HEIGHT));
  }

  /**
   * Has valid max height boolean.
   *
   * @param params the params
   * @return the boolean
   */
  private static boolean hasValidMaxHeight(Map<String, String> params) {
    return Integer.parseInt(params.get(MAX_HEIGHT)) > 0;
  }

  /**
   * Has valid max width boolean.
   *
   * @param params the params
   * @return the boolean
   */
  private static boolean hasValidMaxWidth(Map<String, String> params) {
    return Integer.parseInt(params.get(MAX_WIDTH)) > 0;
  }

  private static boolean containsMaxHeightAndMaxWidth(Map<String, String> params) {
    return params.containsKey(MAX_HEIGHT) || params.containsKey(MAX_WIDTH);
  }

  /**
   * Has thumbnail url boolean.
   *
   * @param oEmbedModel the oEmbed model
   * @return the boolean
   */
  private static boolean hasThumbnailUrl(OEmbedModel oEmbedModel) {
    return oEmbedModel.getThumbnail_url() != null;
  }

  /**
   * Has valid type boolean.
   *
   * @param oEmbedModel the oEmbed model
   * @return the boolean
   */
  private static boolean hasValidType(OEmbedModel oEmbedModel) {
    return (isValidTypePhoto(oEmbedModel) || isValidTypeVideo(oEmbedModel));
  }

  /**
   * Is valid type photo boolean.
   *
   * @param oEmbedModel the oEmbed model
   * @return the boolean
   */
  private static boolean isValidTypePhoto(OEmbedModel oEmbedModel) {
    return hasValidModelAndType(oEmbedModel)
        && oEmbedModel.getType().equalsIgnoreCase("photo")
        && oEmbedModel.getUrl() != null && !oEmbedModel.getUrl().isEmpty()
        && hasValidDimensions(oEmbedModel);
  }

  /**
   * Is valid type video boolean.
   *
   * @param oEmbedModel the oEmbed model
   * @return the boolean
   */
  private static boolean isValidTypeVideo(OEmbedModel oEmbedModel) {
    return hasValidModelAndType(oEmbedModel)
        && oEmbedModel.getType().equalsIgnoreCase("video")
        && oEmbedModel.getHtml() != null && !oEmbedModel.getHtml().isEmpty()
        && hasValidDimensions(oEmbedModel);
  }

  /**
   * Has valid model and type boolean.
   *
   * @param oEmbedModel the oEmbed model
   * @return the boolean
   */
  private static boolean hasValidModelAndType(OEmbedModel oEmbedModel) {
    return oEmbedModel != null && oEmbedModel.getType() != null;
  }

  /**
   * Has valid dimensions boolean.
   *
   * @param oEmbedModel the oEmbed model
   * @return the boolean
   */
  private static boolean hasValidDimensions(OEmbedModel oEmbedModel) {
    return (oEmbedModel.getWidth() > 0 && oEmbedModel.getHeight() > 0);
  }

  /**
   * Has valid version boolean. private
   *
   * @param oEmbedModel the oEmbed model
   * @return the boolean
   */
  private static boolean hasValidVersion(OEmbedModel oEmbedModel) {
    return oEmbedModel != null && oEmbedModel.getVersion() != null
        && oEmbedModel.getVersion().startsWith("1.0");
  }

  /**
   * Gets type.
   *
   * @return the type
   */
  public String getType() {
    return type;
  }

  /**
   * Sets type.
   *
   * @param type the type
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * Gets version.
   *
   * @return the version
   */
  public String getVersion() {
    return version;
  }

  /**
   * Sets version.
   *
   * @param version the version
   */
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   * Gets title.
   *
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * Sets title.
   *
   * @param title the title
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Gets height.
   *
   * @return the height
   */
  public int getHeight() {
    return height;
  }

  /**
   * Sets height.
   *
   * @param height the height
   */
  public void setHeight(int height) {
    this.height = height;
  }

  /**
   * Gets width.
   *
   * @return the width
   */
  public int getWidth() {
    return width;
  }

  /**
   * Sets width.
   *
   * @param width the width
   */
  public void setWidth(int width) {
    this.width = width;
  }

  /**
   * Gets url.
   *
   * @return the url
   */
  public String getUrl() {
    return url;
  }

  /**
   * Sets url.
   *
   * @param url the url
   */
  public void setUrl(String url) {
    this.url = url;
  }

  /**
   * Gets author name.
   *
   * @return the author name
   */
  public String getAuthor_name() {
    return author_name;
  }

  /**
   * Sets author name.
   *
   * @param author_name the author name
   */
  public void setAuthor_name(String author_name) {
    this.author_name = author_name;
  }

  /**
   * Gets author url.
   *
   * @return the author url
   */
  public String getAuthor_url() {
    return author_url;
  }

  /**
   * Sets author url.
   *
   * @param author_url the author url
   */
  public void setAuthor_url(String author_url) {
    this.author_url = author_url;
  }

  /**
   * Gets provider name.
   *
   * @return the provider name
   */
  public String getProvider_name() {
    return provider_name;
  }

  /**
   * Sets provider name.
   *
   * @param provider_name the provider name
   */
  public void setProvider_name(String provider_name) {
    this.provider_name = provider_name;
  }

  /**
   * Gets provider url.
   *
   * @return the provider url
   */
  public String getProvider_url() {
    return provider_url;
  }

  /**
   * Sets provider url.
   *
   * @param provider_url the provider url
   */
  public void setProvider_url(String provider_url) {
    this.provider_url = provider_url;
  }

  /**
   * Gets cache age.
   *
   * @return the cache age
   */
  public String getCache_age() {
    return cache_age;
  }

  /**
   * Sets cache age.
   *
   * @param cache_age the cache age
   */
  public void setCache_age(String cache_age) {
    this.cache_age = cache_age;
  }

  /**
   * Gets thumbnail url.
   *
   * @return the thumbnail url
   */
  public String getThumbnail_url() {
    return thumbnail_url;
  }

  /**
   * Sets thumbnail url.
   *
   * @param thumbnail_url the thumbnail url
   */
  public void setThumbnail_url(String thumbnail_url) {
    this.thumbnail_url = thumbnail_url;
  }

  /**
   * Gets thumbnail height.
   *
   * @return the thumbnail height
   */
  public String getThumbnail_height() {
    return thumbnail_height;
  }

  /**
   * Sets thumbnail height.
   *
   * @param thumbnail_height the thumbnail height
   */
  public void setThumbnail_height(String thumbnail_height) {
    this.thumbnail_height = thumbnail_height;
  }

  /**
   * Gets thumbnail width.
   *
   * @return the thumbnail width
   */
  public String getThumbnail_width() {
    return thumbnail_width;
  }

  /**
   * Sets thumbnail width.
   *
   * @param thumbnail_width the thumbnail width
   */
  public void setThumbnail_width(String thumbnail_width) {
    this.thumbnail_width = thumbnail_width;
  }

  /**
   * Gets html.
   *
   * @return the html
   */
  public String getHtml() {
    return html;
  }

  /**
   * Sets html.
   *
   * @param html the html
   */
  public void setHtml(String html) {
    this.html = html;
  }

  /**
   * Gets duration.
   *
   * @return the duration
   */
  public String getDuration() {
    return duration;
  }

  /**
   * Sets duration.
   *
   * @param duration the duration
   */
  public void setDuration(String duration) {
    this.duration = duration;
  }

  @Override
  public final boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof OEmbedModel that)) {
      return false;
    }

    return height == that.height && width == that.width && type.equals(that.type) && version.equals(that.version)
        && Objects.equals(title, that.title) && url.equals(that.url) && Objects.equals(author_name,
        that.author_name) && Objects.equals(author_url, that.author_url) && Objects.equals(provider_name,
        that.provider_name) && Objects.equals(provider_url, that.provider_url) && Objects.equals(cache_age,
        that.cache_age) && Objects.equals(thumbnail_url, that.thumbnail_url) && Objects.equals(thumbnail_height,
        that.thumbnail_height) && Objects.equals(thumbnail_width, that.thumbnail_width) && Objects.equals(html,
        that.html) && Objects.equals(duration, that.duration);
  }

  @Override
  public int hashCode() {
    int result = type.hashCode();
    result = 31 * result + version.hashCode();
    result = 31 * result + Objects.hashCode(title);
    result = 31 * result + height;
    result = 31 * result + width;
    result = 31 * result + url.hashCode();
    result = 31 * result + Objects.hashCode(author_name);
    result = 31 * result + Objects.hashCode(author_url);
    result = 31 * result + Objects.hashCode(provider_name);
    result = 31 * result + Objects.hashCode(provider_url);
    result = 31 * result + Objects.hashCode(cache_age);
    result = 31 * result + Objects.hashCode(thumbnail_url);
    result = 31 * result + Objects.hashCode(thumbnail_height);
    result = 31 * result + Objects.hashCode(thumbnail_width);
    result = 31 * result + Objects.hashCode(html);
    result = 31 * result + Objects.hashCode(duration);
    return result;
  }
}
