package eu.europeana.metis.harvesting.oaipmh;

import eu.europeana.metis.harvesting.FullRecord;
import eu.europeana.metis.harvesting.HarvesterException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.util.function.Supplier;

/**
 * This is a immutable class representing an (OAI-embedded) record along with it's OAI header. The
 * harvesting identifier is the OAI identifier.
 */
public class OaiRecord implements FullRecord {

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

  @Override
  public String getHarvestingIdentifier() {
    return getHeader().getOaiIdentifier();
  }

  @Override
  public void writeContent(OutputStream outputStream) throws IOException {
    if (isDeleted()) {
      throw new IllegalStateException("Record is deleted at source.");
    }
    outputStream.write(this.record);
  }

  @Override
  public ByteArrayInputStream getContent() {
    if (isDeleted()) {
      throw new IllegalStateException("Record is deleted at source.");
    }
    return new ByteArrayInputStream(record);
  }

  @Override
  public boolean isDeleted() {
    return getHeader().isDeleted();
  }

  /**
   * @deprecated use {@link #getContent()} instead.
   */
  @Deprecated
  public InputStream getRecord() throws HarvesterException {
    return getContent();
  }

  @Override
  public Instant getTimeStamp() {
    return header.getDatestamp();
  }
}
