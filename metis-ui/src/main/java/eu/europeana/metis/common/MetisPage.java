package eu.europeana.metis.common;

import de.flapdoodle.embed.process.collections.Collections;
import eu.europeana.metis.page.HeaderSubMenuBuilder;
import eu.europeana.metis.templates.CssFile;
import eu.europeana.metis.templates.Footer;
import eu.europeana.metis.templates.FooterNavigation;
import eu.europeana.metis.templates.Global;
import eu.europeana.metis.templates.JsFile;
import eu.europeana.metis.templates.JsVar;
import eu.europeana.metis.templates.ListOfLinks;
import eu.europeana.metis.templates.Logo;
import eu.europeana.metis.templates.MenuItem;
import eu.europeana.metis.templates.MetisFooter;
import eu.europeana.metis.templates.MetisHeader;
import eu.europeana.metis.templates.Navigation;
import eu.europeana.metis.templates.NextPrev;
import eu.europeana.metis.templates.Options;
import eu.europeana.metis.templates.Social;
import eu.europeana.metis.templates.SubFooter;
import eu.europeana.metis.templates.Submenu;
import eu.europeana.metis.templates.SubmenuItem;
import eu.europeana.metis.templates.UtilityNav;
import eu.europeana.metis.templates.page.landingpage.Breadcrumb;
import eu.europeana.metis.templates.page.landingpage.I18n;
import eu.europeana.metis.templates.page.landingpage.I18nGlobal;
import eu.europeana.metis.templates.page.landingpage.Newsletter;
import eu.europeana.metis.ui.mongo.domain.UserDTO;
import java.util.Arrays;
import java.util.List;

/**
 * This is common Metis page with the same assets, bread-crumbs and header instantiated.
 *
 * @author alena
 */
public abstract class MetisPage implements AbstractMetisPage {
  protected UserDTO userDTO;

  MetisuiConfig config;

  public MetisPage(MetisuiConfig config) {
      this.config = config;
  }

  @Override
  public List<CssFile> resolveCssFiles() {
    CssFile cssFile1 = new CssFile();
    cssFile1.setPath(config.getCssRoot() + "/css/pandora/screen.css");
    cssFile1.setMedia("all");

    CssFile cssFile2 = new CssFile();
    cssFile2.setPath("http://netdna.bootstrapcdn.com/font-awesome/4.2.0/css/font-awesome.css");
    cssFile2.setMedia("all");

    return Arrays.asList(cssFile1, cssFile2);
  }

  @Override
  public List<JsFile> resolveJsFiles() {
    JsFile jsFile = new JsFile();
    jsFile.setPath(config.getScriptRoot()+"/js/modules/require.js");
    jsFile.setDataMain(config.getScriptRoot() + "/js/modules/main/templates/main-pandora");

    return Arrays.asList(jsFile);
  }

  @Override
  public List<JsVar> resolveJsVars() {
    JsVar jsVar = new JsVar();
    jsVar.setName("pageName");
    jsVar.setValue("portal/index");
    return Collections.singletonList(jsVar);
  }

  @Override
  public List<Breadcrumb> resolveBreadcrumbs() {
    Breadcrumb breadcrumb = new Breadcrumb();
    breadcrumb.setText("Home");
    breadcrumb.setUrl("/");
    return Collections.singletonList(breadcrumb);
  }

  @Override
  public MetisHeader buildMetisHeader() {
    Options options = new Options();
    options.setSearchActive(false);
    options.setSettingsActive(true);
    options.setOursitesHidden(true);
    Logo logo = new Logo();
    logo.setUrl("#");
    logo.setText("Europeana Pandora");

    MenuItem menuItem = new MenuItem();
    menuItem.setText("Sign In");
    menuItem.setUrl("#");
    menuItem.setFontawesome(true);
    menuItem.setIcon("users");
    menuItem.setIconClass("svg-icon-user-signin");
    menuItem.setSubmenu(buildNavigationSubmenu());

    UtilityNav utilityNav = new UtilityNav();
    utilityNav.setMenuId("settings-menu");
    utilityNav.setStyleModifier("caret-right");
    utilityNav.setTabindex("6");
    utilityNav.setItems(Collections.singletonList(menuItem));

    Global global = new Global();
    global.setOptions(options);
    global.setLogo(logo);
    global.setUtilityNav(utilityNav);
    NextPrev nextPrev = new NextPrev();
    nextPrev.setPrevUrl("prev_url_here");
    nextPrev.setNextUrl("next_url_here");
    nextPrev.setResultsUrl("results_url_here");
    Navigation navigation = new Navigation();
    navigation.setHomeUrl("#");
    navigation.setHomeUrl("Home");
    navigation.setNextPrev(nextPrev);
    navigation.setFooter(true);
    navigation.setGlobal(global);
    MetisHeader metisHeader = new MetisHeader();
    metisHeader.setNavigation(navigation);

    return metisHeader;
  }

  private boolean isUserAuthorized() {
    return
        userDTO.notNullUser() && userDTO.getLdapUser().getEmail() != null && userDTO.getLdapUser().getEmail() != null;
  }

  public Submenu buildNavigationSubmenu() {
    if (isUserAuthorized()) {
      return HeaderSubMenuBuilder.buildMenuWhenAuthorized();
    } else {
      return HeaderSubMenuBuilder.buildMenuWhenNotAuthorized();
    }
  }

  @Override
  public MetisFooter buildMetisFooter() {
    MetisFooter metisFooter = new MetisFooter();
    metisFooter.setNavigation(getFooterNavigation());
    return metisFooter;
  }

  private FooterNavigation getFooterNavigation() {
    Footer footer = new Footer();
    footer.setSubfooter(getSubFooter());

    ListOfLinks listOfLinks1 = new ListOfLinks();
    listOfLinks1.setTitle("More Info");
    listOfLinks1.setItems(Arrays.asList(
            getSubmenuItem("About", "/"),
            getSubmenuItem("Development updates", "/"),
            getSubmenuItem("All institutions", "/"),
            getSubmenuItem("Become our partner", "/"),
            getSubmenuItem("Contact us", "/")));

    ListOfLinks listOfLinks2 = new ListOfLinks();
    listOfLinks2.setTitle("Help");
    listOfLinks2
        .setItems(Arrays.asList(
            getSubmenuItem("Search tips", "/"),
            getSubmenuItem("Terms of Use & Policies", "/")));
    ListOfLinks listOfLinks3 = new ListOfLinks();

    listOfLinks3.setTitle("Tools");
    listOfLinks3
        .setItems(Arrays.asList(
            getSubmenuItem("API Docs", "/"),
            getSubmenuItem("Status", "/")));

    footer.setLinklist1(listOfLinks1);
    footer.setLinklist2(listOfLinks2);
    footer.setLinklist3(listOfLinks3);
    footer.setSocial(setupSocial());

    FooterNavigation footerNavigation = new FooterNavigation();
    footerNavigation.setFooter(footer);
    return footerNavigation;
  }

  private SubFooter getSubFooter() {
    SubFooter subFooter = new SubFooter();
    subFooter
        .setItems(Arrays.asList(
            getSubmenuItem("Home", "/"),
            getSubmenuItem("Terms of use and policies", "http://europeana.eu/portal/rights/terms-and-policies.html"),
            getSubmenuItem("Contact us", "/"),
            getSubmenuItem("Sitemap", "/")));
    return subFooter;
  }

  private SubmenuItem getSubmenuItem(String text, String url) {
    SubmenuItem submenuItem = new SubmenuItem();
    submenuItem.setText(text);
    submenuItem.setUrl(url);
    return submenuItem;
  }

  private Social setupSocial() {
    Social social = new Social();
    social.setFacebook(true);
    social.setGithub(false);
    social.setGoogleplus(true);
    social.setLinkedin(false);
    social.setPinterest(true);
    social.setTwitter(true);
    return social;
  }

  protected I18n buildI18n() {
    I18nGlobal i18nGlobal = new I18nGlobal();
    i18nGlobal.setFindUsElsewhere("Find us elsewhere");
    i18nGlobal.setMission(
        "We transform the world with culture! We want to build on Europe’s rich heritage and make it easier for people to use, whether for work, for learning or just for fun.");
    i18nGlobal.setMissionTitle("Our mission");
    i18nGlobal.setNewsletter(createNewsLetter());
    I18n i18n = new I18n();
    i18n.setGlobal(i18nGlobal);

    return i18n;
  }

  private Newsletter createNewsLetter() {
    Newsletter newsletter = new Newsletter();
    newsletter.setChooseLanguage("Choose a language");
    newsletter.setEmailAddressInvalid("Please enter a valid email address.");
    newsletter.setEmailAddressRequired("Please enter your email address.");
    newsletter.setLanguageRequired("Please choose a language for your newsletter.");
    newsletter.setSignup("Sign up for our newsletter");
    newsletter.setSubmitAlt("Subscribe");

    return newsletter;
  }
}
