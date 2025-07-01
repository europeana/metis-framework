package eu.europeana.metis.mediaprocessing.extraction.iiif;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The type Iiif profile detail.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class IIIFProfileDetail {

  /**
   * The Formats.
   */
  private List<String> formats;
  /**
   * The Qualities.
   */
  private List<String> qualities;
  /**
   * The Supports.
   */
  private List<String> supports;

  @Override
  public final boolean equals(Object o) {
    if (!(o instanceof IIIFProfileDetail that)) {
      return false;
    }

    return Objects.equals(formats, that.formats) && Objects.equals(qualities, that.qualities)
        && Objects.equals(supports, that.supports);
  }

  @Override
  public int hashCode() {
    int result = Objects.hashCode(formats);
    result = 31 * result + Objects.hashCode(qualities);
    result = 31 * result + Objects.hashCode(supports);
    return result;
  }

  /**
   * Gets formats.
   *
   * @return the formats
   */
  public List<String> getFormats() {
    return Collections.unmodifiableList(formats);
  }

  /**
   * Sets formats.
   *
   * @param formats the formats
   */
  public void setFormats(List<String> formats) {
    this.formats = Collections.unmodifiableList(formats);
  }

  /**
   * Gets qualities.
   *
   * @return the qualities
   */
  public List<String> getQualities() {
    return Collections.unmodifiableList(qualities);
  }

  /**
   * Sets qualities.
   *
   * @param qualities the qualities
   */
  public void setQualities(List<String> qualities) {
    this.qualities = Collections.unmodifiableList(qualities);
  }

  /**
   * Gets supports.
   *
   * @return the supports
   */
  public List<String> getSupports() {
    return Collections.unmodifiableList(supports);
  }

  /**
   * Sets supports.
   *
   * @param supports the supports
   */
  public void setSupports(List<String> supports) {
    this.supports = Collections.unmodifiableList(supports);
  }
}
