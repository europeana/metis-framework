package eu.europeana.metis.harvesting.oaipmh;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;
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
 * @see org.dspace.xoai.serviceprovider.client.HttpOAIClient
 */
public class CloseableHttpOaiClient implements CloseableOaiClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(CloseableHttpOaiClient.class);

  private static final int DEFAULT_REQUEST_TIMEOUT = 60_000 /* = 1min */;
  private static final int DEFAULT_CONNECTION_TIMEOUT = 30_000 /* = 30sec */;
  private static final int DEFAULT_SOCKET_TIMEOUT = 300_000 /* = 5min */;
  private static final int DEFAULT_NUMBER_OF_RETRIES = 3;
  private static final int DEFAULT_TIME_BETWEEN_RETRIES = 5_000 /* = 5sec */;

  private final String baseUrl;
  private final CloseableHttpClient httpClient;
  private final int numberOfRetries;
  private final int timeBetweenRetries;

  public CloseableHttpOaiClient(String baseUrl) {
    this(baseUrl, null);
  }

  public CloseableHttpOaiClient(String baseUrl, String userAgent) {
    this(baseUrl, userAgent, DEFAULT_NUMBER_OF_RETRIES, DEFAULT_TIME_BETWEEN_RETRIES);
  }

  public CloseableHttpOaiClient(String baseUrl, String userAgent, int numberOfRetries,
          int timeBetweenRetries) {
    this(baseUrl, userAgent, numberOfRetries, timeBetweenRetries, DEFAULT_REQUEST_TIMEOUT,
            DEFAULT_CONNECTION_TIMEOUT, DEFAULT_SOCKET_TIMEOUT);
  }

  public CloseableHttpOaiClient(String baseUrl, String userAgent, int numberOfRetries,
          int timeBetweenRetries, int requestTimeout, int connectionTimeout, int socketTimeout) {
    this.baseUrl = baseUrl;
    this.numberOfRetries = numberOfRetries;
    this.timeBetweenRetries = timeBetweenRetries;
    final RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(connectionTimeout)
            .setSocketTimeout(socketTimeout)
            .setConnectionRequestTimeout(requestTimeout)
            .build();
    this.httpClient = HttpClientBuilder.create()
            .setDefaultRequestConfig(requestConfig)
            .setUserAgent(userAgent)
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

    // So we have a request and a response. Set up cleaning for both.
    final Runnable closeAction = () -> Stream.<Closeable>of(response, request::releaseConnection)
            .forEach(closeable -> {
      try {
        closeable.close();
      } catch (RuntimeException | IOException e) {
        LOGGER.warn("Error while cleaning resources.", e);
      }
    });

    // If we don't succeed, we clean up and throw an exception.
    if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
      closeAction.run();
      throw new HttpException(
              "Error querying service. Returned HTTP Status Code: "
                      + response.getStatusLine().getStatusCode());
    }

    // So we have success. We return the content.
    try {
      return new HttpOaiClientInputStream(response.getEntity().getContent(), closeAction);
    } catch (RuntimeException | IOException e) {
      closeAction.run();
      throw new HttpException(e);
    }
  }

  protected void waitForNextRetry() {
    try {
      Thread.sleep(timeBetweenRetries);
    } catch (InterruptedException ie) {
      Thread.currentThread().interrupt();
      LOGGER.error(ie.getMessage());
    }
  }

  private static class HttpOaiClientInputStream extends InputStream {

    final InputStream source;
    final Runnable afterClosing;

    public HttpOaiClientInputStream(InputStream source, Runnable afterClosing) {
      this.source = source;
      this.afterClosing = afterClosing;
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
        afterClosing.run();
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
