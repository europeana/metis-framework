package eu.europeana.normalization.dates.extraction.dateextractors;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.YearPrecision;
import eu.europeana.normalization.dates.edtf.EdtfDatePart;
import eu.europeana.normalization.dates.edtf.EdtfDatePart.EdtfDatePartBuilder;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
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

          EdtfDatePart startDatePart = new EdtfDatePartBuilder(startYear).build(allowSwitchMonthDay);
          EdtfDatePart endDatePart = new EdtfDatePartBuilder(
              (startDatePart.getYear().getValue() / YearPrecision.CENTURY.getDuration())
                  * YearPrecision.CENTURY.getDuration() + endYear)
              .build(allowSwitchMonthDay);

          updateUncertain(matcher, startDatePart, endDatePart);
          dateNormalizationResult = new DateNormalizationResult(DateNormalizationExtractorMatchId.BRIEF_DATE_RANGE, inputValue,
              new IntervalEdtfDate(new InstantEdtfDate(startDatePart), new InstantEdtfDate(endDatePart)));
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

  private void updateUncertain(Matcher matcher, EdtfDatePart startDatePart, EdtfDatePart endDatePart) {
    if (matcher.group("startsWithQuestionMark") != null || matcher.group("endsWithQuestionMark") != null) {
      startDatePart.setUncertain(true);
      endDatePart.setUncertain(true);
    }
  }

}

