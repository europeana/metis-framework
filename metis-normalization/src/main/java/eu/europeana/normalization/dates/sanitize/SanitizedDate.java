package eu.europeana.normalization.dates.sanitize;

/**
 * Class containing the sanitize operation that was used to sanitize a value and the sanitized value itself.
 */
public class SanitizedDate {

  private final SanitizeOperation sanitizeOperation;
  private final String sanitizedStringDate;

  /**
   * All arguments constructor.
   *
   * @param sanitizeOperation the sanitize operation
   * @param sanitizedStringDate the sanitized value
   */
  public SanitizedDate(SanitizeOperation sanitizeOperation, String sanitizedStringDate) {
    this.sanitizeOperation = sanitizeOperation;
    this.sanitizedStringDate = sanitizedStringDate;
  }

  public SanitizeOperation getSanitizeOperation() {
    return sanitizeOperation;
  }

  public String getSanitizedDateString() {
    return sanitizedStringDate;
  }
}
