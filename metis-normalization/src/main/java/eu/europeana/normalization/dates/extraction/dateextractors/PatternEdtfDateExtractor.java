package eu.europeana.normalization.dates.extraction.dateextractors;

import static eu.europeana.normalization.dates.edtf.DateEdgeType.OPEN;
import static eu.europeana.normalization.dates.edtf.DateEdgeType.UNKNOWN;
import static eu.europeana.normalization.dates.edtf.DateQualification.CHECK_QUALIFICATION_PATTERN;
import static eu.europeana.normalization.dates.edtf.DateQualification.NO_QUALIFICATION;
import static eu.europeana.normalization.dates.edtf.InstantEdtfDate.OVER_4_DIGITS_YEAR_PREFIX;
import static eu.europeana.normalization.dates.edtf.InstantEdtfDate.THRESHOLD_4_DIGITS_YEAR;
import static eu.europeana.normalization.dates.edtf.IntervalEdtfDate.DATES_SEPARATOR;
import static java.lang.String.format;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.edtf.AbstractEdtfDate;
import eu.europeana.normalization.dates.edtf.DateQualification;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import eu.europeana.normalization.dates.edtf.InstantEdtfDateBuilder;
import eu.europeana.normalization.dates.edtf.IntervalEdtfDate;
import eu.europeana.normalization.dates.edtf.Iso8601Parser;
import java.time.DateTimeException;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.regex.Matcher;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The pattern for EDTF dates and compatible with ISO 8601 dates.
 * <p>This parser supports partial Level0 and Level1 from the <a href="https://www.loc.gov/standards/datetime/">Extended
 * Date/Time Format (EDTF) Specification</a>. It only validates the date part of a date and the time if existent it is discarded.
 * Specifically from Level1, seasons and Unspecified digit(s) from the right are not supported
 * </p>
 */
public class PatternEdtfDateExtractor implements DateExtractor {

  private static final Logger LOGGER = LoggerFactory.getLogger(PatternEdtfDateExtractor.class);
  private static final Iso8601Parser ISO_8601_PARSER = new Iso8601Parser();

  @Override
  public DateNormalizationResult extract(String inputValue, DateQualification requestedDateQualification,
      boolean allowSwitchMonthDay) {
    DateNormalizationResult dateNormalizationResult = null;
    try {
      AbstractEdtfDate edtfDate = parse(inputValue, requestedDateQualification, allowSwitchMonthDay);
      dateNormalizationResult = new DateNormalizationResult(DateNormalizationExtractorMatchId.EDTF, inputValue, edtfDate);
    } catch (DateTimeException | NumberFormatException e) {
      LOGGER.debug(format("Date extraction failed %s: ", inputValue), e);
    }
    return dateNormalizationResult;
  }

  private AbstractEdtfDate parse(String dateInput, DateQualification requestedDateQualification, boolean allowSwitchMonthDay)
      throws DateTimeException {
    if (StringUtils.isEmpty(dateInput)) {
      throw new DateTimeException("Empty argument");
    }
    if (dateInput.contains(DATES_SEPARATOR)) {
      return parseInterval(dateInput, requestedDateQualification, allowSwitchMonthDay);
    }
    return parseInstant(dateInput, requestedDateQualification, allowSwitchMonthDay);
  }

  protected InstantEdtfDate parseInstant(String dateInput, DateQualification requestedDateQualification,
      boolean allowSwitchMonthDay) throws DateTimeException {
    final InstantEdtfDate instantEdtfDate;
    if (UNKNOWN.getSerializedRepresentation().equals(dateInput)) {
      instantEdtfDate = InstantEdtfDate.getUnknownInstance();
    } else if (OPEN.getSerializedRepresentation().equals(dateInput)) {
      instantEdtfDate = InstantEdtfDate.getOpenInstance();
    } else if (dateInput.startsWith(String.valueOf(OVER_4_DIGITS_YEAR_PREFIX))) {
      instantEdtfDate = getInstantEdtfDateLongYear(dateInput, requestedDateQualification);
    } else {
      instantEdtfDate = getInstantEdtfDate(dateInput, requestedDateQualification, allowSwitchMonthDay);
    }
    return instantEdtfDate;
  }

  private static InstantEdtfDate getInstantEdtfDate(String dateInput, DateQualification requestedDateQualification,
      boolean allowSwitchMonthDay) {
    Matcher matcher = CHECK_QUALIFICATION_PATTERN.matcher(dateInput);
    String dateInputStrippedModifier = dateInput;
    DateQualification dateQualification = requestedDateQualification;
    if (matcher.matches() && StringUtils.isNotEmpty(matcher.group(1))) {
      //Check modifier value
      String modifier = matcher.group(1);
      dateQualification = requestedDateQualification != null && requestedDateQualification != NO_QUALIFICATION
          ? requestedDateQualification : DateQualification.fromCharacter(modifier.charAt(0));
      dateInputStrippedModifier = dateInput.substring(0, dateInput.length() - 1);
    }
    TemporalAccessor temporalAccessor = ISO_8601_PARSER.parseDatePart(dateInputStrippedModifier);
    if (Math.abs(temporalAccessor.get(ChronoField.YEAR)) > THRESHOLD_4_DIGITS_YEAR) {
      throw new DateTimeException(
          format("Year absolute value greater than %s, should be prefixed with 'Y'", THRESHOLD_4_DIGITS_YEAR));
    }
    return new InstantEdtfDateBuilder(temporalAccessor)
        .withDateQualification(dateQualification)
        .withAllowSwitchMonthDay(allowSwitchMonthDay)
        .build();
  }

  private static InstantEdtfDate getInstantEdtfDateLongYear(String dateInput, DateQualification requestedDateQualification) {
    int year = Integer.parseInt(dateInput.substring(1));
    if (Math.abs(year) <= THRESHOLD_4_DIGITS_YEAR) {
      throw new DateTimeException(
          format("Prefixed year with 'Y' should have absolute value greater than %s", THRESHOLD_4_DIGITS_YEAR));
    }
    return new InstantEdtfDateBuilder(year).withDateQualification(requestedDateQualification).build();
  }

  protected IntervalEdtfDate parseInterval(String dateInput, DateQualification requestedDateQualification,
      boolean allowSwitchMonthDay) throws DateTimeException {
    String startPart = dateInput.substring(0, dateInput.indexOf(DATES_SEPARATOR));
    String endPart = dateInput.substring(dateInput.indexOf(DATES_SEPARATOR) + 1);
    InstantEdtfDate start = parseInstant(startPart, requestedDateQualification, allowSwitchMonthDay);
    InstantEdtfDate end = parseInstant(endPart, requestedDateQualification, allowSwitchMonthDay);

    if ((end.getDateEdgeType() == UNKNOWN || end.getDateEdgeType() == OPEN) && (
        start.getDateEdgeType() == UNKNOWN
            || start.getDateEdgeType() == OPEN)) {
      throw new DateTimeException(dateInput);
    }
    return new IntervalEdtfDate(start, end);
  }

}
