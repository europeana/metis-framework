package eu.europeana.enrichment.api.config;

import eu.europeana.entity.client.config.EntityClientConfiguration;
import java.io.Serial;
import java.util.Properties;

/**
 * The type Entity client extended configuration.
 */
public class EntityClientExtendedConfiguration extends EntityClientConfiguration {

  @Serial
  private static final long serialVersionUID = 7251498348801958021L;
  private static final int MAX_CONNECTIONS_TOTAL = 200;
  private static final int MAX_CONNECTIONS_PER_ROUTE = 50;
  private static final int VALIDATE_AFTER_INACTIVITY = 5;
  private static final int TIME_TO_LIVE = 5;
  private static final int SOCKET_TIMEOUT = 30;
  private static final int REQUEST_CONNECTION_TIMEOUT = 30;
  private static final int RESPONSE_CONNECTION_TIMEOUT = 30;

  /**
   * Instantiates a new Entity client extended configuration.
   *
   * @param properties the properties
   */
  public EntityClientExtendedConfiguration(Properties properties) {
    super(properties);
  }

  /**
   * Gets max connections total.
   *
   * @return the max connections total
   */
  public int getMaxConnectionsTotal() {
    return Integer.parseInt(this.getProperty("maxConnectionsTotal", String.valueOf(MAX_CONNECTIONS_TOTAL)));
  }

  /**
   * Sets max connections total.
   *
   * @param maxConnectionsTotal the max connections total
   */
  public void setMaxConnectionsTotal(int maxConnectionsTotal) {
    this.put("maxConnectionsTotal", maxConnectionsTotal);
  }

  /**
   * Gets max connections per route.
   *
   * @return the max connections per route
   */
  public int getMaxConnectionsPerRoute() {
    return Integer.parseInt(this.getProperty("maxConnectionsPerRoute", String.valueOf(MAX_CONNECTIONS_PER_ROUTE)));
  }

  /**
   * Sets max connections per route.
   *
   * @param maxConnectionsPerRoute the max connections per route
   */
  public void setMaxConnectionsPerRoute(int maxConnectionsPerRoute) {
    this.put("maxConnectionsPerRoute", maxConnectionsPerRoute);
  }

  /**
   * Gets seconds validate after inactivity.
   *
   * @return the seconds validate after inactivity
   */
  public int getSecondsValidateAfterInactivity() {
    return Integer.parseInt(this.getProperty("secondsValidateAfterInactivity", String.valueOf(VALIDATE_AFTER_INACTIVITY)));
  }

  /**
   * Sets seconds validate after inactivity.
   *
   * @param secondsValidateAfterInactivity the seconds validate after inactivity
   */
  public void setSecondsValidateAfterInactivity(int secondsValidateAfterInactivity) {
    this.put("secondsValidateAfterInactivity", secondsValidateAfterInactivity);
  }

  /**
   * Gets time to live seconds.
   *
   * @return the time to live seconds
   */
  public int getTimeToLiveSeconds() {
    return Integer.parseInt(this.getProperty("timeToLiveSeconds", String.valueOf(TIME_TO_LIVE)));
  }

  /**
   * Sets time to live seconds.
   *
   * @param timeToLiveSeconds the time to live seconds
   */
  public void setTimeToLiveSeconds(int timeToLiveSeconds) {
    this.put("timeToLiveSeconds", timeToLiveSeconds);
  }

  /**
   * Gets reactor socket timeout seconds.
   *
   * @return the reactor socket timeout seconds
   */
  public int getReactorSocketTimeoutSeconds() {
    return Integer.parseInt(this.getProperty("reactorSocketTimeout", String.valueOf(SOCKET_TIMEOUT)));
  }

  /**
   * Sets reactor socket timeout seconds.
   *
   * @param reactorSocketTimeout the reactor socket timeout
   */
  public void setReactorSocketTimeoutSeconds(int reactorSocketTimeout) {
    this.put("reactorSocketTimeout", reactorSocketTimeout);
  }

  /**
   * Gets request connection timeout seconds.
   *
   * @return the request connection timeout seconds
   */
  public int getRequestConnectionTimeoutSeconds() {
    return Integer.parseInt(this.getProperty("requestConnectionTimeout", String.valueOf(REQUEST_CONNECTION_TIMEOUT)));
  }

  /**
   * Sets request connection timeout seconds.
   *
   * @param requestConnectionTimeout the request connection timeout
   */
  public void setRequestConnectionTimeoutSeconds(int requestConnectionTimeout) {
    this.put("requestConnectionTimeout", requestConnectionTimeout);
  }

  /**
   * Gets response connection timeout seconds.
   *
   * @return the response connection timeout seconds
   */
  public int getResponseConnectionTimeoutSeconds() {
    return Integer.parseInt(this.getProperty("responseConnectionTimeout", String.valueOf(RESPONSE_CONNECTION_TIMEOUT)));
  }

  /**
   * Sets response connection timeout seconds.
   *
   * @param responseConnectionTimeout the response connection timeout
   */
  public void setResponseConnectionTimeoutSeconds(int responseConnectionTimeout) {
    this.put("responseConnectionTimeout", responseConnectionTimeout);
  }
}
