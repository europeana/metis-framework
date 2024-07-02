package eu.europeana.metis.mediaprocessing.extraction.oembed;

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
  private String author_name;
  private String author_url;
  private String provider_name;
  private String provider_url;
  private String cache_age;
  private String thumbnail_url;
  private String thumbnail_height;
  private String thumbnail_width;
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
  public String getAuthor_name() {
    return author_name;
  }

  /**
   * Sets author name.
   *
   * @param author_name the author name
   */
  public void setAuthor_name(String author_name) {
    this.author_name = author_name;
  }

  /**
   * Gets author url.
   *
   * @return the author url
   */
  public String getAuthor_url() {
    return author_url;
  }

  /**
   * Sets author url.
   *
   * @param author_url the author url
   */
  public void setAuthor_url(String author_url) {
    this.author_url = author_url;
  }

  /**
   * Gets provider name.
   *
   * @return the provider name
   */
  public String getProvider_name() {
    return provider_name;
  }

  /**
   * Sets provider name.
   *
   * @param provider_name the provider name
   */
  public void setProvider_name(String provider_name) {
    this.provider_name = provider_name;
  }

  /**
   * Gets provider url.
   *
   * @return the provider url
   */
  public String getProvider_url() {
    return provider_url;
  }

  /**
   * Sets provider url.
   *
   * @param provider_url the provider url
   */
  public void setProvider_url(String provider_url) {
    this.provider_url = provider_url;
  }

  /**
   * Gets cache age.
   *
   * @return the cache age
   */
  public String getCache_age() {
    return cache_age;
  }

  /**
   * Sets cache age.
   *
   * @param cache_age the cache age
   */
  public void setCache_age(String cache_age) {
    this.cache_age = cache_age;
  }

  /**
   * Gets thumbnail url.
   *
   * @return the thumbnail url
   */
  public String getThumbnail_url() {
    return thumbnail_url;
  }

  /**
   * Sets thumbnail url.
   *
   * @param thumbnail_url the thumbnail url
   */
  public void setThumbnail_url(String thumbnail_url) {
    this.thumbnail_url = thumbnail_url;
  }

  /**
   * Gets thumbnail height.
   *
   * @return the thumbnail height
   */
  public String getThumbnail_height() {
    return thumbnail_height;
  }

  /**
   * Sets thumbnail height.
   *
   * @param thumbnail_height the thumbnail height
   */
  public void setThumbnail_height(String thumbnail_height) {
    this.thumbnail_height = thumbnail_height;
  }

  /**
   * Gets thumbnail width.
   *
   * @return the thumbnail width
   */
  public String getThumbnail_width() {
    return thumbnail_width;
  }

  /**
   * Sets thumbnail width.
   *
   * @param thumbnail_width the thumbnail width
   */
  public void setThumbnail_width(String thumbnail_width) {
    this.thumbnail_width = thumbnail_width;
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
        && Objects.equals(title, that.title) && url.equals(that.url) && Objects.equals(author_name,
        that.author_name) && Objects.equals(author_url, that.author_url) && Objects.equals(provider_name,
        that.provider_name) && Objects.equals(provider_url, that.provider_url) && Objects.equals(cache_age,
        that.cache_age) && Objects.equals(thumbnail_url, that.thumbnail_url) && Objects.equals(thumbnail_height,
        that.thumbnail_height) && Objects.equals(thumbnail_width, that.thumbnail_width) && Objects.equals(html,
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
    result = 31 * result + Objects.hashCode(author_name);
    result = 31 * result + Objects.hashCode(author_url);
    result = 31 * result + Objects.hashCode(provider_name);
    result = 31 * result + Objects.hashCode(provider_url);
    result = 31 * result + Objects.hashCode(cache_age);
    result = 31 * result + Objects.hashCode(thumbnail_url);
    result = 31 * result + Objects.hashCode(thumbnail_height);
    result = 31 * result + Objects.hashCode(thumbnail_width);
    result = 31 * result + Objects.hashCode(html);
    result = 31 * result + Objects.hashCode(duration);
    return result;
  }
}
