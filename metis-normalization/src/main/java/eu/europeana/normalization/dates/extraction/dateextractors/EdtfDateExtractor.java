package eu.europeana.normalization.dates.extraction.dateextractors;

import static eu.europeana.normalization.dates.edtf.DateBoundaryType.OPEN;
import static eu.europeana.normalization.dates.edtf.DateBoundaryType.UNKNOWN;
import static eu.europeana.normalization.dates.edtf.DateQualification.CHECK_QUALIFICATION_PATTERN;
import static eu.europeana.normalization.dates.edtf.DateQualification.NO_QUALIFICATION;
import static eu.europeana.normalization.dates.edtf.InstantEdtfDateBuilder.OVER_4_DIGITS_YEAR_PREFIX;
import static eu.europeana.normalization.dates.edtf.InstantEdtfDateBuilder.THRESHOLD_4_DIGITS_YEAR;
import static eu.europeana.normalization.dates.edtf.IntervalEdtfDate.DATE_INTERVAL_SEPARATOR;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.edtf.AbstractEdtfDate;
import eu.europeana.normalization.dates.edtf.DateQualification;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import eu.europeana.normalization.dates.edtf.InstantEdtfDateBuilder;
import eu.europeana.normalization.dates.edtf.IntervalEdtfDate;
import eu.europeana.normalization.dates.edtf.IntervalEdtfDateBuilder;
import eu.europeana.normalization.dates.edtf.Iso8601Parser;
import eu.europeana.normalization.dates.extraction.DateExtractionException;
import java.lang.invoke.MethodHandles;
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
public class EdtfDateExtractor extends AbstractDateExtractor {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final Iso8601Parser ISO_8601_PARSER = new Iso8601Parser();

  @Override
  public DateNormalizationResult extract(String inputValue, DateQualification requestedDateQualification,
      boolean flexibleDateBuild) throws DateExtractionException {
    if (StringUtils.isEmpty(inputValue)) {
      throw new DateExtractionException("Empty argument");
    }
    final AbstractEdtfDate edtfDate;
    if (inputValue.contains(DATE_INTERVAL_SEPARATOR)) {
      edtfDate = extractInterval(inputValue, requestedDateQualification, flexibleDateBuild);
    } else {
      edtfDate = extractInstant(inputValue, requestedDateQualification, flexibleDateBuild);
    }
    return new DateNormalizationResult(DateNormalizationExtractorMatchId.EDTF, inputValue, edtfDate);
  }

  protected IntervalEdtfDate extractInterval(String dateInput, DateQualification requestedDateQualification,
      boolean allowSwitchMonthDay) throws DateExtractionException {
    String startPart = dateInput.substring(0, dateInput.indexOf(DATE_INTERVAL_SEPARATOR));
    String endPart = dateInput.substring(dateInput.indexOf(DATE_INTERVAL_SEPARATOR) + 1);
    final InstantEdtfDate start = extractInstant(startPart, requestedDateQualification, allowSwitchMonthDay);
    final InstantEdtfDate end = extractInstant(endPart, requestedDateQualification, allowSwitchMonthDay);

    //Are both ends unknown or open, then it is not a date
    if ((end.getDateBoundaryType() == UNKNOWN || end.getDateBoundaryType() == OPEN) &&
        (start.getDateBoundaryType() == UNKNOWN || start.getDateBoundaryType() == OPEN)) {
      throw new DateExtractionException(dateInput);
    }
    return new IntervalEdtfDateBuilder(start, end).withFlexibleDateBuild(allowSwitchMonthDay).build();
  }

  protected InstantEdtfDate extractInstant(String dateInput, DateQualification requestedDateQualification,
      boolean allowSwitchMonthDay) throws DateExtractionException {
    final InstantEdtfDate instantEdtfDate;
    Integer longYear;
    if (UNKNOWN.getDeserializedRepresentation().equals(dateInput)) {
      instantEdtfDate = InstantEdtfDate.getUnknownInstance();
    } else if (OPEN.getDeserializedRepresentation().equals(dateInput)) {
      instantEdtfDate = InstantEdtfDate.getOpenInstance();
    } else if ((longYear = getLongYear(dateInput)) != null) {
      instantEdtfDate = new InstantEdtfDateBuilder(longYear)
          .withLongYear().withDateQualification(requestedDateQualification).build();
    } else {
      instantEdtfDate = extractInstantEdtfDate(dateInput, requestedDateQualification, allowSwitchMonthDay);
    }
    return instantEdtfDate;
  }

  private static Integer getLongYear(String dateInput) {
    final boolean startsWithY = dateInput.startsWith(String.valueOf(OVER_4_DIGITS_YEAR_PREFIX));
    Integer longYear = null;
    if (startsWithY) {
      final String yearSubstring = dateInput.substring(1);
      try {
        //Try parsing year
        longYear = Integer.parseInt(yearSubstring);
      } catch (NumberFormatException er) {
        LOGGER.debug("Not a valid integer at this stage");
      }
      //If prefixed we have to be strict on the length
      if (longYear != null && Math.abs(longYear) <= THRESHOLD_4_DIGITS_YEAR) {
        longYear = null;
      }
    }
    return longYear;
  }

  private static InstantEdtfDate extractInstantEdtfDate(String dateInput, DateQualification requestedDateQualification,
      boolean allowSwitchMonthDay) throws DateExtractionException {
    Matcher matcher = CHECK_QUALIFICATION_PATTERN.matcher(dateInput);
    String dateInputStrippedModifier = dateInput;
    DateQualification dateQualification = requestedDateQualification;

    boolean containsQualification = matcher.matches();
    if (containsQualification && (requestedDateQualification == null || requestedDateQualification == NO_QUALIFICATION)) {
      final String modifier = matcher.group(1);
      if (StringUtils.isNotEmpty(modifier)) {
        dateQualification = DateQualification.fromCharacter(String.valueOf(modifier.charAt(0)));
        dateInputStrippedModifier = dateInput.substring(0, dateInput.length() - 1);
      }
    }

    final TemporalAccessor temporalAccessor = ISO_8601_PARSER.parseDatePart(dateInputStrippedModifier);
    return new InstantEdtfDateBuilder(temporalAccessor)
        .withDateQualification(dateQualification)
        .withFlexibleDateBuild(allowSwitchMonthDay)
        .build();
  }

}
