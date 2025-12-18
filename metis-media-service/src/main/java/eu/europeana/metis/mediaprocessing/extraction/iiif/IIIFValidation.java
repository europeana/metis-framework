package eu.europeana.metis.mediaprocessing.extraction.iiif;

import static eu.europeana.metis.mediaprocessing.MediaProcessorFactory.DEFAULT_MAX_REDIRECT_COUNT;
import static eu.europeana.metis.mediaprocessing.MediaProcessorFactory.DEFAULT_RESOURCE_CONNECT_TIMEOUT;
import static eu.europeana.metis.mediaprocessing.MediaProcessorFactory.DEFAULT_RESOURCE_DOWNLOAD_TIMEOUT;
import static eu.europeana.metis.mediaprocessing.MediaProcessorFactory.DEFAULT_RESOURCE_RESPONSE_TIMEOUT;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.metis.mediaprocessing.extraction.iiif.IIIFInfoJson.SupportedFormats;
import eu.europeana.metis.mediaprocessing.http.ResourceDownloadClient;
import eu.europeana.metis.mediaprocessing.http.ResourceDownloadClient.DownloadMode;
import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import eu.europeana.metis.mediaprocessing.model.Resource;
import java.io.IOException;
import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Iiif validation.
 */
public final class IIIFValidation {

  private static final Logger LOGGER = LoggerFactory.getLogger(IIIFValidation.class);

  private static final List<String> PRIORITIZED_ACCEPTABLE_IMAGE_FORMATS =
      List.of("jpg", "png", "gif", "webp");

  private static final String IIIF_INFO_JSON_V2 = "http://iiif.io/api/image/2/context.json";
  private static final String IIIF_INFO_JSON_V3 = "http://iiif.io/api/image/3/context.json";

  private final ResourceDownloadClient resourceDownloadClient;

  /**
   * Instantiates a new IIIF validation.
   *
   */
  public IIIFValidation() {
    this.resourceDownloadClient = new ResourceDownloadClient(DEFAULT_MAX_REDIRECT_COUNT,
        value -> true, DEFAULT_RESOURCE_CONNECT_TIMEOUT,
        DEFAULT_RESOURCE_RESPONSE_TIMEOUT,
        DEFAULT_RESOURCE_DOWNLOAD_TIMEOUT);
  }

  /**
   * Fetch info json iiif info json.
   *
   * @param rdfResourceEntry the rdf resource entry
   * @return the iiif info json
   */
  public IIIFInfoJson fetchInfoJson(RdfResourceEntry rdfResourceEntry) {
    if (rdfResourceEntry.getServiceReference() == null) {
      return null;
    }
    final RdfResourceEntry infoJsonResourceEntry = new RdfResourceEntry(
        rdfResourceEntry.getServiceReference() + "/info.json",
        rdfResourceEntry.getUrlTypes(), rdfResourceEntry.getResourceKind(),
        rdfResourceEntry.getServiceReference());
    IIIFInfoJson iiifInfoJson = null;
    try (Resource resource = resourceDownloadClient.download(
        new ImmutablePair<>(infoJsonResourceEntry, DownloadMode.MIME_TYPE))) {
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

  private static String getAcceptableImageFormat(SupportedFormats supportedFormats) {
    for (String format : PRIORITIZED_ACCEPTABLE_IMAGE_FORMATS) {
      if (supportedFormats.recommendedFormats().contains(format)) {
        return format;
      }
    }
    for (String format : PRIORITIZED_ACCEPTABLE_IMAGE_FORMATS) {
      if (supportedFormats.additionalFormats().contains(format)) {
        return format;
      }
    }
    return "jpg";
  }

  /**
   * Adjust resource entry to small iiif rdf resource entry.
   *
   * @param resourceEntry the resource entry
   * @param infoJson the info json
   * @return the rdf resource entry
   */
  public static RdfResourceEntry adjustResourceEntryToSmallIIIF(RdfResourceEntry resourceEntry,
      IIIFInfoJson infoJson)  {
    if (infoJson != null) {
      final SupportedFormats supportedFormats = infoJson.getSupportedFormats();
      final String extensionFormat = getAcceptableImageFormat(supportedFormats);
      final String newUrl = infoJson.getId() + "/full/!400,400/0/default." + extensionFormat;
      return new RdfResourceEntry(newUrl, resourceEntry.getUrlTypes(),
          resourceEntry.getResourceKind(), resourceEntry.getServiceReference());
    }
    return resourceEntry;
  }
}
