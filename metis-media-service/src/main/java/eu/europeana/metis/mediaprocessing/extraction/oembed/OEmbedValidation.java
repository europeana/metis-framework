package eu.europeana.metis.mediaprocessing.extraction.oembed;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
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

        if (containsMaxHeightAndMaxWidth(params) && hasValidMaxHeight(params)
            && isOEmbedValidHeight(oEmbedModel, params)) {
          result = true;
        } else {
          LOGGER.warn("Not valid height according to max height");
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

        if (containsMaxHeightAndMaxWidth(params) && hasValidMaxHeight(params)
            && hasThumbnailUrl(oEmbedModel) && isOEmbedValidThumbnailHeight(oEmbedModel, params)) {
          result = true;
        } else {
          LOGGER.warn("Not valid thumbnail size for max height parameter");
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

        if (containsMaxHeightAndMaxWidth(params) && hasValidMaxWidth(params)
            && isOEmbedValidWidth(oEmbedModel, params)) {
          result = true;
        } else {
          LOGGER.warn("Not valid width according to max width");
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

        if (containsMaxHeightAndMaxWidth(params) && hasValidMaxWidth(params)
            && hasThumbnailUrl(oEmbedModel) && isOEmbedValidThumbnailWidth(oEmbedModel, params)) {
          result = true;
        } else {
          LOGGER.warn("Not valid thumbnail size for max width parameter");
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
  private static boolean isValidTypeVideo(OEmbedModel oEmbedModel) {
    return hasValidModelAndType(oEmbedModel)
        && "video".equalsIgnoreCase(oEmbedModel.getType())
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
}
