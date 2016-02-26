package eu.europeana.framework.ui.mvc;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.beans.propertyeditors.CustomBooleanEditor;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import eu.europeana.framework.ui.mvc.wrapper.DatasetWrapper;
import eu.europeana.metis.framework.common.HarvestType;
import eu.europeana.metis.framework.common.HarvestingMetadata;
import eu.europeana.metis.framework.dataset.Country;
import eu.europeana.metis.framework.dataset.Dataset;
import eu.europeana.metis.framework.dataset.Language;
import eu.europeana.metis.framework.dataset.WorkflowStatus;

@Controller
public class MetisPageController {
	
    @RequestMapping(value="/metis", method=RequestMethod.GET)
    public ModelAndView addDatasetForm() {
       Dataset dataset = new Dataset();
       dataset.setWorkflowStatus(WorkflowStatus.CREATED);
       HarvestingMetadata metadata = new HarvestingMetadata();
       metadata.setHarvestType(HarvestType.UNSPECIFIED);
       dataset.setMetadata(metadata);
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
    public String addDatasetSubmit(@ModelAttribute DatasetWrapper dataset, Model model) {    	
        model.addAttribute("dataset", dataset);
        System.out.println(dataset.getName() + " " + dataset.getNotes() + " " + dataset.getCreated());
        return "result";
    }
    
    @RequestMapping(value="/login")
    public String login(Model model) {    	
        return "login";
    }
    
    @InitBinder
    public void initDateBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }
}
