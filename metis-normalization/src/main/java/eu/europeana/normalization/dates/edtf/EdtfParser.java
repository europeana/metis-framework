package eu.europeana.normalization.dates.edtf;

import eu.europeana.normalization.dates.edtf.EdtfDatePart.EdtfDatePartBuilder;
import java.text.ParseException;
import java.time.temporal.TemporalAccessor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/**
 * EDTF parser with implementation for Level 0 and Level 1 with bypassing time part parsing.
 * <p>
 * For more information check here <a href="https://www.loc.gov/standards/datetime/">EDTF library specification</a>
 * </p>
 */
public class EdtfParser {

  private static final Iso8601Parser ISO_8601_PARSER = new Iso8601Parser();

  public AbstractEdtfDate parse(String dateInput) throws ParseException {
    if (StringUtils.isEmpty(dateInput)) {
      throw new ParseException("Empty argument", 0);
    }
    if (dateInput.contains("/")) {
      return parseInterval(dateInput);
    }
    return parseInstant(dateInput);
  }

  protected InstantEdtfDate parseInstant(String dateInput) throws ParseException {
    EdtfDatePart edtfDatePart;
    if (dateInput.isEmpty()) {
      edtfDatePart = EdtfDatePart.getUnknownInstance();
    } else if ("..".equals(dateInput)) {
      edtfDatePart = EdtfDatePart.getUnspecifiedInstance();
    } else if (dateInput.startsWith("Y")) {
      edtfDatePart = new EdtfDatePartBuilder().withYear(Integer.parseInt(dateInput.substring(1))).build();
      //      edtfDatePart = new EdtfDatePart();
      //      edtfDatePart.setYear(Integer.parseInt(dateInput.substring(1)));
    } else {

      Pattern pattern = Pattern.compile("^[^\\?~%]*([\\?~%]?)$");
      Matcher matcher = pattern.matcher(dateInput);
      if (matcher.matches() && StringUtils.isNotEmpty(matcher.group(1))) {
        //Check modifier value
        String dateInputStrippedModifier = dateInput.substring(0, dateInput.length() - 1);
        TemporalAccessor temporalAccessor = ISO_8601_PARSER.parseDatePart(dateInputStrippedModifier);
        edtfDatePart = new EdtfDatePart(temporalAccessor);

        String modifier = matcher.group(1);
        if ("?".equals(modifier)) {
          edtfDatePart.setUncertain(true);
        } else if ("~".equals(modifier)) {
          edtfDatePart.setApproximate(true);
        } else if ("%".equals(modifier)) {
          edtfDatePart.setApproximate(true);
          edtfDatePart.setUncertain(true);
        }
      } else {
        TemporalAccessor temporalAccessor = ISO_8601_PARSER.parseDatePart(dateInput);
        edtfDatePart = new EdtfDatePart(temporalAccessor);
      }
    }

    return new InstantEdtfDate(edtfDatePart);
  }

  protected IntervalEdtfDate parseInterval(String dateInput) throws ParseException {
    String startPart = dateInput.substring(0, dateInput.indexOf('/'));
    String endPart = dateInput.substring(dateInput.indexOf('/') + 1);
    InstantEdtfDate start = parseInstant(startPart);
    InstantEdtfDate end = parseInstant(endPart);

    if ((end.getEdtfDatePart().isUnknown() || end.getEdtfDatePart().isUnspecified()) && (start.getEdtfDatePart().isUnknown()
        || start.getEdtfDatePart().isUnspecified())) {
      throw new ParseException(dateInput, 0);
    }
    return new IntervalEdtfDate(start, end);
  }
}
