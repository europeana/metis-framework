package eu.europeana.metis.harvesting;

import java.io.InputStream;

/**
 * This interface represents a harvested record.
 */
public interface FullRecord {

  /**
   * Makes the record's content available.
   *
   * @return An input stream containing the record. The caller needs to close it after use.
   * @throws HarvesterException In case the record is not (no longer) available.
   */
  InputStream getContent() throws HarvesterException;

  /**
   * @return The harvesting identifier of the entry. This should be unique. This may be different
   * from the record ID (rdf:about) and may exist only in the context of this harvest.
   */
  String getHarvestingIdentifier();
}
