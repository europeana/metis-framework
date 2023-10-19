package eu.europeana.normalization.dates.extraction.extractors;

import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.BC_AD;
import static org.junit.jupiter.params.provider.Arguments.of;

import eu.europeana.normalization.dates.DateNormalizationResult;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class BcAdDateExtractorTest implements DateExtractorTest {

  private static final BcAdDateExtractor PATTERN_BC_AD_DATE_EXTRACTOR = new BcAdDateExtractor();

  @ParameterizedTest
  @MethodSource
  void extract(String input, String expected) {
    assertExtract(input, expected);
  }

  void assertExtract(String input, String expected) {
    final DateNormalizationResult dateNormalizationResult = PATTERN_BC_AD_DATE_EXTRACTOR.extractDateProperty(input);
    assertDateNormalizationResult(dateNormalizationResult, expected, BC_AD);
  }

  private static Stream<Arguments> extract() {
    return Stream.of(
        //Bulgarian
        of("1989 пр.Хр.", "-1988"),
        of("1989 сл.Хр.", "1989"),
        //Croatian
        of("1989 pr. Kr.", "-1988"),
        of("1989 po. Kr.", "1989"),
        //Czech
        of("1989 př. n. l.", "-1988"),
        of("1989 n. l.", "1989"),
        //Danish
        of("1989 f.Kr.", "-1988"),
        of("1989 e.Kr.", "1989"),
        //Dutch
        of("1989 v.Chr.", "-1988"),
        of("1989 n.Chr.", "1989"),
        //English
        of("1989 BC", "-1988"),
        of("1989 AD", "1989"),
        //Estonian
        of("1989 eKr", "-1988"),
        of("1989 pKr", "1989"),
        //Finnish
        of("1989 eKr.", "-1988"),
        of("1989 jKr.", "1989"),
        //French
        of("1989 av. J.-C.", "-1988"),
        of("1989 ap. J.-C.", "1989"),
        //German
        of("1989 v. Chr.", "-1988"),
        of("1989 n. Chr.", "1989"),
        //Greek
        of("1989 π.Χ.", "-1988"),
        of("1989 μ.Χ.", "1989"),
        //Hungarian
        of("1989 i. e.", "-1988"),
        of("1989 i. sz.", "1989"),
        //Irish
        of("1989 RC", "-1988"),
        of("1989 AD", "1989"),
        //Italian
        of("1989 a.C.", "-1988"),
        of("1989 d.C.", "1989"),
        //Latvian
        of("1989 p.m.ē.", "-1988"),
        of("1989 m.ē.", "1989"),
        //Lithuanian
        of("1989 pr. Kr.", "-1988"),
        of("1989 po Kr.", "1989"),
        //Maltese
        of("1989 QK", "-1988"),
        of("1989 WK", "1989"),
        //Polish
        of("1989 p.n.e.", "-1988"),
        of("1989 n.e.", "1989"),
        //Portuguese
        of("1989 a.C.", "-1988"),
        of("1989 d.C.", "1989"),
        //Romanian
        of("1989 î.Hr.", "-1988"),
        of("1989 d.Hr.", "1989"),
        //Slovak
        of("1989 pred Kr.", "-1988"),
        of("1989 po Kr.", "1989"),
        //Slovenian
        of("1989 pr. Kr.", "-1988"),
        of("1989 po Kr.", "1989"),
        //Spanish
        of("1989 a. C.", "-1988"),
        of("1989 d. C.", "1989"),
        //Swedish
        of("1989 f.Kr.", "-1988"),
        of("1989 e.Kr.", "1989"),

        //Less digits
        of("198 AD", "0198"),
        of("19 AD", "0019"),

        //First years
        of("1 AD", "0001"),
        of("1 BC", "0000"),
        of("2 BC", "-0001"),

        //Invalids
        of("0 BC", null),
        of("-1989 BC", null),
        of("-1989 AD", null)
    );
  }
}