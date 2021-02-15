package eu.europeana.metis.harvesting;

/**
 * Class representing settings for network clients.
 */
public class HarvestingClientSettings {

  private static final int DEFAULT_REQUEST_TIMEOUT = 60_000 /* = 1min */;
  private static final int DEFAULT_CONNECTION_TIMEOUT = 30_000 /* = 30sec */;
  private static final int DEFAULT_SOCKET_TIMEOUT = 300_000 /* = 5min */;
  private static final int DEFAULT_NUMBER_OF_RETRIES = 3;
  private static final int DEFAULT_TIME_BETWEEN_RETRIES = 5_000 /* = 5sec */;
  private static final String DEFAULT_USER_AGENT = null;

  private String userAgent = DEFAULT_USER_AGENT;
  private int numberOfRetries = DEFAULT_NUMBER_OF_RETRIES;
  private int timeBetweenRetries = DEFAULT_TIME_BETWEEN_RETRIES;
  private int requestTimeout = DEFAULT_REQUEST_TIMEOUT;
  private int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
  private int socketTimeout = DEFAULT_SOCKET_TIMEOUT;

  public String getUserAgent() {
    return userAgent;
  }

  public int getNumberOfRetries() {
    return numberOfRetries;
  }

  public int getTimeBetweenRetries() {
    return timeBetweenRetries;
  }

  public int getRequestTimeout() {
    return requestTimeout;
  }

  public int getConnectionTimeout() {
    return connectionTimeout;
  }

  public int getSocketTimeout() {
    return socketTimeout;
  }

  /**
   * Set the user agent. The default value (if this method is not called) is {@link
   * #DEFAULT_USER_AGENT}
   *
   * @param userAgent The user agent for connections. Can be null (in which a generated value is
   * used).
   * @return This instance.
   */
  public HarvestingClientSettings setUserAgent(String userAgent) {
    this.userAgent = userAgent;
    return this;
  }

  /**
   * Set the number of retries. The default value (if this method is not called) is {@link
   * #DEFAULT_NUMBER_OF_RETRIES}
   *
   * @param numberOfRetries The number of retries that we apply to connections.
   * @return This instance.
   */
  public HarvestingClientSettings setNumberOfRetries(int numberOfRetries) {
    this.numberOfRetries = numberOfRetries;
    return this;
  }

  /**
   * Set the time between retries. The default value (if this method is not called) is {@link
   * #DEFAULT_TIME_BETWEEN_RETRIES}
   *
   * @param timeBetweenRetries The time (in ms) between any connection retry.
   * @return This instance.
   */
  public HarvestingClientSettings setTimeBetweenRetries(int timeBetweenRetries) {
    this.timeBetweenRetries = timeBetweenRetries;
    return this;
  }

  /**
   * Set the request timeout. The default value (if this method is not called) is {@link
   * #DEFAULT_REQUEST_TIMEOUT}
   *
   * @param requestTimeout The request timeout (in ms) for connections.
   * @return This instance.
   */
  public HarvestingClientSettings setRequestTimeout(int requestTimeout) {
    this.requestTimeout = requestTimeout;
    return this;
  }

  /**
   * Set the connection timout. The default value (if this method is not called) is {@link
   * #DEFAULT_CONNECTION_TIMEOUT}
   *
   * @param connectionTimeout The connection timeout (in ms) for connections.
   * @return This instance.
   */
  public HarvestingClientSettings setConnectionTimeout(int connectionTimeout) {
    this.connectionTimeout = connectionTimeout;
    return this;
  }

  /**
   * Set the socket timeout. The default value (if this method is not called) is {@link
   * #DEFAULT_SOCKET_TIMEOUT}
   *
   * @param socketTimeout The socket timeout (in ms) for connections.
   * @return This instance.
   */
  public HarvestingClientSettings setSocketTimeout(int socketTimeout) {
    this.socketTimeout = socketTimeout;
    return this;
  }
}
