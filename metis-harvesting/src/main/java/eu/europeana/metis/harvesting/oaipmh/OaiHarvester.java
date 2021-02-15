package eu.europeana.metis.harvesting.oaipmh;

import eu.europeana.metis.harvesting.HarvesterException;
import java.io.InputStream;

/**
 * Implementations of this interface provide OAI-PMH harvesting access.
 */
public interface OaiHarvester {

  /**
   * Harvest the record headers.
   *
   * @param harvest The harvest request to execute.
   * @return An iterator providing access to the headers. The caller needs to close it after use.
   * @throws HarvesterException In case something went wrong.
   */
  OaiRecordHeaderIterator harvestRecordHeaders(OaiHarvest harvest) throws HarvesterException;

  /**
   * Harvest an individual record.
   *
   * @param repository The repository from where to harvest.
   * @param oaiIdentifier The OAI-PMH identifier of the record to harvest.
   * @return An input stream containing the record. The caller needs to close it after use.
   * @throws HarvesterException In case something went wrong.
   */
  InputStream harvestRecord(OaiRepository repository, String oaiIdentifier)
          throws HarvesterException;

  /**
   * Count the number of records (by contacting the OAI-PMH repository).
   *
   * @param harvest The harvest request for which to count the records.
   * @return The number of records. Or null if the number could not be determined.
   * @throws HarvesterException In case something went wrong.
   */
  Integer countRecords(OaiHarvest harvest) throws HarvesterException;

}
