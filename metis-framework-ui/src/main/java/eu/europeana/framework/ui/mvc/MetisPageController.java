package eu.europeana.framework.ui.mvc;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import eu.europeana.metis.framework.dataset.Dataset;

@Controller
public class MetisPageController {
	
    @RequestMapping(value="/metis", method=RequestMethod.GET)
    public String addDatasetForm(Model model) {
        model.addAttribute("metis", new Dataset());
        return "metis";
    }

    @RequestMapping(value="/metis", method=RequestMethod.POST)
    public String addDatasetSubmit(@ModelAttribute Dataset dataset, Model model) {
        model.addAttribute("dataset", dataset);
        System.out.println(dataset.getName());
        return "result";
    }
}
