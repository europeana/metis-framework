package eu.europeana.enrichment.api.internal;

import org.mongojack.Id;

public class MongoCodeLookup {

  @Id
  private String id;

  private String codeUri;

  private String originalCodeUri;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getCodeUri() {
    return codeUri;
  }

  public void setCodeUri(String codeUri) {
    this.codeUri = codeUri;
  }

  public String getOriginalCodeUri() {
    return originalCodeUri;
  }

  public void setOriginalCodeUri(String originalCodeUri) {
    this.originalCodeUri = originalCodeUri;
  }
}
