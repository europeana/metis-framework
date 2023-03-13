package eu.europeana.normalization.dates.extraction.dateextractors;

import static eu.europeana.normalization.dates.YearPrecision.CENTURY;
import static eu.europeana.normalization.dates.edtf.DateQualification.NO_QUALIFICATION;
import static eu.europeana.normalization.dates.edtf.DateQualification.UNCERTAIN;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.edtf.DateQualification;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import eu.europeana.normalization.dates.edtf.InstantEdtfDateBuilder;
import eu.europeana.normalization.dates.edtf.IntervalEdtfDateBuilder;
import eu.europeana.normalization.dates.extraction.DateExtractionException;
import eu.europeana.normalization.dates.sanitize.DateFieldSanitizer;
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
 * <p>
 *   This pattern needs to be executed before the Edtf extractor because EDTF could potentially match yyyy/MM and yyyy-MM.
 *   Therefore in this extractor we check only the values that are higher than 12 to avoid a mismatch.
 * </p>
 */
public class BriefRangeDateExtractor extends AbstractDateExtractor {

  private final Pattern briefRangePattern = Pattern.compile("\\??(\\d{3,4})[\\-/](\\d{2})\\??");

  public DateNormalizationResult extract(String inputValue, DateQualification requestedDateQualification,
      boolean flexibleDateBuild) throws DateExtractionException {
    final String sanitizedValue = DateFieldSanitizer.cleanSpacesAndTrim(inputValue);
    final DateQualification dateQualification = computeDateQualification(requestedDateQualification, () ->
        (sanitizedValue.startsWith("?") || sanitizedValue.endsWith("?")) ? UNCERTAIN : NO_QUALIFICATION);

    DateNormalizationResult dateNormalizationResult = DateNormalizationResult.getNoMatchResult(inputValue);
    final Matcher matcher = briefRangePattern.matcher(sanitizedValue);
    if (matcher.matches()) {
      final int startYearFourDigits = Integer.parseInt(matcher.group(1));
      final int startYearLastTwoDigits = startYearFourDigits % CENTURY.getDuration();
      final int endYearTwoDigits = Integer.parseInt(matcher.group(2));
      final int endYearFourDigits = (startYearFourDigits / CENTURY.getDuration()) * CENTURY.getDuration() + endYearTwoDigits;

      if (endYearTwoDigits > Month.DECEMBER.getValue() && startYearLastTwoDigits < endYearTwoDigits) {
        final InstantEdtfDate startDate = new InstantEdtfDateBuilder(startYearFourDigits)
            .withDateQualification(dateQualification)
            .withFlexibleDateBuild(flexibleDateBuild)
            .build();

        final InstantEdtfDate endDate = new InstantEdtfDateBuilder(endYearFourDigits)
            .withDateQualification(dateQualification)
            .withFlexibleDateBuild(flexibleDateBuild)
            .build();

        dateNormalizationResult = new DateNormalizationResult(DateNormalizationExtractorMatchId.BRIEF_DATE_RANGE, inputValue,
            new IntervalEdtfDateBuilder(startDate, endDate).withFlexibleDateBuild(flexibleDateBuild).build());
      }
    }

    return dateNormalizationResult;
  }
}

