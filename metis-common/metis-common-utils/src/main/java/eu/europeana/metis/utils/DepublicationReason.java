package eu.europeana.metis.utils;

/**
 * Enum for depublication reason.
 */
public enum DepublicationReason {

  BROKEN_MEDIA_LINKS("Broken media links", ""),
  GDPR("GDPR", ""),
  PERMISSION_ISSUES("Permission issues", "http://data.europeana.eu/vocabulary/depublicationReason/noPermission"),
  SENSITIVE_CONTENT("Sensitive content", ""),
  REMOVED_DATA_AT_SOURCE("Removed data at source", ""),
  GENERIC("Generic", ""),
  UNKNOWN("Unknown", "");

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
