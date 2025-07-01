package eu.europeana.metis.mediaprocessing.extraction.iiif;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The type IIIf info json model v3 base on the following
 * <a href="https://iiif.io/api/image/3.0/#52-technical-properties">technical properties</a>
 */
public class IIIFInfoJsonV3 implements IIIFInfoJson {

  @JsonProperty("@context")
  private List<String> context;
  @JsonProperty("id")
  private String id;
  private String type;
  private String protocol;
  private String profile;
  private int width;
  private int height;
  private int maxWidth;
  private int maxHeight;
  private int maxArea;
  private List<Size> sizes;
  private List<Tile> tiles;
  private List<String> preferredFormats;
  private String rights;
  private List<String> extraQualities;
  private List<String> extraFormats;
  private List<String> extraFeatures;
  //@JsonProperty("partOf")
  @JsonDeserialize(using = IIIFInfoJsonLinkDeserializer.class)
  private List<IIIFLink> partOf;
  //@JsonProperty("seeAlso")
  @JsonDeserialize(using = IIIFInfoJsonLinkDeserializer.class)
  private List<IIIFLink> seeAlso;
  //@JsonProperty("service")
  @JsonDeserialize(using = IIIFInfoJsonLinkDeserializer.class)
  private List<IIIFLink> service;

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
   * Gets protocol.
   *
   * @return the protocol
   */
  public String getProtocol() {
    return protocol;
  }

  /**
   * Sets protocol.
   *
   * @param protocol the protocol
   */
  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }

  /**
   * Gets width.
   *
   * @return the width
   */
  public int getWidth() {
    return width;
  }

  /**
   * Sets width.
   *
   * @param width the width
   */
  public void setWidth(int width) {
    this.width = width;
  }

  /**
   * Gets height.
   *
   * @return the height
   */
  public int getHeight() {
    return height;
  }

  /**
   * Sets height.
   *
   * @param height the height
   */
  public void setHeight(int height) {
    this.height = height;
  }

  /**
   * Gets sizes.
   *
   * @return the sizes
   */
  public List<Size> getSizes() {
    return Collections.unmodifiableList(sizes);
  }

  /**
   * Sets sizes.
   *
   * @param sizes the sizes
   */
  public void setSizes(List<Size> sizes) {
    this.sizes = Collections.unmodifiableList(sizes);
  }

  /**
   * Gets tiles.
   *
   * @return the tiles
   */
  public List<Tile> getTiles() {
    return Collections.unmodifiableList(tiles);
  }

  /**
   * Sets tiles.
   *
   * @param tiles the tiles
   */
  public void setTiles(List<Tile> tiles) {
    this.tiles = Collections.unmodifiableList(tiles);
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
  public final boolean equals(Object o) {
    if (!(o instanceof IIIFInfoJsonV3 that)) {
      return false;
    }

    return width == that.width && height == that.height && maxWidth == that.maxWidth && maxHeight == that.maxHeight
        && maxArea == that.maxArea && Objects.equals(context, that.context) && Objects.equals(id, that.id)
        && Objects.equals(type, that.type) && Objects.equals(protocol, that.protocol)
        && Objects.equals(sizes, that.sizes) && Objects.equals(tiles, that.tiles) && Objects.equals(
        profile, that.profile);
  }

  @Override
  public int hashCode() {
    int result = Objects.hashCode(context);
    result = 31 * result + Objects.hashCode(id);
    result = 31 * result + Objects.hashCode(type);
    result = 31 * result + Objects.hashCode(protocol);
    result = 31 * result + width;
    result = 31 * result + height;
    result = 31 * result + maxWidth;
    result = 31 * result + maxHeight;
    result = 31 * result + maxArea;
    result = 31 * result + Objects.hashCode(sizes);
    result = 31 * result + Objects.hashCode(tiles);
    result = 31 * result + Objects.hashCode(profile);
    return result;
  }
}
