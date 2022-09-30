package eu.europeana.normalization.dates.extraction.dateextractors;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.YearPrecision;
import eu.europeana.normalization.dates.edtf.EdtfDatePart;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

  private static final Pattern decadePattern = Pattern.compile("\\??(\\d{3})(?:[ux]\\??|\\?\\?)", Pattern.CASE_INSENSITIVE);

  public DateNormalizationResult extract(String inputValue) {
    final String sanitizedValue = inputValue.replaceAll("\\s", " ").trim();
    final boolean uncertain = sanitizedValue.startsWith("?") || sanitizedValue.endsWith("?");

    DateNormalizationResult dateNormalizationResult = null;
    final Matcher matcher = decadePattern.matcher(sanitizedValue);
    if (matcher.matches()) {
      EdtfDatePart datePart = new EdtfDatePart();
      datePart.setYearPrecision(YearPrecision.DECADE);
      datePart.setYear(Integer.parseInt(matcher.group(1)) * YearPrecision.DECADE.getDuration());
      datePart.setUncertain(uncertain);
      dateNormalizationResult = new DateNormalizationResult(
          DateNormalizationExtractorMatchId.DECADE, inputValue, new InstantEdtfDate(datePart));
    }
    return dateNormalizationResult;
  }
}
