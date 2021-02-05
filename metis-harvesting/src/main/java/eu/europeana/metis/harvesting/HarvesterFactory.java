package eu.europeana.metis.harvesting;

import eu.europeana.metis.harvesting.oaipmh.CloseableHttpOaiClient;
import eu.europeana.metis.harvesting.oaipmh.OaiHarvester;
import eu.europeana.metis.harvesting.oaipmh.OaiHarvesterImpl;

public final class HarvesterFactory {

  private HarvesterFactory() {
    // Class not meant to be instantiated.
  }

  public static OaiHarvester createOaiHarvester() {
    return new OaiHarvesterImpl(CloseableHttpOaiClient::new);
  }

  public static OaiHarvester createOaiHarvester(String userAgent, int numberOfRetries,
          int timeBetweenRetries) {
    return new OaiHarvesterImpl(baseUrl ->
            new CloseableHttpOaiClient(baseUrl, userAgent, numberOfRetries, timeBetweenRetries));
  }

  public static OaiHarvester createOaiHarvester(String userAgent, int numberOfRetries,
          int timeBetweenRetries, int requestTimeout, int connectionTimeout, int socketTimeout) {
    return new OaiHarvesterImpl(baseUrl -> new CloseableHttpOaiClient(baseUrl, userAgent,
            numberOfRetries, timeBetweenRetries, requestTimeout, connectionTimeout, socketTimeout));
  }
}
