package eu.europeana.metis.page;

import eu.europeana.metis.templates.Submenu;
import eu.europeana.metis.templates.page.landingpage.LandingPageContent;
import eu.europeana.metis.templates.page.landingpage.register.EmailField;
import eu.europeana.metis.templates.page.landingpage.register.FullNameField;
import eu.europeana.metis.templates.page.landingpage.register.PasswordField;
import eu.europeana.metis.templates.page.landingpage.register.RegisterForm;
import eu.europeana.metis.ui.mongo.domain.UserDTO;
import org.springframework.http.ResponseEntity.HeadersBuilder;

public class RegisterLandingPage extends MetisLandingPage {

  private Boolean isDuplicateUser;

  public RegisterLandingPage() {
    super();
  }

  @Override
  public Submenu buildNavigationSubmenu() {
    return HeaderSubMenuBuilder.buildMenuRegister();
  }

  @Override
  public void addPageContent() {
    buildRegisterPageContent();
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

    if (getIsDuplicateUser()) {
      registerForm.setRegisterErrDuplicateUser("User already exists");
    }

    registerForm.setSubmitBtnText("Submit");
    registerForm.setResetBtnText("Reset");
    registerForm.setFormRequirementsWarning("* needed for registration");

    metisLandingPageModel.setRegisterForm(registerForm);
  }

  public Boolean getIsDuplicateUser() {
    return isDuplicateUser;
  }

  public void setIsDuplicateUser(Boolean isDuplicateUser) {
    this.isDuplicateUser = isDuplicateUser;
  }
}

