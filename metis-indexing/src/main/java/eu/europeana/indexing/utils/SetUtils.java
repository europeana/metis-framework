package eu.europeana.indexing.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class provides utility functionality for set manipulation.
 * 
 * @author jochen
 *
 */
public final class SetUtils {

  private SetUtils() {}

  /**
   * <p>
   * This method generates all possible combinations of elements of different sets. Each combination
   * consists of zero or one element of each set. As such, the number of combinations of sets s1,
   * s2, ..., sn that will be generated is equal to (|s1| + 1) * (|s2| + 1) * ... * (|sn| + 1),
   * where |s| denotes the cardinality (size) of set s. However, depending on the choice of the
   * types and the concatenator, not all of these may be unique and the result set may contain fewer
   * items.
   * </p>
   * 
   * @param input The input sets. They will be processed in the order given here.
   * @param emptyCombination The empty combination (used as a seed for the algorithm).
   * @param concatenator The function that appends/concatenates an element to a combination. This
   *        function <b>must</b> generate a new instance of the combination (rather than return the
   *        same instance).
   * @param <E> The type of the individual elements (input).
   * @param <C> The type of the combinations (output).
   * @return The set of all possible combinations.
   */
  public static <E, C> Set<C> generateCombinations(List<Set<E>> input, C emptyCombination,
      BiFunction<C, E, C> concatenator) {

    // Bootstrap the algorithm by setting the set of new combinations equal to the empty
    // combination (so that it is the input for the first iteration).
    final Set<C> newCombinations = Stream.of(emptyCombination).collect(Collectors.toSet());

    // Go by each input set, in order to add the elements to the combinations.
    for (Set<E> currentInputSet : input) {

      // The combinations we found so far and that we are going to expand on. Note: we will add to
      // the new combinations list: the combinations that are in that list already are kept in place
      // and are those that will not contain an element from the current set.
      final Set<C> currentCombinations = new HashSet<>(newCombinations);

      // Add each element in the current set to each of the current combinations.
      for (E currentInputElement : currentInputSet) {
        for (C currentCombination : currentCombinations) {
          newCombinations.add(concatenator.apply(currentCombination, currentInputElement));
        }
      }
    }

    // Done
    return newCombinations;
  }
}
