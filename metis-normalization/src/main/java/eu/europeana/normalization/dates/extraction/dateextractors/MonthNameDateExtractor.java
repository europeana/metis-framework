package eu.europeana.normalization.dates.extraction.dateextractors;

import static eu.europeana.normalization.dates.DateNormalizationResult.getNoMatchResult;
import static eu.europeana.normalization.dates.extraction.DatePartsIndices.DMY_INDICES;
import static eu.europeana.normalization.dates.extraction.DatePartsIndices.MDY_INDICES;
import static eu.europeana.normalization.dates.extraction.DatePartsIndices.MY_INDICES;
import static java.util.regex.Pattern.compile;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.DateNormalizationResultStatus;
import eu.europeana.normalization.dates.edtf.DateQualification;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import eu.europeana.normalization.dates.edtf.InstantEdtfDateBuilder;
import eu.europeana.normalization.dates.extraction.DateExtractionException;
import eu.europeana.normalization.dates.extraction.DatePartsIndices;
import eu.europeana.normalization.dates.extraction.MonthMultilingual;
import eu.europeana.normalization.dates.sanitize.DateFieldSanitizer;
import java.lang.invoke.MethodHandles;
import java.time.Month;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extractor that matches dates which contain months represented by their name, in all the 24 european languages.
 *
 * <p>Examples of some cases:
 * <ul>
 *   <li>
 *     01 November 1989
 *     01.November.1989
 *     01,November,1989
 *     November 01 1989
 *     November 1989
 *   </li>
 * </ul>
 * </p>
 */
public class MonthNameDateExtractor extends AbstractDateExtractor {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final String DELIMITERS_REGEX = "[ .,]";
  private static final String YEAR_REGEX = "(\\d{4})";
  private static final String DAY_REGEX = "(\\d{1,2})";

  private static final MonthMultilingual monthMultilingual = new MonthMultilingual();
  private static final String MONTH_JOINED_VALUES =
      monthMultilingual.getMonthToAllLanguagesStringsMap().values().stream().flatMap(Set::stream).collect(
          Collectors.joining("|", "(", ")"));

  private enum MonthNameDatePattern {
    DAY_MONTH_YEAR_PATTERN(compilePattern(new String[]{DAY_REGEX, MONTH_JOINED_VALUES, YEAR_REGEX}), DMY_INDICES),
    MONTH_DAY_YEAR_PATTERN(compilePattern(new String[]{MONTH_JOINED_VALUES, DAY_REGEX, YEAR_REGEX}), MDY_INDICES),
    MONTH_YEAR_PATTERN(compilePattern(new String[]{MONTH_JOINED_VALUES, YEAR_REGEX}), MY_INDICES);

    private final Pattern pattern;
    private final DatePartsIndices datePartsIndices;

    MonthNameDatePattern(Pattern pattern, DatePartsIndices datePartsIndices) {
      this.pattern = pattern;
      this.datePartsIndices = datePartsIndices;
    }

    public Pattern getPattern() {
      return pattern;
    }

    public DatePartsIndices getDatePartsIndices() {
      return datePartsIndices;
    }
  }

  private static Pattern compilePattern(String[] parts) {
    return compile(String.join(DELIMITERS_REGEX, parts), Pattern.CASE_INSENSITIVE);
  }

  @Override
  public DateNormalizationResult extract(String inputValue, DateQualification requestedDateQualification,
      boolean flexibleDateBuild) throws DateExtractionException {
    return Arrays.stream(MonthNameDatePattern.values())
                 .map(operation -> extract(operation, requestedDateQualification, inputValue))
                 .filter(dateNormalizationResult -> dateNormalizationResult.getDateNormalizationResultStatus()
                     == DateNormalizationResultStatus.MATCHED).findFirst()
                 .orElse(getNoMatchResult(inputValue));
  }

  private DateNormalizationResult extract(MonthNameDatePattern monthNameDatePattern, DateQualification requestedDateQualification,
      String inputValue) {
    final DateQualification dateQualification = computeDateQualification(requestedDateQualification,
        () -> DateQualification.NO_QUALIFICATION);
    DateNormalizationResult dateNormalizationResult = getNoMatchResult(inputValue);
    try {
      final String sanitizedValue = DateFieldSanitizer.cleanSpacesAndTrim(inputValue);
      final Matcher matcher = monthNameDatePattern.getPattern().matcher(sanitizedValue);
      if (matcher.matches()) {
        final Month month = monthMultilingual.getMonth(
            matcher.group(monthNameDatePattern.getDatePartsIndices().getMonthIndex()));
        final InstantEdtfDateBuilder instantEdtfDateBuilder = new InstantEdtfDateBuilder(
            Integer.parseInt(matcher.group(monthNameDatePattern.getDatePartsIndices().getYearIndex())))
            .withMonth(month.getValue())
            .withDateQualification(dateQualification);
        getDayIfPresent(monthNameDatePattern, matcher).ifPresent(instantEdtfDateBuilder::withDay);
        final InstantEdtfDate instantEdtfDate = instantEdtfDateBuilder.build();
        dateNormalizationResult = new DateNormalizationResult(DateNormalizationExtractorMatchId.MONTH_NAME, inputValue,
            instantEdtfDate);
      }
    } catch (DateExtractionException e) {
      LOGGER.warn("Failed instance extraction!", e);
    }
    return dateNormalizationResult;
  }

  private Optional<Integer> getDayIfPresent(MonthNameDatePattern monthNameDatePattern, Matcher matcher) {
    if (monthNameDatePattern.getDatePartsIndices().getDayIndex() != null) {
      return Optional.of(Integer.parseInt(matcher.group(monthNameDatePattern.getDatePartsIndices().getDayIndex())));
    }
    return Optional.empty();
  }
}

