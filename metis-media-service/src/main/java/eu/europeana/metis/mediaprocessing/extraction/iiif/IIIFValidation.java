package eu.europeana.metis.mediaprocessing.extraction.iiif;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import java.io.ByteArrayOutputStream;
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

  /**
   * properties based on <a href="https://iiif.io/api/image/3.0/#4-image-requests">IIIF Image requests</a>
   * 4.1 Region
   * 4.2 Size
   * 4.3 Rotation
   * 4.4 Quality
   * 4.5 Format
   * 4.7 Floating point values
   * */
  private static final String IIIF_FLOATING_NUMBER = "(?:0|[1-9]\\d*)(?:\\.\\d*[1-9])?";
  private static final String IIIF_DIMENSION = "(\\^?!?" + IIIF_FLOATING_NUMBER + ")?";
  private static final String IIIF_PERCENTAGE_REGION = "(pct:" + IIIF_FLOATING_NUMBER + "," + IIIF_FLOATING_NUMBER + ","
      + IIIF_FLOATING_NUMBER + "," + IIIF_FLOATING_NUMBER + ")";
  private static final String IIIF_PERCENTAGE_SIZE = "(\\^?pct:" + IIIF_FLOATING_NUMBER + ")";
  private static final String IIIF_DIMENSION_REGION = "(" + IIIF_DIMENSION + "," + IIIF_DIMENSION + "," + IIIF_DIMENSION + "," + IIIF_DIMENSION + ")";
  private static final String IIIF_DIMENSION_SIZE = "(" + IIIF_DIMENSION + ",)|(," + IIIF_DIMENSION + ")|(" + IIIF_DIMENSION + "," + IIIF_DIMENSION + ")";
  private static final String IIIF_REGION = "full|(\\^?max)|square|" + IIIF_PERCENTAGE_REGION + "|" + IIIF_DIMENSION_REGION;
  private static final String IIIF_SIZE = "full|(\\^?max)|square|" + IIIF_PERCENTAGE_SIZE + "|" + IIIF_DIMENSION_SIZE;
  private static final String IIIF_ROTATION = "!?" + IIIF_FLOATING_NUMBER;
  private static final String IIIF_QUALITY = "color|gray|bitonal|default";
  private static final String IIIF_FORMAT = "jpg|tif|png|gif|jp2|pdf|webp";
  private static final String IIIF_PARAMS_URI_REGEX = "/(" + IIIF_REGION + ")/(" + IIIF_SIZE + ")/(" + IIIF_ROTATION + ")/(" + IIIF_QUALITY
      + ")\\.(" + IIIF_FORMAT + ")$";

  private static final Pattern IIIF_URL_PATTERN = Pattern.compile("^https?://.+"+IIIF_PARAMS_URI_REGEX+"$");

  private static final int IIIF_INFO_JSON_MAX_SIZE_BYTES = 49_152;
  private static final int IIIF_INFO_JSON_BUFFER_READ_BYTES = 4096;

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
      final HttpURLConnection connection = getIIIFInfoJsonHttpURLConnection(iiifUrl);
      ByteArrayOutputStream jsonResource = new ByteArrayOutputStream();
      try (InputStream input = connection.getInputStream()) {
        byte[] data = new byte[IIIF_INFO_JSON_BUFFER_READ_BYTES];
        int bytesRead;
        int totalBytesRead = 0;
        while ((bytesRead = input.read(data)) != -1 && totalBytesRead <= IIIF_INFO_JSON_MAX_SIZE_BYTES) {
          jsonResource.write(data, 0, bytesRead);
          totalBytesRead += bytesRead;
        }
      } finally {
        connection.disconnect();
      }
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      return objectMapper.readValue(jsonResource.toByteArray(), IIIFInfoJsonV3.class);
    } catch (IOException | URISyntaxException e) {
      throw new MediaExtractionException(e.getMessage(), e);
    }
  }

  private static HttpURLConnection getIIIFInfoJsonHttpURLConnection(String iiifUrl)
      throws URISyntaxException, IOException, MediaExtractionException {
    final String baseUrl = iiifUrl.replaceAll(IIIF_PARAMS_URI_REGEX, "");
    final URI infoUrl = new URI(baseUrl + "/info.json");
    final HttpURLConnection connection = (HttpURLConnection) infoUrl.toURL().openConnection();
    connection.setRequestMethod("GET");
    connection.setRequestProperty("Accept", "application/json");
    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
      throw new MediaExtractionException(String.format("Failed to fetch JSON. HTTP Status %d", connection.getResponseCode()));
    }
    return connection;
  }

  public static RdfResourceEntry fetchIIFSmallVersionOfResource(RdfResourceEntry resourceEntry) throws MediaExtractionException {
    IIIFInfoJsonV3 infoJsonV3 = IIIFValidation.fetchInfoJson(resourceEntry.getResourceUrl());
    if (infoJsonV3 != null) {
      String newUrl = infoJsonV3.getId() + "/full/!400,400/0/default.jpg";
      return new RdfResourceEntry(newUrl, resourceEntry.getUrlTypes(), resourceEntry.getResourceKind());
    }
    return resourceEntry;
  }
}
