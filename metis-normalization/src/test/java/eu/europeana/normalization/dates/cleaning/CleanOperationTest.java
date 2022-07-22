package eu.europeana.normalization.dates.cleaning;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CleanOperationTest {


    @Test
    @Disabled
    void testInitialTextARegex_expectMatch(){
        //TODO Is this a good example for this?
        assertTrue(CleanOperation.INITIAL_TEXT_A.getCleanPattern().matcher("  textASample:  ").matches());
    }

    @Test
    void testInitialTextARegex_expectNoMatch(){
        assertFalse(CleanOperation.INITIAL_TEXT_A.getCleanPattern().matcher("1952-02-25T00:00:00Z-1952-02-25T23:59:59Z").matches());
    }

    @Test
    @Disabled
    void testInitialTextBRegex_expectMatch(){
        //TODO Isn't this the same as PARENTHESES_FULL_VALUE?
        assertTrue(CleanOperation.INITIAL_TEXT_B.getCleanPattern().matcher("(1942-1943)").matches());
    }

    @Test
    void testInitialTextBRegex_expectNoMatch(){
        assertFalse(CleanOperation.INITIAL_TEXT_B.getCleanPattern().matcher("1889 (Herstellung) )").matches());
    }

    @Test
    void testEndingTextRegex_expectMatch(){
        assertTrue(CleanOperation.ENDING_TEXT.getCleanPattern().matcher("  (Herstellung (Werk))  ").matches());
    }

    @Test
    void testEndingTextRegex_expectNoMatch(){
        assertFalse(CleanOperation.ENDING_TEXT.getCleanPattern().matcher("1749 (Herstellung) )").matches());
    }

    @Test
    void testEndingTextSquareBracketsRegex_expectMatch(){
        assertTrue(CleanOperation.ENDING_TEXT_SQUARE_BRACKETS.getCleanPattern().matcher("[17__]").matches());
    }

    @Test
    void testEndingTextSquareBracketsRegex_expectNoMatch(){
        assertFalse(CleanOperation.ENDING_TEXT_SQUARE_BRACKETS.getCleanPattern().matcher("1939 [1942?]").matches());
    }

    @Test
    @Disabled
    void testEndingDotRegex_expectMatch(){
        //TODO: Is it only supposed to be a dot?
        assertTrue(CleanOperation.ENDING_DOT.getCleanPattern().matcher("  .  ").matches());
    }

    @Test
    @Disabled
    void testEndingDotRegex_expectNoMatch(){
        //TODO: Isn't this supposed to be valid?
        assertFalse(CleanOperation.ENDING_DOT.getCleanPattern().matcher("S. XVI-XVIII").matches());
    }

    @Test
    void testSquareBracketsRegex_expectMatch(){
        assertTrue(CleanOperation.SQUARE_BRACKETS.getCleanPattern().matcher("[XVI-XIX]").matches());
    }

    @Test
    void testSquareBracketsRegex_expectNoMatch(){
        assertFalse(CleanOperation.SQUARE_BRACKETS.getCleanPattern().matcher("[a]]  ").matches());
    }

    @Test
    @Disabled
    void testCircaRegex_expectMatch(){
        //TODO: Aren't there supposed to be values after circa and other?
        assertTrue(CleanOperation.CIRCA.getCleanPattern().matcher("circa ").matches());
        assertTrue(CleanOperation.CIRCA.getCleanPattern().matcher("ca. ").matches());
        assertTrue(CleanOperation.CIRCA.getCleanPattern().matcher("c. ").matches());
    }

    @Test
    @Disabled
    void testCircaRegex_expectNoMatch(){
        //TODO: Aren't there supposed to be values after circa and other?
        assertFalse(CleanOperation.CIRCA.getCleanPattern().matcher(" circa 1234").matches());
        assertFalse(CleanOperation.CIRCA.getCleanPattern().matcher(" ca 1234").matches());
        assertFalse(CleanOperation.CIRCA.getCleanPattern().matcher(" c. 1234").matches());
    }

    @Test
    void testSquareBracketsAndCircaRegex_expectMatch(){
        assertTrue(CleanOperation.SQUARE_BRACKETS_AND_CIRCA.getCleanPattern().matcher("[circa 1946]").matches());
        assertTrue(CleanOperation.SQUARE_BRACKETS_AND_CIRCA.getCleanPattern().matcher("[ca. 1946]").matches());
        assertTrue(CleanOperation.SQUARE_BRACKETS_AND_CIRCA.getCleanPattern().matcher("[c. 1946]").matches());
    }

    @Test
    void testSquareBracketsAndCircaRegex_expectNoMatch(){
        assertFalse(CleanOperation.SQUARE_BRACKETS_AND_CIRCA.getCleanPattern().matcher("circa 1234").matches());
        assertFalse(CleanOperation.SQUARE_BRACKETS_AND_CIRCA.getCleanPattern().matcher("ca. 1234").matches());
        assertFalse(CleanOperation.SQUARE_BRACKETS_AND_CIRCA.getCleanPattern().matcher("c. 1234").matches());
    }

    @Test
    @Disabled
    void testSquareBracketEndRegex_expectMatch(){
        //TODO: Is it only the bracket?
        assertTrue(CleanOperation.SQUARE_BRACKET_END.getCleanPattern().matcher("  ]").matches());
    }

    @Test
    void testSquareBracketEndRegex_expectNoMatch(){
        assertFalse(CleanOperation.SQUARE_BRACKET_END.getCleanPattern().matcher("[abc  ").matches());
    }

    @Test
    void testParenthesesFullValueRegex_expectMatch(){
        assertTrue(CleanOperation.PARENTHESES_FULL_VALUE.getCleanPattern().matcher("(1942-1943)").matches());
        assertTrue(CleanOperation.PARENTHESES_FULL_VALUE.getCleanPattern().matcher("(circa 1942-1943)").matches());
    }

    @Test
    void testParenthesesFullValueRegex_expectNoMatch(){
        assertFalse(CleanOperation.PARENTHESES_FULL_VALUE.getCleanPattern().matcher("1942-1943").matches());
    }

    @Test
    @Disabled
    void testParenthesesFullValueAndCircaRegex_expectMatch(){
        assertTrue(CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA.getCleanPattern().matcher("(circa 1942-1943)").matches());
        //TODO: Does it only accept ca and c in capitals?
        assertTrue(CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA.getCleanPattern().matcher("(CA. 1942-1943)").matches());
        assertTrue(CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA.getCleanPattern().matcher("(C. 1942-1943)").matches());
    }

    @Test
    void testParenthesesFullValueAndCircaRegex_expectNoMatch(){
        assertFalse(CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA.getCleanPattern().matcher("circa 1942-1943").matches());
        assertFalse(CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA.getCleanPattern().matcher("ca. 1942-1943").matches());
        assertFalse(CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA.getCleanPattern().matcher("c. 1942-1943").matches());
    }
}
