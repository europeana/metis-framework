package eu.europeana.metis.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.europeana.metis.common.UpdateUserProfileRequest;
import eu.europeana.metis.core.common.Country;
import eu.europeana.metis.page.MetisPageFactory;
import eu.europeana.metis.page.ProfileLandingPage;
import eu.europeana.metis.ui.mongo.domain.UserDTO;
import eu.europeana.metis.ui.mongo.domain.UserOrganizationRole;
import eu.europeana.metis.ui.mongo.service.UserService;
import java.util.ArrayList;
import java.util.Calendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.ldap.userdetails.LdapUserDetailsImpl;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-06-20
 */
@Controller
public class MetisProfilePageController {
  private static final Logger LOGGER = LoggerFactory.getLogger(MetisUserPageController.class);

  private final UserService userService;
  private final MetisPageFactory pageFactory;

  @Autowired
  public MetisProfilePageController(UserService userService, MetisPageFactory pageFactory) {
    this.userService = userService;
    this.pageFactory =  pageFactory;
  }

  @RequestMapping(value = "/profile", method = RequestMethod.GET)
  public ModelAndView profile(Model model) {
    UserDTO userDTO = getAuthenticatedUser();

    ProfileLandingPage profileLandingPage = pageFactory.createProfileLandingPage(userDTO);

    ModelAndView modelAndView = new ModelAndView("templates/Pandora/Metis-Homepage");
    modelAndView.addAllObjects(profileLandingPage.buildModel());
    return modelAndView;
  }

  @RequestMapping(value = "/profile", method = RequestMethod.POST)
  public ModelAndView updateProfile(@ModelAttribute UpdateUserProfileRequest updateUserProfileRequest, Model model) throws JsonProcessingException {
    UserDTO userDTO = getAuthenticatedUser();

    if (userDTO.getLdapUser() != null ) {
      userDTO.getLdapUser().setEmail(updateUserProfileRequest.getUserEmail());
      userDTO.getLdapUser().setFirstName(updateUserProfileRequest.getUserFirstName());
      userDTO.getLdapUser().setLastName(updateUserProfileRequest.getUserLastName());
    }
    if (userDTO.getUser() != null ) {
      userDTO.getUser()
          .setCountry(Country.getCountryFromName(updateUserProfileRequest.getCountry()));
      userDTO.getUser().setEmail(updateUserProfileRequest.getUserEmail());
      userDTO.getUser().setSkypeId(updateUserProfileRequest.getUserSkype());
      userDTO.getUser().setNotes(updateUserProfileRequest.getNotes());
      userDTO.getUser().setModified(Calendar.getInstance().getTime());
      userDTO.getUser().setUserOrganizationRoles(new ArrayList<UserOrganizationRole>());
    }
    userService.updateUserFromDTO(userDTO);

    ProfileLandingPage profileLandingPage = pageFactory.createProfileLandingPage(userDTO);

    ModelAndView modelAndView = new ModelAndView("templates/Pandora/Metis-Homepage");
    modelAndView.addAllObjects(profileLandingPage.buildModel());

    return modelAndView;
  }

  private UserDTO getAuthenticatedUser() {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    String primaryKey =
        principal instanceof LdapUserDetailsImpl ? ((LdapUserDetailsImpl) principal).getUsername()
            : null;
    UserDTO userDTO = userService.getUser(primaryKey);
    LOGGER.info("User profile opened: %s", userDTO.getLdapUser().getFirstName());
    return userDTO;
  }

//  private List<Organization> buildAvailableOrganizationsList() {
////    List<String> organizations = new ArrayList<>();
//    try {
//      List<OrganizationRole> roles = Arrays
//          .asList(OrganizationRole.DATA_AGGREGATOR, OrganizationRole.CONTENT_PROVIDER,
//              OrganizationRole.DIRECT_PROVIDER,
//              OrganizationRole.EUROPEANA);
//      List<Organization> organizationsByRoles = dsOrgRestClient.getAllOrganizationsByRoles(roles);
////      if (organizationsByRoles != null && !organizationsByRoles.isEmpty()) {
////        for (Organization o : organizationsByRoles) {
////          organizations.add(o.getName());
////        }
////      }
//      return organizationsByRoles;
//    } catch (ServerException e) {
//      LOGGER.error("ERROR: *** Zoho server exception: %s", e.getMessage());
//    } catch (Exception e) {
//      LOGGER.error("ERROR: *** CMS exception: ", e.getMessage());
//    }
//    return null;
//  }

}
