package eu.europeana.normalization.dates.extraction.extractors;

import static java.lang.String.format;
import static java.time.format.DateTimeFormatter.ofPattern;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import eu.europeana.normalization.dates.edtf.InstantEdtfDateBuilder;
import eu.europeana.normalization.dates.extraction.DateExtractionException;
import eu.europeana.normalization.dates.extraction.EuropeanLanguage;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A full date pattern that does not follow a particular standard.
 * <p>If a timezone with or without offset is present, those are discarded and the date part is taken as such without any
 * adjustment. For example a date "Wed Nov 01 01:00:00 CEST 1989" will be parsed as "1989-11-01" and not as "1989-10-31"</p>
 * <p>
 * Examples:
 *   <ul>
 *     <li>Wed Nov 01 01:00:00 CEST 1989</li>
 *     <li>1989-11-01 04:05:06 UTC+01</li>
 *     <li>1989-11-01 01:02:03</li>
 *   </ul>
 * </p>
 */
public class FullDateDateExtractor extends AbstractDateExtractor {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final List<DateTimeFormatter> DATE_TIME_FORMATTERS = new LinkedList<>();

  public static final int MIN_MILLISECONDS_WIDTH = 0;
  public static final int MAX_MILLISECONDS_WIDTH = 3;

  static {
    DATE_TIME_FORMATTERS.add(
        new DateTimeFormatterBuilder()
            .append(ofPattern("EEE MMM dd HH:mm:ss zzz"))
            .appendOptional(ofPattern("x"))
            .append(ofPattern(" yyyy"))
            .toFormatter()
    );
    DATE_TIME_FORMATTERS.add(
        new DateTimeFormatterBuilder()
            .append(ofPattern("yyyy-MM-dd HH:mm:ss"))
            .appendFraction(ChronoField.MILLI_OF_SECOND, MIN_MILLISECONDS_WIDTH, MAX_MILLISECONDS_WIDTH, true)
            .optionalStart()
            .append(ofPattern(" zzz"))
            .appendOptional(ofPattern("x"))
            .optionalEnd()
            .toFormatter()
    );
  }

  @Override
  public DateNormalizationResult extract(String inputValue, boolean flexibleDateBuild) throws DateExtractionException {
    for (DateTimeFormatter dateTimeFormatter : DATE_TIME_FORMATTERS) {
      final LocalDateTime localDateTime = parseDateWithLocales(inputValue, dateTimeFormatter);
      if (localDateTime != null) {
        final InstantEdtfDate instantEdtfDate = new InstantEdtfDateBuilder(localDateTime)
            .withFlexibleDateBuild(flexibleDateBuild)
            .build();
        return new DateNormalizationResult(DateNormalizationExtractorMatchId.FORMATTED_FULL_DATE, inputValue, instantEdtfDate);
      }
    }
    return DateNormalizationResult.getNoMatchResult(inputValue);
  }

  private static LocalDateTime parseDateWithLocales(String inputValue, DateTimeFormatter dateTimeFormatter) {
    LocalDateTime localDateTime = null;
    for (EuropeanLanguage europeanLanguage : EuropeanLanguage.values()) {
      final DateTimeFormatter dateTimeFormatterWithLocale = dateTimeFormatter.withLocale(europeanLanguage.getLocale());
      try {
        localDateTime = LocalDateTime.parse(inputValue, dateTimeFormatterWithLocale);
        break;
      } catch (DateTimeParseException e) {
        LOGGER.debug(format("Parsing date failed with date time formatter: %s, and locale: %s", dateTimeFormatterWithLocale,
            dateTimeFormatterWithLocale.getLocale()), e);
      }
    }
    return localDateTime;
  }
}
