package eu.europeana.metis.core.workflow.plugins;

/**
 * Enum for depublication reason.
 */
public enum DepublicationReason {

  BROKEN_MEDIA_LINKS("Broken media links"),
  GDPR("GDPR"),
  LACK_OF_PERMISSIONS("Lack of permissions"),
  SENSITIVE_CONTENT("Sensitive content"),
  REMOVED_DATA_AT_SOURCE("Removed data at source"),
  GENERIC("Generic"),
  UNKNOWN("Unknown");

  private final String valueAsString;

  DepublicationReason(String valueAsString) {
    this.valueAsString = valueAsString;
  }

  @Override
  public String toString(){
    return valueAsString;
  }
}
