package eu.europeana.metis.controller;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.ldap.userdetails.LdapUserDetailsImpl;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import eu.europeana.metis.common.UserProfile;
import eu.europeana.metis.page.AllDatasetsPage;
import eu.europeana.metis.page.MappingToEdmPage;
import eu.europeana.metis.page.MetisLandingPage;
import eu.europeana.metis.page.NewDatasetPage;
import eu.europeana.metis.page.PageView;
import eu.europeana.metis.service.MappingService;
import eu.europeana.metis.ui.mongo.domain.UserDTO;
import eu.europeana.metis.ui.mongo.service.UserService;

/**
 * Metis web pages controller.
 * @author alena
 *
 */
@Controller
public class MetisPageController {	
	final static Logger logger = Logger.getLogger(MetisPageController.class);
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private MappingService mappingService;
	
	/**
	 * View resolves Mapping_To_EDM page.
	 * @return
	 */
	@RequestMapping(value = "/mappings-page")
    public ModelAndView mappingsPage() {
        ModelAndView modelAndView = new ModelAndView("templates/Pandora/Mapping-To-EDM");
        MappingToEdmPage mappingToEdmPage = new MappingToEdmPage();
        mappingToEdmPage.setMappingService(mappingService);
		modelAndView.addAllObjects(mappingToEdmPage.buildModel());
//        System.out.println(MetisMappingUtil.toJson(MappingCard.buildMappingCardModel()));
        return modelAndView;
    }
	
	/**
	 * FIXME
	 * View resolves Mapping_To_EDM page.
	 * @return
	 */
	@RequestMapping(value = "/")
    public ModelAndView homePage() {
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String primaryKey = principal instanceof LdapUserDetailsImpl ? ((LdapUserDetailsImpl)principal).getUsername() : null;
		ModelAndView modelAndView = new ModelAndView("templates/Pandora/Metis-Page");
		UserDTO userDTO = userService.getUser(primaryKey);
		UserProfile userProfile =new UserProfile();
		userProfile.init(userDTO);
		MetisLandingPage metisLandingPage = new MetisLandingPage(PageView.EMPTY, userProfile);
		modelAndView.addAllObjects(metisLandingPage.buildModel());
		return modelAndView;
    }
	
	/**
	 * View resolves New Dataset page.
	 * @return
	 */
	@RequestMapping(value = "/new-dataset-page")
    public ModelAndView newDatasetPage() {
        ModelAndView modelAndView = new ModelAndView("templates/Pandora/New-Dataset-Page");
        modelAndView.addAllObjects((new NewDatasetPage()).buildModel());
//        System.out.println(MetisMappingUtil.toJson(modelAndView.getModel()));
        return modelAndView;
    }

	/**
	 * View resolves All Datasets page.
	 * @return
	 */
	@RequestMapping(value = "/all-datasets-page")
    public ModelAndView allDatasetsPage() {
		//TODO the All Datasets template doesn't exist anymore so for a sub the New Dataset template is used here
        ModelAndView modelAndView = new ModelAndView("templates/Pandora/New-Dataset-Page");
        modelAndView.addAllObjects((new AllDatasetsPage()).buildModel());
//        System.out.println(MetisMappingUtil.toJson(modelAndView.getModel()));
        return modelAndView;
    }
	
    /**
     * View resolves Metis Landing page.
     * @param model
     * @return
     */
    @RequestMapping(value = "/europeana-metis")
    public ModelAndView metis(Model model) {
        ModelAndView modelAndView = new ModelAndView("templates/Pandora/Metis-Page");
        modelAndView.addAllObjects((new MetisLandingPage(PageView.EMPTY)).buildModel());
        return modelAndView;
    }
}