package eu.europeana.normalization.dates.extraction.dateextractors;

import static eu.europeana.normalization.dates.edtf.DateQualification.NO_QUALIFICATION;
import static eu.europeana.normalization.dates.edtf.DateQualification.UNCERTAIN;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.edtf.DateQualification;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import eu.europeana.normalization.dates.edtf.InstantEdtfDateBuilder;
import eu.europeana.normalization.dates.extraction.DateExtractionException;
import eu.europeana.normalization.dates.sanitize.DateFieldSanitizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A year before 1 AD with more than 4 digits. This pattern is typically used in archaeological contexts. The year may contain
 * between 5 and 9 digits. Aso includes the pattern for ranges of this kind of years.
 */
public class LongNegativeYearDateExtractor extends AbstractDateExtractor {

  private static final String OPTIONAL_QUESTION_MARK = "\\??";
  private static final Pattern YEAR_PATTERN = Pattern.compile(OPTIONAL_QUESTION_MARK + "(-?\\d{5,9})" + OPTIONAL_QUESTION_MARK);

  @Override
  public DateNormalizationResult extract(String dateString, DateQualification requestedDateQualification,
      boolean flexibleDateBuild) throws DateExtractionException {
    final String sanitizedValue = DateFieldSanitizer.cleanSpacesAndTrim(dateString);
    final DateQualification dateQualification = computeDateQualification(requestedDateQualification, () ->
        (sanitizedValue.startsWith("?") || sanitizedValue.endsWith("?")) ? UNCERTAIN : NO_QUALIFICATION);

    DateNormalizationResult dateNormalizationResult = DateNormalizationResult.getNoMatchResult(dateString);
    final Matcher matcher = YEAR_PATTERN.matcher(sanitizedValue);
    if (matcher.matches()) {
      final int year = Integer.parseInt(matcher.group(1));
      final InstantEdtfDate instantEdtfDate =
          new InstantEdtfDateBuilder(year).withDateQualification(dateQualification)
                                          .withLongYear()
                                          .withFlexibleDateBuild(flexibleDateBuild).build();
      dateNormalizationResult = new DateNormalizationResult(DateNormalizationExtractorMatchId.LONG_NEGATIVE_YEAR, dateString,
          instantEdtfDate);
    }
    return dateNormalizationResult;
  }

}
