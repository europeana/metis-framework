package eu.europeana.metis.page;

import eu.europeana.metis.config.MetisuiConfig;
import eu.europeana.metis.config.NavigationPaths;
import eu.europeana.metis.templates.Submenu;
import eu.europeana.metis.ui.mongo.domain.RoleRequest;
import eu.europeana.metis.ui.mongo.domain.UserDTO;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by erikkonijnenburg on 30/06/2017.
 */
@Service
public class MetisPageFactory {

  private final HeaderSubMenuBuilder headerSubMenuBuilder;
  private final MetisuiConfig metisuiConfig;
  private final NavigationPaths navigationPaths;

  @Autowired
  public MetisPageFactory(MetisuiConfig config, HeaderSubMenuBuilder builder, NavigationPaths paths) {
    this.metisuiConfig = config;
    this.headerSubMenuBuilder = builder;
    this.navigationPaths=paths;
  }


  public ProfileLandingPage createProfileLandingPage(UserDTO userDTO) {
    ProfileLandingPage page = new ProfileLandingPage(metisuiConfig);
    page.setUserDTO(userDTO);
    page.setSubmenu(buildNavigationSubmenu(userDTO));
    page.setProfileUrl(navigationPaths.getProfileUrl());
    return page;
  }

  public HomeLandingPage createHomeLandingPage(UserDTO userDTO) {
    HomeLandingPage page = new HomeLandingPage(metisuiConfig);
    page.setSubmenu(buildNavigationSubmenu(userDTO));
    return page;
  }

  public MappingToEdmPage createMappingToEdmPage(UserDTO userDTO) {
    MappingToEdmPage page = new MappingToEdmPage(metisuiConfig);
    page.setUserDTO(userDTO);
    page.setSubmenu(buildNavigationSubmenu(userDTO));
    return page;
  }

  public LoginLandingPage createLoginLandingPage() {
    LoginLandingPage page = new LoginLandingPage(metisuiConfig);
    page.setSubmenu(headerSubMenuBuilder.buildMenuForLoginPage());
    return page;
  }

  public RegisterLandingPage createRegisterLandingPage() {
    RegisterLandingPage page = new RegisterLandingPage(metisuiConfig);
    page.setSubmenu(headerSubMenuBuilder.buildMenuRegister());
    return page;
  }

  public MetisDashboardPage createMetisDashBoardPage(UserDTO userDTO) {
    MetisDashboardPage page = new MetisDashboardPage(metisuiConfig);
    page.setUserDTO(userDTO);
    page.setSubmenu(buildNavigationSubmenu(userDTO));
    return page;
  }

  public RequestsLandingPage createRequestsLandingPage(List<RoleRequest> roleRequests) {
    RequestsLandingPage page = new RequestsLandingPage(metisuiConfig);
    page.setRoleRequests(roleRequests);
    page.setSubmenu(headerSubMenuBuilder.buildMenuRegister());
    return page;
  }

  private boolean isUserAuthorized(UserDTO userDTO) {
    return
        userDTO.notNullUser() && userDTO.getLdapUser().getEmail() != null && userDTO.getLdapUser().getEmail() != null;
  }

  private Submenu buildNavigationSubmenu(UserDTO dto) {
    if (isUserAuthorized(dto)) {
      return headerSubMenuBuilder.buildMenuWhenAuthorized();
    } else {
      return headerSubMenuBuilder.buildMenuWhenNotAuthorized();
    }
  }
}
