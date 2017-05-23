package eu.europeana.metis.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.europeana.metis.mapping.organisms.pandora.UserProfile;
import eu.europeana.metis.mapping.util.MetisMappingUtil;
import eu.europeana.metis.page.MappingToEdmPage;
import eu.europeana.metis.page.MetisLandingPage;
import eu.europeana.metis.page.PageView;
import eu.europeana.metis.service.MappingService;
import eu.europeana.metis.ui.mongo.domain.UserDTO;
import eu.europeana.metis.ui.mongo.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.ldap.userdetails.LdapUserDetailsImpl;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Metis web pages controller.
 *
 * @author alena
 */
@Controller
public class MetisPageController {
  private final Logger LOGGER = LoggerFactory.getLogger(MetisPageController.class);

  @Autowired
  private UserService userService;

  @Autowired
  private MappingService mappingService;

  /**
   * View resolves Home page.
   */
  @RequestMapping(value = "/")
  public ModelAndView homePage() throws JsonProcessingException {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    String primaryKey =
        principal instanceof LdapUserDetailsImpl ? ((LdapUserDetailsImpl) principal).getUsername()
            : null;
    ModelAndView modelAndView = new ModelAndView("templates/Pandora/Metis-Homepage");
    UserDTO userDTO = userService.getUser(primaryKey);
    UserProfile userProfile = new UserProfile();
    userProfile.init(userDTO);
    MetisLandingPage metisLandingPage = new MetisLandingPage(PageView.HOME, userProfile);
    modelAndView.addAllObjects(metisLandingPage.buildModel());
    System.out.println(MetisMappingUtil.toJson(modelAndView.getModel()));
    return modelAndView;
  }

  /**
   * View resolves Mapping_To_EDM view. 	 *
   */
  //	FIXME this is used only for test reasons to show how the mapping is populated in a .mustache template. In
  //	the future there won't be a separate page for this view, there will be just a tab on Metis Dashboard.
  @RequestMapping(value = "/mappings-page")
  public ModelAndView mappingsPage() {
    ModelAndView modelAndView = new ModelAndView("templates/Pandora/Mapping-To-EDM");
    MappingToEdmPage mappingToEdmPage = new MappingToEdmPage();
    mappingToEdmPage.setMappingService(mappingService);
    modelAndView.addAllObjects(mappingToEdmPage.buildModel());
//        System.out.println(MetisMappingUtil.toJson(MappingCard.buildMappingCardModel()));
    return modelAndView;
  }
}