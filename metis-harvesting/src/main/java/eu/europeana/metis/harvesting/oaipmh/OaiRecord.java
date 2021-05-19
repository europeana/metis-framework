package eu.europeana.metis.harvesting.oaipmh;

import eu.europeana.metis.harvesting.HarvesterException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.function.Supplier;

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
   * @param recordSupplier A supplier for the record (as byte array).
   */
  public OaiRecord(OaiRecordHeader header, Supplier<byte[]> recordSupplier) {
    this.header = header;
    this.record = this.header.isDeleted() ? new byte[0] : recordSupplier.get();
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
