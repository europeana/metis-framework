package eu.europeana.metis.ui.mvc;

import eu.europeana.metis.framework.common.Country;
import eu.europeana.metis.framework.common.HarvestType;
import eu.europeana.metis.framework.common.HarvestingMetadata;
import eu.europeana.metis.framework.common.Language;
import eu.europeana.metis.framework.dataset.Dataset;
import eu.europeana.metis.framework.dataset.WorkflowStatus;
import eu.europeana.metis.ui.ldap.dao.UserDao;
import eu.europeana.metis.ui.ldap.domain.User;
import eu.europeana.metis.ui.wrapper.DatasetWrapper;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.beans.PropertyEditorSupport;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Controller
public class MetisPageController {

	final static Logger logger = Logger.getLogger(MetisPageController.class);

	@Autowired
    private UserDao userDao;
	
    @RequestMapping(value="/metis", method=RequestMethod.GET)
    public ModelAndView addDatasetForm() {
       Dataset dataset = new Dataset();
       dataset.setWorkflowStatus(WorkflowStatus.CREATED);
       HarvestingMetadata metadata = new HarvestingMetadata();
       metadata.setHarvestType(HarvestType.UNSPECIFIED);
       dataset.setMetadata(metadata);
       Date dateCreated = new Date();
       dataset.setCreated(dateCreated);
       SimpleDateFormat format = new SimpleDateFormat("dd_MM_yyyy");
       dataset.setName(format.format(dateCreated) + "_001");
       ModelAndView modelAndView = new ModelAndView("metis", "command", new DatasetWrapper(dataset));
       
       final List<WorkflowStatus> workflowStatus = Arrays.asList(WorkflowStatus.values());
       modelAndView.addObject("workflowStatus", workflowStatus);
       
       final List<Country> country = Arrays.asList(Country.values());
       modelAndView.addObject("country", country);
       
       final List<Language> language = Arrays.asList(Language.values());
       modelAndView.addObject("language", language);
       
       final List<HarvestType> harvestType = Arrays.asList(HarvestType.values());
       modelAndView.addObject("harvestType", harvestType);       
       return modelAndView;
    }
    
    @RequestMapping(value="/metis", method=RequestMethod.POST)
    public String submitDataset(@ModelAttribute DatasetWrapper dataset, Model model) {    	
        model.addAttribute("dataset", dataset);
        String dataProvider = dataset.getDataProvider();
		if (dataProvider != null && !dataProvider.isEmpty()) {
        	dataset.setName(dataProvider + "_" + dataset.getName());        	
        }
		logger.info("Dataset created: " + dataset.getName() + " " + dataset.getNotes() + " " + dataset.getCreated());
        return "result";
    }
    
    @RequestMapping(value="/register", method=RequestMethod.GET)
    public ModelAndView registerUser() {    	
    	return new ModelAndView("register", "command", new User());
    }
    
    @RequestMapping(value="/register", method=RequestMethod.POST)
    public String submitUser(@ModelAttribute User user, Model model) { 
    	model.addAttribute("user", user);
    	//TODO add the validation for user duplication!
//    	User userFound = userDao.findByPrimaryKey(user.getEmail(), user.getFullName());
//    	if (userFound != null) {
//    		return "register?status=duplicate_user";
//    	}
    	userDao.create(user);
    	logger.info("*** User created: " + user.getFullName() + " ***");
    	logger.info("*** User found: " + userDao.findByPrimaryKey(user.getEmail(), user.getFullName()) + " ***");
    	return "login";

    }
    
    @RequestMapping(value="/login")
    public String login(Model model) {
    	List<User> users = userDao.findAll();

        logger.info("*** All the users found: " + users + " ***");

        for(User user:users){
            System.out.println(new String(user.getPasswordB()));
        }
        return "login";
    }
    
    @RequestMapping(value="/result")
    public String result(Model model) {
        return "result";
    }
    
    @RequestMapping(value="/logout")
    public String logout(Model model) {
        return "login";
    }


    
    @InitBinder
    public void initBinders(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
        binder.registerCustomEditor(Country.class, new PropertyEditorSupport() {
        	@Override
        	public void setAsText(String text) throws IllegalArgumentException {
        		setValue(Country.toCountry(text.toUpperCase().trim()));
        	}
        });
        binder.registerCustomEditor(Language.class, new PropertyEditorSupport() {
        	@Override
        	public void setAsText(String text) throws IllegalArgumentException {
        		setValue(Language.valueOf(text));
        	}
        });
    }
}
