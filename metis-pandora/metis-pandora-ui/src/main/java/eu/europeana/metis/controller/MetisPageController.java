package eu.europeana.metis.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import eu.europeana.metis.common.UserProfile;
import eu.europeana.metis.framework.common.Country;
import eu.europeana.metis.framework.organization.Organization;
import eu.europeana.metis.framework.rest.client.DsOrgRestClient;
import eu.europeana.metis.framework.rest.client.ServerException;
import eu.europeana.metis.mail.notification.MetisMailType;
import eu.europeana.metis.page.AllDatasetsPage;
import eu.europeana.metis.page.MappingToEdmPage;
import eu.europeana.metis.page.MetisLandingPage;
import eu.europeana.metis.page.NewDatasetPage;
import eu.europeana.metis.page.PageView;
import eu.europeana.metis.ui.ldap.domain.User;
import eu.europeana.metis.ui.mongo.domain.DBUser;
import eu.europeana.metis.ui.mongo.domain.OrganizationRole;
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
	private DsOrgRestClient dsOrgRestClient;
	
	@Autowired
	private JavaMailSender javaMailSender;

	@Autowired
	private SimpleMailMessage simpleMailMessage;
	
	/**
	 * View resolves Mapping_To_EDM page.
	 * @return
	 */
	@RequestMapping(value = "/mappings-page")
    public ModelAndView mappingsPage() {
        ModelAndView modelAndView = new ModelAndView("templates/Pandora/Mapping-To-EDM");
        modelAndView.addAllObjects((new MappingToEdmPage()).buildModel());
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
    
    /**
     * View resolves Metis Login page.
     * @param model
     * @return
     */
    @RequestMapping(value = "/login")
    public ModelAndView login(HttpServletRequest request, Model model) {
        ModelAndView modelAndView = new ModelAndView("templates/Pandora/Metis-Page");
        String email = request.getParameter("email");
        UserDTO userDTO = userService.getUser(email);
        UserProfile userProfile = new UserProfile();
        userProfile.init(userDTO);
        MetisLandingPage metisLandingPage = new MetisLandingPage(PageView.LOGIN, userProfile);
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
    public ModelAndView registerUser(@ModelAttribute UserProfile user, Model model) { 
    	model.addAttribute("user", user);
    	ModelAndView modelAndView = new ModelAndView("templates/Pandora/Metis-Page");
    	MetisLandingPage metisLandingPage = new MetisLandingPage(PageView.REGISTER);
    	UserDTO userDTO = userService.getUser(user.getEmail());
		User userFound = userDTO != null ? userDTO.getUser() : null;
    	if (userFound != null) {
            metisLandingPage.setIsDuplicateUser(true);
            modelAndView.addAllObjects(metisLandingPage.buildModel());
    		return modelAndView;
    	}
    	userService.createLdapUser(user);
    	logger.info("*** User created: " + user.getFullName() + " ***");
    	
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
    	String primaryKey = principal instanceof LdapUserDetailsImpl ? ((LdapUserDetailsImpl)principal).getUsername() : null;
    	UserDTO userDTO = userService.getUser(primaryKey);
    	UserProfile userProfile = new UserProfile();
    	userProfile.init(userDTO);
    	ModelAndView modelAndView = new ModelAndView("templates/Pandora/Metis-Page");
    	MetisLandingPage metisLandingPage = new MetisLandingPage(PageView.PROFILE, userProfile);
    	
    	logger.info("*** User profile: " + userProfile.getFullName() + " ***");	
		modelAndView.addAllObjects(metisLandingPage.buildModel());
		List<String> organizations = new ArrayList<>();
		try {
			List<Organization> allOrganizations = dsOrgRestClient.getAllOrganizations();
			if (allOrganizations != null && !allOrganizations.isEmpty()) {
				for (Organization o : allOrganizations) {
					organizations.add(o.getName());
				}
			}
		} catch (ServerException e) {
			logger.error("ERROR: *** Zoho server exception: " + e.getMessage() + " ***");
		} catch (Exception e) {
			logger.error("ERROR: *** CMS exception: " + e.getMessage() + " ***");
		}
		metisLandingPage.buildOrganizationsList(organizations);
//		System.out.println(MetisMappingUtil.toJson(modelAndView.getModel()));
    	return modelAndView;
    }
    
    @RequestMapping(value = "/profile", method=RequestMethod.POST)
    public ModelAndView updateUser(@ModelAttribute UserProfile user, BindingResult result, Model model) { 
    	model.addAttribute("user", user);
    	ModelAndView modelAndView = new ModelAndView("templates/Pandora/Metis-Page");
    	MetisLandingPage metisLandingPage = new MetisLandingPage(PageView.PROFILE, user);
    	UserDTO userDTO = userService.getUser(user.getEmail());
    	if (user != null && userDTO != null) {
    		User ldapUser = userDTO.getUser();
    		if (ldapUser != null) {
    			ldapUser.setFullName(user.getFullName());
    			ldapUser.setLastName(user.getLastName());
    			ldapUser.setPassword(user.getPassword());
    		}	
    		userDTO.setUser(ldapUser);		

    		DBUser dbUser = userDTO.getDbUser();
    		if (dbUser == null) {
    			dbUser = new DBUser();
    			dbUser.setId(new ObjectId());;
    			dbUser.setEmail(user.getEmail());
    		}
    		String orgs = user.getOrganization();
    		List<String> organizations = new ArrayList<>();
    		if (orgs != null) {
    			String[] split = orgs.split(",");
    			for (int i = 0; i < split.length; i++) {
    				split[i] = split[i].trim();    				
    			}
    			organizations.addAll(Arrays.asList(split));
    		}
    		List<String> dbUserOrganizations = new ArrayList<>();
    		List<OrganizationRole> organizationRoles = dbUser.getOrganizationRoles();
    		if (organizationRoles != null) {
    			for (OrganizationRole or: organizationRoles) {
    				String organizationId = or.getOrganizationId();
    				if (organizationId != null && !organizationId.isEmpty()) {
    					dbUserOrganizations.add(organizationId);    				
    				}
    			}    			
    		}
			// if the list of organization is changed in the user profile we
			// create a role request for organizations that are added and create
			// a deletion request for the organizations that are removed from
			// the user profile
    		if (!dbUserOrganizations.isEmpty() || !organizations.isEmpty()) {
    			if (dbUserOrganizations.size() != organizations.size() || !CollectionUtils.disjunction(organizations, dbUserOrganizations).isEmpty()) {
    				for (Object org: CollectionUtils.subtract(organizations, dbUserOrganizations)) {
    					userService.createRequest(dbUser.getEmail(), (String)org, false);
    				}
    				for (Object org: CollectionUtils.subtract(dbUserOrganizations, organizations)) {
    					userService.createRequest(dbUser.getEmail(), (String)org, true);
    				}
    				//mailing notification service
    				simpleMailMessage.setSubject(MetisMailType.ADMIN_ROLE_REQUEST_PENDING.getMailSubject(user.getFullName(), user.getLastName()));
    				simpleMailMessage.setText(MetisMailType.ADMIN_ROLE_REQUEST_PENDING.getMailText(user.getFullName(), user.getLastName()));

    				List<String> allAdminUsers = userService.getAllAdminUsers();
    				simpleMailMessage.setTo(allAdminUsers.toArray(new String[allAdminUsers.size()]));  
    				try {
    					javaMailSender.send(simpleMailMessage);
    					logger.info("*** Email notification for the role request pending been sent to all Metis Europeana admins ***");
       				} catch (MailSendException e) {
       					logger.warn("*** The email address of one or more Europeana admins is incorrect! ***");
       				} catch (Exception e) {
       					logger.error(e.getMessage());
       				}

    				simpleMailMessage.setSubject(MetisMailType.USER_ROLE_REQUEST_PENDING.getMailSubject(user.getFullName(), user.getLastName()));
    				simpleMailMessage.setText(MetisMailType.USER_ROLE_REQUEST_PENDING.getMailText(user.getFullName(), user.getLastName()));
    				simpleMailMessage.setTo(user.getEmail());
    				try {
    					javaMailSender.send(simpleMailMessage);
    				} catch (MailSendException e) {
       					logger.warn("*** The email address of the user is incorrect! ***");
       				}
    				logger.info("*** Email notification for the role request pending been sent to the user: " + user.getEmail() + " ***");
    			}    			
    		}
    		//dbUser.setOrganizations(organizations);
    		dbUser.setCountry(Country.toCountry(user.getCountry()));
    		dbUser.setSkypeId(user.getSkype());
    		userDTO.setDbUser(dbUser);
    	}
    	userService.updateUserFromDTO(userDTO);
    	logger.info("*** User updated: " + user.getFullName() + " ***");
     	    	
		modelAndView.addAllObjects(metisLandingPage.buildModel());
    	return modelAndView;
    }
}