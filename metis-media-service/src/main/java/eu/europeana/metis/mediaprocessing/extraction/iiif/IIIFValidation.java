package eu.europeana.metis.mediaprocessing.extraction.iiif;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

/**
 * The type Iiif validation.
 */
public final class IIIFValidation {

  private static final Pattern IIIF_URL_PATTERN = Pattern.compile(
      "^https?://.+/([^/]+)/([^/]+)/([^/]+)/([^/]+)\\.(jpg|png|tif|gif)$"
  );

  private IIIFValidation() {
    // validation class
  }

  /**
   * Is iiif url boolean.
   *
   * @param url the url
   * @return the boolean
   */
  public static boolean isIIIFUrl(String url) {
    return IIIF_URL_PATTERN.matcher(url).matches();
  }

  /**
   * Fetch info json iiif info json model.
   *
   * @param iiifUrl the iiif url
   * @return the iiif info json model
   * @throws MediaExtractionException the media extraction exception
   */
  public static IIIFInfoJsonV3 fetchInfoJson(String iiifUrl) throws MediaExtractionException {
    try {
      final String floatingNumber = "(?:0|[1-9]\\d*)(?:\\.\\d*[1-9])?";
      final String dimension = "(\\^?!?" + floatingNumber + ")?";
      final String percentageRegion =
          "(pct:" + floatingNumber + "," + floatingNumber + "," + floatingNumber + "," + floatingNumber + ")";
      final String percentage = "(\\^?pct:" + floatingNumber + ")";
      final String dimensionRegion = "(" + dimension + "," + dimension + "," + dimension + "," + dimension + ")";
      final String dimensionSize = "(" + dimension + ",)|(," + dimension + ")|(" + dimension + "," + dimension + ")";
      final String region = "full|(\\^?max)|square|" + percentageRegion + "|" + dimensionRegion;
      final String size = "full|(\\^?max)|square|" + percentage + "|" + dimensionSize;
      final String rotation = "!?" + floatingNumber;
      final String quality = "color|gray|bitonal|default";
      final String format = "jpg|tif|png|gif|jp2|pdf|webp";
      final String iiif_uri_regex = "/(" + region + ")/(" + size + ")/(" + rotation + ")/(" + quality + ")\\.(" + format + ")$";
      final String baseUrl = iiifUrl.replaceAll(iiif_uri_regex, "");
      URI infoUrl = new URI(baseUrl + "/info.json");
      HttpURLConnection connection = (HttpURLConnection) infoUrl.toURL().openConnection();
      connection.setRequestProperty("Accept", "application/json");
      try (InputStream input = connection.getInputStream()) {
        byte[] jsonResource = input.readAllBytes();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper.readValue(jsonResource, IIIFInfoJsonV3.class);
      }
    } catch (IOException | URISyntaxException e) {
      throw new MediaExtractionException(e.getMessage());
    }
  }
}
