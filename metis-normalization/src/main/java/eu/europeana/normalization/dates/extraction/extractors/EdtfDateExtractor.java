package eu.europeana.normalization.dates.extraction.extractors;

import static eu.europeana.normalization.dates.edtf.InstantEdtfDateBuilder.OVER_4_DIGITS_YEAR_PREFIX;
import static eu.europeana.normalization.dates.edtf.InstantEdtfDateBuilder.THRESHOLD_4_DIGITS_YEAR;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.edtf.DateQualification;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import eu.europeana.normalization.dates.edtf.InstantEdtfDateBuilder;
import eu.europeana.normalization.dates.edtf.Iso8601Parser;
import eu.europeana.normalization.dates.extraction.DateExtractionException;
import java.lang.invoke.MethodHandles;
import java.time.temporal.TemporalAccessor;
import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Matcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The pattern for EDTF dates and compatible with ISO 8601 dates.
 * <p>This parser supports partial Level0 and Level1 from the <a href="https://www.loc.gov/standards/datetime/">Extended
 * Date/Time Format (EDTF) Specification</a>. It only validates the date part of a date and the time if existent is discarded.
 * Specifically from Level1, seasons and unspecified digit(s) from the right are not supported
 * </p>
 */
public class EdtfDateExtractor extends AbstractDateExtractor {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final Iso8601Parser ISO_8601_PARSER = new Iso8601Parser();

  @Override
  public DateNormalizationResult extract(String inputValue, boolean allowDayMonthSwap) throws DateExtractionException {
    final InstantEdtfDate instantEdtfDate = extractInstant(inputValue, allowDayMonthSwap);
    return new DateNormalizationResult(DateNormalizationExtractorMatchId.EDTF, inputValue, instantEdtfDate);
  }

  private InstantEdtfDate extractInstant(String dateInput, boolean allowDayMonthSwap) throws DateExtractionException {
    final InstantEdtfDate instantEdtfDate;
    final Integer moreThanFourDigitsYear = getMoreThanFourDigitsYear(dateInput);
    if (moreThanFourDigitsYear != null) {
      instantEdtfDate = new InstantEdtfDateBuilder(moreThanFourDigitsYear).withMoreThanFourDigitsYear().build();
    } else {
      instantEdtfDate = extractInstantEdtfDate(dateInput, allowDayMonthSwap);
    }
    return instantEdtfDate;
  }

  private static Integer getMoreThanFourDigitsYear(String dateInput) {
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

  @Override
  public Set<DateQualification> getQualification(String inputValue) {
    final Matcher qualificationMatcher = DateQualification.PATTERN.matcher(inputValue);
    Set<DateQualification> dateQualifications = EnumSet.noneOf(DateQualification.class);
    if (qualificationMatcher.matches()) {
      final String modifier = qualificationMatcher.group(1);
      dateQualifications = DateQualification.fromCharacter(String.valueOf(modifier.charAt(0)));
    }
    return dateQualifications;
  }

  private InstantEdtfDate extractInstantEdtfDate(String inputValue, boolean allowDayMonthSwap)
      throws DateExtractionException {
    final Set<DateQualification> dateQualifications = getQualification(inputValue);
    String dateInputStrippedModifier = inputValue;
    if (!dateQualifications.isEmpty()) {
      dateInputStrippedModifier = inputValue.substring(0, inputValue.length() - 1);
    }

    final TemporalAccessor temporalAccessor = ISO_8601_PARSER.parseDatePart(dateInputStrippedModifier);
    return new InstantEdtfDateBuilder(temporalAccessor)
        .withDateQualification(dateQualifications)
        .withAllowDayMonthSwap(allowDayMonthSwap)
        .build();
  }

}
