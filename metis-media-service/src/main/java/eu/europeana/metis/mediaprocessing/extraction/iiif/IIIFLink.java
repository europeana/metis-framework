package eu.europeana.metis.mediaprocessing.extraction.iiif;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The type Iiif link.
 */
public class IIIFLink {

  private String id;
  private String type;
  /**
   * The Label.
   */
  @JsonProperty("label")
  public Map<String, List<String>> label;
  private String format;
  private String profile;

  /**
   * Instantiates a new Iiif link.
   */
  public IIIFLink() {
    // constructor
  }

  /**
   * Instantiates a new Iiif link.
   *
   * @param id the id
   * @param type the type
   * @param label the label
   * @param format the format
   * @param profile the profile
   */
  public IIIFLink(String id, String type, Map<String, List<String>> label, String format, String profile) {
    this.id = id;
    this.type = type;
    this.label = label;
    this.format = format;
    this.profile = profile;
  }

  /**
   * Gets profile.
   *
   * @return the profile
   */
  public String getProfile() {
    return profile;
  }

  /**
   * Sets profile.
   *
   * @param profile the profile
   */
  public void setProfile(String profile) {
    this.profile = profile;
  }

  /**
   * Gets format.
   *
   * @return the format
   */
  public String getFormat() {
    return format;
  }

  /**
   * Sets format.
   *
   * @param format the format
   */
  public void setFormat(String format) {
    this.format = format;
  }

  /**
   * Gets label.
   *
   * @return the label
   */
  public Map<String, List<String>> getLabel() {
    return label;
  }

  /**
   * Sets label.
   *
   * @param label the label
   */
  public void setLabel(Map<String, List<String>> label) {
    this.label = label;
  }

  /**
   * Gets type.
   *
   * @return the type
   */
  public String getType() {
    return type;
  }

  /**
   * Sets type.
   *
   * @param type the type
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * Gets id.
   *
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * Sets id.
   *
   * @param id the id
   */
  public void setId(String id) {
    this.id = id;
  }

  @Override
  public final boolean equals(Object o) {
    if (!(o instanceof IIIFLink iiifLink)) {
      return false;
    }

    return Objects.equals(id, iiifLink.id) && Objects.equals(type, iiifLink.type) && Objects.equals(
        label, iiifLink.label) && Objects.equals(format, iiifLink.format) && Objects.equals(profile,
        iiifLink.profile);
  }

  @Override
  public int hashCode() {
    return Objects.hash(Objects.hashCode(id),
        Objects.hashCode(type),
        Objects.hashCode(label), Objects.hashCode(format),
        Objects.hashCode(profile));
  }
}
