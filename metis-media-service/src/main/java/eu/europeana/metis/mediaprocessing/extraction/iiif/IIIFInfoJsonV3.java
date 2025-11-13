package eu.europeana.metis.mediaprocessing.extraction.iiif;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The type IIIf info json model v3 base on the following
 * <a href="https://iiif.io/api/image/3.0/#52-technical-properties">technical properties</a>
 */
public class IIIFInfoJsonV3 extends IIIFInfoJsonBase implements IIIFInfoJson {

  @JsonProperty("@context")
  private List<String> context;
  @JsonProperty("id")
  private String id;
  private String type;
  private String profile;
  private int maxWidth;
  private int maxHeight;
  private int maxArea;
  private List<String> preferredFormats;
  private String rights;
  private List<String> extraQualities;
  private List<String> extraFormats;
  private List<String> extraFeatures;
  @JsonDeserialize(using = IIIFInfoJsonLinkDeserializer.class)
  private List<IIIFLink> partOf;
  @JsonDeserialize(using = IIIFInfoJsonLinkDeserializer.class)
  private List<IIIFLink> seeAlso;
  @JsonDeserialize(using = IIIFInfoJsonLinkDeserializer.class)
  private List<IIIFLink> service;

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
   * Gets preferred formats.
   *
   * @return the preferred formats
   */
  public List<String> getPreferredFormats() {
    return Collections.unmodifiableList(preferredFormats);
  }

  /**
   * Sets preferred formats.
   *
   * @param preferredFormats the preferred formats
   */
  public void setPreferredFormats(List<String> preferredFormats) {
    this.preferredFormats = Collections.unmodifiableList(preferredFormats);
  }

  /**
   * Gets rights.
   *
   * @return the rights
   */
  public String getRights() {
    return rights;
  }

  /**
   * Sets rights.
   *
   * @param rights the rights
   */
  public void setRights(String rights) {
    this.rights = rights;
  }

  /**
   * Gets extra qualities.
   *
   * @return the extra qualities
   */
  public List<String> getExtraQualities() {
    return Collections.unmodifiableList(extraQualities);
  }

  /**
   * Sets extra qualities.
   *
   * @param extraQualities the extra qualities
   */
  public void setExtraQualities(List<String> extraQualities) {
    this.extraQualities = Collections.unmodifiableList(extraQualities);
  }

  /**
   * Gets extra formats.
   *
   * @return the extra formats
   */
  public List<String> getExtraFormats() {
    return Collections.unmodifiableList(extraFormats);
  }

  /**
   * Sets extra formats.
   *
   * @param extraFormats the extra formats
   */
  public void setExtraFormats(List<String> extraFormats) {
    this.extraFormats = Collections.unmodifiableList(extraFormats);
  }

  /**
   * Gets extra features.
   *
   * @return the extra features
   */
  public List<String> getExtraFeatures() {
    return Collections.unmodifiableList(extraFeatures);
  }

  /**
   * Sets extra features.
   *
   * @param extraFeatures the extra features
   */
  public void setExtraFeatures(List<String> extraFeatures) {
    this.extraFeatures = Collections.unmodifiableList(extraFeatures);
  }

  /**
   * Gets part of.
   *
   * @return the part of
   */
  public List<IIIFLink> getPartOf() {
    return Collections.unmodifiableList(partOf);
  }

  /**
   * Sets part of.
   *
   * @param partOf the part of
   */
  public void setPartOf(List<IIIFLink> partOf) {
    this.partOf = Collections.unmodifiableList(partOf);
  }

  /**
   * Gets see also.
   *
   * @return the see also
   */
  public List<IIIFLink> getSeeAlso() {
    return Collections.unmodifiableList(seeAlso);
  }

  /**
   * Sets see also.
   *
   * @param seeAlso the see also
   */
  public void setSeeAlso(List<IIIFLink> seeAlso) {
    this.seeAlso = Collections.unmodifiableList(seeAlso);
  }

  /**
   * Gets service.
   *
   * @return the service
   */
  public List<IIIFLink> getService() {
    return Collections.unmodifiableList(service);
  }

  /**
   * Sets service.
   *
   * @param service the service
   */
  public void setService(List<IIIFLink> service) {
    this.service = Collections.unmodifiableList(service);
  }

  /**
   * Gets context.
   *
   * @return the context
   */
  public List<String> getContext() {
    return Collections.unmodifiableList(context);
  }

  /**
   * Sets context.
   *
   * @param context the context
   */
  public void setContext(List<String> context) {
    this.context = Collections.unmodifiableList(context);
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

  /**
   * Gets max width.
   *
   * @return the max width
   */
  public int getMaxWidth() {
    return maxWidth;
  }

  /**
   * Sets max width.
   *
   * @param maxWidth the max width
   */
  public void setMaxWidth(int maxWidth) {
    this.maxWidth = maxWidth;
  }

  /**
   * Gets max height.
   *
   * @return the max height
   */
  public int getMaxHeight() {
    return maxHeight;
  }

  /**
   * Sets max height.
   *
   * @param maxHeight the max height
   */
  public void setMaxHeight(int maxHeight) {
    this.maxHeight = maxHeight;
  }

  /**
   * Gets max area.
   *
   * @return the max area
   */
  public int getMaxArea() {
    return maxArea;
  }

  /**
   * Sets max area.
   *
   * @param maxArea the max area
   */
  public void setMaxArea(int maxArea) {
    this.maxArea = maxArea;
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

  @Override
  public SupportedFormats getSupportedFormats() {
    return new SupportedFormats(
        Optional.ofNullable(this.preferredFormats).map(HashSet::new).orElseGet(HashSet::new),
        Optional.ofNullable(this.extraFormats).map(HashSet::new).orElseGet(HashSet::new));
  }

  @Override
  public final boolean equals(Object o) {
    if (!(o instanceof IIIFInfoJsonV3 that)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    return maxWidth == that.maxWidth && maxHeight == that.maxHeight && maxArea == that.maxArea && context.equals(that.context)
        && id.equals(that.id) && type.equals(that.type) && profile.equals(that.profile) && Objects.equals(
        preferredFormats, that.preferredFormats) && Objects.equals(rights, that.rights) && Objects.equals(
        extraQualities, that.extraQualities) && Objects.equals(extraFormats, that.extraFormats)
        && Objects.equals(extraFeatures, that.extraFeatures) && Objects.equals(partOf, that.partOf)
        && Objects.equals(seeAlso, that.seeAlso) && Objects.equals(service, that.service);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), context, id, type, profile, maxWidth, maxHeight, maxArea,
        preferredFormats, rights, extraQualities, extraFormats, extraFeatures, partOf, seeAlso,
        service);
  }
}
