package eu.europeana.metis.harvesting.oaipmh;

import eu.europeana.metis.harvesting.HarvesterException;
import java.io.InputStream;

public interface OaiHarvester {

  OaiRecordHeaderIterator harvestRecordHeaders(OaiHarvest harvest) throws HarvesterException;

  InputStream harvestRecord(OaiRepository repository, String oaiIdentifier)
          throws HarvesterException;

  Integer countRecords(OaiHarvest harvest) throws HarvesterException;

}
