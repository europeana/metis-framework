package eu.europeana.metis.utils;

/**
 * Enum for depublication reason.
 * <p>Note: The enum value {@link #LEGACY} is to be used for historical depublication workflows(before the reason was
 * implemented). In other words the historical workflows will be populated by a script once with the {@link #LEGACY} reason, and
 * this value should never be used during depublication since its release. At the time of writing the url String is not meant to
 * be used for populating records in the database(e.g. tombstoning)</p>
 */
public enum DepublicationReason {

  BROKEN_MEDIA_LINKS("Broken media links", "contentTier0"),
  GDPR("GDPR", "gdpr"),
  PERMISSION_ISSUES("Permission issues", "noPermission"),
  SENSITIVE_CONTENT("Sensitive content", "sensitiveContent"),
  REMOVED_DATA_AT_SOURCE("Removed data at source", "sourceRemoval"),
  GENERIC("Generic", "generic"),
  LEGACY("Legacy", "legacy");

  private static final String BASE_URL = "http://data.europeana.eu/vocabulary/depublicationReason/";

  private final String title;
  private final String url;

  DepublicationReason(String title, String urlSuffix) {
    this.title = title;
    this.url = BASE_URL + urlSuffix;
  }

  @Override
  public String toString() {
    return title;
  }

  public String getTitle() {
    return title;
  }

  public String getUrl() {
    return url;
  }
}
