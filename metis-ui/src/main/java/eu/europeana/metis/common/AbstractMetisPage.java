package eu.europeana.metis.common;

import eu.europeana.metis.templates.page.landingpage.Breadcrumb;
import eu.europeana.metis.templates.CssFile;
import eu.europeana.metis.templates.JsFile;
import eu.europeana.metis.templates.JsVar;
import eu.europeana.metis.templates.MetisFooter;
import eu.europeana.metis.templates.MetisHeader;
import eu.europeana.metis.page.PageView;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Abstract Metis application page.
 *
 * @author alena
 */
public abstract class AbstractMetisPage {
  private static final String is_java = "is_java";
  private static final String css_files = "css_files";
  private static final String js_files = "js_files";
  private static final String js_vars = "js_vars";
  private static final String metis_header = "metis_header";
  private static final String metis_footer = "metis_footer";
  private static final String navigation = "navigation";
  private static final String bread_crumbs = "breadcrumbs";
  private static final String page_title = "page_title";
  private static final String image_root = "image_root";
  private static final String i18n = "i18n";
  private static final String version = "version";
  private static final String headline = "headline";
  private static final String excerpt = "excerpt";
  private List<CssFile> cssFiles;
  private List<JsFile> jsFiles;
  private List<Entry<String, String>> jsVars;
  private List<Entry<String, String>> breadcrumbs;

  /**
   * Welcome message head and tail.
   */
  protected static final String text_first = "Welcome ";
  protected static final String text_end = " let's ingest a world of Culture!";

  /**
   * @return a Mapping-To-EDM page Java model.
   */
  public abstract Map<String, Object> buildModel();
//  {
//    Map<String, Object> modelMap = new HashMap<>();
//
//    //global settings, assets, breadcrumbs
//    initAssetsAndBreadcrumbs();
//    modelMap.put(is_java, true);
//    modelMap.put(page_title, "Europeana Metis");
//
//    modelMap.put(image_root, "https://europeanastyleguidetest.a.cdnify.io");
//
//    ObjectMapper m = new ObjectMapper();
//    List<Object> props = m.convertValue(cssFiles, List.class);
//    modelMap.put(css_files, props);
//    props = m.convertValue(jsFiles, List.class);
//    modelMap.put(js_files, props);
//    modelMap.put(js_vars, MetisMappingUtil.buildSimplePairs(jsVars, "name", "value"));
//    modelMap.put(bread_crumbs, MetisMappingUtil.buildSimplePairs(breadcrumbs, "text", "url"));
//
//    //newsletter
//    Map<String, Boolean> page_config = new HashMap<>();
//    page_config.put("newsletter", true);
//    modelMap.put("page_config", page_config);
//
//    //"beta" label
//    Map<String, String> versions = new HashMap<>();
//    versions.put("is_alpha", "false");
//    versions.put("is_beta", "true");
//    modelMap.put(version, versions);
//
//    //site-hero
//    //"beta" label
//    List<Entry<String, String>> headlines = Arrays.asList(
//        new AbstractMap.SimpleEntry<String, String>("short", "false"),
//        new AbstractMap.SimpleEntry<String, String>("medium", "false"),
//        new AbstractMap.SimpleEntry<String, String>("long", "false"));
//    modelMap.put(headline, headlines);
//    //"beta" label
//    List<Entry<String, String>> excerpts = Arrays.asList(
//        new AbstractMap.SimpleEntry<String, String>("vshort", "false"),
//        new AbstractMap.SimpleEntry<String, String>("short", "false"),
//        new AbstractMap.SimpleEntry<String, String>("medium", "false"),
//        new AbstractMap.SimpleEntry<String, String>("long", "false"));
//    modelMap.put(excerpt, excerpts);
//
//    //page header
//    Map<String, Object> navigationMap = new HashMap<>();
//    navigationMap.put(navigation, buildHeader());
//    modelMap.put(metis_header, navigationMap);
//
//    //our sites
//    Map<String, Object> ourSites = new HashMap<>();
////		ourSites.put("our-sites", "Our Sites");
//    ourSites.put("mission",
//        "We transform the world with culture! We want to build on Europe’s "
//            + "rich heritage and make it easier for people to use, whether for work, "
//            + "for learning or just for fun.");
//    ourSites.put("mission-title", "Our mission");
//
//    //newsletter
//    Map<String, String> newsletter = new HashMap<>();
//    newsletter.put("signup", "Sign up for our newsletter");
//    newsletter.put("submit-alt", "Subscribe");
//    newsletter.put("choose-language", "Choose a language");
//    newsletter.put("email-address-invalid", "Please enter a valid email address.");
//    newsletter.put("email-address-required", "Please enter your email address.");
//    newsletter.put("language-required", "Please choose a language for your newsletter.");
//    ourSites.put("newsletter", newsletter);
//    ourSites.put("find-us-elsewhere", "Find us elsewhere");
//
//    Map<String, Map<String, Object>> i18nMap = new HashMap<>();
//    i18nMap.put("global", ourSites);
//    modelMap.put(i18n, i18nMap);
//
//    //body of the page
//    addPageContent(modelMap);
//
//    //footer
//    modelMap.put(metis_footer, buildFooter());
//
//    return modelMap;
//  }

//  public abstract void initAssetsAndBreadcrumbs();
//  {
//    this.cssFiles = resolveCssFilesClass();
//    this.jsFiles = resolveJsFilesClass();
//    this.jsVars = resolveJsVars();
//    this.breadcrumbs = resolveBreadcrumbs();
//  }

  /**
   * @return list of css-files to apply for a Metis page.
   */
  public abstract List<CssFile> resolveCssFiles();
  public abstract List<CssFile> resolveCssFilesClass();

  /**
   * @return list of js-files to apply for a Metis page.
   */
  public abstract List<JsFile> resolveJsFiles();
  public abstract List<JsFile> resolveJsFilesClass();

  /**
   * @return list of js-vars to apply for a Metis page.
   */
  public abstract List<JsVar> resolveJsVars();

  /**
   * @return list of breadcrumbs for Metis page.
   */
  public abstract List<Breadcrumb> resolveBreadcrumbs();

  public abstract void addPageContent();

  /**
   * @return Metis header object model.
   */
  public abstract MetisHeader buildHeader(PageView pageView);

  /**
   * @return Metis footer object model.
   */
  public abstract MetisFooter buildFooter();
}
