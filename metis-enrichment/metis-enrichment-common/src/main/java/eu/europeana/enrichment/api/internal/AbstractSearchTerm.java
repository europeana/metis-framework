package eu.europeana.enrichment.api.internal;

import java.util.Objects;

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

  @Override
  public final boolean equals(Object otherObject) {
    if (otherObject == this) {
      return true;
    }
    if (!(otherObject instanceof SearchTerm)) {
      return false;
    }
    final SearchTerm other = (SearchTerm) otherObject;
    return Objects.equals(other.getTextValue(), this.getTextValue()) && Objects
            .equals(other.getLanguage(), this.getLanguage()) && Objects
            .equals(other.getCandidateTypes(), this.getCandidateTypes());
  }

  @Override
  public final int hashCode() {
    return Objects.hash(this.getTextValue(), this.getLanguage(), this.getCandidateTypes());
  }

  public String getTextValue() {
    return textValue;
  }

  public String getLanguage() {
    return language;
  }
}
