package eu.europeana.normalization.dates.extraction.dateextractors;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.YearPrecision;
import eu.europeana.normalization.dates.edtf.DateQualification;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import eu.europeana.normalization.dates.edtf.InstantEdtfDateBuilder;
import eu.europeana.normalization.dates.edtf.IntervalEdtfDate;
import java.time.Month;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extractor that matches a date range where the end year includes only the rightmost two digits.
 * <p>
 * The end year in this extractor has to:
 *   <ul>
 *     <li>Be higher than 12 to avoid matching a month value from other extractors.</li>
 *     <li>Be higher than the two rightmost digits of the start year.</li>
 *   </ul>
 * </p>
 * <p>This pattern needs to be executed before the Edtf one.</p>
 * <p>Most values that match this pattern also match the EDTF pattern, but would result in an invalid date.</p>
 * <p>This pattern only matches values that would not be valid EDTF dates.</p>
 */
public class PatternBriefDateRangeDateExtractor implements DateExtractor {

  private final Pattern briefDateRangePattern = Pattern.compile(
      "(?<startsWithQuestionMark>\\?\\s*)?(?<start>\\d{3,4})[\\-/](?<end>\\d{2})(?<endsWithQuestionMark>\\s*\\?)?");

  public DateNormalizationResult extract(String inputValue, boolean allowSwitchMonthDay) {
    Matcher matcher = briefDateRangePattern.matcher(inputValue.trim());
    DateNormalizationResult dateNormalizationResult = null;
    if (matcher.matches()) {
      int startYear = Integer.parseInt(matcher.group("start"));
      int endYear = Integer.parseInt(matcher.group("end"));

      if (endYear > Month.DECEMBER.getValue()) {
        int startYearAdjusted = startYear % YearPrecision.CENTURY.getDuration();
        if (startYearAdjusted < endYear) {

          final DateQualification dateQualification =
              (matcher.group("startsWithQuestionMark") != null || matcher.group("endsWithQuestionMark") != null)
                  ? DateQualification.UNCERTAIN : null;

          InstantEdtfDate startDatePart = new InstantEdtfDateBuilder(startYear).withDateQualification(dateQualification)
                                                                               .build(allowSwitchMonthDay);
          InstantEdtfDate endDatePart = new InstantEdtfDateBuilder(
              (startDatePart.getYear().getValue() / YearPrecision.CENTURY.getDuration())
                  * YearPrecision.CENTURY.getDuration() + endYear)
              .withDateQualification(dateQualification)
              .build(allowSwitchMonthDay);

          dateNormalizationResult = new DateNormalizationResult(DateNormalizationExtractorMatchId.BRIEF_DATE_RANGE, inputValue,
              new IntervalEdtfDate(startDatePart, endDatePart));
        }
      }
    }
    return dateNormalizationResult;
  }

  @Override
  public DateNormalizationResult extractDateProperty(String inputValue) {
    return extract(inputValue, true);
  }

  @Override
  public DateNormalizationResult extractGenericProperty(String inputValue) {
    return extract(inputValue, false);
  }
}

