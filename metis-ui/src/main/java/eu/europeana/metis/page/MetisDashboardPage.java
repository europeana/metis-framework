package eu.europeana.metis.page;

import eu.europeana.metis.common.MetisPage;
import eu.europeana.metis.mapping.organisms.global.NavigationTop;
import eu.europeana.metis.mapping.organisms.global.NavigationTopMenu;
import eu.europeana.metis.mapping.organisms.pandora.UserProfile;
import eu.europeana.metis.mapping.util.MetisMappingUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-01
 */
public class MetisDashboardPage extends MetisPage {
  private UserProfile userProfile;
  private static final String is_java = "is_java";
  private static final String css_files = "css_files";
  private static final String js_files = "js_files";
  private static final String js_vars = "js_vars";
  private static final String version = "version";
  private static final String page_title = "page_title";
  private static final String navigation = "navigation";
  private static final String metis_header = "metis_header";
  private static final String image_root = "image_root";
  private static final String metis_footer = "metis_footer";

  private List<Entry<String, String>> cssFiles;
  private List<Entry<String, String>> jsFiles;
  private List<Entry<String, String>> jsVars;

  public MetisDashboardPage(UserProfile userProfile) {
    this.userProfile = userProfile;
  }

  @Override
  public Byte resolveCurrentPage() {
    return null;
  }

  @Override
  public List<NavigationTopMenu> buildUtilityNavigation() {
    return null;
  }

  @Override
  public void addPageContent(Map<String, Object> model) {
  }

  @Override
  public Map<String, Object> buildModel() {
    Map<String, Object> modelMap = new HashMap<>();

    //global settings, assets, breadcrumbs
    initAssetsAndBreadcrumbs();
    modelMap.put(is_java, true);
    modelMap.put(page_title, "Europeana Dashboard");
    modelMap.put(image_root, "https://europeanastyleguidetest.a.cdnify.io");

    modelMap.put(css_files, MetisMappingUtil.buildSimplePairs(cssFiles, "path", "media"));
    modelMap.put(js_files, MetisMappingUtil.buildSimplePairs(jsFiles, "path", "data_main"));
    modelMap.put(js_vars, MetisMappingUtil.buildSimplePairs(jsVars, "name", "value"));

    Map<String, String> inputSearch = new HashMap<>();
    inputSearch.put("title", "Search for dataset");
    inputSearch.put("input_name", "q");
    inputSearch.put("placeholder", "Search for dataset");
    modelMap.put("input_search", inputSearch);

    //"beta" label
    Map<String, String> versions = new HashMap<>();
    versions.put("is_alpha", "false");
    versions.put("is_beta", "true");
    modelMap.put(version, versions);

    Map<String, String> welcomeMessage = new HashMap<>();
    welcomeMessage.put("text_first", "Welcome ");
    welcomeMessage.put("user_name", userProfile.getGivenName());
    welcomeMessage.put("text_end", " let's ingest a world of Culture!");
    modelMap.put("welcome_message", welcomeMessage);

    Map<String, String> doubleBtns = new HashMap<>();
    doubleBtns.put("btn-left-text", "New Dataset");
    doubleBtns.put("btn-right-text", "New Organization");
    doubleBtns.put("btn-right-under-text", "Go to ZOHO");
    doubleBtns.put("url-left", "http://www.cnn.com");
    doubleBtns.put("url-right", "http://www.europeana.eu");
    modelMap.put("double-btns", doubleBtns);

    HashMap<String, String> subMenuItem1 = new HashMap<>();
    subMenuItem1.put("text", "Dataset");
    subMenuItem1.put("url", "javascript:alert('images')");
    subMenuItem1.put("icon", "icon-image");
    HashMap<String, String> subMenuItem2 = new HashMap<>();
    subMenuItem2.put("text", "User");
    subMenuItem2.put("url", "javascript:alert('videos')");
    subMenuItem2.put("icon", "icon-video");
    HashMap<String, String> subMenuItem3 = new HashMap<>();
    subMenuItem3.put("text", "Organization");
    subMenuItem3.put("url", "javascript:alert('sounds')");
    subMenuItem3.put("icon", "icon-music");
    HashMap<String, String> subMenuItem4 = new HashMap<>();
    subMenuItem4.put("text", "All");
    subMenuItem4.put("url", "javascript:alert('texts')");
    subMenuItem4.put("icon", "icon-openbook");
    List<Map<String,String>> subMenuItemList = Arrays.asList(subMenuItem1, subMenuItem2, subMenuItem3, subMenuItem4);
    Map<String, Object> subMenuItems = new HashMap<>();
    subMenuItems.put("items", subMenuItemList);

    Map<String,Object> items = new HashMap<>();
    items.put("url", "#");
    items.put("text", "Datasets");
    items.put("text_mobile", "or browse");
    items.put("submenu", subMenuItems);

    List<Object> itemsList = new ArrayList<>();
    itemsList.add(items);

    Map<String, Object> browseMenu = new HashMap<>();
    browseMenu.put("menu_id", "browse-menu");
    browseMenu.put("items", itemsList);
    modelMap.put("browse_menu", browseMenu);

    Map<String, Object> isDashboard = new HashMap<>();
    isDashboard.put("content", new HashMap<String, Object>());
    modelMap.put("is_dashboard", isDashboard);

    modelMap.put("metis-logged-user", true);
    modelMap.put("metis-header-search", true);
    //page header
    Map<String, Object> navigationMap = new HashMap<>();
    navigationMap.put(navigation, buildHeader());
    modelMap.put(metis_header, navigationMap);

    //footer
    modelMap.put(metis_footer, buildFooter());

    return modelMap;
  }

  @Override
  public NavigationTop buildHeader() {
    //commented for the new design!
    NavigationTop header = new NavigationTop("#", "Home", true);
    header.addOptions(false, true, true);
    header.addLogo("#", "Europeana Metis");
    return header;
  }

  private void initAssetsAndBreadcrumbs() {
    this.cssFiles = resolveCssFiles();
    this.jsFiles = resolveJsFiles();
    this.jsVars = resolveJsVars();
  }
}
