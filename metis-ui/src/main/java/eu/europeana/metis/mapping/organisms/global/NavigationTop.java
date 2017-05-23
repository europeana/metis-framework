//package eu.europeana.metis.mapping.organisms.global;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * Java model representing a navigation bar component: "/organisms/global/navigation-top-accessible".
// *
// * @author alena
// */
//public class NavigationTop {
//  private String home_url;
//  private String home_text;
//  private Map<String, String> next_prev;
//  private Boolean footer;
//  private Map<String, Object> global;
//
//  public NavigationTop(String home_url, String home_text, Boolean footer) {
//    super();
//    this.home_url = home_url;
//    this.home_text = home_text;
//    this.next_prev = new HashMap<>();
//    this.footer = footer;
//    this.global = new HashMap<>();
//  }
//
//  public void addNextPrev(String next_url, String prev_url, String results_url) {
//    next_prev.put("next_url", next_url);
//    next_prev.put("prev_url", prev_url);
//    next_prev.put("results_url", results_url);
//  }
//
//  public void addGlobal(Boolean search_active, Boolean settings_active, Boolean oursites_hidden,
//      String logoUrl, String logoText, String menuId, List<NavigationTopMenu> items,
//      List<NavigationTopMenu> utilityItems) {
//    addOptions(search_active, settings_active, oursites_hidden);
//    addLogo(logoUrl, logoText);
//    if (items != null && !items.isEmpty()) {
//      addPrimaryNavMenu(menuId, items);
//    }
//    addUtilityNavMenu(utilityItems);
//  }
//
//  public void addOptions(Boolean search_active, Boolean settings_active, Boolean oursites_hidden) {
//    Map<String, Object> options = new HashMap<>();
//    options.put("search_active", search_active);
//    options.put("settings_active", settings_active);
//    options.put("oursites_hidden", oursites_hidden);
//    this.global.put("options", options);
//  }
//
//  public void addLogo(String logoUrl, String logoText) {
//    Map<String, String> logo = new HashMap<>();
//    logo.put("url", logoUrl);
//    logo.put("text", logoText);
//    this.global.put("logo", logo);
//  }
//
//  private void addPrimaryNavMenu(String menuId, List<NavigationTopMenu> items) {
//    Map<String, Object> primary_nav = new HashMap<>();
//    primary_nav.put("items", items);
//    primary_nav.put("menu_id", menuId);
//    this.global.put("primary_nav", primary_nav);
//  }
//
//  public void addUtilityNavMenu(List<NavigationTopMenu> utilityItems) {
//    this.global.remove("utility_nav");
//    Map<String, Object> utility_nav = new HashMap<>();
//    utility_nav.put("menu_id", "settings-menu");
//    utility_nav.put("style_modifier", "caret-right");
//    utility_nav.put("tabindex", "6");
//    List<Map<String, Object>> utility_nav_items = new ArrayList<>();
//    Map<String, Object> utility_nav_title = new HashMap<>();
//    utility_nav_title.put("url", "#");
//    utility_nav_title.put("text", "Sign In");
//    utility_nav_title.put("icon_class", "svg-icon-user-signin");
//    utility_nav_title.put("fontawesome", true);
//    utility_nav_title.put("icon", "users");
//
//    Map<String, List<NavigationTopMenu>> submenu = new HashMap<>();
//    submenu.put("items", utilityItems);
//    utility_nav_title.put("submenu", submenu);
//
//    utility_nav_items.add(utility_nav_title);
//    utility_nav.put("items", utility_nav_items);
//    this.global.put("utility_nav", utility_nav);
//  }
//
//  public String getHome_url() {
//    return home_url;
//  }
//
//  public void setHome_url(String home_url) {
//    this.home_url = home_url;
//  }
//
//  public String getHome_text() {
//    return home_text;
//  }
//
//  public void setHome_text(String home_text) {
//    this.home_text = home_text;
//  }
//
//  public Map<String, String> getNext_prev() {
//    return next_prev;
//  }
//
//  public void setNext_prev(Map<String, String> next_prev) {
//    this.next_prev = next_prev;
//  }
//
//  public Boolean getFooter() {
//    return footer;
//  }
//
//  public void setFooter(Boolean footer) {
//    this.footer = footer;
//  }
//
//  public Map<String, Object> getGlobal() {
//    return global;
//  }
//
//  public void setGlobal(Map<String, Object> global) {
//    this.global = global;
//  }
//}
