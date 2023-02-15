package eu.europeana.normalization.dates.edtf;

import static java.lang.String.format;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Iso8601 parser functionality.
 */
public class Iso8601Parser {

  private static final Logger LOGGER = LoggerFactory.getLogger(Iso8601Parser.class);
  private static final DateTimeFormatter dateTimeFormatterUUUMM = DateTimeFormatter.ofPattern("uuuu-MM");
  private static final DateTimeFormatter dateTimeFormatterUUUU = DateTimeFormatter.ofPattern("uuuu");

  /**
   * Parser for the iso8601 date part only. The time part if existent is stripped away.
   * <p>
   * Checks for the following formats in order:
   *   <ul>
   *     <li>{@link DateTimeFormatter#ISO_LOCAL_DATE}</li>
   *     <li>{@link DateTimeFormatter#ofPattern)}("yyyy-MM")</li>
   *     <li>{@link DateTimeFormatter#ofPattern)}("yyyy"). Years will be represented with padding 0 up to 4 digits</li>
   *   </ul>
   * </p>
   *
   * @param dateInput the input date
   * @return the string result if the parse succeeded
   * @throws DateTimeException if the parsing failed
   */
  public TemporalAccessor parseDatePart(String dateInput) throws DateTimeException {

    final String datePartInput;
    //Strip the time part if present, we are not interested in it
    if (dateInput.contains("T")) {
      datePartInput = dateInput.substring(0, dateInput.indexOf('T'));
      if (datePartInput.isEmpty()) {
        throw new DateTimeException("Date part is empty which is not allowed");
      }
    } else {
      datePartInput = dateInput;
    }

    final TemporalAccessor temporalAccessor;
    try {
      temporalAccessor = getTemporalAccessor(datePartInput);
    } catch (DateTimeParseException e) {
      throw new DateTimeException(format("TemporalAccessor could not parse value %s", dateInput), e);
    }
    return temporalAccessor;
  }

  // TODO: 13/02/2023 Check maybe fix this complexity(Perhaps allow Parsed returned class and check during date part creation)
  private TemporalAccessor getTemporalAccessor(String input) {
    try {
      return LocalDate.parse(input, DateTimeFormatter.ISO_LOCAL_DATE);
    } catch (DateTimeParseException e1) {
      LOGGER.debug("Parsing date failed", e1);
      try {
        return YearMonth.parse(input, dateTimeFormatterUUUMM);
      } catch (DateTimeParseException e2) {
        LOGGER.debug("Parsing date failed", e2);
        return Year.parse(input, dateTimeFormatterUUUU);
      }
    }
  }

  public String temporalAccessorToString(TemporalAccessor temporalAccessor) {
    String resultDateString;
    try {
      resultDateString = LocalDate.from(temporalAccessor).toString();
    } catch (DateTimeException exception1) {
      LOGGER.debug("LocalDate parsing failed", exception1);
      try {
        resultDateString = YearMonth.from(temporalAccessor).toString();
      } catch (DateTimeException exception2) {
        LOGGER.debug("YearMonth parsing failed", exception2);
        resultDateString = StringUtils.leftPad(Year.from(temporalAccessor).toString(), 4, "0");
      }
    }
    return resultDateString;
  }
}
