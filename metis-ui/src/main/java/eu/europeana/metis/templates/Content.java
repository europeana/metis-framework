package eu.europeana.metis.templates;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import eu.europeana.metis.templates.page.landingpage.HeroConfig;
import eu.europeana.metis.templates.page.landingpage.Banner;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-22
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "banner",
    "hero_config"
})
public class Content {

  @JsonProperty("banner")
  private Banner banner;
  @JsonProperty("hero_config")
  private HeroConfig heroConfig;

  @JsonProperty("banner")
  public Banner getBanner() {
    return banner;
  }

  @JsonProperty("banner")
  public void setBanner(Banner banner) {
    this.banner = banner;
  }

  @JsonProperty("hero_config")
  public HeroConfig getHeroConfig() {
    return heroConfig;
  }

  @JsonProperty("hero_config")
  public void setHeroConfig(HeroConfig heroConfig) {
    this.heroConfig = heroConfig;
  }

  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<>();

  @JsonAnyGetter
  public Map<String, Object> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter
  public void setAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
  }

}
