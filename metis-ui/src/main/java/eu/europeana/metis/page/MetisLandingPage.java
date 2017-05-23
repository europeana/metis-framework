package eu.europeana.metis.page;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.flapdoodle.embed.process.collections.Collections;
import eu.europeana.metis.common.MetisPage;
import eu.europeana.metis.mapping.atoms.UserRequest;
import eu.europeana.metis.templates.page.landingpage.Excerpt;
import eu.europeana.metis.templates.page.landingpage.Headline;
import eu.europeana.metis.mapping.molecules.pandora.register.EmailField;
import eu.europeana.metis.mapping.molecules.pandora.register.FullNameField;
import eu.europeana.metis.mapping.molecules.pandora.register.PasswordField;
import eu.europeana.metis.mapping.molecules.pandora.register.RegisterForm;
import eu.europeana.metis.mapping.organisms.pandora.UserProfile;
import eu.europeana.metis.templates.page.landingpage.Banner;
import eu.europeana.metis.templates.Content;
import eu.europeana.metis.templates.page.landingpage.ForgotLoginCredentials;
import eu.europeana.metis.templates.page.landingpage.HeroConfig;
import eu.europeana.metis.templates.page.landingpage.LandingPageContent;
import eu.europeana.metis.templates.page.landingpage.LoginForm;
import eu.europeana.metis.templates.page.landingpage.MetisLandingPageModel;
import eu.europeana.metis.templates.PageConfig;
import eu.europeana.metis.templates.Version;
import java.util.List;
import java.util.Map;

/**
 * This web-page represents a Metis Landing page and all user account pages like: Login page,
 * User Profile page, Register User page.
 *
 * @author alena
 */
public class MetisLandingPage extends MetisPage {

  private PageView pageView = PageView.HOME;
  private List<UserRequest> request;
  private Boolean isDuplicateUser = false;
  private Boolean isAuthError = false;
  private static final String ERROR_DUPLICATE_USER = "ERROR: The user with this email address already exists.";
  private static final String ERROR_AUTH = "ERROR: Your email or password is incorrect. Try again please.";
//  private Map<String, Object> contentMap;
  MetisLandingPageModel metisLandingPageModel;

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
  public Map<String, Object> buildModel() {
    metisLandingPageModel = new MetisLandingPageModel();
    metisLandingPageModel.setIsJava(true);
    metisLandingPageModel.setCssFiles(resolveCssFilesClass());
    metisLandingPageModel.setJsFiles(resolveJsFilesClass());
    metisLandingPageModel.setJsVars(resolveJsVars());
    metisLandingPageModel.setBreadcrumbs(resolveBreadcrumbs());
    metisLandingPageModel.setPageTitle("Europeana Metis");
    metisLandingPageModel.setImageRoot("https://europeanastyleguidetest.a.cdnify.io");
    PageConfig pageConfig = new PageConfig();
    pageConfig.setNewsletter(true);
    metisLandingPageModel.setPageConfig(pageConfig);
    Version version = new Version();
    version.setIsBeta(true);
    version.setIsAlpha(false);
    metisLandingPageModel.setVersion(version);
    Headline headline1 = new Headline();
    Headline headline2 = new Headline();
    Headline headline3 = new Headline();
    headline1.setShort("false");
    headline2.setMedium("false");
    headline3.setLong("false");
    metisLandingPageModel.setHeadline(Collections.newArrayList(headline1, headline2, headline3));
    Excerpt excerpt1 = new Excerpt();
    Excerpt excerpt2 = new Excerpt();
    Excerpt excerpt3 = new Excerpt();
    Excerpt excerpt4 = new Excerpt();
    excerpt1.setVshort("false");
    excerpt2.setShort("false");
    excerpt3.setMedium("false");
    excerpt4.setLong("false");
    metisLandingPageModel.setExcerpt(Collections.newArrayList(excerpt1, excerpt2, excerpt3, excerpt4));
    metisLandingPageModel.setMetisHeader(buildHeader(pageView));
    metisLandingPageModel.setI18n(buildI18n());

    addPageContent();

    metisLandingPageModel.setMetisFooter(buildFooter());

    ObjectMapper m = new ObjectMapper();
    Map<String,Object> modelMap = m.convertValue(metisLandingPageModel, Map.class);
    return modelMap;
  }

  @Override
  public void addPageContent() {
//    if (this.contentMap == null) {
//      this.contentMap = new HashMap<>();
//    }
    switch (pageView) {
      case HOME:
//        contentMap.put("is_home", true);
        buildHomePageContent();
        break;
      case LOGIN:
//        contentMap.put("is_login", true);
        buildLoginPageContent();
        break;
      case REGISTER:
//        contentMap.put("is_register", true);
        buildRegisterPageContent();
        break;
//      case PROFILE:
//        contentMap.put("is_profile", true);
//        buildProfilePageContent();
//        break;
//      case REQUESTS:
//        contentMap.put("is_requests", true);
//        buildRequestsPageContent();
//        break;
//      case USER_APPROVE:
//        contentMap.put("is_request_approve", true);
//        buildApproveRequestsPageContent();
//        break;
      default:
        break;
    }

//    model.put("landing_page_content", contentMap);
  }

  /**
   * In the current Metis design there is no need for the bread-crumbs.
   */
//  @Override
//  public List<Entry<String, String>> resolveBreadcrumbs() {
//    List<Entry<String, String>> breadcrumbs = new ArrayList<>();
//    breadcrumbs.add(new SimpleEntry<String, String>("Home", "/"));
//    return breadcrumbs;
//  }

//  @Override
//  public List<NavigationTopMenu> buildUtilityNavigation() {
//    List<NavigationTopMenu> utilityNavigationMenu = new ArrayList<>();
//    switch (this.pageView) {
//      case REGISTER:
//        utilityNavigationMenu.add(new NavigationTopMenu("Login", "/login", true));
//        utilityNavigationMenu.add(new NavigationTopMenu("Home", "/", true));
//        break;
//      case LOGIN:
//        utilityNavigationMenu.add(new NavigationTopMenu("Register", "/register", false));
//        utilityNavigationMenu.add(new NavigationTopMenu("Home", "/", true));
//        break;
//      case PROFILE:
//        utilityNavigationMenu.add(new NavigationTopMenu("Logout", "/logout", false));
//        utilityNavigationMenu.add(new NavigationTopMenu("Home", "/", true));
//        break;
//      case REQUESTS:
//        utilityNavigationMenu.add(new NavigationTopMenu("Logout", "/logout", false));
//        utilityNavigationMenu.add(new NavigationTopMenu("Home", "/", true));
//        break;
//      default:
//        if (user != null && user.getEmail() != null) {
//          utilityNavigationMenu.add(new NavigationTopMenu("Profile", "/profile", true));
//          utilityNavigationMenu.add(new NavigationTopMenu("Logout", "/logout", true));
//        } else {
//          utilityNavigationMenu.add(new NavigationTopMenu("Login", "/login", true));
//          utilityNavigationMenu.add(new NavigationTopMenu("Register", "/register", false));
//        }
//        break;
//    }
//    return utilityNavigationMenu;
//  }


  /**
   * The content for the User Login page.
   */
  private void buildHomePageContent() {
    Banner banner = new Banner();
    banner.setCtaText("Register to metis here");
    banner.setCtaUrl("#");
    banner.setInfoLink("Learn more about Metis");
    banner.setInfoUrl("#");
    banner.setText("Ever wondered how to automatically digest huge amounts of data with the push of a button?");
    banner.setTitle("What can you do with Metis?");
    HeroConfig heroConfig = new HeroConfig();
    heroConfig.setAttributionText("Cyclopides metis L., Cyclopides qua... Museum Fur Naturkunde Berlin");
    heroConfig.setAttributionUrl("http://www.europeana.eu/portal/fr/record/11622/_MFN_DRAWERS_MFN_GERMANY_http___coll_mfn_berlin_de_u_MFNB_Lep_Hesperiidae_D146.html");
    heroConfig.setBrandColour("brand-colour-site");
    heroConfig.setBrandOpacity("brand-opacity100");
    heroConfig.setBrandPosition("brand-bottomleft");
    heroConfig.setHeroImage("https://europeana-styleguide-test.s3.amazonaws.com/images/metis/hero_metis_1600x650_jade.png");
    heroConfig.setLicenseCC0("true");
    Content content = new Content();
    content.setHeroConfig(heroConfig);
    content.setBanner(banner);
    LandingPageContent landingPageContent = new LandingPageContent();
    landingPageContent.setIsHome(true);
    landingPageContent.setContent(content);

    metisLandingPageModel.setLandingPageContent(landingPageContent);

//    Map<String, String> hero_config = new HashMap<>();
//    hero_config.put("hero_image",
//        "https://europeana-styleguide-test.s3.amazonaws.com/images/metis/hero_metis_1600x650_jade.png");
//    hero_config.put("brand_colour", "brand-colour-site");
//    hero_config.put("brand_position", "brand-bottomleft");
//    hero_config.put("brand_opacity", "brand-opacity100");
//    hero_config.put("attribution_text",
//        "Cyclopides metis L., Cyclopides qua... Museum Fur Naturkunde Berlin");
//    hero_config.put("attribution_url",
//        "http://www.europeana.eu/portal/fr/record/11622/_MFN_DRAWERS_MFN_GERMANY_http___coll_mfn_berlin_de_u_MFNB_Lep_Hesperiidae_D146.html");
//    hero_config.put("license_CC0", "true");
////		hero_config.put("license_public", "false");
//
//    Map<String, String> banner = new HashMap<>();
//    banner.put("title", "What can you do with Metis?");
//    banner.put("text",
//        "Ever wondered how to automatically digest huge amounts of data with the push of a button?");
//    banner.put("info_link", "Learn more about Metis");
//    banner.put("info_url", "#");
//    banner.put("cta_url", "#");
//    banner.put("cta_text", "Register to metis here");
//
//    Map<String, Map<String, String>> content = new HashMap<>();
//    content.put("hero_config", hero_config);
//    content.put("banner", banner);
//    contentMap.put("content", content);
  }

  /**
   * The content for the User Login page.
   */
  private void buildLoginPageContent() {
    LandingPageContent landingPageContent = new LandingPageContent();
    landingPageContent.setIsLogin(true);
    metisLandingPageModel.setLandingPageContent(landingPageContent);

    EmailField emailField = new EmailField();
    emailField.setLabel("Email");
    emailField.setPlaceholder("your@email");

    PasswordField passwordField = new PasswordField();
    passwordField.setLabel("Password");
    passwordField.setPlaceholder("Type your password");

    ForgotLoginCredentials forgotLoginCredentials = new ForgotLoginCredentials();
    forgotLoginCredentials.setText("Forgot your account?");
    forgotLoginCredentials.setUrl("#");

    LoginForm loginForm = new LoginForm();
    loginForm.setEmailField(emailField);
    loginForm.setPasswordField(passwordField);
    loginForm.setForgotLoginCredentials(forgotLoginCredentials);
    loginForm.setFormTitle("Sign in to Metis");
    loginForm.setSubmitBtn("Sign In");

    if (isAuthError) {
      // TODO: 23-5-17 Fix on error credentials
    }

    metisLandingPageModel.setLoginForm(loginForm);


//    Map<String, Object> login_form = new HashMap<>();
//
//    Map<String, String> email_field = new HashMap<>();
//    Map<String, String> password_field = new HashMap<>();
//    Map<String, String> forgot_login_credentials = new HashMap<>();
//    Map<String, Object> authentication_error_message = new HashMap<>();
//
//    email_field.put("label", "Email");
//    email_field.put("placeholder", "your@email");
//    password_field.put("label", "Password");
//    password_field.put("placeholder", "Type your password");
//
//    forgot_login_credentials.put("text", "Forgot your account?");
//    forgot_login_credentials.put("url", "#");
//
//    login_form.put("form_title", "Sign in to Metis");
//    login_form.put("email_field", email_field);
//    login_form.put("password_field", password_field);
//
//    if (isAuthError) {
//      authentication_error_message.put("authentication_error_message", "Wrong credentials");
//      login_form.put("login_err_authenticate", authentication_error_message);
//    }
//    login_form.put("forgot_login_credentials", forgot_login_credentials);
//    login_form.put("submit_btn", "Sign In");
//
//    contentMap.put("login_form", login_form);
  }

  /**
   * The content for the Register User page.
   */
  private void buildRegisterPageContent() {
    LandingPageContent landingPageContent = new LandingPageContent();
    landingPageContent.setIsRegister(true);
    metisLandingPageModel.setLandingPageContent(landingPageContent);

    FullNameField fullNameField = new FullNameField();
    fullNameField.setLabel("Name *");
    fullNameField.setFirstNamePlaceholder("First");
    fullNameField.setLastNamePlaceholder("Last");

    EmailField emailField = new EmailField();
    emailField.setLabel("Email *");
    emailField.setPlaceholder("your@email");

    PasswordField passwordField = new PasswordField();
    passwordField.setLabel("New Password *");
    passwordField.setPlaceholder("Create your password");

    RegisterForm registerForm = new RegisterForm();
    registerForm.setFormTitle("Register to Metis");
    registerForm.setFullNameField(fullNameField);
    registerForm.setEmailField(emailField);
    registerForm.setPasswordField(passwordField);

    if (isDuplicateUser) {
      registerForm.setRegisterErrDuplicateUser("User already exists");
    }

    registerForm.setSubmitBtnText("Submit");
    registerForm.setResetBtnText("Reset");
    registerForm.setFormRequirementsWarning("* needed for registration");

    metisLandingPageModel.setRegisterForm(registerForm);

//    ObjectMapper m = new ObjectMapper();
//    Map<String,Object> props = m.convertValue(registerForm, Map.class);
//    contentMap.put("register_form", props);
  }

  /**
   * The content for the User Profile page.
   */
  private void buildProfilePageContent() {
//    if (this.user == null) {
//      return;
//    }
//    String email = this.user.getEmail();
//    contentMap.put("email", email);
//
//    String fullName = user.getFirstName();
//    contentMap.put("fullName", fullName);
//
//    String lastName = user.getLastName();
//    contentMap.put("lastName", lastName);
//
//    if (user instanceof UserProfile) {
//      contentMap.put("skype", ((UserProfile) user).getSkype());
//      //TODO add other DBUser fields.
//    }
//    Country userCountry = Country.toCountry(this.user.getCountry());
//    List<Map<String, String>> countries = new ArrayList<>();
//    for (Country c : Country.values()) {
//      Map<String, String> country = new HashMap<>();
//      if (userCountry != null && userCountry.getName().equals(c.getName())) {
//        country.put("selected", "selected");
//      }
//      country.put("value", c.getIsoCode());
//      country.put("text", c.getName());
//      countries.add(country);
//    }
//    contentMap.put("countries", countries);
  }


  private void buildRequestsPageContent() {
//    if (this.request == null) {
//      return;
//    } else {
//      contentMap.put("request", this.request);
//    }
  }

  private void buildApproveRequestsPageContent() {
//    if (this.user == null) {
//      return;
//    }
//    String email = this.user.getEmail();
//    contentMap.put("email", email);
//
//    String fullName = user.getFirstName();
//    contentMap.put("fullName", fullName);
//
//    String lastName = user.getLastName();
//    contentMap.put("lastName", lastName);
//
//    if (user instanceof UserProfile) {
//      contentMap.put("skype", ((UserProfile) user).getSkype());
//      //TODO add other DBUser fields.
//    }
//    Country userCountry = Country.toCountry(this.user.getCountry());
//    List<Map<String, String>> countries = new ArrayList<>();
//    for (Country c : Country.values()) {
//      Map<String, String> country = new HashMap<>();
//      if (userCountry != null && userCountry.getName().equals(c.getName())) {
//        country.put("selected", "selected");
//      }
//      country.put("value", c.getIsoCode());
//      country.put("text", c.getName());
//      countries.add(country);
//    }
//    contentMap.put("countries", countries);
  }

  /**
   * Transforms the list of organizations to a mustache model.
   */
  public void buildOrganizationsList(List<String> organizations) {
//    if (organizations != null && !organizations.isEmpty()) {
//      List<Entry<String, String>> pairs = new ArrayList<>();
//      for (int i = 0; i < organizations.size(); i++) {
//        pairs.add(new AbstractMap.SimpleEntry<String, String>(i + "", organizations.get(i)));
//      }
//      if (this.contentMap == null) {
//        this.contentMap = new HashMap<>();
//      }
//      contentMap.put("selection_list", MetisMappingUtil.buildSimplePairs(pairs, "value", "title"));
//    }
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
