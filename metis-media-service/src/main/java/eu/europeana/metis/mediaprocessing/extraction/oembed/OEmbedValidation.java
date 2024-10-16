package eu.europeana.metis.mediaprocessing.extraction.oembed;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * The type oEmbed validation methods.
 */
public final class OEmbedValidation {

  private static final String MAX_HEIGHT = "maxheight";
  private static final String MAX_WIDTH = "maxwidth";
  private static final String INVALID_URL = "Invalid url";
  private static final String OEMBED_IS_REQUIRED_MESSAGE = "OEmbedModel is required cannot be null";
  private static final String OEMBED_PROPERTY_CHECK_IGNORED = "Property check ignored it doesn't apply";
  private static final Logger LOGGER = LoggerFactory.getLogger(OEmbedValidation.class);

  private OEmbedValidation() {
    // validations class
  }

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
   * Has valid height size url boolean.
   *
   * @param oEmbedModel the oEmbed model
   * @param url the url
   * @return the boolean
   */
  public static boolean hasValidHeightSizeUrl(OEmbedModel oEmbedModel, String url) {
    return hasValidProperty(oEmbedModel, url,
        "Not valid height dimension size",
        params -> {
          if (hasValidMaxHeight(params) && isOEmbedValidHeight(oEmbedModel, params)) {
            return true;
          } else {
            LOGGER.warn("Not valid height according to max height {}", url);
            return false;
          }
        });
  }

  /**
   * Has valid height size thumbnail boolean.
   *
   * @param oEmbedModel the oEmbed model
   * @param url the url
   * @return the boolean
   */
  public static boolean hasValidHeightSizeThumbnail(OEmbedModel oEmbedModel, String url) {
    return hasValidProperty(oEmbedModel, url,
        "Not valid height thumbnail dimension size",
        params -> {
          if (hasValidMaxHeight(params) && hasThumbnailUrl(oEmbedModel)
              && isOEmbedValidThumbnailHeight(oEmbedModel, params)) {
            return true;
          } else {
            LOGGER.warn("Not valid thumbnail size for max height parameter {}", url);
            return false;
          }
        });
  }

  /**
   * Has valid width size url boolean.
   *
   * @param oEmbedModel the oEmbed model
   * @param url the url
   * @return the boolean
   */
  public static boolean hasValidWidthSizeUrl(OEmbedModel oEmbedModel, String url) {
    return hasValidProperty(oEmbedModel, url,
        "Not valid width dimension size",
        params -> {
          if (hasValidMaxWidth(params) && isOEmbedValidWidth(oEmbedModel, params)) {
            return true;
          } else {
            LOGGER.warn("Not valid width according to max width {}", url);
            return false;
          }
        });
  }

  /**
   * Has valid width size thumbnail boolean.
   *
   * @param oEmbedModel the oEmbed model
   * @param url the url
   * @return the boolean
   */
  public static boolean hasValidWidthSizeThumbnail(OEmbedModel oEmbedModel, String url) {
    return hasValidProperty(oEmbedModel, url,
        "Not valid thumbnail width dimension size",
        params -> {
          if (hasValidMaxWidth(params) && hasThumbnailUrl(oEmbedModel)
              && isOEmbedValidThumbnailWidth(oEmbedModel, params)) {
            return true;
          } else {
            LOGGER.warn("Not valid thumbnail size for max width parameter {}", url);
            return false;
          }
        });
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
      LOGGER.warn("Not valid url dimensions of height {}", url);
    }
    if (hasValidWidthSizeUrl(oEmbedModel, url)) {
      LOGGER.info("Valid url dimensions of width");
    } else {
      LOGGER.warn("Not valid url dimensions of width {}", url);
    }
    if (hasValidHeightSizeThumbnail(oEmbedModel, url)) {
      LOGGER.info("Valid thumbnail dimensions of height");
    } else {
      LOGGER.warn("Not valid thumbnail dimensions of height {}", url);
    }
    if (hasValidWidthSizeThumbnail(oEmbedModel, url)) {
      LOGGER.info("Valid thumbnail dimensions of width");
    } else {
      LOGGER.warn("Not valid thumbnail dimensions of width {}", url);
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
      duration = Optional.ofNullable(oEmbedModel.getDuration()).map(Double::parseDouble).orElse(0.0);
    } catch (NumberFormatException e) {
      duration = 0.0;
    }
    return duration;
  }

  /**
   * Is valid type photo boolean.
   *
   * @param oEmbedModel the oEmbed model
   * @return the boolean
   */
  public static boolean isValidTypePhoto(OEmbedModel oEmbedModel) {
    return hasValidModelAndType(oEmbedModel)
        && "photo".equalsIgnoreCase(oEmbedModel.getType())
        && oEmbedModel.getUrl() != null && !oEmbedModel.getUrl().isEmpty()
        && hasValidDimensions(oEmbedModel);
  }

  /**
   * Is valid type video boolean.
   *
   * @param oEmbedModel the oEmbed model
   * @return the boolean
   */
  public static boolean isValidTypeVideo(OEmbedModel oEmbedModel) {
    return hasValidModelAndType(oEmbedModel)
        && "video".equalsIgnoreCase(oEmbedModel.getType())
        && oEmbedModel.getHtml() != null && !oEmbedModel.getHtml().isEmpty()
        && hasValidDimensions(oEmbedModel);
  }

  /**
   * Has valid version boolean. private
   *
   * @param oEmbedModel the oEmbed model
   * @return the boolean
   */
  public static boolean hasValidVersion(OEmbedModel oEmbedModel) {
    return oEmbedModel != null && oEmbedModel.getVersion() != null
        && oEmbedModel.getVersion().startsWith("1.0");
  }

  /**
   * Has valid property boolean.
   *
   * @param oEmbedModel the o embed model
   * @param url the url
   * @param messageException the message exception
   * @param predicate the predicate
   * @return the boolean
   */
  private static boolean hasValidProperty(OEmbedModel oEmbedModel,
      String url,
      String messageException,
      Predicate<Map<String, String>> predicate) {
    boolean result = false;
    Map<String, String> params;
    Objects.requireNonNull(oEmbedModel, OEMBED_IS_REQUIRED_MESSAGE);
    try {
      params = UriComponentsBuilder.fromUri(new URI(url))
                                   .build()
                                   .getQueryParams()
                                   .toSingleValueMap();
      if (containsMaxHeightAndMaxWidth(params)) {
        result = predicate.test(params);
      } else {
        result = true;
        LOGGER.warn(OEMBED_PROPERTY_CHECK_IGNORED);
      }
    } catch (URISyntaxException e) {
      LOGGER.warn(INVALID_URL, e);
    } catch (NumberFormatException e) {
      LOGGER.warn(messageException, e);
    }
    return result;
  }

  /**
   * Is oEmbed valid thumbnail height boolean.
   *
   * @param oEmbedModel the oEmbed model
   * @param params the params
   * @return the boolean
   */
  private static boolean isOEmbedValidThumbnailHeight(OEmbedModel oEmbedModel, Map<String, String> params) {
    return Integer.parseInt(oEmbedModel.getThumbnailHeight()) <= Integer.parseInt(params.get(MAX_HEIGHT));
  }

  /**
   * Is oEmbed valid thumbnail width boolean.
   *
   * @param oEmbedModel the oEmbed model
   * @param params the params
   * @return the boolean
   */
  private static boolean isOEmbedValidThumbnailWidth(OEmbedModel oEmbedModel, Map<String, String> params) {
    return Integer.parseInt(oEmbedModel.getThumbnailWidth()) <= Integer.parseInt(params.get(MAX_WIDTH));
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

  /**
   * Check if params contains max height and max width boolean.
   *
   * @param params the params
   * @return the boolean
   */
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
    return oEmbedModel.getThumbnailUrl() != null;
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
}
