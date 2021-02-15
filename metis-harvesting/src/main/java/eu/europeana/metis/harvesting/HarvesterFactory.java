package eu.europeana.metis.harvesting;

import eu.europeana.metis.harvesting.http.HttpHarvester;
import eu.europeana.metis.harvesting.http.HttpHarvesterImpl;
import eu.europeana.metis.harvesting.oaipmh.CloseableHttpOaiClient;
import eu.europeana.metis.harvesting.oaipmh.OaiHarvester;
import eu.europeana.metis.harvesting.oaipmh.OaiHarvesterImpl;

/**
 * This is a factory class for all harvester objects that are provided by this module.
 */
public final class HarvesterFactory {

  private HarvesterFactory() {
    // Class not meant to be instantiated.
  }

  /**
   * Create harvester for HTTP (compressed archive) harvesting.
   *
   * @return A new instance of an HTTP harvester.
   */
  public static HttpHarvester createHttpHarvester() {
    return new HttpHarvesterImpl();
  }

  /**
   * Create a harvester for OAI harvesting.
   *
   * @return A new instance of an OAI harvester.
   */
  public static OaiHarvester createOaiHarvester() {
    return createOaiHarvester(new HarvestingClientSettings());
  }

  /**
   * Create a harvester for OAI harvesting.
   *
   * @param userAgent The user agent we wish to set for connections.
   * @param numberOfRetries The number of retries that we apply to connections.
   * @param timeBetweenRetries The time (in ms) between any connection retry.
   * @return A new instance of an OAI harvester.
   */
  public static OaiHarvester createOaiHarvester(String userAgent, int numberOfRetries,
          int timeBetweenRetries) {
    return createOaiHarvester(new HarvestingClientSettings().setUserAgent(userAgent)
            .setNumberOfRetries(numberOfRetries).setTimeBetweenRetries(timeBetweenRetries));
  }

  /**
   * Create a harvester for OAI harvesting.
   *
   * @param settings The client settings to apply.
   * @return A new instance of an OAI harvester.
   */
  public static OaiHarvester createOaiHarvester(HarvestingClientSettings settings) {
    return new OaiHarvesterImpl(baseUrl -> new CloseableHttpOaiClient(baseUrl, settings));
  }
}
