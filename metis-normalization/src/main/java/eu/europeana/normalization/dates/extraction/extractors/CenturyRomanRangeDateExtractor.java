package eu.europeana.normalization.dates.extraction.extractors;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.DateNormalizationResultStatus;
import eu.europeana.normalization.dates.extraction.DateExtractionException;
import eu.europeana.normalization.dates.extraction.DefaultDatesSeparator;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Extractor for Roman century ranges.
 * <p>We reuse the already existent {@link CenturyRomanDateExtractor} code for the boundaries.</p>
 */
public class CenturyRomanRangeDateExtractor extends AbstractRangeDateExtractor<DefaultDatesSeparator> {

  private static final CenturyRomanDateExtractor ROMAN_CENTURY_DATE_EXTRACTOR = new CenturyRomanDateExtractor();

  @Override
  public DateNormalizationResultRangePair extractDateNormalizationResult(String startString, String endString,
      DefaultDatesSeparator rangeDateDelimiters,
      boolean flexibleDateBuild) throws DateExtractionException {
    return new DateNormalizationResultRangePair(
        ROMAN_CENTURY_DATE_EXTRACTOR.extract(startString, flexibleDateBuild),
        ROMAN_CENTURY_DATE_EXTRACTOR.extract(endString, flexibleDateBuild));
  }

  @Override
  public List<DefaultDatesSeparator> getRangeDateQualifiers() {
    return new ArrayList<>(EnumSet.of(DefaultDatesSeparator.DASH_DELIMITER));
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
    return DateNormalizationExtractorMatchId.CENTURY_RANGE_ROMAN;
  }
}
