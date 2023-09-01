package eu.europeana.normalization.dates.extraction.dateextractors;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.edtf.DateQualification;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import eu.europeana.normalization.dates.edtf.InstantEdtfDateBuilder;
import eu.europeana.normalization.dates.edtf.IntervalEdtfDateBuilder;
import eu.europeana.normalization.dates.extraction.DateExtractionException;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PatternBcAdRangeDateExtractor extends AbstractDateExtractor {

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

  Pattern patRange;

  public PatternBcAdRangeDateExtractor() {
    String patYearBcAd = "(?<year>\\d{2,4})\\s*(?<era>";
    patYearBcAd += bcAbbreviations.stream().collect(Collectors.joining("|"));
    patYearBcAd += adAbbreviations.stream().collect(Collectors.joining("|"));
    patYearBcAd = patYearBcAd.substring(0, patYearBcAd.length() - 1) + ")\\.?";

    patRange = Pattern.compile(
        patYearBcAd + "\\s*[\\-\\/]\\s*" + patYearBcAd.replace("<year>", "<year2>").replace("<era>", "<era2>"),
        Pattern.CASE_INSENSITIVE);
  }

  @Override
  public DateNormalizationResult extract(String inputValue, DateQualification requestedDateQualification,
      boolean flexibleDateBuild) throws DateExtractionException {
    Matcher m = patRange.matcher(inputValue);
    if (m.matches()) {
      final InstantEdtfDateBuilder startDatePartBuilder;
      if (isBc(m.group("era"))) {
        startDatePartBuilder = new InstantEdtfDateBuilder(-Integer.parseInt(m.group("year")));
      } else {
        startDatePartBuilder = new InstantEdtfDateBuilder(Integer.parseInt(m.group("year")));
      }
      InstantEdtfDate start = startDatePartBuilder.withDateQualification(requestedDateQualification)
                                                  .withFlexibleDateBuild(flexibleDateBuild).build();

      final InstantEdtfDateBuilder endDatePartBuilder;
      if (isBc(m.group("era2"))) {
        endDatePartBuilder = new InstantEdtfDateBuilder(-Integer.parseInt(m.group("year2")));
      } else {
        endDatePartBuilder = new InstantEdtfDateBuilder(Integer.parseInt(m.group("year2")));
      }
      InstantEdtfDate end = endDatePartBuilder.withDateQualification(requestedDateQualification)
                                              .withFlexibleDateBuild(flexibleDateBuild).build();

      return new DateNormalizationResult(DateNormalizationExtractorMatchId.BC_AD, inputValue,
          new IntervalEdtfDateBuilder(start, end).withFlexibleDateBuild(flexibleDateBuild).build());
    }
    return DateNormalizationResult.getNoMatchResult(inputValue);
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
