package eu.europeana.normalization.dates.edtf;

import eu.europeana.normalization.dates.edtf.EdtfDatePart.EdtfDatePartBuilder;
import java.time.DateTimeException;
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

  public AbstractEdtfDate parse(String dateInput, boolean allowSwitchMonthDay) throws DateTimeException {
    if (StringUtils.isEmpty(dateInput)) {
      throw new DateTimeException("Empty argument");
    }
    if (dateInput.contains("/")) {
      return parseInterval(dateInput, allowSwitchMonthDay);
    }
    return parseInstant(dateInput, allowSwitchMonthDay);
  }

  protected InstantEdtfDate parseInstant(String dateInput, boolean allowSwitchMonthDay) throws DateTimeException {
    EdtfDatePart edtfDatePart;
    DateQualification dateQualification = null;
    if (dateInput.isEmpty()) {
      edtfDatePart = EdtfDatePart.getUnknownInstance();
    } else if ("..".equals(dateInput)) {
      edtfDatePart = EdtfDatePart.getUnspecifiedInstance();
    } else if (dateInput.startsWith("Y")) {
      edtfDatePart = new EdtfDatePartBuilder(Integer.parseInt(dateInput.substring(1))).build(allowSwitchMonthDay);
    } else {

      Pattern pattern = Pattern.compile("^[^\\?~%]*([\\?~%]?)$");
      Matcher matcher = pattern.matcher(dateInput);
      if (matcher.matches() && StringUtils.isNotEmpty(matcher.group(1))) {
        //Check modifier value
        String modifier = matcher.group(1);
        dateQualification = DateQualification.fromCharacter(modifier.charAt(0));
        String dateInputStrippedModifier = dateInput.substring(0, dateInput.length() - 1);
        TemporalAccessor temporalAccessor = ISO_8601_PARSER.parseDatePart(dateInputStrippedModifier);
        edtfDatePart = new EdtfDatePartBuilder(temporalAccessor).build(allowSwitchMonthDay);

      } else {
        TemporalAccessor temporalAccessor = ISO_8601_PARSER.parseDatePart(dateInput);
        edtfDatePart = new EdtfDatePartBuilder(temporalAccessor).build(allowSwitchMonthDay);
      }
    }

    return new InstantEdtfDate(edtfDatePart, dateQualification);
  }

  protected IntervalEdtfDate parseInterval(String dateInput, boolean allowSwitchMonthDay) throws DateTimeException {
    String startPart = dateInput.substring(0, dateInput.indexOf('/'));
    String endPart = dateInput.substring(dateInput.indexOf('/') + 1);
    InstantEdtfDate start = parseInstant(startPart, allowSwitchMonthDay);
    InstantEdtfDate end = parseInstant(endPart, allowSwitchMonthDay);

    if ((end.getEdtfDatePart().isUnknown() || end.getEdtfDatePart().isUnspecified()) && (start.getEdtfDatePart().isUnknown()
        || start.getEdtfDatePart().isUnspecified())) {
      throw new DateTimeException(dateInput);
    }
    return new IntervalEdtfDate(start, end);
  }
}
