package eu.europeana.normalization.dates.extraction.dateextractors;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.edtf.EdtfDatePart;
import eu.europeana.normalization.dates.edtf.EdtfDatePart.EdtfDatePartBuilder;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
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

  public DateNormalizationResult extract(String inputValue) {
    Matcher m;
    m = patYyyyyy.matcher(inputValue);
    if (m.matches()) {
      final EdtfDatePart datePart = new EdtfDatePartBuilder(Integer.parseInt(m.group("year"))).build();
      if (m.group("uncertain") != null || m.group("uncertain2") != null) {
        datePart.setUncertain(true);
      }
      return new DateNormalizationResult(DateNormalizationExtractorMatchId.LONG_YEAR, inputValue, new InstantEdtfDate(datePart));
    }
    m = patYyyyyyRange.matcher(inputValue);
    if (m.matches()) {
      final EdtfDatePart startDatePart = new EdtfDatePartBuilder(Integer.parseInt(m.group("year"))).build();
      final EdtfDatePart endDatePart = new EdtfDatePartBuilder(Integer.parseInt(m.group("year2"))).build();
      IntervalEdtfDate intervalEdtfDate = new IntervalEdtfDate(new InstantEdtfDate(startDatePart),
          new InstantEdtfDate(endDatePart));
      if (m.group("uncertain") != null || m.group("uncertain2") != null) {
        intervalEdtfDate.setUncertain(true);
      }
      return new DateNormalizationResult(DateNormalizationExtractorMatchId.LONG_YEAR, inputValue, intervalEdtfDate);
    }
    return null;
  }

}
