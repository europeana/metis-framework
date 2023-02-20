package eu.europeana.normalization.dates.extraction.dateextractors;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.edtf.EdtfDatePart;
import eu.europeana.normalization.dates.edtf.EdtfParser;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import eu.europeana.normalization.dates.edtf.IntervalEdtfDate;
import java.text.ParseException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Date extractor for DCMI Period
 */
public class DcmiPeriodDateExtractor implements DateExtractor {

  private static final Logger LOGGER = LoggerFactory.getLogger(DcmiPeriodDateExtractor.class);

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
  private static final EdtfParser EDTF_PARSER = new EdtfParser();

  @Override
  public DateNormalizationResult extract(String value) {
    DateNormalizationResult dateNormalizationResult = null;
    if (isValidScheme(value)) {
      try {
        Matcher matcher = DCMI_PERIOD_START_PATTERN.matcher(value);
        InstantEdtfDate start = extractDate(matcher);
        matcher = DCMI_PERIOD_END_PATTERN.matcher(value);
        InstantEdtfDate end = extractDate(matcher);
        String name = extractName(value);

        //At least one end has to be specified
        if (!start.getEdtfDatePart().isUnspecified() || !end.getEdtfDatePart().isUnspecified()) {
          IntervalEdtfDate intervalEdtfDate = new IntervalEdtfDate(name, start, end);
          dateNormalizationResult = new DateNormalizationResult(DateNormalizationExtractorMatchId.DCMI_PERIOD, value,
              intervalEdtfDate);
        }
      } catch (DuplicateFieldException | ParseException e) {
        LOGGER.warn("Exception during dcmi field extraction", e);
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

  private static InstantEdtfDate extractDate(Matcher matcher) throws DuplicateFieldException, ParseException {
    InstantEdtfDate instantEdtfDate = null;
    if (matcher.find()) {
      final String fieldValue = matcher.group(1);
      if (StringUtils.isNotBlank(fieldValue)) {
        instantEdtfDate = (InstantEdtfDate) EDTF_PARSER.parse(fieldValue);
      }
      //if we find it again we declare invalid
      if (matcher.find()) {
        throw new DuplicateFieldException("Found duplicate field");
      }
    }
    return instantEdtfDate == null ? new InstantEdtfDate(EdtfDatePart.getUnspecifiedInstance()) : instantEdtfDate;
  }

  private static String extractName(String value) throws DuplicateFieldException {
    String name = null;
    Matcher matcher = DCMI_PERIOD_NAME_PATTERN.matcher(value);
    if (matcher.find()) {
      name = matcher.group(1).trim();
      //if we find it again we declare invalid
      if (matcher.find()) {
        throw new DuplicateFieldException("Found duplicate field");
      }
    }
    return name;
  }

  private static class DuplicateFieldException extends Exception {

    private static final long serialVersionUID = 4414946393408812953L;

    public DuplicateFieldException(String message) {
      super(message);
    }
  }
}
