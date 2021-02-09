package eu.europeana.metis.harvesting.http;

import eu.europeana.metis.harvesting.HarvesterException;

public interface HttpHarvester {

  HttpRecordIterator harvestRecords(String archiveUrl, String downloadDirectory)
          throws HarvesterException;

}
