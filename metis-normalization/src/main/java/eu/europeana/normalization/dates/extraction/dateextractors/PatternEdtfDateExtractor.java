package eu.europeana.normalization.dates.extraction.dateextractors;

import static eu.europeana.normalization.dates.edtf.DateEdgeType.OPEN;
import static eu.europeana.normalization.dates.edtf.DateEdgeType.UNKNOWN;
import static eu.europeana.normalization.dates.edtf.DateQualification.CHECK_QUALIFICATION_PATTERN;
import static eu.europeana.normalization.dates.edtf.DateQualification.NO_QUALIFICATION;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.edtf.AbstractEdtfDate;
import eu.europeana.normalization.dates.edtf.DateQualification;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import eu.europeana.normalization.dates.edtf.InstantEdtfDateBuilder;
import eu.europeana.normalization.dates.edtf.IntervalEdtfDate;
import eu.europeana.normalization.dates.edtf.Iso8601Parser;
import java.time.DateTimeException;
import java.time.temporal.TemporalAccessor;
import java.util.regex.Matcher;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The pattern for EDTF dates and compatible with ISO 8601 dates.
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
      LOGGER.debug(String.format("Date extraction failed %s: ", inputValue), e);
    }
    return dateNormalizationResult;
  }

  private AbstractEdtfDate parse(String dateInput, DateQualification requestedDateQualification, boolean allowSwitchMonthDay)
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
    if (UNKNOWN.getStringRepresentation().equals(dateInput)) {
      instantEdtfDate = InstantEdtfDate.getUnknownInstance();
    } else if (OPEN.getStringRepresentation().equals(dateInput)) {
      instantEdtfDate = InstantEdtfDate.getOpenInstance();
    } else if (dateInput.startsWith("Y")) {
      instantEdtfDate = new InstantEdtfDateBuilder(Integer.parseInt(dateInput.substring(1)))
          .withDateQualification(requestedDateQualification).build(allowSwitchMonthDay);
    } else {

      Matcher matcher = CHECK_QUALIFICATION_PATTERN.matcher(dateInput);
      if (matcher.matches() && StringUtils.isNotEmpty(matcher.group(1))) {
        //Check modifier value
        String modifier = matcher.group(1);
        dateQualification = requestedDateQualification != null && requestedDateQualification != NO_QUALIFICATION
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

    if ((end.getDateEdgeType() == UNKNOWN || end.getDateEdgeType() == OPEN) && (
        start.getDateEdgeType() == UNKNOWN
            || start.getDateEdgeType() == OPEN)) {
      throw new DateTimeException(dateInput);
    }
    return new IntervalEdtfDate(start, end);
  }

}
