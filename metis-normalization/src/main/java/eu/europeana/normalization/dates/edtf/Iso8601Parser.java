package eu.europeana.normalization.dates.edtf;

import static java.lang.String.format;

import eu.europeana.normalization.dates.extraction.DateExtractionException;
import java.lang.invoke.MethodHandles;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Iso8601 parser functionality.
 */
public class Iso8601Parser {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  public static final int ISO_8601_MINIMUM_YEAR_DIGITS = 4;

  private static final Map<DateTimeFormatter, BiFunction<CharSequence, DateTimeFormatter, TemporalAccessor>> DATE_TIME_FORMATTERS =
      Map.of(DateTimeFormatter.ISO_LOCAL_DATE, LocalDate::parse,
          DateTimeFormatter.ofPattern("uuuu-MM"), YearMonth::parse,
          DateTimeFormatter.ofPattern("uuuu"), Year::parse);

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
   * @throws DateExtractionException if the parsing failed
   */
  public TemporalAccessor parseDatePart(String dateInput) throws DateExtractionException {

    final String datePartInput;
    //Strip the time part if present, we are not interested in it
    if (dateInput.contains("T")) {
      datePartInput = dateInput.substring(0, dateInput.indexOf('T'));
      if (datePartInput.isEmpty()) {
        throw new DateExtractionException("Date part is empty which is not allowed");
      }
    } else {
      datePartInput = dateInput;
    }

    final TemporalAccessor temporalAccessor;
    try {
      temporalAccessor = getTemporalAccessor(datePartInput);
    } catch (DateTimeParseException | DateExtractionException e) {
      throw new DateExtractionException(format("TemporalAccessor could not parse value %s", dateInput), e);
    }
    return temporalAccessor;
  }

  private TemporalAccessor getTemporalAccessor(String input) throws DateExtractionException {
    final TemporalAccessor temporalAccessor = DATE_TIME_FORMATTERS.entrySet().stream().map(entry -> {
      try {
        return entry.getValue().apply(input, entry.getKey());
      } catch (DateTimeParseException e) {
        LOGGER.debug("Parsing date failed", e);
      }
      return null;
    }).filter(Objects::nonNull).findFirst().orElse(null);

    if (temporalAccessor == null) {
      throw new DateExtractionException("Parsing date failed");
    }
    return temporalAccessor;
  }

  protected String temporalAccessorToString(TemporalAccessor temporalAccessor) {
    final String resultDateString;
    if (temporalAccessor instanceof LocalDate) {
      resultDateString = LocalDate.from(temporalAccessor).toString();
    } else if (temporalAccessor instanceof YearMonth) {
      resultDateString = YearMonth.from(temporalAccessor).toString();
    } else if (temporalAccessor instanceof Year) {
      final DecimalFormat decimalFormat = new DecimalFormat("0000");
      resultDateString = decimalFormat.format(Year.from(temporalAccessor).getValue());
    } else {
      resultDateString = null;
    }
    return resultDateString;
  }
}
