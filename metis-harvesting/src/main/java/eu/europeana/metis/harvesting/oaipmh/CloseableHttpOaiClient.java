package eu.europeana.metis.harvesting.oaipmh;

import eu.europeana.metis.harvesting.HarvestingClientSettings;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.dspace.xoai.serviceprovider.exceptions.HttpException;
import org.dspace.xoai.serviceprovider.parameters.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an implementation of {@link org.dspace.xoai.serviceprovider.client.OAIClient} that needs
 * to be closed.
 *
 * @see org.dspace.xoai.serviceprovider.client.HttpOAIClient
 */
public class CloseableHttpOaiClient implements CloseableOaiClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(CloseableHttpOaiClient.class);

  private final String baseUrl;
  private final CloseableHttpClient httpClient;
  private final int numberOfRetries;
  private final int timeBetweenRetries;

  /**
   * Constructor.
   *
   * @param baseUrl The base URL of the OAI-PMH repository.
   * @param settings The client settings.
   */
  public CloseableHttpOaiClient(String baseUrl, HarvestingClientSettings settings) {
    this.baseUrl = baseUrl;
    this.numberOfRetries = settings.getNumberOfRetries();
    this.timeBetweenRetries = settings.getTimeBetweenRetries();
    final RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(settings.getConnectionTimeout())
            .setSocketTimeout(settings.getSocketTimeout())
            .setConnectionRequestTimeout(settings.getRequestTimeout())
            .build();
    this.httpClient = HttpClientBuilder.create()
            .setDefaultRequestConfig(requestConfig)
            .setUserAgent(settings.getUserAgent())
            .build();
  }

  @Override
  public InputStream execute(Parameters parameters) throws HttpException {
    for (int triesLeft = this.numberOfRetries; triesLeft >= 0; triesLeft--) {
      try {
        return executeOnce(parameters);
      } catch (HttpException | RuntimeException e) {
        if (triesLeft == 0) {
          throw e;
        }
        LOGGER.warn("Error executing request. Retries left:{}. Request: {}", triesLeft,
                parameters.toUrl(baseUrl));
        waitForNextRetry();
      }
    }
    throw new IllegalStateException("Shouldn't be here.");
  }

  private InputStream executeOnce(Parameters parameters) throws HttpException {

    // Set up the request and the response.
    final HttpGet request = new HttpGet(parameters.toUrl(baseUrl));
    final CloseableHttpResponse response;
    try {
      response = httpClient.execute(request);
    } catch (RuntimeException | IOException e) {
      request.releaseConnection();
      throw new HttpException(e);
    }

    // If we don't succeed, we clean up and throw an exception.
    if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
      closeSilently(request, response);
      throw new HttpException("Error querying service. Returned HTTP Status Code: "
              + response.getStatusLine().getStatusCode());
    }

    // So we have success. We return the content.
    try {
      return new HttpOaiClientInputStream(response.getEntity().getContent(), request, response);
    } catch (RuntimeException | IOException e) {
      closeSilently(request, response);
      throw new HttpException(e);
    }
  }

  private void waitForNextRetry() {
    try {
      Thread.sleep(timeBetweenRetries);
    } catch (InterruptedException ie) {
      Thread.currentThread().interrupt();
      LOGGER.error(ie.getMessage());
    }
  }

  private static void closeSilently(HttpGet request, CloseableHttpResponse response) {
    List.<Closeable>of(response, request::releaseConnection).forEach(closeable -> {
      try {
        closeable.close();
      } catch (RuntimeException | IOException e) {
        LOGGER.warn("Error while cleaning resources.", e);
      }
    });
  }

  /**
   * An implementation of an input stream that wraps around a source input stream but allows
   * additional closing functionality.
   */
  private static class HttpOaiClientInputStream extends InputStream {

    private final InputStream source;
    private final HttpGet request;
    private final CloseableHttpResponse response;

    /**
     * Constructor.
     *
     * @param source The source input stream.
     * @param request The request - to be closed when the stream is closed.
     * @param response The response - to be closed when the stream is closed.
     */
    public HttpOaiClientInputStream(InputStream source, HttpGet request,
            CloseableHttpResponse response) {
      this.source = source;
      this.request = request;
      this.response = response;
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
    public int available() throws IOException {
      return source.available();
    }

    @Override
    public void close() throws IOException {
      try {
        source.close();
      } finally {
        closeSilently(request, response);
      }
    }
  }

  @Override
  public void close() {
    try {
      httpClient.close();
    } catch (IOException e) {
      LOGGER.warn("Error while closing client.", e);
    }
  }
}
