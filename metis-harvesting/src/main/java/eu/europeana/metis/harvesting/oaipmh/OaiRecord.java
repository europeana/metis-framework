package eu.europeana.metis.harvesting.oaipmh;

import eu.europeana.metis.harvesting.HarvesterException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * This is a immutable class representing an (OAI-embedded) record along with it's OAI header.
 */
public class OaiRecord {

  private final OaiRecordHeader header;
  private final byte[] record;

  /**
   * Constructor.
   *
   * @param header The OAI header of the record.
   * @param record The record itself (the embedded record).
   */
  public OaiRecord(OaiRecordHeader header, byte[] record) {
    this.header = header;
    this.record = record;
  }

  public OaiRecordHeader getHeader() {
    return header;
  }

  /**
   * Makes the embedded record available.
   *
   * @return An input stream containing the record. The caller needs to close it after use.
   * @throws HarvesterException In case the record is marked as deleted.
   */
  public InputStream getRecord() throws HarvesterException {
    if (getHeader().isDeleted()) {
      throw new HarvesterException("The record is deleted.");
    }
    return new ByteArrayInputStream(record);
  }
}
