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
import java.util.Objects;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.Status.Family;
import org.apache.commons.lang3.StringUtils;
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

  private final ScheduledExecutorService connectionCleaningSchedule = Executors
      .newScheduledThreadPool(1);

  private final int responseTimeout;
  private final int requestTimeout;
  private final HttpClient httpClient;
  private final int maxNumberOfRedirects;
  private HttpResponse<InputStream> httpResponse;

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

    httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofMillis(connectTimeout))
        .build();

    maxNumberOfRedirects = maxRedirectCount;

    // Set the request config settings
    this.responseTimeout = responseTimeout;
    this.requestTimeout = requestTimeout;

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

    BodyHandler<InputStream> handler = BodyHandlers.ofInputStream();
    CancelableBodyWrapper<InputStream> bodyWrapper = new CancelableBodyWrapper<>(handler);

     httpResponse = makeHttpRequest(resourceUri, bodyWrapper);

    // Set up the abort trigger
    final TimerTask abortTask = new TimerTask() {
      @Override
      public void run() {
        LOGGER.info("Aborting request due to time limit: {}.", resourceUri.getPath());
        bodyWrapper.cancel();
        if (httpResponse.body() != null) {
          try {
            httpResponse.body().close();
          } catch (IOException e) {
            LOGGER.warn(
                    "Something went wrong while trying to close the input stream after cancelling the http request.",
                    e);
          }
        }
      }
    };
    final Timer timer = new Timer(true);
    timer.schedule(abortTask, requestTimeout);

    final int statusCode;

    if (httpResponse != null) {
      statusCode = httpResponse.statusCode();
    } else {
      throw new IOException("A problem occurred when sending the request");
    }

    final URI actualUri = httpResponse.uri();

    if(actualUri == null){
      throw new IOException("There was some trouble retrieving the uri");
    }


    // Do first check redirection and analysis
    if(Family.familyOf(statusCode) == Family.REDIRECTION){
      final String redirectUris = Optional.ofNullable(httpResponse.headers().firstValue(HttpHeaders.LOCATION))
          .map(Optional::get)
          .filter(StringUtils::isNotBlank)
          .orElseThrow(IOException::new);
      httpResponse = performRedirect(statusCode, resourceUri.resolve(redirectUris),
          maxNumberOfRedirects, bodyWrapper);
    } else if (Status.fromStatusCode(statusCode) != Status.OK) {
      throw new IOException(
          String.format("Download failed of resource %s. Status code %s", resourceUri, statusCode));
    }

    // Obtain header information.
    final String mimeType = Optional.ofNullable(httpResponse.headers().firstValue(HttpHeaders.CONTENT_TYPE))
        .map(Optional::get)
        .filter(StringUtils::isNotBlank)
        .orElse(null);
    final long fileSize = httpResponse.headers().firstValueAsLong(HttpHeaders.CONTENT_LENGTH).orElse(0);

    // Process the result.
    final ContentRetriever content = httpResponse.body() == null ?
        ContentRetriever.forEmptyContent() : httpResponse::body;
    final R result;
    try {
      result = createResult(resourceEntry, actualUri, mimeType,
              fileSize <= 0 ? null : fileSize, content);
    } catch (IOException | RuntimeException e) {
      if (bodyWrapper.isCancelled()) {
        throw new IOException("The request was aborted: it exceeded the time limit.");
      }else{
        throw e;
      }
    }

    // If aborted (and createResult did not throw an exception) throw exception anyway.
    if (bodyWrapper.isCancelled()) {
      throw new IOException("The request was aborted: it exceeded the time limit.");
    }

    // Cancel abort trigger
    timer.cancel();
    abortTask.cancel();

    return result;
  }

  private HttpResponse<InputStream> performRedirect(int statusCode, URI location, int redirectsLeft,
      CancelableBodyWrapper<InputStream> bodyWrapper)
      throws IOException {

    HttpResponse<InputStream> response = null;

    while (Status.Family.familyOf(statusCode) == Family.REDIRECTION) {
      if (redirectsLeft > 0 && location != null) {
        response = makeHttpRequest(location, bodyWrapper);

        if (response != null) {
          statusCode = response.statusCode();
          location = response.headers().firstValue(HttpHeaders.LOCATION).map(location::resolve)
              .orElse(location);
        } else {
          statusCode = 0;
          location = null;
        }

        redirectsLeft--;


      } else {
        throw new IOException("Could not retrieve the entity: too many redirects.");
      }
    }

    if (response != null){
      return response;
    }
    else {
      throw new IOException("There was trouble retrieving the response from the redirect request");
    }

  }

  private HttpResponse<InputStream> makeHttpRequest(URI uri,
      CancelableBodyWrapper<InputStream> bodyWrapper) {

    HttpResponse<InputStream> httpResponse = null;

    HttpRequest httpRequest = HttpRequest.newBuilder()
        .GET()
        .timeout(Duration.ofMillis(responseTimeout))
        .uri(uri)
        .build();

    // Execute the request.
    try {
      httpResponse = httpClient.send(httpRequest, bodyWrapper);
    } catch (InterruptedException interruptedException) {
      LOGGER.info("A problem occurred while sending a request");
      Thread.currentThread().interrupt();
    } catch(IOException e){
      LOGGER.info("A problem occurred while sending a request");
    }

    return httpResponse;
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
  public void close() throws IOException {
    connectionCleaningSchedule.shutdown();
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
