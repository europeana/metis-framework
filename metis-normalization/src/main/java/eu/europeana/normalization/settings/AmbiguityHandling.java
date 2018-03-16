package eu.europeana.normalization.settings;

import java.util.List;
import java.util.function.Function;

/**
 * This enum lists the possible ambiguity handling strategies for matching languages.
 */
public enum AmbiguityHandling {

  /** An ambiguous match does not count: no match will be returned. **/
  NO_MATCH(list -> null),

  /** In case of an ambiguous match the first result will be returned. **/
  CHOOSE_FIRST(list -> list.get(0));

  private final Function<List<String>, String> ambiguousMatchResolver;

  private AmbiguityHandling(Function<List<String>, String> ambiguousMatchResolver) {
    this.ambiguousMatchResolver = ambiguousMatchResolver;
  }

  /**
   * Resolves an ambiguous match according to this handling strategy.
   * 
   * @param possibleMatches The possible matches to choose from.
   * @return The match that is chosen. May be null.
   */
  public String resolveAmbiguousMatch(List<String> possibleMatches) {
    return ambiguousMatchResolver.apply(possibleMatches);
  }
}
