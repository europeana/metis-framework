package eu.europeana.metis.mapping.controller;

import eu.europeana.metis.mapping.model.MappingToolPageModel;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * 
 * @author alena
 *
 */
@Controller
public class MappingToolPageController {
    
	/**
	 * View resolves Mapping_To_EDM page.
	 * @return
	 */
	@RequestMapping(value = "/")
    public ModelAndView menu() {
        ModelAndView modelAndView = new ModelAndView("templates/Pandora/Mapping-To-EDM");
        modelAndView.addAllObjects((new MappingToolPageModel()).buildModel());
//        System.out.println(MetisMappingUtil.toJson(modelAndView.getModel()));
        return modelAndView;
    }
	
	
}