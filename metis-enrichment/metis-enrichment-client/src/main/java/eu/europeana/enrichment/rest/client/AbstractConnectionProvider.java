package eu.europeana.enrichment.rest.client;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

public abstract class AbstractConnectionProvider {

  /**
   * The default value of the maximum amount of time, in milliseconds, we wait for a connection to a
   * resource before timing out. It's currently set to {@value AbstractConnectionProvider#DEFAULT_CONNECT_TIMEOUT}
   * milliseconds.
   */
  public static final int DEFAULT_CONNECT_TIMEOUT = 10_000;

  /**
   * The default value of the maximum amount of time, in milliseconds, we wait for the response
   * before timing out. It's currently set to {@value AbstractConnectionProvider#DEFAULT_RESPONSE_TIMEOUT}
   * milliseconds.
   */
  public static final int DEFAULT_RESPONSE_TIMEOUT = 60_000;

  /**
   * The default value of the batch size with which we query the enrichment service. It's currently
   * set to {@value AbstractConnectionProvider#DEFAULT_BATCH_SIZE_ENRICHMENT} values.
   */
  public static final int DEFAULT_BATCH_SIZE_ENRICHMENT = 20;

  private int connectTimeout = DEFAULT_CONNECT_TIMEOUT;
  private int responseTimeout = DEFAULT_RESPONSE_TIMEOUT;
  protected int batchSizeEnrichment = DEFAULT_BATCH_SIZE_ENRICHMENT;

  /**
   * Set the maximum amount of time, in milliseconds, we wait for a connection before timing out.
   * The default (when not calling this method) is {@value AbstractConnectionProvider#DEFAULT_CONNECT_TIMEOUT}
   * milliseconds.
   *
   * @param connectTimeout The maximum amount of time, in milliseconds, we wait for a connection
   * before timing out. If not positive, this signifies that the connection does not time out.
   * @return This instance, for convenience.
   */
  public void setConnectTimeout(int connectTimeout) {
    this.connectTimeout = connectTimeout;
  }

  /**
   * Set the maximum amount of time, in milliseconds, we wait for the response. The default (when
   * not calling this method is {@value AbstractConnectionProvider#DEFAULT_RESPONSE_TIMEOUT}
   * milliseconds.
   *
   * @param responseTimeout The maximum amount of time, in milliseconds, we wait for the response
   * before timing out. If not positive, this signifies that the response does not time out.
   * @return This instance, for convenience.
   */
  public void setResponseTimeout(int responseTimeout) {
    this.responseTimeout = responseTimeout;
  }

  /**
   * Set the batch size with which we query the enrichment service. The default (when not calling
   * this method) is {@value AbstractConnectionProvider#DEFAULT_BATCH_SIZE_ENRICHMENT} values.
   *
   * @param batchSizeEnrichment The batch size. Must be strictly positive.
   * @return This instance, for convenience.
   */
  public void setBatchSizeEnrichment(int batchSizeEnrichment) {
    if (batchSizeEnrichment < 1) {
      throw new IllegalArgumentException("Batch size cannot be 0 or negative.");
    }
    this.batchSizeEnrichment = batchSizeEnrichment;
  }

  /**
   * Creates a new Http connection factory using the values set up previously for the connection
   * timeout and response time out
   *
   * @return a HttpComponentsClientHttpRequestFactory instance with all the information set up
   * previously
   */
  protected HttpComponentsClientHttpRequestFactory createRequestFactory() {

    final HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
    requestFactory.setConnectTimeout(Math.max(connectTimeout, 0));
    requestFactory.setReadTimeout(Math.max(responseTimeout, 0));

    return requestFactory;
  }

}