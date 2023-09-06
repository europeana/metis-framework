package eu.europeana.normalization.dates.extraction.dateextractors;

import static eu.europeana.normalization.dates.DateNormalizationResult.getNoMatchResult;
import static eu.europeana.normalization.dates.YearPrecision.CENTURY;
import static eu.europeana.normalization.dates.edtf.DateQualification.NO_QUALIFICATION;
import static eu.europeana.normalization.dates.edtf.DateQualification.UNCERTAIN;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.DateNormalizationResultStatus;
import eu.europeana.normalization.dates.edtf.DateQualification;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import eu.europeana.normalization.dates.edtf.InstantEdtfDateBuilder;
import eu.europeana.normalization.dates.extraction.DateExtractionException;
import eu.europeana.normalization.dates.sanitize.DateFieldSanitizer;
import java.time.Month;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extractor that matches a date range where the end year includes only the rightmost two digits.
 * <p>
 * The end year in this extractor has to:
 *   <ul>
 *     <li>Be higher than 12 to avoid matching a month value from other extractors.</li>
 *     <li>Be higher than the two rightmost digits of the start year.</li>
 *   </ul>
 * </p>
 * <p>
 *   This pattern needs to be executed before the Edtf extractor because EDTF could potentially match yyyy/MM and yyyy-MM.
 *   Therefore in this extractor we check only the values that are higher than 12 to avoid a mismatch.
 * </p>
 */
public class BriefRangeDateExtractor extends AbstractDateExtractor implements RangeDateExtractor<String> {

  private static final String OPTIONAL_QUESTION_MARK = "\\??";
  private static final Pattern YEAR_PATTERN = Pattern.compile(OPTIONAL_QUESTION_MARK + "(\\d{2,4})" + OPTIONAL_QUESTION_MARK);
  private static final String[] DATES_DELIMITERS = new String[]{"-", "/"};

  @Override
  public DateNormalizationResult extract(String inputValue, DateQualification requestedDateQualification,
      boolean flexibleDateBuild) throws DateExtractionException {
    return extractRange(inputValue, requestedDateQualification, flexibleDateBuild);
  }

  @Override
  public DateNormalizationResult extractStartDateNormalizationResult(String dateString, String rangeDateDelimiters,
      DateQualification requestedDateQualification,
      boolean flexibleDateBuild) throws DateExtractionException {
    DateNormalizationResult dateNormalizationResult = getNoMatchResult(dateString);
    final DateNormalizationResult startYearDateDateNormalizationResult = extractYear(dateString, requestedDateQualification,
        flexibleDateBuild);

    if (startYearDateDateNormalizationResult.getDateNormalizationResultStatus() == DateNormalizationResultStatus.MATCHED) {
      int startYearDigitsLength = (int) (
          Math.log10(((InstantEdtfDate) startYearDateDateNormalizationResult.getEdtfDate()).getYear().getValue()) + 1);
      if (startYearDigitsLength > 2) {
        dateNormalizationResult = startYearDateDateNormalizationResult;
      }
    }

    return dateNormalizationResult;
  }

  @Override
  public DateNormalizationResult extractEndDateNormalizationResult(DateNormalizationResult startDateNormalizationResult,
      String dateString, String rangeDateDelimiters, DateQualification requestedDateQualification, boolean flexibleDateBuild)
      throws DateExtractionException {
    DateNormalizationResult dateNormalizationResult = getNoMatchResult(dateString);
    final DateNormalizationResult endDateNormalizationResult = extractYear(dateString, requestedDateQualification,
        flexibleDateBuild);

    if (endDateNormalizationResult.getDateNormalizationResultStatus() == DateNormalizationResultStatus.MATCHED) {
      final DateQualification endDateQualification = endDateNormalizationResult.getEdtfDate().getDateQualification();

      final int startYearFourDigits = ((InstantEdtfDate) startDateNormalizationResult.getEdtfDate()).getYear().getValue();
      final int startYearLastTwoDigits = startYearFourDigits % CENTURY.getDuration();
      final int endYear = ((InstantEdtfDate) endDateNormalizationResult.getEdtfDate()).getYear().getValue();

      int endYearDigitsLength = (int) (Math.log10(endYear) + 1);
      if (endYearDigitsLength == 2 && endYear > Month.DECEMBER.getValue() && startYearLastTwoDigits < endYear) {
        final int endYearFourDigits = (startYearFourDigits / CENTURY.getDuration()) * CENTURY.getDuration() + endYear;
        final InstantEdtfDate endInstantEdtfDate = new InstantEdtfDateBuilder(endYearFourDigits).withDateQualification(
            endDateQualification).withFlexibleDateBuild(flexibleDateBuild).build();
        dateNormalizationResult = new DateNormalizationResult(DateNormalizationExtractorMatchId.BRIEF_DATE_RANGE, dateString,
            endInstantEdtfDate);
      }
    }

    return dateNormalizationResult;
  }

  private DateNormalizationResult extractYear(String dateString, DateQualification requestedDateQualification,
      boolean flexibleDateBuild) throws DateExtractionException {
    final String sanitizedValue = DateFieldSanitizer.cleanSpacesAndTrim(dateString);
    final DateQualification dateQualification = computeDateQualification(requestedDateQualification, () ->
        (sanitizedValue.startsWith("?") || sanitizedValue.endsWith("?")) ? UNCERTAIN : NO_QUALIFICATION);

    DateNormalizationResult dateNormalizationResult = DateNormalizationResult.getNoMatchResult(dateString);
    final Matcher matcher = YEAR_PATTERN.matcher(sanitizedValue);
    if (matcher.matches()) {
      final int year = Integer.parseInt(matcher.group(1));
      final InstantEdtfDate instantEdtfDate = new InstantEdtfDateBuilder(year).withDateQualification(dateQualification)
                                                                              .withFlexibleDateBuild(flexibleDateBuild).build();
      dateNormalizationResult = new DateNormalizationResult(DateNormalizationExtractorMatchId.BRIEF_DATE_RANGE, dateString,
          instantEdtfDate);
    }
    return dateNormalizationResult;
  }

  @Override
  public List<String> getDateDelimiters() {
    return List.of(DATES_DELIMITERS);
  }

  @Override
  public String getDatesSeparator(String dateDelimiter) {
    return dateDelimiter;
  }

  @Override
  public boolean isRangeMatchSuccess(String rangeDateDelimiters, DateNormalizationResult startDateResult,
      DateNormalizationResult endDateResult) {
    return startDateResult.getDateNormalizationResultStatus() == DateNormalizationResultStatus.MATCHED
        && endDateResult.getDateNormalizationResultStatus() == DateNormalizationResultStatus.MATCHED;
  }

  @Override
  public DateNormalizationExtractorMatchId getDateNormalizationExtractorId(DateNormalizationResult startDateResult,
      DateNormalizationResult endDateResult) {
    return DateNormalizationExtractorMatchId.BRIEF_DATE_RANGE;
  }
}

