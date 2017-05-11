package eu.europeana.metis.core.rest.response;

import eu.europeana.metis.core.organization.Organization;
import org.springframework.web.servlet.ModelAndView;


/**
 * Created by ymamakis on 11/11/16.
 */
public class PublicOrganizationView{

    public  static ModelAndView generateResponse(Organization organization) throws IllegalAccessException, InstantiationException {
        ModelAndView view = new ModelAndView("json");
        view.addObject("id",organization.getId());
        view.addObject("scope",organization.getScope());
        view.addObject("language",organization.getLanguage());
        view.addObject("website",organization.getWebsite());
        view.addObject("acronym",organization.getAcronym());
        view.addObject("altLabel",organization.getAltLabel());
        view.addObject("description",organization.getDescription());
        view.addObject("domain",organization.getDomain());
        view.addObject("country",organization.getCountry());
        view.addObject("role",organization.getRoles());
        view.addObject("geographicLevel",organization.getGeographicLevel());
        view.addObject("logoLocation",organization.getLogoLocation());
        view.addObject("name",organization.getName());
        view.addObject("organizationUri",organization.getOrganizationUri());
        view.addObject("prefLabel",organization.getPrefLabel());
        view.addObject("sameAs",organization.getSameAs());
        view.addObject("sector",organization.getSector());
        return view;
    }
}
