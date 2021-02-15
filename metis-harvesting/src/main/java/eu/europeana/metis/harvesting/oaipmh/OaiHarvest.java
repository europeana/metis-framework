package eu.europeana.metis.harvesting.oaipmh;

import java.time.Instant;

/**
 * Immutable object representing a OAI-PMH harvest request.
 */
public class OaiHarvest extends OaiRepository {

  private static final long serialVersionUID = -6300664626628834436L;

  private final String setSpec;
  private final Instant from;
  private final Instant until;

  /**
   * Constructor.
   *
   * @param repositoryUrl The base url of the repository.
   * @param metadataPrefix The metadata prefix (optional).
   * @param setSpec The set spec value.
   * @param from The from value (optional).
   * @param until The until value (optional).
   */
  public OaiHarvest(String repositoryUrl, String metadataPrefix, String setSpec,
          Instant from, Instant until) {
    super(repositoryUrl, metadataPrefix);
    this.setSpec = setSpec;
    this.from = from;
    this.until = until;
  }

  /**
   * Constructor.
   *
   * @param repositoryUrl The base url of the repository.
   * @param metadataPrefix The metadata prefix (optional).
   * @param setSpec The set spec value.
   */
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

  /**
   * Builder class for instances of {@link OaiHarvest}.
   */
  public static class Builder {

    private String repositoryUrl;
    private String metadataPrefix;
    private String setSpec;
    private Instant from;
    private Instant until;

    public Builder setRepositoryUrl(String repositoryUrl) {
      this.repositoryUrl = repositoryUrl;
      return this;
    }

    public Builder setMetadataPrefix(String metadataPrefix) {
      this.metadataPrefix = metadataPrefix;
      return this;
    }

    public Builder setSetSpec(String setSpec) {
      this.setSpec = setSpec;
      return this;
    }

    public Builder setFrom(Instant from) {
      this.from = from;
      return this;
    }

    public Builder setUntil(Instant until) {
      this.until = until;
      return this;
    }

    /**
     * Build.
     *
     * @return The instance.
     */
    public OaiHarvest createOaiHarvest() {
      return new OaiHarvest(repositoryUrl, metadataPrefix, setSpec, from, until);
    }
  }
}
