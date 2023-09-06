package eu.europeana.normalization.dates.extraction;

/**
 * Basic default enum for date separators
 */
public enum DefaultDatesSeparator implements DatesSeparator {
  DASH_DELIMITER("-"),
  SLASH_DELIMITER("/");

  private final String datesSeparator;

  DefaultDatesSeparator(String datesSeparator) {
    this.datesSeparator = datesSeparator;
  }

  @Override
  public String getDatesSeparator() {
    return datesSeparator;
  }
}
