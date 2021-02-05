package eu.europeana.metis.harvesting.oaipmh;

import java.time.Instant;

public class OaiHarvest extends OaiRepository {

  private static final long serialVersionUID = -6300664626628834436L;

  private final String setSpec;
  private final Instant from;
  private final Instant until;

  public OaiHarvest(String repositoryUrl, String metadataPrefix, String setSpec,
          Instant from, Instant until) {
    super(repositoryUrl, metadataPrefix);
    this.setSpec = setSpec;
    this.from = from;
    this.until = until;
  }

  public OaiHarvest(String repositoryUrl, String metadataPrefix, String setSpec) {
    this(repositoryUrl, metadataPrefix, setSpec, null, null);
  }

  public String getSetSpec() {
    return setSpec;
  }

  public Instant getFrom() {
    return from;
  }

  public Instant getUntil() {
    return until;
  }

  @Override
  public String toString() {
    return "OaiHarvest{" +
            "setSpec='" + setSpec + '\'' +
            ", from=" + from +
            ", until=" + until +
            "} " + super.toString();
  }
}
