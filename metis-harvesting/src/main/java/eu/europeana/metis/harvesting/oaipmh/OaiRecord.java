package eu.europeana.metis.harvesting.oaipmh;

import eu.europeana.metis.harvesting.FullRecord;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.util.function.Supplier;

/**
 * This is a immutable class representing an (OAI-embedded) record along with it's OAI header. The
 * harvesting identifier is the OAI identifier.
 */
public class OaiRecord implements FullRecord {

  private final OaiRecordHeader header;
  private final byte[] content;

  /**
   * Constructor.
   *
   * @param header The OAI header of the record.
   * @param recordSupplier A supplier for the record (as byte array).
   */
  public OaiRecord(OaiRecordHeader header, Supplier<byte[]> recordSupplier) {
    this.header = header;
    this.content = this.header.isDeleted() ? new byte[0] : recordSupplier.get();
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
    outputStream.write(this.content);
  }

  @Override
  public ByteArrayInputStream getContent() {
    if (isDeleted()) {
      throw new IllegalStateException("Record is deleted at source.");
    }
    return new ByteArrayInputStream(content);
  }

  @Override
  public boolean isDeleted() {
    return getHeader().isDeleted();
  }

  @Override
  public Instant getTimeStamp() {
    return header.getDatestamp();
  }
}
