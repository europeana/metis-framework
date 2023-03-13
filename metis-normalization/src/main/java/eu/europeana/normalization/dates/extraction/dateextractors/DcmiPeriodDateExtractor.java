package eu.europeana.normalization.dates.extraction.dateextractors;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.edtf.DateBoundaryType;
import eu.europeana.normalization.dates.edtf.DateQualification;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import eu.europeana.normalization.dates.edtf.InstantEdtfDateBuilder;
import eu.europeana.normalization.dates.edtf.IntervalEdtfDate;
import eu.europeana.normalization.dates.edtf.IntervalEdtfDateBuilder;
import eu.europeana.normalization.dates.edtf.Iso8601Parser;
import eu.europeana.normalization.dates.extraction.DateExtractionException;
import java.time.temporal.TemporalAccessor;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/**
 * Date extractor for DCMI Period.
 *
 * @see <a href="https://www.dublincore.org/specifications/dublin-core/dcmi-period/">DCMI Period Encoding Scheme</a>
 */
public class DcmiPeriodDateExtractor extends AbstractDateExtractor {

  private static final Iso8601Parser ISO_8601_PARSER = new Iso8601Parser();
  private static final String NON_SPACE_NON_SEMICOLON = "[^\\s;]*";
  private static final String NON_SPACE_NON_LINE_END = "[^\\s$]*";
  private static final String VALUE_ENDING = "(?:;|$)";
  private static final String SPACE_VALUE_ENDING = "\\s*" + VALUE_ENDING;
  private static final String EQUALS_SPACES_WRAPPED = "\\s*=\\s*";
  private static final String DCMI_FIELD_REGEX =
      EQUALS_SPACES_WRAPPED + "(" + NON_SPACE_NON_SEMICOLON + "|" + NON_SPACE_NON_LINE_END + ")" + SPACE_VALUE_ENDING;
  private static final Pattern DCMI_PERIOD_SCHEME_PATTERN = Pattern.compile("scheme" + DCMI_FIELD_REGEX);
  private static final Pattern DCMI_PERIOD_START_PATTERN = Pattern.compile("start" + DCMI_FIELD_REGEX);
  private static final Pattern DCMI_PERIOD_END_PATTERN = Pattern.compile("end" + DCMI_FIELD_REGEX);

  /**
   * This pattern captures the name field, and it is a little different that the rest of the fields because we want to capture
   * spaces as well.
   * <p>
   * It also captures spaces, so redundant starting or ending spaces can be simply trimmed to avoid quadratic regex
   * complexity.</p>
   */
  private static final Pattern DCMI_PERIOD_NAME_PATTERN = Pattern.compile(
      "name" + EQUALS_SPACES_WRAPPED + "([^;]*|[^$]*)" + VALUE_ENDING);

  private static final Set<String> W3C_DTF_SCHEME_VALUES = Set.of("W3C-DTF", "W3CDTF");

  @Override
  public DateNormalizationResult extract(String value, DateQualification requestedDateQualification,
      boolean flexibleDateBuild) throws DateExtractionException {
    DateNormalizationResult dateNormalizationResult = DateNormalizationResult.getNoMatchResult(value);
    if (isValidScheme(value)) {
      Matcher matcher = DCMI_PERIOD_START_PATTERN.matcher(value);
      InstantEdtfDate start = extractDate(matcher, requestedDateQualification, flexibleDateBuild);
      matcher = DCMI_PERIOD_END_PATTERN.matcher(value);
      InstantEdtfDate end = extractDate(matcher, requestedDateQualification, flexibleDateBuild);
      String name = extractName(value);

      //At least one end has to be specified
      if (start.getDateBoundaryType() == DateBoundaryType.DECLARED || end.getDateBoundaryType() == DateBoundaryType.DECLARED) {
        IntervalEdtfDate intervalEdtfDate = new IntervalEdtfDateBuilder(start, end).withLabel(name)
                                                                                   .withFlexibleDateBuild(
                                                                                       flexibleDateBuild)
                                                                                   .build();
        dateNormalizationResult = new DateNormalizationResult(DateNormalizationExtractorMatchId.DCMI_PERIOD, value,
            intervalEdtfDate);
      }
    }
    return dateNormalizationResult;
  }

  /**
   * Checks if the scheme definition of the DCMI period provided is valid.
   * <p>
   * The scheme is valid if:
   *   <ul>
   *     <li>it is not specified</li>
   *     <li>it is specified and it is of type W3C-DTF</li>
   *   </ul>
   * </p>
   *
   * @param dcmiPeriod the DCMI period
   * @return if the scheme is valid in the period
   */
  private static boolean isValidScheme(String dcmiPeriod) {
    final Matcher schemeMatcher = DCMI_PERIOD_SCHEME_PATTERN.matcher(dcmiPeriod);
    boolean isValidScheme = true;
    //If scheme present we only accept W3C-DTF.
    if (schemeMatcher.find()) {
      String dcmiScheme = schemeMatcher.group(1);
      if (W3C_DTF_SCHEME_VALUES.stream().noneMatch(dcmiScheme::equalsIgnoreCase)) {
        isValidScheme = false;
      }
    }
    return isValidScheme;
  }

  private InstantEdtfDate extractDate(Matcher matcher, DateQualification requestedDateQualification,
      boolean allowSwitchMonthDay) throws DateExtractionException {
    InstantEdtfDate instantEdtfDate = null;
    if (matcher.find()) {
      final String fieldValue = matcher.group(1);
      if (StringUtils.isNotBlank(fieldValue)) {
        TemporalAccessor temporalAccessor = ISO_8601_PARSER.parseDatePart(fieldValue);
        DateQualification dateQualification = computeDateQualification(requestedDateQualification,
            () -> DateQualification.NO_QUALIFICATION);
        instantEdtfDate = new InstantEdtfDateBuilder(temporalAccessor).withDateQualification(dateQualification)
                                                                      .withFlexibleDateBuild(allowSwitchMonthDay).build();
      }
      //if we find it again we declare invalid
      if (matcher.find()) {
        throw new DateExtractionException("Found duplicate field");
      }
    }
    return instantEdtfDate == null ? InstantEdtfDate.getOpenInstance() : instantEdtfDate;
  }

  private static String extractName(String value) throws DateExtractionException {
    String name = null;
    Matcher matcher = DCMI_PERIOD_NAME_PATTERN.matcher(value);
    if (matcher.find()) {
      name = matcher.group(1).trim();
      //if we find it again we declare invalid
      if (matcher.find()) {
        throw new DateExtractionException("Found duplicate field");
      }
    }
    return name;
  }
}
