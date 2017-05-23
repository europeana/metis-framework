package eu.europeana.metis.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.europeana.metis.core.common.Country;
import eu.europeana.metis.core.common.OrganizationRole;
import eu.europeana.metis.core.mail.notification.MetisMailType;
import eu.europeana.metis.core.organization.Organization;
import eu.europeana.metis.core.rest.client.DsOrgRestClient;
import eu.europeana.metis.core.rest.client.ServerException;
import eu.europeana.metis.core.search.common.OrganizationSearchBean;
import eu.europeana.metis.mapping.atoms.UserRequest;
import eu.europeana.metis.mapping.organisms.pandora.UserProfile;
import eu.europeana.metis.page.MetisDashboardPage;
import eu.europeana.metis.page.MetisLandingPage;
import eu.europeana.metis.page.PageView;
import eu.europeana.metis.ui.ldap.domain.User;
import eu.europeana.metis.ui.mongo.domain.DBUser;
import eu.europeana.metis.ui.mongo.domain.RoleRequest;
import eu.europeana.metis.ui.mongo.domain.UserDTO;
import eu.europeana.metis.ui.mongo.service.UserService;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.bson.types.ObjectId;
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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * Metis User related web pages controller.
 *
 * @author alena
 */
@Controller
public class MetisUserPageController {

  private final Logger LOGGER = LoggerFactory.getLogger(MetisUserPageController.class);

  @Autowired
  private UserService userService;

  @Autowired
  private DsOrgRestClient dsOrgRestClient;

  @Autowired
  private JavaMailSender javaMailSender;

  @Autowired
  private SimpleMailMessage simpleMailMessage;

  /**
   * Resolves user login page.
   */
  @RequestMapping(value = "/login")
  public ModelAndView login(@RequestParam(value = "authentication_error", required = false) boolean authentication_error)
      throws JsonProcessingException {
    ModelAndView modelAndView = new ModelAndView("templates/Pandora/Metis-Homepage");
    MetisLandingPage metisLandingPage = new MetisLandingPage(PageView.LOGIN);
    metisLandingPage.setIsAuthError(authentication_error);
    modelAndView.addAllObjects(metisLandingPage.buildModel());
//    System.out.println(MetisMappingUtil.toJson(modelAndView.getModel()));
    return modelAndView;
  }

  /**
   * Resolves user registration page.
   */
  @RequestMapping(value = "/register", method = RequestMethod.GET)
  public ModelAndView register() throws JsonProcessingException {
    ModelAndView modelAndView = new ModelAndView("templates/Pandora/Metis-Homepage");
    MetisLandingPage metisLandingPage = new MetisLandingPage(PageView.REGISTER);
    modelAndView.addAllObjects(metisLandingPage.buildModel());
    return modelAndView;
  }

  /**
   * Handles the user registration submission.
   */
  @RequestMapping(value = "/register", method = RequestMethod.POST)
  public ModelAndView registerUser(@ModelAttribute UserProfile user, Model model) {
    model.addAttribute("user", user);
    ModelAndView modelAndView = new ModelAndView("templates/Pandora/Metis-Homepage");
    MetisLandingPage metisLandingPage = new MetisLandingPage(PageView.REGISTER);
    UserDTO userDTO = userService.getUser(user.getEmail());
    User userFound = userDTO != null ? userDTO.getUser() : null;
    if (userFound != null) {
      metisLandingPage.setIsDuplicateUser(true);
      modelAndView.addAllObjects(metisLandingPage.buildModel());
      return modelAndView;
    }
    userService.createLdapUser(user);
    LOGGER.info("*** User created: " + user.getFirstName() + " ***");

    modelAndView.setViewName("redirect:/");
    return modelAndView;
  }

  @RequestMapping(value = "/dashboard")
  public ModelAndView dashboardPage() throws JsonProcessingException {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    String primaryKey =
        principal instanceof LdapUserDetailsImpl ? ((LdapUserDetailsImpl) principal).getUsername()
            : null;
    UserDTO userDTO = userService.getUser(primaryKey);
    UserProfile userProfile = new UserProfile();
    userProfile.init(userDTO);
    LOGGER.info("*** User profile opened: " + userProfile.getFirstName() + " ***");

    ModelAndView modelAndView = new ModelAndView("templates/Pandora/Metis-Dashboard");
    MetisDashboardPage metisDashboardPage = new MetisDashboardPage(userProfile);
    modelAndView.addAllObjects(metisDashboardPage.buildModel());
//    System.out.println(MetisMappingUtil.toJson(modelAndView.getModel()));
    return modelAndView;
  }

  /**
   * Resolves user profile page
   */
  @RequestMapping(value = "/profile", method = RequestMethod.GET)
  public ModelAndView profile(Model model) throws JsonProcessingException {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    String primaryKey =
        principal instanceof LdapUserDetailsImpl ? ((LdapUserDetailsImpl) principal).getUsername()
            : null;
    UserDTO userDTO = userService.getUser(primaryKey);
    UserProfile userProfile = new UserProfile();
    userProfile.init(userDTO);
    LOGGER.info("*** User profile opened: " + userProfile.getFirstName() + " ***");

    ModelAndView modelAndView = new ModelAndView("templates/Pandora/Metis-Homepage");
    MetisLandingPage metisLandingPage = new MetisLandingPage(PageView.PROFILE, userProfile);
    metisLandingPage.buildOrganizationsList(buildAvailableOrganizationsList());
    modelAndView.addAllObjects(metisLandingPage.buildModel());
    return modelAndView;
  }

  /**
   * Handles user profile update.
   */
  @RequestMapping(value = "/profile", method = RequestMethod.POST)
  public ModelAndView updateUser(@ModelAttribute UserProfile user, BindingResult result,
      Model model) {
    model.addAttribute("user", user);
    ModelAndView modelAndView = new ModelAndView("templates/Pandora/Metis-Homepage");
    MetisLandingPage metisLandingPage = new MetisLandingPage(PageView.PROFILE, user);
    UserDTO userDTO = userService.getUser(user.getEmail());
    if (user != null && userDTO != null) {
      //update user in LDAP
      User ldapUser = userDTO.getUser();
      if (ldapUser != null) {
        ldapUser.setFirstName(user.getFirstName());
        ldapUser.setLastName(user.getLastName());
        ldapUser.setPassword(user.getPassword());
      } else {
        LOGGER.error("ERROR: LDAP User " + user.getEmail() + " not found!");
      }
      userDTO.setUser(ldapUser);

      //update user in database
      DBUser dbUser = userDTO.getDbUser();
      if (dbUser == null) {
        dbUser = new DBUser();
        dbUser.setId(new ObjectId());
      }
      dbUser.setEmail(user.getEmail());
      dbUser.setCountry(Country.toCountry(user.getCountry()));
      dbUser.setSkypeId(user.getSkype());
      dbUser.setOrganizationRoles(resolveUserOrganizationRoles(user, dbUser));
      userDTO.setDbUser(dbUser);
    }
    userService.updateUserFromDTO(userDTO);
    LOGGER.info("*** User updated: " + user.getFirstName() + " ***");

    modelAndView.addAllObjects(metisLandingPage.buildModel());
    return modelAndView;
  }

  @RequestMapping(value = "/profile", method = RequestMethod.GET, params = "userId")
  public ModelAndView requestApproveUser(String userId, Model model) {
    LOGGER.info("User Profile To Approve: " + userId);
    //TODO
    DBUser userByID = userService.getUserByRequestID(userId);
    UserDTO userDTO = userService.getUser(userByID.getEmail());
    UserProfile userProfile = new UserProfile();
    userProfile.init(userDTO);
    LOGGER.info("*** User profile opened: " + userProfile.getFirstName() + " ***");

    ModelAndView modelAndView = new ModelAndView("templates/Pandora/Metis-Homepage");
    MetisLandingPage metisLandingPage = new MetisLandingPage(PageView.USER_APPROVE, userProfile);
    metisLandingPage.buildOrganizationsList(buildAvailableOrganizationsList());
    modelAndView.addAllObjects(metisLandingPage.buildModel());
    return modelAndView;
  }

  /**
   *
   * @param userId
   * @param model
   * @return
   */
  @RequestMapping(value = "/profile", method = RequestMethod.POST, params = "userId")
  public void requestValidateUser(@ModelAttribute UserProfile user, Model model, String userId) {
    model.addAttribute("user", user);
//    	ModelAndView modelAndView = new ModelAndView("templates/Pandora/Metis-Homepage");
//    	MetisLandingPage metisLandingPage = new MetisLandingPage(PageView.PROFILE, user);
    UserDTO userDTO = userService.getUser(user.getEmail());
    if (user != null && userDTO != null) {
      //update user in LDAP
      User ldapUser = userDTO.getUser();
      if (ldapUser != null) {
        ldapUser.setFirstName(user.getFirstName());
        ldapUser.setLastName(user.getLastName());
        ldapUser.setPassword(user.getPassword());
      } else {
        LOGGER.error("ERROR: LDAP User " + user.getEmail() + " not found!");
      }
      userDTO.setUser(ldapUser);

      //update user in database
      DBUser dbUser = userDTO.getDbUser();
      if (dbUser == null) {
        dbUser = new DBUser();
        dbUser.setId(new ObjectId());
      }
      dbUser.setEmail(user.getEmail());
      dbUser.setCountry(Country.toCountry(user.getCountry()));
      dbUser.setSkypeId(user.getSkype());
      dbUser.setOrganizationRoles(resolveUserOrganizationRoles(user, dbUser));
      userDTO.setDbUser(dbUser);
    }
//    	return null;
  }

  /**
   *
   * @param model
   * @return
   */
  @RequestMapping(value = "/requests", method = RequestMethod.GET)
  public ModelAndView userRequests(Model model) {
    ModelAndView modelAndView = new ModelAndView("templates/Pandora/Metis-Homepage");
    List<UserRequest> userRequestsList = new ArrayList<>();
    List<RoleRequest> allRequests = userService.getAllRequests(null, null);
    SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy hh:mm");
    for (RoleRequest rr : allRequests) {
      Date requestDate = rr.getRequestDate();
      userRequestsList
          .add(new UserRequest(rr.getId().toString(), rr.getUserId(), rr.getOrganizationId(),
              format.format(requestDate), rr.isDeleteRequest(), rr.getRequestStatus()));
    }
    MetisLandingPage metisLandingPage = new MetisLandingPage(PageView.REQUESTS, userRequestsList);
    modelAndView.addAllObjects(metisLandingPage.buildModel());
//        System.out.println(MetisMappingUtil.toJson(modelAndView.getModel()));
    return modelAndView;
  }

  //TODO the method implementation is not finished yet
  @ResponseBody
  @RequestMapping(value = "/profile/suggestOrganizations", method = RequestMethod.GET, params = "searchTerm")
  public String suggestOrganizationsViaAjax(@RequestBody String response, String searchTerm) {
    String result = "";
    //logic
    List<Organization> orgs = suggestOrganizations(searchTerm);
    //FIXME comment the line below
//    System.out.println(MetisMappingUtil.toJson(orgs));
    return result;
  }

  /**
   * Method retrieves the list of organizations from Zoho.
   * TODO: return the filtered list of organizations (only the organizations of specific type).
   */
  private List<String> buildAvailableOrganizationsList() {
    List<String> organizations = new ArrayList<>();
    try {
      List<OrganizationRole> roles = Arrays
          .asList(OrganizationRole.DATA_AGGREGATOR, OrganizationRole.CONTENT_PROVIDER, OrganizationRole.DIRECT_PROVIDER,
              OrganizationRole.EUROPEANA);
      List<Organization> organizationsByRoles = dsOrgRestClient.getAllOrganizationsByRoles(roles);
      if (organizationsByRoles != null && !organizationsByRoles.isEmpty()) {
        for (Organization o : organizationsByRoles) {
          organizations.add(o.getName());
        }
      }
    } catch (ServerException e) {
      LOGGER.error("ERROR: *** Zoho server exception: " + e.getMessage() + " ***");
    } catch (Exception e) {
      LOGGER.error("ERROR: *** CMS exception: " + e.getMessage() + " ***");
    }
    return organizations;
  }

  /**
   *
   * @param term
   * @return
   */
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

  /**
   * Method creates user role requests and resolves the new list of organization roles.
   */
  private List<eu.europeana.metis.ui.mongo.domain.OrganizationRole> resolveUserOrganizationRoles(UserProfile user, DBUser dbUser) {
    List<eu.europeana.metis.ui.mongo.domain.OrganizationRole> oldOrganizationRoles = dbUser.getOrganizationRoles();
    List<String> newOrganizationsList = resolveUserOrganizations(user);
    List<String> oldOrganizationsList = new ArrayList<>();
    if (oldOrganizationRoles != null && !oldOrganizationRoles.isEmpty()) {
      for (eu.europeana.metis.ui.mongo.domain.OrganizationRole o : oldOrganizationRoles) {
        oldOrganizationsList.add(o.getOrganizationId());
      }
    }
    @SuppressWarnings("unchecked")
    Collection<String> organizationsToKeepUnchanged = CollectionUtils
        .intersection(oldOrganizationsList, newOrganizationsList);
    @SuppressWarnings("unchecked")
    Collection<String> organizationsToDelete = CollectionUtils
        .subtract(oldOrganizationsList, newOrganizationsList);
    @SuppressWarnings("unchecked")
    Collection<String> organizationsToAdd = CollectionUtils
        .subtract(newOrganizationsList, oldOrganizationsList);
    if (!organizationsToAdd.isEmpty() || !organizationsToDelete.isEmpty()) {
      for (String organization : organizationsToAdd) {
        userService.createRequest(dbUser.getEmail(), organization, false);
      }
      for (String organization : organizationsToDelete) {
        userService.createRequest(dbUser.getEmail(), organization, true);
      }
      sendEmailNotifications(user);
    }

    List<eu.europeana.metis.ui.mongo.domain.OrganizationRole> newOrganizationRoles = new ArrayList<>();
    for (String organization : organizationsToKeepUnchanged) {
      for (eu.europeana.metis.ui.mongo.domain.OrganizationRole organizationRole : oldOrganizationRoles) {
        if (organizationRole.getOrganizationId().equals(organization)) {
          newOrganizationRoles.add(organizationRole);
        }
      }
    }
    for (String organization : organizationsToAdd) {
      eu.europeana.metis.ui.mongo.domain.OrganizationRole or = new eu.europeana.metis.ui.mongo.domain.OrganizationRole();
      or.setOrganizationId(organization);
      or.setRole(null);
      newOrganizationRoles.add(or);
    }
    return newOrganizationRoles;
  }

  /**
   * Method sends email notifications for user role request.
   */
  private void sendEmailNotifications(UserProfile user) {
    String userFullName = user.getFirstName();
    String userLastName = user.getLastName();
    String userEmail = user.getEmail();
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

  /**
   * Method resolves the new organization list that user belongs to.
   */
  private List<String> resolveUserOrganizations(UserProfile user) {
    String orgs = user.getOrganization();
    List<String> organizations = new ArrayList<>();
    if (orgs != null) {
      String[] split = orgs.split(",");
      for (int i = 0; i < split.length; i++) {
        split[i] = split[i].trim();
      }
      organizations.addAll(Arrays.asList(split));
    }
    return organizations;
  }
}
