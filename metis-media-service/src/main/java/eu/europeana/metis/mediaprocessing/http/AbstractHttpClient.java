package eu.europeana.metis.mediaprocessing.http;

import eu.europeana.metis.mediaprocessing.http.wrappers.CancelableBodyWrapper;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response.Status.Family;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents an HTTP request client that can be used to resolve a resource link. This
 * client is thread-safe.
 *
 * @param <I> The type of the resource entry (the input object defining the request).
 * @param <R> The type of the resulting/downloaded object (the result of the request).
 */
abstract class AbstractHttpClient<I, R> implements Closeable {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractHttpClient.class);

  private final int connectTimeout;
  private final int responseTimeout;
  private final int requestTimeout;
  private final int maxNumberOfRedirects;

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
    this.connectTimeout = connectTimeout;
    this.responseTimeout = responseTimeout;
    this.requestTimeout = requestTimeout;
    this.maxNumberOfRedirects = maxRedirectCount;
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
    final URI resourceUri = URI.create(getResourceUrl(resourceEntry));
    final BodyHandler<InputStream> handler = BodyHandlers.ofInputStream();
    final CancelableBodyWrapper<InputStream> bodyWrapper = new CancelableBodyWrapper<>(handler);

    // Set up the abort trigger - fill the future later!
    final CompletableFuture<HttpResponse<InputStream>> futureRequest = new CompletableFuture<>();
    final TimerTask abortTask = new TimerTask() {
      @Override
      public void run() {
        LOGGER.info("Aborting request due to time limit: {}.", resourceUri.getPath());
        bodyWrapper.cancel();
        futureRequest.thenAccept(response -> {
          try {
            response.body().close();
          } catch (IOException e) {
            LOGGER.warn("Something went wrong while trying to close the input stream after"
                + " cancelling the http request.", e);
          }
        });
      }
    };
    final Timer timer = new Timer(true);
    timer.schedule(abortTask, requestTimeout);

    final ExecutorService httpConnectionsThreadPool = Executors.newSingleThreadExecutor();
    HttpResponse<InputStream> httpResponse = null;
    try {

      // Create the client.
      final HttpClient httpClient = HttpClient.newBuilder().executor(httpConnectionsThreadPool)
              .connectTimeout(Duration.ofMillis(connectTimeout)).build();

      // Execute the request and save the result.
      final Pair<HttpResponse<InputStream>, URI> redirectedResponse = makeRedirectedRequest(
          resourceUri, httpClient, bodyWrapper);
      final URI actualUri = redirectedResponse.getRight();
      httpResponse = redirectedResponse.getLeft();
      futureRequest.complete(httpResponse);

      // Obtain header information.
      final String mimeType = httpResponse.headers().firstValue(HttpHeaders.CONTENT_TYPE)
          .filter(StringUtils::isNotBlank).orElse(null);
      final long fileSize = httpResponse.headers().firstValueAsLong(HttpHeaders.CONTENT_LENGTH)
          .orElse(0);

      // Process the result.
      final ContentRetriever content =
          httpResponse.body() == null ? ContentRetriever.forEmptyContent() : httpResponse::body;
      final R result;
      try {
        result = createResult(resourceEntry, actualUri, mimeType, fileSize <= 0 ? null : fileSize,
            content);
      } catch (IOException | RuntimeException e) {
        if (bodyWrapper.isCancelled()) {
          throw new IOException("The request was aborted: it exceeded the time limit.", e);
        } else {
          throw e;
        }
      }

      // If aborted (and createResult did not throw an exception) throw exception anyway.
      if (bodyWrapper.isCancelled()) {
        throw new IOException("The request was aborted: it exceeded the time limit.");
      }

      // Done.
      return result;
    } finally {

      // Cancel abort trigger
      timer.cancel();
      abortTask.cancel();

      // Close the connection thread and the response.
      httpConnectionsThreadPool.shutdownNow();
      if (httpResponse != null) {
        httpResponse.body().close();
      }
    }
  }

  private Pair<HttpResponse<InputStream>, URI> makeRedirectedRequest(final URI resourceUri,
      HttpClient httpClient, CancelableBodyWrapper<InputStream> bodyWrapper) throws IOException {

    // Bootstrap the redirection loop
    URI currentLocation = resourceUri;
    HttpResponse<InputStream> currentResponse = null;
    int redirectsLeft = maxNumberOfRedirects;

    try {

      // Loop while we get redirects.
      while (true) {

        // Perform the request. If we get a non-redirect result, we're done.
        currentResponse = makeHttpRequest(currentLocation, httpClient, bodyWrapper);
        if (Family.familyOf(currentResponse.statusCode()) != Family.REDIRECTION) {
          break;
        }

        // So it is a redirect. Check and reduce redirect counter.
        if (redirectsLeft == 0) {
          throw new IOException("Could not retrieve the entity: too many redirects.");
        }
        redirectsLeft--;

        // Compute current location.
        currentLocation = currentResponse.headers().firstValue(HttpHeaders.LOCATION)
            .filter(StringUtils::isNotBlank).map(currentLocation::resolve).orElseThrow(
                () -> new IOException("There was problems retrieving the redirect Location value"));

        // Prepare for next step.
        currentResponse.body().close();
      }

      // If we don't have success, we throw an exception.
      if (Family.familyOf(currentResponse.statusCode()) != Family.SUCCESSFUL) {
        throw new IOException(String
            .format("Download failed of resource %s. Status code %s for location %s", resourceUri,
                currentResponse.statusCode(), currentLocation));
      }

      // We have success. Done.
      return Pair.of(currentResponse, currentLocation);

    } catch (IOException | RuntimeException e) {

      // Make sure to close any response before throwing any exception.
      if (currentResponse != null) {
        currentResponse.body().close();
      }
      throw e;
    }
  }

  private HttpResponse<InputStream> makeHttpRequest(URI uri, HttpClient httpClient,
      CancelableBodyWrapper<InputStream> bodyWrapper) throws IOException {

    // Create the request.
    final HttpRequest httpRequest = HttpRequest.newBuilder().GET()
        .timeout(Duration.ofMillis(responseTimeout)).uri(uri).build();

    // Execute the request.
    final HttpResponse<InputStream> response;
    try {
      response = httpClient.send(httpRequest, bodyWrapper);
    } catch (InterruptedException interruptedException) {
      LOGGER.info("The thread was interrupted");
      Thread.currentThread().interrupt();
      throw new IOException("Could not retrieve the response: thread was interrupted.",
          interruptedException);
    }

    // Return the response object.
    if (response == null) {
      throw new IOException("Could not retrieve the response: object was null.");
    }
    return response;
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
   * Also, the stream could be closed at any time (e.g. when the request times out), at which point
   * an exception should be thrown.
   * @return The resulting object.
   * @throws IOException In case a connection or other IO problem occurred.
   */
  protected abstract R createResult(I resourceEntry, URI actualUri, String mimeType, Long fileSize,
      ContentRetriever contentRetriever) throws IOException;

  @Override
  public void close() {
    // Nothing to do.
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
