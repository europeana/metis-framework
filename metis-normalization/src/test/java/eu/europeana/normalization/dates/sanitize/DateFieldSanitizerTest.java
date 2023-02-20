package eu.europeana.normalization.dates.sanitize;

import static eu.europeana.normalization.dates.sanitize.SanitizeOperation.CAPTURE_VALUE_IN_PARENTHESES;
import static eu.europeana.normalization.dates.sanitize.SanitizeOperation.CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA;
import static eu.europeana.normalization.dates.sanitize.SanitizeOperation.CAPTURE_VALUE_IN_SQUARE_BRACKETS;
import static eu.europeana.normalization.dates.sanitize.SanitizeOperation.CAPTURE_VALUE_IN_SQUARE_BRACKETS_WITH_CIRCA;
import static eu.europeana.normalization.dates.sanitize.SanitizeOperation.ENDING_CLOSING_SQUARE_BRACKET;
import static eu.europeana.normalization.dates.sanitize.SanitizeOperation.ENDING_DOT;
import static eu.europeana.normalization.dates.sanitize.SanitizeOperation.ENDING_PARENTHESES;
import static eu.europeana.normalization.dates.sanitize.SanitizeOperation.ENDING_SQUARE_BRACKETS;
import static eu.europeana.normalization.dates.sanitize.SanitizeOperation.STARTING_CIRCA;
import static eu.europeana.normalization.dates.sanitize.SanitizeOperation.STARTING_PARENTHESES;
import static eu.europeana.normalization.dates.sanitize.SanitizeOperation.STARTING_TEXT_UNTIL_FIRST_COLON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.params.provider.Arguments.of;

import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DateFieldSanitizerTest {

  private static final DateFieldSanitizer DATE_FIELD_SANITIZER = new DateFieldSanitizer();

  @ParameterizedTest
  @MethodSource
  void sanitize1stTimeDateProperty(SanitizeOperation expectedSanitizeOperation, String input, String expectedResult) {
    assertCleaner(DATE_FIELD_SANITIZER::sanitize1stTimeDateProperty, expectedSanitizeOperation, input, expectedResult);
  }

  private static Stream<Arguments> sanitize1stTimeDateProperty() {
    return Stream.of(
        of(STARTING_TEXT_UNTIL_FIRST_COLON, "textA:textB", "textB"),
        of(STARTING_TEXT_UNTIL_FIRST_COLON, "textA: ", null),
        of(STARTING_TEXT_UNTIL_FIRST_COLON, "   :textB", "textB"),

        of(STARTING_PARENTHESES, "(textA)textB", "textB"),
        of(STARTING_PARENTHESES, "( textA )textB", "textB"),
        of(STARTING_PARENTHESES, "(textA) textB", "textB"),
        of(STARTING_PARENTHESES, "(textA)(textB)textC", "textC"),
        of(STARTING_PARENTHESES, "(textA)(textB)", null),

        of(ENDING_PARENTHESES, "text(1942-1943)", "text"),
        of(ENDING_PARENTHESES, "text(1942-1943)(1950)", "text"),
        of(ENDING_PARENTHESES, "text( 1942-1943 )", "text"),
        of(ENDING_PARENTHESES, "text( 1942-1943)", "text"),
        of(ENDING_PARENTHESES, "text(1942-1943 )", "text"),
        of(ENDING_PARENTHESES, "text(1942-1943 ) (1928)", "text"),

        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS_WITH_CIRCA, "[circa 2000]", "2000"),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS_WITH_CIRCA, "[circa AD]", "AD"),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS_WITH_CIRCA, "[ca AD]", "AD"),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS_WITH_CIRCA, "[ca xyz]", "xyz"),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS_WITH_CIRCA, "[ca. 123]", "123"),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS_WITH_CIRCA, "[c. 123]", "123"),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS_WITH_CIRCA, "[circa ad][c. 123]", "ad123"),

        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS, "textA[1942-1943]textB", "textA1942-1943textB"),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS, "[textA]-[textB]", "textA-textB"),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS, "text[1942-1943][1950]", "text1942-19431950"),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS, "text[ 1942-1943 ]", "text 1942-1943 "),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS, "text[ 1942-1943]", "text 1942-1943"),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS, "text[1942-1943 ]", "text1942-1943 "),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS, "text[1942-1943 ] [1928]", "text1942-1943  1928"),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS, "[textA] [textB]", "textA textB"),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS, " [textA]", "textA"),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS, " [textA][textB]", "textAtextB"),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS, " [textA][ textB]", "textA textB"),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS, " [textA][ textB ]", "textA textB "),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS, " [textA][ [textB] ]", "textA [textB ]"),

        of(STARTING_CIRCA, "circa 2000", "2000"),
        of(STARTING_CIRCA, "ca 2000", "2000"),
        of(STARTING_CIRCA, "c 2000", "2000"),
        of(STARTING_CIRCA, "ca. 2000", "2000"),
        of(STARTING_CIRCA, "ca.2000", "2000"),
        of(STARTING_CIRCA, "c. 2000", "2000"),
        of(STARTING_CIRCA, "c.2000", "2000"),
        of(STARTING_CIRCA, " c 2000", "2000"),
        of(STARTING_CIRCA, " ca 2000", "2000"),
        of(STARTING_CIRCA, " circa 2000", "2000"),

        of(ENDING_CLOSING_SQUARE_BRACKET, "text ]", "text"),
        of(ENDING_CLOSING_SQUARE_BRACKET, "textA]textB ]", "textA]textB"),

        of(ENDING_DOT, "text.", "text"),
        of(ENDING_DOT, "text...", "text.."),
        of(ENDING_DOT, ".text...", ".text.."),

        //Cases where the capture should fail
        of(STARTING_TEXT_UNTIL_FIRST_COLON, "textA", null),
        //Empty results means failure
        of(STARTING_TEXT_UNTIL_FIRST_COLON, "textA:", null),
        of(ENDING_PARENTHESES, "(1942-1943", null),
        //Empty result means failure
        of(ENDING_PARENTHESES, "(1942-1943)", null),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS_WITH_CIRCA, "[circa2000", null),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS, "1942-1943", null),
        of(STARTING_CIRCA, "circa2000", null),
        of(ENDING_CLOSING_SQUARE_BRACKET, "no bracket", null),
        of(ENDING_DOT, "text", null),
        //Empty result means failure
        of(ENDING_DOT, ".", null),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS, "text[[1942-1943]]", "text[1942-1943]"),
        of(CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "(circa 2000)", null),
        of(CAPTURE_VALUE_IN_PARENTHESES, "(1942-1943)", null)
    );
  }

  @ParameterizedTest
  @MethodSource
  void sanitize2ndTimeDateProperty(SanitizeOperation expectedSanitizeOperation, String input, String expectedResult) {
    assertCleaner(DATE_FIELD_SANITIZER::sanitize2ndTimeDateProperty, expectedSanitizeOperation, input, expectedResult);
  }

  private static Stream<Arguments> sanitize2ndTimeDateProperty() {
    return Stream.of(
        of(ENDING_SQUARE_BRACKETS, "text[1942-1943]", "text"),
        of(CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "(circa 2000)", "2000"),
        of(CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "(circa AD)", "AD"),
        of(CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "(ca AD)", "AD"),
        of(CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "(ca xyz)", "xyz"),
        of(CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "(ca. 123)", "123"),
        of(CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "(c. 123)", "123"),
        of(CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "(circa ad)(c. 123)", "ad)(c. 123"),

        of(CAPTURE_VALUE_IN_PARENTHESES, "(1942-1943)", "1942-1943"),
        of(CAPTURE_VALUE_IN_PARENTHESES, "(textA) (textB)", "textA) (textB"),
        of(CAPTURE_VALUE_IN_PARENTHESES, " (textA)", "textA"),
        of(CAPTURE_VALUE_IN_PARENTHESES, " (textA)(textB)", "textA)(textB"),
        of(CAPTURE_VALUE_IN_PARENTHESES, " (textA)( textB)", "textA)( textB"),
        of(CAPTURE_VALUE_IN_PARENTHESES, " (textA)( textB )", "textA)( textB "),

        //Cases where the capture should fail
        of(ENDING_SQUARE_BRACKETS, "text[1942-1943textB", null),
        of(CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "(circa2000", null),
        of(CAPTURE_VALUE_IN_PARENTHESES, "no parenthesis", null)
    );
  }

  /**
   * Test to achieve a second sanitization after the first sanitization has failed or produced some result for the second
   * sanitization.
   *
   * @param expectedSanitizeOperation the expected "final"(2nd) sanitization operation
   * @param input the input
   * @param expectedResult expected result
   */
  @ParameterizedTest
  @MethodSource
  void sanitize1stAnd2ndTimeDateProperty(SanitizeOperation expectedSanitizeOperation, String input, String expectedResult) {
    final SanitizedDate sanitize1stResult = DATE_FIELD_SANITIZER.sanitize1stTimeDateProperty(input);
    assertCleaner(DATE_FIELD_SANITIZER::sanitize2ndTimeDateProperty, expectedSanitizeOperation,
        sanitize1stResult == null ? input : sanitize1stResult.getSanitizedDateString(), expectedResult);
  }

  private static Stream<Arguments> sanitize1stAnd2ndTimeDateProperty() {
    return Stream.of(
        //1st sanitization = text[[1942-1943]]] -> text[1942-1943]] | 2nd sanitization = text[1942-1943]] -> text
        of(ENDING_SQUARE_BRACKETS, "textA[[textB]]]", "textA"),
        of(ENDING_SQUARE_BRACKETS, "textA:1500[textB]", "1500"),
        of(ENDING_SQUARE_BRACKETS, "(1500textB)2000[textC]", "2000"),
        of(ENDING_SQUARE_BRACKETS, "(1500-1720)(2001-2020)2000[textC]", "2000"),
        of(ENDING_SQUARE_BRACKETS, "(circa 1720)circa 2000[textC]", "circa 2000"),
        of(ENDING_SQUARE_BRACKETS, "(circa 1720)2700[info]2000[textC]", "2700"),

        //1st sanitization fails because STARTING_PARENTHESES, ENDING_PARENTHESES give empty result, so it moves to the 2nd sanitization
        of(CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "(circa 2000)", "2000"),
        of(CAPTURE_VALUE_IN_PARENTHESES, "(textA)", "textA"),
        //1st sanitization = textA:(textB) -> (textB) | 2nd sanitization = (textB) -> textB
        of(CAPTURE_VALUE_IN_PARENTHESES, "textA:(textB)", "textB"),
        //1st sanitization = (circa [circa 2000]) -> (circa 2000) | 2nd sanitization = (circa 2000) -> 2000
        of(CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "(circa [circa 2000])", "2000"),
        //1st sanitization = (textA-[textB]-textC) -> (textA-textB-textC) | 2nd sanitization = (textA-textB-textC) -> textA-textB-textC
        of(CAPTURE_VALUE_IN_PARENTHESES, "(textA-[textB]-textC)", "textA-textB-textC"),

        //Cases where the capture should fail
        //1st sanitization = circa 2000 -> 2000 2nd sanitization will not match because of Matcher.matches
        of(null, "circa 2000", null),
        //1st sanitization = text ] -> text 2nd sanitization will not match because of Matcher.matches
        of(null, "text ]", null),
        //1st sanitization = text . -> text 2nd sanitization will not match because of Matcher.matches
        of(null, "text.", null),
        of(ENDING_SQUARE_BRACKETS, "text[1942-1943textB", null),
        //Must miss closing parenthesis otherwise is captured from next operation
        of(CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "(circa2000", null),
        of(CAPTURE_VALUE_IN_PARENTHESES, "no parenthesis", null)
    );
  }


  @ParameterizedTest
  @MethodSource
  void cleanGenericProperty(SanitizeOperation expectedSanitizeOperation, String input, String expectedResult) {
    assertCleaner(DATE_FIELD_SANITIZER::sanitizeGenericProperty, expectedSanitizeOperation, input, expectedResult);
  }

  private static Stream<Arguments> cleanGenericProperty() {
    return Stream.of(
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS_WITH_CIRCA, "[circa 2000]", "2000"),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS_WITH_CIRCA, "[circa AD]", "AD"),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS_WITH_CIRCA, "[ca AD]", "AD"),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS_WITH_CIRCA, "[ca xyz]", "xyz"),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS_WITH_CIRCA, "[ca. 123]", "123"),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS_WITH_CIRCA, "[c. 123]", "123"),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS_WITH_CIRCA, "[circa ad][c. 123]", "ad123"),

        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS, "textA[1942-1943]textB", "textA1942-1943textB"),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS, "text[1942-1943][1950]", "text1942-19431950"),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS, "text[ 1942-1943 ]", "text 1942-1943 "),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS, "text[ 1942-1943]", "text 1942-1943"),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS, "text[1942-1943 ]", "text1942-1943 "),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS, "text[1942-1943 ] [1928]", "text1942-1943  1928"),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS, "[textA]-[textB]", "textA-textB"),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS, "[textA] [textB]", "textA textB"),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS, " [textA]", "textA"),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS, " [textA][textB]", "textAtextB"),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS, " [textA][ textB]", "textA textB"),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS, " [textA][ textB ]", "textA textB "),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS, " [textA][ [textB] ]", "textA [textB ]"),

        of(STARTING_CIRCA, "circa 2000", "2000"),
        of(STARTING_CIRCA, "ca 2000", "2000"),
        of(STARTING_CIRCA, "c 2000", "2000"),
        of(STARTING_CIRCA, "ca. 2000", "2000"),
        of(STARTING_CIRCA, "ca.2000", "2000"),
        of(STARTING_CIRCA, "c. 2000", "2000"),
        of(STARTING_CIRCA, "c.2000", "2000"),
        of(STARTING_CIRCA, " c 2000", "2000"),
        of(STARTING_CIRCA, " ca 2000", "2000"),
        of(STARTING_CIRCA, " circa 2000", "2000"),

        of(ENDING_PARENTHESES, "text(1942-1943)", "text"),

        //Cases where the capture should fail
        //Must miss closing bracket otherwise is captured from next operation
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS_WITH_CIRCA, "[circa2000", null),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS, "textA[1942-1943textB", null),
        of(STARTING_CIRCA, "circa2000", null),
        //Empty result means failure
        of(ENDING_PARENTHESES, "(1942-1943)", null),
        of(ENDING_PARENTHESES, "(1942-1943", null)
    );
  }

  private void assertCleaner(Function<String, SanitizedDate> sanitizeFunction, SanitizeOperation expectedSanitizeOperation,
      String input, String expectedResult) {
    final SanitizedDate sanitizedDate = sanitizeFunction.apply(input);
    if (expectedResult == null) {
      assertNull(sanitizedDate);
    } else {
      assertEquals(expectedResult, sanitizedDate.getSanitizedDateString());
      assertEquals(expectedSanitizeOperation, sanitizedDate.getSanitizeOperation());
    }
  }
}