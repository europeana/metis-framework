package eu.europeana.metis.harvesting.oaipmh;

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import org.dspace.xoai.model.oaipmh.Header;

/**
 * This is a immutable class representing an OAI-PMH record header.
 */
public class OaiRecordHeader implements Serializable {

  private static final long serialVersionUID = -2613166675241431794L;

  private final String oaiIdentifier;
  private final boolean isDeleted;
  private final Instant datestamp;

  /**
   * Constructor.
   * @param oaiIdentifier The OAI-PMH identifier of the record.
   * @param isDeleted Whether the record is marked as deleted.
   * @param datestamp The datestamp of the record.
   */
  public OaiRecordHeader(String oaiIdentifier, boolean isDeleted, Instant datestamp) {
    this.oaiIdentifier = oaiIdentifier;
    this.isDeleted = isDeleted;
    this.datestamp = datestamp;
  }

  /**
   * Convert from a library object.
   *
   * @param header The library object from which to convert.
   * @return An instance.
   */
  static OaiRecordHeader convert(Header header) {
    return new OaiRecordHeader(header.getIdentifier(), header.isDeleted(),
            Optional.ofNullable(header.getDatestamp()).map(Date::toInstant).orElse(null));
  }

  public String getOaiIdentifier() {
    return oaiIdentifier;
  }

  public boolean isDeleted() {
    return isDeleted;
  }

  public Instant getDatestamp() {
    return datestamp;
  }
}
