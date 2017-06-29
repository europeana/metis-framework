package eu.europeana.metis.controller;

import eu.europeana.metis.common.UserProfileRequest;
import eu.europeana.metis.core.mail.notification.MetisMailType;
import eu.europeana.metis.core.organization.Organization;
import eu.europeana.metis.core.rest.client.DsOrgRestClient;
import eu.europeana.metis.core.rest.client.ServerException;
import eu.europeana.metis.core.search.common.OrganizationSearchBean;
import eu.europeana.metis.page.LoginLandingPage;
import eu.europeana.metis.page.MetisDashboardPage;
import eu.europeana.metis.page.RegisterLandingPage;
import eu.europeana.metis.page.RequestsLandingPage;
import eu.europeana.metis.ui.mongo.domain.RoleRequest;
import eu.europeana.metis.ui.mongo.domain.UserDTO;
import eu.europeana.metis.ui.mongo.service.UserService;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.ldap.userdetails.LdapUserDetailsImpl;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * Metis User related web pages controller.
 *
 * @author alena
 */
@Controller
public class MetisUserPageController {

  private static final Logger LOGGER = LoggerFactory.getLogger(MetisUserPageController.class);

  private final UserService userService;
  private final DsOrgRestClient dsOrgRestClient;
  private final JavaMailSender javaMailSender;
  private final SimpleMailMessage simpleMailMessage;

  @Autowired
  public MetisUserPageController(UserService userService, DsOrgRestClient dsOrgRestClient,
      JavaMailSender javaMailSender, SimpleMailMessage simpleMailMessage) {
    this.userService = userService;
    this.dsOrgRestClient = dsOrgRestClient;
    this.javaMailSender = javaMailSender;
    this.simpleMailMessage = simpleMailMessage;
  }

  /**
   * Resolves user login page.
   */
  @RequestMapping(value = "/login")
  public ModelAndView login(
      @RequestParam(value = "authentication_error", required = false) boolean authenticationError) {
    LoginLandingPage metisLandingPage = new LoginLandingPage();
    metisLandingPage.setIsAuthError(authenticationError);

    ModelAndView modelAndView = new ModelAndView("templates/Pandora/Metis-Homepage");
    modelAndView.addAllObjects(metisLandingPage.buildModel());
//    System.out.println(MetisMappingUtil.toJson(modelAndView.getModel()));
    return modelAndView;
  }

  @RequestMapping(value = "/register", method = RequestMethod.GET)
  public ModelAndView register() {
    RegisterLandingPage metisLandingPage = new RegisterLandingPage();

    ModelAndView modelAndView = new ModelAndView("templates/Pandora/Metis-Homepage");
    modelAndView.addAllObjects(metisLandingPage.buildModel());
    return modelAndView;
  }

  @RequestMapping(value = "/register", method = RequestMethod.POST)
  public ModelAndView registerUser(@ModelAttribute UserProfileRequest userProfileRequest,
      Model model) {
    RegisterLandingPage metisLandingPage = new RegisterLandingPage();
    model.addAttribute("user", userProfileRequest);
    UserDTO storedUserDto = userService.getUser(userProfileRequest.getEmail());

    if (storedUserDto.notNullUser()) {
      ModelAndView modelAndView = new ModelAndView("templates/Pandora/Metis-Homepage");
      metisLandingPage.setIsDuplicateUser(true);
      modelAndView.addAllObjects(metisLandingPage.buildModel());
      return modelAndView;
    }
    userService
        .createUser(userProfileRequest.getFirstName(), userProfileRequest.getLastName(),
            userProfileRequest.getEmail(),
            userProfileRequest.getPassword());
    LOGGER.info("*** User created: " + userProfileRequest.getFirstName() + " ***");

    return new ModelAndView("redirect:/profile");
  }

  @RequestMapping(value = "/dashboard")
  public ModelAndView dashboardPage() {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    String primaryKey =
        principal instanceof LdapUserDetailsImpl ? ((LdapUserDetailsImpl) principal).getUsername()
            : null;
    UserDTO userDTO = userService.getUser(primaryKey);
//    UserProfile userProfile = new UserProfile();
//    userProfile.init(userDTO);
    LOGGER.info("*** User profile opened: " + userDTO.getLdapUser().getFirstName() + " ***");

    MetisDashboardPage metisDashboardPage = new MetisDashboardPage(userDTO);

    ModelAndView modelAndView = new ModelAndView("templates/Pandora/Metis-Dashboard");
    modelAndView.addAllObjects(metisDashboardPage.buildModel());
//    System.out.println(MetisMappingUtil.toJson(modelAndView.getModel()));
    return modelAndView;
  }

//  @RequestMapping(value = "/profile", method = RequestMethod.GET)
//  public ModelAndView profile(Model model) throws JsonProcessingException {
//    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//    String primaryKey =
//        principal instanceof LdapUserDetailsImpl ? ((LdapUserDetailsImpl) principal).getUsername()
//            : null;
//    UserDTO userDTO = userService.getUser(primaryKey);
//    UserProfile userProfile = new UserProfile();
//    userProfile.init(userDTO);
//    LOGGER.info("*** User profile opened: " + userProfile.getFirstName() + " ***");
//
//    ModelAndView modelAndView = new ModelAndView("molecules/pandora/user-profile.mustache");
//    MetisLandingPage metisLandingPage = new MetisLandingPage(PageView.PROFILE, userProfile);
//    metisLandingPage.buildOrganizationsList(buildAvailableOrganizationsList());
//    modelAndView.addAllObjects(metisLandingPage.buildModel());
//    return modelAndView;
//  }

//  @RequestMapping(value = "/profile", method = RequestMethod.POST)
//  public ModelAndView updateUserProfile(@ModelAttribute UserProfile user, BindingResult result,
//      Model model) {
//    model.addAttribute("user", user);
//    ModelAndView modelAndView = new ModelAndView("templates/Pandora/Metis-Homepage");
//    MetisLandingPage metisLandingPage = new MetisLandingPage(PageView.PROFILE, user);
//    UserDTO userDTO = userService.getUser(user.getEmail());
//    if (userDTO.notNullUser()) {
//      //update user in LDAP
//      LdapUser ldapLdapUser = userDTO.getLdapUser();
//      ldapLdapUser.setFirstName(user.getFirstName());
//      ldapLdapUser.setLastName(user.getLastName());
//      ldapLdapUser.setPassword(user.getPassword());
//      userDTO.setLdapUser(ldapLdapUser);
//
//      //update user in database
//      User mongoUser = userDTO.getUser();
//      if (mongoUser == null) {
//        mongoUser = new User();
//        mongoUser.setId(new ObjectId());
//      }
//      mongoUser.setEmail(user.getEmail());
//      mongoUser.setCountry(Country.toCountry(user.getCountry()));
//      mongoUser.setSkypeId(user.getSkype());
//      mongoUser.setOrganizationRoles(resolveUserOrganizationRoles(user, mongoUser));
//      userDTO.setUser(mongoUser);
//    } else {
//      LOGGER.error("User " + user.getEmail() + " not found!");
//    }
//    userService.updateUserFromDTO(userDTO);
//    LOGGER.info("*** User updated: " + user.getFirstName() + " ***");
//
//    modelAndView.addAllObjects(metisLandingPage.buildModel());
//    return modelAndView;
//  }
//
//  @RequestMapping(value = "/profile", method = RequestMethod.GET, params = "userId")
//  public ModelAndView requestApproveUser(String userId, Model model) {
//    LOGGER.info("User Profile To Approve: " + userId);
//    //TODO
//    User userByID = userService.getUserByRequestID(userId);
//    UserDTO userDTO = userService.getUser(userByID.getEmail());
//    UserProfile userProfile = new UserProfile();
//    userProfile.init(userDTO);
//    LOGGER.info("*** User profile opened: " + userProfile.getFirstName() + " ***");
//
//    ModelAndView modelAndView = new ModelAndView("templates/Pandora/Metis-Homepage");
//    MetisLandingPage metisLandingPage = new MetisLandingPage(PageView.USER_APPROVE, userProfile);
//    metisLandingPage.buildOrganizationsList(buildAvailableOrganizationsList());
//    modelAndView.addAllObjects(metisLandingPage.buildModel());
//    return modelAndView;
//  }
//
//  @RequestMapping(value = "/profile", method = RequestMethod.POST, params = "userId")
//  public void requestValidateUser(@ModelAttribute UserProfile user, Model model, String userId) {
//    model.addAttribute("user", user);
//    UserDTO userDTO = userService.getUser(user.getEmail());
//    if (user != null && userDTO != null) {
//      //update user in LDAP
//      LdapUser ldapLdapUser = userDTO.getLdapUser();
//      if (ldapLdapUser != null) {
//        ldapLdapUser.setFirstName(user.getFirstName());
//        ldapLdapUser.setLastName(user.getLastName());
//        ldapLdapUser.setPassword(user.getPassword());
//      } else {
//        LOGGER.error("ERROR: LDAP User " + user.getEmail() + " not found!");
//      }
//      userDTO.setLdapUser(ldapLdapUser);
//
//      //update user in database
//      User mongoUser = userDTO.getUser();
//      if (mongoUser == null) {
//        mongoUser = new User();
//        mongoUser.setId(new ObjectId());
//      }
//      mongoUser.setEmail(user.getEmail());
//      mongoUser.setCountry(Country.toCountry(user.getCountry()));
//      mongoUser.setSkypeId(user.getSkype());
//      mongoUser.setOrganizationRoles(resolveUserOrganizationRoles(user, mongoUser));
//      userDTO.setUser(mongoUser);
//    }
//  }

  @RequestMapping(value = "/requests", method = RequestMethod.GET)
  public ModelAndView userRequests() {
    List<RoleRequest> roleRequests = userService.getAllRequests(null, null);
    RequestsLandingPage metisLandingPage = new RequestsLandingPage(roleRequests);

    ModelAndView modelAndView = new ModelAndView("templates/Pandora/Metis-Homepage");
    modelAndView.addAllObjects(metisLandingPage.buildModel());
    return modelAndView;
  }

//  //TODO the method implementation is not finished yet
//  @ResponseBody
//  @RequestMapping(value = "/profile/suggestOrganizations", method = RequestMethod.GET, params = "searchTerm")
//  public String suggestOrganizationsViaAjax(@RequestBody String response, String searchTerm) {
//    String result = "";
//    List<Organization> orgs = suggestOrganizations(searchTerm);
//    return result;
//  }

//  /**
//   * Method retrieves the list of organizations from Zoho.
//   * TODO: return the filtered list of organizations (only the organizations of specific type).
//   */
//  private List<String> buildAvailableOrganizationsList() {
//    List<String> organizations = new ArrayList<>();
//    try {
//      List<OrganizationRole> roles = Arrays
//          .asList(OrganizationRole.DATA_AGGREGATOR, OrganizationRole.CONTENT_PROVIDER,
//              OrganizationRole.DIRECT_PROVIDER,
//              OrganizationRole.EUROPEANA);
//      List<Organization> organizationsByRoles = dsOrgRestClient.getAllOrganizationsByRoles(roles);
//      if (organizationsByRoles != null && !organizationsByRoles.isEmpty()) {
//        for (Organization o : organizationsByRoles) {
//          organizations.add(o.getName());
//        }
//      }
//    } catch (ServerException e) {
//      LOGGER.error("ERROR: *** Zoho server exception: " + e.getMessage() + " ***");
//    } catch (Exception e) {
//      LOGGER.error("ERROR: *** CMS exception: " + e.getMessage() + " ***");
//    }
//    return organizations;
//  }

  private List<Organization> suggestOrganizations(String term) {
    List<Organization> suggestedOrganizations = new ArrayList<>();
    try {
      List<OrganizationSearchBean> suggestOrganizations = dsOrgRestClient
          .suggestOrganizations(term);
      for (OrganizationSearchBean searchBean : suggestOrganizations) {
        Organization orgById = dsOrgRestClient
            .getOrganizationByOrganizationId(searchBean.getId());
        if (orgById != null) {
          suggestedOrganizations.add(orgById);
        }
      }
    } catch (ServerException e) {
      LOGGER.error(e.getMessage());
    }
    return suggestedOrganizations;
  }

//  /**
//   * Method creates user role requests and resolves the new list of organization roles.
//   */
//  private List<UserOrganizationRole> resolveUserOrganizationRoles(
//      UserDTO user, User mongoUser) {
//    List<UserOrganizationRole> oldUserOrganizationRoles = mongoUser
//        .getUserOrganizationRoles();
//    List<String> newOrganizationsList = resolveUserOrganizations(user);
//    List<String> oldOrganizationsList = new ArrayList<>();
//    if (oldUserOrganizationRoles != null && !oldUserOrganizationRoles.isEmpty()) {
//      for (UserOrganizationRole o : oldUserOrganizationRoles) {
//        oldOrganizationsList.add(o.getOrganizationId());
//      }
//    }
//    @SuppressWarnings("unchecked")
//    Collection<String> organizationsToKeepUnchanged = CollectionUtils
//        .intersection(oldOrganizationsList, newOrganizationsList);
//    @SuppressWarnings("unchecked")
//    Collection<String> organizationsToDelete = CollectionUtils
//        .subtract(oldOrganizationsList, newOrganizationsList);
//    @SuppressWarnings("unchecked")
//    Collection<String> organizationsToAdd = CollectionUtils
//        .subtract(newOrganizationsList, oldOrganizationsList);
//    if (!organizationsToAdd.isEmpty() || !organizationsToDelete.isEmpty()) {
//      for (String organization : organizationsToAdd) {
//        userService.createRequest(mongoUser.getEmail(), organization, false);
//      }
//      for (String organization : organizationsToDelete) {
//        userService.createRequest(mongoUser.getEmail(), organization, true);
//      }
//      sendEmailNotifications(user);
//    }
//
//    List<UserOrganizationRole> newUserOrganizationRoles = new ArrayList<>();
//    for (String organization : organizationsToKeepUnchanged) {
//      for (UserOrganizationRole userOrganizationRole : oldUserOrganizationRoles) {
//        if (userOrganizationRole.getOrganizationId().equals(organization)) {
//          newUserOrganizationRoles.add(userOrganizationRole);
//        }
//      }
//    }
//    for (String organization : organizationsToAdd) {
//      UserOrganizationRole or = new UserOrganizationRole();
//      or.setOrganizationId(organization);
//      or.setRole(null);
//      newUserOrganizationRoles.add(or);
//    }
//    return newUserOrganizationRoles;
//  }

  /**
   * Method sends email notifications for user role request.
   */
  private void sendEmailNotifications(UserDTO user) {
    String userFullName = user.getLdapUser().getFirstName();
    String userLastName = user.getLdapUser().getLastName();
    String userEmail = user.getLdapUser().getEmail();
    List<String> allAdminUsers = userService.getAllAdminUsers();

    //all Europeana admins notification
    simpleMailMessage.setSubject(
        MetisMailType.ADMIN_ROLE_REQUEST_PENDING.getMailSubject(userFullName, userLastName));
    simpleMailMessage
        .setText(MetisMailType.ADMIN_ROLE_REQUEST_PENDING.getMailText(userFullName, userLastName));
    simpleMailMessage.setTo(allAdminUsers.toArray(new String[allAdminUsers.size()]));
    try {
      javaMailSender.send(simpleMailMessage);
      LOGGER.info(
          "*** Email notification for the role request pending been sent to all Metis Europeana admins ***");
    } catch (MailSendException e) {
      LOGGER.warn("*** The email address of one or more Europeana admins is incorrect! ***");
    } catch (Exception e) {
      LOGGER.error(e.getMessage());
    }

    //user notification
    simpleMailMessage.setSubject(
        MetisMailType.USER_ROLE_REQUEST_PENDING.getMailSubject(userFullName, userLastName));
    simpleMailMessage
        .setText(MetisMailType.USER_ROLE_REQUEST_PENDING.getMailText(userFullName, userLastName));
    simpleMailMessage.setTo(userEmail);
    try {
      javaMailSender.send(simpleMailMessage);
      LOGGER.info(
          "*** Email notification for the role request pending been sent to the user: " + userEmail
              + " ***");
    } catch (MailSendException e) {
      LOGGER.warn("*** The email address of the user is incorrect! ***");
    }
  }

//  /**
//   * Method resolves the new organization list that user belongs to.
//   */
//  private List<String> resolveUserOrganizations(UserDTO user) {
//    String orgs = user.getUser()..getOrganization();
//    List<String> organizations = new ArrayList<>();
//    if (orgs != null) {
//      String[] split = orgs.split(",");
//      for (int i = 0; i < split.length; i++) {
//        split[i] = split[i].trim();
//      }
//      organizations.addAll(Arrays.asList(split));
//    }
//    return organizations;
//  }
}
