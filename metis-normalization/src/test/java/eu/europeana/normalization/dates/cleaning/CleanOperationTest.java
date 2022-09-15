package eu.europeana.normalization.dates.cleaning;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Matcher;
import java.util.stream.Stream;
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
      assertEquals(expectedResult, cleanOperation.getReplaceOperation().apply(matcher));
    }
  }

  private static Stream<Arguments> extractData() {
    // TODO: 14/09/2022 Check if some of the matches commented are supposed to be matching or not?
    return Stream.of(
        //INITIAL_TEXT_A
        Arguments.of(CleanOperation.INITIAL_TEXT_A, "textA:textB", "textB"),
        Arguments.of(CleanOperation.INITIAL_TEXT_A, "textA", null),
        //Should this match with space at the end?? (I'm assuming this was to match multiple spaces and replace them. Is a single space okay though or the same applies?)
        Arguments.of(CleanOperation.INITIAL_TEXT_A, "textA: textB", "textB"),

        //INITIAL_TEXT_B
        Arguments.of(CleanOperation.INITIAL_TEXT_B, "(1942-1943)", ""),
        Arguments.of(CleanOperation.INITIAL_TEXT_B, "1942-1943)", null),
        //This seems incorrect?(It does not match the last closing parenthesis)
        Arguments.of(CleanOperation.INITIAL_TEXT_B, "((1942-1943))", ")"),
        Arguments.of(CleanOperation.INITIAL_TEXT_B, "(((1942-1943))", ")"),
        Arguments.of(CleanOperation.INITIAL_TEXT_B, "((1942-1943)))", "))"),
        //Should this match with space at the end??
        Arguments.of(CleanOperation.INITIAL_TEXT_B, "(1942-1943) ", ""),

        //ENDING_TEXT
        Arguments.of(CleanOperation.ENDING_TEXT, "(1942-1943)", ""),
        Arguments.of(CleanOperation.ENDING_TEXT, "(1942-1943", null),
        //This matches more if we compare it with the INITIAL_TEXT_B similar example. On the other hand the example just above with missing closing bracket does not match..
        Arguments.of(CleanOperation.ENDING_TEXT, "((1942-1943))", ""),
        Arguments.of(CleanOperation.ENDING_TEXT, "(((1942-1943))", ""),
        Arguments.of(CleanOperation.ENDING_TEXT, "((1942-1943)))", ""),
        //Should this match with space at the beginning??
        Arguments.of(CleanOperation.ENDING_TEXT, " (1942-1943)", ""),

        //ENDING_TEXT_SQUARE_BRACKETS
        Arguments.of(CleanOperation.ENDING_TEXT_SQUARE_BRACKETS, "[1942-1943]", ""),
        Arguments.of(CleanOperation.ENDING_TEXT_SQUARE_BRACKETS, "[1942-1943", null),
        //This matches more if we compare it with the INITIAL_TEXT_B similar example.
        Arguments.of(CleanOperation.ENDING_TEXT_SQUARE_BRACKETS, "[[1942-1943]]", ""),
        Arguments.of(CleanOperation.ENDING_TEXT_SQUARE_BRACKETS, "[[[1942-1943]]", ""),
        Arguments.of(CleanOperation.ENDING_TEXT_SQUARE_BRACKETS, "[[1942-1943]]]", ""),
        //Should this match with space at the beginning??
        Arguments.of(CleanOperation.ENDING_TEXT_SQUARE_BRACKETS, " [1942-1943]", ""),
        Arguments.of(CleanOperation.ENDING_TEXT_SQUARE_BRACKETS, " textA[1942-1943]", " textA"),

        //ENDING_DOT
        Arguments.of(CleanOperation.ENDING_DOT, "textA.", "textA"),
        Arguments.of(CleanOperation.ENDING_DOT, "textA .", "textA"),
        Arguments.of(CleanOperation.ENDING_DOT, "textA", null),

        //SQUARE_BRACKETS
        Arguments.of(CleanOperation.SQUARE_BRACKETS, "textA[1942-1943]textB", "textA1942-1943textB"),
        //This is a side effect. It matches [[1942-1943] but groups [1942-1943 and then replaces the match with the group resulting in [1942-1943]
        Arguments.of(CleanOperation.SQUARE_BRACKETS, "textA[[1942-1943]]textB", "textA[1942-1943]textB"),
        //Should it capture also nested brackets?
        Arguments.of(CleanOperation.SQUARE_BRACKETS, "[[[I-V]]]", "[[I-V]]"),
        //Is this okay?
        Arguments.of(CleanOperation.SQUARE_BRACKETS, "textA[[1942-1943]]]textB", "textA[1942-1943]]textB"),
        //Is this okay?
        Arguments.of(CleanOperation.SQUARE_BRACKETS, "[I-V]]", "I-V]"),
        Arguments.of(CleanOperation.SQUARE_BRACKETS, "[I-V]", "I-V"),
        Arguments.of(CleanOperation.SQUARE_BRACKETS, "[I-V]textA[V-X]", "I-VtextAV-X"),
        Arguments.of(CleanOperation.SQUARE_BRACKETS, "textA[1942-1943textB", null),
        Arguments.of(CleanOperation.SQUARE_BRACKETS, "1942-1943", null),

        //CIRCA
        //Some force the dot and some do not. What is the norm?
        Arguments.of(CleanOperation.CIRCA, "circa2000", "2000"),
        Arguments.of(CleanOperation.CIRCA, "circa 2000", "2000"),
        Arguments.of(CleanOperation.CIRCA, "ca.2000", "2000"),
        Arguments.of(CleanOperation.CIRCA, "ca. 2000", "2000"),
        Arguments.of(CleanOperation.CIRCA, "ca2000", "2000"),
        Arguments.of(CleanOperation.CIRCA, "c.2000", "2000"),
        Arguments.of(CleanOperation.CIRCA, "c. 2000", "2000"),
        Arguments.of(CleanOperation.CIRCA, "CIRCA2000", "2000"),
        Arguments.of(CleanOperation.CIRCA, "CIRCA 2000", "2000"),
        Arguments.of(CleanOperation.CIRCA, "CA.2000", "2000"),
        Arguments.of(CleanOperation.CIRCA, "CA. 2000", "2000"),
        Arguments.of(CleanOperation.CIRCA, "CA2000", "2000"),
        Arguments.of(CleanOperation.CIRCA, "C.2000", "2000"),
        Arguments.of(CleanOperation.CIRCA, "C. 2000", "2000"),
        Arguments.of(CleanOperation.CIRCA, "c2000", null),
        Arguments.of(CleanOperation.CIRCA, "nocirca", null),

        //SQUARE_BRACKETS_AND_CIRCA
        //Some force the dot and some do not. What is the norm?
        Arguments.of(CleanOperation.SQUARE_BRACKETS_AND_CIRCA, "[circa2000]", "2000"),
        Arguments.of(CleanOperation.SQUARE_BRACKETS_AND_CIRCA, "[circa 2000]", "2000"),
        Arguments.of(CleanOperation.SQUARE_BRACKETS_AND_CIRCA, "[ca.2000]", "2000"),
        Arguments.of(CleanOperation.SQUARE_BRACKETS_AND_CIRCA, "[ca. 2000]", "2000"),
        Arguments.of(CleanOperation.SQUARE_BRACKETS_AND_CIRCA, "[ca2000]", "2000"),
        Arguments.of(CleanOperation.SQUARE_BRACKETS_AND_CIRCA, "[c.2000]", "2000"),
        Arguments.of(CleanOperation.SQUARE_BRACKETS_AND_CIRCA, "[c. 2000]", "2000"),
        Arguments.of(CleanOperation.SQUARE_BRACKETS_AND_CIRCA, "[c. 2000] ", "2000 "), //End space not replaced
        Arguments.of(CleanOperation.SQUARE_BRACKETS_AND_CIRCA, "[CIRCA2000]", "2000"),
        Arguments.of(CleanOperation.SQUARE_BRACKETS_AND_CIRCA, "[CIRCA 2000]", "2000"),
        Arguments.of(CleanOperation.SQUARE_BRACKETS_AND_CIRCA, "[CA.2000]", "2000"),
        Arguments.of(CleanOperation.SQUARE_BRACKETS_AND_CIRCA, "[CA. 2000]", "2000"),
        Arguments.of(CleanOperation.SQUARE_BRACKETS_AND_CIRCA, "[CA2000]", "2000"),
        Arguments.of(CleanOperation.SQUARE_BRACKETS_AND_CIRCA, "[C.2000]", "2000"),
        Arguments.of(CleanOperation.SQUARE_BRACKETS_AND_CIRCA, "[C. 2000]", "2000"),
        Arguments.of(CleanOperation.SQUARE_BRACKETS_AND_CIRCA, "[C. 2000] ", "2000 "), //End space not replaced
        Arguments.of(CleanOperation.SQUARE_BRACKETS_AND_CIRCA, " [C. 2000]", " 2000"), //Start space not captured
        Arguments.of(CleanOperation.SQUARE_BRACKETS_AND_CIRCA, "[[C. 2000]]", "[2000]"), //Is this okay?
        Arguments.of(CleanOperation.SQUARE_BRACKETS_AND_CIRCA, "[[C. 2000]]]", "[2000]]"), //Is this okay?
        Arguments.of(CleanOperation.SQUARE_BRACKETS_AND_CIRCA, "[c. 2000", null),
        Arguments.of(CleanOperation.SQUARE_BRACKETS_AND_CIRCA, "[c2000]", null),
        Arguments.of(CleanOperation.SQUARE_BRACKETS_AND_CIRCA, "[C2000]", null),
        Arguments.of(CleanOperation.SQUARE_BRACKETS_AND_CIRCA, "[nocirca]", null),

        //SQUARE_BRACKET_END
        Arguments.of(CleanOperation.SQUARE_BRACKET_END, "]", ""),
        Arguments.of(CleanOperation.SQUARE_BRACKET_END, "text ]", "text"),
        Arguments.of(CleanOperation.SQUARE_BRACKET_END, "text ] ", null),
        Arguments.of(CleanOperation.SQUARE_BRACKET_END, "no bracket", null),

        //PARENTHESES_FULL_VALUE
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE, "(1942-1943)", "1942-1943"),
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE, " (text)", "text"),
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE, "(text) ", "text"),
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE, " (text) ", "text"),
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE, "(text) ", "text"),
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE, "(circa text) ", "circa text"),
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE, "((text)) ", null), //Nested parenthesis won't be captured
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE, "(text)) ", null), //Double parenthesis won't be captured
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE, "no parenthesis", null),

        //PARENTHESES_FULL_VALUE_AND_CIRCA (this was with case-sensitive and it was updated to case-insensitive)
        //Spaces are not replaced in some cases
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA, "(circa2000)", "2000"),
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA, "(circa 2000)", " 2000"), //captures space too
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA, "(ca.2000)", "2000"),
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA, "(ca. 2000)", " 2000"),
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA, "(ca2000)", "2000"),
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA, "(c.2000)", "2000"),
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA, "(c. 2000)", " 2000"),
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA, "(c. 2000) ", " 2000"), //End space replaced
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA, "(CIRCA2000)", "2000"),
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA, "(CIRCA 2000)", " 2000"),
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA, "(CA.2000)", "2000"),
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA, "(CA. 2000)", " 2000"),
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA, "(CA2000)", "2000"),
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA, "(C.2000)", "2000"),
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA, "(C. 2000)", " 2000"),
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA, "(C. 2000) ", " 2000"), //End space replaced
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA, " (C. 2000)", " 2000"), //Start space not captured
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA, "((C. 2000))", null), //Is this okay?
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA, "((C. 2000)))", null), //Is this okay?
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA, "(c. 2000", null),
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA, "(c2000)", null),
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA, "(C2000)", null),
        Arguments.of(CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA, "(nocirca)", null)
    );
  }
}
