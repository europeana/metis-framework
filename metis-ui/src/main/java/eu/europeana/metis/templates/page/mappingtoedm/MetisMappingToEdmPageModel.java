package eu.europeana.metis.templates.page.mappingtoedm;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import eu.europeana.metis.templates.CssFile;
import eu.europeana.metis.templates.JsFile;
import eu.europeana.metis.templates.JsVar;
import eu.europeana.metis.templates.MetisHeader;
import eu.europeana.metis.templates.page.landingpage.Breadcrumb;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-23
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "is_java",
    "css_files",
    "js_files",
    "js_vars",
    "page_title",
    "breadcrumbs",
    "metis_header",
    "action_menu",
    "mapping_card"
})
public class MetisMappingToEdmPageModel {

  @JsonProperty("is_java")
  private Boolean isJava;
  @JsonProperty("css_files")
  private List<CssFile> cssFiles = null;
  @JsonProperty("js_files")
  private List<JsFile> jsFiles = null;
  @JsonProperty("js_vars")
  private List<JsVar> jsVars = null;
  @JsonProperty("page_title")
  private String pageTitle;
  @JsonProperty("breadcrumbs")
  private List<Breadcrumb> breadcrumbs = null;
  @JsonProperty("metis_header")
  private MetisHeader metisHeader;
  @JsonProperty("action_menu")
  private List<ActionMenu> actionMenu = null;
  @JsonProperty("mapping_card")
  private MappingCard mappingCard;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  @JsonProperty("is_java")
  public Boolean getIsJava() {
    return isJava;
  }

  @JsonProperty("is_java")
  public void setIsJava(Boolean isJava) {
    this.isJava = isJava;
  }

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

  @JsonProperty("js_vars")
  public List<JsVar> getJsVars() {
    return jsVars;
  }

  @JsonProperty("js_vars")
  public void setJsVars(List<JsVar> jsVars) {
    this.jsVars = jsVars;
  }

  @JsonProperty("page_title")
  public String getPageTitle() {
    return pageTitle;
  }

  @JsonProperty("page_title")
  public void setPageTitle(String pageTitle) {
    this.pageTitle = pageTitle;
  }

  @JsonProperty("breadcrumbs")
  public List<Breadcrumb> getBreadcrumbs() {
    return breadcrumbs;
  }

  @JsonProperty("breadcrumbs")
  public void setBreadcrumbs(List<Breadcrumb> breadcrumbs) {
    this.breadcrumbs = breadcrumbs;
  }

  @JsonProperty("metis_header")
  public MetisHeader getMetisHeader() {
    return metisHeader;
  }

  @JsonProperty("metis_header")
  public void setMetisHeader(MetisHeader metisHeader) {
    this.metisHeader = metisHeader;
  }

  @JsonProperty("action_menu")
  public List<ActionMenu> getActionMenu() {
    return actionMenu;
  }

  @JsonProperty("action_menu")
  public void setActionMenu(List<ActionMenu> actionMenu) {
    this.actionMenu = actionMenu;
  }

  @JsonProperty("mapping_card")
  public MappingCard getMappingCard() {
    return mappingCard;
  }

  @JsonProperty("mapping_card")
  public void setMappingCard(MappingCard mappingCard) {
    this.mappingCard = mappingCard;
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