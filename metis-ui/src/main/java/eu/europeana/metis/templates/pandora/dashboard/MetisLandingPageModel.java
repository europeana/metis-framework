package eu.europeana.metis.templates.pandora.dashboard;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import eu.europeana.metis.mapping.molecules.pandora.Breadcrumb;
import eu.europeana.metis.mapping.molecules.pandora.CssFile;
import eu.europeana.metis.mapping.molecules.pandora.Excerpt;
import eu.europeana.metis.mapping.molecules.pandora.Headline;
import eu.europeana.metis.mapping.molecules.pandora.I18n;
import eu.europeana.metis.mapping.molecules.pandora.JsFile;
import eu.europeana.metis.mapping.molecules.pandora.JsVar;
import eu.europeana.metis.mapping.molecules.pandora.MetisFooter;
import eu.europeana.metis.mapping.molecules.pandora.MetisHeader;
import eu.europeana.metis.mapping.molecules.pandora.register.RegisterForm;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-23
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "js_vars",
    "page_config",
    "metis_header",
    "page_title",
    "image_root",
    "version",
    "css_files",
    "i18n",
    "is_java",
    "js_files",
    "landing_page_content",
    "login_form",
    "register_form",
    "metis_footer",
    "excerpt",
    "headline",
    "breadcrumbs"
})
public class MetisLandingPageModel {

  @JsonProperty("js_vars")
  private List<JsVar> jsVars = null;
  @JsonProperty("page_config")
  private PageConfig pageConfig;
  @JsonProperty("metis_header")
  private MetisHeader metisHeader;
  @JsonProperty("page_title")
  private String pageTitle;
  @JsonProperty("image_root")
  private String imageRoot;
  @JsonProperty("version")
  private Version version;
  @JsonProperty("css_files")
  private List<CssFile> cssFiles = null;
  @JsonProperty("i18n")
  private I18n i18n;
  @JsonProperty("is_java")
  private Boolean isJava;
  @JsonProperty("js_files")
  private List<JsFile> jsFiles = null;
  @JsonProperty("landing_page_content")
  private LandingPageContent landingPageContent;
  @JsonProperty("login_form")
  private LoginForm loginForm;
  @JsonProperty("register_form")
  private RegisterForm registerForm;
  @JsonProperty("metis_footer")
  private MetisFooter metisFooter;
  @JsonProperty("excerpt")
  private List<Excerpt> excerpt = null;
  @JsonProperty("headline")
  private List<Headline> headline = null;
  @JsonProperty("breadcrumbs")
  private List<Breadcrumb> breadcrumbs = null;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  @JsonProperty("js_vars")
  public List<JsVar> getJsVars() {
    return jsVars;
  }

  @JsonProperty("js_vars")
  public void setJsVars(List<JsVar> jsVars) {
    this.jsVars = jsVars;
  }

  @JsonProperty("page_config")
  public PageConfig getPageConfig() {
    return pageConfig;
  }

  @JsonProperty("page_config")
  public void setPageConfig(PageConfig pageConfig) {
    this.pageConfig = pageConfig;
  }

  @JsonProperty("metis_header")
  public MetisHeader getMetisHeader() {
    return metisHeader;
  }

  @JsonProperty("metis_header")
  public void setMetisHeader(MetisHeader metisHeader) {
    this.metisHeader = metisHeader;
  }

  @JsonProperty("page_title")
  public String getPageTitle() {
    return pageTitle;
  }

  @JsonProperty("page_title")
  public void setPageTitle(String pageTitle) {
    this.pageTitle = pageTitle;
  }

  @JsonProperty("image_root")
  public String getImageRoot() {
    return imageRoot;
  }

  @JsonProperty("image_root")
  public void setImageRoot(String imageRoot) {
    this.imageRoot = imageRoot;
  }

  @JsonProperty("version")
  public Version getVersion() {
    return version;
  }

  @JsonProperty("version")
  public void setVersion(Version version) {
    this.version = version;
  }

  @JsonProperty("css_files")
  public List<CssFile> getCssFiles() {
    return cssFiles;
  }

  @JsonProperty("css_files")
  public void setCssFiles(List<CssFile> cssFiles) {
    this.cssFiles = cssFiles;
  }

  @JsonProperty("i18n")
  public I18n getI18n() {
    return i18n;
  }

  @JsonProperty("i18n")
  public void setI18n(I18n i18n) {
    this.i18n = i18n;
  }

  @JsonProperty("is_java")
  public Boolean getIsJava() {
    return isJava;
  }

  @JsonProperty("is_java")
  public void setIsJava(Boolean isJava) {
    this.isJava = isJava;
  }

  @JsonProperty("js_files")
  public List<JsFile> getJsFiles() {
    return jsFiles;
  }

  @JsonProperty("js_files")
  public void setJsFiles(List<JsFile> jsFiles) {
    this.jsFiles = jsFiles;
  }

  @JsonProperty("landing_page_content")
  public LandingPageContent getLandingPageContent() {
    return landingPageContent;
  }

  @JsonProperty("landing_page_content")
  public void setLandingPageContent(LandingPageContent landingPageContent) {
    this.landingPageContent = landingPageContent;
  }

  @JsonProperty("login_form")
  public LoginForm getLoginForm() {
    return loginForm;
  }

  @JsonProperty("login_form")
  public void setLoginForm(LoginForm loginForm) {
    this.loginForm = loginForm;
  }

  @JsonProperty("register_form")
  public RegisterForm getRegisterForm() {
    return registerForm;
  }

  @JsonProperty("register_form")
  public void setRegisterForm(RegisterForm registerForm) {
    this.registerForm = registerForm;
  }

  @JsonProperty("metis_footer")
  public MetisFooter getMetisFooter() {
    return metisFooter;
  }

  @JsonProperty("metis_footer")
  public void setMetisFooter(MetisFooter metisFooter) {
    this.metisFooter = metisFooter;
  }

  @JsonProperty("excerpt")
  public List<Excerpt> getExcerpt() {
    return excerpt;
  }

  @JsonProperty("excerpt")
  public void setExcerpt(List<Excerpt> excerpt) {
    this.excerpt = excerpt;
  }

  @JsonProperty("headline")
  public List<Headline> getHeadline() {
    return headline;
  }

  @JsonProperty("headline")
  public void setHeadline(List<Headline> headline) {
    this.headline = headline;
  }

  @JsonProperty("breadcrumbs")
  public List<Breadcrumb> getBreadcrumbs() {
    return breadcrumbs;
  }

  @JsonProperty("breadcrumbs")
  public void setBreadcrumbs(List<Breadcrumb> breadcrumbs) {
    this.breadcrumbs = breadcrumbs;
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
