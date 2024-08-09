package eu.europeana.metis.debias.detect.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * The type Metadata.
 */
public class Metadata {
  private String annotator;
  @JsonProperty("thesaurus_version")
  private String thesaurusVersion;
  private LocalDateTime date;

  /**
   * Gets annotator.
   *
   * @return the annotator
   */
  public String getAnnotator() {
    return annotator;
  }

  /**
   * Sets annotator.
   *
   * @param annotator the annotator
   */
  public void setAnnotator(String annotator) {
    this.annotator = annotator;
  }

  /**
   * Gets thesaurus version.
   *
   * @return the thesaurus version
   */
  public String getThesaurusVersion() {
    return thesaurusVersion;
  }

  /**
   * Sets thesaurus version.
   *
   * @param thesaurusVersion the thesaurus version
   */
  public void setThesaurusVersion(String thesaurusVersion) {
    this.thesaurusVersion = thesaurusVersion;
  }

  /**
   * Gets date.
   *
   * @return the date
   */
  public LocalDateTime getDate() {
    return date;
  }

  /**
   * Sets date.
   *
   * @param date the date
   */
  public void setDate(LocalDateTime date) {
    this.date = date;
  }
}
