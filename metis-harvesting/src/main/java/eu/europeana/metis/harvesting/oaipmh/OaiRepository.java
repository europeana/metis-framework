package eu.europeana.metis.harvesting.oaipmh;

import java.io.Serializable;

public class OaiRepository implements Serializable {

  private static final long serialVersionUID = -7857963246782477550L;

  private final String repositoryUrl;
  private final String metadataPrefix;

  public OaiRepository(String repositoryUrl, String metadataPrefix) {
    this.repositoryUrl = repositoryUrl;
    this.metadataPrefix = metadataPrefix;
  }

  public String getRepositoryUrl() {
    return repositoryUrl;
  }

  public String getMetadataPrefix() {
    return metadataPrefix;
  }
}
