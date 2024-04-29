package eu.europeana.metis.network;

import static eu.europeana.metis.utils.SonarqubeNullcheckAvoidanceUtils.performThrowingFunction;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.protocol.RedirectLocations;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ContentDisposition;

/**
 * <p>
 * This class represents an HTTP request client that can be used to resolve a link. This
 * client is thread-safe, but the connection settings are tuned for use by one thread only.
 * </p>
 * <p>
 * <i>Implementation note:</i> We are using Apache's http client, which, at the time of writing,
 * has some unexpected behavior when it comes to aborted/canceled requests. This client supports not
 * downloading the entire content (e.g. when we know we don't need it). But if we close the input
 * stream containing the content, it waits for all IO to finish (which could mean downloading the
 * entire content anyway). To circumvent this problem, we don't close any of the streams, we just
 * stop reading from them. Then we cancel the whole request, and only then close all associated
 * resources.
 * </p>
 *
 * @param <I> The type of the link (the input object defining the request).
 * @param <R> The type of the resulting/downloaded object (the result of the request).
 */
public abstract class AbstractHttpClient<I, R> implements Closeable {

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
  protected AbstractHttpClient(int maxRedirectCount, int connectTimeout, int responseTimeout,
          int requestTimeout) {

    final ConnectionConfig connectionConfig = ConnectionConfig.custom()
                                                   .setConnectTimeout(Timeout.ofMilliseconds(connectTimeout))
                                                   .setSocketTimeout(Timeout.ofMilliseconds(responseTimeout)).build();

    // Create a connection manager tuned to one thread use.
    connectionManager = new PoolingHttpClientConnectionManager();
    connectionManager.setDefaultConnectionConfig(connectionConfig);
    connectionManager.setDefaultMaxPerRoute(1);

    // Set the request config settings
    final RequestConfig requestConfig = RequestConfig.custom().setMaxRedirects(maxRedirectCount).build();
    this.requestTimeout = requestTimeout;

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
   * @param link The link to resolve.
   * @return The resulting/downloaded object.
   * @throws IOException In case a connection or other IO problem occurred (including an HTTP status
   * other than 2xx).
   */
  public R download(I link) throws IOException {
    return download(link, Collections.emptyMap());
  }

  /**
   * This method resolves a resource link and returns the result. Note: this method is not meant to
   * be overridden/extended by subclasses.
   *
   * @param link The link to resolve.
   * @param requestHeaders The request headers to set. This is a map, mapping the header names to
   * the (full) header values.
   * @return The resulting/downloaded object.
   * @throws IOException In case a connection or other IO problem occurred (including an HTTP status
   * other than 2xx).
   */
  public R download(I link, Map<String, String> requestHeaders) throws IOException {

    // Set up the connection.
    final String resourceUrl = getResourceUrl(link);
    final HttpGet httpGet;
    try {
      httpGet = new HttpGet(resourceUrl);
    } catch (IllegalArgumentException e) {
      LOGGER.debug("Malformed URL", e);
      throw new MalformedURLException(e.getMessage());
    }
    requestHeaders.forEach(httpGet::setHeader);
    final HttpClientContext context = HttpClientContext.create();

    // Set up the abort trigger
    final TimerTask abortTask = new TimerTask() {
      @Override
      public void run() {
        synchronized (httpGet) {
          if (httpGet.cancel()) {
            LOGGER.info("Aborting request due to time limit: {}.", resourceUrl);
          }
        }
      }
    };
    final Timer timer = new Timer(true);
    timer.schedule(abortTask, requestTimeout);

    // We prepare a list of closeables, as we only want to close things after aborting the request.
    final ArrayList<Closeable> closeables = new ArrayList<>();

    // Execute the request.
    try {
      final CloseableHttpResponse responseObject = client.execute(httpGet, context);
      closeables.add(responseObject);

      // Do first analysis
      final HttpEntity responseEntity = performThrowingFunction(responseObject, response -> {
        final int status = response.getCode();
        if (!httpCallIsSuccessful(status)) {
          throw new IOException("Download failed of resource " + resourceUrl + ". Status code " +
              status + " (message: " + response.getReasonPhrase() + ").");
        }
        return response.getEntity();
      });
      closeables.add(responseEntity);

      // Obtain header information.
      final String mimeType = Optional.ofNullable(responseEntity).map(HttpEntity::getContentType)
              .orElse(null);
      final Long fileSize = Optional.ofNullable(responseEntity).map(HttpEntity::getContentLength)
              .filter(size -> size >= 0).orElse(null);
      final RedirectLocations redirectUris = context.getRedirectLocations();
      final URI actualUri = (redirectUris == null || redirectUris.size() == 0) ? httpGet.getUri()
              : redirectUris.get(redirectUris.size() - 1);
      final ContentDisposition contentDisposition = Optional.ofNullable(responseObject).map( re -> {
                try {
                  return re.getHeader("Content-Disposition") != null ?
                          ContentDisposition.parse(re.getHeader("Content-Disposition").getValue()) : null;
                } catch (ProtocolException ex) {
                  LOGGER.debug("No content-disposition header, nothing to do", ex);
                  return null;
                }
              }
      ).orElse(null);
      // Obtain the result (check for timeout just in case).
      final ContentRetriever contentRetriever = ContentRetriever.forNonCloseableContent(
              responseEntity == null ? InputStream::nullInputStream : responseEntity::getContent,
              closeables::add);
      return createResult(link, actualUri, contentDisposition, mimeType, fileSize,  contentRetriever);

    } catch (URISyntaxException e) {

      // Shouldn't really happen.
      throw new IOException("An unexpected exception occurred.", e);

    } catch (RuntimeException | IOException e) {

      // If aborted, provide a nicer message. Otherwise, just rethrow.
      if (httpGet.isCancelled()) {
        throw new IOException("The request was aborted: it exceeded the time limit.", e);
      }
      throw e;

    } finally {

      // Cancel abort trigger
      timer.cancel();
      abortTask.cancel();

      // Cancel the request to stop downloading.
      synchronized (httpGet) {
        if (httpGet.cancel()) {
          LOGGER.debug("Aborting request after all processing is completed: {}.", resourceUrl);
        }
      }

      // Only now can we close the input streams.
      closeables.forEach(closeable -> {
        try {
          closeable.close();
        } catch (IOException | RuntimeException e) {
          LOGGER.debug("Closing all resources after all processing is completed.", e);
        }
      });
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
   * @param providedLink The link for which the request was sent.
   * @param actualUri The actual URI where the resource was found (could be different from the
   * provided link after redirections).
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
  protected abstract R createResult(I providedLink, URI actualUri, ContentDisposition contentDisposition, String mimeType, Long fileSize,
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
     * Create a content retriever for content that should not be closed.
     * @param contentRetriever A content retriever that can supply the content.
     * @param contentRetrievedListener Callback for when the content is retrieved.
     * @return A content retriever.
     */
    static ContentRetriever forNonCloseableContent(ContentRetriever contentRetriever,
            final Consumer<InputStream> contentRetrievedListener) {
      return () -> {
        final InputStream content = contentRetriever.getContent();
        contentRetrievedListener.accept(content);
        return new UnclosedInputStream(content);
      };
    }
  }

  /**
   * An implementation of InputStream that can not be closed.
   */
  private static class UnclosedInputStream extends InputStream {

    private final InputStream source;

    public UnclosedInputStream(InputStream source) {
      this.source = source;
    }

    @Override
    public int available() throws IOException {
      return source.available();
    }

    @Override
    public int read() throws IOException {
      return source.read();
    }

    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {
      return source.read(buffer, offset, length);
    }

    @Override
    public void close() {
      // We avoid closing the input stream.
    }
  }
}
