package eu.europeana.metis.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.europeana.metis.page.MetisLandingPage;
import eu.europeana.metis.page.ProfileLandingPage;
import eu.europeana.metis.ui.mongo.domain.UserDTO;
import eu.europeana.metis.ui.mongo.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.ldap.userdetails.LdapUserDetailsImpl;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-06-20
 */
@Controller
public class MetisProfilePageController {
  private final Logger LOGGER = LoggerFactory.getLogger(MetisUserPageController.class);

  private final UserService userService;

  @Autowired
  public MetisProfilePageController(UserService userService) {
    this.userService = userService;
  }

  @RequestMapping(value = "/profile", method = RequestMethod.GET)
  public ModelAndView profile(Model model) throws JsonProcessingException {
    UserDTO userDTO = getAuthenticatedUser();

    MetisLandingPage metisLandingPage = new ProfileLandingPage(userDTO);

    ModelAndView modelAndView = new ModelAndView("templates/Pandora/Metis-Homepage");
    modelAndView.addAllObjects(metisLandingPage.buildModel());
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
