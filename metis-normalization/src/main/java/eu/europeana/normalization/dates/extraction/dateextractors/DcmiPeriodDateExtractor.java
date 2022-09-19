package eu.europeana.normalization.dates.extraction.dateextractors;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.edtf.EdtfDatePart;
import eu.europeana.normalization.dates.edtf.EdtfParser;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import eu.europeana.normalization.dates.edtf.IntervalEdtfDate;
import eu.europeana.normalization.dates.extraction.DcmiPeriod;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Date extractor for DCMI Period
 */
public class DcmiPeriodDateExtractor implements DateExtractor {


  // TODO: 22/07/2022 Need to fix this regex, it is reported as dangerous
  private static final Pattern DCMI_PERIOD_NAME_WITHOUT_FIELD = Pattern.compile("^([^;=]*)\\s*;");
  private static final Pattern DCMI_PERIOD = Pattern.compile("(start|end|name)\\s*=\\s*(.*?)(?:;|\\s*$)");
  private static final Pattern DCMI_PERIOD_SCHEME = Pattern.compile("scheme\\s*=\\s*(.*?)(?:;|\\s*$)");

  public static DcmiPeriod decodePeriod(String value) {
    // Parse value
    Matcher schemeMatcher = DCMI_PERIOD_SCHEME.matcher(value);
    boolean mayBeW3CDTFEncoded = true;
    if (schemeMatcher.find()) {
      String schemeString = schemeMatcher.group(1);
      if (!"W3C-DTF".equalsIgnoreCase(schemeString) && !"W3CDTF".equalsIgnoreCase(schemeString)) {
        mayBeW3CDTFEncoded = false;
      }
    }
    try {
      if (mayBeW3CDTFEncoded) {
        // Declare fields
        InstantEdtfDate start = null;
        InstantEdtfDate end = null;
        String name = null;
        // Parse
        Matcher m = DCMI_PERIOD.matcher(value);
        while (m.find()) {
          String field = m.group(1);
          String fieldValue = m.group(2);
          if ("start".equals(field)) {
            if (start != null) {
              return null;
            }
            start = parseW3CDTF(fieldValue);
          } else if ("end".equals(field)) {
            if (end != null) {
              return null;
            }
            end = parseW3CDTF(fieldValue);
          } else if ("name".equals(field)) {
            if (name != null) {
              return null;
            }
            name = fieldValue;
          }
        }
        if (start == null && end == null) {
          return null;
        }
        if (name == null) {
          m = DCMI_PERIOD_NAME_WITHOUT_FIELD.matcher(value);
          if (m.find()) {
            name = m.group(1);
          }
        }
        return new DcmiPeriod(start, end, name);
      }
    } catch (IllegalArgumentException ignore) {
      // Parse error
    }
    return null;
  }

  /**
   * @throws IllegalArgumentException if the value cannot be parsed
   */
  private static InstantEdtfDate parseW3CDTF(String value) {
    try {
      EdtfParser parser = new EdtfParser();
      return (InstantEdtfDate) parser.parse(value);
    } catch (ParseException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  public DateNormalizationResult extract(String inputValue) {
    try {
      DcmiPeriod decoded = decodePeriod(inputValue);
      if (decoded == null) {
        return null;
      }

      InstantEdtfDate edtfStart;
      InstantEdtfDate edtfEnd;
      if (decoded.hasStart()) {
        edtfStart = decoded.getStart();
      } else {
        edtfStart = new InstantEdtfDate(EdtfDatePart.getUnspecifiedInstance());
      }
      if (decoded.hasEnd()) {
        edtfEnd = decoded.getEnd();
      } else {
        edtfEnd = new InstantEdtfDate(EdtfDatePart.getUnspecifiedInstance());
      }

      IntervalEdtfDate intervalEdtfDate = new IntervalEdtfDate(decoded.getName(), edtfStart, edtfEnd);
      return new DateNormalizationResult(DateNormalizationExtractorMatchId.DCMI_PERIOD, inputValue, intervalEdtfDate);
    } catch (IllegalStateException e) {
      // a parsing error occoured
      return null;
    }
  }
}
