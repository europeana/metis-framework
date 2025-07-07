package eu.europeana.metis.mediaprocessing.extraction.iiif;

import static eu.europeana.metis.mediaprocessing.MediaProcessorFactory.DEFAULT_MAX_REDIRECT_COUNT;
import static eu.europeana.metis.mediaprocessing.MediaProcessorFactory.DEFAULT_RESOURCE_CONNECT_TIMEOUT;
import static eu.europeana.metis.mediaprocessing.MediaProcessorFactory.DEFAULT_RESOURCE_DOWNLOAD_TIMEOUT;
import static eu.europeana.metis.mediaprocessing.MediaProcessorFactory.DEFAULT_RESOURCE_RESPONSE_TIMEOUT;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.metis.mediaprocessing.http.ResourceDownloadClient;
import eu.europeana.metis.mediaprocessing.http.ResourceDownloadClient.DownloadMode;
import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import eu.europeana.metis.mediaprocessing.model.Resource;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Iiif validation.
 */
public final class IIIFValidation {

  private static final Logger LOGGER = LoggerFactory.getLogger(IIIFValidation.class);
  private static final String IIIF_FLOATING_NUMBER = "(?:0|[1-9]\\d*)(?:\\.\\d*[1-9])?";
  private static final String IIIF_DIMENSION = "(\\^?!?" + IIIF_FLOATING_NUMBER + ")?";
  private static final String IIIF_DIMENSION_REGION = "(" + IIIF_DIMENSION + "," + IIIF_DIMENSION + "," + IIIF_DIMENSION + "," + IIIF_DIMENSION + ")";
  private static final String IIIF_DIMENSION_SIZE = "(" + IIIF_DIMENSION + ",)|(," + IIIF_DIMENSION + ")|(" + IIIF_DIMENSION + "," + IIIF_DIMENSION + ")";
  private static final String IIIF_PERCENTAGE_REGION = "(pct:" + IIIF_FLOATING_NUMBER + "," + IIIF_FLOATING_NUMBER + ","
      + IIIF_FLOATING_NUMBER + "," + IIIF_FLOATING_NUMBER + ")";
  private static final String IIIF_REGION = "full|(\\^?max)|square|" + IIIF_PERCENTAGE_REGION + "|" + IIIF_DIMENSION_REGION;
  private static final String IIIF_PERCENTAGE_SIZE = "(\\^?pct:" + IIIF_FLOATING_NUMBER + ")";
  private static final String IIIF_SIZE = "full|(\\^?max)|square|" + IIIF_PERCENTAGE_SIZE + "|" + IIIF_DIMENSION_SIZE;
  private static final String IIIF_ROTATION = "!?" + IIIF_FLOATING_NUMBER;
  private static final String IIIF_QUALITY = "color|gray|bitonal|default";
  private static final String IIIF_FORMAT = "jpg|tif|png|gif|jp2|pdf|webp";
  private static final String IIIF_PARAMS_URI_REGEX =
      "/(" + IIIF_REGION + ")/(" + IIIF_SIZE + ")/(" + IIIF_ROTATION + ")/(" + IIIF_QUALITY + ")\\.(" + IIIF_FORMAT + ")$";
  private static final Pattern IIIF_URL_PATTERN = Pattern.compile("^https?://.+" + IIIF_PARAMS_URI_REGEX + "$");
  private static final Pattern IIIF_URL_EXTENSION_FORMAT = Pattern.compile("\\.([^.]+)$");

  private static final String IIIF_INFO_JSON_V2 = "http://iiif.io/api/image/2/context.json";
  private static final String IIIF_INFO_JSON_V3 = "http://iiif.io/api/image/3/context.json";

  private final ResourceDownloadClient resourceDownloadClient;

  /**
   * Instantiates a new Iiif validation.
   * TODO JV: Ensure that the timeout and redirect parameters are not the default ones, but come
   *  from the configuration.
   */
  public IIIFValidation() {
    this.resourceDownloadClient = new ResourceDownloadClient(DEFAULT_MAX_REDIRECT_COUNT,
        value -> true, DEFAULT_RESOURCE_CONNECT_TIMEOUT,
        DEFAULT_RESOURCE_RESPONSE_TIMEOUT,
        DEFAULT_RESOURCE_DOWNLOAD_TIMEOUT);
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
   * Fetch info json iiif info json.
   *
   * @param rdfResourceEntry the rdf resource entry
   * @return the iiif info json
   */
  public IIIFInfoJson fetchInfoJson(RdfResourceEntry rdfResourceEntry) {
    IIIFInfoJson iiifInfoJson = null;
    try {
      final String baseUrl = rdfResourceEntry.getResourceUrl().replaceAll(IIIF_PARAMS_URI_REGEX, "");
      final RdfResourceEntry infoJsonResourceEntry = new RdfResourceEntry(baseUrl + "/info.json",
          rdfResourceEntry.getUrlTypes(), rdfResourceEntry.getResourceKind());
      Resource resource = resourceDownloadClient.download(new ImmutablePair<>(infoJsonResourceEntry, DownloadMode.MIME_TYPE));
      final String fieldContext = "@context";
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      JsonNode jsonNode = objectMapper.readTree(resource.getContentStream().readAllBytes());
      if (jsonNode.get(fieldContext).isArray()
          // according to documentation
          // the context is at last position on info.json v3
          && IIIF_INFO_JSON_V3.equals(jsonNode.get(fieldContext).get(jsonNode.get(fieldContext).size() - 1).asText())) {
        iiifInfoJson = objectMapper.readValue(jsonNode.toString(), IIIFInfoJsonV3.class);
      } else if (IIIF_INFO_JSON_V3.equals(jsonNode.get(fieldContext).asText())) {
        iiifInfoJson = objectMapper.readValue(jsonNode.toString(), IIIFInfoJsonV3.class);
      } else if (IIIF_INFO_JSON_V2.equals(jsonNode.get(fieldContext).asText())) {
        iiifInfoJson = objectMapper.readValue(jsonNode.toString(), IIIFInfoJsonV2.class);
      } else {
        LOGGER.warn("No info json found for IIIF resource entry. {} ", rdfResourceEntry.getResourceUrl());
      }
    } catch (IOException e) {
      LOGGER.error("error while trying to fetch info json", e);
    }
    return iiifInfoJson;
  }

  /**
   * Adjust resource entry to small iiif rdf resource entry.
   *
   * @param resourceEntry the resource entry
   * @param infoJson the info json
   * @return the rdf resource entry
   */
  public static RdfResourceEntry adjustResourceEntryToSmallIIIF(RdfResourceEntry resourceEntry, IIIFInfoJson infoJson)  {
    final Matcher matcher = IIIF_URL_EXTENSION_FORMAT.matcher(resourceEntry.getResourceUrl());
    final String extensionFormat;
    if (matcher.find()) {
      extensionFormat = "." + matcher.group(1);
    } else {
      extensionFormat = ".jpg";
    }
    if (infoJson != null) {
      final String newUrl = infoJson.getId() + "/full/!400,400/0/default" + extensionFormat;
      return new RdfResourceEntry(newUrl, resourceEntry.getUrlTypes(), resourceEntry.getResourceKind());
    }
    return resourceEntry;
  }
}
