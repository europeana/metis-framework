package eu.europeana.normalization.dates.extraction;

/**
 * Basic default enum for date separators
 */
public enum DefaultDatesSeparator implements DatesSeparator {
  DASH_DELIMITER("-"),
  SLASH_DELIMITER("/");

  private final String stringRepresentation;

  DefaultDatesSeparator(String stringRepresentation) {
    this.stringRepresentation = stringRepresentation;
  }

  @Override
  public String getStringRepresentation() {
    return stringRepresentation;
  }
}
