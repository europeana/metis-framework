package eu.europeana.normalization.dates;

import static eu.europeana.normalization.dates.DateNormalizationResultStatus.MATCHED;

import eu.europeana.normalization.dates.edtf.AbstractEdtfDate;
import eu.europeana.normalization.dates.sanitize.SanitizeOperation;

/**
 * Contains the result of a date normalisation.
 * <p>
 * It contains the pattern that was matched, if some sanitizing was performed, and the normalised value (if successfully
 * normalised).
 * </p>
 */
public class DateNormalizationResult {

  private DateNormalizationResultStatus dateNormalizationResultStatus = MATCHED;
  private SanitizeOperation sanitizeOperation;
  private final DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId;
  private final String originalInput;
  private final AbstractEdtfDate edtfDate;

  /**
   * Constructor with all parameters.
   *
   * @param dateNormalizationExtractorMatchId the date normalization extractor match identifier
   * @param originalInput the original input value
   * @param edtfDate the date result
   */
  public DateNormalizationResult(DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId,
      String originalInput, AbstractEdtfDate edtfDate) {
    this.dateNormalizationExtractorMatchId = dateNormalizationExtractorMatchId;
    this.originalInput = originalInput;
    this.edtfDate = edtfDate;
  }

  /**
   * Copy constructor with adding {@link SanitizeOperation}
   *
   * @param dateNormalizationResult the date normalization result to copy
   * @param sanitizeOperation the sanitization operation
   */
  public DateNormalizationResult(DateNormalizationResult dateNormalizationResult, SanitizeOperation sanitizeOperation) {
    this(dateNormalizationResult.getDateNormalizationExtractorMatchId(), dateNormalizationResult.getOriginalInput(),
        dateNormalizationResult.getEdtfDate());
    this.sanitizeOperation = sanitizeOperation;
  }

  private DateNormalizationResult(DateNormalizationResultStatus dateNormalizationResultStatus, String originalInput) {
    this(null, originalInput, null);
    this.dateNormalizationResultStatus = dateNormalizationResultStatus;
  }

  public DateNormalizationResultStatus getDateNormalizationResultStatus() {
    return dateNormalizationResultStatus;
  }

  /**
   * Get an instance of a date normalization result for no matches.
   *
   * @param originalInput the original input value
   * @return the no match result
   */
  public static DateNormalizationResult getNoMatchResult(String originalInput) {
    return new DateNormalizationResult(DateNormalizationResultStatus.NO_MATCH, originalInput);
  }

  public DateNormalizationExtractorMatchId getDateNormalizationExtractorMatchId() {
    return dateNormalizationExtractorMatchId;
  }

  public SanitizeOperation getSanitizeOperation() {
    return sanitizeOperation;
  }

  public String getOriginalInput() {
    return originalInput;
  }

  public AbstractEdtfDate getEdtfDate() {
    return edtfDate;
  }
}
