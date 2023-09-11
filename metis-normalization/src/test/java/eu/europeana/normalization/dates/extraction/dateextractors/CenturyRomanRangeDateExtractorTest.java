package eu.europeana.normalization.dates.extraction.dateextractors;

import static eu.europeana.normalization.dates.edtf.DateQualification.NO_QUALIFICATION;
import static org.junit.jupiter.params.provider.Arguments.of;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CenturyRomanRangeDateExtractorTest implements DateExtractorTest {

  private static final CenturyRomanRangeDateExtractor ROMAN_CENTURY_RANGE_DATE_EXTRACTOR = new CenturyRomanRangeDateExtractor();

  void assertExtract(String input, String expected) {
    final DateNormalizationResult dateNormalizationResult = ROMAN_CENTURY_RANGE_DATE_EXTRACTOR.extractDateProperty(input,
        NO_QUALIFICATION);
    assertDateNormalizationResult(dateNormalizationResult, expected, DateNormalizationExtractorMatchId.CENTURY_RANGE_ROMAN);
  }

  @ParameterizedTest
  @MethodSource
  void extractRoman(String input, String expected) {
    assertExtract(input, expected);
  }

  private static Stream<Arguments> extractRoman() {
    return Stream.of(
        //Uppercase
        of("I-II", "00XX/01XX"),
        of("II-III", "01XX/02XX"),
        of("III-IV", "02XX/03XX"),
        of("IV-V", "03XX/04XX"),
        of("V-VI", "04XX/05XX"),
        of("VI-VII", "05XX/06XX"),
        of("VII-VIII", "06XX/07XX"),
        of("VIII-IX", "07XX/08XX"),
        of("IX-X", "08XX/09XX"),
        of("X-XI", "09XX/10XX"),
        of("XI-XII", "10XX/11XX"),
        of("XII-XIII", "11XX/12XX"),
        of("XIII-XIV", "12XX/13XX"),
        of("XIV-XV", "13XX/14XX"),
        of("XV-XVI", "14XX/15XX"),
        of("XVI-XVII", "15XX/16XX"),
        of("XVII-XVIII", "16XX/17XX"),
        of("XVIII-XIX", "17XX/18XX"),
        of("XIX-XX", "18XX/19XX"),
        of("XX-XXI", "19XX/20XX"),

        //Lowercase
        of("i-ii", "00XX/01XX"),
        of("ii-iii", "01XX/02XX"),
        of("iii-iv", "02XX/03XX"),
        of("iv-v", "03XX/04XX"),
        of("v-vi", "04XX/05XX"),
        of("vi-vii", "05XX/06XX"),
        of("vii-viii", "06XX/07XX"),
        of("viii-ix", "07XX/08XX"),
        of("ix-x", "08XX/09XX"),
        of("x-xi", "09XX/10XX"),
        of("xi-xii", "10XX/11XX"),
        of("xii-xiii", "11XX/12XX"),
        of("xiii-xiv", "12XX/13XX"),
        of("xiv-xv", "13XX/14XX"),
        of("xv-xvi", "14XX/15XX"),
        of("xvi-xvii", "15XX/16XX"),
        of("xvii-xviii", "16XX/17XX"),
        of("xviii-xix", "17XX/18XX"),
        of("xix-xx", "18XX/19XX"),
        of("xx-xxi", "19XX/20XX"),

        //Prefixes
        of("s I-II", "00XX/01XX"),
        of("S I-II", "00XX/01XX"),
        of("s. I-II", "00XX/01XX"),
        of("S. I-II", "00XX/01XX"),
        of("sec.IV-VII", "03XX/06XX"),
        of("SEC.IV-VII", "03XX/06XX"),
        of("sec. IV-VII", "03XX/06XX"),
        of("SEC. IV-VII", "03XX/06XX"),
        of("saec.VII-XVIII", "06XX/17XX"),
        of("SAEC.VII-XVIII", "06XX/17XX"),
        of("saec. XVI-XVIII", "15XX/17XX"),
        of("SAEC. XVI-XVIII", "15XX/17XX"),

        //Other possibilities and uncertain
        of("s I-iI", "00XX/01XX"),
        of("  s I-II  ", "00XX/01XX"),
        of("?saec.X-XVIII", "09XX?/17XX"),
        of("X-XVIII?", "09XX/17XX?"),
        of("?saec.X-XVIII?", "09XX?/17XX?"),

        //Non matches
        of("S. XIIII-XIIIV", null, null), //Invalid roman
        of("S. XVIII-", null, null, null), //Open-ended incorrect
        of("sII-V", null, null), //Without a dot a space is required
        of("secVI-XVII", null, null), //Without a dot a space is required
        of("saecX-XVIII?", null, null) //Without a dot a space is required
    );
  }

}