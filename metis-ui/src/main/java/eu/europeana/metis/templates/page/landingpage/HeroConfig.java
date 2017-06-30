package eu.europeana.metis.templates.page.landingpage;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-23
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "hero_image",
    "attribution_url",
    "attribution_text",
    "brand_colour",
    "license_CC0",
    "brand_position",
    "brand_opacity"
})
public class HeroConfig {

  @JsonProperty("hero_image")
  private String heroImage;
  @JsonProperty("attribution_url")
  private String attributionUrl;
  @JsonProperty("attribution_text")
  private String attributionText;
  @JsonProperty("brand_colour")
  private String brandColour;
  @JsonProperty("license_CC0")
  private String licenseCC0;
  @JsonProperty("brand_position")
  private String brandPosition;
  @JsonProperty("brand_opacity")
  private String brandOpacity;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<>();

  @JsonProperty("hero_image")
  public String getHeroImage() {
    return heroImage;
  }

  @JsonProperty("hero_image")
  public void setHeroImage(String heroImage) {
    this.heroImage = heroImage;
  }

  @JsonProperty("attribution_url")
  public String getAttributionUrl() {
    return attributionUrl;
  }

  @JsonProperty("attribution_url")
  public void setAttributionUrl(String attributionUrl) {
    this.attributionUrl = attributionUrl;
  }

  @JsonProperty("attribution_text")
  public String getAttributionText() {
    return attributionText;
  }

  @JsonProperty("attribution_text")
  public void setAttributionText(String attributionText) {
    this.attributionText = attributionText;
  }

  @JsonProperty("brand_colour")
  public String getBrandColour() {
    return brandColour;
  }

  @JsonProperty("brand_colour")
  public void setBrandColour(String brandColour) {
    this.brandColour = brandColour;
  }

  @JsonProperty("license_CC0")
  public String getLicenseCC0() {
    return licenseCC0;
  }

  @JsonProperty("license_CC0")
  public void setLicenseCC0(String licenseCC0) {
    this.licenseCC0 = licenseCC0;
  }

  @JsonProperty("brand_position")
  public String getBrandPosition() {
    return brandPosition;
  }

  @JsonProperty("brand_position")
  public void setBrandPosition(String brandPosition) {
    this.brandPosition = brandPosition;
  }

  @JsonProperty("brand_opacity")
  public String getBrandOpacity() {
    return brandOpacity;
  }

  @JsonProperty("brand_opacity")
  public void setBrandOpacity(String brandOpacity) {
    this.brandOpacity = brandOpacity;
  }

  @JsonAnyGetter
  public Map<String, Object> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter
  public void setAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
  }

}
