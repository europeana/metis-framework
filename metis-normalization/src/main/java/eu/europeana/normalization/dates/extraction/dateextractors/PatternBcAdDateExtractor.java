package eu.europeana.normalization.dates.extraction.dateextractors;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.edtf.DateQualification;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import eu.europeana.normalization.dates.edtf.InstantEdtfDateBuilder;
import eu.europeana.normalization.dates.edtf.IntervalEdtfDate;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A year with an indication of the era, for example ‘3000 BC’. Currently, the normalisation process recognizes ‘BC/AD’ and
 * ‘AC/DC’, but the abbreviations used in other languages will be supported in the future. Or a date range where the start/end
 * years contain an indication of the era.
 */
public class PatternBcAdDateExtractor implements DateExtractor {

  static final HashSet<String> bcAbbreviations = new HashSet<>();

  static {
    bcAbbreviations.add("B\\.?C".toLowerCase());
    bcAbbreviations.add("A\\.?C".toLowerCase());
    bcAbbreviations.add("v\\.?Chr".toLowerCase());
    bcAbbreviations.add("vC".toLowerCase());
    bcAbbreviations.add("avant J\\.?-C".toLowerCase());
    bcAbbreviations.add("av[\\. ]J\\.?-C".toLowerCase());
    //bcAbbreviations.add("eKr"); removed due to ambiguity
    bcAbbreviations.add("f\\.?Kr".toLowerCase());
    bcAbbreviations.add("π\\.*Χ".toLowerCase());
  }

  static final HashSet<String> adAbbreviations = new HashSet<>();

  static {
    adAbbreviations.add("A\\.?D".toLowerCase());
    adAbbreviations.add("D\\.?C".toLowerCase());
    adAbbreviations.add("n\\.?Chr".toLowerCase());
    adAbbreviations.add("nC".toLowerCase());
    adAbbreviations.add("après J-C".toLowerCase());
    adAbbreviations.add("apres J-C".toLowerCase());
    adAbbreviations.add("ap[\\. ]J-C".toLowerCase());
    //adAbbreviations.add("eKr"); removed due to ambiguity
    adAbbreviations.add("j\\.?Kr".toLowerCase());
    adAbbreviations.add("μ\\.?Χ".toLowerCase());
  }

  static final HashSet<Pattern> bcAbbreviationsPatterns = new HashSet<>();

  static {
    for (String abbrev : bcAbbreviations) {
      bcAbbreviationsPatterns.add(Pattern.compile(abbrev, Pattern.CASE_INSENSITIVE));
    }
  }

  Pattern patYyyy;
  Pattern patRange;

  public PatternBcAdDateExtractor() {
    String patYearBcAd = "(?<year>\\d{2,4})\\s*(?<era>";
    patYearBcAd += bcAbbreviations.stream().collect(Collectors.joining("|"));
    patYearBcAd += adAbbreviations.stream().collect(Collectors.joining("|"));
    patYearBcAd = patYearBcAd.substring(0, patYearBcAd.length() - 1) + ")\\.?";

    patYyyy = Pattern.compile(patYearBcAd, Pattern.CASE_INSENSITIVE);
    patRange = Pattern.compile(
        patYearBcAd + "\\s*[\\-\\/]\\s*" + patYearBcAd.replace("<year>", "<year2>").replace("<era>", "<era2>"),
        Pattern.CASE_INSENSITIVE);
  }

  @Override
  public DateNormalizationResult extract(String inputValue, DateQualification requestedDateQualification,
      boolean allowSwitchMonthDay) {
    Matcher m = patYyyy.matcher(inputValue);
    if (m.matches()) {
      final InstantEdtfDateBuilder instantEdtfDateBuilder;
      if (bcAbbreviations.contains(m.group("era").toLowerCase())) {
        instantEdtfDateBuilder = new InstantEdtfDateBuilder(-Integer.parseInt(m.group("year")));
      } else {
        instantEdtfDateBuilder = new InstantEdtfDateBuilder(Integer.parseInt(m.group("year")));
      }
      return new DateNormalizationResult(DateNormalizationExtractorMatchId.BC_AD, inputValue,
          instantEdtfDateBuilder.withDateQualification(requestedDateQualification).build(allowSwitchMonthDay));
    }
    m = patRange.matcher(inputValue);
    if (m.matches()) {
      final InstantEdtfDateBuilder startDatePartBuilder;
      if (isBc(m.group("era"))) {
        startDatePartBuilder = new InstantEdtfDateBuilder(-Integer.parseInt(m.group("year")));
      } else {
        startDatePartBuilder = new InstantEdtfDateBuilder(Integer.parseInt(m.group("year")));
      }
      InstantEdtfDate start = startDatePartBuilder.withDateQualification(requestedDateQualification).build(allowSwitchMonthDay);

      final InstantEdtfDateBuilder endDatePartBuilder;
      if (isBc(m.group("era2"))) {
        endDatePartBuilder = new InstantEdtfDateBuilder(-Integer.parseInt(m.group("year2")));
      } else {
        endDatePartBuilder = new InstantEdtfDateBuilder(Integer.parseInt(m.group("year2")));
      }
      InstantEdtfDate end = endDatePartBuilder.withDateQualification(requestedDateQualification).build(allowSwitchMonthDay);

      return new DateNormalizationResult(DateNormalizationExtractorMatchId.BC_AD, inputValue, new IntervalEdtfDate(start, end));
    }
    return null;
  }

  private boolean isBc(String abbreviation) {
    for (Pattern pat : bcAbbreviationsPatterns) {
      if (pat.matcher(abbreviation).matches()) {
        return true;
      }
    }
    return false;
  }
}
