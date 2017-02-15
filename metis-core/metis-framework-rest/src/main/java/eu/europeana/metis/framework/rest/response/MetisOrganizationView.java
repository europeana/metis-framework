package eu.europeana.metis.framework.rest.response;


import eu.europeana.metis.framework.organization.Organization;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by ymamakis on 11/11/16.
 */
public class MetisOrganizationView extends PublicOrganizationView{

    public static ModelAndView generateResponse(Organization organization) throws InstantiationException, IllegalAccessException {

        /**
         * private String organizationId;
         private Contact mainContact;
         private String technicalContact;
         private Address address;
         private boolean dea;
         private boolean deaSent;
         private boolean deaSigned;
         private String deaNotes;
         private HarvestingMetadata metadata;
          */
        ModelAndView view = PublicOrganizationView.generateResponse(organization);
        view.addObject("dea",organization.isDea());
        view.addObject("metadata",organization.getHarvestingMetadata());
        view.addObject("organizationId",organization.getOrganizationId());

        return view;
    }
}
