package eu.europeana.metis.mediaprocessing.http;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * This class represents an HTTP request client that can be used to resolve a resource link.
 *
 * @param <I> The type of the resource entry (the input object defining the request).
 * @param <R> The type of the resulting/downloaded object (the result of the request).
 */
abstract class AbstractHttpClient<I, R> implements Closeable {

  private static final int HTTP_SUCCESS_MIN_INCLUSIVE = HttpStatus.SC_OK;
  private static final int HTTP_SUCCESS_MAX_EXCLUSIVE = HttpStatus.SC_MULTIPLE_CHOICES;

  private final CloseableHttpClient client;

  /**
   * Constructor.
   *
   * @param maxRedirectCount The maximum number of times we follow a redirect status (status 3xx).
   * @param connectTimeout The connection timeout in milliseconds.
   * @param socketTimeout The socket timeout in milliseconds.
   */
  AbstractHttpClient(int maxRedirectCount, int connectTimeout, int socketTimeout) {
    final RequestConfig requestConfig = RequestConfig.custom().setMaxRedirects(maxRedirectCount)
        .setConnectTimeout(connectTimeout).setSocketTimeout(socketTimeout).build();
    client = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
  }

  /**
   * This method resolves a resource link and returns the result. Note: this method is not meant to
   * be overridden/extended by subclasses.
   *
   * @param resourceEntry The entry (resource link) to resolve.
   * @return The resulting/downloaded object.
   * @throws IOException In case a connection or other IO problem occurred (including an HTTP status
   * other than 2xx).
   */
  public R download(I resourceEntry) throws IOException {

    // Set up the connection.
    final String resourceUlr = getResourceUrl(resourceEntry);
    final HttpGet httpGet = new HttpGet(resourceUlr);
    final HttpClientContext context = HttpClientContext.create();
    try (final CloseableHttpResponse response = client.execute(httpGet, context)) {

      // Check response code.
      final int status = response.getStatusLine().getStatusCode();
      if (!httpCallIsSuccessful(status)) {
        throw new IOException("Download failed of resource " + resourceUlr + ". Status code " +
            status + " (message: " + response.getStatusLine().getReasonPhrase() + ").");
      }

      // Obtain header information.
      final String mimeType = response.getEntity().getContentType().getValue();
      final List<URI> redirectUris = context.getRedirectLocations();
      final URI actualUri =
          redirectUris == null ? httpGet.getURI() : redirectUris.get(redirectUris.size() - 1);

      // Process the result.
      return createResult(resourceEntry, actualUri, mimeType, response.getEntity()::getContent);
    }
  }

  private static boolean httpCallIsSuccessful(int status) {
    return status >= HTTP_SUCCESS_MIN_INCLUSIVE && status < HTTP_SUCCESS_MAX_EXCLUSIVE;
  }

  /**
   * This method extracts the resource URL (where to send the request) from the resource entry.
   *
   * @param resourceEntry The resource entry for which to obtain the URL.
   * @return The URL where the resource entry can be obtained.
   */
  protected abstract String getResourceUrl(I resourceEntry);

  /**
   * This method creates the resulting object from the downloaded data. Subclasses must implement
   * this method.
   *
   * @param resourceEntry The resource for which the request was sent.
   * @param actualUri The actual URI where the resource was found (could be different from the
   * resource link after redirections).
   * @param mimeType The type of the resulting object, as returned by the response.
   * @param contentRetriever Object that allows access to the resulting data. Note that if this
   * object is not used, the data is not transferred (or the transfer is cancelled). Note that this
   * stream cannot be used after this method returns, as the connection will be closed immediately.
   * @return The resulting object.
   * @throws IOException In case a connection or other IO problem occurred.
   */
  protected abstract R createResult(I resourceEntry, URI actualUri, String mimeType,
      ContentRetriever contentRetriever) throws IOException;

  @Override
  public void close() throws IOException {
    client.close();
  }

  /**
   * Objects of this type can supply an input stream for the result content of a request. If (and
   * ONLY if) this object is used to obtain an input stream, the caller must also close that
   * stream.
   */
  @FunctionalInterface
  protected interface ContentRetriever {

    /**
     * @return An input stream for the result content.
     * @throws IOException In case a connection or other IO problem occurred.
     */
    InputStream getContent() throws IOException;
  }
}
