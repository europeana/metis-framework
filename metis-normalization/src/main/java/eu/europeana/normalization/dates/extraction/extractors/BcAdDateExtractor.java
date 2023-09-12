package eu.europeana.normalization.dates.extraction.extractors;

import static eu.europeana.normalization.dates.DateNormalizationResult.getNoMatchResult;
import static java.util.regex.Pattern.compile;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.edtf.InstantEdtfDateBuilder;
import eu.europeana.normalization.dates.extraction.DateExtractionException;
import eu.europeana.normalization.dates.extraction.EuropeanLanguage;
import java.text.DateFormatSymbols;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A year with an indication of the era in european languages
 * <p>
 * Some examples:
 *   <ul>
 *     <li>1989 BC</li>
 *     <li>1989 AD</li>
 *     <li>1989 π.Χ.</li>
 *     <li>1989 μ.Χ.</li>
 *   </ul>
 * </p>
 */
public class BcAdDateExtractor extends AbstractDateExtractor {

  private static final String YEAR_REGEX = "(\\d{1,4})";
  private static final String DELIMITERS_REGEX = " ";
  private static final Set<String> adAbbreviations = new HashSet<>();
  private static final Pattern pattern;

  static {
    final Set<String> bcAbbreviations = new HashSet<>();
    for (EuropeanLanguage europeanLanguage : EuropeanLanguage.values()) {
      final DateFormatSymbols symbols = DateFormatSymbols.getInstance(europeanLanguage.getLocale());
      bcAbbreviations.add(symbols.getEras()[0]);
      adAbbreviations.add(symbols.getEras()[1]);
    }
    final String abbreviationsJoinedValues = Stream.concat(bcAbbreviations.stream(), adAbbreviations.stream())
                                                   .map(Pattern::quote)
                                                   .collect(Collectors.joining("|", "(", ")"));
    pattern = compile(String.join(DELIMITERS_REGEX, YEAR_REGEX, abbreviationsJoinedValues), Pattern.CASE_INSENSITIVE);
  }

  @Override
  public DateNormalizationResult extract(String inputValue,
      boolean flexibleDateBuild) throws DateExtractionException {
    DateNormalizationResult dateNormalizationResult = getNoMatchResult(inputValue);

    Matcher matcher = pattern.matcher(inputValue);
    if (matcher.matches()) {
      final int year = Integer.parseInt(matcher.group(1));
      //Year should not be 0 on an era
      if (year != 0) {
        final boolean isAd = adAbbreviations.contains(matcher.group(2));
        int yearSign = isAd ? 1 : -1;
        int yearAdjusted = (isAd ? year : (year - 1)) * yearSign;
        InstantEdtfDateBuilder instantEdtfDateBuilder = new InstantEdtfDateBuilder(yearAdjusted);
        dateNormalizationResult = new DateNormalizationResult(DateNormalizationExtractorMatchId.BC_AD, inputValue,
            instantEdtfDateBuilder.build());
      }
    }
    return dateNormalizationResult;
  }
}
