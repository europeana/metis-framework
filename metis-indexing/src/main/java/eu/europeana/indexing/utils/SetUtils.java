package eu.europeana.indexing.utils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

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
   * This method generates all possible combinations of elements of different sets.
   * </p>
   * <p>
   * Each combination consists of zero or one element of each set. As such, the number of
   * combinations of sets s1, s2, ..., sn that will be generated is equal to (|s1| + 1) * (|s2| + 1)
   * * ... * (|sn| + 1), where |s| denotes the cardinality (size) of set s. However, depending on
   * the choice of the types and the concatenator, not all of these may be unique and the result set
   * may contain fewer items.
   * </p>
   * <p>
   * It follows that if one of the input sets is empty, the result will be as if that set was not
   * included in the options at all.
   * </p>
   *
   * @param input The input sets. They will be processed in the order given here. Input sets can be
   * empty, but not null.
   * @param emptyCombination The empty combination (used as a seed for the algorithm).
   * @param concatenator The function that appends/concatenates an element to a combination. This
   * function <b>must</b> generate a new instance of the combination (rather than return the same
   * instance).
   * @param <E> The type of the individual elements (input).
   * @param <C> The type of the combinations (output).
   * @return The set of all possible combinations.
   */
  public static <E, C> Set<C> generateOptionalCombinations(List<Set<E>> input, C emptyCombination,
          BiFunction<C, E, C> concatenator) {
    // Check and ignore empty set to avoid needless computations.
    final List<Set<E>> filteredSet = input.stream().filter(set -> !set.isEmpty())
            .collect(Collectors.toList());
    return generateCombinations(filteredSet, emptyCombination, concatenator, false);
  }

  /**
   * <p>
   * This method generates all possible combinations of elements of different sets.
   * </p>
   * <p>
   * Each combination consists of exactly one element of each set. As such, the number of
   * combinations of sets s1, s2, ..., sn that will be generated is equal to |s1| * |s2| * ... *
   * |sn|, where |s| denotes the cardinality (size) of set s. However, depending on the choice of
   * the types and the concatenator, not all of these may be unique and the result set may contain
   * fewer items.
   * </p>
   * <p>
   * It follows that if one of the input sets is empty, the result set will be empty.
   * </p>
   *
   * @param input The input sets. They will be processed in the order given here. Input sets can be
   * empty, but not null.
   * @param emptyCombination The empty combination (used as a seed for the algorithm).
   * @param concatenator The function that appends/concatenates an element to a combination. This
   * function <b>must</b> generate a new instance of the combination (rather than return the same
   * instance).
   * @param <E> The type of the individual elements (input).
   * @param <C> The type of the combinations (output).
   * @return The set of all possible combinations.
   */
  public static <E, C> Set<C> generateForcedCombinations(List<Set<E>> input, C emptyCombination,
          BiFunction<C, E, C> concatenator) {
    // Check to avoid needless computations
    if (input.stream().anyMatch(Set::isEmpty)) {
      return Collections.emptySet();
    }
    return generateCombinations(input, emptyCombination, concatenator, true);
  }

  private static <E, C> Set<C> generateCombinations(List<Set<E>> input, C emptyCombination,
          BiFunction<C, E, C> concatenator, boolean forceOneFromEachSet) {

    // Bootstrap the algorithm by setting the set of new combinations equal to the empty
    // combination (so that it is the input for the first iteration). This set will be worked
    // on during the iterations below, and it will in the end contain the solution.
    Set<C> result = Set.of(emptyCombination);

    // Go by each input set, in order to add the elements to the combinations.
    for (Set<E> currentInputSet : input) {

      // Create a snapshot of the current state, which we use to be able to continue working on the
      // result list later.
      final Set<C> currentCombinations = result;

      // Prepare the result for this iteration.
      if (forceOneFromEachSet) {

        // So we start with an empty result set. All combinations in the result must have an element
        // from the current input set, no incomplete ones are allowed.
        result = new HashSet<>();
      } else {

        // We start with a COPY of the current situation. All combinations that are we put in the
        // result list here are those that will not contain an element from the current set.
        result = new HashSet<>(currentCombinations);
      }

      // Add each element in the current set to each of the current combinations.
      for (E currentInputElement : currentInputSet) {
        for (C currentCombination : currentCombinations) {
          result.add(concatenator.apply(currentCombination, currentInputElement));
        }
      }
    }

    // Done
    return result;
  }
}
