package eu.europeana.indexing.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

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
  public void testGenerateOptionalCombinations() {

    // Create sets of options
    final Set<Character> allowedDigits = Stream.of('1', '2', '3').collect(Collectors.toSet());
    final Set<Character> allowedlowerCase = Stream.of('a', 'b').collect(Collectors.toSet());
    final Set<Character> allowedUpperCase = Collections.singleton('X');
    final List<Set<Character>> options =
            Arrays.asList(allowedDigits, allowedlowerCase, allowedUpperCase);

    // Generate the combinations.
    final Set<String> result = SetUtils
            .generateOptionalCombinations(options, "?", (word, letter) -> word + letter);

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

    // Verify that sending an empty list in addition to the others will not affect the result.
    final List<Set<Character>> optionsWithEmptySet = Arrays
            .asList(allowedDigits, allowedlowerCase, Collections.emptySet(), allowedUpperCase);
    final Set<String> resultWithEmptySet = SetUtils
            .generateOptionalCombinations(optionsWithEmptySet, "?", (word, letter) -> word + letter);
    assertEquals(result, resultWithEmptySet);
  }

  /*
   * We test this by generating all strings that satisfy the following conditions:
   *
   * 1. It starts with the symbol ?
   *
   * 2. After that it must contain one digit (1, 2 or 3)
   *
   * 3. After that it must contain one lower case letter (a or b)
   *
   * 4. After that it must contain one upper case letter (only X)
   */
  @Test
  public void testGenerateForcedCombinations() {

    // Create sets of options
    final Set<Character> allowedDigits = Stream.of('1', '2', '3').collect(Collectors.toSet());
    final Set<Character> allowedlowerCase = Stream.of('a', 'b').collect(Collectors.toSet());
    final Set<Character> allowedUpperCase = Collections.singleton('X');
    final List<Set<Character>> options =
            Arrays.asList(allowedDigits, allowedlowerCase, allowedUpperCase);

    // Generate the combinations.
    final Set<String> result = SetUtils
            .generateForcedCombinations(options, "?", (word, letter) -> word + letter);

    // Test the total size (all generated combinations should be different)
    int expectedSize = 1;
    for (Set<Character> option : options) {
      expectedSize *= option.size();
    }
    assertEquals(expectedSize, result.size());

    // Test that all strings match the specifications.
    final Pattern pattern = Pattern.compile("\\A\\?[123][ab]X\\Z");
    for (String resultString : result) {
      assertTrue(pattern.matcher(resultString).matches());
    }

    // Verify that sending an empty list in addition to the others will create in an empty result.
    final List<Set<Character>> optionsWithEmptySet = Arrays
            .asList(allowedDigits, allowedlowerCase, Collections.emptySet(), allowedUpperCase);
    final Set<String> resultWithEmptySet = SetUtils
            .generateForcedCombinations(optionsWithEmptySet, "?", (word, letter) -> word + letter);
    assertTrue(resultWithEmptySet.isEmpty());
  }
}
