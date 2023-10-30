package eu.europeana.normalization.dates.extraction.extractors;

import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.BC_AD;
import static org.junit.jupiter.params.provider.Arguments.of;

import eu.europeana.normalization.dates.DateNormalizationResult;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class BcAdRangeDateExtractorTest implements DateExtractorTest {

  private static final BcAdRangeDateExtractor BC_AD_RANGE_DATE_EXTRACTOR = new BcAdRangeDateExtractor();

  @ParameterizedTest
  @MethodSource
  void extract(String input, String expected) {
    assertExtract(input, expected);
  }

  void assertExtract(String input, String expected) {
    final DateNormalizationResult dateNormalizationResult = BC_AD_RANGE_DATE_EXTRACTOR.extractDateProperty(input);
    assertDateNormalizationResult(dateNormalizationResult, expected, BC_AD);
  }

  private static Stream<Arguments> extract() {
    return Stream.of(
        //BC-BC
        of("1990 BC-1989 BC", "-1989/-1988"),
        of("1990 BC/1989 BC", "-1989/-1988"),
        of("1990 BC - 1989 BC", "-1989/-1988"),
        of("1990 BC / 1989 BC", "-1989/-1988"),
        of("1990 BC-1 BC", "-1989/0000"),

        //BC-BC(Greek)
        of("1990 π.Χ.-1989 π.Χ.", "-1989/-1988"),
        of("1990 π.Χ./1989 π.Χ.", "-1989/-1988"),
        of("1990 π.Χ. - 1989 π.Χ.", "-1989/-1988"),
        of("1990 π.Χ. / 1989 π.Χ.", "-1989/-1988"),
        of("1990 π.Χ.-1 π.Χ.", "-1989/0000"),

        //AD-AD
        of("1989 AD-1990 AD", "1989/1990"),
        of("1989 AD/1990 AD", "1989/1990"),
        of("1989 AD - 1990 AD", "1989/1990"),
        of("1989 AD / 1990 AD", "1989/1990"),

        //AD-AD(Greek)
        of("1989 μ.Χ.-1990 μ.Χ.", "1989/1990"),
        of("1989 μ.Χ./1990 μ.Χ.", "1989/1990"),
        of("1989 μ.Χ. - 1990 μ.Χ.", "1989/1990"),
        of("1989 μ.Χ. / 1990 μ.Χ.", "1989/1990"),

        //BC-AD
        of("1989 π.Χ.-1989 μ.Χ.", "-1988/1989"),
        of("1989 π.Χ.-1 μ.Χ.", "-1988/0001"),

        //Invalids
        of("1990 BC//1989 BC", null),
        of("-1990 BC-1989 BC", null),
        of("-1990 BC--1989 BC", null),
        of("1990 BC , 1989 BC", null),
        of("1989 BC-0 BC", null),
        of("1989 BC-0 AD", null)
    );
  }
}