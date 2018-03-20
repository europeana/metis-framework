package eu.europeana.normalization.languages;

/**
 * Instances of this class denote a match result of language matching. It contains the source text,
 * the found matches (language codes) and the type of match that was made. The language code depends
 * on the {@link LanguagesVocabulary} that was configured for the {@link LanguageMatcher}.
 * 
 * @author jochen
 *
 */
public class LanguageMatch {

  /**
   * This enum lists the possible match types.
   * 
   * @author jochen
   *
   */
  public enum Type {

    /** Indicates that the match was made by finding a code in the input. **/
    CODE_MATCH,

    /** Indicates that the match was made by finding a label in the input. **/
    LABEL_MATCH,

    /** Indicates that no match could be made and the match result will be null. **/
    NO_MATCH
  }

  private final String input;
  private final String match;
  private final Type type;

  /**
   * Constructor.
   * 
   * @param input The input text.
   * @param match The match that was found.
   * @param type The type of the match.
   */
  public LanguageMatch(String input, String match, Type type) {
    this.input = input;
    this.match = match;
    this.type = type;
  }

  /**
   * 
   * @return The input text.
   */
  public String getInput() {
    return input;
  }

  /**
   * 
   * @return The match (language code) that was found.
   */
  public String getMatch() {
    return match;
  }

  /**
   * 
   * @return The match type.
   */
  public Type getType() {
    return type;
  }
}

