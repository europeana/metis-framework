package eu.europeana.normalization.dates.extraction.dateextractors;

import static eu.europeana.normalization.dates.edtf.DateEdgeType.OPEN;
import static eu.europeana.normalization.dates.edtf.DateEdgeType.UNKNOWN;
import static eu.europeana.normalization.dates.edtf.DateQualification.CHECK_QUALIFICATION_PATTERN;
import static eu.europeana.normalization.dates.edtf.DateQualification.NO_QUALIFICATION;
import static eu.europeana.normalization.dates.edtf.InstantEdtfDateBuilder.OVER_4_DIGITS_YEAR_PREFIX;
import static eu.europeana.normalization.dates.edtf.IntervalEdtfDate.DATES_SEPARATOR;
import static java.lang.String.format;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.edtf.AbstractEdtfDate;
import eu.europeana.normalization.dates.edtf.DateQualification;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import eu.europeana.normalization.dates.edtf.InstantEdtfDateBuilder;
import eu.europeana.normalization.dates.edtf.IntervalEdtfDate;
import eu.europeana.normalization.dates.edtf.IntervalEdtfDateBuilder;
import eu.europeana.normalization.dates.edtf.Iso8601Parser;
import java.time.DateTimeException;
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
public class EdtfDateExtractor implements DateExtractor {

  private static final Logger LOGGER = LoggerFactory.getLogger(EdtfDateExtractor.class);
  private static final Iso8601Parser ISO_8601_PARSER = new Iso8601Parser();

  @Override
  public DateNormalizationResult extract(String inputValue, DateQualification requestedDateQualification,
      boolean allowSwitchMonthDay) {
    DateNormalizationResult dateNormalizationResult = null;
    try {
      if (StringUtils.isEmpty(inputValue)) {
        throw new DateTimeException("Empty argument");
      }
      final AbstractEdtfDate edtfDate;
      if (inputValue.contains(DATES_SEPARATOR)) {
        edtfDate = extractInterval(inputValue, requestedDateQualification, allowSwitchMonthDay);
      } else {
        edtfDate = extractInstant(inputValue, requestedDateQualification, allowSwitchMonthDay);
      }
      dateNormalizationResult = new DateNormalizationResult(DateNormalizationExtractorMatchId.EDTF, inputValue, edtfDate);
    } catch (DateTimeException | NumberFormatException e) {
      LOGGER.debug(format("Date extraction failed %s: ", inputValue), e);
    }
    return dateNormalizationResult;
  }

  protected IntervalEdtfDate extractInterval(String dateInput, DateQualification requestedDateQualification,
      boolean allowSwitchMonthDay) throws DateTimeException {
    String startPart = dateInput.substring(0, dateInput.indexOf(DATES_SEPARATOR));
    String endPart = dateInput.substring(dateInput.indexOf(DATES_SEPARATOR) + 1);
    final InstantEdtfDate start = extractInstant(startPart, requestedDateQualification, allowSwitchMonthDay);
    final InstantEdtfDate end = extractInstant(endPart, requestedDateQualification, allowSwitchMonthDay);

    //Are both ends unknown or open, then it is not a date
    if ((end.getDateEdgeType() == UNKNOWN || end.getDateEdgeType() == OPEN) &&
        (start.getDateEdgeType() == UNKNOWN || start.getDateEdgeType() == OPEN)) {
      throw new DateTimeException(dateInput);
    }
    return new IntervalEdtfDateBuilder(start, end).withAllowSwitchStartEnd(allowSwitchMonthDay).build();
  }

  protected InstantEdtfDate extractInstant(String dateInput, DateQualification requestedDateQualification,
      boolean allowSwitchMonthDay) throws DateTimeException {
    final InstantEdtfDate instantEdtfDate;
    if (UNKNOWN.getDeserializedRepresentation().equals(dateInput)) {
      instantEdtfDate = InstantEdtfDate.getUnknownInstance();
    } else if (OPEN.getDeserializedRepresentation().equals(dateInput)) {
      instantEdtfDate = InstantEdtfDate.getOpenInstance();
    } else if (dateInput.startsWith(String.valueOf(OVER_4_DIGITS_YEAR_PREFIX))) {
      int year = Integer.parseInt(dateInput.substring(1));
      instantEdtfDate = new InstantEdtfDateBuilder(year).withLongYearPrefixedWithY()
                                                        .withDateQualification(requestedDateQualification).build();
    } else {
      instantEdtfDate = extractInstantEdtfDate(dateInput, requestedDateQualification, allowSwitchMonthDay);
    }
    return instantEdtfDate;
  }

  private static InstantEdtfDate extractInstantEdtfDate(String dateInput, DateQualification requestedDateQualification,
      boolean allowSwitchMonthDay) {
    Matcher matcher = CHECK_QUALIFICATION_PATTERN.matcher(dateInput);
    String dateInputStrippedModifier = dateInput;
    DateQualification dateQualification = requestedDateQualification;
    if (matcher.matches() && StringUtils.isNotEmpty(matcher.group(1))) {
      //Check modifier value
      final String modifier = matcher.group(1);
      //Overwrite date qualification if requested
      dateQualification = requestedDateQualification != null && requestedDateQualification != NO_QUALIFICATION
          ? requestedDateQualification : DateQualification.fromCharacter(modifier.charAt(0));
      dateInputStrippedModifier = dateInput.substring(0, dateInput.length() - 1);
    }
    final TemporalAccessor temporalAccessor = ISO_8601_PARSER.parseDatePart(dateInputStrippedModifier);
    return new InstantEdtfDateBuilder(temporalAccessor)
        .withDateQualification(dateQualification)
        .withAllowSwitchMonthDay(allowSwitchMonthDay)
        .build();
  }

}
