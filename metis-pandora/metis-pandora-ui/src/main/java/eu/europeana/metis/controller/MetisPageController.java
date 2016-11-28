package eu.europeana.metis.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.ldap.userdetails.LdapUserDetailsImpl;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import eu.europeana.metis.mapping.util.MetisMappingUtil;
import eu.europeana.metis.page.AllDatasetsPage;
import eu.europeana.metis.page.MappingToEdmPage;
import eu.europeana.metis.page.MetisLandingPage;
import eu.europeana.metis.page.NewDatasetPage;
import eu.europeana.metis.page.PageView;
import eu.europeana.metis.ui.ldap.dao.UserDao;
import eu.europeana.metis.ui.ldap.domain.User;

/**
 * 
 * @author alena
 *
 */
@Controller
public class MetisPageController {
	
	final static Logger logger = Logger.getLogger(MetisPageController.class);
	
	@Autowired
    private UserDao userDao;
	
//	@Autowired
//	private LdapUserDetailsManager ldapUserDetailsManager;
	
	/**
	 * View resolves Mapping_To_EDM page.
	 * @return
	 */
	@RequestMapping(value = "/mappings-page")
    public ModelAndView mappingsPage() {
        ModelAndView modelAndView = new ModelAndView("templates/Pandora/Mapping-To-EDM");
        modelAndView.addAllObjects((new MappingToEdmPage()).buildModel());
//        System.out.println(MetisMappingUtil.toJson(modelAndView.getModel()));
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
    	User user = userDao.findByPrimaryKey(principal instanceof LdapUserDetailsImpl ? ((LdapUserDetailsImpl)principal).getUsername() : null);
		ModelAndView modelAndView = new ModelAndView("templates/Pandora/Metis-Page");
		MetisLandingPage metisLandingPage = new MetisLandingPage(PageView.EMPTY, user);
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
    	List<User> users = userDao.findAll();
        logger.info("*** All the users found: " + users + " ***");
        ModelAndView modelAndView = new ModelAndView("templates/Pandora/Metis-Page");
        modelAndView.addAllObjects((new MetisLandingPage(PageView.EMPTY)).buildModel());
        return modelAndView;
    }
    
    /**
     * View resolves Metis Login page.
     * @param model
     * @return
     */
    @RequestMapping(value = "/login")
    public ModelAndView login(HttpServletRequest request, Model model) {
        ModelAndView modelAndView = new ModelAndView("templates/Pandora/Metis-Page");
        String email = request.getParameter("email");
        MetisLandingPage metisLandingPage = new MetisLandingPage(PageView.LOGIN, userDao.findByPrimaryKey(email));

        String authError = request.getParameter("authentication_error");
        if (authError != null && authError.equals("true")) {
        	metisLandingPage.setIsAuthError(true);     	
        }
		modelAndView.addAllObjects(metisLandingPage.buildModel());
        return modelAndView;
    }
    
    /**
     * View resolves Metis Register page.
     * @param model
     * @return
     */
    @RequestMapping(value = "/register")
    public ModelAndView register(Model model) {
        ModelAndView modelAndView = new ModelAndView("templates/Pandora/Metis-Page");
        MetisLandingPage metisLandingPage = new MetisLandingPage(PageView.REGISTER);
		modelAndView.addAllObjects(metisLandingPage.buildModel());
        return modelAndView;
    }
    
    @RequestMapping(value = "/register", method=RequestMethod.POST)
    public ModelAndView registerUser(@ModelAttribute User user, Model model) { 
    	model.addAttribute("user", user);
    	ModelAndView modelAndView = new ModelAndView("templates/Pandora/Metis-Page");
    	MetisLandingPage metisLandingPage = new MetisLandingPage(PageView.REGISTER);
    	User userFound = userDao.findByPrimaryKey(user.getEmail());
    	if (userFound != null) {
            metisLandingPage.setIsDuplicateUser(true);
            modelAndView.addAllObjects(metisLandingPage.buildModel());
    		return modelAndView;
    	}
    	userDao.create(user);
    	logger.info("*** User created: " + user.getFullName() + " ***");
    	logger.info("*** User found: " + userDao.findByPrimaryKey(user.getEmail()) + " ***");
    	
		modelAndView.addAllObjects(metisLandingPage.buildModel());
		modelAndView.setViewName("redirect:login?email="+ user.getEmail());
    	return modelAndView;
    }
        
    @RequestMapping(value = "/logout")
    public ModelAndView logout(Model model) {
    	 ModelAndView modelAndView = new ModelAndView("templates/Pandora/Metis-Page");
         MetisLandingPage metisLandingPage = new MetisLandingPage(PageView.EMPTY);
         modelAndView.addAllObjects(metisLandingPage.buildModel());
         return modelAndView;
    }
    
    @RequestMapping(value = "/profile", method=RequestMethod.GET)
    public ModelAndView proflie(Model model) {
    	Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    	User user = userDao.findByPrimaryKey(principal instanceof LdapUserDetailsImpl ? ((LdapUserDetailsImpl)principal).getUsername() : null);
    	ModelAndView modelAndView = new ModelAndView("templates/Pandora/Metis-Page");
    	MetisLandingPage metisLandingPage = new MetisLandingPage(PageView.PROFILE, user);
    	
    	logger.info("*** User updated: " + user.getFullName() + " ***");	
		modelAndView.addAllObjects(metisLandingPage.buildModel());
//		System.out.println(MetisMappingUtil.toJson(modelAndView.getModel()));
    	return modelAndView;
    }
    
    @RequestMapping(value = "/profile", method=RequestMethod.POST)
    public ModelAndView updateUser(@ModelAttribute User user, Model model) { 
    	model.addAttribute("user", user);
    	ModelAndView modelAndView = new ModelAndView("templates/Pandora/Metis-Page");
    	MetisLandingPage metisLandingPage = new MetisLandingPage(PageView.PROFILE);
    	User userFound = userDao.findByPrimaryKey(user.getEmail());
    	if (userFound != null) {
    		userDao.update(user);
            modelAndView.addAllObjects(metisLandingPage.buildModel());
    		return modelAndView;
    	}
    	logger.info("*** User updated: " + user.getFullName() + " ***");
    	    	
		modelAndView.addAllObjects(metisLandingPage.buildModel());
		modelAndView.setViewName("redirect:login?email="+ user.getEmail());
    	return modelAndView;
    }
}