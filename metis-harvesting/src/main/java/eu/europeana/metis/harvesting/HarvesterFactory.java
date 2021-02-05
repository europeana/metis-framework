package eu.europeana.metis.harvesting;

import eu.europeana.metis.harvesting.oaipmh.OaiHarvester;
import eu.europeana.metis.harvesting.oaipmh.OaiHarvesterImpl;

public final class HarvesterFactory {

  private HarvesterFactory() {
    // Class not meant to be instantiated.
  }

  public static OaiHarvester createHarvester() {
    return new OaiHarvesterImpl();
  }
}
