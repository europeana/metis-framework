package eu.europeana.metis.network;

import eu.europeana.metis.network.StringHttpClient.StringContent;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.springframework.http.ContentDisposition;

/**
 * An implementation of {@link AbstractHttpClient} that returns a String (assuming UTF-8 encoding).
 */
public class StringHttpClient extends AbstractHttpClient<URI, StringContent> {

  /**
   * Constructor.
   *
   * @param maxRedirectCount The maximum number of times we follow a redirect status (status 3xx).
   * @param connectTimeout The connection timeout in milliseconds.
   * @param responseTimeout The response timeout in milliseconds.
   * @param requestTimeout The time after which the request will be aborted (if it hasn't finished
   */
  public StringHttpClient(int maxRedirectCount, int connectTimeout, int responseTimeout,
          int requestTimeout) {
    super(maxRedirectCount, connectTimeout, responseTimeout, requestTimeout);
  }

  @Override
  protected String getResourceUrl(URI resourceEntry) {
    return resourceEntry.toString();
  }

  @Override
  protected StringContent createResult(URI providedLink, URI actualUri, ContentDisposition contentDisposition,
                                       String mimeType, Long fileSize, ContentRetriever contentRetriever) throws IOException {
    try (final InputStream inputStream = contentRetriever.getContent()) {
      return new StringContent(IOUtils.toString(inputStream, StandardCharsets.UTF_8), mimeType);
    }
  }

  /**
   * A class for String content and the content's media type (mime type).
   */
  public static class StringContent {

    private final String content;
    private final String contentType;

    /**
     * Constructor.
     *
     * @param content The content itself.
     * @param contentType The (media/mime) type of the content.
     */
    public StringContent(String content, String contentType) {
      this.content = content;
      this.contentType = contentType;
    }

    public String getContent() {
      return content;
    }

    public String getContentType() {
      return contentType;
    }
  }
}
