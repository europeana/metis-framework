package eu.europeana.normalization.dates.extraction.dateextractors;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.edtf.DateQualification;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate.EdtfDatePartBuilder;
import eu.europeana.normalization.dates.edtf.IntervalEdtfDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A year before 1 AD with more than 4 digits. This pattern is typically used in archaeological contexts. The year may contain
 * between 5 and 9 digits. Aso includes the pattern for ranges of this kind of years.
 */
public class PatternLongNegativeYearDateExtractor implements DateExtractor {

  Pattern patYyyyyy = Pattern.compile("\\s*(?<uncertain>\\?)?(?<year>-\\d{5,9})(?<uncertain2>\\?)?\\s*",
      Pattern.CASE_INSENSITIVE);
  Pattern patYyyyyyRange = Pattern.compile(
      "\\s*(?<uncertain>\\?)?(?<year>-\\d{5,9})\\s*/\\s*(?<year2>-\\d{5,9})(?<uncertain2>\\?)?\\s*",
      Pattern.CASE_INSENSITIVE);

  private DateNormalizationResult extract(String inputValue, boolean allowSwitchMonthDay) {
    Matcher m;
    m = patYyyyyy.matcher(inputValue);
    if (m.matches()) {
      final DateQualification dateQualification =
          (m.group("uncertain") != null || m.group("uncertain2") != null) ? DateQualification.UNCERTAIN : null;

      final InstantEdtfDate datePart = new EdtfDatePartBuilder(Integer.parseInt(m.group("year"))).withDateQualification(
          dateQualification).build(allowSwitchMonthDay);
      return new DateNormalizationResult(DateNormalizationExtractorMatchId.LONG_YEAR, inputValue, datePart);
    }
    m = patYyyyyyRange.matcher(inputValue);
    if (m.matches()) {
      final DateQualification dateQualification =
          (m.group("uncertain") != null || m.group("uncertain2") != null) ? DateQualification.UNCERTAIN : null;

      final InstantEdtfDate startDatePart = new EdtfDatePartBuilder(Integer.parseInt(m.group("year"))).withDateQualification(
          dateQualification).build(allowSwitchMonthDay);
      final InstantEdtfDate endDatePart = new EdtfDatePartBuilder(Integer.parseInt(m.group("year2"))).withDateQualification(
          dateQualification).build(allowSwitchMonthDay);
      IntervalEdtfDate intervalEdtfDate = new IntervalEdtfDate(startDatePart, endDatePart);
      return new DateNormalizationResult(DateNormalizationExtractorMatchId.LONG_YEAR, inputValue, intervalEdtfDate);
    }
    return null;
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
