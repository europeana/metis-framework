package eu.europeana.normalization.dates.extraction.dateextractors;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.DateNormalizationResultStatus;
import eu.europeana.normalization.dates.edtf.DateQualification;
import eu.europeana.normalization.dates.extraction.DateExtractionException;
import eu.europeana.normalization.dates.extraction.DefaultDatesSeparator;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * A year before 1 AD with more than 4 digits. This pattern is typically used in archaeological contexts. The year may contain
 * between 5 and 9 digits. Aso includes the pattern for ranges of this kind of years.
 */
public class LongNegativeYearRangeDateExtractor extends AbstractRangeDateExtractor<DefaultDatesSeparator> {

  private static final LongNegativeYearDateExtractor LONG_NEGATIVE_YEAR_DATE_EXTRACTOR = new LongNegativeYearDateExtractor();

  @Override
  public List<DefaultDatesSeparator> getRangeDateQualifiers() {
    return new ArrayList<>(EnumSet.of(DefaultDatesSeparator.SLASH_DELIMITER));
  }

  @Override
  public DateNormalizationResultRangePair extractDateNormalizationResult(String startString, String endString,
      DefaultDatesSeparator rangeDateDelimiters, DateQualification requestedDateQualification, boolean flexibleDateBuild)
      throws DateExtractionException {
    return new DateNormalizationResultRangePair(
        LONG_NEGATIVE_YEAR_DATE_EXTRACTOR.extract(startString, requestedDateQualification, flexibleDateBuild),
        LONG_NEGATIVE_YEAR_DATE_EXTRACTOR.extract(endString, requestedDateQualification, flexibleDateBuild));
  }

  @Override
  public boolean isRangeMatchSuccess(DefaultDatesSeparator rangeDateDelimiters, DateNormalizationResult startDateResult,
      DateNormalizationResult endDateResult) {
    return startDateResult.getDateNormalizationResultStatus() == DateNormalizationResultStatus.MATCHED
        && endDateResult.getDateNormalizationResultStatus() == DateNormalizationResultStatus.MATCHED;
  }

  @Override
  public DateNormalizationExtractorMatchId getDateNormalizationExtractorId(DateNormalizationResult startDateResult,
      DateNormalizationResult endDateResult) {
    return DateNormalizationExtractorMatchId.LONG_NEGATIVE_YEAR;
  }
}
