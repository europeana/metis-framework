package eu.europeana.metis.harvesting.oaipmh;

import eu.europeana.metis.harvesting.HarvesterException;
import eu.europeana.metis.harvesting.HarvestingIterator;

/**
 * Implementations of this interface provide OAI-PMH harvesting access.
 */
public interface OaiHarvester {

  /**
   * Harvest the record headers.
   *
   * @param harvest The harvest request to execute.
   * @return An iterator providing access to the headers. The caller needs to close it after use.
   */
  HarvestingIterator<OaiRecordHeader, OaiRecordHeader> harvestRecordHeaders(OaiHarvest harvest);

  /**
   * Harvest the full records.
   *
   * @param harvest The harvest request to execute.
   * @return An iterator providing access to the headers. The caller needs to close it after use.
   */
  HarvestingIterator<OaiRecord, OaiRecordHeader> harvestRecords(OaiHarvest harvest);

  /**
   * Harvest an individual record.
   *
   * @param repository The repository from where to harvest.
   * @param oaiIdentifier The OAI-PMH identifier of the record to harvest.
   * @return The record.
   * @throws HarvesterException In case something went wrong.
   */
  OaiRecord harvestRecord(OaiRepository repository, String oaiIdentifier)
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
