package eu.europeana.metis.templates.page.dashboard;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import eu.europeana.metis.templates.CssFile;
import eu.europeana.metis.templates.JsFile;
import eu.europeana.metis.templates.MetisHeader;
import eu.europeana.metis.templates.Version;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-22
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "is_java",
    "css_files",
    "js_files",
    "jQuery",
    "input_search",
    "version",
    "welcome_message",
    "double_btns",
    "browse_menu",
    "is_dashboard",
    "page_title",
    "metis_logged_user",
    "metis_header_search",
    "metis_header"
})
public class DashboardPageModel {

  @JsonProperty("is_java")
  private Boolean isJava;
  @JsonProperty("css_files")
  private List<CssFile> cssFiles = null;
  @JsonProperty("js_files")
  private List<JsFile> jsFiles = null;
  @JsonProperty("jQuery")
  private Boolean jQuery;
  @JsonProperty("input_search")
  private InputSearch inputSearch;
  @JsonProperty("version")
  private Version version;
  @JsonProperty("welcome_message")
  private WelcomeMessage welcomeMessage;
  @JsonProperty("double_btns")
  private DoubleBtns doubleBtns;
  @JsonProperty("browse_menu")
  private BrowseMenu browseMenu;
  @JsonProperty("is_dashboard")
  private IsDashboard isDashboard;
  @JsonProperty("page_title")
  private String pageTitle;
  @JsonProperty("metis_logged_user")
  private MetisLoggedUser metisLoggedUser;
  @JsonProperty("metis_header_search")
  private Boolean metisHeaderSearch;
  @JsonProperty("metis_header")
  private MetisHeader metisHeader;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<>();

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

  @JsonProperty("jQuery")
  public Boolean getJQuery() {
    return jQuery;
  }

  @JsonProperty("jQuery")
  public void setJQuery(Boolean jQuery) {
    this.jQuery = jQuery;
  }

  @JsonProperty("input_search")
  public InputSearch getInputSearch() {
    return inputSearch;
  }

  @JsonProperty("input_search")
  public void setInputSearch(InputSearch inputSearch) {
    this.inputSearch = inputSearch;
  }

  @JsonProperty("version")
  public Version getVersion() {
    return version;
  }

  @JsonProperty("version")
  public void setVersion(Version version) {
    this.version = version;
  }

  @JsonProperty("welcome_message")
  public WelcomeMessage getWelcomeMessage() {
    return welcomeMessage;
  }

  @JsonProperty("welcome_message")
  public void setWelcomeMessage(WelcomeMessage welcomeMessage) {
    this.welcomeMessage = welcomeMessage;
  }

  @JsonProperty("double_btns")
  public DoubleBtns getDoubleBtns() {
    return doubleBtns;
  }

  @JsonProperty("double_btns")
  public void setDoubleBtns(DoubleBtns doubleBtns) {
    this.doubleBtns = doubleBtns;
  }

  @JsonProperty("browse_menu")
  public BrowseMenu getBrowseMenu() {
    return browseMenu;
  }

  @JsonProperty("browse_menu")
  public void setBrowseMenu(BrowseMenu browseMenu) {
    this.browseMenu = browseMenu;
  }

  @JsonProperty("is_dashboard")
  public IsDashboard getIsDashboard() {
    return isDashboard;
  }

  @JsonProperty("is_dashboard")
  public void setIsDashboard(IsDashboard isDashboard) {
    this.isDashboard = isDashboard;
  }

  @JsonProperty("page_title")
  public String getPageTitle() {
    return pageTitle;
  }

  @JsonProperty("page_title")
  public void setPageTitle(String pageTitle) {
    this.pageTitle = pageTitle;
  }

  @JsonProperty("metis_logged_user")
  public MetisLoggedUser getMetisLoggedUser() {
    return metisLoggedUser;
  }

  @JsonProperty("metis_logged_user")
  public void setMetisLoggedUser(MetisLoggedUser metisLoggedUser) {
    this.metisLoggedUser = metisLoggedUser;
  }

  @JsonProperty("metis_header_search")
  public Boolean getMetisHeaderSearch() {
    return metisHeaderSearch;
  }

  @JsonProperty("metis_header_search")
  public void setMetisHeaderSearch(Boolean metisHeaderSearch) {
    this.metisHeaderSearch = metisHeaderSearch;
  }

  @JsonProperty("metis_header")
  public MetisHeader getMetisHeader() {
    return metisHeader;
  }

  @JsonProperty("metis_header")
  public void setMetisHeader(MetisHeader metisHeader) {
    this.metisHeader = metisHeader;
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
