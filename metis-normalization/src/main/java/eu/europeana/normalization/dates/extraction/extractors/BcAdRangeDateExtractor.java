package eu.europeana.normalization.dates.extraction.extractors;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.DateNormalizationResultStatus;
import eu.europeana.normalization.dates.extraction.DateExtractionException;
import eu.europeana.normalization.dates.extraction.DefaultDatesSeparator;
import java.util.List;

/**
 * Extractor for BC and AD date ranges with variations in the separators of date components.
 * <p>We reuse the already existent {@link BcAdDateExtractor} code for the boundaries.</p>
 */
public class BcAdRangeDateExtractor extends AbstractRangeDateExtractor<DefaultDatesSeparator> {

  private static final BcAdDateExtractor BC_AD_DATE_EXTRACTOR = new BcAdDateExtractor();

  @Override
  public DateNormalizationResultRangePair extractDateNormalizationResult(String startString, String endString,
      DefaultDatesSeparator rangeDateDelimiters,
      boolean allowDayMonthSwap) throws DateExtractionException {
    return new DateNormalizationResultRangePair(
        BC_AD_DATE_EXTRACTOR.extract(startString, allowDayMonthSwap),
        BC_AD_DATE_EXTRACTOR.extract(endString, allowDayMonthSwap));
  }

  @Override
  public List<DefaultDatesSeparator> getRangeDateQualifiers() {
    return List.of(DefaultDatesSeparator.values());
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
    return DateNormalizationExtractorMatchId.BC_AD;
  }
}
