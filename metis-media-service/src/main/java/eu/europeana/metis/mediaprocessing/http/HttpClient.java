package eu.europeana.metis.mediaprocessing.http;

import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

/**
 * This class represents an HTTP request client that can be used to resolve a resource link.
 *
 * @param <R> The type of the resulting/downloaded object (the result of the request).
 */
abstract class HttpClient<R> implements Closeable {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpClient.class);

  private static final long CLEAN_TASK_CHECK_INTERVAL_IN_SECONDS = 60L;
  private static final long MAX_TASK_IDLE_TIME_IN_SECONDS = 300L;

  private final CloseableHttpClient client;
  private final ScheduledExecutorService connectionCleaningSchedule = Executors
      .newScheduledThreadPool(1);

  /**
   * Constructor.
   *
   * @param maxRedirectCount The maximum number of times we follow a redirect status (status 3xx).
   * @param connectTimeout The connection timeout in milliseconds.
   * @param socketTimeout The socket timeout in milliseconds.
   */
  HttpClient(int maxRedirectCount, int connectTimeout, int socketTimeout) {

    // Set the request config settings
    final RequestConfig requestConfig = RequestConfig.custom().setMaxRedirects(maxRedirectCount)
        .setConnectTimeout(connectTimeout).setSocketTimeout(socketTimeout).build();

    // Create a connection manager tuned to one thread use.
    final PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
    connectionManager.setDefaultMaxPerRoute(1);

    // Build the client.
    client = HttpClients.custom().setDefaultRequestConfig(requestConfig)
        .setConnectionManager(connectionManager).build();

    // Start the cleaning thread.
    connectionCleaningSchedule.scheduleWithFixedDelay(() -> cleanConnections(connectionManager),
        CLEAN_TASK_CHECK_INTERVAL_IN_SECONDS, CLEAN_TASK_CHECK_INTERVAL_IN_SECONDS,
        TimeUnit.SECONDS);
  }

  private void cleanConnections(PoolingHttpClientConnectionManager connectionManager) {
    try {
      connectionManager.closeExpiredConnections();
      connectionManager
          .closeIdleConnections(MAX_TASK_IDLE_TIME_IN_SECONDS, TimeUnit.SECONDS);
    } catch (RuntimeException e) {
      LOGGER.warn("Could not clean up expired and idle connections.", e);
    }
  }

  /**
   * This method resolves a resource link and returns the result. Note: this method is not meant to
   * be overridden/extended by subclasses.
   *
   * @param resourceEntry The entry (resource link) to resolve.
   * @return The resulting/downloaded object.
   * @throws IOException In case a connection or other IO problem occurred (including an HTTP status
   *         other than 2xx).
   */
  public R download(RdfResourceEntry resourceEntry) throws IOException {

    // Set up the connection.
    final HttpGet httpGet = new HttpGet(resourceEntry.getResourceUrl());
    final HttpClientContext context = HttpClientContext.create();
    try (final CloseableHttpResponse response = client.execute(httpGet, context)) {

      // Check response code.
      final int status = response.getStatusLine().getStatusCode();
      if (status < 200 || status >= 300) {
        throw new IOException(
            "Download failed of resource " + resourceEntry.getResourceUrl() + ". Status code "
                + status + " (message: " + response.getStatusLine().getReasonPhrase() + ").");
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

  /**
   * This method creates the resulting object from the downloaded data. Subclasses must implement
   * this method.
   *
   * @param resourceEntry The resource for which the request was sent.
   * @param actualUri The actual URI where the resource was found (could be different from the
   *        resource link after redirections).
   * @param mimeType The type of the resulting object, as returned by the response.
   * @param contentRetriever Object that allows access to the resulting data. Note that if this
   *        object is not used, the data is not transferred (or the transfer is cancelled). Note
   *        that this stream cannot be used after this method returns, as the connection will be
   *        closed immediately.
   * @return The resulting object.
   * @throws IOException In case a connection or other IO problem occurred.
   */
  protected abstract R createResult(RdfResourceEntry resourceEntry, URI actualUri, String mimeType,
      ContentRetriever contentRetriever) throws IOException;

  @Override
  public void close() throws IOException {
    connectionCleaningSchedule.shutdown();
    client.close();
  }

  /**
   * Objects of this type can supply an input stream for the result content of a request. If (and
   * ONLY if) this object is used to obtain an input stream, the caller must also close that stream.
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
