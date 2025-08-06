package eu.europeana.enrichment.api.config;

import eu.europeana.entity.client.config.EntityClientConfiguration;
import java.util.Properties;

/**
 * The type Entity client extended configuration.
 */
public class EntityClientExtendedConfiguration extends EntityClientConfiguration {

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
    return (int) super.get("maxConnectionsTotal");
  }

  /**
   * Sets max connections total.
   *
   * @param maxConnectionsTotal the max connections total
   */
  public void setMaxConnectionsTotal(int maxConnectionsTotal) {
    super.put("maxConnectionsTotal", maxConnectionsTotal);
  }

  /**
   * Gets max connections per route.
   *
   * @return the max connections per route
   */
  public int getMaxConnectionsPerRoute() {
    return (int) super.get("maxConnectionsPerRoute");
  }

  /**
   * Sets max connections per route.
   *
   * @param maxConnectionsPerRoute the max connections per route
   */
  public void setMaxConnectionsPerRoute(int maxConnectionsPerRoute) {
    super.put("maxConnectionsPerRoute", maxConnectionsPerRoute);
  }

  /**
   * Gets seconds validate after inactivity.
   *
   * @return the seconds validate after inactivity
   */
  public int getSecondsValidateAfterInactivity() {
    return (int) super.get("secondsValidateAfterInactivity");
  }

  /**
   * Sets seconds validate after inactivity.
   *
   * @param secondsValidateAfterInactivity the seconds validate after inactivity
   */
  public void setSecondsValidateAfterInactivity(int secondsValidateAfterInactivity) {
    super.put("secondsValidateAfterInactivity", secondsValidateAfterInactivity);
  }

  /**
   * Gets time to live seconds.
   *
   * @return the time to live seconds
   */
  public int getTimeToLiveSeconds() {
    return (int) super.get("timeToLiveSeconds");
  }

  /**
   * Sets time to live seconds.
   *
   * @param timeToLiveSeconds the time to live seconds
   */
  public void setTimeToLiveSeconds(int timeToLiveSeconds) {
    super.put("timeToLiveSeconds", timeToLiveSeconds);
  }

  /**
   * Gets reactor socket timeout seconds.
   *
   * @return the reactor socket timeout seconds
   */
  public int getReactorSocketTimeoutSeconds() {
    return (int) super.get("reactorSocketTimeout");
  }

  /**
   * Sets reactor socket timeout seconds.
   *
   * @param reactorSocketTimeout the reactor socket timeout
   */
  public void setReactorSocketTimeoutSeconds(int reactorSocketTimeout) {
    super.put("reactorSocketTimeout", reactorSocketTimeout);
  }

  /**
   * Gets request connection timeout seconds.
   *
   * @return the request connection timeout seconds
   */
  public int getRequestConnectionTimeoutSeconds() {
    return (int) super.get("requestConnectionTimeout");
  }

  /**
   * Sets request connection timeout seconds.
   *
   * @param requestConnectionTimeout the request connection timeout
   */
  public void setRequestConnectionTimeoutSeconds(int requestConnectionTimeout) {
    super.put("requestConnectionTimeout", requestConnectionTimeout);
  }

  /**
   * Gets response connection timeout seconds.
   *
   * @return the response connection timeout seconds
   */
  public int getResponseConnectionTimeoutSeconds() {
    return (int) super.get("responseConnectionTimeout");
  }

  /**
   * Sets response connection timeout seconds.
   *
   * @param responseConnectionTimeout the response connection timeout
   */
  public void setResponseConnectionTimeoutSeconds(int responseConnectionTimeout) {
    super.put("responseConnectionTimeout", responseConnectionTimeout);
  }
}
