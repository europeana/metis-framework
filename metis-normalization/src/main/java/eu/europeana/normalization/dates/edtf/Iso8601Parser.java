package eu.europeana.normalization.dates.edtf;

import java.text.ParseException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.TemporalAccessor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Iso8601 parser functionality.
 */
public class Iso8601Parser {

  private static final Logger LOGGER = LoggerFactory.getLogger(Iso8601Parser.class);

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
   * @throws ParseException if the parsing failed
   */
  public String parseDatePart(String dateInput) throws ParseException {

    final String datePartInput;
    //Strip the time part if present, we are not interested in it
    if (dateInput.contains("T")) {
      datePartInput = dateInput.substring(0, dateInput.indexOf('T'));
      if (datePartInput.isEmpty()) {
        throw new ParseException("Date part is empty which is not allowed", 0);
      }
    } else {
      datePartInput = dateInput;
    }

    DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder()
        .appendOptional(DateTimeFormatter.ISO_LOCAL_DATE)
        .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM"))
        .appendOptional(DateTimeFormatter.ofPattern("yyyy"))
        .toFormatter();

    TemporalAccessor temporalAccessor = dateTimeFormatter.parse(datePartInput);
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
