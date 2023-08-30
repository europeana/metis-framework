package eu.europeana.normalization.dates.extraction.dateextractors;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.edtf.DateQualification;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import eu.europeana.normalization.dates.edtf.InstantEdtfDateBuilder;
import eu.europeana.normalization.dates.extraction.DateExtractionException;
import eu.europeana.normalization.dates.extraction.MonthMultilingual;
import java.time.Month;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

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

  public static final String DELIMITERS_REGEX = "[ .,]";
  public static final String YEAR_REGEX = "(\\d{4})";
  public static final String DAY_REGEX = "(\\d{1,2})";

  private final EnumMap<Month, PatternDatePartsPair> dayMonthYearPattern = new EnumMap<>(Month.class);
  private final EnumMap<Month, PatternDatePartsPair> monthDayYearPattern = new EnumMap<>(Month.class);
  private final EnumMap<Month, PatternDatePartsPair> monthYearPattern = new EnumMap<>(Month.class);
  private final List<EnumMap<Month, PatternDatePartsPair>> patternMapsInOrder = new LinkedList<>();

  /**
   * Constructor
   */
  public MonthNameDateExtractor() {
    initializePatterns();
  }

  private void initializePatterns() {
    final MonthMultilingual monthMultilingual = new MonthMultilingual();
    Map<Month, Set<String>> monthToAllLanguagesStringsMap = monthMultilingual.getMonthToAllLanguagesStringsMap();
    for (Entry<Month, Set<String>> entry : monthToAllLanguagesStringsMap.entrySet()) {
      final String monthValuesOrRegex = "(" + String.join("|", entry.getValue()) + ")";
      dayMonthYearPattern.put(entry.getKey(), new PatternDatePartsPair(
          compilePattern(new String[]{DAY_REGEX, monthValuesOrRegex, YEAR_REGEX}), DatePartsIndices.DMY_INDICES));
      monthDayYearPattern.put(entry.getKey(), new PatternDatePartsPair(
          compilePattern(new String[]{monthValuesOrRegex, DAY_REGEX, YEAR_REGEX}), DatePartsIndices.MDY_INDICES));
      monthYearPattern.put(entry.getKey(), new PatternDatePartsPair(
          compilePattern(new String[]{monthValuesOrRegex, YEAR_REGEX}), DatePartsIndices.MY_INDICES));
    }
    patternMapsInOrder.add(dayMonthYearPattern);
    patternMapsInOrder.add(monthDayYearPattern);
    patternMapsInOrder.add(monthYearPattern);
  }

  private Pattern compilePattern(String[] parts) {
    return Pattern.compile(String.join(DELIMITERS_REGEX, parts), Pattern.CASE_INSENSITIVE);
  }

  @Override
  public DateNormalizationResult extract(String inputValue, DateQualification requestedDateQualification,
      boolean flexibleDateBuild) throws DateExtractionException {
    final DateQualification dateQualification = computeDateQualification(requestedDateQualification,
        () -> DateQualification.NO_QUALIFICATION);

    for (Month month : Month.values()) {
      for (EnumMap<Month, PatternDatePartsPair> patternMap : patternMapsInOrder) {
        final PatternDatePartsPair patternDatePartsPair = patternMap.get(month);
        final Matcher matcher = patternDatePartsPair.getPattern().matcher(inputValue);
        if (matcher.matches()) {
          final InstantEdtfDateBuilder instantEdtfDateBuilder = new InstantEdtfDateBuilder(
              Integer.parseInt(matcher.group(patternDatePartsPair.getDatePartsIndices().tripleIndices.getLeft())))
              .withMonth(month.getValue())
              .withDateQualification(dateQualification);
          getDayIfPresent(patternDatePartsPair, matcher).ifPresent(instantEdtfDateBuilder::withDay);
          final InstantEdtfDate datePart = instantEdtfDateBuilder.build();
          return new DateNormalizationResult(DateNormalizationExtractorMatchId.MONTH_NAME, inputValue, datePart);
        }
      }
    }
    return DateNormalizationResult.getNoMatchResult(inputValue);
  }

  private Optional<Integer> getDayIfPresent(PatternDatePartsPair patternDatePartsPair, Matcher matcher) {
    if (patternDatePartsPair.getDatePartsIndices().tripleIndices.getRight() != null) {
      return Optional.of(Integer.parseInt(matcher.group(patternDatePartsPair.getDatePartsIndices().tripleIndices.getRight())));
    }
    return Optional.empty();
  }

  enum DatePartsIndices {
    DMY_INDICES(ImmutableTriple.of(3, 2, 1)),
    MDY_INDICES(ImmutableTriple.of(3, 1, 2)),
    MY_INDICES(ImmutableTriple.of(2, 1, null));

    private final Triple<Integer, Integer, Integer> tripleIndices;

    DatePartsIndices(Triple<Integer, Integer, Integer> tripleIndices) {
      this.tripleIndices = tripleIndices;
    }
  }

  static class PatternDatePartsPair {
    private final Pattern pattern;
    private final DatePartsIndices datePartsIndices;

    public PatternDatePartsPair(Pattern pattern, DatePartsIndices datePartsIndices) {
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
}

