package eu.europeana.enrichment.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class parses strings to years. It takes into account all known versions of the era
 * indicators (e.g. BC or AD, supporting various languages). Years can be of one of the following
 * formats:
 * <ul>
 * <li>[year digits] [era]</li>
 * <li>[era] [year digits]</li>
 * <li>-[year digits] (for BC years)</li>
 * <li>[year digits] (for AD years)</li>
 * </ul>
 * 
 * @author jochen
 *
 */
public class YearParser {

  private static final Logger LOGGER = LoggerFactory.getLogger(YearParser.class);

  private static final String BC_LIST_FILE = "bcList";
  private static final String AD_LIST_FILE = "adList";

  protected static final String REGEX_YEAR_GROUP_NAME = "year";
  private static final String REGEX_SPACE_SEGMENT = "\\s*";
  private static final String REGEX_YEAR_SEGMENT =
      REGEX_SPACE_SEGMENT + "(?<" + REGEX_YEAR_GROUP_NAME + ">\\d+)" + REGEX_SPACE_SEGMENT;
  private static final String YEAR_WITHOUT_ERA_REGEX =
      REGEX_SPACE_SEGMENT + "(?<" + REGEX_YEAR_GROUP_NAME + ">-?\\d+)" + REGEX_SPACE_SEGMENT;

  private static final Pattern YEAR_WITHOUT_ERA_PATTERN = Pattern.compile(YEAR_WITHOUT_ERA_REGEX);
  private static List<Pattern> bcPatterns = null;
  private static List<Pattern> adPatterns = null;

  List<Pattern> getBcPatterns() {
    synchronized (YearParser.class) {
      if (bcPatterns == null) {
        bcPatterns = createEraPatterns(BC_LIST_FILE);
      }
      return Collections.unmodifiableList(bcPatterns);
    }
  }

  List<Pattern> getAdPatterns() {
    synchronized (YearParser.class) {
      if (adPatterns == null) {
        adPatterns = createEraPatterns(AD_LIST_FILE);
      }
      return Collections.unmodifiableList(adPatterns);
    }
  }

  private static List<Pattern> createEraPatterns(String filePath) {

    // Read the lines from the file.
    final List<String> readLines;
    try {
      readLines = IOUtils.readLines(YearParser.class.getClassLoader().getResourceAsStream(filePath),
          StandardCharsets.UTF_8.name());
    } catch (IOException e) {
      LOGGER.error("Problem reading file '" + filePath + "'", e);
      return Collections.emptyList();
    }

    // Trim, remove empty lines and duplicates.
    final Set<String> eraLines = readLines.stream().map(String::trim)
        .filter(line -> !line.isEmpty()).collect(Collectors.toSet());

    // Create patterns: either the year digits come before, or they come after.
    return eraLines.stream().flatMap(YearParser::composeEraRegexPatterns)
        .map(regex -> Pattern.compile(regex, Pattern.CASE_INSENSITIVE))
        .collect(Collectors.toList());
  }

  private static Stream<String> composeEraRegexPatterns(String input) {
    final String quote = Pattern.quote(input);
    return Stream.of(REGEX_SPACE_SEGMENT + quote + REGEX_YEAR_SEGMENT,
        REGEX_YEAR_SEGMENT + quote + REGEX_SPACE_SEGMENT);
  }

  /**
   * This method parses a list of strings to a year.
   * 
   * @param input The list of input strings. Is not null.
   * @return A (possibly empty) set of years. The set is not null, nor does it contain null entries.
   */
  public Set<Integer> parse(List<String> input) {
    return input.stream().map(this::parse).filter(Objects::nonNull).collect(Collectors.toSet());
  }

  /**
   * This method parses a string to a year.
   * 
   * @param input The input string. Is not null.
   * @return The year, or null if it could not parse the string.
   */
  public Integer parse(String input) {

    // First try converting it to an integer directly (possibly with minus sign).
    Integer year = parseYearFromPattern(input, YEAR_WITHOUT_ERA_PATTERN);

    // If that fails, try the AD list first (when in doubt, assume it is AD).
    if (year == null) {
      year = parseYearFromPatterns(input, getAdPatterns());
    }

    // If that fails, try the BC list (note: year must be negated).
    if (year == null) {
      year = parseYearFromPatterns(input, getBcPatterns());
      year = year == null ? null : -year;
    }

    // Done
    return year;
  }

  private Integer parseYearFromPatterns(String input, List<Pattern> patterns) {
    return patterns.stream().map(pattern -> parseYearFromPattern(input, pattern))
        .filter(Objects::nonNull).findAny().orElse(null);
  }

  private Integer parseYearFromPattern(String input, Pattern pattern) {
    final Matcher matcher = pattern.matcher(input);
    if (!matcher.matches()) {
      return null;
    }
    final String yearString = matcher.group(REGEX_YEAR_GROUP_NAME);
    try {
      return Integer.valueOf(yearString);
    } catch (NumberFormatException e) {
      // Cannot really happen as it already matches the regex.
      LOGGER.error("Problem when parsing string '" + yearString + "'.", e);
      return null;
    }
  }
}
