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
        Arguments.of(CleanOperation.INITIAL_TEXT_A, "textA:textB", "textB"),
        Arguments.of(CleanOperation.INITIAL_TEXT_A, "textA:textB:textC", "textB:textC"),
        Arguments.of(CleanOperation.INITIAL_TEXT_A, "textA:", ""),
        Arguments.of(CleanOperation.INITIAL_TEXT_A, "textA", null),
        Arguments.of(CleanOperation.INITIAL_TEXT_A, "textA: textB", "textB"),

        //INITIAL_TEXT_B
        Arguments.of(CleanOperation.INITIAL_TEXT_B, "(1942-1943)text", "text"),
        Arguments.of(CleanOperation.INITIAL_TEXT_B, "(1942-1943)", ""),
        Arguments.of(CleanOperation.INITIAL_TEXT_B, "1942-1943)", null),
        Arguments.of(CleanOperation.INITIAL_TEXT_B, "((1942-1943))text", "text"),
        Arguments.of(CleanOperation.INITIAL_TEXT_B, "(((1942-1943))text", "text"),
        Arguments.of(CleanOperation.INITIAL_TEXT_B, "((1942-1943)))text", "text"),
        Arguments.of(CleanOperation.INITIAL_TEXT_B, "((1942-1943)))textA)textB", "textB"),
        Arguments.of(CleanOperation.INITIAL_TEXT_B, "(1942-1943) text", "text"),

        //ENDING_TEXT
        Arguments.of(CleanOperation.ENDING_TEXT, "text(1942-1943)", "text"),
        Arguments.of(CleanOperation.ENDING_TEXT, "(1942-1943)", ""),
        Arguments.of(CleanOperation.ENDING_TEXT, "(1942-1943", null),
        Arguments.of(CleanOperation.ENDING_TEXT, "text((1942-1943))", "text"),
        Arguments.of(CleanOperation.ENDING_TEXT, "text(((1942-1943))", "text"),
        Arguments.of(CleanOperation.ENDING_TEXT, "text((1942-1943)))", "text"),
        Arguments.of(CleanOperation.ENDING_TEXT, "text (1942-1943)", "text"),

        //ENDING_TEXT_SQUARE_BRACKETS
        Arguments.of(CleanOperation.ENDING_TEXT_SQUARE_BRACKETS, "text[1942-1943]", "text"),
        Arguments.of(CleanOperation.ENDING_TEXT_SQUARE_BRACKETS, "[1942-1943]", ""),
        Arguments.of(CleanOperation.ENDING_TEXT_SQUARE_BRACKETS, "[1942-1943", null),
        Arguments.of(CleanOperation.ENDING_TEXT_SQUARE_BRACKETS, "text[[1942-1943]]", "text"),
        Arguments.of(CleanOperation.ENDING_TEXT_SQUARE_BRACKETS, "text[[[1942-1943]]", "text"),
        Arguments.of(CleanOperation.ENDING_TEXT_SQUARE_BRACKETS, "text[[1942-1943]]]", "text"),
        Arguments.of(CleanOperation.ENDING_TEXT_SQUARE_BRACKETS, "text [1942-1943]", "text"),
        Arguments.of(CleanOperation.ENDING_TEXT_SQUARE_BRACKETS, "textA textB[1942-1943]", "textA textB"),

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
        Arguments.of(CleanOperation.CIRCA, "circa 2000", "2000"),
        Arguments.of(CleanOperation.CIRCA, "ca.2000", "2000"),
        Arguments.of(CleanOperation.CIRCA, "ca. 2000", "2000"),
        Arguments.of(CleanOperation.CIRCA, "c.2000", "2000"),
        Arguments.of(CleanOperation.CIRCA, "c. 2000", "2000"),
        Arguments.of(CleanOperation.CIRCA, "CIRCA 2000", "2000"),
        Arguments.of(CleanOperation.CIRCA, "CA.2000", "2000"),
        Arguments.of(CleanOperation.CIRCA, "CA. 2000", "2000"),
        Arguments.of(CleanOperation.CIRCA, "C.2000", "2000"),
        Arguments.of(CleanOperation.CIRCA, "C. 2000", "2000"),
        Arguments.of(CleanOperation.CIRCA, "circa2000", null),
        Arguments.of(CleanOperation.CIRCA, "CIRCA2000", null),
        Arguments.of(CleanOperation.CIRCA, "CA2000", null),
        Arguments.of(CleanOperation.CIRCA, "ca2000", null),
        Arguments.of(CleanOperation.CIRCA, "C2000", null),
        Arguments.of(CleanOperation.CIRCA, "c2000", null),
        Arguments.of(CleanOperation.CIRCA, "nocirca", null),

        //SQUARE_BRACKETS_AND_CIRCA
        Arguments.of(CleanOperation.SQUARE_BRACKETS_AND_CIRCA, "[circa 2000]", "2000"),
        Arguments.of(CleanOperation.SQUARE_BRACKETS_AND_CIRCA, "[ca.2000]", "2000"),
        Arguments.of(CleanOperation.SQUARE_BRACKETS_AND_CIRCA, "[ca. 2000]", "2000"),
        Arguments.of(CleanOperation.SQUARE_BRACKETS_AND_CIRCA, "[c.2000]", "2000"),
        Arguments.of(CleanOperation.SQUARE_BRACKETS_AND_CIRCA, "[c. 2000]", "2000"),
        Arguments.of(CleanOperation.SQUARE_BRACKETS_AND_CIRCA, "[c. 2000] text", "2000 text"),
        Arguments.of(CleanOperation.SQUARE_BRACKETS_AND_CIRCA, "[CIRCA 2000]", "2000"),
        Arguments.of(CleanOperation.SQUARE_BRACKETS_AND_CIRCA, "[CA.2000]", "2000"),
        Arguments.of(CleanOperation.SQUARE_BRACKETS_AND_CIRCA, "[CA. 2000]", "2000"),
        Arguments.of(CleanOperation.SQUARE_BRACKETS_AND_CIRCA, "[CA. 2000 text]", "2000 text"),
        Arguments.of(CleanOperation.SQUARE_BRACKETS_AND_CIRCA, "[C.2000]", "2000"),
        Arguments.of(CleanOperation.SQUARE_BRACKETS_AND_CIRCA, "[C. 2000]", "2000"),
        Arguments.of(CleanOperation.SQUARE_BRACKETS_AND_CIRCA, "[C. 2000] text", "2000 text"),
        Arguments.of(CleanOperation.SQUARE_BRACKETS_AND_CIRCA, "text [C. 2000]", "text 2000"),
        Arguments.of(CleanOperation.SQUARE_BRACKETS_AND_CIRCA, "[[C. 2000]]", "[2000]"),
        Arguments.of(CleanOperation.SQUARE_BRACKETS_AND_CIRCA, "[[C. 2000]]]", "[2000]]"),
        Arguments.of(CleanOperation.SQUARE_BRACKETS_AND_CIRCA, "[c. 2000", null),
        Arguments.of(CleanOperation.SQUARE_BRACKETS_AND_CIRCA, "[circa2000]", null),
        Arguments.of(CleanOperation.SQUARE_BRACKETS_AND_CIRCA, "[CIRCA2000]", null),
        Arguments.of(CleanOperation.SQUARE_BRACKETS_AND_CIRCA, "[ca2000]", null),
        Arguments.of(CleanOperation.SQUARE_BRACKETS_AND_CIRCA, "[CA2000]", null),
        Arguments.of(CleanOperation.SQUARE_BRACKETS_AND_CIRCA, "[c2000]", null),
        Arguments.of(CleanOperation.SQUARE_BRACKETS_AND_CIRCA, "[C2000]", null),
        Arguments.of(CleanOperation.SQUARE_BRACKETS_AND_CIRCA, "[nocirca]", null),

        //SQUARE_BRACKET_END
        Arguments.of(CleanOperation.SQUARE_BRACKET_END, "]", ""),
        Arguments.of(CleanOperation.SQUARE_BRACKET_END, "text ]", "text"),
        Arguments.of(CleanOperation.SQUARE_BRACKET_END, "textA ] textB", null),
        Arguments.of(CleanOperation.SQUARE_BRACKET_END, "no bracket", null),

        //PARENTHESES_FULL_VALUE
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE, "(1942-1943)", "1942-1943"),
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE, " (text)", "text"),
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE, "(text) ", "text"),
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE, " (text) ", "text"),
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE, "(circa text) ", "circa text"),
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE, "(textA) (textB)", "textA) (textB"),
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE, "((text)) ", "(text)"),
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE, "(text)) ", "text)"),
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE, "no parenthesis", null),

        //PARENTHESES_FULL_VALUE_AND_CIRCA
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA, "(circa 2000)", "2000"),
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA, "(ca.2000)", "2000"),
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA, "(ca. 2000)", "2000"),
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA, "(c.2000)", "2000"),
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA, "(c. 2000)", "2000"),
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA, "(c. 2000) ", "2000"),
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA, "(CIRCA 2000)", "2000"),
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA, "(CA.2000)", "2000"),
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA, "(CA. 2000)", "2000"),
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA, "(C.2000)", "2000"),
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA, "(C. 2000)", "2000"),
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA, "(C. 2000) ", "2000"),
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA, " (C. 2000)", "2000"),
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA, "(C. (2000))", "(2000)"),
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA, "((C. 2000))", null),
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA, "((C. 2000)))", null),
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA, "(c. 2000", null),
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA, "(circa2000)", null),
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA, "(CIRCA2000)", null),
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA, "(ca2000)", null),
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA, "(CA2000)", null),
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA, "(c2000)", null),
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA, "(C2000)", null),
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA, "(nocirca)", null)
    );
  }
}
