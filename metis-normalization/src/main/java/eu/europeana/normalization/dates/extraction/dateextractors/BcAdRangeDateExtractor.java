package eu.europeana.normalization.dates.extraction.dateextractors;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.DateNormalizationResultStatus;
import eu.europeana.normalization.dates.edtf.DateQualification;
import eu.europeana.normalization.dates.extraction.DateExtractionException;
import java.util.List;

/**
 * Extractor for BC and AD date ranges with variations in the separators of date components.
 * <p>We reuse the already existent {@link BcAdDateExtractor} code for the boundaries.</p>
 */
public class BcAdRangeDateExtractor extends AbstractDateExtractor implements RangeDateExtractor<String> {

  private static final BcAdDateExtractor BC_AD_DATE_EXTRACTOR = new BcAdDateExtractor();
  private static final String[] DATES_DELIMITERS = new String[]{"-", "/"};

  @Override
  public DateNormalizationResult extract(String inputValue, DateQualification requestedDateQualification,
      boolean flexibleDateBuild) throws DateExtractionException {
    return extractRange(inputValue, requestedDateQualification, flexibleDateBuild);
  }

  @Override
  public DateNormalizationResult extractDateNormalizationResult(String dateString, String rangeDateDelimiters,
      DateQualification requestedDateQualification,
      boolean flexibleDateBuild) throws DateExtractionException {
    return BC_AD_DATE_EXTRACTOR.extract(dateString, requestedDateQualification, flexibleDateBuild);
  }

  @Override
  public List<String> getDateDelimiters() {
    return List.of(DATES_DELIMITERS);
  }

  @Override
  public String getDatesSeparator(String dateDelimiter) {
    return dateDelimiter;
  }

  @Override
  public boolean isRangeMatchSuccess(String rangeDateDelimiters, DateNormalizationResult startDateResult,
      DateNormalizationResult endDateResult) {
    return startDateResult.getDateNormalizationResultStatus() == DateNormalizationResultStatus.MATCHED
        && endDateResult.getDateNormalizationResultStatus() == DateNormalizationResultStatus.MATCHED;
  }

  @Override
  public DateNormalizationExtractorMatchId getDateNormalizationExtractorId(DateNormalizationResult startDateResult,
      DateNormalizationResult endDateResult) {
    return DateNormalizationExtractorMatchId.BC_AD;
  }
}
