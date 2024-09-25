package eu.europeana.metis.utils;

/**
 * Enum for depublication reason.
 */
public enum DepublicationReason {

  BROKEN_MEDIA_LINKS("Broken media links", "http://data.europeana.eu/vocabulary/depublicationReason/contentTier0"),
  GDPR("GDPR", "http://data.europeana.eu/vocabulary/depublicationReason/gdpr"),
  PERMISSION_ISSUES("Permission issues", "http://data.europeana.eu/vocabulary/depublicationReason/noPermission"),
  SENSITIVE_CONTENT("Sensitive content", "http://data.europeana.eu/vocabulary/depublicationReason/sensitiveContent"),
  REMOVED_DATA_AT_SOURCE("Removed data at source", "http://data.europeana.eu/vocabulary/depublicationReason/sourceRemoval"),
  GENERIC("Generic", "http://data.europeana.eu/vocabulary/depublicationReason/generic"),
  UNKNOWN("Unknown", "http://data.europeana.eu/vocabulary/depublicationReason/unknown");

  private final String valueAsString;
  private final String url;

  DepublicationReason(String valueAsString, String url) {
    this.valueAsString = valueAsString;
    this.url = url;
  }

  @Override
  public String toString(){
    return valueAsString;
  }

  public String getUrl() {
    return url;
  }
}
