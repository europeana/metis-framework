package eu.europeana.metis.mapping.molecules.pandora.register;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-22
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "css_files",
    "js_files",
    "register_form"
})
public class RegisterPageModel {

  @JsonProperty("css_files")
  private List<CssFile> cssFiles = null;
  @JsonProperty("js_files")
  private List<JsFile> jsFiles = null;
  @JsonProperty("register_form")
  private RegisterForm registerForm;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  @JsonProperty("css_files")
  public List<CssFile> getCssFiles() {
    return cssFiles;
  }

  @JsonProperty("css_files")
  public void setCssFiles(List<CssFile> cssFiles) {
    this.cssFiles = cssFiles;
  }

  @JsonProperty("js_files")
  public List<JsFile> getJsFiles() {
    return jsFiles;
  }

  @JsonProperty("js_files")
  public void setJsFiles(List<JsFile> jsFiles) {
    this.jsFiles = jsFiles;
  }

  @JsonProperty("register_form")
  public RegisterForm getRegisterForm() {
    return registerForm;
  }

  @JsonProperty("register_form")
  public void setRegisterForm(RegisterForm registerForm) {
    this.registerForm = registerForm;
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
