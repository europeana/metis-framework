package eu.europeana.metis.harvesting.oaipmh;

import java.io.Serializable;
import java.time.Instant;

public class OaiRecordHeader implements Serializable {

  private static final long serialVersionUID = -2613166675241431794L;

  private final String oaiIdentifier;
  private final boolean isDeleted;
  private final Instant datestamp;

  public OaiRecordHeader(String oaiIdentifier, boolean isDeleted, Instant datestamp) {
    this.oaiIdentifier = oaiIdentifier;
    this.isDeleted = isDeleted;
    this.datestamp = datestamp;
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
