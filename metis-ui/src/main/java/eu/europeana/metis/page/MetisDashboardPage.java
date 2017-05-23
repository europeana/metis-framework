package eu.europeana.metis.page;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.flapdoodle.embed.process.collections.Collections;
import eu.europeana.metis.common.MetisPage;
import eu.europeana.metis.mapping.organisms.pandora.UserProfile;
import eu.europeana.metis.templates.page.dashboard.BrowseMenu;
import eu.europeana.metis.templates.Content;
import eu.europeana.metis.templates.page.dashboard.DashboardPageModel;
import eu.europeana.metis.templates.page.dashboard.DoubleBtns;
import eu.europeana.metis.templates.Global;
import eu.europeana.metis.templates.page.dashboard.InputSearch;
import eu.europeana.metis.templates.page.dashboard.IsDashboard;
import eu.europeana.metis.templates.Logo;
import eu.europeana.metis.templates.MenuItem;
import eu.europeana.metis.templates.MetisHeader;
import eu.europeana.metis.templates.page.dashboard.MetisLoggedUser;
import eu.europeana.metis.templates.Navigation;
import eu.europeana.metis.templates.Options;
import eu.europeana.metis.templates.Submenu;
import eu.europeana.metis.templates.SubmenuItem;
import eu.europeana.metis.templates.Version;
import eu.europeana.metis.templates.page.dashboard.WelcomeMessage;
import java.util.Map;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-01
 */
public class MetisDashboardPage extends MetisPage {
  private UserProfile userProfile;

  public MetisDashboardPage(UserProfile userProfile) {
    this.userProfile = userProfile;
  }

  @Override
  public Map<String, Object> buildModel() {
    DashboardPageModel dashboardPageModel = new DashboardPageModel();
    dashboardPageModel.setIsJava(true);
    dashboardPageModel.setCssFiles(resolveCssFiles());
    dashboardPageModel.setJsFiles(resolveJsFiles());
    dashboardPageModel.setJQuery(false);
    dashboardPageModel.setInputSearch(createInputSearch());
    dashboardPageModel.setVersion(createVersion());
    dashboardPageModel.setWelcomeMessage(createWelcomeMessage());
    dashboardPageModel.setDoubleBtns(createDoubleBtns());
    dashboardPageModel.setBrowseMenu(createBrowseMenu());
    dashboardPageModel.setIsDashboard(createIsDashboard());
    dashboardPageModel.setPageTitle("Europeana Dashboard");
    dashboardPageModel.setMetisLoggedUser(createMetisLoggedUser());
    dashboardPageModel.setMetisHeaderSearch(true);
    dashboardPageModel.setMetisHeader(createMetisHeader());

    ObjectMapper m = new ObjectMapper();
    Map<String,Object> modelMap = m.convertValue(dashboardPageModel, Map.class);
    return modelMap;
  }

  @Override
  public void addPageContent() {

  }

  private InputSearch createInputSearch()
  {
    InputSearch inputSearch = new InputSearch();
    inputSearch.setTitle("Search for dataset");
    inputSearch.setInputName("q");
    inputSearch.setPlaceholder("Search for dataset");

    return inputSearch;
  }

  private Version createVersion()
  {
    Version version = new Version();
    version.setIsAlpha(false);
    version.setIsBeta(true);

    return version;
  }

  private WelcomeMessage createWelcomeMessage()
  {
    WelcomeMessage welcomeMessage = new WelcomeMessage();
    welcomeMessage.setTextEnd("Welcome ");
    welcomeMessage.setUserName(userProfile.getFirstName());
    welcomeMessage.setTextFirst(" let's ingest a world of Culture!");

    return welcomeMessage;
  }

  private DoubleBtns createDoubleBtns()
  {
    DoubleBtns doubleBtns = new DoubleBtns();
    doubleBtns.setBtnLeftText("New Dataset");
    doubleBtns.setBtnRightText("New Organization");
    doubleBtns.setBtnRightUnderText("Go to ZOHO");
    doubleBtns.setUrlLeft("http://www.cnn.com");
    doubleBtns.setUrlRight("http://www.europeana.eu");

    return doubleBtns;
  }

  private BrowseMenu createBrowseMenu()
  {
    SubmenuItem submenuItem1 = new SubmenuItem();
    SubmenuItem submenuItem2 = new SubmenuItem();
    SubmenuItem submenuItem3 = new SubmenuItem();
    SubmenuItem submenuItem4 = new SubmenuItem();
    submenuItem1.setText("Dataset");
    submenuItem1.setUrl("javascript:alert('images')");
    submenuItem1.setIcon("icon-image");
    submenuItem2.setText("User");
    submenuItem2.setUrl("javascript:alert('videos')");
    submenuItem2.setIcon("icon-video");
    submenuItem3.setText("Organization");
    submenuItem3.setUrl("javascript:alert('sounds')");
    submenuItem3.setIcon("icon-music");
    submenuItem4.setText("All");
    submenuItem4.setUrl("javascript:alert('texts')");
    submenuItem4.setIcon("icon-openbook");
    Submenu submenu = new Submenu();
    submenu.setItems(Collections.newArrayList(submenuItem1, submenuItem2, submenuItem3, submenuItem4));


    MenuItem menuItem = new MenuItem();
    menuItem.setUrl("#");
    menuItem.setText("Datasets");
    menuItem.setTextMobile("or browse");
    menuItem.setSubmenu(submenu);

    BrowseMenu browseMenu = new BrowseMenu();
    browseMenu.setMenuId("metis_search_menu");
    browseMenu.setItems(Collections.newArrayList(menuItem));

    return browseMenu;
  }

  private IsDashboard createIsDashboard()
  {
    IsDashboard isDashboard = new IsDashboard();
    isDashboard.setContent(new Content());

    return isDashboard;
  }

  private MetisHeader createMetisHeader()
  {
    Options options = new Options();
    options.setSearchActive(false);
    options.setSettingsActive(true);
    options.setOursitesHidden(true);
    Logo logo = new Logo();
    logo.setUrl("#");
    logo.setText("Europeana Pandora");
    Global global = new Global();
    global.setOptions(options);
    global.setLogo(logo);
    Navigation navigation = new Navigation();
    navigation.setGlobal(global);
    MetisHeader metisHeader = new MetisHeader();
    metisHeader.setNavigation(navigation);

    return metisHeader;
  }

  private MetisLoggedUser createMetisLoggedUser(){
    MetisLoggedUser metisLoggedUser = new MetisLoggedUser();
    metisLoggedUser.setMenuId("loggedin-user");
    metisLoggedUser.setIconClass("svg-icon-loggedin-user");

    SubmenuItem metisLoggedUserSubmenuItem1 = new SubmenuItem();
    SubmenuItem metisLoggedUserSubmenuItem2 = new SubmenuItem();
    metisLoggedUserSubmenuItem1.setText("My Profile");
    metisLoggedUserSubmenuItem1.setUrl("/profile");
    metisLoggedUserSubmenuItem1.setIcon("icon-image");
    metisLoggedUserSubmenuItem2.setText("Log out");
    metisLoggedUserSubmenuItem2.setUrl("/logout");
    metisLoggedUserSubmenuItem2.setIcon("icon-video");
    Submenu metisLoggedUserSubmenu = new Submenu();
    metisLoggedUserSubmenu.setItems(Collections.newArrayList(metisLoggedUserSubmenuItem1, metisLoggedUserSubmenuItem2));
    MenuItem metisLoggedUserMenuItem = new MenuItem();
    metisLoggedUserMenuItem.setUrl("#");
    metisLoggedUserMenuItem.setText("");
    metisLoggedUserMenuItem.setTextMobile("or browse");
    metisLoggedUserMenuItem.setSubmenu(metisLoggedUserSubmenu);
    metisLoggedUser.setItems(Collections.newArrayList(metisLoggedUserMenuItem));

    return metisLoggedUser;
  }
}
