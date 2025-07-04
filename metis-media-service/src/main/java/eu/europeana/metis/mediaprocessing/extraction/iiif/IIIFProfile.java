package eu.europeana.metis.mediaprocessing.extraction.iiif;

import java.util.Objects;

/**
 * The type Iiif profile.
 */
public class IIIFProfile {

  private String url;
  private IIIFProfileDetail detail; // may be null

  @Override
  public final boolean equals(Object o) {
    if (!(o instanceof IIIFProfile that)) {
      return false;
    }

    return Objects.equals(url, that.url) && Objects.equals(detail, that.detail);
  }

  @Override
  public int hashCode() {
    return Objects.hash(Objects.hashCode(url), Objects.hashCode(detail));
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
   * Gets detail.
   *
   * @return the detail
   */
  public IIIFProfileDetail getDetail() {
    return detail;
  }

  /**
   * Sets detail.
   *
   * @param detail the detail
   */
  public void setDetail(IIIFProfileDetail detail) {
    this.detail = detail;
  }
}
