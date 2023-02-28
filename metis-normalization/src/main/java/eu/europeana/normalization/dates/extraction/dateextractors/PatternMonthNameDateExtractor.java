package eu.europeana.normalization.dates.extraction.dateextractors;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.edtf.DateQualification;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import eu.europeana.normalization.dates.edtf.InstantEdtfDateBuilder;
import eu.europeana.normalization.dates.extraction.DateExtractionException;
import eu.europeana.normalization.dates.extraction.MonthMultilingual;
import java.time.Month;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A date where the month is specified by its name or an abbreviation. Supports all the official languages of the European Union
 */
public class PatternMonthNameDateExtractor extends AbstractDateExtractor {

  HashMap<Month, Pattern> patternDayMonthYear = new HashMap<>(12);
  HashMap<Month, Pattern> patternMonthDayYear = new HashMap<>(12);
  HashMap<Month, Pattern> patternMonthYear = new HashMap<>(12);

  public PatternMonthNameDateExtractor() {
    MonthMultilingual months = new MonthMultilingual();
    for (Month month : Month.values()) {
      String monthNamesPattern = null;
      for (String m : months.getMonthStrings(month)) {
        if (monthNamesPattern == null) {
          monthNamesPattern = "(?<month>";
        } else {
          monthNamesPattern += "|";
        }
        monthNamesPattern += m.replaceAll("\\.", "\\.");
      }
      monthNamesPattern += ")";

      patternDayMonthYear
          .put(month,
              Pattern.compile(
                  "\\s*(?<day>\\d\\d?)[ .,]([a-zA-Z]{0,2}[ .,])?" + monthNamesPattern
                      + "[ .,]([a-zA-Z]{0,2}[ .,])?(?<year>\\d{4})\\s*",
                  Pattern.CASE_INSENSITIVE));
      patternMonthDayYear.put(month, Pattern.compile("\\s*" + monthNamesPattern
              + "[ .,]([a-zA-Z]{0,2}[ .,])?(?<day>\\d\\d?)[ .,][a-zA-Z]{0,2}[ .,](?<year>\\d{4})\\s*",
          Pattern.CASE_INSENSITIVE));
      patternMonthYear.put(month,
          Pattern.compile("\\s*" + monthNamesPattern + "[ .,]([a-zA-Z]{0,2}[ .,])?(?<year>\\d{4})\\s*",
              Pattern.CASE_INSENSITIVE));
    }
  }

  @Override
  public DateNormalizationResult extract(String inputValue, DateQualification requestedDateQualification,
      boolean flexibleDateBuild) throws DateExtractionException {
    final DateQualification dateQualification = computeDateQualification(requestedDateQualification,
        () -> DateQualification.NO_QUALIFICATION);

    for (Month month : Month.values()) {
      Matcher m = patternDayMonthYear.get(month).matcher(inputValue);
      if (m.matches()) {
        final InstantEdtfDate datePart = new InstantEdtfDateBuilder(Integer.parseInt(m.group("year")))
            .withMonth(month.getValue())
            .withDay(Integer.parseInt(m.group("day")))
            .withDateQualification(dateQualification)
            .withFlexibleDateBuild(flexibleDateBuild)
            .build();
        return new DateNormalizationResult(DateNormalizationExtractorMatchId.MONTH_NAME, inputValue, datePart);
      }
      m = patternMonthDayYear.get(month).matcher(inputValue);
      if (m.matches()) {
        final InstantEdtfDate datePart = new InstantEdtfDateBuilder(Integer.parseInt(m.group("year")))
            .withMonth(month.getValue())
            .withDay(Integer.parseInt(m.group("day")))
            .withDateQualification(dateQualification)
            .withFlexibleDateBuild(flexibleDateBuild)
            .build();
        return new DateNormalizationResult(DateNormalizationExtractorMatchId.MONTH_NAME, inputValue, datePart);
      }
      m = patternMonthYear.get(month).matcher(inputValue);
      if (m.matches()) {
        final InstantEdtfDate datePart = new InstantEdtfDateBuilder(Integer.parseInt(m.group("year")))
            .withMonth(month.getValue())
            .withDateQualification(dateQualification)
            .withFlexibleDateBuild(flexibleDateBuild)
            .build();
        return new DateNormalizationResult(DateNormalizationExtractorMatchId.MONTH_NAME, inputValue, datePart);
      }
    }
    return DateNormalizationResult.getNoMatchResult(inputValue);
  }
}
