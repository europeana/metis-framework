package eu.europeana.metis.page;

import eu.europeana.metis.config.MetisuiConfig;
import eu.europeana.metis.core.common.Country;
import eu.europeana.metis.templates.UserRole;
import eu.europeana.metis.templates.ViewMode;
import eu.europeana.metis.templates.page.landingpage.LandingPageContent;
import eu.europeana.metis.templates.page.landingpage.profile.Active;
import eu.europeana.metis.templates.page.landingpage.profile.Approved;
import eu.europeana.metis.templates.page.landingpage.profile.Countries;
import eu.europeana.metis.templates.page.landingpage.profile.CountryItem;
import eu.europeana.metis.templates.page.landingpage.profile.Created;
import eu.europeana.metis.templates.page.landingpage.profile.Notes;
import eu.europeana.metis.templates.page.landingpage.profile.OrganizationModel;
import eu.europeana.metis.templates.page.landingpage.profile.RoleModel;
import eu.europeana.metis.templates.page.landingpage.profile.RoleType;
import eu.europeana.metis.templates.page.landingpage.profile.SelectedOrganizations;
import eu.europeana.metis.templates.page.landingpage.profile.Updated;
import eu.europeana.metis.templates.page.landingpage.profile.UserEmail;
import eu.europeana.metis.templates.page.landingpage.profile.UserFields;
import eu.europeana.metis.templates.page.landingpage.profile.UserFirstName;
import eu.europeana.metis.templates.page.landingpage.profile.UserId;
import eu.europeana.metis.templates.page.landingpage.profile.UserLastName;
import eu.europeana.metis.templates.page.landingpage.profile.UserProfileModel;
import eu.europeana.metis.templates.page.landingpage.profile.UserSkype;
import eu.europeana.metis.ui.mongo.domain.Role;
import eu.europeana.metis.ui.mongo.domain.UserDTO;
import eu.europeana.metis.ui.mongo.domain.UserOrganizationRole;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ProfileLandingPage extends MetisLandingPage {

   public ProfileLandingPage(UserDTO userDTO, MetisuiConfig config) {
    super(userDTO, config);
  }

  @Override
  public void addPageContent() {
    buildProfilePageContent();
  }

  private void buildProfilePageContent() {
    if (!this.userDTO.notNullUser()) {
      return;
    }
    LandingPageContent landingPageContent = new LandingPageContent();
    landingPageContent.setIsProfile(true);
    metisLandingPageModel.setLandingPageContent(landingPageContent);

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");

    UserProfileModel userProfileModel = createUserProfileModel(simpleDateFormat);

    metisLandingPageModel.setUserProfileModel(userProfileModel);
    metisLandingPageModel.setViewMode(new ViewMode("preview"));
    metisLandingPageModel.setUserRole(new UserRole("metisUser"));
  }

  private UserProfileModel createUserProfileModel(SimpleDateFormat simpleDateFormat) {
    UserFields userFields = new UserFields();
    userFields.setUserEmail(new UserEmail("Email *", userDTO.getLdapUser().getEmail()));
    userFields.setUserId(new UserId("User ID", userDTO.getUser().getId().toString()));
    userFields
        .setUserFirstName(new UserFirstName("First Name *", userDTO.getLdapUser().getFirstName()));
    userFields
        .setUserLastName(new UserLastName("Last Name *", userDTO.getLdapUser().getLastName()));
    userFields.setUserSkype(new UserSkype("Skype", userDTO.getUser().getSkypeId()));
    userFields.setCreated(
        new Created("Created", "", userDTO.getUser().getCreated() == null ? null : simpleDateFormat.format(userDTO.getUser().getCreated())));
    userFields.setUpdated(
        new Updated("Updated", "", userDTO.getUser().getCreated() == null ? null : simpleDateFormat.format(userDTO.getUser().getModified())));
    userFields
        .setActive(new Active("Active", "", Boolean.toString(userDTO.getLdapUser().isActive())));
    userFields.setApproved(
        new Approved("Approved", "", Boolean.toString(userDTO.getLdapUser().isApproved())));
    userFields.setNotes(new Notes("Notes", "", userDTO.getUser().getNotes()));
    userFields.setCountries(buildCountriesList());
    userFields.setSelectedOrganizations(buildOrganizationsList());

    UserProfileModel userProfileModel = new UserProfileModel();
    userProfileModel.setUserFields(userFields);
    userProfileModel.setRoleTypes(buildRoleTypeList());
    return userProfileModel;
  }

  private SelectedOrganizations buildOrganizationsList() {
    List<OrganizationModel> organizationModels = new ArrayList<>();

    List<UserOrganizationRole> userOrganizationRoles = userDTO.getUser().getUserOrganizationRoles();
    if(userOrganizationRoles != null) {
      for (UserOrganizationRole userOrganizationRole : userOrganizationRoles) {
        OrganizationModel organizationModel = new OrganizationModel(
            userOrganizationRole.getOrganizationName(),
            Integer.parseInt(userOrganizationRole.getOrganizationId()),
            userOrganizationRole.getRole().name(), 1);
        organizationModels.add(organizationModel);
      }
    }
    return new SelectedOrganizations("Selected Organizations", organizationModels);
  }

  private List<RoleType> buildRoleTypeList()
  {
    List<RoleType> roleTypes = new ArrayList<>();
    List<RoleModel> roleModels = new ArrayList<>();
    roleModels.add(new RoleModel(Role.EUROPEANA_VIEWER.getName()));
    roleModels.add(new RoleModel(Role.EUROPEANA_DATA_OFFICER.getName()));
    roleModels.add(new RoleModel(Role.EUROPEANA_ADMIN.getName()));
    RoleType roleType = new RoleType("1", roleModels);
    roleTypes.add(roleType);

    roleModels = new ArrayList<>();
    roleModels.add(new RoleModel(Role.PROVIDER_VIEWER.getName()));
    roleModels.add(new RoleModel(Role.PROVIDER_DATA_OFFICER.getName()));
    roleModels.add(new RoleModel(Role.PROVIDER_ADMIN.getName()));
    roleType = new RoleType("2", roleModels);
    roleTypes.add(roleType);

    roleModels = new ArrayList<>();
    roleModels.add(new RoleModel(Role.DATA_PROVIDER_CONTACT.getName()));
    roleType = new RoleType("3", roleModels);
    roleTypes.add(roleType);

    return roleTypes;
  }

  private Countries buildCountriesList() {
    Country userCountry = userDTO.getUser().getCountry();

    ArrayList<CountryItem> countryItems = new ArrayList<>();
    for (Country country : Country.values()) {
      CountryItem countryItem;
      if (userCountry != null && userCountry.equals(country)) {
        countryItem = new CountryItem(country.getName(), country.getIsoCode(), true);
      } else {
        countryItem = new CountryItem(country.getName(), country.getIsoCode(), false);
      }
      countryItems.add(countryItem);
    }

    Countries countries = new Countries();
    countries.setLabel("Country");
    countries.setStartValue("Select country");
    countries.setItems(countryItems);

    return countries;
  }
}
