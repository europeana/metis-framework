package eu.europeana.metis.mediaprocessing.wrappers;

import static org.apache.tika.metadata.TikaCoreProperties.RESOURCE_NAME_KEY;

import eu.europeana.metis.mediaprocessing.model.RemoteResourceMetadata;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.Locale;
import org.apache.tika.Tika;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.HttpHeaders;
import org.apache.tika.metadata.Metadata;
import org.springframework.http.ContentDisposition;

/**
 * Wrapper class of Tika
 */
public class TikaWrapper {

  private final Tika tika;

  /**
   * It creates a new instance of Tika
   */
  public TikaWrapper() {
    this.tika = new Tika();
  }

  /**
   * This method returns the resource name giving precedence to the information
   * that is contained in Content-Disposition header if exists.
   * If Content-Disposition resource name is empty then it gets it from the URI provided.
   *
   * @param contentDisposition content-disposition header information that can contain a resource name
   * @param actualUri          actual URI that can contain a resource name
   * @return String with the resource name
   */
  private static String getResourceNameFromContentDispositionOrFromActualURI(
      ContentDisposition contentDisposition, URI actualUri) {
    final String extractedResourceName;
    if (contentDisposition != null &&
        (contentDisposition.isInline() || contentDisposition.isAttachment())) {
      extractedResourceName = contentDisposition.getFilename();
    } else {
      final String resourcePath = actualUri.getPath().trim();
      if (resourcePath.isEmpty() || resourcePath.endsWith("/")) {
        extractedResourceName = null;
      } else {
        final int slashIndex = resourcePath.lastIndexOf('/');
        extractedResourceName =
            slashIndex < 0 ? resourcePath : resourcePath.substring(slashIndex + 1);
      }
    }
    return extractedResourceName;
  }

  private static Metadata getMetadataFromResource(RemoteResourceMetadata resource) {
    final Metadata metadata = new Metadata();
    final String resourceName = getResourceNameFromContentDispositionOrFromActualURI(
        resource.getProvidedContentDisposition(), resource.getActualLocation());
    if (resourceName != null) {
      metadata.set(RESOURCE_NAME_KEY, resourceName);
    }
    if (resource.getProvidedMimeType() != null) {
      final int separatorIndex = resource.getProvidedMimeType().indexOf(';');
      final String adjustedMimeType = separatorIndex < 0 ? resource.getProvidedMimeType() :
          resource.getProvidedMimeType().substring(0, separatorIndex);
      metadata.set(HttpHeaders.CONTENT_TYPE, adjustedMimeType);
    }
    if (resource.getProvidedFileSize() != null) {
      metadata.set(HttpHeaders.CONTENT_LENGTH, resource.getProvidedFileSize().toString());
    }
    return metadata;
  }

  /**
   * It uses tika's own detect method
   *
   * @param resource The metadata of the resource
   * @param inputStream The input stream containing the resource
   * @return The mime type detected from the input stream
   * @throws IOException in case detection fails
   */
  public String detect(RemoteResourceMetadata resource, InputStream inputStream)
      throws IOException {
    return this.detectInternal(inputStream, getMetadataFromResource(resource));
  }

  /**
   * It uses tika's own detect method
   *
   * @param resource The metadata of the resource
   * @param path The path to the resource
   * @return The mime type detected from the input stream
   * @throws IOException in case detection fails
   */
  public String detect(RemoteResourceMetadata resource, Path path)
      throws IOException {
    final Metadata metadata = getMetadataFromResource(resource);
    try (final InputStream stream = TikaInputStream.get(path, metadata)) {
      return this.detectInternal(stream, metadata);
    }
  }

  /**
   * It uses tika's own detect method
   *
   * @param inputStream The input stream to detect from
   * @param metadata The metadata associated with the input stream
   * @return The mime type detected from the input stream
   * @throws IOException in case detection fails
   */
  private String detectInternal(InputStream inputStream, Metadata metadata) throws IOException {
    String detect = tika.detect(inputStream, metadata);

    //LAS normalization. We have to do a code matching since the glob matching already exists in tika.mimetypes.xml
    if ("application/x-asprs".equals(detect)) {
      if (metadata.get(RESOURCE_NAME_KEY).toLowerCase(Locale.ROOT).endsWith(".laz")) {
        detect = "application/vnd.laszip";
      } else {
        detect = "application/vnd.las";
      }
    }
    return detect;
  }
}
