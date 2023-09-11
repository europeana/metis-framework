package eu.europeana.normalization.dates.extraction.extractors;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.edtf.DateQualification;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import eu.europeana.normalization.dates.edtf.IntervalEdtfDate;
import eu.europeana.normalization.dates.edtf.IntervalEdtfDateBuilder;
import eu.europeana.normalization.dates.extraction.DateExtractionException;
import eu.europeana.normalization.dates.extraction.DatesSeparator;
import eu.europeana.normalization.dates.sanitize.DateFieldSanitizer;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The abstract class adding the option to a reusable range extractor functionality.
 * <p>It is a generic way to capture ranges for all implementations. It is based on required methods being implemented from
 * {@link RangeDateExtractor} interface.</p>
 *
 * @param <T> the object containing delimiters/separators for dates
 */
public abstract class AbstractRangeDateExtractor<T extends DatesSeparator> extends AbstractDateExtractor implements
    RangeDateExtractor<T> {

  /**
   * Extract the date normalization result for a range.
   * <p>
   * The date is split in two boundaries using the {@link T} to provide the separators. The result will contain the first split
   * that is exactly splitting the original value in two parts(boundaries) and those two boundaries are valid parsable boundaries
   * or null if none found.
   * </p>
   *
   * @param inputValue the range value to attempt parsing
   * @param requestedDateQualification the overwriting value of date qualification, if any
   * @param flexibleDateBuild the flag indicating if during creating of the dates we are flexible with validation
   * @return the date normalization result
   * @throws DateExtractionException if anything happened during the extraction of the date
   */
  @Override
  public DateNormalizationResult extract(String inputValue, DateQualification requestedDateQualification,
      boolean flexibleDateBuild) throws DateExtractionException {
    DateNormalizationResult rangeDate = DateNormalizationResult.getNoMatchResult(inputValue);
    for (T rangeDateQualifier : getRangeDateQualifiers()) {
      // Split with -1 limit does not discard empty splits
      final List<String> sanitizedDateList =
          Arrays.stream(inputValue.split(rangeDateQualifier.getDatesSeparator(), -1))
                .map(DateFieldSanitizer::cleanSpacesAndTrim).collect(
                    Collectors.toList());
      // The sanitizedDateList has to be exactly in two, and then we can verify.
      // This also guarantees that the separator used is not used for unknown characters.
      if (sanitizedDateList.size() == 2) {
        final DateNormalizationResultRangePair dateNormalizationResultRangePair = extractDateNormalizationResult(
            sanitizedDateList.get(0), sanitizedDateList.get(1), rangeDateQualifier, requestedDateQualification,
            flexibleDateBuild);
        final DateNormalizationResult startResult = dateNormalizationResultRangePair.getStartDateNormalizationResult();
        final DateNormalizationResult endResult = dateNormalizationResultRangePair.getEndDateNormalizationResult();
        if (isRangeMatchSuccess(rangeDateQualifier, startResult, endResult)) {
          final DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId =
              getDateNormalizationExtractorId(startResult, endResult);
          final IntervalEdtfDate intervalEdtfDate = new IntervalEdtfDateBuilder((InstantEdtfDate) startResult.getEdtfDate(),
              (InstantEdtfDate) endResult.getEdtfDate()).withFlexibleDateBuild(flexibleDateBuild).build();
          rangeDate = new DateNormalizationResult(dateNormalizationExtractorMatchId, inputValue, intervalEdtfDate);
          break;
        }
      }
    }
    return rangeDate;
  }
}
