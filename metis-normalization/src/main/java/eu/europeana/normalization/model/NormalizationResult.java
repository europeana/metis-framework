package eu.europeana.normalization.model;

/**
 * The class containing the reporting on a normalization action.
 */
public final class NormalizationResult {

  private final NormalizationReport report;
  private final String normalizedRecordInEdmXml;
  private final String errorMessage;

  private NormalizationResult(String normalizedRecordInEdmXml, NormalizationReport report,
      String errorMessage) {
    this.normalizedRecordInEdmXml = normalizedRecordInEdmXml;
    this.report = report;
    this.errorMessage = errorMessage;
  }

  /**
   * Creates instance of this class in the case of an error.
   * 
   * @param errorMessage The error message (cannot be null).
   * @param edmRecord The original record.
   * @return An instance of this class.
   */
  public static NormalizationResult createInstanceForError(String errorMessage, String edmRecord) {
    if (edmRecord == null) {
      throw new IllegalArgumentException("The record should not be null.");
    }
    if (errorMessage == null) {
      throw new IllegalArgumentException("The error message should not be null.");
    }
    return new NormalizationResult(edmRecord, null, errorMessage);
  }

  /**
   * Creates instance of this class in the case of a successful normalization.
   * 
   * @param normalizedRecordInEdmXml The normalized record.
   * @param report The normalization report.
   * @return An instance of this class.
   */
  public static NormalizationResult createInstanceForSuccess(String normalizedRecordInEdmXml,
      NormalizationReport report) {
    if (normalizedRecordInEdmXml == null) {
      throw new IllegalArgumentException("The record should not be null.");
    }
    if (report == null) {
      throw new IllegalArgumentException("The report should not be null.");
    }
    return new NormalizationResult(normalizedRecordInEdmXml, report, null);
  }

  /**
   * 
   * @return The report. Is null if and only if there was an error.
   */
  public NormalizationReport getReport() {
    return report;
  }

  /**
   * 
   * @return The normalized record. If there was an error, this is the original record.
   */
  public String getNormalizedRecordInEdmXml() {
    return normalizedRecordInEdmXml;
  }

  /**
   * 
   * @return The error message. Is non-null if and only if there was an error.
   */
  public String getErrorMessage() {
    return errorMessage;
  }
}
