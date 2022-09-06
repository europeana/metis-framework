package eu.europeana.normalization.dates.extraction.dateextractors;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.edtf.EdtfDatePart;
import eu.europeana.normalization.dates.edtf.EdtfParser;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import eu.europeana.normalization.dates.edtf.IntervalEdtfDate;
import eu.europeana.normalization.dates.extraction.DcmiPeriod;
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

  private static final Pattern DCMI_PERIOD_START_NAME_WITHOUT_FIELD = Pattern.compile("^((?!scheme|start|end)[^;]*)\\s*;");
  private static final String NON_SPACE_NON_SEMICOLON = "[^\\s;]*";
  private static final String NON_SPACE_NON_LINE_END = "[^\\s$]*";
  private static final String VALUE_ENDING = "(?:;|$)";
  private static final String SPACE_VALUE_ENDING = "\\s*" + VALUE_ENDING;
  private static final Pattern DCMI_PERIOD_SCHEME_PATTERN = Pattern.compile(
      "scheme\\s*=\\s*(" + NON_SPACE_NON_SEMICOLON + "|" + NON_SPACE_NON_LINE_END + ")" + SPACE_VALUE_ENDING);


  private static final Pattern DCMI_PERIOD_START = Pattern.compile(
      "start\\s*=\\s*(" + NON_SPACE_NON_SEMICOLON + "|" + NON_SPACE_NON_LINE_END + ")" + SPACE_VALUE_ENDING);
  private static final Pattern DCMI_PERIOD_END = Pattern.compile(
      "end\\s*=\\s*(" + NON_SPACE_NON_SEMICOLON + "|" + NON_SPACE_NON_LINE_END + ")" + SPACE_VALUE_ENDING);
  //This also matches ending spaces of the value
  private static final Pattern DCMI_PERIOD_NAME = Pattern.compile("name\\s*=\\s*([^;]*|[^$]*)" + VALUE_ENDING);

  private static final Set<String> W3C_DTF_VALUES = Set.of("W3C-DTF", "W3CDTF");

  /**
   * Extracts a DCMI period from a provided value.
   *
   * @param value the value containing a period in dcmi format
   * @return the dmci period object extracted
   */
  public static DcmiPeriod extractDcmiPeriod(String value) {

    DcmiPeriod dcmiPeriod = null;
    if (isValidScheme(value)) {
      try {
        Matcher matcher = DCMI_PERIOD_START.matcher(value);
        InstantEdtfDate start = extractDate(matcher);
        matcher = DCMI_PERIOD_END.matcher(value);
        InstantEdtfDate end = extractDate(matcher);
        String name = extractName(value);

        if (start != null || end != null) {
          dcmiPeriod = new DcmiPeriod(start, end, name);
        }

      } catch (DuplicateFieldException | ParseException e) {
        LOGGER.warn("Exception during dcmi field extraction", e);
      }
    }
    return dcmiPeriod;
  }

  private static InstantEdtfDate extractDate(Matcher matcher) throws DuplicateFieldException, ParseException {
    InstantEdtfDate instantEdtfDate = null;
    if (matcher.find()) {
      final String fieldValue = matcher.group(1);
      if (StringUtils.isNotBlank(fieldValue)) {
        instantEdtfDate = parseW3CDTF(fieldValue);
      }
      //if we find it again we declare invalid
      if (matcher.find()) {
        throw new DuplicateFieldException("Found duplicate field");
      }
    }
    return instantEdtfDate;
  }

  private static String extractName(String value) throws DuplicateFieldException {
    String name = null;
    Matcher matcher = DCMI_PERIOD_NAME.matcher(value);
    if (matcher.find()) {
      name = matcher.group(1);
      name = name.trim(); //Clean up ending spaces
      //if we find it again we declare invalid
      if (matcher.find()) {
        throw new DuplicateFieldException("Found duplicate field");
      }
    }

    //If name is null try checking beginning without field name
    if (name == null) {
      matcher = DCMI_PERIOD_START_NAME_WITHOUT_FIELD.matcher(value);
      if (matcher.find()) {
        name = matcher.group(1);
      }
    }
    return name;
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
      if (W3C_DTF_VALUES.stream().noneMatch(dcmiScheme::equalsIgnoreCase)) {
        isValidScheme = false;
      }
    }
    return isValidScheme;
  }

  private static InstantEdtfDate parseW3CDTF(String value) throws ParseException {
    EdtfParser parser = new EdtfParser();
    return (InstantEdtfDate) parser.parse(value);
  }

  @Override
  public DateNormalizationResult extract(String value) {
    try {
      DcmiPeriod dcmiPeriod = extractDcmiPeriod(value);
      if (dcmiPeriod == null) {
        return null;
      }

      InstantEdtfDate edtfStart;
      InstantEdtfDate edtfEnd;
      if (dcmiPeriod.hasStart()) {
        edtfStart = dcmiPeriod.getStart();
      } else {
        edtfStart = new InstantEdtfDate(EdtfDatePart.getUnspecifiedInstance());
      }
      if (dcmiPeriod.hasEnd()) {
        edtfEnd = dcmiPeriod.getEnd();
      } else {
        edtfEnd = new InstantEdtfDate(EdtfDatePart.getUnspecifiedInstance());
      }

      IntervalEdtfDate intervalEdtfDate = new IntervalEdtfDate(dcmiPeriod.getName(), edtfStart, edtfEnd);
      return new DateNormalizationResult(DateNormalizationExtractorMatchId.DCMI_PERIOD, value, intervalEdtfDate);
    } catch (IllegalStateException e) {
      // a parsing error occurred
      return null;
    }
  }

  private static class DuplicateFieldException extends Exception {

    private static final long serialVersionUID = 4414946393408812953L;

    public DuplicateFieldException(String message) {
      super(message);
    }
  }
}
