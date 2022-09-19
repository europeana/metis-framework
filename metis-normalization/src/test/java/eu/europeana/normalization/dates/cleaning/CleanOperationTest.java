package eu.europeana.normalization.dates.cleaning;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Matcher;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CleanOperationTest {

  @ParameterizedTest
  @MethodSource("extractData")
  void extract(CleanOperation cleanOperation, String input, String expectedResult) {
    final Matcher matcher = cleanOperation.getCleanPattern().matcher(input);
    final boolean matches = cleanOperation.getMatchingCheck().test(matcher);
    if (expectedResult == null) {
      assertFalse(matches);
    } else {
      assertTrue(matches);
      final String result = cleanOperation.getReplaceOperation().apply(matcher);
      assertEquals(expectedResult, result);
      if (StringUtils.isNotEmpty(expectedResult)) {
        assertTrue(cleanOperation.getIsOperationSuccessful().test(result));
      }
    }
  }

  private static Stream<Arguments> extractData() {
    return Stream.of(
        //INITIAL_TEXT_A
        Arguments.of(CleanOperation.STARTING_TEXT_UNTIL_FIRST_COLON, "textA:textB", "textB"),
        Arguments.of(CleanOperation.STARTING_TEXT_UNTIL_FIRST_COLON, "textA:textB:textC", "textB:textC"),
        Arguments.of(CleanOperation.STARTING_TEXT_UNTIL_FIRST_COLON, "textA:", ""),
        Arguments.of(CleanOperation.STARTING_TEXT_UNTIL_FIRST_COLON, "textA", null),
        Arguments.of(CleanOperation.STARTING_TEXT_UNTIL_FIRST_COLON, "textA: textB", "textB"),

        //INITIAL_TEXT_B
        Arguments.of(CleanOperation.STARTING_PARENTHESES, "(1942-1943)text", "text"),
        Arguments.of(CleanOperation.STARTING_PARENTHESES, "(1942-1943)", ""),
        Arguments.of(CleanOperation.STARTING_PARENTHESES, "1942-1943)", null),
        Arguments.of(CleanOperation.STARTING_PARENTHESES, "((1942-1943))text", "text"),
        Arguments.of(CleanOperation.STARTING_PARENTHESES, "(((1942-1943))text", "text"),
        Arguments.of(CleanOperation.STARTING_PARENTHESES, "((1942-1943)))text", "text"),
        Arguments.of(CleanOperation.STARTING_PARENTHESES, "((1942-1943)))textA)textB", "textB"),
        Arguments.of(CleanOperation.STARTING_PARENTHESES, "(1942-1943) text", "text"),

        //ENDING_TEXT
        Arguments.of(CleanOperation.ENDING_PARENTHESES, "text(1942-1943)", "text"),
        Arguments.of(CleanOperation.ENDING_PARENTHESES, "(1942-1943)", ""),
        Arguments.of(CleanOperation.ENDING_PARENTHESES, "(1942-1943", null),
        Arguments.of(CleanOperation.ENDING_PARENTHESES, "text((1942-1943))", "text"),
        Arguments.of(CleanOperation.ENDING_PARENTHESES, "text(((1942-1943))", "text"),
        Arguments.of(CleanOperation.ENDING_PARENTHESES, "text((1942-1943)))", "text"),
        Arguments.of(CleanOperation.ENDING_PARENTHESES, "text (1942-1943)", "text"),

        //ENDING_TEXT_SQUARE_BRACKETS
        Arguments.of(CleanOperation.ENDING_SQUARE_BRACKETS, "text[1942-1943]", "text"),
        Arguments.of(CleanOperation.ENDING_SQUARE_BRACKETS, "[1942-1943]", ""),
        Arguments.of(CleanOperation.ENDING_SQUARE_BRACKETS, "[1942-1943", null),
        Arguments.of(CleanOperation.ENDING_SQUARE_BRACKETS, "text[[1942-1943]]", "text"),
        Arguments.of(CleanOperation.ENDING_SQUARE_BRACKETS, "text[[[1942-1943]]", "text"),
        Arguments.of(CleanOperation.ENDING_SQUARE_BRACKETS, "text[[1942-1943]]]", "text"),
        Arguments.of(CleanOperation.ENDING_SQUARE_BRACKETS, "text [1942-1943]", "text"),
        Arguments.of(CleanOperation.ENDING_SQUARE_BRACKETS, "textA textB[1942-1943]", "textA textB"),

        //ENDING_DOT
        Arguments.of(CleanOperation.ENDING_DOT, "text.", "text"),
        Arguments.of(CleanOperation.ENDING_DOT, ".", ""),
        Arguments.of(CleanOperation.ENDING_DOT, "text .", "text"),
        Arguments.of(CleanOperation.ENDING_DOT, "text", null),

        //SQUARE_BRACKETS
        Arguments.of(CleanOperation.SQUARE_BRACKETS, "textA[1942-1943]textB", "textA1942-1943textB"),
        Arguments.of(CleanOperation.SQUARE_BRACKETS, "textA[[1942-1943]]textB", "textA[1942-1943]textB"),
        //We don't capture nested brackets
        Arguments.of(CleanOperation.SQUARE_BRACKETS, "[[[I-V]]]", "[[I-V]]"),
        Arguments.of(CleanOperation.SQUARE_BRACKETS, "textA[[1942-1943]]]textB", "textA[1942-1943]]textB"),
        Arguments.of(CleanOperation.SQUARE_BRACKETS, "[I-V]]", "I-V]"),
        Arguments.of(CleanOperation.SQUARE_BRACKETS, "[I-V]", "I-V"),
        Arguments.of(CleanOperation.SQUARE_BRACKETS, "[I-V]textA[V-X]", "I-VtextAV-X"),
        Arguments.of(CleanOperation.SQUARE_BRACKETS, "textA[1942-1943textB", null),
        Arguments.of(CleanOperation.SQUARE_BRACKETS, "1942-1943", null),

        //CIRCA
        Arguments.of(CleanOperation.STARTING_CIRCA, "circa 2000", "2000"),
        Arguments.of(CleanOperation.STARTING_CIRCA, "ca.2000", "2000"),
        Arguments.of(CleanOperation.STARTING_CIRCA, "ca. 2000", "2000"),
        Arguments.of(CleanOperation.STARTING_CIRCA, "c.2000", "2000"),
        Arguments.of(CleanOperation.STARTING_CIRCA, "c. 2000", "2000"),
        Arguments.of(CleanOperation.STARTING_CIRCA, "CIRCA 2000", "2000"),
        Arguments.of(CleanOperation.STARTING_CIRCA, "CA.2000", "2000"),
        Arguments.of(CleanOperation.STARTING_CIRCA, "CA. 2000", "2000"),
        Arguments.of(CleanOperation.STARTING_CIRCA, "C.2000", "2000"),
        Arguments.of(CleanOperation.STARTING_CIRCA, "C. 2000", "2000"),
        Arguments.of(CleanOperation.STARTING_CIRCA, "circa2000", null),
        Arguments.of(CleanOperation.STARTING_CIRCA, "CIRCA2000", null),
        Arguments.of(CleanOperation.STARTING_CIRCA, "CA2000", null),
        Arguments.of(CleanOperation.STARTING_CIRCA, "ca2000", null),
        Arguments.of(CleanOperation.STARTING_CIRCA, "C2000", null),
        Arguments.of(CleanOperation.STARTING_CIRCA, "c2000", null),
        Arguments.of(CleanOperation.STARTING_CIRCA, "nocirca", null),

        //SQUARE_BRACKETS_AND_CIRCA
        Arguments.of(CleanOperation.STARTING_SQUARE_BRACKETS_WITH_CIRCA, "[circa 2000]", "2000"),
        Arguments.of(CleanOperation.STARTING_SQUARE_BRACKETS_WITH_CIRCA, "[ca.2000]", "2000"),
        Arguments.of(CleanOperation.STARTING_SQUARE_BRACKETS_WITH_CIRCA, "[ca. 2000]", "2000"),
        Arguments.of(CleanOperation.STARTING_SQUARE_BRACKETS_WITH_CIRCA, "[c.2000]", "2000"),
        Arguments.of(CleanOperation.STARTING_SQUARE_BRACKETS_WITH_CIRCA, "[c. 2000]", "2000"),
        Arguments.of(CleanOperation.STARTING_SQUARE_BRACKETS_WITH_CIRCA, "[c. 2000] text", "2000 text"),
        Arguments.of(CleanOperation.STARTING_SQUARE_BRACKETS_WITH_CIRCA, "[CIRCA 2000]", "2000"),
        Arguments.of(CleanOperation.STARTING_SQUARE_BRACKETS_WITH_CIRCA, "[CA.2000]", "2000"),
        Arguments.of(CleanOperation.STARTING_SQUARE_BRACKETS_WITH_CIRCA, "[CA. 2000]", "2000"),
        Arguments.of(CleanOperation.STARTING_SQUARE_BRACKETS_WITH_CIRCA, "[CA. 2000 text]", "2000 text"),
        Arguments.of(CleanOperation.STARTING_SQUARE_BRACKETS_WITH_CIRCA, "[C.2000]", "2000"),
        Arguments.of(CleanOperation.STARTING_SQUARE_BRACKETS_WITH_CIRCA, "[C. 2000]", "2000"),
        Arguments.of(CleanOperation.STARTING_SQUARE_BRACKETS_WITH_CIRCA, "[C. 2000] text", "2000 text"),
        Arguments.of(CleanOperation.STARTING_SQUARE_BRACKETS_WITH_CIRCA, "text [C. 2000]", "text 2000"),
        Arguments.of(CleanOperation.STARTING_SQUARE_BRACKETS_WITH_CIRCA, "[[C. 2000]]", "[2000]"),
        Arguments.of(CleanOperation.STARTING_SQUARE_BRACKETS_WITH_CIRCA, "[[C. 2000]]]", "[2000]]"),
        Arguments.of(CleanOperation.STARTING_SQUARE_BRACKETS_WITH_CIRCA, "[c. 2000", null),
        Arguments.of(CleanOperation.STARTING_SQUARE_BRACKETS_WITH_CIRCA, "[circa2000]", null),
        Arguments.of(CleanOperation.STARTING_SQUARE_BRACKETS_WITH_CIRCA, "[CIRCA2000]", null),
        Arguments.of(CleanOperation.STARTING_SQUARE_BRACKETS_WITH_CIRCA, "[ca2000]", null),
        Arguments.of(CleanOperation.STARTING_SQUARE_BRACKETS_WITH_CIRCA, "[CA2000]", null),
        Arguments.of(CleanOperation.STARTING_SQUARE_BRACKETS_WITH_CIRCA, "[c2000]", null),
        Arguments.of(CleanOperation.STARTING_SQUARE_BRACKETS_WITH_CIRCA, "[C2000]", null),
        Arguments.of(CleanOperation.STARTING_SQUARE_BRACKETS_WITH_CIRCA, "[nocirca]", null),

        //SQUARE_BRACKET_END
        Arguments.of(CleanOperation.CLOSING_SQUARE_BRACKET, "]", ""),
        Arguments.of(CleanOperation.CLOSING_SQUARE_BRACKET, "text ]", "text"),
        Arguments.of(CleanOperation.CLOSING_SQUARE_BRACKET, "textA ] textB", null),
        Arguments.of(CleanOperation.CLOSING_SQUARE_BRACKET, "no bracket", null),

        //PARENTHESES_FULL_VALUE
        Arguments.of(CleanOperation.CAPTURE_VALUE_IN_PARENTHESES, "(1942-1943)", "1942-1943"),
        Arguments.of(CleanOperation.CAPTURE_VALUE_IN_PARENTHESES, " (text)", "text"),
        Arguments.of(CleanOperation.CAPTURE_VALUE_IN_PARENTHESES, "(text) ", "text"),
        Arguments.of(CleanOperation.CAPTURE_VALUE_IN_PARENTHESES, " (text) ", "text"),
        Arguments.of(CleanOperation.CAPTURE_VALUE_IN_PARENTHESES, "(circa text) ", "circa text"),
        Arguments.of(CleanOperation.CAPTURE_VALUE_IN_PARENTHESES, "(textA) (textB)", "textA) (textB"),
        Arguments.of(CleanOperation.CAPTURE_VALUE_IN_PARENTHESES, "((text)) ", "(text)"),
        Arguments.of(CleanOperation.CAPTURE_VALUE_IN_PARENTHESES, "(text)) ", "text)"),
        Arguments.of(CleanOperation.CAPTURE_VALUE_IN_PARENTHESES, "no parenthesis", null),

        //PARENTHESES_FULL_VALUE_AND_CIRCA
        Arguments.of(CleanOperation.CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "(circa 2000)", "2000"),
        Arguments.of(CleanOperation.CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "(ca.2000)", "2000"),
        Arguments.of(CleanOperation.CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "(ca. 2000)", "2000"),
        Arguments.of(CleanOperation.CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "(c.2000)", "2000"),
        Arguments.of(CleanOperation.CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "(c. 2000)", "2000"),
        Arguments.of(CleanOperation.CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "(c. 2000) ", "2000"),
        Arguments.of(CleanOperation.CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "(CIRCA 2000)", "2000"),
        Arguments.of(CleanOperation.CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "(CA.2000)", "2000"),
        Arguments.of(CleanOperation.CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "(CA. 2000)", "2000"),
        Arguments.of(CleanOperation.CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "(C.2000)", "2000"),
        Arguments.of(CleanOperation.CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "(C. 2000)", "2000"),
        Arguments.of(CleanOperation.CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "(C. 2000) ", "2000"),
        Arguments.of(CleanOperation.CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, " (C. 2000)", "2000"),
        Arguments.of(CleanOperation.CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "(C. (2000))", "(2000)"),
        Arguments.of(CleanOperation.CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "((C. 2000))", null),
        Arguments.of(CleanOperation.CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "((C. 2000)))", null),
        Arguments.of(CleanOperation.CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "(c. 2000", null),
        Arguments.of(CleanOperation.CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "(circa2000)", null),
        Arguments.of(CleanOperation.CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "(CIRCA2000)", null),
        Arguments.of(CleanOperation.CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "(ca2000)", null),
        Arguments.of(CleanOperation.CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "(CA2000)", null),
        Arguments.of(CleanOperation.CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "(c2000)", null),
        Arguments.of(CleanOperation.CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "(C2000)", null),
        Arguments.of(CleanOperation.CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA, "(nocirca)", null)
    );
  }
}
