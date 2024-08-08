package eu.europeana.metis.mediaprocessing.extraction.oembed;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * Model based on the standard https://oembed.com/
 */
public class OEmbedModel {

  private String type;
  private String version;
  private String title;
  private int height;
  private int width;
  private String url;
  @JsonProperty("author_name")
  private String authorName;
  @JsonProperty("author_url")
  private String authorUrl;
  @JsonProperty("provider_name")
  private String providerName;
  @JsonProperty("provider_url")
  private String providerUrl;
  @JsonProperty("cache_age")
  private String cacheAge;
  @JsonProperty("thumbnail_url")
  private String thumbnailUrl;
  @JsonProperty("thumbnail_height")
  private String thumbnailHeight;
  @JsonProperty("thumbnail_width")
  private String thumbnailWidth;
  private String html;
  private String duration;

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
   * Gets version.
   *
   * @return the version
   */
  public String getVersion() {
    return version;
  }

  /**
   * Sets version.
   *
   * @param version the version
   */
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   * Gets title.
   *
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * Sets title.
   *
   * @param title the title
   */
  public void setTitle(String title) {
    this.title = title;
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
   * Gets url.
   *
   * @return the url
   */
  public String getUrl() {
    return url;
  }

  /**
   * Sets url.
   *
   * @param url the url
   */
  public void setUrl(String url) {
    this.url = url;
  }

  /**
   * Gets author name.
   *
   * @return the author name
   */
  public String getAuthorName() {
    return authorName;
  }

  /**
   * Sets author name.
   *
   * @param authorName the author name
   */
  public void setAuthorName(String authorName) {
    this.authorName = authorName;
  }

  /**
   * Gets author url.
   *
   * @return the author url
   */
  public String getAuthorUrl() {
    return authorUrl;
  }

  /**
   * Sets author url.
   *
   * @param authorUrl the author url
   */
  public void setAuthorUrl(String authorUrl) {
    this.authorUrl = authorUrl;
  }

  /**
   * Gets provider name.
   *
   * @return the provider name
   */
  public String getProviderName() {
    return providerName;
  }

  /**
   * Sets provider name.
   *
   * @param providerName the provider name
   */
  public void setProviderName(String providerName) {
    this.providerName = providerName;
  }

  /**
   * Gets provider url.
   *
   * @return the provider url
   */
  public String getProviderUrl() {
    return providerUrl;
  }

  /**
   * Sets provider url.
   *
   * @param providerUrl the provider url
   */
  public void setProviderUrl(String providerUrl) {
    this.providerUrl = providerUrl;
  }

  /**
   * Gets cache age.
   *
   * @return the cache age
   */
  public String getCacheAge() {
    return cacheAge;
  }

  /**
   * Sets cache age.
   *
   * @param cacheAge the cache age
   */
  public void setCacheAge(String cacheAge) {
    this.cacheAge = cacheAge;
  }

  /**
   * Gets thumbnail url.
   *
   * @return the thumbnail url
   */
  public String getThumbnailUrl() {
    return thumbnailUrl;
  }

  /**
   * Sets thumbnail url.
   *
   * @param thumbnailUrl the thumbnail url
   */
  public void setThumbnailUrl(String thumbnailUrl) {
    this.thumbnailUrl = thumbnailUrl;
  }

  /**
   * Gets thumbnail height.
   *
   * @return the thumbnail height
   */
  public String getThumbnailHeight() {
    return thumbnailHeight;
  }

  /**
   * Sets thumbnail height.
   *
   * @param thumbnailHeight the thumbnail height
   */
  public void setThumbnailHeight(String thumbnailHeight) {
    this.thumbnailHeight = thumbnailHeight;
  }

  /**
   * Gets thumbnail width.
   *
   * @return the thumbnail width
   */
  public String getThumbnailWidth() {
    return thumbnailWidth;
  }

  /**
   * Sets thumbnail width.
   *
   * @param thumbnailWidth the thumbnail width
   */
  public void setThumbnailWidth(String thumbnailWidth) {
    this.thumbnailWidth = thumbnailWidth;
  }

  /**
   * Gets html.
   *
   * @return the html
   */
  public String getHtml() {
    return html;
  }

  /**
   * Sets html.
   *
   * @param html the html
   */
  public void setHtml(String html) {
    this.html = html;
  }

  /**
   * Gets duration.
   *
   * @return the duration
   */
  public String getDuration() {
    return duration;
  }

  /**
   * Sets duration.
   *
   * @param duration the duration
   */
  public void setDuration(String duration) {
    this.duration = duration;
  }

  @Override
  public final boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof OEmbedModel that)) {
      return false;
    }

    return height == that.height && width == that.width && type.equals(that.type) && version.equals(that.version)
        && Objects.equals(title, that.title) && url.equals(that.url) && Objects.equals(authorName,
        that.authorName) && Objects.equals(authorUrl, that.authorUrl) && Objects.equals(providerName,
        that.providerName) && Objects.equals(providerUrl, that.providerUrl) && Objects.equals(cacheAge,
        that.cacheAge) && Objects.equals(thumbnailUrl, that.thumbnailUrl) && Objects.equals(thumbnailHeight,
        that.thumbnailHeight) && Objects.equals(thumbnailWidth, that.thumbnailWidth) && Objects.equals(html,
        that.html) && Objects.equals(duration, that.duration);
  }

  @Override
  public int hashCode() {
    int result = type.hashCode();
    result = 31 * result + version.hashCode();
    result = 31 * result + Objects.hashCode(title);
    result = 31 * result + height;
    result = 31 * result + width;
    result = 31 * result + url.hashCode();
    result = 31 * result + Objects.hashCode(authorName);
    result = 31 * result + Objects.hashCode(authorUrl);
    result = 31 * result + Objects.hashCode(providerName);
    result = 31 * result + Objects.hashCode(providerUrl);
    result = 31 * result + Objects.hashCode(cacheAge);
    result = 31 * result + Objects.hashCode(thumbnailUrl);
    result = 31 * result + Objects.hashCode(thumbnailHeight);
    result = 31 * result + Objects.hashCode(thumbnailWidth);
    result = 31 * result + Objects.hashCode(html);
    result = 31 * result + Objects.hashCode(duration);
    return result;
  }
}
