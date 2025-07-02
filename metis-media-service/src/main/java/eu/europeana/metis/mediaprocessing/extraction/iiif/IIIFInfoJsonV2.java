package eu.europeana.metis.mediaprocessing.extraction.iiif;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * The type IIIf info json model v2 base on the following
 * <a href="https://iiif.io/api/image/2.0/#image-information">technical properties</a>
 */
public class IIIFInfoJsonV2 extends IIIFInfoJsonBase implements IIIFInfoJson {

  @JsonProperty("@context")
  private String context;
  @JsonProperty("@id")
  private String id;

  @JsonProperty("profile")
  @JsonDeserialize(using = IIIFInfoJsonProfileDeserializer.class)
  private IIIFProfile profile;

  /**
   * Gets profile.
   *
   * @return the profile
   */
  public IIIFProfile getProfile() {
    return profile;
  }

  /**
   * Sets profile.
   *
   * @param profile the profile
   */
  public void setProfile(IIIFProfile profile) {
    this.profile = profile;
  }

  /**
   * Gets context.
   *
   * @return the context
   */
  public String getContext() {
    return context;
  }

  /**
   * Sets context.
   *
   * @param context the context
   */
  public void setContext(String context) {
    this.context = context;
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
    if (!(o instanceof IIIFInfoJsonV2 that)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    return context.equals(that.context) && id.equals(that.id) && profile.equals(that.profile);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + context.hashCode();
    result = 31 * result + id.hashCode();
    result = 31 * result + profile.hashCode();
    return result;
  }
}
