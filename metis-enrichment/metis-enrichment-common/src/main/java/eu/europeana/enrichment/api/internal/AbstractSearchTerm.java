package eu.europeana.enrichment.api.internal;

/**
 * This class is a basic implementation of {@link SearchTerm} that leaves the details of the
 * candidate types unimplemented.
 */
public abstract class AbstractSearchTerm implements SearchTerm {

  private final String textValue;
  private final String language;

  public AbstractSearchTerm(String textValue, String language) {
    this.textValue = textValue;
    this.language = language;
  }

  public String getTextValue() {
    return textValue;
  }

  public String getLanguage() {
    return language;
  }
}
