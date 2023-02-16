package eu.europeana.normalization.dates.edtf;

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

  public AbstractEdtfDate parse(String dateInput, boolean allowSwitchMonthDay) {
    return parse(dateInput, DateQualification.NO_QUALIFICATION, allowSwitchMonthDay);
  }

  public AbstractEdtfDate parse(String dateInput, DateQualification requestedDateQualification, boolean allowSwitchMonthDay)
      throws DateTimeException {
    if (StringUtils.isEmpty(dateInput)) {
      throw new DateTimeException("Empty argument");
    }
    if (dateInput.contains("/")) {
      return parseInterval(dateInput, requestedDateQualification, allowSwitchMonthDay);
    }
    return parseInstant(dateInput, requestedDateQualification, allowSwitchMonthDay);
  }

  protected InstantEdtfDate parseInstant(String dateInput, DateQualification requestedDateQualification,
      boolean allowSwitchMonthDay) throws DateTimeException {
    final InstantEdtfDate instantEdtfDate;
    final DateQualification dateQualification;
    if (dateInput.isEmpty()) {
      instantEdtfDate = InstantEdtfDate.getUnknownInstance();
    } else if ("..".equals(dateInput)) {
      instantEdtfDate = InstantEdtfDate.getOpenInstance();
    } else if (dateInput.startsWith("Y")) {
      instantEdtfDate = new InstantEdtfDateBuilder(Integer.parseInt(dateInput.substring(1)))
          .withDateQualification(requestedDateQualification).build(allowSwitchMonthDay);
    } else {

      Pattern pattern = Pattern.compile("^[^\\?~%]*([\\?~%]?)$");
      Matcher matcher = pattern.matcher(dateInput);
      if (matcher.matches() && StringUtils.isNotEmpty(matcher.group(1))) {
        //Check modifier value
        String modifier = matcher.group(1);
        dateQualification = requestedDateQualification != null && requestedDateQualification != DateQualification.NO_QUALIFICATION
            ? requestedDateQualification : DateQualification.fromCharacter(modifier.charAt(0));
        String dateInputStrippedModifier = dateInput.substring(0, dateInput.length() - 1);
        TemporalAccessor temporalAccessor = ISO_8601_PARSER.parseDatePart(dateInputStrippedModifier);
        instantEdtfDate = new InstantEdtfDateBuilder(temporalAccessor)
            .withDateQualification(dateQualification)
            .build(allowSwitchMonthDay);

      } else {
        TemporalAccessor temporalAccessor = ISO_8601_PARSER.parseDatePart(dateInput);
        instantEdtfDate = new InstantEdtfDateBuilder(temporalAccessor)
            .withDateQualification(requestedDateQualification)
            .build(allowSwitchMonthDay);
      }
    }
    return instantEdtfDate;
  }

  protected IntervalEdtfDate parseInterval(String dateInput, DateQualification requestedDateQualification,
      boolean allowSwitchMonthDay) throws DateTimeException {
    String startPart = dateInput.substring(0, dateInput.indexOf('/'));
    String endPart = dateInput.substring(dateInput.indexOf('/') + 1);
    InstantEdtfDate start = parseInstant(startPart, requestedDateQualification, allowSwitchMonthDay);
    InstantEdtfDate end = parseInstant(endPart, requestedDateQualification, allowSwitchMonthDay);

    if ((end.getDateEdgeType() == DateEdgeType.UNKNOWN || end.getDateEdgeType() == DateEdgeType.OPEN) && (
        start.getDateEdgeType() == DateEdgeType.UNKNOWN
            || start.getDateEdgeType() == DateEdgeType.OPEN)) {
      throw new DateTimeException(dateInput);
    }
    return new IntervalEdtfDate(start, end);
  }
}
