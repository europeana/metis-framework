package eu.europeana.metis.core.workflow.plugins;

public enum DepublicationReason {

  BROKEN_MEDIA_LINKS("Broken media links"),
  GDPR("GDPR"),
  PERMISSION_ISSUES("Permission issues"),
  SENSITIVE_CONTENT("Sensitive content"),
  REMOVED_DATA_AT_SOURCE("Removed data at source"),
  GENERIC("Generic"),
  UNKNOWN("Unknown");

  private final String valueAsString;

  DepublicationReason(String valueAsString) {
    this.valueAsString = valueAsString;
  }
  
  public String toString(){
    return valueAsString;
  }
}
