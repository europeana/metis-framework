package eu.europeana.metis.debias.detect.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

/**
 * The type Metadata.
 */
public class Metadata {

  private String annotator;
  private String thesaurus;
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private Date date;

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
  public String getThesaurus() {
    return thesaurus;
  }

  /**
   * Sets thesaurus version.
   *
   * @param thesaurus the thesaurus version
   */
  public void setThesaurus(String thesaurus) {
    this.thesaurus = thesaurus;
  }

  /**
   * Gets date.
   *
   * @return the date
   */
  public Date getDate() {
    if (date != null) {
      return new Date(date.getTime());
    } else {
      return null;
    }
  }

  /**
   * Sets date.
   *
   * @param date the date
   */
  public void setDate(Date date) {
    if (date != null) {
      this.date = new Date(date.getTime());
    }
  }
}
