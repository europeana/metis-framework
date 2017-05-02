package eu.europeana.metis.page;

import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import eu.europeana.metis.common.MetisPage;
import eu.europeana.metis.framework.common.Country;
import eu.europeana.metis.mapping.atoms.UserRequest;
import eu.europeana.metis.mapping.organisms.global.NavigationTopMenu;
import eu.europeana.metis.mapping.organisms.pandora.UserProfile;
import eu.europeana.metis.mapping.util.MetisMappingUtil;

/**
 * This web-page represents a Metis Landing page and all user account pages like: Login page,
 * User Profile page, Register User page.
 *
 * @author alena
 */
public class MetisLandingPage extends MetisPage {

  private PageView pageView = PageView.HOME;
  private UserProfile user;
  private List<UserRequest> request;
  private Boolean isDuplicateUser = false;
  private Boolean isAuthError = false;
  private static final String ERROR_DUPLICATE_USER = "ERROR: The user with this email address already exists.";
  private static final String ERROR_AUTH = "ERROR: Your email or password is incorrect. Try again please.";
  private Map<String, Object> contentMap;

  @Override
  public Byte resolveCurrentPage() {
    return -1;
  }

  public MetisLandingPage(PageView pageView) {
    super();
    this.pageView = pageView;
  }

  public MetisLandingPage(PageView pageView, UserProfile user) {
    this(pageView);
    this.user = user;
  }

  public MetisLandingPage(PageView pageView, List<UserRequest> userRequests) {
    this(pageView);
    this.pageView = pageView;
    this.request = userRequests;
  }

  /**
   * FIXME when the user profile form is unified with user approve form this constructor will be
   * removed
   *
   * @boolean isApprove - just to distinguish this constructor from the other
   */
  public MetisLandingPage(PageView pageView, UserProfile user, boolean isApprove) {
    this(pageView);
    this.pageView = pageView;
    this.user = user;
  }

  @Override
  public void addPageContent(Map<String, Object> model) {
    if (this.contentMap == null) {
      this.contentMap = new HashMap<>();
    }
    switch (pageView) {
      case HOME:
        contentMap.put("is_home", true);
        buildHomePageContent();
        break;
      case LOGIN:
        contentMap.put("is_login", true);
        buildLoginPageContent();
        break;
      case REGISTER:
        contentMap.put("is_register", true);
        buildRegisterPageContent();
        break;
      case PROFILE:
        contentMap.put("is_profile", true);
        buildProfilePageContent();
        break;
      case REQUESTS:
        contentMap.put("is_requests", true);
        buildRequestsPageContent();
        break;
      case USER_APPROVE:
        contentMap.put("is_request_approve", true);
        buildApproveRequestsPageContent();
        break;
      default:
        break;
    }

    model.put("landing_page_content", contentMap);
  }

  /**
   * In the current Metis design there is no need for the bread-crumbs.
   */
  @Override
  public List<Entry<String, String>> resolveBreadcrumbs() {
    List<Entry<String, String>> breadcrumbs = new ArrayList<>();
    breadcrumbs.add(new SimpleEntry<String, String>("Home", "/"));
    return breadcrumbs;
  }

  @Override
  public List<NavigationTopMenu> buildUtilityNavigation() {
    List<NavigationTopMenu> utilityNavigationMenu = new ArrayList<>();
    switch (this.pageView) {
      case REGISTER:
        utilityNavigationMenu.add(new NavigationTopMenu("Login", "/login", true));
        utilityNavigationMenu.add(new NavigationTopMenu("Home", "/", true));
        break;
      case LOGIN:
        utilityNavigationMenu.add(new NavigationTopMenu("Register", "/register", false));
        utilityNavigationMenu.add(new NavigationTopMenu("Home", "/", true));
        break;
      case PROFILE:
        utilityNavigationMenu.add(new NavigationTopMenu("Logout", "/logout", false));
        utilityNavigationMenu.add(new NavigationTopMenu("Home", "/", true));
        break;
      case REQUESTS:
        utilityNavigationMenu.add(new NavigationTopMenu("Logout", "/logout", false));
        utilityNavigationMenu.add(new NavigationTopMenu("Home", "/", true));
        break;
      default:
        if (user != null && user.getEmail() != null) {
          utilityNavigationMenu.add(new NavigationTopMenu("Profile", "/profile", true));
          utilityNavigationMenu.add(new NavigationTopMenu("Logout", "/logout", true));
        } else {
          utilityNavigationMenu.add(new NavigationTopMenu("Login", "/login", true));
          utilityNavigationMenu.add(new NavigationTopMenu("Register", "/register", false));
        }
        break;
    }
    return utilityNavigationMenu;
  }


  /**
   * The content for the User Login page.
   */
  private void buildHomePageContent() {
    Map<String, String> hero_config = new HashMap<>();
    hero_config.put("hero_image",
        "https://europeana-styleguide-test.s3.amazonaws.com/images/metis/hero_metis_1600x650_jade.png");
    hero_config.put("brand_colour", "brand-colour-site");
    hero_config.put("brand_position", "brand-bottomleft");
    hero_config.put("brand_opacity", "brand-opacity100");
    hero_config.put("attribution_text",
        "Cyclopides metis L., Cyclopides qua... Museum Fur Naturkunde Berlin");
    hero_config.put("attribution_url",
        "http://www.europeana.eu/portal/fr/record/11622/_MFN_DRAWERS_MFN_GERMANY_http___coll_mfn_berlin_de_u_MFNB_Lep_Hesperiidae_D146.html");
    hero_config.put("license_CC0", "true");
//		hero_config.put("license_public", "false");

    Map<String, String> banner = new HashMap<>();
    banner.put("title", "What can you do with Metis?");
    banner.put("text",
        "Ever wondered how to automatically digest huge amounts of data with the push of a button?");
    banner.put("info_link", "Learn more about Metis");
    banner.put("info_url", "#");
    banner.put("cta_url", "#");
    banner.put("cta_text", "Register to metis here");

    Map<String, Map<String, String>> content = new HashMap<>();
    content.put("hero_config", hero_config);
    content.put("banner", banner);
    contentMap.put("content", content);
  }

  /**
   * The content for the User Login page.
   */
  private void buildLoginPageContent() {
    if (isAuthError) {
      contentMap.put("login_err_authenticate", ERROR_AUTH);
    }
    if (this.user == null) {
      return;
    }
    String email = this.user.getEmail();
    contentMap.put("email", email);

//		byte[] password = user.getPassword();
//		contentMap.put("password", password);

  }

  /**
   * The content for the Register User page.
   */
  private void buildRegisterPageContent() {
    if (isDuplicateUser) {
      contentMap.put("register_err_duplicate_user", ERROR_DUPLICATE_USER);
    }
  }

  /**
   * The content for the User Profile page.
   */
  private void buildProfilePageContent() {
    if (this.user == null) {
      return;
    }
    String email = this.user.getEmail();
    contentMap.put("email", email);

    String fullName = user.getGivenName();
    contentMap.put("fullName", fullName);

    String lastName = user.getLastName();
    contentMap.put("lastName", lastName);

    if (user instanceof UserProfile) {
      contentMap.put("skype", ((UserProfile) user).getSkype());
      //TODO add other DBUser fields.
    }
    Country userCountry = Country.toCountry(this.user.getCountry());
    List<Map<String, String>> countries = new ArrayList<>();
    for (Country c : Country.values()) {
      Map<String, String> country = new HashMap<>();
      if (userCountry != null && userCountry.getName().equals(c.getName())) {
        country.put("selected", "selected");
      }
      country.put("value", c.getIsoCode());
      country.put("text", c.getName());
      countries.add(country);
    }
    contentMap.put("countries", countries);
  }


  private void buildRequestsPageContent() {
    if (this.request == null) {
      return;
    } else {
      contentMap.put("request", this.request);
    }
  }

  private void buildApproveRequestsPageContent() {
    if (this.user == null) {
      return;
    }
    String email = this.user.getEmail();
    contentMap.put("email", email);

    String fullName = user.getGivenName();
    contentMap.put("fullName", fullName);

    String lastName = user.getLastName();
    contentMap.put("lastName", lastName);

    if (user instanceof UserProfile) {
      contentMap.put("skype", ((UserProfile) user).getSkype());
      //TODO add other DBUser fields.
    }
    Country userCountry = Country.toCountry(this.user.getCountry());
    List<Map<String, String>> countries = new ArrayList<>();
    for (Country c : Country.values()) {
      Map<String, String> country = new HashMap<>();
      if (userCountry != null && userCountry.getName().equals(c.getName())) {
        country.put("selected", "selected");
      }
      country.put("value", c.getIsoCode());
      country.put("text", c.getName());
      countries.add(country);
    }
    contentMap.put("countries", countries);
  }

  /**
   * Transforms the list of organizations to a mustache model.
   */
  public void buildOrganizationsList(List<String> organizations) {
    if (organizations != null && !organizations.isEmpty()) {
      List<Entry<String, String>> pairs = new ArrayList<>();
      for (int i = 0; i < organizations.size(); i++) {
        pairs.add(new AbstractMap.SimpleEntry<String, String>(i + "", organizations.get(i)));
      }
      if (this.contentMap == null) {
        this.contentMap = new HashMap<>();
      }
      contentMap.put("selection_list", MetisMappingUtil.buildSimplePairs(pairs, "value", "title"));
    }
  }

  public Boolean getIsDuplicateUser() {
    return isDuplicateUser;
  }

  public void setIsDuplicateUser(Boolean isDuplicateUser) {
    this.isDuplicateUser = isDuplicateUser;
  }

  public Boolean getIsAuthError() {
    return isAuthError;
  }

  public void setIsAuthError(Boolean isAuthError) {
    this.isAuthError = isAuthError;
  }

  public PageView getPageView() {
    return pageView;
  }

  public void setPageView(PageView pageView) {
    this.pageView = pageView;
  }

  public UserProfile getUser() {
    return user;
  }

  public void setUser(UserProfile user) {
    this.user = user;
  }

  public List<UserRequest> getRequest() {
    return request;
  }

  public void setRequest(List<UserRequest> request) {
    this.request = request;
  }
}
