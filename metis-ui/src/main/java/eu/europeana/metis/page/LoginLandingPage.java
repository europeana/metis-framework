package eu.europeana.metis.page;

import eu.europeana.metis.config.MetisuiConfig;
import eu.europeana.metis.templates.Submenu;
import eu.europeana.metis.templates.page.landingpage.ForgotLoginCredentials;
import eu.europeana.metis.templates.page.landingpage.LandingPageContent;
import eu.europeana.metis.templates.page.landingpage.LoginErrAuthenticate;
import eu.europeana.metis.templates.page.landingpage.LoginForm;
import eu.europeana.metis.templates.page.landingpage.register.EmailField;
import eu.europeana.metis.templates.page.landingpage.register.PasswordField;/**
 * Created by erikkonijnenburg on 27/06/2017.
 */
public class LoginLandingPage extends MetisLandingPage {

  private Boolean isAuthError;

  public LoginLandingPage(MetisuiConfig config) {
    super(config);
  }

  @Override
  public void addPageContent() {
    buildLoginPageContent();
  }

  @Override
  public Submenu buildNavigationSubmenu() {
    return HeaderSubMenuBuilder.buildMenuForLoginPage();
  }

  public Boolean getIsAuthError() {
    return isAuthError;
  }

  public void setIsAuthError(Boolean isAuthError) {
    this.isAuthError = isAuthError;
  }

  /**
   * The content for the User Login page.
   */
  private void buildLoginPageContent(){
    metisLandingPageModel.setLoginForm(createForm());

    LandingPageContent landingPageContent = new LandingPageContent();
    landingPageContent.setIsLogin(true);
    metisLandingPageModel.setLandingPageContent(landingPageContent);
  }

  private LoginForm createForm() {
    EmailField emailField = createEmailField();
    PasswordField passwordField = createPasswordField();
    ForgotLoginCredentials forgotLoginCredentials = CreateForgotLoginCredentials();

    LoginForm loginForm = new LoginForm();
    loginForm.setEmailField(emailField);
    loginForm.setPasswordField(passwordField);
    loginForm.setForgotLoginCredentials(forgotLoginCredentials);
    loginForm.setFormTitle("Sign in to Metis");
    loginForm.setSubmitBtn("Sign In");

    if (getIsAuthError()) {
      LoginErrAuthenticate loginErrAuthenticate = new LoginErrAuthenticate();
      loginErrAuthenticate.setAuthenticationErrorMessage("Wrong credentials");
      loginForm.setLoginErrAuthenticate(loginErrAuthenticate);
    }
    return loginForm;
  }

  private ForgotLoginCredentials CreateForgotLoginCredentials() {
    ForgotLoginCredentials forgotLoginCredentials = new ForgotLoginCredentials();
    forgotLoginCredentials.setText("Forgot your account?");
    forgotLoginCredentials.setUrl("#");
    return forgotLoginCredentials;
  }

  private PasswordField createPasswordField() {
    PasswordField passwordField = new PasswordField();
    passwordField.setLabel("Password");
    passwordField.setPlaceholder("Type your password");
    return passwordField;
  }

  private EmailField createEmailField() {
    EmailField emailField = new EmailField();
    emailField.setLabel("Email");
    emailField.setPlaceholder("your@email");
    return emailField;
  }


}
