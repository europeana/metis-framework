package eu.europeana.metis.mediaprocessing.http;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.protocol.RedirectLocations;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents an HTTP request client that can be used to resolve a resource link. This
 * client is thread-safe, but the connection settings are tuned for use by one thread only.
 *
 * @param <I> The type of the resource entry (the input object defining the request).
 * @param <R> The type of the resulting/downloaded object (the result of the request).
 */
abstract class AbstractHttpClient<I, R> implements Closeable {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractHttpClient.class);

  private static final int HTTP_SUCCESS_MIN_INCLUSIVE = HttpStatus.SC_OK;
  private static final int HTTP_SUCCESS_MAX_EXCLUSIVE = HttpStatus.SC_MULTIPLE_CHOICES;

  private static final long CLEAN_TASK_CHECK_INTERVAL_IN_SECONDS = 60L;
  private static final long MAX_TASK_IDLE_TIME_IN_SECONDS = 300L;

  private final PoolingHttpClientConnectionManager connectionManager;
  private final CloseableHttpClient client;
  private final ScheduledExecutorService connectionCleaningSchedule = Executors
      .newScheduledThreadPool(1);

  private final int requestTimeout;

  /**
   * Constructor.
   *
   * @param maxRedirectCount The maximum number of times we follow a redirect status (status 3xx).
   * @param connectTimeout The connection timeout in milliseconds.
   * @param responseTimeout The response timeout in milliseconds.
   * @param requestTimeout The time after which the request will be aborted (if it hasn't finished
   * by then). In milliseconds.
   */
  AbstractHttpClient(int maxRedirectCount, int connectTimeout, int responseTimeout,
      int requestTimeout) {

    // Set the request config settings
    final RequestConfig requestConfig = RequestConfig.custom().setMaxRedirects(maxRedirectCount)
            .setConnectTimeout(Timeout.ofMilliseconds(connectTimeout))
            .setResponseTimeout(Timeout.ofMilliseconds(responseTimeout)).build();
    this.requestTimeout = requestTimeout;

    // Create a connection manager tuned to one thread use.
    connectionManager = new PoolingHttpClientConnectionManager();
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
      connectionManager.closeExpired();
      connectionManager.closeIdle(TimeValue.ofSeconds(MAX_TASK_IDLE_TIME_IN_SECONDS));
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
   * other than 2xx).
   */
  public R download(I resourceEntry) throws IOException {

    // Set up the connection.
    final String resourceUlr = getResourceUrl(resourceEntry);
    final HttpGet httpGet = new HttpGet(resourceUlr);
    final HttpClientContext context = HttpClientContext.create();

    // Set up the abort trigger
    final TimerTask abortTask = new TimerTask() {
      @Override
      public void run() {
        LOGGER.info("Aborting request due to time limit: {}.", resourceUlr);
        httpGet.abort();
      }
    };
    final Timer timer = new Timer(true);
    timer.schedule(abortTask, requestTimeout);

    // Execute the request.
    try (final CloseableHttpResponse response = client.execute(httpGet, context)) {

      // Check response code.
      final int status = response.getCode();
      if (!httpCallIsSuccessful(status)) {
        throw new IOException("Download failed of resource " + resourceUlr + ". Status code " +
            status + " (message: " + response.getReasonPhrase() + ").");
      }

      // Obtain header information.
      final HttpEntity responseEntity = response.getEntity();
      final String mimeType = Optional.ofNullable(responseEntity).map(HttpEntity::getContentType)
              .orElse(null);
      final Long fileSize = Optional.ofNullable(responseEntity).map(HttpEntity::getContentLength)
          .filter(size -> size >= 0).orElse(null);
      final RedirectLocations redirectUris = context.getRedirectLocations();
      final URI actualUri = (redirectUris == null || redirectUris.size() == 0) ? httpGet.getUri()
              : redirectUris.get(redirectUris.size() - 1);

      // Process the result.
      final ContentRetriever content = responseEntity == null ?
          ContentRetriever.forEmptyContent() : responseEntity::getContent;
      return createResult(resourceEntry, actualUri, mimeType, fileSize, content);

    } catch (URISyntaxException e) {

      // Shouldn't really happen.
      throw new IOException("An unexpected exception occurred.", e);

    } catch (IOException e) {

      // If aborted, provide a nicer message. Otherwise, just rethrow.
      if (httpGet.isAborted()) {
        throw new IOException("The request was aborted: it exceeded the time limit.", e);
      }
      throw e;

    } finally {

      // Cancel abort trigger
      timer.cancel();
      abortTask.cancel();
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
   * @param mimeType The type of the resulting object, as returned by the response. Is null if no
   * mime type was provided.
   * @param fileSize The file size of the resulting object, as returned by the response. Is null if
   * no file size was provided.
   * @param contentRetriever Object that allows access to the resulting data. Note that if this
   * object is not used, the data is not transferred (or the transfer is cancelled). Note that this
   * stream cannot be used after this method returns, as the connection will be closed immediately.
   * @return The resulting object.
   * @throws IOException In case a connection or other IO problem occurred.
   */
  protected abstract R createResult(I resourceEntry, URI actualUri, String mimeType, Long fileSize,
      ContentRetriever contentRetriever) throws IOException;

  @Override
  public void close() throws IOException {
    connectionCleaningSchedule.shutdown();
    connectionManager.close();
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

    /**
     * @return A content retriever for empty content.
     */
    static ContentRetriever forEmptyContent() {
      return () -> new ByteArrayInputStream(new byte[0]);
    }
  }
}
