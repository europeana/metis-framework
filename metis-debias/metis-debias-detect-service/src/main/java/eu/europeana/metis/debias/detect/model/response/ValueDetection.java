package eu.europeana.metis.debias.detect.model.response;

import java.util.Collections;
import java.util.List;

/**
 * The type Value detection.
 */
public class ValueDetection {

  private String language;
  private String literal;
  private List<Tag> tags;

  /**
   * Gets language.
   *
   * @return the language
   */
  public String getLanguage() {
    return language;
  }

  /**
   * Sets language.
   *
   * @param language the language
   */
  public void setLanguage(String language) {
    this.language = language;
  }

  /**
   * Gets literal.
   *
   * @return the literal
   */
  public String getLiteral() {
    return literal;
  }

  /**
   * Sets literal.
   *
   * @param literal the literal
   */
  public void setLiteral(String literal) {
    this.literal = literal;
  }

  /**
   * Gets tags.
   *
   * @return the tags
   */
  public List<Tag> getTags() {
    return Collections.unmodifiableList(tags);
  }

  /**
   * Sets tags.
   *
   * @param tags the tags
   */
  public void setTags(List<Tag> tags) {
    this.tags = Collections.unmodifiableList(tags);
  }
}
