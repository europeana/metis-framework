package eu.europeana.normalization.dates.extraction.dateextractors;

import static java.lang.String.format;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.YearPrecision;
import eu.europeana.normalization.dates.edtf.DateQualification;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import eu.europeana.normalization.dates.edtf.InstantEdtfDateBuilder;
import eu.europeana.normalization.dates.sanitize.DateFieldSanitizer;
import java.time.DateTimeException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extractor that matches a decade of the format YYY[ux].
 * <p>The decade may start and/or end with a question mark indicating uncertainty. Dates such as '198-' are not supported because
 * they may indicate a decade or a time period with an open end.</p>
 * <p>
 * Examples:
 *   <ul>
 *     <li>180u</li>
 *     <li>180x</li>
 *     <li>?180u</li>
 *     <li>?180x</li>
 *     <li>180??</li>
 *     <li>180x?</li>
 *   </ul>
 * </p>
 * <p>
 * A decade represented as YYYu or YYYx. For example, '198u', '198x' Dates such as '198-' are not supported because they may
 * indicate a decade or a time period with an open end
 */
public class DecadeDateExtractor implements DateExtractor {

  private static final Logger LOGGER = LoggerFactory.getLogger(DecadeDateExtractor.class);
  private static final Pattern decadePattern = Pattern.compile("\\??(\\d{3})(?:[ux]\\??|\\?\\?)", Pattern.CASE_INSENSITIVE);

  @Override
  public DateNormalizationResult extract(String inputValue, DateQualification requestedDateQualification,
      boolean allowSwitchesDuringValidation) {
    final String sanitizedValue = DateFieldSanitizer.cleanSpacesAndTrim(inputValue);
    final DateQualification dateQualification = computeDateQualification(requestedDateQualification, () ->
        (sanitizedValue.startsWith("?") || sanitizedValue.endsWith("?")) ? DateQualification.UNCERTAIN : null);

    DateNormalizationResult dateNormalizationResult = null;
    try {
      final Matcher matcher = decadePattern.matcher(sanitizedValue);
      if (matcher.matches()) {
        final InstantEdtfDate datePart = new InstantEdtfDateBuilder(
            Integer.parseInt(matcher.group(1)) * YearPrecision.DECADE.getDuration())
            .withYearPrecision(YearPrecision.DECADE)
            .withDateQualification(dateQualification)
            .withAllowSwitchMonthDay(allowSwitchesDuringValidation)
            .build();
        dateNormalizationResult = new DateNormalizationResult(
            DateNormalizationExtractorMatchId.DECADE, inputValue, datePart);
      }
    } catch (DateTimeException e) {
      LOGGER.debug(format("Date extraction failed %s: ", inputValue), e);
    }
    return dateNormalizationResult;
  }
}
