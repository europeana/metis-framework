package eu.europeana.normalization.languages;

/**
 * This class represents a label attached to a language. A language label is a name that a language
 * can have in a given (possibly different) language and script. For instance, the English language
 * could have a label "Engels" where the language is Dutch and the script is Latin.
 *
 * @author Nuno Freire (nfreire@gmail.com)
 */
public class LanguageLabel {

  /** Label **/
  private final String label;

  /** Language **/
  private final String language;

  /** String script **/
  private final String script;

  /**
   * Constructor.
   * 
   * @param label The language label.
   * @param language The language of the label.
   * @param script The script of the label.
   */
  public LanguageLabel(String label, String language, String script) {
    this.label = label;
    this.language = language;
    this.script = script;
  }

  /**
   * 
   * @return The language label.
   */
  public String getLabel() {
    return label;
  }

  /**
   * 
   * @return The language of the label.
   */
  public String getLanguage() {
    return language;
  }

  /**
   * 
   * @return The script of the label.
   */
  public String getScript() {
    return script;
  }

  @Override
  public String toString() {
    return "Label [label=" + label + ", language=" + language + ", script=" + script + "]";
  }
}
