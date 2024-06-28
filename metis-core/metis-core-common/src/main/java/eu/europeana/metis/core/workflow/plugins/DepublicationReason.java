package eu.europeana.metis.core.workflow.plugins;

public enum DepublicationReason {

  BROKEN_MEDIA_LINKS("Broken media links"),
  GDPR("GDPR"),
  LACK_OF_PERMISSIONS("Lack of permissions"),
  SENSITIVE_CONTENT("Sensitive content"),
  REMOVED_DATA_ST_SOURCE("Removed data source"),
  GENERIC("Generic"),
  UNKNOWN("Unknown");

  private String valueAsString;

  DepublicationReason(String valueAsString) {
    this.valueAsString = valueAsString;
  }
}
