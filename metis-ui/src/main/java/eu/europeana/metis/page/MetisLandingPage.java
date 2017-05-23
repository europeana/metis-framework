package eu.europeana.metis.page;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.flapdoodle.embed.process.collections.Collections;
import eu.europeana.metis.common.MetisPage;
import eu.europeana.metis.core.common.Country;
import eu.europeana.metis.mapping.molecules.pandora.register.EmailField;
import eu.europeana.metis.mapping.molecules.pandora.register.FullNameField;
import eu.europeana.metis.mapping.molecules.pandora.register.PasswordField;
import eu.europeana.metis.mapping.molecules.pandora.register.RegisterForm;
import eu.europeana.metis.mapping.organisms.pandora.UserProfile;
import eu.europeana.metis.templates.Content;
import eu.europeana.metis.templates.PageConfig;
import eu.europeana.metis.templates.Version;
import eu.europeana.metis.templates.page.landingpage.Banner;
import eu.europeana.metis.templates.page.landingpage.CountryModel;
import eu.europeana.metis.templates.page.landingpage.Excerpt;
import eu.europeana.metis.templates.page.landingpage.ForgotLoginCredentials;
import eu.europeana.metis.templates.page.landingpage.Headline;
import eu.europeana.metis.templates.page.landingpage.HeroConfig;
import eu.europeana.metis.templates.page.landingpage.LandingPageContent;
import eu.europeana.metis.templates.page.landingpage.LoginForm;
import eu.europeana.metis.templates.page.landingpage.MetisLandingPageModel;
import eu.europeana.metis.templates.page.landingpage.Request;
import eu.europeana.metis.templates.page.landingpage.SelectionList;
import eu.europeana.metis.ui.mongo.domain.RoleRequest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
  private List<RoleRequest> roleRequests;
  private Boolean isDuplicateUser = false;
  private Boolean isAuthError = false;
  private static final String ERROR_DUPLICATE_USER = "ERROR: The user with this email address already exists.";
  private static final String ERROR_AUTH = "ERROR: Your email or password is incorrect. Try again please.";
  MetisLandingPageModel metisLandingPageModel;

  public MetisLandingPage(PageView pageView) {
    super();
    this.pageView = pageView;
  }

  public MetisLandingPage(PageView pageView, UserProfile user) {
    this(pageView);
    this.user = user;
  }

  public MetisLandingPage(PageView pageView, List<RoleRequest> roleRequests) {
    this(pageView);
    this.pageView = pageView;
    this.roleRequests = roleRequests;
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
    metisLandingPageModel.setCssFiles(resolveCssFiles());
    metisLandingPageModel.setJsFiles(resolveJsFiles());
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
    switch (pageView) {
      case HOME:
        buildHomePageContent();
        break;
      case LOGIN:
        buildLoginPageContent();
        break;
      case REGISTER:
        buildRegisterPageContent();
        break;
      case PROFILE:
        buildProfilePageContent();
        break;
      case REQUESTS:
        buildRequestsPageContent();
        break;
      default:
        break;
    }
  }

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
  }

  /**
   * The content for the User Profile page.
   */
  private void buildProfilePageContent() {
    if (this.user == null) {
      return;
    }
    LandingPageContent landingPageContent = new LandingPageContent();
    landingPageContent.setIsProfile(true);
    metisLandingPageModel.setLandingPageContent(landingPageContent);

    metisLandingPageModel.setFullName(user.getFirstName());
    metisLandingPageModel.setLastName(user.getLastName());
    metisLandingPageModel.setEmail(user.getEmail());

    if (user instanceof UserProfile) {
      metisLandingPageModel.setSkype(user.getSkype());
      //TODO add other DBUser fields.
    }

    Country userCountry = Country.toCountry(user.getCountry());
    List<CountryModel> countryModels = new ArrayList<>();
    for (Country c : Country.values()) {
      CountryModel countryModel = new CountryModel();
      if (userCountry != null && userCountry.getName().equals(c.getName())) {
        countryModel.setSelected("selected");
      }
      countryModel.setText(c.getName());
      countryModel.setValue(c.getIsoCode());
      countryModels.add(countryModel);
    }
    metisLandingPageModel.setCountries(countryModels);
  }


  private void buildRequestsPageContent() {
    if (roleRequests == null) {
      return;
    } else {
      LandingPageContent landingPageContent = new LandingPageContent();
      landingPageContent.setIsRequests(true);
      metisLandingPageModel.setLandingPageContent(landingPageContent);

      SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy hh:mm");
      List<Request> requests = new ArrayList<>();
      for (RoleRequest roleRequest : roleRequests)
      {
        Request request = new Request();
        request.setId(roleRequest.getId().toString());
        request.setEmail(roleRequest.getUserId());
        request.setOrganization(roleRequest.getOrganizationId());
        request.setUrl("/profile?userId=" + request.getId());
        request.setDate(format.format(roleRequest.getRequestDate()));
        if (roleRequest.isDeleteRequest()){
          request.setType("Delete");
          request.setRequestType("user-request-type-delete");
        }
        else {
          request.setType("Add");
          request.setRequestType("user-request-type-add");
        }
        if (roleRequest.getRequestStatus() == null || roleRequest.getRequestStatus().equalsIgnoreCase("Pending"))
        {
          request.setStatus("Pending");
          request.setStatusType("user-request-pending");
        }
        else if (roleRequest.getRequestStatus().equalsIgnoreCase("Accepted"))
        {
          request.setStatus("Accepted");
          request.setStatusType("user-request-accepted");
        }
        else if (roleRequest.getRequestStatus().equalsIgnoreCase("Rejected"))
        {
          request.setStatus("Rejected");
          request.setStatusType("user-request-rejected");
        }
        requests.add(request);
      }
      metisLandingPageModel.setRequest(requests);
    }
  }

  /**
   * Transforms the list of organizations to a mustache model.
   */
  public void buildOrganizationsList(List<String> organizations) {
    List<SelectionList> selectionLists = new ArrayList<>();

    if (organizations != null && !organizations.isEmpty()) {
      for (int i = 0; i < organizations.size(); i++) {
        SelectionList selectionList = new SelectionList();
        selectionList.setTitle(organizations.get(i));
        selectionList.setValue(i);
        selectionLists.add(selectionList);
      }
      metisLandingPageModel.setSelectionList(selectionLists);
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
}
