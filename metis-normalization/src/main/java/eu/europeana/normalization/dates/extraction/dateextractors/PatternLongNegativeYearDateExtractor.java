package eu.europeana.normalization.dates.extraction.dateextractors;

import static eu.europeana.normalization.dates.edtf.DateQualification.NO_QUALIFICATION;
import static eu.europeana.normalization.dates.edtf.DateQualification.UNCERTAIN;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.edtf.DateQualification;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import eu.europeana.normalization.dates.edtf.InstantEdtfDateBuilder;
import eu.europeana.normalization.dates.edtf.IntervalEdtfDate;
import eu.europeana.normalization.dates.edtf.IntervalEdtfDateBuilder;
import eu.europeana.normalization.dates.extraction.DateExtractionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A year before 1 AD with more than 4 digits. This pattern is typically used in archaeological contexts. The year may contain
 * between 5 and 9 digits. Aso includes the pattern for ranges of this kind of years.
 */
public class PatternLongNegativeYearDateExtractor extends AbstractDateExtractor {

  Pattern patYyyyyy = Pattern.compile("\\s*(?<uncertain>\\?)?(?<year>-\\d{5,9})(?<uncertain2>\\?)?\\s*",
      Pattern.CASE_INSENSITIVE);
  Pattern patYyyyyyRange = Pattern.compile(
      "\\s*(?<uncertain>\\?)?(?<year>-\\d{5,9})\\s*/\\s*(?<year2>-\\d{5,9})(?<uncertain2>\\?)?\\s*",
      Pattern.CASE_INSENSITIVE);

  @Override
  public DateNormalizationResult extract(String inputValue, DateQualification requestedDateQualification,
      boolean flexibleDateBuild) throws DateExtractionException {
    final DateQualification dateQualification;

    final Matcher m = patYyyyyy.matcher(inputValue);
    if (m.matches()) {
      dateQualification =
          computeDateQualification(requestedDateQualification,
              () -> (m.group("uncertain") != null || m.group("uncertain2") != null) ? UNCERTAIN : NO_QUALIFICATION);

      final InstantEdtfDate datePart = new InstantEdtfDateBuilder(Integer.parseInt(m.group("year")))
          .withLongYear().withDateQualification(dateQualification).withFlexibleDateBuild(flexibleDateBuild).build();
      return new DateNormalizationResult(DateNormalizationExtractorMatchId.LONG_NEGATIVE_YEAR, inputValue, datePart);
    }
    final Matcher m2 = patYyyyyyRange.matcher(inputValue);
    if (m2.matches()) {
      dateQualification =
          computeDateQualification(requestedDateQualification,
              () -> (m2.group("uncertain") != null || m2.group("uncertain2") != null) ? UNCERTAIN : NO_QUALIFICATION);

      final InstantEdtfDate startDatePart = new InstantEdtfDateBuilder(Integer.parseInt(m2.group("year")))
          .withLongYear().withDateQualification(dateQualification).withFlexibleDateBuild(flexibleDateBuild).build();
      final InstantEdtfDate endDatePart = new InstantEdtfDateBuilder(Integer.parseInt(m2.group("year2")))
          .withLongYear().withDateQualification(dateQualification).withFlexibleDateBuild(flexibleDateBuild).build();
      IntervalEdtfDate intervalEdtfDate = new IntervalEdtfDateBuilder(startDatePart, endDatePart).withFlexibleDateBuild(
          flexibleDateBuild).build();
      return new DateNormalizationResult(DateNormalizationExtractorMatchId.LONG_NEGATIVE_YEAR, inputValue, intervalEdtfDate);
    }
    return DateNormalizationResult.getNoMatchResult(inputValue);
  }
}
