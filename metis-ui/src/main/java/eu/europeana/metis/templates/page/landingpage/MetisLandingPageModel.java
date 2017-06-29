package eu.europeana.metis.templates.page.landingpage;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import eu.europeana.metis.templates.CssFile;
import eu.europeana.metis.templates.JsFile;
import eu.europeana.metis.templates.JsVar;
import eu.europeana.metis.templates.MetisFooter;
import eu.europeana.metis.templates.MetisHeader;
import eu.europeana.metis.templates.PageConfig;
import eu.europeana.metis.templates.UserRole;
import eu.europeana.metis.templates.Version;
import eu.europeana.metis.templates.ViewMode;
import eu.europeana.metis.templates.page.landingpage.profile.UserProfileModel;
import eu.europeana.metis.templates.page.landingpage.register.RegisterForm;
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
    "fullName",
    "lastName",
    "skype",
    "email",
    "countries",
    "selection_list",
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
    "request",
    "login_form",
    "register_form",
    "user_profile",
    "user_role",
    "view_mode",
    "metis_footer",
    "excerpt",
    "headline",
    "breadcrumbs"
})
public class MetisLandingPageModel {

  @JsonProperty("js_vars")
  private List<JsVar> jsVars = null;
  @JsonProperty("fullName")
  private String fullName;
  @JsonProperty("lastName")
  private String lastName;
  @JsonProperty("skype")
  private String skype;
  @JsonProperty("email")
  private String email;
//  @JsonProperty("countries")
//  private List<CountryModel> countries = null;
//  @JsonProperty("selection_list")
//  private List<SelectionList> selectionList = null;
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
  @JsonProperty("request")
  private List<Request> request = null;
  @JsonProperty("login_form")
  private LoginForm loginForm;
  @JsonProperty("register_form")
  private RegisterForm registerForm;
  @JsonProperty("user_profile")
  private UserProfileModel userProfileModel;
  @JsonProperty("user_role")
  private UserRole userRole;
  @JsonProperty("view_mode")
  private ViewMode viewMode;
  @JsonProperty("metis_footer")
  private MetisFooter metisFooter;
  @JsonProperty("excerpt")
  private List<Excerpt> excerpt = null;
  @JsonProperty("headline")
  private List<Headline> headline = null;
  @JsonProperty("breadcrumbs")
  private List<Breadcrumb> breadcrumbs = null;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<>();

  @JsonProperty("js_vars")
  public List<JsVar> getJsVars() {
    return jsVars;
  }

  @JsonProperty("js_vars")
  public void setJsVars(List<JsVar> jsVars) {
    this.jsVars = jsVars;
  }

  @JsonProperty("fullName")
  public String getFullName() {
    return fullName;
  }

  @JsonProperty("fullName")
  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  @JsonProperty("lastName")
  public String getLastName() {
    return lastName;
  }

  @JsonProperty("lastName")
  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  @JsonProperty("skype")
  public String getSkype() {
    return skype;
  }

  @JsonProperty("skype")
  public void setSkype(String skype) {
    this.skype = skype;
  }

  @JsonProperty("email")
  public String getEmail() {
    return email;
  }

  @JsonProperty("email")
  public void setEmail(String email) {
    this.email = email;
  }

//  @JsonProperty("countries")
//  public List<CountryModel> getCountries() {
//    return countries;
//  }
//
//  @JsonProperty("countries")
//  public void setCountries(
//      List<CountryModel> countries) {
//    this.countries = countries;
//  }
//
//  @JsonProperty("selection_list")
//  public List<SelectionList> getSelectionList() {
//    return selectionList;
//  }
//
//  @JsonProperty("selection_list")
//  public void setSelectionList(
//      List<SelectionList> selectionList) {
//    this.selectionList = selectionList;
//  }

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

  @JsonProperty("request")
  public List<Request> getRequest() {
    return request;
  }

  @JsonProperty("request")
  public void setRequest(List<Request> request) {
    this.request = request;
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

  @JsonProperty("user_profile")
  public UserProfileModel getUserProfileModel() {
    return userProfileModel;
  }

  @JsonProperty("user_profile")
  public void setUserProfileModel(UserProfileModel userProfileModel) {
    this.userProfileModel = userProfileModel;
  }

  @JsonProperty("user_role")
  public UserRole getUserRole() {
    return userRole;
  }

  @JsonProperty("user_role")
  public void setUserRole(UserRole userRole) {
    this.userRole = userRole;
  }

  @JsonProperty("view_mode")
  public ViewMode getViewMode() {
    return viewMode;
  }

  @JsonProperty("view_mode")
  public void setViewMode(ViewMode viewMode) {
    this.viewMode = viewMode;
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
