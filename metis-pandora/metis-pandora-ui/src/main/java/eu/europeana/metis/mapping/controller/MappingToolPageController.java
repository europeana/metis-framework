package eu.europeana.metis.mapping.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import eu.europeana.metis.mapping.util.MetisMappingUtil;
import eu.europeana.metis.page.AllDatasetsPage;
import eu.europeana.metis.page.MappingToEdmPage;
import eu.europeana.metis.page.NewDatasetPage;

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
	@RequestMapping(value = "/home-page")
    public ModelAndView homePage() {
        ModelAndView modelAndView = new ModelAndView("templates/Pandora/Mapping-To-EDM");
        modelAndView.addAllObjects((new MappingToEdmPage()).buildModel());
//        System.out.println(MetisMappingUtil.toJson(modelAndView.getModel()));
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
        System.out.println(MetisMappingUtil.toJson(modelAndView.getModel()));
        return modelAndView;
    }

	/**
	 * View resolves All Datasets page.
	 * @return
	 */
	@RequestMapping(value = "/all-datasets-page")
    public ModelAndView allDatasetsPage() {
        ModelAndView modelAndView = new ModelAndView("templates/Pandora/All-Datasets-Page");
        modelAndView.addAllObjects((new AllDatasetsPage()).buildModel());
        System.out.println(MetisMappingUtil.toJson(modelAndView.getModel()));
        return modelAndView;
    }
}