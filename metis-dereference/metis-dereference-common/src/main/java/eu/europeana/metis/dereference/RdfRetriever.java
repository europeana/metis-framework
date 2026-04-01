package eu.europeana.metis.dereference;

import eu.europeana.metis.common.rdf.ComplianceException;
import eu.europeana.metis.common.rdf.RdfRepresentationConverter;
import eu.europeana.metis.common.rdf.RdfRepresentation;
import eu.europeana.metis.network.StringHttpClient;
import eu.europeana.metis.network.StringHttpClient.StringContent;
import jakarta.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

/**
 * Helper class to retrieve a remote unmapped entity.
 */
public class RdfRetriever {

  private static final int MAX_NUMBER_OF_REDIRECTS = 5;
  private static final int CONNECT_TIMEOUT = 10_000;
  private static final int RESPONSE_TIMEOUT = 20_000;
  private static final int REQUEST_TIMEOUT = 60_000;

  private static final String DEFAULT_MEDIA_TYPE = "application/rdf+xml";
  private static final String DEFAULT_USER_AGENT = "MetisDereferencer/1.0 (Europeana Foundation)";

  /**
   * Retrieve a remote entity from a resource as a String.
   *
   * @param resourceId           The remote entity to retrieve (resource IDs are in fact URIs)
   * @param resourceUriGenerator A generator for the resource's URI. Cannot be null.
   * @param mediaType            The media type to set. If null, the default media type is used.
   * @param userAgent            The user agent to set. If null, the default user agent is used.
   * @return A string representation of the remote entity.
   * @throws IOException If there was an issue retrieving the entity, the provided resource ID
   *                     does not form a valid URI, or the resource could not be converted to XML.
   */
  public String retrieve(String resourceId, ResourceUriGenerator resourceUriGenerator,
      String mediaType, String userAgent) throws IOException {
    if (resourceId == null) {
      throw new IllegalArgumentException("Parameter resourceId cannot be null.");
    }
    try {
      return retrieveFromSource(resourceUriGenerator.generateUri(resourceId), mediaType, userAgent);
    } catch (URISyntaxException | ComplianceException e) {
      throw new IOException(e.getMessage(), e);
    }
  }

  /**
   * Retrieve remote content from a resource as a String.
   *
   * @param resourceUri The location of the content to retrieve.
   * @param mediaType   The media type to set. If null, the default media type is used.
   * @param userAgent   The user agent to set. If null, the default user agent is used.
   * @return A string representation of the remote content.
   * @throws IOException If there was an issue retrieving the content, the provided location does
   *                     not form a valid URI, or the content could not be converted to XML.
   */
  public String retrieve(String resourceUri, String mediaType, String userAgent)
      throws IOException {
    return retrieve(resourceUri, ResourceUriGenerator.identityGenerator(), mediaType, userAgent);
  }

  private static String retrieveFromSource(URI resourceUri, String mediaType, String userAgent)
      throws IOException, ComplianceException {

    // Check the media type
    final String mediaTypeToUse = Optional.ofNullable(mediaType).orElse(DEFAULT_MEDIA_TYPE);
    final RdfRepresentation representation = RdfRepresentation.forMediaType(mediaTypeToUse);
    if (representation == null) {
      throw new IllegalArgumentException(
          "Could not retrieve the entity: unknown media type: " + mediaType + ".");
    }

    // Obtain the response.
    final Map<String, String> headers = Map.of(HttpHeaders.ACCEPT, mediaTypeToUse,
        HttpHeaders.USER_AGENT, Optional.ofNullable(userAgent).orElse(DEFAULT_USER_AGENT));
    final StringContent response;
    try (final StringHttpClient client = new StringHttpClient(MAX_NUMBER_OF_REDIRECTS,
        CONNECT_TIMEOUT, RESPONSE_TIMEOUT, REQUEST_TIMEOUT)) {
      response = client.download(resourceUri, headers);
    }

    // Check the response.
    if (StringUtils.isBlank(response.getContent())) {
      throw new IOException("Could not retrieve the entity: it is empty.");
    } else if (Strings.CS.startsWith(response.getContentType(), "text/html")
        || response.getContent().contains("<html>")) {
      throw new IOException("Could not retrieve the entity: seems to be an HTML document.");
    }

    // Convert the response to XML. If already XML, return the content unchanged (this is for
    // backwards compatibility reasons: all existing XSLT transformations assume unchanged content).
    final String result;
    if (representation == RdfRepresentation.XML) {
      result = response.getContent();
    } else {
      result = RdfRepresentationConverter.convertToXmlAndNormalizeHierarchy(response.getContent(),
          representation, resourceUri.toString());
    }
    return result;
  }
}
