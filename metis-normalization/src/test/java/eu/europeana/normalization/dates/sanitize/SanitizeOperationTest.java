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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.of;

import java.util.regex.Matcher;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SanitizeOperationTest {

  @ParameterizedTest
  @MethodSource("extractData")
  void extract(SanitizeOperation sanitizeOperation, String input, String expectedResult) {
    final Matcher matcher = sanitizeOperation.getSanitizePattern().matcher(input);
    final boolean matches = sanitizeOperation.getMatchingCheck().test(matcher);
    if (expectedResult == null) {
      assertFalse(matches);
    } else {
      assertTrue(matches);
      final String result = sanitizeOperation.getReplaceOperation().apply(matcher);
      assertEquals(expectedResult, result);
      if (StringUtils.isNotEmpty(expectedResult)) {
        assertTrue(sanitizeOperation.getIsOperationSuccessful().test(result));
      }
    }
  }

  private static Stream<Arguments> extractData() {
    return Stream.of(
        //STARTING_TEXT_UNTIL_FIRST_COLON
        of(STARTING_TEXT_UNTIL_FIRST_COLON, "textA:textB", "textB"),
        of(STARTING_TEXT_UNTIL_FIRST_COLON, "textA:textB:textC", "textB:textC"),
        of(STARTING_TEXT_UNTIL_FIRST_COLON, "textA:", ""),
        of(STARTING_TEXT_UNTIL_FIRST_COLON, "textA", null),
        of(STARTING_TEXT_UNTIL_FIRST_COLON, "textA: textB", "textB"),
        of(STARTING_TEXT_UNTIL_FIRST_COLON, "   :textB", "textB"),

        //STARTING_PARENTHESES
        of(STARTING_PARENTHESES, "(1942-1943)text", "text"),
        of(STARTING_PARENTHESES, "(1942-1943)", ""),
        of(STARTING_PARENTHESES, "1942-1943)", null),
        of(STARTING_PARENTHESES, "((1942-1943))text", "text"),
        of(STARTING_PARENTHESES, "(((1942-1943))text", "text"),
        of(STARTING_PARENTHESES, "((1942-1943)))text", "text"),
        of(STARTING_PARENTHESES, "((1942-1943)))textA)textB", "textB"),
        of(STARTING_PARENTHESES, "(1942-1943) text", "text"),

        //ENDING_PARENTHESES
        of(ENDING_PARENTHESES, "text(1942-1943)", "text"),
        of(ENDING_PARENTHESES, "(1942-1943)", ""),
        of(ENDING_PARENTHESES, "(1942-1943", null),
        of(ENDING_PARENTHESES, "text((1942-1943))", "text"),
        of(ENDING_PARENTHESES, "text(((1942-1943))", "text"),
        of(ENDING_PARENTHESES, "text((1942-1943)))", "text"),
        of(ENDING_PARENTHESES, "text (1942-1943)", "text"),

        //ENDING_SQUARE_BRACKETS
        of(ENDING_SQUARE_BRACKETS, "text[1942-1943]", "text"),
        of(ENDING_SQUARE_BRACKETS, "[1942-1943]", ""),
        of(ENDING_SQUARE_BRACKETS, "[1942-1943", null),
        of(ENDING_SQUARE_BRACKETS, "text[[1942-1943]]", "text"),
        of(ENDING_SQUARE_BRACKETS, "text[[[1942-1943]]", "text"),
        of(ENDING_SQUARE_BRACKETS, "text[[1942-1943]]]", "text"),
        of(ENDING_SQUARE_BRACKETS, "text [1942-1943]", "text"),
        of(ENDING_SQUARE_BRACKETS, "textA textB[1942-1943]", "textA textB"),

        //ENDING_DOT
        of(ENDING_DOT, "text.", "text"),
        of(ENDING_DOT, ".", ""),
        of(ENDING_DOT, "text .", "text"),
        of(ENDING_DOT, "text", null),

        //SQUARE_BRACKETS
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS, "textA[1942-1943]textB", "textA1942-1943textB"),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS, "textA[[1942-1943]]textB", "textA[1942-1943]textB"),
        //We don't capture nested brackets
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS, "[[[I-V]]]", "[[I-V]]"),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS, "textA[[1942-1943]]]textB", "textA[1942-1943]]textB"),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS, "[I-V]]", "I-V]"),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS, "[I-V]", "I-V"),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS, "[I-V]textA[V-X]", "I-VtextAV-X"),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS, "textA[1942-1943textB", null),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS, "textA1942-1943]textB", null),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS, "1942-1943", null),

        //STARTING_CIRCA
        of(STARTING_CIRCA, "circa 2000", "2000"),
        of(STARTING_CIRCA, "ca.2000", "2000"),
        of(STARTING_CIRCA, "ca. 2000", "2000"),
        of(STARTING_CIRCA, "c.2000", "2000"),
        of(STARTING_CIRCA, "c. 2000", "2000"),
        of(STARTING_CIRCA, "CIRCA 2000", "2000"),
        of(STARTING_CIRCA, "CA.2000", "2000"),
        of(STARTING_CIRCA, "CA. 2000", "2000"),
        of(STARTING_CIRCA, "C.2000", "2000"),
        of(STARTING_CIRCA, "C. 2000", "2000"),
        of(STARTING_CIRCA, "circa2000", null),
        of(STARTING_CIRCA, "CIRCA2000", null),
        of(STARTING_CIRCA, "CA2000", null),
        of(STARTING_CIRCA, "ca2000", null),
        of(STARTING_CIRCA, "C2000", null),
        of(STARTING_CIRCA, "c2000", null),
        of(STARTING_CIRCA, "nocirca", null),

        //STARTING_SQUARE_BRACKETS_WITH_CIRCA
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS_WITH_CIRCA, "[circa 2000]", "2000"),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS_WITH_CIRCA, "[ca.2000]", "2000"),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS_WITH_CIRCA, "[ca. 2000]", "2000"),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS_WITH_CIRCA, "[c.2000]", "2000"),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS_WITH_CIRCA, "[c. 2000]", "2000"),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS_WITH_CIRCA, "[c. 2000] text", "2000 text"),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS_WITH_CIRCA, "[CIRCA 2000]", "2000"),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS_WITH_CIRCA, "[CA.2000]", "2000"),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS_WITH_CIRCA, "[CA. 2000]", "2000"),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS_WITH_CIRCA, "[CA. 2000 text]", "2000 text"),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS_WITH_CIRCA, "[C.2000]", "2000"),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS_WITH_CIRCA, "[C. 2000]", "2000"),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS_WITH_CIRCA, "[C. 2000] text", "2000 text"),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS_WITH_CIRCA, "text [C. 2000]", "text 2000"),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS_WITH_CIRCA, "[[C. 2000]]", "[2000]"),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS_WITH_CIRCA, "[[C. 2000]]]", "[2000]]"),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS_WITH_CIRCA, "[C. (2000)]", "(2000)"),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS_WITH_CIRCA, "[c. 2000", null),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS_WITH_CIRCA, "[circa2000]", null),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS_WITH_CIRCA, "[CIRCA2000]", null),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS_WITH_CIRCA, "[ca2000]", null),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS_WITH_CIRCA, "[CA2000]", null),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS_WITH_CIRCA, "[c2000]", null),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS_WITH_CIRCA, "[C2000]", null),
        of(CAPTURE_VALUE_IN_SQUARE_BRACKETS_WITH_CIRCA, "[nocirca]", null),

        //CLOSING_SQUARE_BRACKET
        of(ENDING_CLOSING_SQUARE_BRACKET, "]", ""),
        of(ENDING_CLOSING_SQUARE_BRACKET, "text ]", "text"),
        of(ENDING_CLOSING_SQUARE_BRACKET, "textA ] textB", null),
        of(ENDING_CLOSING_SQUARE_BRACKET, "no bracket", null),

        //CAPTURE_VALUE_IN_PARENTHESES
        of(CAPTURE_VALUE_IN_PARENTHESES, "(1942-1943)", "1942-1943"),
        of(CAPTURE_VALUE_IN_PARENTHESES, " (text)", "text"),
        of(CAPTURE_VALUE_IN_PARENTHESES, "(text) ", "text"),
        of(CAPTURE_VALUE_IN_PARENTHESES, " (text) ", "text"),
        of(CAPTURE_VALUE_IN_PARENTHESES, "(circa text) ", "circa text"),
        of(CAPTURE_VALUE_IN_PARENTHESES, "(textA) (textB)", "textA) (textB"),
        of(CAPTURE_VALUE_IN_PARENTHESES, "((text)) ", "(text)"),
        of(CAPTURE_VALUE_IN_PARENTHESES, "(text)) ", "text)"),
        of(CAPTURE_VALUE_IN_PARENTHESES, "no parenthesis", null),

        //CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA
        of(CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "(circa 2000)", "2000"),
        of(CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "(ca.2000)", "2000"),
        of(CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "(ca. 2000)", "2000"),
        of(CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "(c.2000)", "2000"),
        of(CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "(c. 2000)", "2000"),
        of(CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "(c. 2000) ", "2000"),
        of(CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "(CIRCA 2000)", "2000"),
        of(CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "(CA.2000)", "2000"),
        of(CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "(CA. 2000)", "2000"),
        of(CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "(C.2000)", "2000"),
        of(CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "(C. 2000)", "2000"),
        of(CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "(C. 2000) ", "2000"),
        of(CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, " (C. 2000)", "2000"),
        of(CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "(C. (2000))", "(2000)"),
        of(CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "(C. (2000))", "(2000)"),
        of(CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "((C. 2000))", null),
        of(CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "((C. 2000)))", null),
        of(CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "(c. 2000", null),
        of(CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "(circa2000)", null),
        of(CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "(CIRCA2000)", null),
        of(CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "(ca2000)", null),
        of(CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "(CA2000)", null),
        of(CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "(c2000)", null),
        of(CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "(C2000)", null),
        of(CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "(nocirca)", null)
    );
  }
}
