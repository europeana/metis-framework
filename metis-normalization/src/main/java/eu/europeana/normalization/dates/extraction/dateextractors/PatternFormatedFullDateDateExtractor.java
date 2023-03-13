package eu.europeana.normalization.dates.extraction.dateextractors;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.edtf.DateQualification;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import eu.europeana.normalization.dates.edtf.InstantEdtfDateBuilder;
import eu.europeana.normalization.dates.extraction.DateExtractionException;
import eu.europeana.normalization.dates.extraction.MonthMultilingual;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Patterns for date formats that are well-structured but do not follow a particular standard
 */
public class PatternFormatedFullDateDateExtractor extends AbstractDateExtractor {

  MonthMultilingual monthNames = new MonthMultilingual();

  // "Thu Dec 31 01:00:00 CET 1863","31 Dec 1863"
  // month day hour minute second year
  Pattern patFormatedDate = Pattern
      .compile("\\w{3} (\\w{3}) (\\d{2}) (\\d{2}):(\\d{2}):(\\d{2}) \\w{3,4} (\\d{1,4})");

  // 2020-06-21 13:43:26 UTC
  // year month day hour minute second
  Pattern patFormatedDate2 = Pattern
      .compile("(\\d{4})-(\\d{2})-(\\d{2}) (\\d{2}):(\\d{2}):(\\d{2}) \\w{3,4}\\s?(\\d{0,4})");

  // 2018-03-27 09:08:34
  // year month day hour minute second
  Pattern patFormatedDate3 = Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2}) (\\d{2}):(\\d{2}):(\\d{2})(\\.\\d{1,3})?");

  @Override
  public DateNormalizationResult extract(String inputValue, DateQualification requestedDateQualification,
      boolean flexibleDateBuild) throws DateExtractionException {
    final DateQualification dateQualification = computeDateQualification(requestedDateQualification,
        () -> DateQualification.NO_QUALIFICATION);

    Matcher m = patFormatedDate2.matcher(inputValue);
    if (m.matches()) {
      final InstantEdtfDate datePart = new InstantEdtfDateBuilder(Integer.parseInt(m.group(1)))
          .withMonth(Integer.parseInt(m.group(2)))
          .withDay(Integer.parseInt(m.group(3)))
          .withDateQualification(dateQualification).withFlexibleDateBuild(flexibleDateBuild).build();
      return new DateNormalizationResult(DateNormalizationExtractorMatchId.FORMATTED_FULL_DATE, inputValue, datePart);
    }
    m = patFormatedDate.matcher(inputValue);
    if (m.matches()) {
      final InstantEdtfDate datePart = new InstantEdtfDateBuilder(Integer.parseInt(m.group(6)))
          .withMonth(monthNames.getMonthIndexValue(m.group(1)))
          .withDay(Integer.parseInt(m.group(2)))
          .withDateQualification(dateQualification).withFlexibleDateBuild(flexibleDateBuild).build();
      return new DateNormalizationResult(DateNormalizationExtractorMatchId.FORMATTED_FULL_DATE, inputValue, datePart);
    }
    m = patFormatedDate3.matcher(inputValue);
    if (m.matches()) {
      final InstantEdtfDate datePart = new InstantEdtfDateBuilder(Integer.parseInt(m.group(1)))
          .withMonth(Integer.parseInt(m.group(2)))
          .withDay(Integer.parseInt(m.group(3)))
          .withDateQualification(dateQualification).withFlexibleDateBuild(flexibleDateBuild).build();
      return new DateNormalizationResult(DateNormalizationExtractorMatchId.FORMATTED_FULL_DATE, inputValue, datePart);
    }
    return DateNormalizationResult.getNoMatchResult(inputValue);
  }
}
