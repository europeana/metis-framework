package eu.europeana.indexing.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;

public class SetUtilsTest {

  /*
   * We test this by generating all strings that satisfy the following conditions:
   * 
   * 1. It starts with the symbol ?
   * 
   * 2. After that it may contain one digit (1, 2 or 3)
   * 
   * 3. After that it may contain one lower case letter (a or b)
   * 
   * 4. After that it may contain one upper case letter (only X)
   */
  @Test
  public void testGenerateCombinations() {

    // Create sets of options
    final Set<Character> allowedDigits = Stream.of('1', '2', '3').collect(Collectors.toSet());
    final Set<Character> allowedlowerCase = Stream.of('a', 'b').collect(Collectors.toSet());
    final Set<Character> allowedUpperCase = Collections.singleton('X');
    final List<Set<Character>> options =
        Arrays.asList(allowedDigits, allowedlowerCase, allowedUpperCase);

    // Generate the combinations.
    final Set<String> result =
        SetUtils.generateCombinations(options, "?", (word, letter) -> word + letter);

    // Test the total size (all generated combinations should be different)
    int expectedSize = 1;
    for (Set<Character> option : options) {
      expectedSize *= option.size() + 1;
    }
    assertEquals(expectedSize, result.size());

    // Test that all strings match the specifications.
    final Pattern pattern = Pattern.compile("\\A\\?[123]?[ab]?X?\\Z");
    for (String resultString : result) {
      assertTrue(pattern.matcher(resultString).matches());
    }
  }
}
